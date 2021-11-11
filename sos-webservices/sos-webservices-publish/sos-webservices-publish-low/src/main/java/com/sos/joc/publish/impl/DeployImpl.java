package com.sos.joc.publish.impl;

import java.io.IOException;
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

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.fileordersource.FileOrderSource;

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
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, DeployFilter.class);
            DeployFilter deployFilter = Globals.objectMapper.readValue(filter, DeployFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(deployFilter.getAuditLog(), CategoryType.DEPLOYMENT);

            Set<String> allowedControllerIds = Collections.emptySet();
            allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                    availableController, xAccessToken).getDeployments().getDeploy()).collect(Collectors.toSet());

            String account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // process filter
            Set<String> controllerIds = new HashSet<String>(deployFilter.getControllerIds());
            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> draftFoldersToStore = getDraftConfigurationFoldersToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> deployFoldersToStoreAgain = getDeployConfigurationFoldersToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);

            List<Config> foldersToDelete = null;
            if (deployFilter.getDelete() != null) {
                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
                if (!(foldersToDelete.size() == 1 && "/".equals(foldersToDelete.get(0).getConfiguration().getPath()))) {
                    foldersToDelete = PublishUtils.handleFolders(foldersToDelete, dbLayer);
                }
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
                configurationDBItemsToStore.addAll(PublishUtils.getValidDeployableDraftInventoryConfigurationsfromFolders(draftFoldersToStore,
                        dbLayer));
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToStore = null;
            if (!deployConfigsToStoreAgain.isEmpty()) {
                depHistoryDBItemsToStore = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
            }
            if (!deployFoldersToStoreAgain.isEmpty()) {
                if (depHistoryDBItemsToStore == null) {
                    depHistoryDBItemsToStore = new ArrayList<DBItemDeploymentHistory>();
                }
                depHistoryDBItemsToStore.addAll(PublishUtils.getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(deployFoldersToStoreAgain,
                        dbLayer));
            }
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
            if (deployConfigsToDelete != null && !deployConfigsToDelete.isEmpty()) {
                depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToDeployDelete.stream().collect(Collectors.groupingBy(
                            DBItemDeploymentHistory::getPath));
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
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No private key found for signing! - Please check your private key from the key management section in your profile.");
            }
            
            final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
            List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();
            // DeployAudit audit = null;
            // store to selected controllers
            for (String controllerId : controllerIds) {
                if (!allowedControllerIds.contains(controllerId)) {
                    continue;
                }
                folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

                // sign deployed configurations with new versionId
                // Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations =
                // new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
                Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
                // determine agent names to be replaced
                Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
                Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources = new HashSet<UpdateableFileOrderSourceAgentName>();
                // determine all (latest) entries from the given folders

                if (foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    foldersToDelete.stream().map(Config::getConfiguration).map(item -> dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(),
                            controllerId, item.getRecursive())).forEach(item -> itemsFromFolderToDelete.addAll(item));
                }
                if (unsignedDrafts != null) {
                    List<DBItemDeploymentHistory> filteredUnsignedDrafts = unsignedDrafts.stream().filter(draft -> canAdd(draft.getPath(),
                            permittedFolders)).map(item -> {
                                return PublishUtils.cloneInvCfgToDepHistory(item, account, controllerId, commitId, dbAuditlog.getId(), releasedScripts);
                            }).collect(Collectors.toList());
                    if (filteredUnsignedDrafts != null && !filteredUnsignedDrafts.isEmpty()) {
                        // // WORKAROUND: old items with leading slash
                        // PublishUtils.updatePathWithNameInContent(filteredUnsignedDrafts);
                        filteredUnsignedDrafts.stream().filter(item -> item.getType() == ConfigurationType.WORKFLOW.intValue()).forEach(
                                item -> {
                                    updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer));
                                    item.setCommitId(commitId);
                                });
                        filteredUnsignedDrafts.stream().filter(item -> item.getType() == ConfigurationType.JOBRESOURCE.intValue()).forEach(
                                item -> {
                                    item.setCommitId(commitId);
                                });
                        filteredUnsignedDrafts.stream().filter(item -> item.getType() == ConfigurationType.FILEORDERSOURCE.intValue()).forEach(
                                item -> {
                                    UpdateableFileOrderSourceAgentName update = PublishUtils.getUpdateableAgentRefInFileOrderSource(item,
                                            controllerId, dbLayer);
                                    try {
                                        ((FileOrderSource) item.readUpdateableContent()).setAgentPath(update.getAgentId());
                                        // Globals.objectMapper.readValue(item.readUpdateableContent(),
                                        // com.sos.inventory.model.fileordersource.FileOrderSource.class);
                                        // fileOrderSource.setAgentPath(update.getAgentId());
                                        // item.writeUpdateableContent(Globals.objectMapper.writeValueAsString(fileOrderSource));
                                        updateableAgentNamesFileOrderSources.add(update);
                                    } catch (Exception e) {
                                    }
                                });
                        verifiedDeployables.putAll(PublishUtils.getDraftsWithSignature(commitId, account, filteredUnsignedDrafts,
                                updateableAgentNames, keyPair, controllerId, hibernateSession));

                    }
                }
                // already deployed objects AgentName handling
                // all items will be signed or re-signed with current commitId
                if (unsignedReDeployables != null && !unsignedReDeployables.isEmpty()) {
                    // filter regarding folder permissions
                    List<DBItemDeploymentHistory> filteredUnsignedReDeployables = unsignedReDeployables.stream().filter(draft -> canAdd(draft
                            .getPath(), permittedFolders)).peek(item -> {
                                try {
                                    item.writeUpdateableContent(JsonConverter.readAsConvertedDeployObject(item.getPath(), item.getInvContent(),
                                            StoreDeployments.CLASS_MAPPING.get(item.getType()), commitId, releasedScripts));
                                } catch (IOException e) {
                                    throw new JocException(e);
                                }
                            }).collect(Collectors.toList());
                    if (!filteredUnsignedReDeployables.isEmpty()) {
                        // // WORKAROUND: old items with leading slash
                        // PublishUtils.updatePathWithNameInContent(filteredUnsignedReDeployables);
                        filteredUnsignedReDeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item
                                .getType()))).forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item,
                                        controllerId, dbLayer)));
                        filteredUnsignedReDeployables.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.equals(ConfigurationType.fromValue(
                                item.getType()))).forEach(item -> {
                                    UpdateableFileOrderSourceAgentName update = PublishUtils.getUpdateableAgentRefInFileOrderSource(item,
                                            controllerId, dbLayer);
                                    try {
                                        ((FileOrderSource) item.readUpdateableContent()).setAgentPath(update.getAgentId());
                                        updateableAgentNamesFileOrderSources.add(update);
                                    } catch (Exception e) {
                                    }
                                });
                        verifiedDeployables.putAll(PublishUtils.getDraftsWithSignature(commitId, account, filteredUnsignedReDeployables,
                                updateableAgentNames, keyPair, controllerId, hibernateSession));
                    }
                }
                // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename
                List<DBItemDeploymentHistory> toDeleteForRename = PublishUtils.checkRenamingForUpdate(verifiedDeployables.keySet(), controllerId,
                        dbLayer, keyPair.getKeyAlgorithm());
                // and subsequently call delete for the object with the previous path before committing the update
                if (toDeleteForRename != null && !toDeleteForRename.isEmpty()) {
                    // clone list as it has to be final now for processing in CompleteableFuture.thenAccept method
                    final List<DBItemDeploymentHistory> toDelete = toDeleteForRename;
                    // set new versionId for second round (delete items)
                    final String versionIdForDeleteRenamed = UUID.randomUUID().toString();
                    // call updateRepo command via Proxy of given controllers
                    DeleteDeployments.storeNewDepHistoryEntries(dbLayer, toDelete, versionIdForDeleteRenamed);
                    PublishUtils.updateItemsDelete(versionIdForDeleteRenamed, toDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDelete(either, controllerId, account, versionIdForDeleteRenamed, getAccessToken(),
                                getJocError());
                    });
                }
                if (verifiedDeployables != null && !verifiedDeployables.isEmpty()) {
                    SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, verifiedDeployables, updateableAgentNames,
                            updateableAgentNamesFileOrderSources, dbAuditlog.getId());
                    // call updateRepo command via ControllerApi for given controller
                    StoreDeployments.callUpdateItemsFor(dbLayer, signedItemsSpec, account, commitId, controllerId, getAccessToken(), getJocError(),
                            API_CALL);
                }
            }
            // Delete from all known controllers
            final String commitIdForDelete = UUID.randomUUID().toString();
            final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
            List<DBItemInventoryConfiguration> invConfigurationsToDelete = new ArrayList<DBItemInventoryConfiguration>();
            Map<String, List<DBItemDeploymentHistory>> itemsFromFolderToDeletePerController = new HashMap<String, List<DBItemDeploymentHistory>>();
            List<DBItemDeploymentHistory> filteredDepHistoryItemsToDelete = Collections.emptyList();
            List<DBItemDeploymentHistory> filteredItemsFromFolderToDelete = Collections.emptyList();
            // loop 1: store db entries optimistically
            for (String controllerId : allowedControllerIds) {
                folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                // store history entries for delete operation optimistically
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    filteredDepHistoryItemsToDelete = depHistoryDBItemsToDeployDelete.stream().filter(history -> canAdd(history.getPath(),
                            permittedFolders)).collect(Collectors.toList());
                    invConfigurationsToDelete.addAll(DeleteDeployments.getInvConfigurationsForTrash(dbLayer, DeleteDeployments
                            .storeNewDepHistoryEntries(dbLayer, filteredDepHistoryItemsToDelete, commitIdForDelete)));
                }
                if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // first filter for folder permissions
                    // remember filtered items for later
                    filteredItemsFromFolderToDelete = itemsFromFolderToDelete.stream().filter(fromFolder -> canAdd(fromFolder.getPath(),
                            permittedFolders)).collect(Collectors.toList());
                    // second filter for not already deleted
                    final List<DBItemDeploymentHistory> itemsToDelete = filteredItemsFromFolderToDelete.stream().filter(item -> item.getControllerId()
                            .equals(controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation()))).collect(Collectors
                                    .toList());
                    itemsFromFolderToDeletePerController.put(controllerId, itemsToDelete);
                    // store history entries for delete operation optimistically
                    invConfigurationsToDelete.addAll(DeleteDeployments.getInvConfigurationsForTrash(dbLayer, DeleteDeployments
                            .storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                    // audit = new DeployAudit(deployFilter.getAuditLog(), null, commitId, "delete", account);
                }
            }
            // delete configurations optimistically
            List<Configuration> folders = null;
            if (foldersToDelete != null) {
                folders = foldersToDelete.stream().map(item -> item.getConfiguration()).collect(Collectors.toList());
            }
            DeleteDeployments.deleteConfigurations(dbLayer, folders, invConfigurationsToDelete, commitIdForDeleteFromFolder, getAccessToken(),
                    getJocError(), dbAuditlog.getId(), withoutFolderDeletion);

            // loop 2: send commands to controllers
            for (String controllerId : allowedControllerIds) {
                if (filteredDepHistoryItemsToDelete != null && !filteredDepHistoryItemsToDelete.isEmpty()) {
                    // set new versionId for second round (delete items)
                    // call updateRepo command via Proxy of given controllers
                    final List<DBItemDeploymentHistory> toDelete = filteredDepHistoryItemsToDelete;
                    PublishUtils.updateItemsDelete(commitIdForDelete, toDelete, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForDelete, getAccessToken(), getJocError());
                    });
                }
                // process folder to Delete
                if (filteredItemsFromFolderToDelete != null && !filteredItemsFromFolderToDelete.isEmpty()) {
                    PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsFromFolderToDeletePerController.get(controllerId), controllerId)
                            .thenAccept(either -> {
                                DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForDelete, getAccessToken(),
                                        getJocError());
                            });
                }
            }
            // if (audit != null) {
            // logAuditMessage(audit);
            // storeAuditLogEntry(audit);
            // }
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

}