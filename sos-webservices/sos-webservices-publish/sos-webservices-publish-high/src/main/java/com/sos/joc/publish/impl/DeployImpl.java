package com.sos.joc.publish.impl;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DeployAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerId;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("inventory/deployment")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./inventory/deployment/deploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployImpl.class);
    private DBLayerDeploy dbLayer = null;
//    private boolean hasErrors = false;
    private List<Err419> listOfErrors = new ArrayList<Err419>();

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, DeployFilter.class);
            DeployFilter deployFilter = Globals.objectMapper.readValue(filter, DeployFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // get all available controller instances
            Map<String, List<DBItemInventoryJSInstance>> allControllers = dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(
                    DBItemInventoryJSInstance::getControllerId));
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            /* TODO: 
             * - check for configurationIds with -marked-for-delete- set
             * - get all deployments from history related to the given configurationId
             * - get all controllers from those deployments
             * - delete all those existing deployments from all determined controllers
            **/
            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            
            List<Config> foldersToDelete = null;
            if (deployFilter.getDelete() != null) {
                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
                .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
            }

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToDeploy = null;
            if (draftConfigsToStore != null) {
                configurationDBItemsToDeploy = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore);
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeploy = null;
            if (deployConfigsToStoreAgain != null) {
                depHistoryDBItemsToDeploy = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
            if (deployConfigsToDelete != null) {
                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
            }

            Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> signedDeployments = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();

            for (DBItemInventoryConfiguration update : configurationDBItemsToDeploy) {
                DBItemDepSignatures signature = dbLayer.getSignature(update.getId());
                if (signature != null) {
                    signedDrafts.put(update, signature);
                }
            }
            for (DBItemDeploymentHistory depHistory : depHistoryDBItemsToDeploy) {
                DBItemDepSignatures signature = dbLayer.getSignature(depHistory.getId());
                if (signature != null) {
                    signedDeployments.put(depHistory, signature);
                }
            }
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations =
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = 
                    new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            String versionId = null;
            final String versionIdForDelete = UUID.randomUUID().toString();
            final String versionIdForDeleteFromFolder = UUID.randomUUID().toString();
            // only signed objects will be processed
            // existing signatures of objects are verified
            for (DBItemInventoryConfiguration draft : signedDrafts.keySet()) {
                if (versionId == null) {
                    versionId = dbLayer.getVersionId(draft);
                }
                verifiedConfigurations.put(PublishUtils.verifySignature(account, draft, signedDrafts.get(draft), hibernateSession,
                        JocSecurityLevel.HIGH), signedDrafts.get(draft));
            }
            for (DBItemDeploymentHistory deployed : signedDeployments.keySet()) {
                verifiedReDeployables.put(PublishUtils.verifySignature(account, deployed, signedDeployments.get(deployed), hibernateSession,
                        JocSecurityLevel.HIGH), signedDeployments.get(deployed));
            }
            final String versionIdForUpdate = versionId;
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No public key or X.509 Certificate found for signature verification! - "
                        + "Please check your key from the key management section in your profile.");
            }
            // call UpdateRepo for all provided Controllers
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
            for (String controllerId : allControllers.keySet()) {
                // determine all (latest) entries from the given folders
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(verifiedConfigurations.keySet(),
                        controllerId, dbLayer, keyPair.getKeyAlgorithm());
                if (toDeleteForRename != null) {
                    toDeleteForRename.addAll(PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), controllerId, dbLayer, 
                            keyPair.getKeyAlgorithm()));
                } else {
                    toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), controllerId, dbLayer, 
                            keyPair.getKeyAlgorithm());
                }
                // and subsequently call delete for the object with the previous path before committing the update
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                    // call updateRepo command via Proxy of given controllers
                    PublishUtils.updateItemsDelete(versionIdForDeleteRenamed, toDelete, controllerId, dbLayer, keyPair.getKeyAlgorithm())
                    .thenAccept(either -> {
                                processAfterDelete(either, toDelete, controllerId, account, versionIdForDeleteRenamed, null);
                            }).get();
                }
            }
            for (String controllerId : controllerIds) {
                // determine all (latest) entries from the given folders
                List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();
                if(foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    foldersToDelete.stream()
                        .map(Config::getConfiguration)
                        .map(item -> dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId))
                            .forEach(item -> itemsFromFolderToDelete.addAll(item));
                }
                if ((verifiedConfigurations != null && !verifiedConfigurations.isEmpty())
                        || (verifiedReDeployables != null && !verifiedReDeployables.isEmpty())) {
                    // call updateRepo command via ControllerApi for given controllers
                    String signerDN = null;
                    X509Certificate cert = null;
                    switch (keyPair.getKeyAlgorithm()) {
                    case SOSKeyConstants.PGP_ALGORITHM_NAME:
                        PublishUtils.updateItemsAddOrUpdatePGP(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId, 
                                dbLayer).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                            deployFilter);
                                }).get();
                        break;
                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateItemsAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                            deployFilter);
                                }).get();
                        break;
                    case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateItemsAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, controllerId,
                                            deployFilter);
                                }).get();
                        break;
                    }
                }
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    // set new versionId for second round (delete items)
                        // call updateRepo command via Proxy of given controllers
                    final List<DBItemDeploymentHistory> toDelete = depHistoryDBItemsToDeployDelete;
                    PublishUtils.updateItemsDelete(versionIdForDelete, toDelete, controllerId, dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                                processAfterDelete(either, toDelete, controllerId, account, versionIdForDelete, deployFilter);
                            }).get();
                }
                // process folder to Delete
                if(itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // determine all (latest) entries from the given folder
                    final List<Config> folders = foldersToDelete;
                    final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream()
                            .filter(item -> item.getControllerId().equals(controllerId) 
                                    && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation())))
                            .collect(Collectors.toList());
                    PublishUtils.updateItemsDelete(
                            versionIdForDeleteFromFolder, 
                            itemsToDelete, 
                            controllerId, 
                            dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                            processAfterDeleteFromFolder(either, itemsToDelete, folders, controllerId, account, versionIdForDeleteFromFolder, 
                                    deployFilter);
                    });//.get()
                }
                if (verifiedConfigurations != null && !verifiedConfigurations.isEmpty()) {
                    dbLayer.cleanupSignaturesForConfigurations(verifiedConfigurations.keySet());
                    dbLayer.cleanupCommitIdsForConfigurations(verifiedConfigurations.keySet());
                }
                if (verifiedReDeployables != null && !verifiedReDeployables.isEmpty()) {
                    dbLayer.cleanupSignaturesForRedeployments(verifiedReDeployables.keySet());
                    dbLayer.cleanupCommitIdsForRedeployments(verifiedReDeployables.keySet());
                }
            }
