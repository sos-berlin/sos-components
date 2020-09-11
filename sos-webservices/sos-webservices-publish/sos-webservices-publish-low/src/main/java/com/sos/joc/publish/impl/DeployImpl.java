package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.Controller;
import com.sos.joc.model.publish.DeployDelete;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployUpdate;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("publish")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./publish/deploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployImpl.class);
    private DBLayerDeploy dbLayer = null;
    private boolean hasErrors = false;
    private List<Err419> listOfErrors = new ArrayList<Err419>();

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, DeployFilter deployFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        JsonValidator.validateFailFast(Globals.objectMapper.writeValueAsBytes(deployFilter), DeployFilter.class);
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, deployFilter, xAccessToken, "",
                getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = Globals.defaultProfileAccount;
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // get all available controller instances
            Map<String, List<DBItemInventoryJSInstance>> allControllers = 
                    dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(DBItemInventoryJSInstance::getSchedulerId));
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            Set<Long> configurationIdsToDeploy = getConfigurationIdsToUpdateFromFilter(deployFilter);
            Set<Long> deploymentIdsToReDeploy = getDeploymentIdsToUpdateFromFilter(deployFilter);
            Set<Long> deploymentIdsToDeleteFromConfigIds = getDeploymentIdsToDeleteByConfigurationIdsFromFilter(deployFilter, allControllers);
            Set<Long> configurationIdsToDelete = getConfigurationIdsToDeleteFromFilter(deployFilter);

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToDeploy = dbLayer.getFilteredInventoryConfigurationsByIds(configurationIdsToDeploy);
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeploy = dbLayer.getFilteredDeploymentHistory(deploymentIdsToReDeploy);
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistory(deploymentIdsToDeleteFromConfigIds);

            // sign undeployed configurations
            Set<DBItemInventoryConfiguration> unsignedDrafts = new HashSet<DBItemInventoryConfiguration>(configurationDBItemsToDeploy);
            Set<DBItemDeploymentHistory> unsignedReDeployables = new HashSet<DBItemDeploymentHistory>(depHistoryDBItemsToDeploy);
            // sign deployed configurations with new versionId
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = 
                    new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            // set new versionId for first round (update items)
            String versionId = UUID.randomUUID().toString();
            final Date deploymentDate = Date.from(Instant.now());
            // all items will be signed or re-signed with current versionId
            verifiedConfigurations.putAll(
                    PublishUtils.getDraftsWithSignature(versionId, account, unsignedDrafts, hibernateSession, JocSecurityLevel.LOW));
            verifiedReDeployables.putAll(
                    PublishUtils.getDeploymentsWithSignature(versionId, account, unsignedReDeployables, hibernateSession, JocSecurityLevel.LOW));
            // call UpdateRepo for all provided Controllers and all objects to update
            for (String controllerId : controllerIds) {
                List<DBItemInventoryJSInstance> controllerDBItems = Proxies.getControllerDbInstances().get(controllerId);
                ClusterState clusterState = null;
                try {
                    clusterState = Globals.objectMapper.readValue(
                            Proxy.of(controllerId).currentState().clusterState().toJson(), ClusterState.class);
                } catch (Exception e) {
                    List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(
                            verifiedConfigurations, verifiedReDeployables, controllerId, account, versionId, e.getMessage());
                    // if not successful the rest of processing should not stop
                    // objects and the related controllerId have to be stored in a submissions table for reprocessing
                    dbLayer.cloneFailedDeployment(failedDeployUpdateItems);
                    hasErrors = true;
                    listOfErrors.add(new BulkError().get(e, new JocError(e.getMessage()), "/"));
                    continue;
                }
                Long activeClusterControllerId = null;
                if (clusterState != null && !clusterState.getTYPE().equals(ClusterType.EMPTY)) {
                    final String activeClusterUri = clusterState.getIdToUri().getAdditionalProperties().get(clusterState.getActiveId());
                    Optional<Long> optional = controllerDBItems.stream().filter(
                            controller -> activeClusterUri.equals(controller.getClusterUri()))
                    .map(DBItemInventoryJSInstance::getId).findFirst();
                    if (optional.isPresent()) {
                        activeClusterControllerId =  optional.get();
                    } else {
                        activeClusterControllerId = controllerDBItems.get(0).getId();
                    }
                } else {
                    activeClusterControllerId = controllerDBItems.get(0).getId();
                }
                
                // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename 
                // and subsequently call delete for the object with the previous path before committing the update 
                PublishUtils.checkPathRenamingForUpdate(verifiedConfigurations.keySet(), activeClusterControllerId, dbLayer);
                PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), activeClusterControllerId, dbLayer);

                // call updateRepo command via Proxy of given controllers
                Either<Problem, Void> either = 
                        PublishUtils.updateRepo(versionId, verifiedConfigurations, verifiedReDeployables, null, controllerId, dbLayer);
                if (either.isRight()) {
                    // no error occurred
                    Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                            verifiedConfigurations, account, dbLayer, versionId, activeClusterControllerId, deploymentDate);
                    deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                            verifiedReDeployables, account, dbLayer, versionId, activeClusterControllerId, deploymentDate));
                    PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), hibernateSession);
                    LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controllerId));
                } else if (either.isLeft()) {
                    // an error occurred
                    String message = String.format(
                            "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                    LOGGER.warn(message);
                    // updateRepo command is atomic, therefore all items are rejected
                    List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(
                            verifiedConfigurations, verifiedReDeployables, controllerId, account, versionId, either.getLeft().message());
                    // if not successful the objects and the related controllerId have to be stored 
                    // in a submissions table for reprocessing
                    dbLayer.cloneFailedDeployment(failedDeployUpdateItems);
                    hasErrors = true;
                    if (either.getLeft().codeOrNull() != null) {
                        listOfErrors.add(
                                new BulkError().get(new JocError(either.getLeft().message()), "/"));
                    } else {
                        listOfErrors.add(
                                new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
                    }
                }
            }
            if (configurationIdsToDelete != null && !configurationIdsToDelete.isEmpty()) {
                // set new versionId for second round (delete items)
                versionId = UUID.randomUUID().toString();
                for (String controller : allControllers.keySet()) {
                    // call updateRepo command via Proxy of given controllers
                    Either<Problem, Void> either = PublishUtils.updateRepo(versionId, null, null, depHistoryDBItemsToDeployDelete, controller,
                            dbLayer);
                    if (either.isRight()) {
                        Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete,
                                dbLayer);
                    } else if (either.isLeft()) {
                        String message = String.format("Response from Controller \"%1$s:\": %2$s", controller, either.getLeft().message());
                        LOGGER.warn(message);
                        // updateRepo command is atomic, therefore all items are rejected
                        List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                                depHistoryDBItemsToDeployDelete, controller, account, versionId, either.getLeft().message());
                        // if not successful the objects and the related controllerId have to be stored 
                        // in a submissions table for reprocessing
                        dbLayer.cloneFailedDeployment(failedDeployDeleteItems);
                        hasErrors = true;
                        if (either.getLeft().codeOrNull() != null) {
                            listOfErrors.add(
                                    new BulkError().get(new JocError(either.getLeft().message()), "/"));
                        } else {
                            listOfErrors.add(
                                    new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
                        }
                    }
                    JocInventory.deleteConfigurations(configurationIdsToDelete);
                }
            }
            if (hasErrors) {
                return JOCDefaultResponse.responseStatus419(listOfErrors);
            } else {
                if (verifiedConfigurations != null && !verifiedConfigurations.isEmpty()) {
                    dbLayer.cleanupSignaturesForConfigurations(verifiedConfigurations.keySet());
                    dbLayer.cleanupCommitIdsForConfigurations(verifiedConfigurations.keySet());
                }
                if (verifiedReDeployables != null && !verifiedReDeployables.isEmpty()) {
                    dbLayer.cleanupSignaturesForRedeployments(verifiedReDeployables.keySet());
                    dbLayer.cleanupCommitIdsForRedeployments(verifiedReDeployables.keySet());
                }
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
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
    
    private Set<Long> getConfigurationIdsToDeleteFromFilter (DeployFilter deployFilter) {
        return deployFilter.getDelete().stream().map(DeployDelete::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private Set<Long> getDeploymentIdsToDeleteByConfigurationIdsFromFilter (DeployFilter deployFilter, 
            Map<String, List<DBItemInventoryJSInstance>> controllerInstances)
            throws SOSHibernateException {
        Set<Long> configurationIds = 
                deployFilter.getDelete().stream().map(DeployDelete::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> deploymentIdsToDelete = new HashSet<Long>();
        for (String controller : controllerInstances.keySet()) {
            List<Long> deploymentIds = dbLayer.getLatestDeploymentFromConfigurationId(configurationIds, controller);
            deploymentIdsToDelete.addAll(deploymentIds);
        }
        return deploymentIdsToDelete;
    }
}