package com.sos.joc.publish.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
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
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.DbItemConfWithOriginalContent;
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
            Map<String, List<DBItemInventoryJSInstance>> allControllers = dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(
                    DBItemInventoryJSInstance::getControllerId));
            // process filter
            Set<String> controllerIds = new HashSet<String>(deployFilter.getControllerIds());
            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> draftFoldersToStore = getDraftConfigurationFoldersToStoreFromFilter(deployFilter);
            /*
             * TODO: - check for configurationIds with -marked-for-delete- set - get all deployments from history related to the given configurationId - 
             * get all controllers from those deployments - delete all those existing deployments from all determined controllers
             **/
            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> deployFoldersToStoreAgain = getDeployConfigurationFoldersToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            List<Config> foldersToDelete = null;
            if (deployFilter.getDelete() != null) {
                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream().filter(item -> 
                    item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
                foldersToDelete = PublishUtils.handleFolders(foldersToDelete, dbLayer);
            }

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToStore = null;
            if (!draftConfigsToStore.isEmpty()) {
                configurationDBItemsToStore = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore);
            }
            if (!draftFoldersToStore.isEmpty()) {
                if (configurationDBItemsToStore == null) {
                    configurationDBItemsToStore = new ArrayList<DBItemInventoryConfiguration>();
                }
                configurationDBItemsToStore.addAll(PublishUtils.getValidDeployableDraftInventoryConfigurationsfromFolders(draftFoldersToStore, dbLayer));
            }
            Set<DbItemConfWithOriginalContent> cfgsDBItemsToStore = null;
            if (configurationDBItemsToStore != null) {
                cfgsDBItemsToStore = configurationDBItemsToStore.stream()
                        .map(item -> new DbItemConfWithOriginalContent(item, item.getContent()))
                        .filter(Objects::nonNull).collect(Collectors.toSet());
            }
            final Set<DbItemConfWithOriginalContent> unmodified = cfgsDBItemsToStore;
            List<DBItemDeploymentHistory> depHistoryDBItemsToStore = null;
            if (!deployConfigsToStoreAgain.isEmpty()) {
                depHistoryDBItemsToStore = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
            }
            if (!deployFoldersToStoreAgain.isEmpty()) {
                if (depHistoryDBItemsToStore == null) {
                    depHistoryDBItemsToStore = new ArrayList<DBItemDeploymentHistory>();
                }
                depHistoryDBItemsToStore.addAll(PublishUtils.getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(deployFoldersToStoreAgain, dbLayer));
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
            if (deployConfigsToDelete != null && !deployConfigsToDelete.isEmpty()) {
                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToDeployDelete.stream()
                            .collect(Collectors.groupingBy(DBItemDeploymentHistory::getPath));
                    depHistoryDBItemsToDeployDelete = grouped.keySet().stream().map(item -> grouped.get(item).get(0)).collect(Collectors.toList());
                }
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
            
            // set new versionId for first round (update items)
            final String versionIdForUpdate = UUID.randomUUID().toString();
            final String versionIdForDelete = UUID.randomUUID().toString();
            final String versionIdForDeleteFromFolder = UUID.randomUUID().toString();
            // call UpdateRepo for all provided Controllers and all objects to update
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No private key found for signing! - Please check your private key from the key management section in your profile.");
            }
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
            for (String controllerId : controllerIds) {
                // sign deployed configurations with new versionId
                final Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations =
                        new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
                Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
                // determine agent names to be replaced
                Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
                // determine all (latest) entries from the given folders
                List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();

                if(foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    foldersToDelete.stream()
                        .map(Config::getConfiguration)
                        .map(item -> dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId, item.getRecursive()))
                        .forEach(item -> itemsFromFolderToDelete.addAll(item));
                }

                if (unsignedDrafts != null) {
                    // WORKAROUND: old items with leading slash
                    PublishUtils.updatePathWithNameInContent(unsignedDrafts);
                    unsignedDrafts.stream().filter(item -> item.getTypeAsEnum().equals(ConfigurationType.WORKFLOW)).forEach(
                            item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                    verifiedConfigurations.putAll(PublishUtils.getDraftsWithSignature(versionIdForUpdate, account, unsignedDrafts,
                            updateableAgentNames, keyPair, controllerId, hibernateSession));
                }
                // already deployed objects AgentName handling
                // all items will be signed or re-signed with current versionId
                if (unsignedReDeployables != null && !unsignedReDeployables.isEmpty()) {
                    // WORKAROUND: old items with leading slash
                    PublishUtils.updatePathWithNameInContent(unsignedReDeployables);
                    unsignedReDeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType())))
                            .forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId,
                                    dbLayer)));
                    verifiedReDeployables.putAll(PublishUtils.getDeploymentsWithSignature(versionIdForUpdate, account, unsignedReDeployables,
                            hibernateSession, JocSecurityLevel.LOW));
                }
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(verifiedConfigurations.keySet(),
                        controllerId, dbLayer, keyPair.getKeyAlgorithm());
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    toDeleteForRename.addAll(PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), controllerId, dbLayer, keyPair
                            .getKeyAlgorithm()));
                } else {
                    toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(verifiedReDeployables.keySet(), controllerId, dbLayer, keyPair
                            .getKeyAlgorithm());
                }
                // and subsequently call delete for the object with the previous path before committing the update
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                    // call updateRepo command via Proxy of given controllers
                    PublishUtils.updateItemsDelete(versionIdForDeleteRenamed, toDelete, controllerId, dbLayer, keyPair.getKeyAlgorithm()).thenAccept(
                            either -> {
                                processAfterDelete(either, toDelete, controllerId, account, versionIdForDeleteRenamed, null);
                            });// .get();
                }
                if ((verifiedConfigurations != null && !verifiedConfigurations.isEmpty()) || (verifiedReDeployables != null && !verifiedReDeployables
                        .isEmpty())) {
                    // call updateRepo command via ControllerApi for given controllers
//                    String signerDN = null;
//                    X509Certificate cert = null;
                    switch (keyPair.getKeyAlgorithm()) {
                    case SOSKeyConstants.PGP_ALGORITHM_NAME:
                        PublishUtils.updateItemsAddOrUpdatePGP(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                            versionIdForUpdate, controllerId, deployFilter, unmodified);
                                });// .get()
                        break;
                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
