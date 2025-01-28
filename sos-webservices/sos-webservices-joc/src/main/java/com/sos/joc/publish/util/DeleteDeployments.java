package com.sos.joc.publish.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.dailyplan.impl.DailyPlanCancelOrderImpl;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.orderwatch.OrderWatchPath;
import js7.proxy.javaapi.JControllerProxy;

public class DeleteDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDeployments.class);
    private static final List<DeployType> DELETE_ORDER = Arrays.asList(
            DeployType.FILEORDERSOURCE, 
            DeployType.WORKFLOW, 
            DeployType.JOBRESOURCE, 
            DeployType.PLANNABLEBOARD, 
            DeployType.NOTICEBOARD, 
            DeployType.LOCK);
    private static final List<ConfigurationType> RESTORE_ORDER = Arrays.asList(
            ConfigurationType.FOLDER, 
            ConfigurationType.REPORT,
            ConfigurationType.LOCK,
            ConfigurationType.INCLUDESCRIPT, 
            ConfigurationType.NOTICEBOARD, 
            ConfigurationType.JOBRESOURCE,
            ConfigurationType.NONWORKINGDAYSCALENDAR, 
            ConfigurationType.WORKINGDAYSCALENDAR, 
            ConfigurationType.JOBTEMPLATE,
            ConfigurationType.WORKFLOW, 
            ConfigurationType.FILEORDERSOURCE, 
            ConfigurationType.SCHEDULE);

     public static Set<DBItemInventoryConfiguration> delete(Collection<DBItemDeploymentHistory> dbItems, DBLayerDeploy dbLayer, String account, String accessToken,
            JocError jocError, Long auditlogId, boolean withoutFolderDeletion, boolean withEvents, String cancelOrderDate) throws SOSHibernateException {
        if (dbItems == null || dbItems.isEmpty()) {
            return Collections.emptySet();
        }
        final String commitId = UUID.randomUUID().toString();
        final String commitIdforFileOrderSource = UUID.randomUUID().toString();
        
        // delete configurations optimistically
        Set<DBItemInventoryConfiguration> invConfsToTrash = getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, dbItems, commitId,
                commitIdforFileOrderSource, account, auditlogId));
        deleteConfigurations(dbLayer, null, invConfsToTrash, accessToken, jocError, auditlogId, withoutFolderDeletion, withEvents);
        
        Map<String, Map<DeployType, List<DBItemDeploymentHistory>>> dbItemsPerController = dbItems.stream().filter(Objects::nonNull).filter(
                item -> OperationType.UPDATE.value() == item.getOperation())
                    .collect(Collectors.groupingBy(DBItemDeploymentHistory::getControllerId, Collectors.groupingBy(DBItemDeploymentHistory::getTypeAsEnum)));
        
        // optimistic DB operations
        for (Map.Entry<String, Map<DeployType, List<DBItemDeploymentHistory>>> entry : dbItemsPerController.entrySet()) {
            List<DBItemDeploymentHistory> fileOrderSourceItems = entry.getValue().getOrDefault(
                    DeployType.FILEORDERSOURCE, Collections.emptyList());
            if (fileOrderSourceItems.isEmpty() || entry.getValue().keySet().size() == 1) {
                List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                for (DeployType type : DELETE_ORDER) {
                    sortedItems.addAll(entry.getValue().getOrDefault(type, Collections.emptyList()));
                }

                // send commands to controllers
                UpdateItemUtils.updateItemsDelete(commitId, sortedItems, entry.getKey())
                    .thenAccept(either -> processAfterDelete(either, entry.getKey(), account, commitId, accessToken, jocError, cancelOrderDate));
            } else {
                List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                for (DeployType type : DELETE_ORDER) {
                    if (!type.equals(DeployType.FILEORDERSOURCE)) {
                        sortedItems.addAll(entry.getValue().getOrDefault(type, Collections.emptyList()));
                    }
                }
                
                // send commands to controllers
                Set<String> fileOrderSourceNames = fileOrderSourceItems.stream().map(item -> item.getName()).collect(Collectors.toSet());
                UpdateItemUtils.updateItemsDelete(commitIdforFileOrderSource, fileOrderSourceItems, entry.getKey())
                    .thenAccept(either -> {
                        processAfterDelete(either, entry.getKey(), account, commitIdforFileOrderSource, accessToken, jocError, cancelOrderDate,
                                sortedItems, commitId, fileOrderSourceNames);
                    });
            }
        }
        return invConfsToTrash;
    }
    
    public static Set<DBItemInventoryConfiguration> deleteFolder(String apiCall, String folder, boolean recursive, Collection<String> controllerIds, DBLayerDeploy dbLayer,
            String account, String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion, boolean withEvents, String cancelOrderDate)
            throws SOSHibernateException {
        Configuration conf = new Configuration();
        conf.setObjectType(ConfigurationType.FOLDER);
        conf.setPath(folder);
        conf.setRecursive(recursive);
        return deleteFolder(apiCall, conf, controllerIds, dbLayer, account, accessToken, jocError, auditlogId, withoutFolderDeletion,
                withEvents, cancelOrderDate);
    }
    
    public static Set<DBItemInventoryConfiguration> deleteFolder(String apiCall, Configuration conf, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion,
            boolean withEvents, String cancelOrderDate) throws SOSHibernateException {
        if (conf == null || conf.getPath() == null || conf.getPath().isEmpty()) {
            return Collections.emptySet();
        }
   
        final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
        final String commitIdForDeleteFileOrderSource = UUID.randomUUID().toString();
        
        List<DBItemDeploymentHistory> dbItems = controllerIds.stream().map(controllerId -> dbLayer.getLatestDepHistoryItemsFromFolder(conf.getPath(),
                controllerId, conf.getRecursive())).flatMap(List::stream).filter(item -> OperationType.DELETE.value() != item.getOperation()).collect(
                        Collectors.toList());

        // delete configurations optimistically
        Set<DBItemInventoryConfiguration> invItemsforTrash = getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, dbItems,
                commitIdForDeleteFromFolder, commitIdForDeleteFileOrderSource, account, auditlogId));
        deleteConfigurations(dbLayer, null, invItemsforTrash, accessToken, jocError, auditlogId, withoutFolderDeletion, withEvents);
        
        Map<String, Map<DeployType, List<DBItemDeploymentHistory>>> itemsToDeletePerController = dbItems.stream().collect(Collectors.groupingBy(
                DBItemDeploymentHistory::getControllerId, Collectors.groupingBy(DBItemDeploymentHistory::getTypeAsEnum)));
        
        
        // optimistic DB operations
        for (String controllerId : itemsToDeletePerController.keySet()) {
            if (!itemsToDeletePerController.get(controllerId).isEmpty()) {
                List<DBItemDeploymentHistory> fileOrderSourceItems = itemsToDeletePerController.get(controllerId).getOrDefault(
                        DeployType.FILEORDERSOURCE, Collections.emptyList());
                if (fileOrderSourceItems.isEmpty() /*|| itemsToDeletePerController.keySet().size() == 1*/) {
                    List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                    for (DeployType type : DELETE_ORDER) {
                        sortedItems.addAll(itemsToDeletePerController.get(controllerId).getOrDefault(type, Collections.emptyList()));
                    }
                    // send commands to controllers
                    UpdateItemUtils.updateItemsDelete(commitIdForDeleteFromFolder, sortedItems, controllerId).thenAccept(
                            either -> processAfterDelete(either, controllerId, account, commitIdForDeleteFromFolder, accessToken, jocError, cancelOrderDate));
                } else {
                    List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                    for (DeployType type : DELETE_ORDER) {
                        if (!type.equals(DeployType.FILEORDERSOURCE)) {
                            sortedItems.addAll(itemsToDeletePerController.get(controllerId).getOrDefault(type, Collections.emptyList()));
                        }
                    }
                    // send commands to controllers
                    Set<String> fileOrderSourceNames = fileOrderSourceItems.stream().map(item -> item.getName()).collect(Collectors.toSet());
                    UpdateItemUtils.updateItemsDelete(commitIdForDeleteFileOrderSource, fileOrderSourceItems, controllerId).thenAccept(
                            either -> {
                                processAfterDelete(either, controllerId, account, commitIdForDeleteFileOrderSource, accessToken, jocError, cancelOrderDate,
                                        sortedItems, commitIdForDeleteFromFolder, fileOrderSourceNames);
                            });
                }
            }
        }
        return invItemsforTrash;
    }
    
    public static void processAfterDelete(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError, String cancelOrderDate) {
        processAfterDelete(either, controllerId, account, commitId, accessToken, jocError, cancelOrderDate, null, null, null);
    }
    
    public static void processAfterDelete(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError, String cancelOrderDate, List<DBItemDeploymentHistory> toDelete, String commitId2, Set<String> fileOrderSourceNames) {
        SOSHibernateSession newHibernateSession = null;
        try {
            if (either.isLeft()) {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
                newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
                final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected

                // get all already optimistically stored entries for the commit
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
               
                // update all previously optimistically stored entries with the error message and change the state
                Map<Integer, Set<DBItemInventoryConfigurationTrash>> itemsFromTrashByType = 
                        new HashMap<Integer, Set<DBItemInventoryConfigurationTrash>>();
                InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    optimistic.setErrorMessage(either.getLeft().message());
                    optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                    optimistic.setDeleteDate(null);
                    dbLayer.getSession().update(optimistic);
                    // TODO: restore related inventory configuration - Recover and remove from trash
                    if(itemsFromTrashByType.containsKey(optimistic.getType())) {
                        itemsFromTrashByType.get(optimistic.getType())
                            .add(invDbLayer.getTrashConfiguration(optimistic.getPath(), optimistic.getType()));
                    } else {
                        itemsFromTrashByType.put(optimistic.getType(), new HashSet<>());
                        itemsFromTrashByType.get(optimistic.getType())
                            .add(invDbLayer.getTrashConfiguration(optimistic.getPath(), optimistic.getType()));
                    }
                }
                if(!itemsFromTrashByType.isEmpty()) {
                    Set<String> parentFolders = new HashSet<String>();
                    for (ConfigurationType objType : RESTORE_ORDER) {
                        Set<DBItemInventoryConfigurationTrash> itemsFromTrash = itemsFromTrashByType.get(objType.intValue());
                        if(itemsFromTrash != null) {
                            for (DBItemInventoryConfigurationTrash trashItem : itemsFromTrash) {
                                if (trashItem != null) {
                                    parentFolders.add(trashItem.getFolder());
                                    JocInventory.insertConfiguration(invDbLayer, recreateItem(trashItem, null, invDbLayer));
                                    invDbLayer.getSession().delete(trashItem);
                                }
                            }
                        }
                    }
                    for(String parentFolder : parentFolders) {
                        JocInventory.makeParentDirs(invDbLayer, Paths.get(parentFolder), ConfigurationType.FOLDER);
                        JocInventory.postFolderEvent(parentFolder);
                    }
                }
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
            } else {
                if(toDelete != null && commitId2 != null && !toDelete.isEmpty() && fileOrderSourceNames != null && !fileOrderSourceNames.isEmpty() ) {
                    JControllerProxy proxy = Proxy.of(controllerId);
                    Set<OrderWatchPath> fosPaths = fileOrderSourceNames.stream().map(fos -> OrderWatchPath.of(fos)).collect(Collectors.toSet());
                    for (int second = 0; second < 10; second++) {
                        try {
                            if (second < 9 && !proxy.currentState().pathToFileWatch().keySet().stream().anyMatch(fos -> fosPaths.contains(fos))) {
                                // file order source is deleted
                                break;
                            }
                            TimeUnit.SECONDS.sleep(1L);
                        } catch (Exception e) {}
                    }
                    
                    UpdateItemUtils.updateItemsDelete(commitId2, toDelete, controllerId)
                    .thenAccept(either2 -> {
                        processAfterDelete(either2, controllerId, account, commitId2, accessToken, jocError, cancelOrderDate);
                    });
                }
                if(cancelOrderDate != null) {
                    newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
                    final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
                    DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
                    DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
                    DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
                    orderFilter.setControllerIds(Collections.singletonList(controllerId));
                    if("now".equals(cancelOrderDate)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
                        orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
                    } else {
                        orderFilter.setDailyPlanDateFrom(cancelOrderDate);
                    }
                    List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                    orderFilter.setWorkflowPaths(optimisticEntries.stream().filter(item -> item.getTypeAsEnum().equals(DeployType.WORKFLOW))
                                .map(workflow -> workflow.getName()).collect(Collectors.toList()));
                    try {
                        CompletableFuture<Either<Problem, Void>> cancelOrderResponse =
                                cancelOrderImpl.cancelOrders(orderFilter, accessToken, false, false)
                                .getOrDefault(controllerId, CompletableFuture.supplyAsync(() -> Either.right(null)));
                            cancelOrderResponse.thenAccept(either2 -> {
                                if(either2.isRight()) {
                                    try {
                                        boolean successful = deleteOrdersImpl.deleteOrders(orderFilter, accessToken, false, false, false);
                                        if (!successful) {
                                            LOGGER.warn("Order delete failed due to missing permission.");
                                        }
                                    } catch (SOSHibernateException e) {
                                        LOGGER.warn("delete of planned orders in db failed.", e.getMessage());
                                    }
                                } else {
                                    LOGGER.warn("Order cancel failed due to missing permission.");
                                }
                            });
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage());
                    }

                }
            }
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

    public static Set<DBItemDeploymentHistory> storeNewDepHistoryEntries(DBLayerDeploy dbLayer, Collection<DBItemDeploymentHistory> itemsToDelete,
            String commitId, String account, Long auditLogId) {
        return PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer, commitId, null, false, account, auditLogId);
    }

    public static Set<DBItemDeploymentHistory> storeNewDepHistoryEntries(DBLayerDeploy dbLayer, Collection<DBItemDeploymentHistory> itemsToDelete,
            String commitId, String commitIdforFileOrderSource, String account, Long auditLogId) {
        return PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer, commitId, commitIdforFileOrderSource, false, account, auditLogId);
    }

    public static Set<DBItemDeploymentHistory> storeNewDepHistoryEntriesForRevoke(DBLayerDeploy dbLayer, List<DBItemDeploymentHistory> deletedItems,
            String commitId, Long auditLogId, String account) {
        Set<DBItemDeploymentHistory> deletedObjects = Collections.emptySet();
        Set<String> folders = new HashSet<>();
        List<Long> workflowInvIds = new ArrayList<>();
        try {
            if (deletedItems != null && !deletedItems.isEmpty()) {
                deletedObjects = new HashSet<DBItemDeploymentHistory>();
                for (DBItemDeploymentHistory item : deletedItems) {
                    folders.add(item.getFolder());
                    if (JocInventory.isWorkflow(item.getType())) {
                        workflowInvIds.add(item.getInventoryConfigurationId()); 
                    }
                    DBItemDeploymentHistory newEntry = new DBItemDeploymentHistory();
                    newEntry.setOperation(OperationType.DELETE.value());
                    newEntry.setState(DeploymentState.DEPLOYED.value());
                    newEntry.setDeleteDate(Date.from(Instant.now()));
                    newEntry.setDeploymentDate(Date.from(Instant.now()));
                    
                    newEntry.setAccount(account);
                    newEntry.setAuditlogId(auditLogId);
                    newEntry.setContent(item.getContent());
                    newEntry.setControllerId(item.getControllerId());
                    newEntry.setControllerInstanceId(item.getControllerInstanceId());
                    newEntry.setFolder(item.getFolder());
                    newEntry.setInvContent(item.getInvContent());
                    newEntry.setInventoryConfigurationId(item.getInventoryConfigurationId());
                    newEntry.setName(item.getName());
                    newEntry.setPath(item.getPath());
                    newEntry.setSignedContent(item.getSignedContent());
                    newEntry.setTitle(item.getTitle());
                    newEntry.setType(item.getType());
                    newEntry.setVersion(item.getVersion());
                    newEntry.setCommitId(commitId);
                    if (item.getSignedContent() == null || item.getSignedContent().isEmpty()) {
                        newEntry.setSignedContent(".");
                    }
                    dbLayer.getSession().save(newEntry);
                    deletedObjects.add(newEntry);
                    DBItemInventoryConfiguration orig = dbLayer.getInventoryConfigurationByNameAndType(item.getName(), item.getType());
                    if (orig != null) {
                        orig.setDeployed(false);
                        dbLayer.getSession().update(orig);
                    }
                }
                folders.forEach(JocInventory::postEvent);
                JocInventory.postDeployHistoryEventWhenDeleted(deletedObjects);
                // InventoryTaggingUpdated
                if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                    InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(dbLayer.getSession());
                    dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
                }
            }

        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deletedObjects;
    }

