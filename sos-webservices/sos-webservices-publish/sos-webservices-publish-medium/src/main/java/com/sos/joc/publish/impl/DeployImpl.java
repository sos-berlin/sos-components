package com.sos.joc.publish.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
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
import com.sos.joc.publish.util.DeleteDeployments;
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
            // process filter
            Set<String> controllerIds = new HashSet<String>(deployFilter.getControllerIds());
            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> draftFoldersToStore = getDraftConfigurationFoldersToStoreFromFilter(deployFilter);
            /*
             * TODO: - check for configurationIds with -marked-for-delete- set - get all deployments from history related to the given configurationId - get all
             * controllers from those deployments - delete all those existing deployments from all determined controllers
             **/
            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> deployFoldersToStoreAgain = getDeployConfigurationFoldersToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            List<Config> foldersToDelete = null;
            if (deployFilter.getDelete() != null) {
                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
                .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
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
            final String commitId = UUID.randomUUID().toString();

            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.MEDIUM);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No private key found for signing! - Please check your private key from the key management section in your profile.");
            }
            List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();
            // store to selected controllers
            for (String controllerId : controllerIds) {
                // sign deployed configurations with new versionId
                Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations =
                        new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
                Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
                // determine agent names to be replaced
                Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
                // determine all (latest) entries from the given folders

                if(foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    foldersToDelete.stream()
                        .map(Config::getConfiguration)
                        .map(item -> dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId, item.getRecursive()))
                        .forEach(item -> itemsFromFolderToDelete.addAll(item));
                }

                if (unsignedDrafts != null) {
                    // WORKAROUND: old items with leading slash
                    PublishUtils.updatePathWithNameInContent(unsignedDrafts);
                    unsignedDrafts.stream()
                    .filter(item -> item.getTypeAsEnum().equals(ConfigurationType.WORKFLOW))
                    .forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                    verifiedConfigurations.putAll(PublishUtils.getDraftsWithSignature(
                            commitId, account, unsignedDrafts, updateableAgentNames, keyPair, controllerId, hibernateSession));
                }
                // already deployed objects AgentName handling
                // all items will be signed or re-signed with current commitId
                if (unsignedReDeployables != null && !unsignedReDeployables.isEmpty()) {
                    // WORKAROUND: old items with leading slash
                    PublishUtils.updatePathWithNameInContent(unsignedReDeployables);
                    unsignedReDeployables.stream()
                    .filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType())))
                    .forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                    verifiedReDeployables.putAll(
                            PublishUtils.getDeploymentsWithSignature(commitId, account, unsignedReDeployables, hibernateSession, 
                                    JocSecurityLevel.MEDIUM));
                }
                // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename 
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkPathRenamingForUpdate(
                        verifiedConfigurations.keySet(), controllerId, dbLayer, keyPair.getKeyAlgorithm());
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
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
                        PublishUtils.updateItemsDelete(versionIdForDeleteRenamed, toDelete, controllerId).thenAccept(either -> {
                            DeleteDeployments.processAfterDelete(either, toDelete, controllerId, account, versionIdForDeleteRenamed,
                                    getAccessToken(), getJocError());
                        });
                }
                if ((verifiedConfigurations != null && !verifiedConfigurations.isEmpty())
                        || (verifiedReDeployables != null && !verifiedReDeployables.isEmpty())) {
                    // call updateRepo command via ControllerApi for given controllers
                    boolean verified = false;
                    String signerDN = null;
                    X509Certificate cert = null;
                    switch(keyPair.getKeyAlgorithm()) {
                    case SOSKeyConstants.PGP_ALGORITHM_NAME:
                        PublishUtils.updateItemsAddOrUpdatePGP(commitId, verifiedConfigurations, verifiedReDeployables, controllerId, 
                                dbLayer).thenAccept(either -> {
                                    processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account, 
                                            commitId, controllerId, deployFilter, unmodified);
                        });
                        break;
                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                        if (verified) {
                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                    dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                                .thenAccept(either -> {
                                        processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                                commitId, controllerId, deployFilter, unmodified);
                                    });
                        } else {
                          signerDN = cert.getSubjectDN().getName();
                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                  dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN)
                              .thenAccept(either -> {
                                      processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                              commitId, controllerId, deployFilter, unmodified);
                                  });
                        }
                        break;
                    case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                        if (verified) {
                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                    dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                                .thenAccept(either -> {
                                        processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                                commitId, controllerId, deployFilter, unmodified);
                                    });
                        } else {
                          signerDN = cert.getSubjectDN().getName();
                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                  dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN)
                              .thenAccept(either -> {
                                      processAfterAdd(either, verifiedConfigurations, updateableAgentNames, verifiedReDeployables, account,
                                              commitId, controllerId, deployFilter, unmodified);
                                  });
                        }
                        break;
                    }
                }
            }
            // Delete from all known controllers
            final String commitIdForDelete = UUID.randomUUID().toString();
            final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
            Set<DBItemInventoryConfiguration> invConfigurationsToDelete = Collections.emptySet();

            for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    final List<DBItemDeploymentHistory> itemsToDelete = depHistoryDBItemsToDeployDelete;
                    PublishUtils.updateItemsDelete(commitIdForDelete, itemsToDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDelete(either, itemsToDelete, controllerId, account, commitIdForDelete, 
                                getAccessToken(), getJocError());
                    });
                    // store history entries optimistically
                    if (invConfigurationsToDelete.isEmpty()) {
                        invConfigurationsToDelete = new HashSet<>(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitId)));
                    } else {
                        invConfigurationsToDelete.addAll(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitId)));
                    }
//                    DeleteDeployments.storeDepHistoryAndDeleteSetOfConfigurations(dbLayer, itemsToDelete, commitIdForDelete);
                }
                // process folder to Delete
                if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // determine all (latest) entries from the given folder
                    final List<Config> folders = foldersToDelete;
                    final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream().filter(item -> item.getControllerId().equals(
                            controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation()))).collect(Collectors.toList());
                    PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsToDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDeleteFromFolder(either, itemsToDelete, 
                                folders.stream().map(item -> item.getConfiguration()).collect(Collectors.toList()),
                                controllerId, account, commitIdForDeleteFromFolder, getAccessToken(), getJocError(), false);
                    });
                    // store history entries optimistically
                    if (invConfigurationsToDelete.isEmpty()) {
                        invConfigurationsToDelete = new HashSet<>(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitId)));
                    } else {
                        invConfigurationsToDelete.addAll(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitId)));
                    }
                }
            }
            // delete configurations optimistically
            List<Configuration> folders = null;
            if (foldersToDelete != null) {
                folders = foldersToDelete.stream().map(item -> item.getConfiguration()).collect(Collectors.toList());
            }
            DeleteDeployments.deleteConfigurations(dbLayer, folders, invConfigurationsToDelete, commitIdForDeleteFromFolder, getAccessToken(), 
                    getJocError(), withoutFolderDeletion);

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

    private List<Configuration> getDraftConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
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

    private List<Configuration> getDeployConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
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

    private List<Configuration> getDeployConfigurationsToDeleteFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getDelete() != null) {
            return deployFilter.getDelete().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
          return new ArrayList<Configuration>();
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
            DeployFilter deployFilter,
            Set<DbItemConfWithOriginalContent> cfgsDBItemsToStore) {
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
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
}