package com.sos.joc.publish.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DeployAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
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
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, DeployFilter.class);
            DeployFilter deployFilter = Globals.objectMapper.readValue(filter, DeployFilter.class);
            // this has to be moved to the processing of the items for each one
//            DeployAudit deployAudit = new DeployAudit(deployFilter);
//            logAuditMessage(deployAudit);
//            storeAuditLogEntry(deployAudit);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = Globals.defaultProfileAccount;
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // get all available controller instances
            Map<String, List<DBItemInventoryJSInstance>> allControllers = 
                    dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(DBItemInventoryJSInstance::getControllerId));
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            Set<Long> configurationIdsToDeploy = getConfigurationIdsToUpdateFromFilter(deployFilter);
            /* TODO: 
             * - check for configurationIds with -marked-for-delete- set
             * - get all deployments from history related to the given configurationId
             * - get all controllers from those deployments
             * - delete all those existing deployments from all determined controllers
            **/
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
            final String versionIdForUpdate = UUID.randomUUID().toString();
            final Date deploymentDate = Date.from(Instant.now());
            // all items will be signed or re-signed with current versionId
            verifiedConfigurations.putAll(
                    PublishUtils.getDraftsWithSignature(versionIdForUpdate, account, unsignedDrafts, hibernateSession, JocSecurityLevel.LOW));
            verifiedReDeployables.putAll(
                    PublishUtils.getDeploymentsWithSignature(versionIdForUpdate, account, unsignedReDeployables, hibernateSession, 
                            JocSecurityLevel.LOW));
            // call UpdateRepo for all provided Controllers and all objects to update
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename 
            for (String controller : allControllers.keySet()) {
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                        verifiedConfigurations.keySet(), controller, dbLayer, keyPair.getKeyAlgorithm());
                if (toDeleteForRename != null) {
                    toDeleteForRename.addAll(PublishUtils.checkPathRenamingForUpdate(
                            verifiedReDeployables.keySet(), controller, dbLayer, keyPair.getKeyAlgorithm()));
                } else {
                    toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                            verifiedReDeployables.keySet(), controller, dbLayer, keyPair.getKeyAlgorithm());
                }
                // and subsequently call delete for the object with the previous path before committing the update 
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                        // call updateRepo command via Proxy of given controllers
                        PublishUtils.updateRepoDelete(versionIdForDeleteRenamed, toDelete, controller, dbLayer, 
                                keyPair.getKeyAlgorithm()).thenAccept(either -> {
                                processAfterDelete(either, toDelete, controller, account, versionIdForDeleteRenamed, null);
                        }).get();
                        JocInventory.deleteConfigurations(configurationIdsToDelete);
                }
            }
            for (String controllerId : controllerIds) {
                // call updateRepo command via ControllerApi for given controllers
                String signerDN = null;
                X509Certificate cert = null;
                switch(keyPair.getKeyAlgorithm()) {
                case SOSKeyConstants.PGP_ALGORITHM_NAME:
                    PublishUtils.updateRepoAddOrUpdatePGP(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId, 
                            dbLayer).thenAccept(either -> {
                                processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                    deploymentDate, deployFilter);
                    }).get();
                    break;
                case SOSKeyConstants.RSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                            dbLayer,SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                    deploymentDate, deployFilter);
                    }).get();
                    break;
                case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                            dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                    deploymentDate, deployFilter);
                    }).get();
                    break;
                }
            }
            if (configurationIdsToDelete != null && !configurationIdsToDelete.isEmpty()) {
                // set new versionId for second round (delete items)
                final String versionIdForDelete = UUID.randomUUID().toString();
                for (String controller : allControllers.keySet()) {
                    // call updateRepo command via Proxy of given controllers
                    PublishUtils.updateRepoDelete(versionIdForDelete, depHistoryDBItemsToDeployDelete, controller, dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                            processAfterDelete(either, depHistoryDBItemsToDeployDelete, controller, account, versionIdForDelete, deployFilter);
                    }).get();
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
    
    private void processAfterAdd (
            Either<Problem, Void> either, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account,
            String versionIdForUpdate,
            String controllerId,
            Date deploymentDate,
            DeployFilter deployFilter) {
        if (either.isRight()) {
            // no error occurred
            Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                    verifiedConfigurations, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
            deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                    verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
            PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), dbLayer.getSession());
            LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controllerId));
            createAuditLogFor(deployedObjects, deployFilter, controllerId, true);
        } else if (either.isLeft()) {
            // an error occurred
            String message = String.format(
                    "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
            LOGGER.error(message);
            // updateRepo command is atomic, therefore all items are rejected
            List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(
                    verifiedConfigurations, verifiedReDeployables, controllerId, account, versionIdForUpdate, either.getLeft().message());
            createAuditLogFor(failedDeployUpdateItems, deployFilter, controllerId, true);

            // if not successful the objects and the related controllerId have to be stored 
            // in a submissions table for reprocessing
            dbLayer.createSubmissionForFailedDeployments(failedDeployUpdateItems);
            hasErrors = true;
            if (either.getLeft().codeOrNull() != null) {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
            } else {
                listOfErrors.add(new BulkError().get(new JocError(either.getLeft().message()), "/"));
            }
        }
    }
    
    private void processAfterDelete (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete, 
            String controller, 
            String account, 
            String versionIdForDelete,
            DeployFilter deployFilter) {
        if (either.isRight()) {
            Set<DBItemDeploymentHistory> deletedDeployItems = 
                    PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete, dbLayer);
            if (deployFilter != null) {
                createAuditLogFor(deletedDeployItems, deployFilter, controller, false);
            }
        } else if (either.isLeft()) {
            String message = String.format("Response from Controller \"%1$s:\": %2$s", controller, either.getLeft().message());
            LOGGER.warn(message);
            // updateRepo command is atomic, therefore all items are rejected
            List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                    depHistoryDBItemsToDeployDelete, controller, account, versionIdForDelete, either.getLeft().message());
            // if not successful the objects and the related controllerId have to be stored 
            // in a submissions table for reprocessing
            if (deployFilter != null) {
                createAuditLogFor(failedDeployDeleteItems, deployFilter, controller, false);
            }
            dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
            hasErrors = true;
            if (either.getLeft().codeOrNull() != null) {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().message()), "/"));
            } else {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), 
                                either.getLeft().message()), "/"));
            }
        }
    }
    
    private void createAuditLogFor(Collection<DBItemDeploymentHistory> depHistoryEntries, DeployFilter deployFilter, String controllerId,
            boolean update) {
        if(deployFilter.getAuditLog()== null) {
            deployFilter.setAuditLog(new AuditParams());   
        }
        for (DBItemDeploymentHistory deployedItem : depHistoryEntries) {
            if (deployFilter.getAuditLog().getComment() == null || deployFilter.getAuditLog().getComment().isEmpty()) {
                if (update) {
                    deployFilter.getAuditLog().setComment(
                            String.format("autom. comment: object %1$s updated on controller %2$s", deployedItem.getPath(), controllerId));
                } else {
                    deployFilter.getAuditLog().setComment(
                            String.format("autom. comment: object %1$s removed from controller %2$s", deployedItem.getPath(), controllerId));
                }
            }
            DeployAudit deployAudit = new DeployAudit(deployFilter, controllerId, deployedItem.getPath(), deployedItem.getId(), update);
            logAuditMessage(deployAudit);
            storeAuditLogEntry(deployAudit);
        }
    }
    
}