//    public static List<DBItemInventoryConfiguration> getInvConfigurationsForTrash (DBLayerDeploy dbLayer, Set<DBItemDeploymentHistory> deletedDeployItems ) {
//        return dbLayer.getInventoryConfigurationsByIds(
//                deletedDeployItems.stream().map(item -> item.getInventoryConfigurationId()).distinct().collect(Collectors.toList()));
//    }
    
    public static Set<DBItemInventoryConfiguration> getInvConfigurationsForTrash (DBLayerDeploy dbLayer, Set<DBItemDeploymentHistory> deletedDeployItems ) {
        //List<DBItemInventoryConfiguration> invConfigurations = new ArrayList<DBItemInventoryConfiguration>();
        //TODO dbLayer.getConfigurationByName(item.getName(), item.getType())) can be null
        return deletedDeployItems.stream().map(item -> dbLayer.getConfigurationByName(item.getName(), item.getType())).filter(Objects::nonNull).collect(Collectors.toSet());
//        deletedDeployItems.stream().forEach(item -> invConfigurations.add(dbLayer.getConfigurationByName(item.getName(), item.getType())));
//        return invConfigurations;
    }
    
    public static void deleteConfigurations(DBLayerDeploy dbLayer, List<Configuration> folders, Set<DBItemInventoryConfiguration> itemsToDelete, 
            String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion) {
        deleteConfigurations(dbLayer, folders, itemsToDelete, accessToken, jocError, auditlogId, withoutFolderDeletion, true);
    }
    
    public static void deleteConfigurations(DBLayerDeploy dbLayer, List<Configuration> folders,
            Set<DBItemInventoryConfiguration> itemsToDelete, String accessToken, JocError jocError, Long auditlogId,
            boolean withoutFolderDeletion, boolean withEvents) {
        // add all elements from the folder(s)
        Set<String> foldersForEvent = new HashSet<>();
        List<Long> workflowInvIds = new ArrayList<>();
        if (folders != null) {
            for (Configuration folder : folders) {
                itemsToDelete.addAll(dbLayer.getInventoryConfigurationsByFolder(folder.getPath(), folder.getRecursive()));
                if (withEvents) {
                    if ("/".equals(folder.getPath())) {
                        foldersForEvent.add("/");
                    } else {
                        foldersForEvent.add(Paths.get(folder.getPath()).getParent().toString().replace('\\', '/'));
                    }
                }
            }
        }
        // delete and put to trash
        InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
        for (DBItemInventoryConfiguration invConfiguration : itemsToDelete) {
            if (invConfiguration != null) {
                invConfiguration.setAuditLogId(auditlogId);
                JocInventory.deleteInventoryConfigurationAndPutToTrash(invConfiguration, invDbLayer, ConfigurationType.FOLDER);
                if (withEvents) {
                    foldersForEvent.add(invConfiguration.getFolder());
                    if (JocInventory.isWorkflow(invConfiguration.getType())) {
                        workflowInvIds.add(invConfiguration.getId());
                    }
                }
            }
        }
        if (!withoutFolderDeletion) {
            // delete folders
            if (folders != null && !folders.isEmpty()) {
                for (Configuration folder : folders) {
                    try {
                        JocInventory.deleteEmptyFolders(invDbLayer, folder.getPath());
                    } catch (SOSHibernateException e) {
                        ProblemHelper.postProblemEventIfExist(Either.left(Problem.fromThrowable(e)), accessToken, jocError, null);
                    }
                }
            }
        }
        // post events
        for (String folder: foldersForEvent) {
            JocInventory.postEvent(folder);
            JocInventory.postTrashEvent(folder);
        }
        // post event: InventoryTaggingUpdated
        if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
            InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(dbLayer.getSession());
            dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
        }
    }
    
    private static DBItemInventoryConfiguration recreateItem(DBItemInventoryConfigurationTrash oldItem, Long auditLogId, InventoryDBLayer dbLayer) {
        DBItemInventoryConfiguration item = new DBItemInventoryConfiguration();
        item.setId(null);
        item.setPath(oldItem.getPath());
        item.setFolder(oldItem.getFolder());
        item.setName(oldItem.getName());
        item.setDeployed(true);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        item.setCreated(item.getModified());
        item.setDeleted(false);
        item.setAuditLogId(auditLogId);
        item.setTitle(oldItem.getTitle());
        item.setType(oldItem.getType());
        item.setContent(oldItem.getContent());
        try {
            Validator.validate(item.getTypeAsEnum(), item.getContent().getBytes(StandardCharsets.UTF_8), dbLayer, null);
            item.setValid(true);
        } catch (Throwable e) {
            item.setValid(false);
        }
        return item;
    }

    public static void processAfterRevoke(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError, String cancelOrderDate) {
        SOSHibernateSession newHibernateSession = null;
        try {
            if (either.isLeft()) {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
                newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
                final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected

                // get all already optimistically stored entries for the commit
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                // update all previously optimistically stored entries with the error message and change the state
                Map<Integer, Set<DBItemInventoryConfigurationTrash>> itemsFromTrashByType = 
                        new HashMap<Integer, Set<DBItemInventoryConfigurationTrash>>();
                InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    optimistic.setErrorMessage(either.getLeft().message());
                    optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                    optimistic.setDeleteDate(null);
                    dbLayer.getSession().update(optimistic);
                    DBItemInventoryConfiguration orig = dbLayer.getInventoryConfigurationByNameAndType(optimistic.getName(), optimistic.getType());
                    if (orig != null) {
                        orig.setDeployed(true);
                        dbLayer.getSession().update(orig);
                    }
}
                
                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
            }
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

}
