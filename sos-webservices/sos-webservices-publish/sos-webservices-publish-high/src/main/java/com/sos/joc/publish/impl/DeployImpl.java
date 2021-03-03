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
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
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
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;

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
            final String commitId = versionId;
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No public key or X.509 Certificate found for signature verification! - "
                        + "Please check your key from the key management section in your profile.");
            }
            // call UpdateRepo for all provided Controllers
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
            List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();

            for (String controllerId : controllerIds) {
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
                    PublishUtils.updateItemsDelete(versionIdForDeleteRenamed, toDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDelete(
                                either, toDelete, controllerId, account, versionIdForDeleteRenamed, getAccessToken(), getJocError());
                    });
                }
                // determine all (latest) entries from the given folders
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
                        PublishUtils.updateItemsAddOrUpdatePGP(commitId, verifiedConfigurations, verifiedReDeployables, controllerId, 
                                dbLayer).thenAccept(either -> {
                                    StoreDeployments.processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, commitId, controllerId, 
                                            getAccessToken(), getJocError(), API_CALL);
                                });
                        break;
                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                        if (verified) {
                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                    dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate())
                                .thenAccept(either -> {
                                    StoreDeployments.processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, commitId, controllerId, 
                                            getAccessToken(), getJocError(), API_CALL);
                                    });
                        } else {
                          signerDN = cert.getSubjectDN().getName();
                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                  dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN)
                              .thenAccept(either -> {
                                  StoreDeployments.processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, commitId, controllerId, 
                                          getAccessToken(), getJocError(), API_CALL);
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
                                    StoreDeployments.processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, commitId, controllerId, 
                                            getAccessToken(), getJocError(), API_CALL);
                                    });
                        } else {
                          signerDN = cert.getSubjectDN().getName();
                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, verifiedConfigurations, verifiedReDeployables, controllerId,
                                  dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN)
                              .thenAccept(either -> {
                                  StoreDeployments.processAfterAdd(either, verifiedConfigurations, verifiedReDeployables, account, commitId, controllerId, 
                                          getAccessToken(), getJocError(), API_CALL);
                                  });
                        }
                        break;
                    }
                }
                // store new history entries and update inventory for update operation optimistically
                StoreDeployments.storeNewDepHistoryEntries(verifiedConfigurations, null, verifiedReDeployables, account, commitId, controllerId, 
                        unmodified, getAccessToken(), getJocError(), dbLayer);
            }
            Set<DBItemInventoryConfiguration> invConfigurationsToDelete = Collections.emptySet();
            final String commitIdForDelete = UUID.randomUUID().toString();
            final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
            for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    // set new versionId for second round (delete items)
                    // call updateRepo command via Proxy of given controllers
                    final List<DBItemDeploymentHistory> toDelete = depHistoryDBItemsToDeployDelete;
                    PublishUtils.updateItemsDelete(commitIdForDelete, toDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDelete(either, toDelete, controllerId, account, commitIdForDelete, getAccessToken(),
                                getJocError());
                    });
                    // store history entries for delete operation optimistically
                    if (invConfigurationsToDelete.isEmpty()) {
                        invConfigurationsToDelete = new HashSet<>(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, toDelete, commitIdForDelete)));
                    } else {
                        invConfigurationsToDelete.addAll(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, toDelete, commitIdForDelete)));
                    }
                }
                // process folder to Delete
                if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // determine all (latest) entries from the given folder
                    final List<Config> folders = foldersToDelete;
                    final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream().filter(item -> item.getControllerId().equals(
                            controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation()))).collect(Collectors.toList());
                    PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsToDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDeleteFromFolder(either, itemsToDelete, folders.stream().map(item -> item.getConfiguration())
                                .collect(Collectors.toList()), controllerId, account, commitIdForDelete, getAccessToken(), getJocError(), false);
                    });
                    // store history entries for delete operation optimistically
                    if (invConfigurationsToDelete.isEmpty()) {
                        invConfigurationsToDelete = new HashSet<>(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                    } else {
                        invConfigurationsToDelete.addAll(
                                DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
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

}