//            if (hasErrors) {
//                return JOCDefaultResponse.responseStatus419(listOfErrors);
//            } else {
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
//            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private Set<String> getControllerIdsFromFilter(DeployFilter deployFilter) {
        return deployFilter.getControllerIds().stream().map(ControllerId::getControllerId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private List<Configuration> getDraftConfigurationsToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<Configuration> getDeployConfigurationsToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<Configuration> getDeployConfigurationsToDeleteFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getDelete() != null) {
            return deployFilter.getDelete().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private void processAfterAdd (
            Either<Problem, Void> either, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account,
            String versionIdForUpdate,
            String controllerId,
            DeployFilter deployFilter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            final Date deploymentDate = Date.from(Instant.now());
            if (either.isRight()) {
                // no error occurred
                Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                        verifiedConfigurations, null, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
                deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                        verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
                createAuditLogForEach(deployedObjects, deployFilter, controllerId, true, versionIdForUpdate);
                PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet(), null, controllerId, dbLayer.getSession());
                LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controllerId));
                JocInventory.handleWorkflowSearch(newHibernateSession, deployedObjects, false);
            } else if (either.isLeft()) {
                // an error occurred
                String message = String.format(
                        "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.error(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(
                        verifiedConfigurations, verifiedReDeployables, controllerId, account, versionIdForUpdate, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployUpdateItems);
//                hasErrors = true;
                if (either.getLeft().codeOrNull() != null) {
                    listOfErrors.add(
                            new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
                } else {
                    listOfErrors.add(new BulkError().get(new JocError(either.getLeft().message()), "/"));
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);;
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    private void processAfterDelete (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete, 
            String controller, 
            String account, 
            String versionIdForDelete,
            DeployFilter deployFilter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isRight()) {
                Set<Long> draftConfigsToDelete = depHistoryDBItemsToDeployDelete.stream()
                        .map(DBItemDeploymentHistory::getInventoryConfigurationId).collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = 
                        PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete, dbLayer);
                JocInventory.deleteConfigurations(draftConfigsToDelete);
                createAuditLogForEach(deletedDeployItems, deployFilter, controller, false, versionIdForDelete);
                JocInventory.deleteConfigurations(draftConfigsToDelete);
                JocInventory.handleWorkflowSearch(newHibernateSession, deletedDeployItems, true);
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controller, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                        depHistoryDBItemsToDeployDelete, controller, account, versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
//                hasErrors = true;
                if (either.getLeft().codeOrNull() != null) {
                    listOfErrors.add(
                            new BulkError().get(new JocError(either.getLeft().message()), "/"));
                } else {
                    listOfErrors.add(
                            new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), 
                                    either.getLeft().message()), "/"));
                }
            }
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    private void processAfterDeleteFromFolder (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> itemsToDelete, 
            List<Config> foldersToDelete,
            String controllerId, 
            String account, 
            String versionIdForDelete,
            DeployFilter deployFilter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isRight()) {
                Set<Long> configurationIdsToDelete = itemsToDelete.stream()
                        .map(item -> dbLayer.getInventoryConfigurationIdByPathAndType(item.getPath(), item.getType()))
                        .collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = 
                        PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer);
                createAuditLogForEach(deletedDeployItems, deployFilter, controllerId, false, versionIdForDelete);
                JocInventory.deleteConfigurations(configurationIdsToDelete);
                JocInventory.handleWorkflowSearch(newHibernateSession, deletedDeployItems, true);
                if (foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    for (Config folder : foldersToDelete) {
                        // check if deployable objects still exist in the folder
                        Set<DBItemDeploymentHistory> stillActiveDeployments = 
                                PublishUtils.getLatestDepHistoryEntriesActiveForFolder(folder, dbLayer);
                        // check if releasable objects still exist in the folder
                        List<DBItemInventoryReleasedConfiguration> stillActiveReleased = dbLayer.
                                getReleasedConfigurations(folder.getConfiguration().getPath());
                        List<DBItemInventoryConfiguration> stillActiveReleasables = dbLayer
                                .getReleasableConfigurations(folder.getConfiguration().getPath());
                        if (checkAnyItemsStillExist(
                                stillActiveDeployments, 
                                stillActiveReleased, 
                                stillActiveReleasables)) {
                            if (checkDeploymentItemsStillExist(stillActiveDeployments)) {
                                LOGGER.warn(String
                                        .format("removed folder \"%1$s\" can´t be deleted from inventory. Deployments still exist on controller %1$s.", 
                                                folder.getConfiguration().getPath(), controllerId));
                            }
                            if (checkReleasedItemsStillExist(stillActiveReleased)) {
                                LOGGER.warn(String
                                        .format("removed folder \"%1$s\" can´t be deleted from inventory, released objects still exist.", 
                                                folder.getConfiguration().getPath()));
                            }
                            if (checkReleaseablesItemsStillExist(stillActiveReleasables)) {
                                LOGGER.warn(String
                                        .format("removed folder \"%1$s\" can´t be deleted from inventory, releasable objects still exist.", 
                                                folder.getConfiguration().getPath()));
                            }
                        } else {
                            // no active items still exist in dep history, inv configurations and inv released configurations
                            // folder can be safely deleted from inventory
                            DBItemInventoryConfiguration folderConfig = dbLayer
                                    .getInvConfigurationFolder(folder.getConfiguration().getPath());
                            if(folderConfig != null) {
                                JocInventory.deleteConfigurations(
                                        new HashSet<Long>(Arrays.asList(folderConfig.getId())));
                            }
                        }
                    }
                }
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                        itemsToDelete, controllerId, account, versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
//                hasErrors = true;
                if (either.getLeft().codeOrNull() != null) {
                    listOfErrors.add(
                            new BulkError().get(new JocError(either.getLeft().message()), "/"));
                } else {
                    listOfErrors.add(
                            new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), 
                                    either.getLeft().message()), "/"));
                }
            }
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    private void createAuditLogForEach(Collection<DBItemDeploymentHistory> depHistoryEntries, DeployFilter deployFilter, String controllerId,
            boolean update, String commitId) {
        Set<DeployAudit> audits = depHistoryEntries.stream().map(item -> { 
            if (update) {
                return new DeployAudit(deployFilter, update, controllerId, commitId, item.getId(),
                        item.getPath(), String.format("object %1$s updated on controller %2$s", item.getPath(), controllerId));
            } else {
                return new DeployAudit(deployFilter, update, controllerId, commitId, item.getId(),
                        item.getPath(), String.format("object %1$s deleted from controller %2$s", item.getPath(), controllerId));
            }
        }).collect(Collectors.toSet());
        audits.stream().forEach(audit -> logAuditMessage(audit));
        audits.stream().forEach(audit -> storeAuditLogEntry(audit));
    }
    
    private boolean checkAnyItemsStillExist (
            Set<DBItemDeploymentHistory> deployments, 
            List<DBItemInventoryReleasedConfiguration> released, 
            List<DBItemInventoryConfiguration> releaseables) {
        return checkDeploymentItemsStillExist(deployments)
                || checkReleasedItemsStillExist(released)
                || checkReleaseablesItemsStillExist(releaseables);
    }

    private boolean checkDeploymentItemsStillExist (Set<DBItemDeploymentHistory> deployments) {
        if (deployments != null && !deployments.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean checkReleasedItemsStillExist (List<DBItemInventoryReleasedConfiguration> released) {
        if (released != null && !released.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean checkReleaseablesItemsStillExist (List<DBItemInventoryConfiguration> releaseables) {
        if(releaseables != null && !releaseables.isEmpty()) {
            return true;
        }
        return false;
    }
}