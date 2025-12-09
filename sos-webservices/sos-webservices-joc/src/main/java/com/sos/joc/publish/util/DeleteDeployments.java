package com.sos.joc.publish.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.orderwatch.OrderWatchPath;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflowId;
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

    public static Set<DBItemInventoryConfiguration> delete(Collection<DBItemDeploymentHistory> dbItems, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion, boolean withEvents, String cancelOrderDate)
            throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        
        if (dbItems == null || dbItems.isEmpty()) {
            return Collections.emptySet();
        }
        
        
        Map<String, Map<DeployType, List<DBItemDeploymentHistory>>> dbItemsPerController = dbItems.stream().filter(Objects::nonNull).filter(
                item -> OperationType.UPDATE.value() == item.getOperation()).collect(Collectors.groupingBy(DBItemDeploymentHistory::getControllerId,
                        Collectors.groupingBy(DBItemDeploymentHistory::getTypeAsEnum)));

        // check older workflow versions
        for (Map.Entry<String, Map<DeployType, List<DBItemDeploymentHistory>>> entry : dbItemsPerController.entrySet()) {
            checkIfWorkflowsHaveOrders(entry.getKey(), entry.getValue().getOrDefault(DeployType.WORKFLOW, Collections.emptyList()).stream().map(
                    DBItemDeploymentHistory::getName).collect(Collectors.toSet()));
        }

        final String commitId = UUID.randomUUID().toString();
        final String commitIdforFileOrderSource = UUID.randomUUID().toString();
        
        // delete configurations optimistically
        Set<DBItemInventoryConfiguration> invConfsToTrash = getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, dbItems, commitId,
                commitIdforFileOrderSource, account, auditlogId));
        deleteConfigurations(dbLayer, null, invConfsToTrash, accessToken, jocError, auditlogId, withoutFolderDeletion, withEvents);
        
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
                UpdateItemUtils.updateItemsDelete(commitIdforFileOrderSource, fileOrderSourceItems, entry.getKey()).thenAccept(
                        either -> processAfterDelete(either, entry.getKey(), account, commitIdforFileOrderSource, accessToken, jocError,
                                cancelOrderDate, sortedItems, commitId, fileOrderSourceNames));
            }
        }
        return invConfsToTrash;
    }
    
    public static Set<DBItemInventoryConfiguration> deleteFolder(String apiCall, String folder, boolean recursive, Collection<String> controllerIds,
            DBLayerDeploy dbLayer, String account, String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion,
            boolean withEvents, String cancelOrderDate) throws SOSHibernateException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {

        Configuration conf = new Configuration();
        conf.setObjectType(ConfigurationType.FOLDER);
        conf.setPath(folder);
        conf.setRecursive(recursive);
        return deleteFolder(apiCall, conf, controllerIds, dbLayer, account, accessToken, jocError, auditlogId, withoutFolderDeletion,
                withEvents, cancelOrderDate);
    }
    
    public static Set<DBItemInventoryConfiguration> deleteFolder(String apiCall, Configuration conf, Collection<String> controllerIds,
            DBLayerDeploy dbLayer, String account, String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion,
            boolean withEvents, String cancelOrderDate) throws SOSHibernateException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {

        if (conf == null || conf.getPath() == null || conf.getPath().isEmpty()) {
            return Collections.emptySet();
        }
        
        List<DBItemDeploymentHistory> dbItems = controllerIds.stream().flatMap(controllerId -> dbLayer.getLatestDepHistoryItemsFromFolder(conf
                .getPath(), controllerId, conf.getRecursive())).filter(item -> OperationType.DELETE.value() != item.getOperation()).collect(Collectors
                        .toList());
        
        Map<String, Map<DeployType, List<DBItemDeploymentHistory>>> itemsToDeletePerController = dbItems.stream().collect(Collectors.groupingBy(
                DBItemDeploymentHistory::getControllerId, Collectors.groupingBy(DBItemDeploymentHistory::getTypeAsEnum)));
        
        // check older workflow versions
        for (Map.Entry<String, Map<DeployType, List<DBItemDeploymentHistory>>> entry : itemsToDeletePerController.entrySet()) {
            checkIfWorkflowsHaveOrders(entry.getKey(), entry.getValue().getOrDefault(DeployType.WORKFLOW, Collections.emptyList()).stream().map(
                    DBItemDeploymentHistory::getName).collect(Collectors.toSet()));
        }
        
        final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
        final String commitIdForDeleteFileOrderSource = UUID.randomUUID().toString();
   
        // delete configurations optimistically
        Set<DBItemInventoryConfiguration> invItemsforTrash = getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, dbItems,
                commitIdForDeleteFromFolder, commitIdForDeleteFileOrderSource, account, auditlogId));
        deleteConfigurations(dbLayer, null, invItemsforTrash, accessToken, jocError, auditlogId, withoutFolderDeletion, withEvents);
        
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
                    UpdateItemUtils.updateItemsDelete(commitIdForDeleteFromFolder, sortedItems, controllerId).thenAccept(either -> processAfterDelete(
                            either, controllerId, account, commitIdForDeleteFromFolder, accessToken, jocError, cancelOrderDate));
                } else {
                    List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                    for (DeployType type : DELETE_ORDER) {
                        if (!type.equals(DeployType.FILEORDERSOURCE)) {
                            sortedItems.addAll(itemsToDeletePerController.get(controllerId).getOrDefault(type, Collections.emptyList()));
                        }
                    }
                    // send commands to controllers
                    Set<String> fileOrderSourceNames = fileOrderSourceItems.stream().map(item -> item.getName()).collect(Collectors.toSet());
                    UpdateItemUtils.updateItemsDelete(commitIdForDeleteFileOrderSource, fileOrderSourceItems, controllerId).thenAccept(either -> {
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
    
    private static void processAfterDelete(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError, String cancelOrderDate, String commitId2) {
        processAfterDelete(either, controllerId, account, commitId, accessToken, jocError, cancelOrderDate, null, commitId2, null);
    }
    
    public static void processAfterDelete(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError, String cancelOrderDate, List<DBItemDeploymentHistory> toDelete, String commitId2,
            Set<String> fileOrderSourceNames) {
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
                List<DBItemDeploymentHistory> currentOptimisticEntries = dbLayer.getDepHistory(commitId);
                List<DBItemDeploymentHistory> previousOptimisticEntries = dbLayer.getDepHistory(commitId2);
                List<DBItemDeploymentHistory> optimisticEntries = Stream
                        .concat(currentOptimisticEntries.stream(), previousOptimisticEntries.stream()).collect(Collectors.toList());
               
                // update all previously optimistically stored entries with the error message and change the state
                Map<Integer, Set<DBItemInventoryConfigurationTrash>> itemsFromTrashByType = 
                        new HashMap<Integer, Set<DBItemInventoryConfigurationTrash>>();
                InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    if(currentOptimisticEntries.contains(optimistic)) {
                        optimistic.setErrorMessage(either.getLeft().message());
                        optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                        optimistic.setDeleteDate(null);
                        dbLayer.getSession().update(optimistic);
                    }
                    // restore related inventory configuration - Recover and remove from trash
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
                    List<DBItemInventoryConfiguration> updated = new ArrayList<DBItemInventoryConfiguration>();
                    for (ConfigurationType objType : RESTORE_ORDER) {
                        Set<DBItemInventoryConfigurationTrash> itemsFromTrash = itemsFromTrashByType.get(objType.intValue());
                        if(itemsFromTrash != null) {
                            for (DBItemInventoryConfigurationTrash trashItem : itemsFromTrash) {
                                if (trashItem != null) {
                                    parentFolders.add(trashItem.getFolder());
                                    DBItemInventoryConfiguration recreatedFromTrash = recreateItem(trashItem, null, invDbLayer);
                                    JocInventory.insertConfiguration(invDbLayer, recreatedFromTrash);
                                    updated.add(recreatedFromTrash);
                                    invDbLayer.getSession().delete(trashItem);
                                }
                            }
                        }
                    }
                    DependencyResolver.updateDependencies(updated);
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
                    Set<OrderWatchPath> fosPaths = fileOrderSourceNames.stream().map(OrderWatchPath::of).collect(Collectors.toSet());
                    for (int second = 0; second < 10; second++) {
                        if (!proxy.currentState().pathToFileWatch().keySet().stream().anyMatch(fos -> fosPaths.contains(fos))) {
                            // file order source is deleted
                            break;
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (Exception e) {}
                    }
                    UpdateItemUtils.updateItemsDelete(commitId2, toDelete, controllerId).thenAccept(either2 -> processAfterDelete(either2,
                            controllerId, account, commitId2, accessToken, jocError, cancelOrderDate, commitId));
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
            String commitId, String controllerId, Long auditLogId, String account) {
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
                    newEntry.setControllerId(controllerId);
                    newEntry.setControllerInstanceId(0L); // TODO ???
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
    
    public static Set<DBItemInventoryConfiguration> getInvConfigurationsForTrash(DBLayerDeploy dbLayer,
            Set<DBItemDeploymentHistory> deletedDeployItems) {
        return deletedDeployItems.stream().map(item -> dbLayer.getConfigurationByName(item.getName(), item.getType())).filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
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
            String accessToken, JocError jocError) {
        processAfterRevoke(either, controllerId, account, commitId, accessToken, jocError, null, null, null);
    }
    
    public static void processAfterRevoke(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError, List<DBItemDeploymentHistory> toDelete, String commitId2, Set<String> fileOrderSourceNames) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
            final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
            Map<DBItemDeploymentHistory, DBItemInventoryConfiguration> toUpdate = optimisticEntries.stream().collect(Collectors.toMap(Function.identity(), 
                    item-> {
                        try {
                            return  dbLayer.getSession().get(DBItemInventoryConfiguration.class, item.getInventoryConfigurationId());
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        }
                    }));
            if (either.isLeft()) {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                // get all already optimistically stored entries for the commit
                // update all previously optimistically stored entries with the error message and change the state
                Set<DBItemInventoryConfiguration> updated = new HashSet<>();
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    optimistic.setErrorMessage(either.getLeft().message());
                    optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                    optimistic.setDeleteDate(null);
                    dbLayer.getSession().update(optimistic);
                    DBItemInventoryConfiguration orig = toUpdate.get(optimistic);
                    if (orig != null) {
                        orig.setDeployed(true);
                        dbLayer.getSession().update(orig);
                    }
                }
//                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
            } else {
                if (toDelete != null && commitId2 != null && !toDelete.isEmpty() && fileOrderSourceNames != null && !fileOrderSourceNames.isEmpty()) {
                    JControllerProxy proxy = Proxy.of(controllerId);
                    Set<OrderWatchPath> fosPaths = fileOrderSourceNames.stream().map(OrderWatchPath::of).collect(Collectors.toSet());
                    for (int second = 0; second < 10; second++) {
                        if (!proxy.currentState().pathToFileWatch().keySet().stream().anyMatch(fosPaths::contains)) {
                            // file order source is deleted
                            break;
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (Exception e) {
                        }
                    }
                    UpdateItemUtils.updateItemsDelete(commitId2, toDelete, controllerId).thenAccept(either2 -> processAfterRevoke(either2,
                            controllerId, account, commitId2, accessToken, jocError));
                }
                DependencyResolver.updateDependencies(toUpdate.values());
            }
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }
    
    public static void checkIfWorkflowsHaveOrders(String controllerId, Set<String> workflowNames) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {

        if (workflowNames != null && !workflowNames.isEmpty()) {
            checkIfWorkflowsHaveOrders(controllerId, Proxy.of(controllerId).currentState(), workflowNames);
        }
    }

    private static void checkIfWorkflowsHaveOrders(String controllerId, JControllerState currentState, Set<String> workflowNames) {
        currentState.idToOrder().values().stream().map(JOrder::workflowId).map(JWorkflowId::path).map(WorkflowPath::string).filter(
                workflowNames::contains).findAny().map(w -> String.format(
                        "Workflow '%s' on Controller '%s' still contains orders to process", w, controllerId)).map(JocBadRequestException::new)
                .ifPresent(e -> {
                    throw e;
                });
//        WorkflowsHelper.oldJWorkflowIds(currentState).map(JWorkflowId::path).map(WorkflowPath::string).filter(workflowNames::contains).findAny().map(
//                w -> String.format("Workflow '%s' on Controller '%s' has older version(s) that still have orders to process", w, controllerId)).map(
//                        JocBadRequestException::new).ifPresent(e -> {
//                            throw e;
//                        });
    }

}
