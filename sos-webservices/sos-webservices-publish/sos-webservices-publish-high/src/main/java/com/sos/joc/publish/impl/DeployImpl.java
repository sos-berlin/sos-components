package com.sos.joc.publish.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;

@Path("inventory/deployment")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./inventory/deployment/deploy";
    private DBLayerDeploy dbLayer = null;
    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) throws Exception {
        return postDeploy(xAccessToken, filter, false);
    }

    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter, boolean withoutFolderDeletion) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
//            initLogging(API_CALL, filter, xAccessToken);
//            JsonValidator.validate(filter, DeployFilter.class);
//            DeployFilter deployFilter = Globals.objectMapper.readValue(filter, DeployFilter.class);
//            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//            DBItemJocAuditLog dbAuditlog = storeAuditLog(deployFilter.getAuditLog(), CategoryType.DEPLOYMENT);
//            
//            Set<String> allowedControllerIds = Collections.emptySet();
//            allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream()
//            		.filter(availableController -> getControllerPermissions(availableController, xAccessToken).getDeployments().getDeploy()).collect(Collectors.toSet());
//
//            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
//            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
//            dbLayer = new DBLayerDeploy(hibernateSession);
//            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
//            // process filter
//            Set<String> controllerIds = new HashSet<String>(deployFilter.getControllerIds());
//            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
//            /*
//             * TODO: - check for configurationIds with -marked-for-delete- set - get all deployments from history related to the given configurationId - get all
//             * controllers from those deployments - delete all those existing deployments from all determined controllers
//             **/
//            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
//            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
//            
//            List<Config> foldersToDelete = null;
//            if (deployFilter.getDelete() != null) {
//                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
//                .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
//                if (!(foldersToDelete.size() == 1 && "/".equals(foldersToDelete.get(0).getConfiguration().getPath()))) {
//                    foldersToDelete = PublishUtils.handleFolders(foldersToDelete, dbLayer);
//                }
//            }
//
//            // read all objects provided in the filter from the database
//            List<DBItemInventoryConfiguration> configurationDBItemsToStore = null;
//            if (draftConfigsToStore != null) {
//                configurationDBItemsToStore = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore);
//            }
//            List<DBItemDeploymentHistory> depHistoryDBItemsToStore = null;
//            if (!deployConfigsToStoreAgain.isEmpty()) {
//                depHistoryDBItemsToStore = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
//            }
//            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
//            if (deployConfigsToDelete != null && !deployConfigsToDelete.isEmpty()) {
//                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
//                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
//                    Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToDeployDelete.stream()
//                            .collect(Collectors.groupingBy(DBItemDeploymentHistory::getPath));
//                    depHistoryDBItemsToDeployDelete = grouped.keySet().stream().map(item -> grouped.get(item).get(0)).collect(Collectors.toList());
//                }
//            }
//
//            Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = 
//                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
//            Map<DBItemDeploymentHistory, DBItemDepSignatures> signedDeployments = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
//
//            for (DBItemInventoryConfiguration update : configurationDBItemsToStore) {
//                DBItemDepSignatures signature = dbLayer.getSignature(update.getId());
//                if (signature != null) {
//                    signedDrafts.put(update, signature);
//                }
//            }
//            for (DBItemDeploymentHistory depHistory : depHistoryDBItemsToStore) {
//                DBItemDepSignatures signature = dbLayer.getSignature(depHistory.getId());
//                if (signature != null) {
//                    signedDeployments.put(depHistory, signature);
//                }
//            }
//            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations =
//                    new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
//            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables = 
//                    new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
//            String versionId = null;
//            // only signed objects will be processed
//            // existing signatures of objects are verified
//            for (DBItemInventoryConfiguration draft : signedDrafts.keySet()) {
//                if (versionId == null) {
//                    versionId = dbLayer.getVersionId(draft);
//                }
//                verifiedConfigurations.put(PublishUtils.verifySignature(account, draft, signedDrafts.get(draft), hibernateSession,
//                        JocSecurityLevel.HIGH), signedDrafts.get(draft));
//            }
//            for (DBItemDeploymentHistory deployed : signedDeployments.keySet()) {
//                verifiedReDeployables.put(PublishUtils.verifySignature(account, deployed, signedDeployments.get(deployed), hibernateSession,
//                        JocSecurityLevel.HIGH), signedDeployments.get(deployed));
//            }
//            final String commitId = versionId;
//            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
//            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
//            if (keyPair == null) {
//                throw new JocMissingKeyException(
//                        "No public key or X.509 Certificate found for signature verification! - "
//                        + "Please check your key from the key management section in your profile.");
//            }
//            List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();
//
//            //DeployAudit audit = null;
//            // call ControllerApi for all provided and allowed Controllers
//            for (String controllerId : controllerIds) {
//            	if (!allowedControllerIds.contains(controllerId)) {
//            		continue;
//            	}
//            	// filter for folder permissions
//            	folderPermissions.setSchedulerId(controllerId);
//                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
////                Map<DBItemInventoryConfiguration, DBItemDepSignatures> filteredConfigurations = verifiedConfigurations.entrySet().stream()
////                		.filter(entry -> canAdd(entry.getKey().getPath(), permittedFolders)).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//                Map<DBItemDeploymentHistory, DBItemDepSignatures> filteredDeployments = verifiedReDeployables.entrySet().stream()
//                		.filter(entry -> canAdd(entry.getKey().getPath(), permittedFolders)).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//                // store new history entries and update inventory for update operation optimistically
//                SignedItemsSpec spec = new SignedItemsSpec(keyPair, filteredConfigurations, filteredDeployments, null, null, dbAuditlog.getId());
//                StoreDeployments.storeNewDepHistoryEntries(spec, account, commitId, controllerId, getAccessToken(), getJocError(), dbLayer);
//                // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
//                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkRenamingForUpdate(filteredConfigurations.keySet(), controllerId, dbLayer, 
//                		keyPair.getKeyAlgorithm());
//                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
//                    toDeleteForRename.addAll(PublishUtils.checkRenamingForUpdate(filteredDeployments.keySet(), controllerId, dbLayer, keyPair.getKeyAlgorithm()));
//                } else {
//                    toDeleteForRename = PublishUtils.checkRenamingForUpdate(filteredDeployments.keySet(), controllerId, dbLayer, keyPair.getKeyAlgorithm());
//                }
//                // and subsequently call delete for the object with the previous path before committing the update
//                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
//                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
//                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
//                    // set new versionId for second round (delete items)
//                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
//                    DeleteDeployments.storeNewDepHistoryEntries(dbLayer, toDelete, versionIdForDeleteRenamed);
//                    // call updateRepo command via Proxy of given controllers
//                    PublishUtils.updateItemsDelete(versionIdForDeleteRenamed, toDelete, controllerId).thenAccept(either -> {
//                        DeleteDeployments.processAfterDelete(either, controllerId, account, versionIdForDeleteRenamed, getAccessToken(), getJocError());
//                    });
//                }
//                // determine all (latest) entries from the given folders
//                if(foldersToDelete != null && !foldersToDelete.isEmpty()) {
//                    foldersToDelete.stream()
//                        .map(Config::getConfiguration)
//                        .map(item -> dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId))
//                            .forEach(item -> itemsFromFolderToDelete.addAll(item));
//                }
//                if ((filteredConfigurations != null && !filteredConfigurations.isEmpty())
//                        || (filteredDeployments != null && !filteredDeployments.isEmpty())) {
//                    //audit = new DeployAudit(deployFilter.getAuditLog(), controllerId, commitId, "update", account);
//                    // call updateRepo command via ControllerApi for given controllers
//                    boolean verified = false;
//                    String signerDN = null;
//                    X509Certificate cert = null;
//                    switch (keyPair.getKeyAlgorithm()) {
//                    case SOSKeyConstants.PGP_ALGORITHM_NAME:
//                        PublishUtils.updateItemsAddOrUpdatePGP(commitId, filteredConfigurations, filteredDeployments, controllerId, 
//                                dbLayer).thenAccept(either -> {
//                                    StoreDeployments.processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError(), API_CALL);
//                                });
//                        break;
//                    case SOSKeyConstants.RSA_ALGORITHM_NAME:
//                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
//                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
//                        if (verified) {
//                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, filteredConfigurations, filteredDeployments, controllerId,
//                                    dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate())
//                                .thenAccept(either -> {
//                                    StoreDeployments.processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError(), API_CALL);
//                                    });
//                        } else {
//                          signerDN = cert.getSubjectDN().getName();
//                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, filteredConfigurations, filteredDeployments, controllerId,
//                                  dbLayer, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN)
//                              .thenAccept(either -> {
//                                  StoreDeployments.processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError(), API_CALL);
//                                  });
//                        }
//                        break;
//                    case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
//                        cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
//                        verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
//                        if (verified) {
//                            PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, filteredConfigurations, filteredDeployments, controllerId,
//                                    dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate())
//                                .thenAccept(either -> {
//                                    StoreDeployments.processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError(), API_CALL);
//                                    });
//                        } else {
//                          signerDN = cert.getSubjectDN().getName();
//                          PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, filteredConfigurations, filteredDeployments, controllerId,
//                                  dbLayer, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN)
//                              .thenAccept(either -> {
//                                  StoreDeployments.processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError(), API_CALL);
//                                  });
//                        }
//                        break;
//                    }
//                }
//            }
//
//            Set<DBItemInventoryConfiguration> invConfigurationsToDelete = new HashSet<DBItemInventoryConfiguration>();
//            Map<String, List<DBItemDeploymentHistory>> itemsFromFolderToDeletePerController = new HashMap<String, List<DBItemDeploymentHistory>>();
//            final String commitIdForDelete = UUID.randomUUID().toString();
//            final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
//            List<DBItemDeploymentHistory> filteredItemsToDelete = Collections.emptyList();
//            List<DBItemDeploymentHistory> filteredItemsFromFolderToDelete = Collections.emptyList();
//            // loop 1: store db entries optimistically
//            for (String controllerId : allowedControllerIds) {
//            	// filter for folder permissions
//            	folderPermissions.setSchedulerId(controllerId);
//                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
//
//            	// store history entries for delete operation optimistically
//                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
//                	filteredItemsToDelete = depHistoryDBItemsToDeployDelete.stream()
//                			.filter(item -> canAdd(item.getPath(), permittedFolders)).collect(Collectors.toList());
//                	
//                    invConfigurationsToDelete.addAll(
//                            DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
//                                    DeleteDeployments.storeNewDepHistoryEntries(dbLayer, filteredItemsToDelete, commitIdForDelete)));
//                }
//                if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
//                    // store history entries for delete operation optimistically
//                    final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream()
//                    		.filter(item -> item.getControllerId().equals(controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation())))
//                    		.filter(item -> canAdd(item.getPath(), permittedFolders))
//                    		.collect(Collectors.toList());
//                	filteredItemsFromFolderToDelete = itemsToDelete;
//                    itemsFromFolderToDeletePerController.put(controllerId, itemsToDelete);
//                    invConfigurationsToDelete.addAll(
//                            DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
//                                    DeleteDeployments.storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
//                    //audit = new DeployAudit(deployFilter.getAuditLog(), null, commitId, "delete", account);
//                }
//            }
//            // delete configurations optimistically
//            List<Configuration> folders = null;
//            if (foldersToDelete != null) {
//                folders = foldersToDelete.stream().map(item -> item.getConfiguration()).collect(Collectors.toList());
//            }
//            DeleteDeployments.deleteConfigurations(dbLayer, folders, invConfigurationsToDelete, commitIdForDeleteFromFolder, getAccessToken(), 
//                    getJocError(), dbAuditlog.getId(), withoutFolderDeletion);
//            // loop 2: send commands to controllers
//            for (String controllerId : allowedControllerIds) {
//                if (filteredItemsToDelete != null && !filteredItemsToDelete.isEmpty()) {
//                    // call updateRepo command via Proxy of given controllers
//                    final List<DBItemDeploymentHistory> toDelete = filteredItemsToDelete;
//                    PublishUtils.updateItemsDelete(commitIdForDelete, toDelete, controllerId).thenAccept(either -> {
//                        DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForDelete, getAccessToken(), getJocError());
//                    });
//                }
//                // process folder to Delete
//                if (filteredItemsFromFolderToDelete != null && !filteredItemsFromFolderToDelete.isEmpty()) {
//                    PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsFromFolderToDeletePerController.get(controllerId), controllerId)
//                        .thenAccept(either -> {
//                            DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForDelete, getAccessToken(), getJocError());
//                        }); 
//                } 
//            }
////            if (audit != null) {
////                logAuditMessage(audit);
////                storeAuditLogEntry(audit);
////            }
//            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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