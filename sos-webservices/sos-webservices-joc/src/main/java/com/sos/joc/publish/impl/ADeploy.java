package com.sos.joc.publish.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.PublishSemaphore;
import com.sos.joc.classes.inventory.ReleaseDeploySemaphore;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
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
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.joc.publish.util.UpdateItemUtils;
import com.sos.sign.model.fileordersource.FileOrderSource;

import js7.proxy.javaapi.JControllerProxy;

public abstract class ADeploy extends JOCResourceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADeploy.class);
    
    public static final String API_CALL = "./inventory/deployment/deploy";
    private static final String SEMAPHORE_ID = "DEPLOY";


    public void deploy(String xAccessToken, DeployFilter deployFilter, DBItemJocAuditLog dbAuditlog, JocSecurityLevel secLvl, String apiCall)
            throws Exception {
        String account;
        SOSHibernateSession session = null;
        try {
            if (JocSecurityLevel.HIGH.equals(secLvl)) {
                throw new JocNotImplementedException("This operation is not available for Security Level HIGH, use <import_deploy> instead.");
            } else if(JocSecurityLevel.LOW.equals(secLvl)) {
                account =  ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            } else {
                account = this.getAccount();
            }
            if(account == null) {
                JocError error = new JocError("cannot determine account for signing.");
                throw new JocException(error);
            }
            if (PublishSemaphore.availablePermits(deployFilter.getTransactionId()) == 1) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            LOGGER.debug("acquire semaphore from deploy with AT " + deployFilter.getTransactionId());
            PublishSemaphore.tryAcquire(deployFilter.getTransactionId(), SEMAPHORE_ID);
            
            Set<String> allowedControllerIds = Collections.emptySet();
            allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> 
                    getBasicControllerPermissions(availableController, xAccessToken).getDeployments().getDeploy()).collect(Collectors.toSet());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer  = new DBLayerDeploy(session);
            // process filter
            Set<String> controllerIds = new HashSet<String>(deployFilter.getControllerIds());
            List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> draftFoldersToStore = getDraftConfigurationFoldersToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
            List<Configuration> deployFoldersToStoreAgain = getDeployConfigurationFoldersToStoreFromFilter(deployFilter);
            List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
            
            List<Config> foldersToDelete = null;
            if (deployFilter.getDelete() != null) {
                foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
                .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER)).collect(Collectors.toList());
                if (!(foldersToDelete.size() == 1 && "/".equals(foldersToDelete.get(0).getConfiguration().getPath()))) {
                    foldersToDelete = PublishUtils.handleFolders(foldersToDelete, dbLayer);
                }
            }
            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> configurationDBItemsToStore = null;
            if (!draftConfigsToStore.isEmpty()) {
                configurationDBItemsToStore = dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore, true);
            }
            /*
             * get all objects from INV_CONFIGURATION with deployed = false
             * 
             *                  START
             * */
            if (!draftFoldersToStore.isEmpty()) {
                if (configurationDBItemsToStore == null) {
                    configurationDBItemsToStore = new ArrayList<DBItemInventoryConfiguration>();
                }
                configurationDBItemsToStore.addAll(PublishUtils.getValidDeployableDraftInventoryConfigurationsfromFolders(draftFoldersToStore, dbLayer));
            }
            
            /*
             * get all objects from INV_CONFIGURATION with deployed = false
             * 
             *                  END
             * */
            List<DBItemDeploymentHistory> depHistoryDBItemsToStore = null;
            if (!deployConfigsToStoreAgain.isEmpty()) {
                depHistoryDBItemsToStore = dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain);
            }
            /*
             * get all latest objects from DEP_HISTORY where INV_CONFIGURATION object has deployed = true
             * 
             *                  START
             * */
            if (!deployFoldersToStoreAgain.isEmpty()) {
                if (depHistoryDBItemsToStore == null) {
                    depHistoryDBItemsToStore = new ArrayList<DBItemDeploymentHistory>();
                }
                depHistoryDBItemsToStore.addAll(PublishUtils.getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(deployFoldersToStoreAgain, dbLayer));
            }
            /*
             * get all latest objects from DEP_HISTORY where INV_CONFIGURATION object has deployed = true
             * 
             *                  END
             * */
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

            DBLayerKeys dbLayerKeys = new DBLayerKeys(session);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, secLvl);
            if (keyPair == null) {
                throw new JocMissingKeyException(
                        "No private key found for signing! - Please check your private key from the key management section in your profile.");
            }
            
            final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
            List<DBItemDeploymentHistory> itemsFromFolderToDelete = new ArrayList<DBItemDeploymentHistory>();
            
            // store to selected controllers
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(dbLayer.getSession());
            Map<String, Map<String, Set<String>>> agentsWithAliasesByControllerId = agentDbLayer.getAgentWithAliasesByControllerIds(controllerIds);

            for (String controllerId : controllerIds) {
                if (!allowedControllerIds.contains(controllerId)) {
                    continue;
                }
                folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

                // sign deployed configurations with new versionId
                Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
                // determine agent names to be replaced
                Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
                Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources = new HashSet<UpdateableFileOrderSourceAgentName>();
                // determine all (latest) entries from the given folders

                if (foldersToDelete != null && !foldersToDelete.isEmpty()) {
                    itemsFromFolderToDelete.addAll(foldersToDelete.stream().map(Config::getConfiguration).flatMap(item -> dbLayer
                            .getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId, item.getRecursive())).collect(Collectors.toSet()));

                }
                if (unsignedDrafts != null) {
                    List<DBItemDeploymentHistory> filteredUnsignedDrafts = unsignedDrafts.stream()
                            .filter(draft -> canAdd(draft.getPath(), permittedFolders))
                            .map(item -> PublishUtils.cloneInvCfgToDepHistory(item, account, controllerId, commitId, dbAuditlog.getId(), releasedScripts))
                            .collect(Collectors.toList());
                    if(filteredUnsignedDrafts != null && !filteredUnsignedDrafts.isEmpty()) {
                        filteredUnsignedDrafts.stream()
                            .filter(item -> item.getType() == ConfigurationType.WORKFLOW.intValue())
                            .forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(agentsWithAliasesByControllerId, item, controllerId)));
                        filteredUnsignedDrafts.stream()
                            .filter(item -> item.getType() == ConfigurationType.FILEORDERSOURCE.intValue())
                            .forEach(item -> {
                                UpdateableFileOrderSourceAgentName update = PublishUtils.getUpdateableAgentRefInFileOrderSource(agentsWithAliasesByControllerId, item, controllerId);
                                try {
                                    ((FileOrderSource)item.readUpdateableContent()).setAgentPath(update.getAgentId());
                                    updateableAgentNamesFileOrderSources.add(update);
                                } catch (Exception e) {}
                            });
                        verifiedDeployables.putAll(PublishUtils.getDraftsWithSignature(
                                commitId, account, filteredUnsignedDrafts, updateableAgentNames, keyPair, controllerId, session));
                    }
                }
                // already deployed objects AgentName handling
                // all items will be signed or re-signed with current commitId
                if (unsignedReDeployables != null && !unsignedReDeployables.isEmpty()) {
                    // filter regarding folder permissions
                    List<DBItemDeploymentHistory> filteredUnsignedReDeployables = unsignedReDeployables.stream()
                            .filter(draft -> canAdd(draft.getPath(), permittedFolders)).map(dbItem -> cloneToNew(dbItem))
                            .peek(item -> {
                                try {
                                    item.writeUpdateableContent(JsonConverter.readAsConvertedDeployObject(controllerId, item.getPath(), item
                                            .getInvContent(), StoreDeployments.CLASS_MAPPING.get(item.getType()), commitId, releasedScripts));
                                    item.setCommitId(commitId);
                                } catch (IOException e) {
                                    throw new JocException(e);
                                }
                            }).collect(Collectors.toList());
                    if (!filteredUnsignedReDeployables.isEmpty()) {
                        filteredUnsignedReDeployables.stream()
                            .filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType())))
                            .forEach(item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(agentsWithAliasesByControllerId, item, controllerId)));
                        filteredUnsignedReDeployables.stream()
                            .filter(item -> ConfigurationType.FILEORDERSOURCE.equals(ConfigurationType.fromValue(item.getType())))
                            .forEach(item -> {
                                UpdateableFileOrderSourceAgentName update = PublishUtils.getUpdateableAgentRefInFileOrderSource(agentsWithAliasesByControllerId, item, controllerId);
                                try {
                                    ((FileOrderSource)item.readUpdateableContent()).setAgentPath(update.getAgentId());
                                    updateableAgentNamesFileOrderSources.add(update);
                                } catch (Exception e) {}
                            });
                        verifiedDeployables.putAll(PublishUtils.getDraftsWithSignature(
                                commitId, account, filteredUnsignedReDeployables, updateableAgentNames, keyPair, controllerId, session));
                    }
                }
                // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename 
                Set<DBItemDeploymentHistory> renamedOriginalHistoryEntries = UpdateItemUtils
                        .checkRenamingForUpdate(verifiedDeployables.keySet(), controllerId, dbLayer);
                if (verifiedDeployables != null && !verifiedDeployables.isEmpty()) {
                    if (deployFilter.getAddOrdersDateFrom() != null ) {
                        
                        DailyPlanOrderFilterDef orderFilter = CancelOrdersPublishHelper.getDailyPlanOrderFilter(verifiedDeployables.keySet(), 
                                Optional.of(renamedOriginalHistoryEntries), deployFilter.getAddOrdersDateFrom(), controllerId);

                        PublishSemaphore.getInstance().getSemaphore(deployFilter.getTransactionId()).map(ReleaseDeploySemaphore::getWorkflowNames)
                            .ifPresent(set -> orderFilter.getWorkflowPaths().removeAll(set));
                        
                        List<CompletableFuture<ControllerCommandResponse>> cancelOrderResponse = CancelOrdersPublishHelper.getCancelOrderFutures(xAccessToken, orderFilter, null);
                        
                        CompletableFuture.allOf(cancelOrderResponse.toArray(CompletableFuture[]::new)).thenRun(() -> {
                            Map<Boolean, List<ControllerCommandResponse>> mappedFutures = cancelOrderResponse.stream().map(CompletableFuture::join)
                                    .collect(Collectors.groupingBy(ControllerCommandResponse::hasException));
                            mappedFutures.putIfAbsent(true, Collections.emptyList());
                            mappedFutures.putIfAbsent(false, Collections.emptyList());
                            
                            if(!mappedFutures.get(true).isEmpty()) {
                                // contains futures with errors
                                ProblemHelper.postExceptionsIfExist(mappedFutures.get(true), xAccessToken, getJocError());
                            } else {
                                SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, verifiedDeployables, updateableAgentNames,
                                        updateableAgentNamesFileOrderSources, dbAuditlog.getId());
                                // call updateRepo command via ControllerApi for given controller
                                SOSHibernateSession sessionAfterCancel = null;
                                try {
                                    sessionAfterCancel = Globals.createSosHibernateStatelessConnection("deploy-after-cancelOrders"); 
                                    StoreDeployments.callUpdateItemsFor(new DBLayerDeploy(sessionAfterCancel), signedItemsSpec, renamedOriginalHistoryEntries, account, commitId, controllerId,
                                            getAccessToken(), getJocError(), apiCall, deployFilter.getAddOrdersDateFrom(), deployFilter.getIncludeLate(), deployFilter.getTransactionId());
                                } catch (Exception e) {
                                    throw new JocDeployException(e);
                                } finally {
                                    Globals.disconnect(sessionAfterCancel);
                                }
                            }
                        });
                    } else {
                        SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, verifiedDeployables, updateableAgentNames,
                                updateableAgentNamesFileOrderSources, dbAuditlog.getId());
                        // call updateRepo command via ControllerApi for given controller
                        SOSHibernateSession sessionWithoutCancel = null;
                        try {
                            sessionWithoutCancel = Globals.createSosHibernateStatelessConnection("deploy"); 
                            StoreDeployments.callUpdateItemsFor(new DBLayerDeploy(sessionWithoutCancel), signedItemsSpec, renamedOriginalHistoryEntries, account, commitId, controllerId,
                                    getAccessToken(), getJocError(), apiCall, deployFilter.getAddOrdersDateFrom(), deployFilter.getIncludeLate(), deployFilter.getTransactionId());
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        } finally {
                            Globals.disconnect(sessionWithoutCancel);
                        }
                    }
                }
            }
            // Delete from all known controllers
            // set new versionId for second round (delete items)
            final String commitIdForDelete = UUID.randomUUID().toString();
            final String commitIdForDeleteFileOrderSources = UUID.randomUUID().toString();
            Set<DBItemInventoryConfiguration> invConfigurationsToDelete = new HashSet<DBItemInventoryConfiguration>();

            Map<String, List<DBItemDeploymentHistory>> itemsToDeletePerController = new HashMap<String, List<DBItemDeploymentHistory>>();
            // loop 1: store db entries optimistically
            for (String controllerId : allowedControllerIds) {
                List<DBItemDeploymentHistory> filteredDepHistoryItemsToDelete = new ArrayList<DBItemDeploymentHistory>();
                folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                // store history entries for delete operation optimistically
                if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                    filteredDepHistoryItemsToDelete.addAll(depHistoryDBItemsToDeployDelete.stream()
                            .filter(history -> canAdd(history.getPath(), permittedFolders))
                            .collect(Collectors.toList()));
                }
                if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                    // first filter for folder permissions
                    // second filter for not already deleted
                    // remember filtered items for later
                    filteredDepHistoryItemsToDelete.addAll(itemsFromFolderToDelete.stream()
                            .filter(fromFolder -> canAdd(fromFolder.getPath(), permittedFolders))
                            .filter(item -> item.getControllerId().equals(controllerId) 
                                    && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation())))
                            .collect(Collectors.toList()));
                }
                Map<Boolean, List<DBItemDeploymentHistory>> allItemsToDelete = filteredDepHistoryItemsToDelete.stream()
                        .collect(Collectors.groupingBy(fos -> DeployType.FILEORDERSOURCE.equals(fos.getTypeAsEnum())));
                // store history entries for delete operation optimistically
                invConfigurationsToDelete.addAll(DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, allItemsToDelete.get(true), commitIdForDeleteFileOrderSources, account ,dbAuditlog.getId())));
                invConfigurationsToDelete.addAll(DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                        DeleteDeployments.storeNewDepHistoryEntries(dbLayer, allItemsToDelete.get(false), commitIdForDelete, account ,dbAuditlog.getId())));
                itemsToDeletePerController.put(controllerId, filteredDepHistoryItemsToDelete);
            }
            // delete configurations optimistically from inventory
            List<Configuration> folders = null;
            if (foldersToDelete != null) {
                folders = foldersToDelete.stream().map(item -> item.getConfiguration()).collect(Collectors.toList());
            }
            DeleteDeployments.deleteConfigurations(dbLayer, folders, invConfigurationsToDelete, getAccessToken(), 
                    getJocError(), dbAuditlog.getId(), false);
            // loop 2: send commands to controllers
            for (String controllerId : allowedControllerIds) {
                // call updateRepo command via Proxy of given controllers
                if(itemsToDeletePerController.get(controllerId) != null && !itemsToDeletePerController.get(controllerId).isEmpty()) {
                    JControllerProxy proxy = Proxy.of(controllerId);
                    Map<Boolean, List<DBItemDeploymentHistory>> allItemsToDelete = itemsToDeletePerController.get(controllerId).stream()
                            .collect(Collectors.groupingBy(fos -> DeployType.FILEORDERSOURCE.equals(fos.getTypeAsEnum())));
                   if(allItemsToDelete.get(true) != null && !allItemsToDelete.get(true).isEmpty()) {
                       UpdateItemUtils.updateItemsDelete(commitIdForDeleteFileOrderSources, allItemsToDelete.get(true), proxy)
                       .thenAccept(either -> {
                           DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForDeleteFileOrderSources, xAccessToken, 
                                   getJocError(), deployFilter.getAddOrdersDateFrom(), allItemsToDelete.get(false), commitIdForDelete, 
                                   allItemsToDelete.get(true).stream().map(DBItemDeploymentHistory::getName).collect(Collectors.toSet()));
                       });
                   } else if(allItemsToDelete.get(false) != null && !allItemsToDelete.get(false).isEmpty()) {
                       UpdateItemUtils.updateItemsDelete(commitIdForDelete, allItemsToDelete.get(false), proxy).thenAccept(either -> {
                           DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForDelete, getAccessToken(), getJocError(),
                                   deployFilter.getAddOrdersDateFrom());
                       });
                   }
                }
            }
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private List<Configuration> getDraftConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).peek(item -> item.setCommitId(null)).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
   }
    
    private List<Configuration> getDraftConfigurationFoldersToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
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
    
    public static DBItemDeploymentHistory cloneToNew(DBItemDeploymentHistory oldItem) {
        DBItemDeploymentHistory newItem = new DBItemDeploymentHistory();
        newItem.setAccount(oldItem.getAccount());
        newItem.setAuditlogId(oldItem.getAuditlogId());
        newItem.setCommitId(oldItem.getCommitId());
        newItem.setContent(oldItem.getContent());
        newItem.setControllerId(oldItem.getControllerId());
        newItem.setControllerInstanceId(oldItem.getControllerInstanceId());
        newItem.setDeploymentDate(oldItem.getDeploymentDate());
        newItem.setFolder(oldItem.getFolder());
        newItem.setInvContent(oldItem.getInvContent());
        newItem.setInventoryConfigurationId(oldItem.getInventoryConfigurationId());
        newItem.setName(oldItem.getName());
        newItem.setOperation(oldItem.getOperation());
        newItem.setPath(oldItem.getPath());
        newItem.setSignedContent(oldItem.getSignedContent());
        newItem.setState(oldItem.getState());
        newItem.setTitle(oldItem.getTitle());
        newItem.setType(oldItem.getType());
        newItem.setVersion(oldItem.getVersion());
        newItem.writeUpdateableContent(oldItem.readUpdateableContent());
        return newItem;
    }
}