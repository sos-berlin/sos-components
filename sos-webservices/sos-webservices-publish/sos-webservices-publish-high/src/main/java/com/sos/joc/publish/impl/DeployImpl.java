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
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
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
    private boolean withoutFolderDeletion = false;

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) throws Exception {
        return postDeploy(xAccessToken, filter, false);
    }

    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter, boolean withoutFolderDeletion) throws Exception {
        this.withoutFolderDeletion = withoutFolderDeletion;
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
            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            // get all available controller instances
            Map<String, List<DBItemInventoryJSInstance>> allControllers = dbLayer.getAllControllers().stream().collect(Collectors.groupingBy(
                    DBItemInventoryJSInstance::getControllerId));
            // process filter
            Set<String> controllerIds = new HashSet<String>(deployFilter.getControllerIds());
            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            /*
             * TODO: - check for configurationIds with -marked-for-delete- set - get all deployments from history related to the given configurationId - get all
             * controllers from those deployments - delete all those existing deployments from all determined controllers
             **/
            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            
            List<Config> foldersToDelete = null;
            if (deployFilter.getDelete() != null) {
                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
                .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
                foldersToDelete = PublishUtils.handleFolders(foldersToDelete, dbLayer);
            }

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToStore = null;
            if (draftConfigsToStore != null) {
                configurationDBItemsToStore = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore);
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
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
            if (deployConfigsToDelete != null && !deployConfigsToDelete.isEmpty()) {
                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToDeployDelete.stream()
                            .collect(Collectors.groupingBy(DBItemDeploymentHistory::getPath));
                    depHistoryDBItemsToDeployDelete = grouped.keySet().stream().map(item -> grouped.get(item).get(0)).collect(Collectors.toList());
                }
            }

            Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = 
                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> signedDeployments = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();

            for (DBItemInventoryConfiguration update : configurationDBItemsToStore) {
                DBItemDepSignatures signature = dbLayer.getSignature(update.getId());
                if (signature != null) {
                    signedDrafts.put(update, signature);
                }
            }
            for (DBItemDeploymentHistory depHistory : depHistoryDBItemsToStore) {
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
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
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
                    boolean verified = false;
                    String signerDN = null;
                    X509Certificate cert = null;
                    switch (keyPair.getKeyAlgorithm()) {
                    case SOSKeyConstants.PGP_ALGORITHM_NAME:
                        PublishUtils.updateItemsAddOrUpdatePGP(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId, 
                                dbLayer).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, versionIdForUpdate, 
                                            controllerId, deployFilter, unmodified);
                                });
                        break;
                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                        if (verified) {
                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                    dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                                .thenAccept(either -> {
                                        processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account,
                                                versionIdForUpdate, controllerId, deployFilter, unmodified);
                                    });
                        } else {
                          signerDN = cert.getSubjectDN().getName();
                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                  dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN)
                              .thenAccept(either -> {
                                      processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account,
                                              versionIdForUpdate, controllerId, deployFilter, unmodified);
                                  });
                        }
                        break;
                    case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                        if (verified) {
                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                    dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                                .thenAccept(either -> {
                                        processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account,
                                                versionIdForUpdate, controllerId, deployFilter, unmodified);
                                    });
                        } else {
                          signerDN = cert.getSubjectDN().getName();
                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(versionIdForUpdate, verifiedConfigurations, verifiedReDeployables, controllerId,
                                  dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN)
                              .thenAccept(either -> {
                                      processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account,
                                              versionIdForUpdate, controllerId, deployFilter, unmodified);
                                  });
                        }
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
            DeployFilter deployFilter,
            Set<DbItemConfWithOriginalContent> cfgsDBItemsToStore) {
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
                        null, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate));
                    PublishUtils.prepareNextInvConfigGeneration(verifiedConfigurations.keySet().stream().collect(Collectors.toSet()),
                            cfgsDBItemsToStore, controllerId, dbLayer.getSession());
                    // cleanup stored signatures
                    dbLayer.cleanupSignatures(verifiedConfigurations.keySet().stream()
                        .map(item -> verifiedConfigurations.get(item)).filter(Objects::nonNull).collect(Collectors.toSet()));
                    // cleanup stored commitIds
                    deployedObjects.stream().forEach(item -> dbLayer.cleanupCommitIds(item.getCommitId()));
                }
                if (verifiedReDeployables != null && !verifiedReDeployables.isEmpty()) {
                    Set<DBItemDeploymentHistory> cloned = PublishUtils.cloneDepHistoryItemsToRedeployed(
                            verifiedReDeployables, account, dbLayer, versionIdForUpdate, controllerId, deploymentDate);
                    deployedObjects.addAll(cloned);
                    // cleanup stored signatures
                    dbLayer.cleanupSignatures(verifiedReDeployables.keySet().stream()
                            .map(item -> verifiedReDeployables.get(item)).filter(Objects::nonNull).collect(Collectors.toSet()));
                    // cleanup stored commitIds
                    cloned.stream().forEach(item -> dbLayer.cleanupCommitIds(item.getCommitId()));
                }
                if (!deployedObjects.isEmpty()) {
                    LOGGER.info(String.format("Update command send to Controller \"%1$s\".", controllerId));
                    JocInventory.handleWorkflowSearch(newHibernateSession, deployedObjects, false);
                }
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
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), null);
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
            final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            final InventoryDBLayer invDbLayer = new InventoryDBLayer(newHibernateSession);
            if (either.isRight()) {
                Set<DBItemInventoryConfiguration> configurationsToDelete = itemsToDelete.stream()
                        .map(item -> dbLayer.getInventoryConfigurationByNameAndType(item.getName(), item.getType()))
                        .collect(Collectors.toSet());
                Set<DBItemDeploymentHistory> deletedDeployItems = 
                        PublishUtils.updateDeletedDepHistoryAndPutToTrash(itemsToDelete, dbLayer, versionIdForDelete);
                configurationsToDelete.stream().forEach(item -> JocInventory.deleteInventoryConfigurationAndPutToTrash(item, invDbLayer));
//                JocInventory.deleteConfigurations(configurationsToDelete);
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
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), null);
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
            final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            final InventoryDBLayer invDbLayer = new InventoryDBLayer(newHibernateSession);
            if (either.isRight()) {
                Set<DBItemInventoryConfiguration> configurationsToDelete = itemsToDelete.stream()
                        .map(item -> dbLayer.getInventoryConfigurationByNameAndType(item.getName(), item.getType()))
                        .collect(Collectors.toSet());
                foldersToDelete.stream()
                    .forEach(item -> configurationsToDelete.addAll(
                        dbLayer.getInventoryConfigurationsByFolder(item.getConfiguration().getPath(), item.getConfiguration().getRecursive())));
                Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistoryAndPutToTrash(itemsToDelete, dbLayer, versionIdForDelete);
                configurationsToDelete.stream().forEach(item -> JocInventory.deleteInventoryConfigurationAndPutToTrash(item, invDbLayer));
//                JocInventory.deleteConfigurations(configurationsToDelete);
                JocInventory.handleWorkflowSearch(newHibernateSession, deletedDeployItems, true);
                if (!withoutFolderDeletion) {
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
                                            null);
                               }
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
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), getAccessToken(), getJocError(), null);
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