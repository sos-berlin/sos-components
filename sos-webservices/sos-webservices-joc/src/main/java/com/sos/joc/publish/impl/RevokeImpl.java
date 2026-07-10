package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.inventory.RecallRevokeSemaphore;
import com.sos.joc.classes.inventory.RemoveSemaphore;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.ProxyNotCoupledException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RevokeFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IRevoke;
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.joc.publish.util.UpdateItemUtils;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.proxy.javaapi.JControllerProxy;

@Path("inventory/deployment")
public class RevokeImpl extends JOCResourceImpl implements IRevoke {

    private static final String API_CALL = "./inventory/deployment/revoke";
//    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeImpl.class);
    private DBLayerDeploy dbLayer = null;
    private static final String SEMAPHORE_ID = "REVOKE";

    @Override
    public JOCDefaultResponse postRevoke(String xAccessToken, byte[] filter) throws Exception {

        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.DEPLOYMENT);
            JsonValidator.validate(filter, RevokeFilter.class);
            RevokeFilter revokeFilter = Globals.objectMapper.readValue(filter, RevokeFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Thread deployThread = new Thread(() -> {
                Logger logger = LoggerFactory.getLogger("revokeThread");
                SOSHibernateSession hibernateSession = null;
                String transactionId = revokeFilter.getTransactionId();
                if (revokeFilter.getTransactionId() == null || revokeFilter.getTransactionId().isEmpty()) {
                    transactionId = UUID.randomUUID().toString();
                    revokeFilter.setTransactionId(transactionId);
                }
                try {
                    logger.debug("acquire semaphore from deploy with transactionId " + transactionId);
                    if (RemoveSemaphore.availablePermits(transactionId) == 1) {
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                    RemoveSemaphore.tryAcquire(transactionId, SEMAPHORE_ID);
                    logger.debug("acquire semaphore from revoke with transactionId " + transactionId);
                    // if semaphore already contains workflownames from potential recall operation, remove those workflow from cancel order call
                    Set<String> workflowsWithAlreadyCanceledOrders = RemoveSemaphore.getInstance().getSemaphore(transactionId)
                            .map(RecallRevokeSemaphore::getWorkflowNames).orElse(Collections.emptySet());
                    DBItemJocAuditLog dbAuditlog = storeAuditLog(revokeFilter.getAuditLog());
                    Set<String> allowedControllerIds = Collections.emptySet();
                    allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> 
                            getBasicControllerPermissions(availableController, xAccessToken).getDeployments().getDeploy()).collect(Collectors.toSet());
                    String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
                    hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                    dbLayer = new DBLayerDeploy(hibernateSession);
                    // process filter
                    Set<String> controllerIds = new HashSet<String>(revokeFilter.getControllerIds());
                    List<Configuration> deployConfigsToRevoke = getDeployConfigurationsToDeleteFromFilter(revokeFilter);
                    List<Configuration> foldersToRevoke = getFoldersToDeleteFromFilter(revokeFilter);
                    // collect items for set of single items
                    List<DBItemDeploymentHistory> depHistoryDBItemsToRevoke = null;
                    if (deployConfigsToRevoke != null && !deployConfigsToRevoke.isEmpty()) {
                        depHistoryDBItemsToRevoke = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToRevoke);
                        if (depHistoryDBItemsToRevoke != null && !depHistoryDBItemsToRevoke.isEmpty()) {
                            Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToRevoke.stream().collect(Collectors.groupingBy(
                                    DBItemDeploymentHistory::getPath));
                            depHistoryDBItemsToRevoke = grouped.keySet().stream().map(item -> grouped.get(item)).flatMap(List::stream).collect(Collectors
                                    .toList());
                        }
                    }
                    // collect Items for set of folders
                    Stream<DBItemDeploymentHistory> itemsFromFolderToRevoke = Stream.empty();
                    for (String controllerId : controllerIds) {
                        if (!foldersToRevoke.isEmpty()) {
                            itemsFromFolderToRevoke = foldersToRevoke.stream().flatMap(folder -> dbLayer.getLatestDepHistoryItemsFromFolder(folder.getPath(),
                                    controllerId, folder.getRecursive()));
                        }
                    }
                    Map<String, List<DBItemDeploymentHistory>> itemsPerControllerToRevokeFromFolder = 
                            itemsFromFolderToRevoke.collect(Collectors.groupingBy(DBItemDeploymentHistory::getControllerId));
                    Date collectingItemsFinished = Date.from(Instant.now());
                    logger.trace("*** collecting items finished ***" + collectingItemsFinished);
                    // Delete from all allowed controllers from filter
                    final String commitIdForRevoke = UUID.randomUUID().toString();
                    final String commitIdForRevokeFileOrderSources = UUID.randomUUID().toString();
                    // loop 1: store db entries optimistically
                    for (String controllerId : controllerIds) {
                        if (!allowedControllerIds.contains(controllerId)) {
                            continue;
                        }
                        List<DBItemDeploymentHistory> filteredDepHistoryItemsToRevoke = new ArrayList<DBItemDeploymentHistory>();
                        folderPermissions.setSchedulerId(controllerId);
                        Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        // store history entries for delete operation optimistically
                        if (depHistoryDBItemsToRevoke != null && !depHistoryDBItemsToRevoke.isEmpty()) {
                            filteredDepHistoryItemsToRevoke.addAll(depHistoryDBItemsToRevoke.stream()
                                    .filter(history -> canAdd(history.getPath(), permittedFolders)).collect(Collectors.toList()));
                        }
                        if (itemsPerControllerToRevokeFromFolder != null && !itemsPerControllerToRevokeFromFolder.isEmpty()) {
                            if(itemsPerControllerToRevokeFromFolder.containsKey(controllerId)) {
                                filteredDepHistoryItemsToRevoke.addAll(itemsPerControllerToRevokeFromFolder.get(controllerId).stream()
                                        .filter(fromFolder -> canAdd(fromFolder.getPath(), permittedFolders))
                                        .filter(item -> !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation())))
                                        .collect(Collectors.toList()));
                            }
                        }
                        
                        Set<DBItemDeploymentHistory> filteredDepHistoryItemsforCancelOrders = filteredDepHistoryItemsToRevoke.stream()
                                .filter(item -> !workflowsWithAlreadyCanceledOrders.contains(item.getName())).collect(Collectors.toSet());
                                
                        DailyPlanOrderFilterDef orderFilter = CancelOrdersPublishHelper.getDailyPlanOrderFilter(filteredDepHistoryItemsforCancelOrders,
                                Optional.ofNullable(null), "now", controllerId);
                        Map<String, Set<String>> workflowsPerController = filteredDepHistoryItemsforCancelOrders.stream().filter(item -> DeployType.WORKFLOW.equals(item.getTypeAsEnum()))
                            .collect(Collectors.groupingBy(DBItemDeploymentHistory::getControllerId, Collectors.mapping(DBItemDeploymentHistory::getName, Collectors.toSet())));
                        
                        List<CompletableFuture<ControllerCommandResponse>> cancelOrderResponse = 
                                CancelOrdersPublishHelper.getCancelOrderFutures(xAccessToken, orderFilter, getApplyFunction(workflowsPerController.get(controllerId)));
                        
                        CompletableFuture.allOf(cancelOrderResponse.toArray(CompletableFuture[]::new)).thenRun(() -> {
                            Map<Boolean, List<ControllerCommandResponse>> mappedFutures = cancelOrderResponse.stream().map(CompletableFuture::join)
                                    .collect(Collectors.groupingBy(ControllerCommandResponse::hasException));
                            mappedFutures.putIfAbsent(true, Collections.emptyList());
                            mappedFutures.putIfAbsent(false, Collections.emptyList());
                            
                            if(!mappedFutures.get(true).isEmpty()) {
                                // post exception if futures with errors exist
                                ProblemHelper.postExceptionsIfExist(mappedFutures.get(true), xAccessToken, getJocError());
                            } 
                            if(!mappedFutures.get(false).isEmpty()) {
                                //      STORE optimistically and send commands to controllers where no errors occurred, if no orders are still attached
                                JControllerProxy proxy;
                                try {
                                    proxy = Proxy.of(controllerId);
                                } catch (ExecutionException e) {
                                    throw new ProxyNotCoupledException(e);
                                }
                                Map<Boolean, List<DBItemDeploymentHistory>> allItemsToDelete = filteredDepHistoryItemsToRevoke.stream().collect(Collectors.groupingBy(
                                        fos -> DeployType.FILEORDERSOURCE.equals(fos.getTypeAsEnum())));
                                DeleteDeployments.storeNewDepHistoryEntriesForRevoke(dbLayer, allItemsToDelete.get(true), commitIdForRevokeFileOrderSources,
                                        controllerId, dbAuditlog.getId(), account);
                                DeleteDeployments.storeNewDepHistoryEntriesForRevoke(dbLayer, allItemsToDelete.get(false), commitIdForRevoke, controllerId, dbAuditlog
                                        .getId(), account);
                                if(allItemsToDelete.get(true) != null && !allItemsToDelete.get(true).isEmpty()) {
                                    UpdateItemUtils.updateItemsDelete(commitIdForRevokeFileOrderSources, allItemsToDelete.get(true), proxy)
                                    .thenAccept(either -> {
                                        DeleteDeployments.processAfterRevoke(either, controllerId, account, commitIdForRevokeFileOrderSources, xAccessToken, 
                                                getJocError(), allItemsToDelete.get(false), commitIdForRevoke, 
                                                allItemsToDelete.get(true).stream().map(DBItemDeploymentHistory::getName).collect(Collectors.toSet()));
                                    });
                                    
                                } else if(allItemsToDelete.get(false) != null && !allItemsToDelete.get(false).isEmpty()) {
                                    UpdateItemUtils.updateItemsDelete(commitIdForRevoke, allItemsToDelete.get(false), proxy)
                                    .thenAccept(either -> {
                                        DeleteDeployments.processAfterRevoke(either, controllerId, account, commitIdForRevoke, getAccessToken(), getJocError());
                                    });
                                    
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                } finally {
                    removeSemapohoreFinally(transactionId, logger);
                    Globals.disconnect(hibernateSession);
                }
            }, "revoke");
            deployThread.start();
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    private Function<ControllerCommandResponse, ControllerCommandResponse> getApplyFunction (Set<String> workflowNames) {
        return ccr -> {
            if (ccr.hasException()) {
                return ccr;
            }
            Set<String> outerWorkflowNames = workflowNames;
            String cId = ccr.getControllerId();
            try {
                JControllerProxy proxy = Proxy.of(cId);
                Stream<String> workflows = UpdateItemUtils.getWorkflowNamesWithOrders(proxy.currentState());
                if(workflows.anyMatch(outerWorkflowNames::contains)) {
                    throw new JocDeployException("Orders still attached to workflows. Revoke operation canceled.");
                }
            } catch (Exception e) {
                return new ControllerCommandResponse(cId, Optional.of(e));
            }
            return ccr;
        };
    }

    private List<Configuration> getDeployConfigurationsToDeleteFromFilter (RevokeFilter revokeFilter) {
        if (revokeFilter.getDeployConfigurations() != null && !revokeFilter.getDeployConfigurations().isEmpty()) {
            return revokeFilter.getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }
    
    private List<Configuration> getFoldersToDeleteFromFilter (RevokeFilter revokeFilter) {
        if (revokeFilter.getDeployConfigurations() != null && !revokeFilter.getDeployConfigurations().isEmpty()) {
            return revokeFilter.getDeployConfigurations().stream()
                    .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(Config::getConfiguration).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<Configuration>();
        }
    }
    
    private static void removeSemapohoreFinally(String transactionId, Logger logger) {
        RemoveSemaphore.release(transactionId);
        logger.debug("release semaphore from revoke with transactionId " + transactionId);
        if (RemoveSemaphore.getInstance().getSemaphore(transactionId).map(RecallRevokeSemaphore::getInitialCaller).filter(SEMAPHORE_ID::equals)
                .isPresent()) {
            RemoveSemaphore.remove(transactionId);
            logger.debug("Semaphore from " + SEMAPHORE_ID + " finally removed.");
        }
    }

}