//                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
//                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateItemsAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                            .thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                            versionIdForUpdate, controllerId, deployFilter, unmodified);
                                });// .get()
                        break;
                    case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
//                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
//                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateItemsAddOrUpdateWithX509(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                            .thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                            versionIdForUpdate, controllerId, deployFilter, unmodified);
                                });// .get()
                        break;
                    }
                }
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    final List<DBItemDeploymentHistory> itemsToDelete = depHistoryDBItemsToDeployDelete;
                    PublishUtils.updateItemsDelete(versionIdForDelete, itemsToDelete, controllerId, dbLayer, keyPair.getKeyAlgorithm()).thenAccept(
                            either -> {
                                processAfterDelete(either, itemsToDelete, controllerId, account, versionIdForDelete, deployFilter);
                            });// .get()
                }
                // process folder to Delete
                if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // determine all (latest) entries from the given folder
                    final List<Config> folders = foldersToDelete;
                    final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream().filter(item -> item.getControllerId().equals(
                            controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation()))).collect(Collectors.toList());
                    PublishUtils.updateItemsDelete(versionIdForDeleteFromFolder, itemsToDelete, controllerId, dbLayer, keyPair.getKeyAlgorithm())
                            .thenAccept(either -> {
                                processAfterDeleteFromFolder(either, itemsToDelete, folders, controllerId, account, versionIdForDeleteFromFolder,
                                        deployFilter);
                            });// .get()
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

    private List<Configuration> getDraftConfigurationsToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream().filter(item -> !item.getConfiguration().getObjectType().equals(
                    ConfigurationType.FOLDER)).map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }

    private List<Configuration> getDraftConfigurationFoldersToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream().filter(item -> item.getConfiguration().getObjectType().equals(
                    ConfigurationType.FOLDER)).map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }

    private List<Configuration> getDeployConfigurationsToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream().filter(item -> !item.getConfiguration().getObjectType().equals(
                    ConfigurationType.FOLDER)).map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }

    private List<Configuration> getDeployConfigurationFoldersToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream().filter(item -> item.getConfiguration().getObjectType().equals(
                    ConfigurationType.FOLDER)).map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }

    private List<Configuration> getDeployConfigurationsToDeleteFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getDelete() != null) {
            return deployFilter.getDelete().getDeployConfigurations().stream().filter(item -> !item.getConfiguration().getObjectType().equals(
                    ConfigurationType.FOLDER)).map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }

    private void processAfterAdd(
            Either<Problem, Void> either, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables,
            String account, 
            String versionIdForUpdate,
            String controllerId, 
            DeployFilter deployFilter, 
            Set<DbItemConfWithOriginalContent> withOriginalContent) {
        // First create a new db session as the session of the parent web service can already been closed
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            final Date deploymentDate = Date.from(Instant.now());
            if (either.isRight()) {
                // no error occurred
                Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
                if (verifiedConfigurations != null && !verifiedConfigurations.isEmpty()) {
                    deployedObjects.addAll(PublishUtils.cloneInvConfigurationsToDepHistoryItems(verifiedConfigurations,
                        updateableAgentNames, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
                    PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet().stream().collect(Collectors.toSet()),
                            withOriginalContent, controllerId, dbLayer.getSession());
                }
                if (verifiedReDeployables != null && !verifiedReDeployables.isEmpty()) {
                    deployedObjects.addAll(PublishUtils.cloneDepHistoryItemsToRedeployed(verifiedReDeployables, account, dbLayer, versionIdForUpdate,
                            controllerId, deploymentDate));
                }
                if (!deployedObjects.isEmpty()) {
                    LOGGER.info(String.format("Update command send to Controller \"%1$s\".", controllerId));
                    JocInventory.handleWorkflowSearch(newHibernateSession, deployedObjects, false);
                }
            } else if (either.isLeft()) {
                // an error occurred
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.error(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployUpdateItems = dbLayer.updateFailedDeploymentForUpdate(verifiedConfigurations,
                        verifiedReDeployables, controllerId, account, versionIdForUpdate, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployUpdateItems);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), controllerId);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    private void processAfterDelete (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> itemsToDelete, 
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
                        .map(item -> item.getInventoryConfigurationId())
                        .collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = 
                        PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer);
                JocInventory.deleteConfigurations(configurationIdsToDelete);
                JocInventory.handleWorkflowSearch(newHibernateSession, deletedDeployItems, true);
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(
                        itemsToDelete, controllerId, account, versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), controllerId);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

    private void processAfterDeleteFromFolder(Either<Problem, Void> either, List<DBItemDeploymentHistory> itemsToDelete,
            List<Config> foldersToDelete, String controllerId, String account, String versionIdForDelete, DeployFilter deployFilter) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isRight()) {
                Set<Long> configurationIdsToDelete = itemsToDelete.stream()
                        .map(item -> dbLayer.getInventoryConfigurationIdByPathAndType(item.getPath(), item.getType()))
                        .collect(Collectors.toSet());
                foldersToDelete.stream()
                    .forEach(item -> configurationIdsToDelete.addAll(
                        dbLayer.getDeployableInventoryConfigurationIdsByFolder(item.getConfiguration().getPath(), item.getConfiguration().getRecursive())));
                Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer);
                JocInventory.deleteConfigurations(configurationIdsToDelete);
                JocInventory.handleWorkflowSearch(newHibernateSession, deletedDeployItems, true);
                if (foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    for (Config folder : foldersToDelete) {
                        // check if deployable objects still exist in the folder
                        Set<DBItemDeploymentHistory> stillActiveDeployments = PublishUtils.getLatestDepHistoryEntriesActiveForFolder(folder, dbLayer);
                        if (checkDeploymentItemsStillExist(stillActiveDeployments)) {
                            String controllersFormatted = stillActiveDeployments.stream()
                                    .map(item -> item.getControllerId()).collect(Collectors.joining(", "));
                            LOGGER.warn(String.format(
                                    "removed folder \"%1$s\" can´t be deleted from inventory. Deployments still exist on controllers %2$s.", folder
                                            .getConfiguration().getPath(), controllersFormatted));
                        } else {
                            try {
                                JocInventory.deleteEmptyFolders(new InventoryDBLayer(newHibernateSession), folder.getConfiguration().getPath());
                            } catch (SOSHibernateException e) {
                                ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(),
                                        controllerId);
                           }
                        }
                    }
                }
            } else if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(itemsToDelete, controllerId, account,
                        versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), controllerId);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

    private boolean checkDeploymentItemsStillExist(Set<DBItemDeploymentHistory> deployments) {
        if (deployments != null && !deployments.isEmpty()) {
            return true;
        }
        return false;
    }

}