package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
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
import com.sos.joc.model.publish.JSConfigurationState;
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
                    /* getPermissonsJocCockpit("", xAccessToken).getPublish().isDeploy() */
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = Globals.defaultProfileAccount;
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            Set<Long> configurationIdsToUpdate = getConfigurationIdsToUpdateFromFilter(deployFilter);
            Set<Long> deploymentIdsToUpdate = getDeploymentIdsToUpdateFromFilter(deployFilter);
            Set<Long> deploymentIdsToDelete = getDeploymentIdsToDeleteFromFilter(deployFilter);
            // read all objects provided in the filter from the database
//            List<DBItemInventoryJSInstance> controllers = dbLayer.getControllers(controllerIds);
            List<DBItemInventoryConfiguration> invCfgsToUpdate = dbLayer.getFilteredInventoryConfigurationsByIds(configurationIdsToUpdate);
            List<DBItemDeploymentHistory> depHistoryToUpdate = dbLayer.getFilteredDeploymentHistory(deploymentIdsToUpdate);
            List<DBItemDeploymentHistory> toDelete = dbLayer.getFilteredDeploymentHistory(deploymentIdsToDelete);
            
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Set<DBItemInventoryConfiguration> unsignedDrafts = new HashSet<DBItemInventoryConfiguration>();
            for (DBItemInventoryConfiguration update : invCfgsToUpdate) {
                List<DBItemDepSignatures> signatures = dbLayer.getSignatures(update.getId());
                DBItemDepSignatures latestSignature = null;
                if (signatures != null && !signatures.isEmpty()) {
                    Comparator<DBItemDepSignatures> comp = Comparator.comparingLong(sig -> sig.getModified().getTime());
                    DBItemDepSignatures first = signatures.stream().sorted(comp).findFirst().get();
                    DBItemDepSignatures last = signatures.stream().sorted(comp.reversed()).findFirst().get();
                    latestSignature = last;
                    signedDrafts.put(update, latestSignature);
                } else {
                    unsignedDrafts.add(update);
                }
            }
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            // versionId on objects will be removed
            // versionId on command stays
            // Clarify: Keep UUID as versionId?
            final String versionId = UUID.randomUUID().toString();
            final Date deploymentDate = Date.from(Instant.now());
            // signed and unsigned objects are allowed
            // existing signatures of objects are verified
            for(DBItemInventoryConfiguration draft : signedDrafts.keySet()) {
                verifiedDrafts.put(PublishUtils.verifySignature(account, draft, signedDrafts.get(draft), hibernateSession), signedDrafts.get(draft));
            }
            verifiedDrafts.putAll(PublishUtils.getDraftsWithSignature(versionId, account, unsignedDrafts, hibernateSession));
            // call UpdateRepo for all provided Controllers
            JSConfigurationState deployConfigurationState = null;
            for (String controllerId : controllerIds) {
                try {
                    PublishUtils.updateRepo(versionId, verifiedDrafts, depHistoryToUpdate, toDelete, controllerId, dbLayer);
                    deployConfigurationState = JSConfigurationState.DEPLOYED_SUCCESSFULLY;
                    ClusterState clusterState = Globals.objectMapper.readValue(
                            Proxy.of(controllerId).currentState().clusterState().toJson(), ClusterState.class);
                    String activeClusterUri = clusterState.getIdToUri().getAdditionalProperties().get(clusterState.getActiveId());
                    Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvCfgsToDepHistory(
                            verifiedDrafts, account, hibernateSession, versionId, dbLayer.getActiveClusterControllerDBItemId(activeClusterUri), deploymentDate);
                    deployedObjects.addAll(dbLayer.createNewDepHistoryItems(depHistoryToUpdate, deploymentDate));
                    Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(toDelete, dbLayer);
                    PublishUtils.prepareNextInvConfigGeneration(verifiedDrafts.keySet(), hibernateSession);
                    LOGGER.info(String.format("Deploy to Master \"%1$s\" was successful!",
                            controllerId));
                } catch (JobSchedulerBadRequestException e) {
                    LOGGER.error(e.getError().getCode());
                    LOGGER.error(String.format("Response from Master \"%1$s:\":", controllerId));
                    LOGGER.error(String.format("%1$s", e.getError().getMessage()));
                    deployConfigurationState = JSConfigurationState.NOT_DEPLOYED;
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(controllerId, e.getError().getMessage());
                    // updateRepo command is atomar, therefore all items are refused, no historyItem will be stored in the DB
                    continue;
                } catch (JobSchedulerConnectionRefusedException e) {
                    String errorMessage = String.format("Connection to Controller \"%1$s\" failed! Objects not deployed!",
                            controllerId);
                    LOGGER.error(errorMessage);
                    deployConfigurationState = JSConfigurationState.NOT_DEPLOYED;
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(controllerId, errorMessage);
                    // updateRepo command is atomar, therefore all items are refused, no historyItem will be stored in the DB
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
                
                JocError error = new JocError("JOC-419", "Deploy was not successful on Master(s): ", metaInfos);
                throw new JocDeployException(error);
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
        return deployFilter.getDelete().stream().map(DeployDelete::getDeploymentId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

}