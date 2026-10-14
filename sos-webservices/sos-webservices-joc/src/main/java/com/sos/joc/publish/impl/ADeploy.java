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
import java.util.function.Function;
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
import com.sos.joc.classes.publish.DeployAction;
import com.sos.joc.classes.publish.record.DeployTransportRecord;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.dailyplan.impl.DailyPlanCancelOrderImpl;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
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
import com.sos.joc.exceptions.JocReleaseException;
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
import com.sos.joc.publish.record.DeployRecord;
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
                account = this.getAccountName();
            }
            if(account == null) {
                JocError error = new JocError("cannot determine account for signing.");
                throw new JocException(error);
            }
            if (PublishSemaphore.availablePermits(deployFilter.getTransactionId()) == 1) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            // TODO timeout needed
            while(!PublishSemaphore.tryAcquire(deployFilter.getTransactionId(), SEMAPHORE_ID)) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
            LOGGER.debug("DEPLOY: acquire semaphore with transactionId " + deployFilter.getTransactionId());
            
            Set<String> allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> 
                    getBasicControllerPermissions(availableController).getDeployments().getDeploy()).collect(Collectors.toSet());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer  = new DBLayerDeploy(session);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(session);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, secLvl);
            if (keyPair == null || keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
                throw new JocMissingKeyException(
                        "No private key found for signing! - Please check your private key from the key management section in your profile.");
            }
            // process filter
            Set<String> controllerIds = deployFilter.getControllerIds().stream()
                    .filter(controllerId -> allowedControllerIds.contains(controllerId)).collect(Collectors.toSet());
            if(controllerIds.isEmpty() || allowedControllerIds.isEmpty()) {
                throw new JocDeployException("No controller to deploy to, due to missing permissions.");
            }
            // read all objects provided in the filter from the database
            List<Config> foldersToDelete = getFoldersToDelete(deployFilter, dbLayer);
            Set<DBItemDeploymentHistory> itemsFromFolderToDelete = getDepItemsFromFolders(controllerIds, deployFilter, dbLayer);
            // get all objects from INV_CONFIGURATION with deployed = false
            Set<DBItemInventoryConfiguration> unsignedDrafts = getInvDbItemsToStore(deployFilter, dbLayer);
            // get all latest objects from DEP_HISTORY where INV_CONFIGURATION object has deployed = true
            Set<DBItemDeploymentHistory> unsignedReDeployables = getDepDbItemsToStore(deployFilter, dbLayer);
            // set new versionId for first round (update items)
            final String commitId = UUID.randomUUID().toString();
            // set new versionId for second round (delete items)
            final String commitIdForDelete = UUID.randomUUID().toString();
            final String commitIdForDeleteFileOrderSources = UUID.randomUUID().toString();
            final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(session);
            // Map<ControllerId, Map<AgentId, Set<AgentNames and Aliases>>>
            Map<String, Map<String, Set<String>>> agentsWithAliasesByControllerId = agentDbLayer.getAgentWithAliasesByControllerIds(controllerIds);

            Long auditLogId = dbAuditlog.getId();
            // prepare items to deploy
            Map<String, DeployRecord> verifiedDeployablesPerController = 
                    prepareItemsPerController(controllerIds, commitId, account, keyPair, auditLogId, unsignedDrafts, unsignedReDeployables, 
                            agentsWithAliasesByControllerId, releasedScripts, deployFilter, dbLayer);
            // check Paths of ConfigurationObject and latest Deployment (if exists) to determine a rename 
            Map<String, Set<DBItemDeploymentHistory>> renamedOriginalEntriesPerController = new HashMap<>();
            controllerIds.forEach(controllerId -> {
                try {
                    renamedOriginalEntriesPerController.put(controllerId, UpdateItemUtils.checkRenamingForUpdate(
                            verifiedDeployablesPerController.get(controllerId).verifiedDeployables().keySet(), controllerId, dbLayer));
                } catch (Exception e) {
                    throw new JocException(e);
                }
            });
            // store new history entries and update inventory for update operation optimistically
            controllerIds.forEach(controllerId -> {
                DeleteDeployments.storeNewDepHistoryEntries(dbLayer, renamedOriginalEntriesPerController.get(controllerId),
                        commitId, null, account, auditLogId);
                if (!StoreDeployments.API_CALL_REDEPLOY.equals(apiCall) && !StoreDeployments.API_CALL_SYNC.equals(apiCall)) {
                    SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, 
                            verifiedDeployablesPerController.get(controllerId).verifiedDeployables(),
                            verifiedDeployablesPerController.get(controllerId).updateableAgentNames(),
                            verifiedDeployablesPerController.get(controllerId).updateableAgentNamesFileOrderSources(), dbAuditlog.getId());
                    StoreDeployments.storeNewDepHistoryEntries(signedItemsSpec, account, commitId, controllerId, xAccessToken, getJocError(), 
                            dbLayer, false);
                }
            });
            Map<String, List<DBItemDeploymentHistory>> itemsToDeletePerController = prepareItemsToDelete(allowedControllerIds, 
                    itemsFromFolderToDelete, dbLayer, account, commitIdForDelete, commitIdForDeleteFileOrderSources, foldersToDelete, auditLogId);

            DeployTransportRecord updateRecord = new DeployTransportRecord(account, commitId, this, apiCall, deployFilter.getAddOrdersDateFrom(), 
                    false, deployFilter.getTransactionId(), DeployAction.UPDATE);
            List<CompletableFuture<ControllerCommandResponse>> updateFutures = new ArrayList<>();
            // deploy to selected and delete from all allowed controllers
            for (String controllerId : allowedControllerIds) {
                CompletableFuture<ControllerCommandResponse> cancelOrderResponse = null;
                if (deployFilter.getAddOrdersDateFrom() != null) {
                    Set<DBItemDeploymentHistory> forCancelOrders = new HashSet<>();
                    if (controllerIds.contains(controllerId)) {
                        Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables = verifiedDeployablesPerController.get(controllerId)
                                .verifiedDeployables();
                        forCancelOrders.addAll(verifiedDeployables.keySet());
                    }
                    forCancelOrders.addAll(itemsToDeletePerController.get(controllerId));
                    DailyPlanOrderFilterDef orderFilter = CancelOrdersPublishHelper.getDailyPlanOrderFilter(forCancelOrders,
                            Optional.ofNullable(renamedOriginalEntriesPerController.get(controllerId)), deployFilter.getAddOrdersDateFrom(),
                            controllerId);
                    PublishSemaphore.getInstance().getSemaphore(deployFilter.getTransactionId()).map(ReleaseDeploySemaphore::getWorkflowNames)
                            .ifPresent(set -> orderFilter.getWorkflowPaths().removeAll(set));
                    LOGGER.debug("DEPLOY: already processed workflows from Semaphore information removed from order filter");
                    LOGGER.debug("DEPLOY: order cancel - future started");
                    cancelOrderResponse = getCancelOrderFuture(this, orderFilter, null);
                } else {
                    cancelOrderResponse = CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.empty()));
                }
                updateFutures.add(
                cancelOrderResponse.thenCompose(ccr -> {
                    if(ccr.hasException()) {
                        return CompletableFuture.completedFuture(new ControllerCommandResponse(ccr.getControllerId(), ccr.getException(), 
                                Optional.of(updateRecord)));
                    } else {
                        if(verifiedDeployablesPerController.get(controllerId) != null) {
                            SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, 
                                    verifiedDeployablesPerController.get(controllerId).verifiedDeployables(),
                                    verifiedDeployablesPerController.get(controllerId).updateableAgentNames(),
                                    verifiedDeployablesPerController.get(controllerId).updateableAgentNamesFileOrderSources(), dbAuditlog
                                            .getId());
                            SOSHibernateSession sessionAfterCancel = null;
                            try {
                                sessionAfterCancel = Globals.createSosHibernateStatelessConnection("deploy-after-cancelOrders");
                                return StoreDeployments.callUpdateItemsFor(new DBLayerDeploy(sessionAfterCancel), signedItemsSpec,
                                        renamedOriginalEntriesPerController.get(controllerId), controllerId, updateRecord);
                            } finally {
                                Globals.disconnect(sessionAfterCancel);
                            }
                        } else {
                            return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.empty(), 
                                    Optional.of(updateRecord)));
                        }
                    }
                }));
                CompletableFuture<ControllerCommandResponse> future = null;
                try {
                    future = processDelete(controllerId, itemsToDeletePerController, deployFilter, Proxy.of(controllerId), account,
                            commitIdForDelete, commitIdForDeleteFileOrderSources, apiCall);
                } catch (Exception e) {
                    DeployTransportRecord deleteRecord = new DeployTransportRecord(account, commitIdForDelete, this, apiCall, 
                            deployFilter.getAddOrdersDateFrom(), false, deployFilter.getTransactionId(), DeployAction.DELETE);
                    future = CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.of(e), Optional.of(deleteRecord)));
                }
                if(future != null) {
                    updateFutures.add(future);
                }
            }
            processUpdateFutures(updateFutures, deployFilter, xAccessToken);
        } catch(Exception e) {
            if(deployFilter.getTransactionId() != null) {
                try {
                    PublishSemaphore.release(deployFilter.getTransactionId());
                    LOGGER.debug("DEPLOY: error occurred - final release of semaphore from deploy with transactionId " 
                    + deployFilter.getTransactionId());
                    if(PublishSemaphore.getInstance().getSemaphore(deployFilter.getTransactionId())
                            .map(ReleaseDeploySemaphore::getInitialCaller).filter(SEMAPHORE_ID::equals).isPresent()) {
                        PublishSemaphore.remove(deployFilter.getTransactionId());
                        LOGGER.debug("DEPLOY: error occurred - final remove of semaphore from deploy with transactionId " 
                        + deployFilter.getTransactionId());
                    }
                } catch (Exception e1) {}
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private CompletableFuture<ControllerCommandResponse> processDelete (String controllerId, 
            Map<String, List<DBItemDeploymentHistory>> itemsToDeletePerController, DeployFilter deployFilter, JControllerProxy proxy,
            String account, String commitIdForDelete, String commitIdForDeleteFileOrderSources, String apiCall) {
        CompletableFuture<ControllerCommandResponse> future = null;
        if(itemsToDeletePerController.get(controllerId) != null && !itemsToDeletePerController.get(controllerId).isEmpty()) {
            Map<Boolean, List<DBItemDeploymentHistory>> allItemsToDelete = itemsToDeletePerController.get(controllerId).stream()
                    .collect(Collectors.groupingBy(fos -> DeployType.FILEORDERSOURCE.equals(fos.getTypeAsEnum())));
           if(allItemsToDelete.get(true) != null && !allItemsToDelete.get(true).isEmpty()) {
               Set<String> fileOrderSourceNames = allItemsToDelete.get(true).stream().map(DBItemDeploymentHistory::getName)
                       .collect(Collectors.toSet());
               DeployTransportRecord record = new DeployTransportRecord(account, commitIdForDeleteFileOrderSources, this, apiCall, 
                       deployFilter.getAddOrdersDateFrom(), false, deployFilter.getTransactionId(), DeployAction.DELETE);
               future = DeleteDeployments.getDeleteItemsFuture(proxy, allItemsToDelete.get(true), controllerId, record).thenCompose(ccr -> {
                   if (ccr.hasException()) {
                       return CompletableFuture.completedFuture(ccr);
                   } else {
                       if(DeleteDeployments.checkFOSdeleted(fileOrderSourceNames, proxy)) {
                           DeployTransportRecord record2 = new DeployTransportRecord(account, commitIdForDelete, this, apiCall, 
                                   deployFilter.getAddOrdersDateFrom(), false, deployFilter.getTransactionId(), DeployAction.DELETE);
                           return DeleteDeployments.getDeleteItemsFuture(proxy, allItemsToDelete.get(false), controllerId, record2);
                       } else {
                           return CompletableFuture.completedFuture(ccr);
                       }
                   }
               });
           } else if(allItemsToDelete.get(false) != null && !allItemsToDelete.get(false).isEmpty()) {
               DeployTransportRecord record = new DeployTransportRecord(account, commitIdForDelete, this, apiCall, 
                       deployFilter.getAddOrdersDateFrom(), false, deployFilter.getTransactionId(), DeployAction.DELETE);
               future = DeleteDeployments.getDeleteItemsFuture(proxy, allItemsToDelete.get(false), controllerId, record);
           }
        }
        return future;
    }
    
    private void processUpdateFutures(List<CompletableFuture<ControllerCommandResponse>> futures, DeployFilter deployFilter, String xAccessToken) {
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            List<CompletableFuture<ControllerCommandResponse>> processAfterFutures = new ArrayList<>();
            futures.stream().map(CompletableFuture::join).forEach(ccr -> {
                if (DeployAction.UPDATE.equals(ccr.getDeployTransportRecord().get().action())) {
                    processAfterFutures.add(StoreDeployments.processAfterAdd(ccr.hasException() ? ccr.getException().get() : null, ccr
                            .getControllerId(), ccr.getDeployTransportRecord().get()));
                } else {
                    processAfterFutures.add(DeleteDeployments.processAfterDelete(ccr.hasException() ? ccr.getException().get() : null, ccr
                            .getControllerId(), ccr.getDeployTransportRecord().get()));
                }
            });
            // process when all asynchronous (processAfter) futures are finished
            processAfterUpdatesFutures(processAfterFutures, deployFilter, xAccessToken);
        });
    }
    
    private void processAfterUpdatesFutures(List<CompletableFuture<ControllerCommandResponse>> processAfterFutures, DeployFilter deployFilter, String xAccessToken) {
        CompletableFuture.allOf(processAfterFutures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            Map<Boolean, List<ControllerCommandResponse>> mappedFutures = processAfterFutures.stream().map(CompletableFuture::join)
                    .collect(Collectors.groupingBy(ControllerCommandResponse::hasException));
            // futures with exceptions
            mappedFutures.putIfAbsent(true, Collections.emptyList());
            // futures without exception
            mappedFutures.putIfAbsent(false, Collections.emptyList());
            if(!mappedFutures.get(true).isEmpty()) {
                ProblemHelper.postExceptionsIfExist(mappedFutures.get(true), xAccessToken, getJocError());
            }
            if(deployFilter.getTransactionId() != null) {
                try {
                    PublishSemaphore.release(deployFilter.getTransactionId());
                    LOGGER.debug("DEPLOY: final release of semaphore from deploy with transactionId " + deployFilter.getTransactionId());
                    if(PublishSemaphore.getInstance().getSemaphore(deployFilter.getTransactionId())
                            .map(ReleaseDeploySemaphore::getInitialCaller).filter(str -> str.equals(SEMAPHORE_ID)).isPresent()) {
                        PublishSemaphore.remove(deployFilter.getTransactionId());
                        LOGGER.debug("DEPLOY: final remove of semaphore from deploy with transactionId " + deployFilter.getTransactionId());
                    }
                } catch (Exception e) {}
            }
        });
    }
    
    private Map<String, DeployRecord> prepareItemsPerController(Set<String> controllerIds, String commitId, 
            String account, JocKeyPair keyPair, Long auditLogId, Set<DBItemInventoryConfiguration> unsignedDrafts, 
            Set<DBItemDeploymentHistory> unsignedReDeployables, Map<String, Map<String, Set<String>>> agentsWithAliasesByControllerId, 
            Map<String, String> releasedScripts, DeployFilter deployFilter, DBLayerDeploy dbLayer) throws Exception {
        Map<String, DeployRecord> signedDeployablesPerController = new HashMap<>();
        for (String controllerId : controllerIds) {
            folderPermissions.setSchedulerId(controllerId);
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            // sign deployed configurations with new versionId
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
            // determine agent names to be replaced
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources = new HashSet<UpdateableFileOrderSourceAgentName>();
            // determine all (latest) entries from the given folders

            if (unsignedDrafts != null) {
                List<DBItemDeploymentHistory> filteredUnsignedDrafts = unsignedDrafts.stream()
                        .filter(draft -> canAdd(draft.getPath(), permittedFolders))
                        .map(item -> PublishUtils.cloneInvCfgToDepHistory(item, account, controllerId, commitId, auditLogId, releasedScripts))
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
                            commitId, account, filteredUnsignedDrafts, updateableAgentNames, keyPair, controllerId, dbLayer.getSession()));
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
                            commitId, account, filteredUnsignedReDeployables, updateableAgentNames, keyPair, controllerId, dbLayer.getSession()));
                }
            }
            signedDeployablesPerController.put(controllerId, new DeployRecord(verifiedDeployables, updateableAgentNames, 
                    updateableAgentNamesFileOrderSources));
        }
        return signedDeployablesPerController;
    }
    
    private Map<String, List<DBItemDeploymentHistory>> prepareItemsToDelete (Set<String> allowedControllerIds, 
            Set<DBItemDeploymentHistory> itemsFromFolderToDelete, DBLayerDeploy dbLayer, String account, String commitIdForDelete, 
            String commitIdForDeleteFileOrderSources, List<Config> foldersToDelete, Long auditLogId) {
        Map<String, List<DBItemDeploymentHistory>> itemsToDeletePerController = new HashMap<String, List<DBItemDeploymentHistory>>();
        Set<DBItemInventoryConfiguration> invConfigurationsToDelete = new HashSet<DBItemInventoryConfiguration>();
        for (String controllerId : allowedControllerIds) {
            List<DBItemDeploymentHistory> filteredDepHistoryItemsToDelete = new ArrayList<DBItemDeploymentHistory>();
            folderPermissions.setSchedulerId(controllerId);
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            // store history entries for delete operation optimistically
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
                    DeleteDeployments.storeNewDepHistoryEntries(dbLayer, allItemsToDelete.get(true), commitIdForDeleteFileOrderSources,
                            account, auditLogId)));
            invConfigurationsToDelete.addAll(DeleteDeployments.getInvConfigurationsForTrash(dbLayer, 
                    DeleteDeployments.storeNewDepHistoryEntries(dbLayer, allItemsToDelete.get(false), commitIdForDelete, account, 
                            auditLogId)));
            itemsToDeletePerController.put(controllerId, filteredDepHistoryItemsToDelete);
        }
        // delete configurations optimistically from inventory
        List<Configuration> folders = null;
        if (foldersToDelete != null) {
            folders = foldersToDelete.stream().map(item -> item.getConfiguration()).collect(Collectors.toList());
        }
        DeleteDeployments.deleteConfigurations(dbLayer, folders, invConfigurationsToDelete, getAccessToken(),  getJocError(), auditLogId,
                false);
        return itemsToDeletePerController;
    }
    
    private Set<DBItemInventoryConfiguration> getInvDbItemsToStore(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        Set<DBItemInventoryConfiguration> items = getInvItemsToStore(deployFilter, dbLayer);
        if(items.isEmpty()) {
            return getInvItemsToStoreFromFolder(deployFilter, dbLayer);
        }
        items.addAll(getInvItemsToStoreFromFolder(deployFilter, dbLayer));
        return items;
    }
    
    private Set<DBItemInventoryConfiguration> getInvItemsToStore(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        List<Configuration> draftConfigsToStore = getDraftConfigurationsToStoreFromFilter(deployFilter);
        if (!draftConfigsToStore.isEmpty()) {
            return dbLayer.getFilteredInventoryConfiguration(draftConfigsToStore, true).stream().collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
    
    private Set<DBItemInventoryConfiguration> getInvItemsToStoreFromFolder(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        List<Configuration> draftFoldersToStore = getDraftConfigurationFoldersToStoreFromFilter(deployFilter);
        if (!draftFoldersToStore.isEmpty()) {
            return PublishUtils.getValidDeployableDraftInventoryConfigurationsfromFolders(draftFoldersToStore, dbLayer);
        }
        return Collections.emptySet();
    }
    
    private List<Configuration> getDraftConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).peek(item -> item.setCommitId(null)).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    private List<Configuration> getDraftConfigurationFoldersToStoreFromFilter(DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDraftConfigurations().stream()
                    .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Set<DBItemDeploymentHistory> getDepDbItemsToStore(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        Set<DBItemDeploymentHistory> items = getDepItemsToStore(deployFilter, dbLayer);
        if(items.isEmpty()) {
            return getDepItemsToStoreFromFolder(deployFilter, dbLayer);
        }
        items.addAll(getDepItemsToStoreFromFolder(deployFilter, dbLayer));
        return items;
    }
    
    private Set<DBItemDeploymentHistory> getDepItemsToStore(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        List<Configuration> deployConfigsToStoreAgain = getDeployConfigurationsToStoreFromFilter(deployFilter);
        if (!deployConfigsToStoreAgain.isEmpty()) {
            return dbLayer.getFilteredDeploymentHistory(deployConfigsToStoreAgain).stream().collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
    
    private Set<DBItemDeploymentHistory> getDepItemsToStoreFromFolder(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        List<Configuration> deployFoldersToStoreAgain = getDeployConfigurationFoldersToStoreFromFilter(deployFilter);
        if (!deployFoldersToStoreAgain.isEmpty()) {
            return PublishUtils.getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(deployFoldersToStoreAgain, dbLayer);
        }
        return Collections.emptySet();
    }
    
    private List<Configuration> getDeployConfigurationsToStoreFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getStore() != null) {
            return deployFilter.getStore().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
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

    private Set<DBItemDeploymentHistory> getDepDbItemsToDelete(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        List<Configuration> deployConfigsToDelete = getDeployConfigurationsToDeleteFromFilter(deployFilter);
        if (!deployConfigsToDelete.isEmpty()) {
            List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
            if (!depHistoryDBItemsToDeployDelete.isEmpty()) {
                Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToDeployDelete.stream()
                        .collect(Collectors.groupingBy(DBItemDeploymentHistory::getPath));
                return grouped.keySet().stream().map(item -> grouped.get(item).get(0)).collect(Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }
    
    private List<Configuration> getDeployConfigurationsToDeleteFromFilter (DeployFilter deployFilter) {
        if (deployFilter.getDelete() != null) {
            return deployFilter.getDelete().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
          return Collections.emptyList();
        }
    }
    
    private List<Config> getFoldersToDelete(DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        if (deployFilter.getDelete() != null) {
            List<Config> foldersToDelete = deployFilter.getDelete().getDeployConfigurations().stream()
                    .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .filter(folder -> "/".equals(folder.getConfiguration().getPath())).collect(Collectors.toList());
            if (!foldersToDelete.isEmpty()) {
                foldersToDelete = PublishUtils.handleFolders(foldersToDelete, dbLayer);
            }
            return foldersToDelete;
        }
        return Collections.emptyList();
    }
    
    private Set<DBItemDeploymentHistory> getDepItemsFromFolders(Set<String> controllerIds, DeployFilter deployFilter, DBLayerDeploy dbLayer) {
        Set<DBItemDeploymentHistory> itemsFromFolderToDelete = new HashSet<DBItemDeploymentHistory>();
        List<Config> folders = getFoldersToDelete(deployFilter, dbLayer);
        if (!folders.isEmpty()) {
            controllerIds.stream().forEach(controllerId -> {
                itemsFromFolderToDelete.addAll(folders.stream().map(Config::getConfiguration).flatMap(item -> dbLayer
                        .getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId, item.getRecursive())).collect(Collectors.toSet()));
            });
        }
        itemsFromFolderToDelete.addAll(getDepDbItemsToDelete(deployFilter, dbLayer));
        return itemsFromFolderToDelete;
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

    private static CompletableFuture<ControllerCommandResponse> getCancelOrderFuture(JOCResourceImpl impl, DailyPlanOrderFilterDef orderFilter,
            Function<ControllerCommandResponse, ControllerCommandResponse> apply) {
        String controllerId = orderFilter.getControllerIds().get(0);
        if ((orderFilter.getWorkflowPaths() == null || orderFilter.getWorkflowPaths().isEmpty()) && 
                (orderFilter.getSchedulePaths() == null || orderFilter.getSchedulePaths().isEmpty())) {
            return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId));
        }
        try {
            DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
            cancelOrderImpl.setCurrentAccount(impl);
            DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
            deleteOrdersImpl.setCurrentAccount(impl);
            Map<String, List<DBItemDailyPlanOrder>> ordersPerController = cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter);
            Map<String, CompletableFuture<ControllerCommandResponse>> cancelOrderResponsePerController = cancelOrderImpl.cancelOrders(
                    ordersPerController);
            if(!cancelOrderResponsePerController.isEmpty() && cancelOrderResponsePerController.get(controllerId) != null) {
                CompletableFuture<ControllerCommandResponse> f = cancelOrderResponsePerController.get(controllerId).thenApply(ccr -> {
                    if (ccr.getException().isEmpty()) {
                        DailyPlanOrderFilterDef localOrderScheduleFilter = new DailyPlanOrderFilterDef();
                        localOrderScheduleFilter.setControllerIds(Collections.singletonList(controllerId));
                        localOrderScheduleFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                        localOrderScheduleFilter.setSchedulePaths(orderFilter.getSchedulePaths());
                        
                        DailyPlanOrderFilterDef localOrderWorkflowFilter = new DailyPlanOrderFilterDef();
                        localOrderWorkflowFilter.setControllerIds(Collections.singletonList(controllerId));
                        localOrderWorkflowFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                        localOrderWorkflowFilter.setWorkflowPaths(orderFilter.getWorkflowPaths());
                        
                        boolean successful1 = true;
                        boolean successful2 = true;
                        try {
                            // TODO create Method to transfer a set of order objects to delete instead of a filter
                            if (orderFilter.getSchedulePaths() != null && !orderFilter.getSchedulePaths().isEmpty()) {
                                successful1 = deleteOrdersImpl.deleteOrders(localOrderScheduleFilter, false, false, false); 
                            }
                            if (orderFilter.getWorkflowPaths() != null && !orderFilter.getWorkflowPaths().isEmpty()) {
                                successful2 = deleteOrdersImpl.deleteOrders(localOrderWorkflowFilter, false, false, false);
                            }
                            if (!successful1 || !successful2) {
                                return new ControllerCommandResponse(controllerId, Optional.of(new JocReleaseException(
                                        "Order delete failed due to missing permission.")));
                            }
                        } catch (Exception e) {
                            return new ControllerCommandResponse(controllerId, Optional.of(e));
                        }
                    }
                    return ccr;
                });
                if (apply != null) {
                    f.thenApply(apply);
                }
                return f;
            } else {
                return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.empty()));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new ControllerCommandResponse(controllerId, Optional.of(e)));
        }
    }

}