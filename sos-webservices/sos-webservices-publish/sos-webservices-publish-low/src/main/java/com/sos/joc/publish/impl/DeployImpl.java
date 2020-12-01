package com.sos.joc.publish.impl;

import java.io.IOException;
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
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.ControllerId;
import com.sos.joc.model.publish.DeployConfig;
import com.sos.joc.model.publish.DeployConfigDelete;
import com.sos.joc.model.publish.DeployConfiguration;
import com.sos.joc.model.publish.DeployConfigurationDelete;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DraftConfig;
import com.sos.joc.model.publish.DraftConfiguration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
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
    private boolean hasErrors = false;
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
            String account = Globals.defaultProfileAccount;
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // get all available controller instances
            Map<String, List<DBItemInventoryJSInstance>> allControllers = 
                    dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(DBItemInventoryJSInstance::getControllerId));
            // process filter
            Set<String> controllerIds = getControllerIdsFromFilter(deployFilter);
            List<DraftConfiguration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            /* TODO: 
             * - check for configurationIds with -marked-for-delete- set
             * - get all deployments from history related to the given configurationId
             * - get all controllers from those deployments
             * - delete all those existing deployments from all determined controllers
            **/
            List<DeployConfiguration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<DeployConfigurationDelete> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            final List<DeployConfigDelete> foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
                    .filter(item -> item.getDeployConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToStore = null;
            if (!draftConfigsToStore.isEmpty()) {
                configurationDBItemsToStore = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore);
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToStore = null;
            if (!deployConfigsToStoreAgain.isEmpty()) {
                depHistoryDBItemsToStore = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
            if (!deployConfigsToDelete.isEmpty()) {
                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
            }

            // sign undeployed configurations
            Set<DBItemInventoryConfiguration> unsignedDrafts = null;
            if (configurationDBItemsToStore != null) {
                unsignedDrafts = new HashSet<DBItemInventoryConfiguration>(configurationDBItemsToStore);
            }
            Set<DBItemDeploymentHistory> unsignedReDeployables = null;
            if (depHistoryDBItemsToStore != null) {
                unsignedReDeployables = new HashSet<DBItemDeploymentHistory>(depHistoryDBItemsToStore);
            }
            
            // determine all (latest) entries from the given folders
            List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();
            if(foldersToDelete != null && !foldersToDelete.isEmpty()) {
                foldersToDelete.stream()
                    .map(DeployConfigDelete::getDeployConfiguration)
                    .map(item -> dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath()))
                        .forEach(item -> itemsFromFolderToDelete.addAll(item));
            }

            // determine agent names to be replaced
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            // sign deployed configurations with new versionId
            final Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            // set new versionId for first round (update items)
            final String versionIdForUpdate = UUID.randomUUID().toString();
            final String versionIdForDelete = UUID.randomUUID().toString();
            final String versionIdForDeleteFromFolder = UUID.randomUUID().toString();
            final Date deploymentDate = Date.from(Instant.now());
            // all items will be signed or re-signed with current versionId
            if (unsignedReDeployables != null && !unsignedReDeployables.isEmpty()) {
                verifiedReDeployables.putAll(
                        PublishUtils.getDeploymentsWithSignature(versionIdForUpdate, account, unsignedReDeployables, hibernateSession, 
                                JocSecurityLevel.LOW));
            }
            // call UpdateRepo for all provided Controllers and all objects to update
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename 
            for (String controllerId : controllerIds) {
                if (unsignedDrafts != null) {
                    unsignedDrafts.stream()
                    .filter(item -> item.getTypeAsEnum().equals(ConfigurationType.WORKFLOW))
                    .forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                    verifiedConfigurations.putAll(PublishUtils.getDraftsWithSignature(
                            versionIdForUpdate, account, unsignedDrafts, updateableAgentNames, hibernateSession, JocSecurityLevel.LOW));
                }
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                        verifiedConfigurations.keySet(), controllerId, dbLayer, keyPair.getKeyAlgorithm());
                if (toDeleteForRename != null) {
                    toDeleteForRename.addAll(PublishUtils.checkPathRenamingForUpdate(
                            verifiedReDeployables.keySet(), controllerId, dbLayer, keyPair.getKeyAlgorithm()));
                } else {
                    toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                            verifiedReDeployables.keySet(), controllerId, dbLayer, keyPair.getKeyAlgorithm());
                }
                // and subsequently call delete for the object with the previous path before committing the update 
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                        // call updateRepo command via Proxy of given controllers
                        PublishUtils.updateRepoDelete(versionIdForDeleteRenamed, toDelete, controllerId, dbLayer, 
                                keyPair.getKeyAlgorithm()).thenAccept(either -> {
                                processAfterDelete(either, toDelete, controllerId, account, versionIdForDeleteRenamed, null);
                        });//.get();
                }
                if ((verifiedConfigurations != null && !verifiedConfigurations.isEmpty())
                        || (verifiedReDeployables != null && !verifiedReDeployables.isEmpty())) {
                    // call updateRepo command via ControllerApi for given controllers
                    String signerDN = null;
                    X509Certificate cert = null;
                    switch(keyPair.getKeyAlgorithm()) {
                    case SOSKeyConstants.PGP_ALGORITHM_NAME:
                        PublishUtils.updateRepoAddOrUpdatePGP(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId, 
                                dbLayer).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account, 
                                            versionIdForUpdate, controllerId, deploymentDate, deployFilter);
                        });//.get()
                        break;
                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer,SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account, 
                                            versionIdForUpdate, controllerId, deploymentDate, deployFilter);
                        });//.get()
                        break;
                    case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateRepoAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account, 
                                            versionIdForUpdate, controllerId, deploymentDate, deployFilter);
                        });//.get()
                        break;
                    }
                }
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    final List<DBItemDeploymentHistory> itemsToDelete = depHistoryDBItemsToDeployDelete;
                    PublishUtils.updateRepoDelete(versionIdForDelete, itemsToDelete, controllerId, dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                            processAfterDelete(either, itemsToDelete, controllerId, account, versionIdForDelete, deployFilter);
                    });//.get()
                }
                // process folder to Delete
                if(itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // determine all (latest) entries from the given folder
                    final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream()
                            .filter(item -> item.getControllerId().equals(controllerId) 
                                    && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation())))
                            .collect(Collectors.toList());
                    PublishUtils.updateRepoDelete(
                            versionIdForDeleteFromFolder, 
                            itemsToDelete, 
                            controllerId, 
                            dbLayer, 
                            keyPair.getKeyAlgorithm()).thenAccept(either -> {
                            processAfterDeleteFromFolder(either, itemsToDelete, foldersToDelete, controllerId, account, versionIdForDeleteFromFolder, deployFilter);
                    });//.get()
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
        return deployFilter.getControllerIds().stream().map(ControllerId::getControllerId).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    private List<DraftConfiguration> getDraftConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> !item.getDraftConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(DraftConfig::getDraftConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<DraftConfiguration>();
        }
    }
    
    private List<DeployConfiguration> getDeployConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream()
                    .filter(item -> !item.getDeployConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(DeployConfig::getDeployConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<DeployConfiguration>();
        }
    }
    
    private List<DeployConfigurationDelete> getDeployConfigurationsToDeleteFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getDelete() != null) {
            return deployFilter.getDelete().getDeployConfigurations().stream()
                    .filter(item -> !item.getDeployConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(DeployConfigDelete::getDeployConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
          return new ArrayList<DeployConfigurationDelete>();
        }
    }
    
    private void processAfterAdd (
            Either<Problem, Void> either, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account,
            String versionIdForUpdate,
            String controllerId,
            Date deploymentDate,
            DeployFilter deployFilter) {
        // First create a new db session as the session of the parent web service can already been closed
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);

            if (either.isRight()) {
                // no error occurred
                Set<DBItemDeploymentHistory> deployedObjects = PublishUtils.cloneInvConfigurationsToDepHistoryItems(
                        verifiedConfigurations, updateableAgentNames, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
                deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(
                        verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
                PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet().stream().collect(Collectors.toSet()), 
                        updateableAgentNames, controllerId, dbLayer.getSession());
                LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controllerId));
                createAuditLogForEach(deployedObjects, deployFilter, controllerId, true, versionIdForUpdate);
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
                hasErrors = true;
                if (either.getLeft().codeOrNull() != null) {
                    listOfErrors.add(
                            new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
                } else {
                    listOfErrors.add(new BulkError().get(new JocError(either.getLeft().message()), "/"));
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
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
                Set<Long> configurationIdsToDelete = depHistoryDBItemsToDeployDelete.stream()
                        .map(DBItemDeploymentHistory::getInventoryConfigurationId).collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = 
                        PublishUtils.updateDeletedDepHistory(depHistoryDBItemsToDeployDelete, dbLayer);
                createAuditLogForEach(deletedDeployItems, deployFilter, controller, false, versionIdForDelete);
                JocInventory.deleteConfigurations(configurationIdsToDelete);
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controller, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                        depHistoryDBItemsToDeployDelete, controller, account, versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
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
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    private void processAfterDeleteFromFolder (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> itemsToDelete, 
            List<DeployConfigDelete> foldersToDelete,
            String controller, 
            String account, 
            String versionIdForDelete,
            DeployFilter deployFilter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isRight()) {
                Set<Long> configurationIdsToDelete = itemsToDelete.stream()
                        .map(DBItemDeploymentHistory::getInventoryConfigurationId).collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = 
                        PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer);
                createAuditLogForEach(deletedDeployItems, deployFilter, controller, false, versionIdForDelete);
                JocInventory.deleteConfigurations(configurationIdsToDelete);
                Set<DBItemDeploymentHistory> stillActive = PublishUtils.getLatestDepHistoryEntriesActiveForFolders(foldersToDelete, dbLayer);
                if (stillActive != null && !stillActive.isEmpty()) {
                    Set<String> controllerIds = stillActive.stream().map(item -> item.getControllerId()).collect(Collectors.toSet());
                    controllerIds.stream()
                        .forEach(item -> 
                            LOGGER.warn(String.format("removed folder can´t be deleted from inventory. Deployments still exist on controller %1$s.", item)));
                    
                } else {
                    // no active items from dep history, folders can be safely deleted from inventory
                    JocInventory.deleteConfigurations(dbLayer.getInvCfgFolders(
                            foldersToDelete.stream()
                            .map(item -> item.getDeployConfiguration().getPath())
                            .collect(Collectors.toList()))
                    .stream()
                    .map(item -> item.getId()).collect(Collectors.toSet()));
                }
                
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controller, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                        itemsToDelete, controller, account, versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
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
    
}