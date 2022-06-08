package com.sos.joc.publish.util;

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
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class DeleteDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDeployments.class);
    private static final List<DeployType> DELETE_ORDER = Arrays.asList(
            DeployType.FILEORDERSOURCE, 
            DeployType.WORKFLOW, 
            DeployType.JOBRESOURCE, 
            DeployType.NOTICEBOARD, 
            DeployType.LOCK);


    public static boolean delete(Collection<DBItemDeploymentHistory> dbItems, DBLayerDeploy dbLayer, String account, String accessToken,
            JocError jocError, Long auditlogId, boolean withoutFolderDeletion) throws SOSHibernateException {
        if (dbItems == null || dbItems.isEmpty()) {
            return true;
        }
        final String commitId = UUID.randomUUID().toString();
        final String commitIdforFileOrderSource = UUID.randomUUID().toString();
        List<DBItemInventoryConfiguration> invConfigurationsToDelete = new ArrayList<>();
        List<DBItemInventoryConfiguration> fileOrderSourcesToDelete = new ArrayList<>();
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
                invConfigurationsToDelete.addAll(getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, sortedItems, commitId))); 
                
                // send commands to controllers
                PublishUtils.updateItemsDelete(commitId, sortedItems, entry.getKey())
                    .thenAccept(either -> processAfterDelete(either, entry.getKey(), account, commitId, accessToken, jocError));
            } else {
                List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                for (DeployType type : DELETE_ORDER) {
                    if (!type.equals(DeployType.FILEORDERSOURCE)) {
                        sortedItems.addAll(entry.getValue().getOrDefault(type, Collections.emptyList()));
                    }
                }
                fileOrderSourcesToDelete.addAll(getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, fileOrderSourceItems,
                        commitIdforFileOrderSource)));
                invConfigurationsToDelete.addAll(getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, sortedItems, commitId)));

                // send commands to controllers
                PublishUtils.updateItemsDelete(commitIdforFileOrderSource, fileOrderSourceItems, entry.getKey())
                    .thenAccept(either2 -> {
                        processAfterDelete(either2, entry.getKey(), account, commitIdforFileOrderSource, accessToken, jocError);
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            //
                        }
                        PublishUtils.updateItemsDelete(commitId, sortedItems, entry.getKey())
                        .thenAccept(either -> {
                            processAfterDelete(either, entry.getKey(), account, commitId, accessToken, jocError);
                        });
                    });
            }
        }
        
        // delete configurations optimistically
        deleteConfigurations(dbLayer, null, fileOrderSourcesToDelete, commitIdforFileOrderSource, accessToken, jocError, auditlogId, withoutFolderDeletion);
        deleteConfigurations(dbLayer, null, invConfigurationsToDelete, commitId, accessToken, jocError, auditlogId, withoutFolderDeletion);
        
        return true;
    }
    
    public static boolean deleteFolder(String folder, boolean recursive, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion,
            boolean withEvents) throws SOSHibernateException {
        Configuration conf = new Configuration();
        conf.setObjectType(ConfigurationType.FOLDER);
        conf.setPath(folder);
        conf.setRecursive(recursive);
        return deleteFolder(conf, controllerIds, dbLayer, account, accessToken, jocError, auditlogId, withoutFolderDeletion,
                withEvents);
    }
    
    public static boolean deleteFolder(Configuration conf, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion,
            boolean withEvents) throws SOSHibernateException {
        if (conf == null || conf.getPath() == null || conf.getPath().isEmpty()) {
            return true;
        }
   
        final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
        final String commitIdForDeleteFileOrderSource = UUID.randomUUID().toString();
        List<DBItemInventoryConfiguration> invConfigurationsToDelete = new ArrayList<>();
        List<DBItemInventoryConfiguration> fileOrderSourceToDelete = new ArrayList<>();
        Map<String, Map<DeployType, List<DBItemDeploymentHistory>>> itemsToDeletePerController =
                new HashMap<String, Map<DeployType, List<DBItemDeploymentHistory>>>();
        // optimistic DB operations
        for (String controllerId : controllerIds) {
            // determine all (latest) entries from the given folder
            itemsToDeletePerController.put(controllerId, dbLayer.getLatestDepHistoryItemsFromFolder(conf.getPath(), controllerId, conf.getRecursive())
                    .stream().filter(item -> OperationType.DELETE.value() != item.getOperation()).collect(Collectors.groupingBy(
                            DBItemDeploymentHistory::getTypeAsEnum)));

            if (!itemsToDeletePerController.get(controllerId).isEmpty()) {
                
                List<DBItemDeploymentHistory> fileOrderSourceItems = itemsToDeletePerController.get(controllerId).getOrDefault(
                        DeployType.FILEORDERSOURCE, Collections.emptyList());
                if (fileOrderSourceItems.isEmpty() || itemsToDeletePerController.keySet().size() == 1) {
                    List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                    for (DeployType type : DELETE_ORDER) {
                        sortedItems.addAll(itemsToDeletePerController.get(controllerId).getOrDefault(type, Collections.emptyList()));
                    }
                    // store history entries optimistically
                    invConfigurationsToDelete.addAll(getInvConfigurationsForTrash(dbLayer,
                            storeNewDepHistoryEntries(dbLayer, sortedItems, commitIdForDeleteFromFolder)));
                    
                    // send commands to controllers
                    PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, sortedItems, controllerId).thenAccept(
                            either -> processAfterDelete(either, controllerId, account, commitIdForDeleteFromFolder, accessToken, jocError));
                } else {
                    List<DBItemDeploymentHistory> sortedItems = new ArrayList<>();
                    for (DeployType type : DELETE_ORDER) {
                        if (!type.equals(DeployType.FILEORDERSOURCE)) {
                            sortedItems.addAll(itemsToDeletePerController.get(controllerId).getOrDefault(type, Collections.emptyList()));
                        }
                    }
                    // store history entries optimistically
                    fileOrderSourceToDelete.addAll(getInvConfigurationsForTrash(dbLayer, 
                            storeNewDepHistoryEntries(dbLayer, fileOrderSourceItems, commitIdForDeleteFileOrderSource)));
                    invConfigurationsToDelete.addAll(getInvConfigurationsForTrash(dbLayer, 
                            storeNewDepHistoryEntries(dbLayer, sortedItems, commitIdForDeleteFromFolder)));
                    
                    // send commands to controllers
                    PublishUtils.updateItemsDelete(commitIdForDeleteFileOrderSource, fileOrderSourceItems, controllerId).thenAccept(
                            either2 -> {
                                processAfterDelete(either2, controllerId, account, commitIdForDeleteFileOrderSource, accessToken, jocError);
                                try {
                                    TimeUnit.SECONDS.sleep(10);
                                } catch (InterruptedException e) {
                                    //
                                }
                                PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, sortedItems, controllerId).thenAccept(
                                        either -> processAfterDelete(either, controllerId, account, commitIdForDeleteFromFolder, accessToken, jocError));
                            });
                }
            }
        }
        
        // delete configurations optimistically
        deleteConfigurations(dbLayer, null, fileOrderSourceToDelete, commitIdForDeleteFileOrderSource,
                accessToken, jocError, auditlogId, withoutFolderDeletion, withEvents);
        deleteConfigurations(dbLayer, Collections.singletonList(conf), invConfigurationsToDelete, commitIdForDeleteFromFolder,
                accessToken, jocError, auditlogId, withoutFolderDeletion, withEvents);
        return true;
    }
    
    public static boolean delete(Collection<Configuration> confs, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion)
            throws SOSHibernateException {
        if (confs == null || confs.isEmpty()) {
            return true;
        }
        List<Configuration> deployConfigsToDelete = confs.stream().filter(Objects::nonNull).filter(item -> !ConfigurationType.FOLDER.equals(item
                .getObjectType())).collect(Collectors.toList());
        List<Configuration> foldersToDelete = confs.stream().filter(Objects::nonNull).filter(item -> ConfigurationType.FOLDER.equals(item
                .getObjectType())).collect(Collectors.toList());
        foldersToDelete = PublishUtils.handleFolders1(foldersToDelete, dbLayer);

        List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete = null;
        if (deployConfigsToDelete != null && !deployConfigsToDelete.isEmpty()) {
            depHistoryDBItemsToDeployDelete = dbLayer.getFilteredDeploymentHistoryToDelete(deployConfigsToDelete);
            if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToDeployDelete.stream().collect(Collectors.groupingBy(
                        DBItemDeploymentHistory::getPath));
                depHistoryDBItemsToDeployDelete = grouped.values().stream().map(item -> item.get(0)).collect(Collectors.toList());
            }
        }
        
        final String commitId = UUID.randomUUID().toString();
        final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
        List<DBItemInventoryConfiguration> invConfigurationsToDelete = new ArrayList<>();
        Map<String, List<DBItemDeploymentHistory>> itemsToDeletePerController = new HashMap<String, List<DBItemDeploymentHistory>>();
        Map<String, List<DBItemDeploymentHistory>> itemsFromFolderToDeletePerController = new HashMap<String, List<DBItemDeploymentHistory>>();

        // optimistic DB operations
        for (String controllerId : controllerIds) {
            if (foldersToDelete != null && !foldersToDelete.isEmpty()) {
                itemsFromFolderToDeletePerController.put(controllerId, foldersToDelete.stream().map(item -> dbLayer
                            .getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId, item.getRecursive())).filter(Objects::nonNull).flatMap(
                                    List::stream).collect(Collectors.toList()));
                // store history entries optimistically
                invConfigurationsToDelete.addAll(getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, 
                        itemsFromFolderToDeletePerController.get(controllerId), commitIdForDeleteFromFolder)));
            }
            if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                itemsToDeletePerController.put(controllerId, depHistoryDBItemsToDeployDelete);
                // store history entries optimistically
                invConfigurationsToDelete.addAll(getInvConfigurationsForTrash(dbLayer, storeNewDepHistoryEntries(dbLayer, 
                        itemsToDeletePerController.get(controllerId), commitIdForDeleteFromFolder)));
            }
        }
        // delete configurations optimistically
        deleteConfigurations(dbLayer, foldersToDelete, invConfigurationsToDelete, commitIdForDeleteFromFolder, accessToken, jocError, 
                auditlogId, withoutFolderDeletion);

        // send commands to controllers
        for (String controllerId : controllerIds) {
            if (itemsToDeletePerController.get(controllerId) != null && !itemsToDeletePerController.get(controllerId).isEmpty()) {
                // send command to controller
                PublishUtils.updateItemsDelete(commitId, itemsToDeletePerController.get(controllerId), controllerId).thenAccept(
                        either -> processAfterDelete(either, controllerId, account, commitId, accessToken, jocError));
            }
            // process folder to Delete
            if (itemsFromFolderToDeletePerController.get(controllerId) != null && !itemsFromFolderToDeletePerController.get(controllerId).isEmpty()) {
                PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsFromFolderToDeletePerController.get(controllerId), controllerId).thenAccept(
                        either -> processAfterDelete(either, controllerId, account, commitIdForDeleteFromFolder, accessToken, jocError));
            }
        }
        return true;
    }

    public static void processAfterDelete(Either<Problem, Void> either, String controllerId, String account, String commitId, 
            String accessToken, JocError jocError) {
        SOSHibernateSession newHibernateSession = null;
        try {
            if (either.isLeft()) {
                newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
                final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected

                // get all already optimistically stored entries for the commit
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                // update all previously optimistically stored entries with the error message and change the state
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    optimistic.setErrorMessage(either.getLeft().message());
                    optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                    optimistic.setDeleteDate(null);
                    dbLayer.getSession().update(optimistic);
                    // TODO: restore related inventory configuration - Recover and remove from trash
                    

                }
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

    public static Set<DBItemDeploymentHistory> storeNewDepHistoryEntries(DBLayerDeploy dbLayer, List<DBItemDeploymentHistory> itemsToDelete, String commitId) {
        return PublishUtils.updateDeletedDepHistory(itemsToDelete, dbLayer, commitId, false);
    }

    public static Set<DBItemDeploymentHistory> storeNewDepHistoryEntriesForRevoke(DBLayerDeploy dbLayer, List<DBItemDeploymentHistory> deletedItems,
            String commitId, Long auditLogId) {
        Set<DBItemDeploymentHistory> deletedObjects = Collections.emptySet();
        Set<String> folders = Collections.emptySet();
        try {
            if (deletedItems != null && !deletedItems.isEmpty()) {
                folders = deletedItems.stream().map(item -> item.getFolder()).collect(Collectors.toSet());
                deletedObjects = new HashSet<DBItemDeploymentHistory>();
                for (DBItemDeploymentHistory item : deletedItems) {
                    item.setId(null);
                    item.setCommitId(commitId);
                    item.setOperation(OperationType.DELETE.value());
                    item.setState(DeploymentState.DEPLOYED.value());
                    item.setDeleteDate(Date.from(Instant.now()));
                    item.setDeploymentDate(Date.from(Instant.now()));
                    item.setAuditlogId(auditLogId);
                    if (item.getSignedContent() == null || item.getSignedContent().isEmpty()) {
                        item.setSignedContent(".");
                    }
                    dbLayer.getSession().save(item);
                    deletedObjects.add(item);
                    DBItemInventoryConfiguration orig = dbLayer.getInventoryConfigurationByNameAndType(item.getName(), item.getType());
                    if (orig != null) {
                        orig.setDeployed(false);
                        dbLayer.getSession().update(orig);
                    }
                }
                folders.stream().forEach(folder -> JocInventory.postEvent(folder));
            }

        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deletedObjects;
    }

    public static List<DBItemInventoryConfiguration> getInvConfigurationsForTrash (DBLayerDeploy dbLayer, Set<DBItemDeploymentHistory> deletedDeployItems ) {
        return dbLayer.getInventoryConfigurationsByIds(
                deletedDeployItems.stream().map(item -> item.getInventoryConfigurationId()).distinct().collect(Collectors.toList()));
    }
    
    public static void deleteConfigurations(DBLayerDeploy dbLayer, List<Configuration> folders, List<DBItemInventoryConfiguration> itemsToDelete, 
            String commitId, String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion) {
        deleteConfigurations(dbLayer, folders, itemsToDelete, commitId, accessToken, jocError, auditlogId, withoutFolderDeletion, true);
    }
    
    public static void deleteConfigurations(DBLayerDeploy dbLayer, List<Configuration> folders, List<DBItemInventoryConfiguration> itemsToDelete,
            String commitId, String accessToken, JocError jocError, Long auditlogId, boolean withoutFolderDeletion,
            boolean withEvents) {
        // add all elements from the folder(s)
        Set<String> foldersForEvent = new HashSet<>();
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
        for (DBItemInventoryConfiguration invConfiguration : itemsToDelete.stream().collect(Collectors.toSet())) {
            invConfiguration.setAuditLogId(auditlogId);
            JocInventory.deleteInventoryConfigurationAndPutToTrash(invConfiguration, invDbLayer);
            if (withEvents) {
                foldersForEvent.add(invConfiguration.getFolder());
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
    }
        
}
