package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.publish.Controller;
import com.sos.joc.model.publish.DeployDelete;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployUpdate;
import com.sos.joc.model.publish.JSDeploymentState;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.PublishUtils;

@Path("publish")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./publish/deploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployImpl.class);
    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, DeployFilter deployFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        Boolean deployHasErrors = false;
        Map<String, String> mastersWithDeployErrors = new HashMap<String,String>();
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, deployFilter, xAccessToken, "",
                    getPermissonsJocCockpit("", xAccessToken).getJS7Controller().getAdministration().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            Set<Long> configurationIdsToDeploy = getConfigurationIdsToUpdateFromFilter(deployFilter);
            Set<Long> deploymentIdsToReDeploy = getDeploymentIdsToUpdateFromFilter(deployFilter);
            Set<Long> deploymentIdsToDeleteFromConfigIds = getDeploymentIdsToDeleteByConfigurationIdsFromFilter(deployFilter);
            Set<Long> deploymentIdsToDelete = getDeploymentIdsToDeleteFromFilter(deployFilter);

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToDeploy = 
                    dbLayer.getFilteredInventoryConfigurationsByIds(configurationIdsToDeploy);
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeploy = 
                    dbLayer.getFilteredDeploymentHistory(deploymentIdsToReDeploy);
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = 
                    dbLayer.getFilteredDeploymentHistory(deploymentIdsToDelete);
            
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts =
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> signedDeployments = 
                    new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
//            Set<DBItemInventoryConfiguration> unsignedDrafts = new HashSet<DBItemInventoryConfiguration>();
            for (DBItemInventoryConfiguration update : configurationDBItemsToDeploy) {
                List<DBItemDepSignatures> signatures = dbLayer.getSignatures(update.getId());
                DBItemDepSignatures latestSignature = null;
                if (signatures != null && !signatures.isEmpty()) {
                    Comparator<DBItemDepSignatures> comp = Comparator.comparingLong(sig -> sig.getModified().getTime());
                    DBItemDepSignatures first = signatures.stream().sorted(comp).findFirst().get();
                    DBItemDepSignatures last = signatures.stream().sorted(comp.reversed()).findFirst().get();
                    latestSignature = last;
                    signedDrafts.put(update, latestSignature);
                }
            }
            // TODO: re-deploy
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = 
                    new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            // versionId on objects will be removed
            // versionId on command stays
            // Clarify: Keep UUID as versionId?
            final String versionId = UUID.randomUUID().toString();
            final Date deploymentDate = Date.from(Instant.now());
            // only signed objects will be processed
            // existing signatures of objects are verified
            for(DBItemInventoryConfiguration draft : signedDrafts.keySet()) {
                verifiedConfigurations.put(PublishUtils.verifySignature(
                        account, draft, signedDrafts.get(draft), hibernateSession), signedDrafts.get(draft));
            }
            for (DBItemDeploymentHistory deployed : signedDeployments.keySet()) {
                verifiedReDeployables.put(PublishUtils.verifySignature(
                        account, deployed, signedDeployments.get(deployed), hibernateSession), signedDeployments.get(deployed));
            }

            // call UpdateRepo for all provided Controllers
            JSDeploymentState deploymentState = null;
            for (String controllerId : controllerIds) {
                try {
                    PublishUtils.updateRepo(
                            versionId, verifiedConfigurations, verifiedReDeployables, depHistoryDBItemsToDeployDelete, controllerId, dbLayer);
                    deploymentState = JSDeploymentState.DEPLOYED;
                    ClusterState clusterState = Globals.objectMapper.readValue(
                            Proxy.of(controllerId).currentState().clusterState().toJson(), ClusterState.class);
                    String activeClusterUri = clusterState.getIdToUri().getAdditionalProperties().get(clusterState.getActiveId());
                    List<DBItemInventoryJSInstance> controllerDBItems = Proxies.getControllerDbInstances().get(controllerId);
                    DBItemInventoryJSInstance activeClusterController = 
                            controllerDBItems.stream().filter(
                                    controller -> activeClusterUri.equals(controller.getClusterUri())).findFirst().get();
                    Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                            verifiedConfigurations, account, hibernateSession, versionId, activeClusterController.getId(), deploymentDate);
                    deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                            verifiedReDeployables, account, hibernateSession, versionId, activeClusterController.getId(), deploymentDate));
                    Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete, dbLayer);
                    PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), hibernateSession);
                    LOGGER.info(String.format("Deploy to Master \"%1$s\" was successful!",
                            controllerId));
                } catch (JobSchedulerBadRequestException e) {
                    String message = String.format(
                            "Response from Master \"%1$s:\": %2$s - %3$s", controllerId, e.getError().getCode(), e.getError().getMessage());
                    LOGGER.warn(message);
                    deploymentState = JSDeploymentState.NOT_DEPLOYED;
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(controllerId, e.getError().getMessage());
                    dbLayer.updateFailedDeployedItems(verifiedConfigurations, verifiedReDeployables, depHistoryDBItemsToDeployDelete, controllerId);
                    // updateRepo command is atomar, therefore all items are rejected
                    continue;
                } catch (JobSchedulerConnectionRefusedException e) {
                    String errorMessage = String.format("Connection to Controller \"%1$s\" failed!", controllerId);
                    LOGGER.warn(errorMessage);
                    deploymentState = JSDeploymentState.NOT_DEPLOYED;
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(controllerId, errorMessage);
                    // updateRepo command is atomar, therefore all items are rejected
                    dbLayer.updateFailedDeployedItems(verifiedConfigurations, verifiedReDeployables, depHistoryDBItemsToDeployDelete, controllerId);
                    continue;
                } 
            }
            if (deployHasErrors) {
                String[] metaInfos = new String[mastersWithDeployErrors.size()];
                int index = 0;
                for(String key : mastersWithDeployErrors.keySet()) {
                    metaInfos[index] = String.format("%1$s: %2$s", key, mastersWithDeployErrors.get(key));
                    index++;
                }
//                
//                JocError error = new JocError("JOC-419", "Deploy was not successful on Master(s): ", metaInfos);
//                throw new JocDeployException(error);
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private Set<String> getControllerIdsFromFilter (DeployFilter deployFilter) {
        return deployFilter.getControllers().stream().map(Controller::getController).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private Set<Long> getConfigurationIdsToUpdateFromFilter (DeployFilter deployFilter) {
        return deployFilter.getUpdate().stream().map(DeployUpdate::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private Set<Long> getDeploymentIdsToUpdateFromFilter (DeployFilter deployFilter) {
        return deployFilter.getUpdate().stream().map(DeployUpdate::getDeploymentId).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private Set<Long> getDeploymentIdsToDeleteFromFilter (DeployFilter deployFilter) {
        return deployFilter.getDelete().stream().map(DeployDelete::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<Long> getDeploymentIdsToDeleteByConfigurationIdsFromFilter (DeployFilter deployFilter) {
        Set<Long> configurationIds = 
                deployFilter.getDelete().stream().map(DeployDelete::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
        try {
            List<Long> deploymentIds = dbLayer.getLatestDeploymentFromConfigurationId(configurationIds);
            return new HashSet<Long>(deploymentIds);
        } catch (SOSHibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return deployFilter.getDelete().stream().map(DeployDelete::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
        
    }
}