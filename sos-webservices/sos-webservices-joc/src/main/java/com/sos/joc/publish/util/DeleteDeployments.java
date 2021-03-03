package com.sos.joc.publish.util;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class DeleteDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDeployments.class);

    // account: LOW -> Globals.defaultProfileAccount, MEDIUM, HIGH -> jobschedulerUser.getSosShiroCurrentUser().getUsername()

    public static boolean delete(Collection<DBItemDeploymentHistory> dbItems, DBLayerDeploy dbLayer, String account, String accessToken,
            JocError jocError, boolean withoutFolderDeletion) throws SOSHibernateException {
        if (dbItems == null || dbItems.isEmpty()) {
            return true;
        }
        Map<String, List<DBItemDeploymentHistory>> dbItemsPerController = dbItems.stream().filter(Objects::nonNull).filter(
                item -> OperationType.UPDATE.value() == item.getOperation()).collect(Collectors.groupingBy(DBItemDeploymentHistory::getControllerId));
        final String commitId = UUID.randomUUID().toString();
        Set<DBItemInventoryConfiguration> invConfigurationsToDelete = Collections.emptySet();

        for (Map.Entry<String, List<DBItemDeploymentHistory>> entry : dbItemsPerController.entrySet()) {
            // Call ControllerApi
            PublishUtils.updateItemsDelete(commitId, entry.getValue(), entry.getKey()).thenAccept(either -> processAfterDelete(either, entry
                    .getValue(), entry.getKey(), account, commitId, accessToken, jocError));
            // store history entries optimistically
            if (invConfigurationsToDelete.isEmpty()) {
                invConfigurationsToDelete = new HashSet<>(
                        getInvConfigurationsForTrash(dbLayer, 
                                storeNewDepHistoryEntries(dbLayer, entry.getValue(), commitId)));
            } else {
                invConfigurationsToDelete.addAll(
                        getInvConfigurationsForTrash(dbLayer, 
                                storeNewDepHistoryEntries(dbLayer, entry.getValue(), commitId)));
            }
        }
        // delete configurations optimistically
        deleteConfigurations(dbLayer, null, invConfigurationsToDelete, commitId, accessToken, jocError, withoutFolderDeletion);
        return true;
    }
    
    public static boolean deleteFolder(String folder, boolean recursive, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, boolean withoutFolderDeletion) throws SOSHibernateException {
        Configuration conf = new Configuration();
        conf.setObjectType(ConfigurationType.FOLDER);
        conf.setPath(folder);
        conf.setRecursive(recursive);
        return deleteFolder(conf, controllerIds, dbLayer, account, accessToken, jocError, withoutFolderDeletion);
    }
    
    public static boolean deleteFolder(Configuration conf, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, boolean withoutFolderDeletion) throws SOSHibernateException {
        if (conf == null || conf.getPath() == null || conf.getPath().isEmpty()) {
            return true;
        }
        
        final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();
        Set<DBItemInventoryConfiguration> invConfigurationsToDelete = Collections.emptySet();
        
        for (String controllerId : controllerIds) {
            // determine all (latest) entries from the given folder
            List<DBItemDeploymentHistory> itemsToDelete = dbLayer.getLatestDepHistoryItemsFromFolder(conf.getPath(), controllerId, conf
                    .getRecursive()).stream().filter(item -> OperationType.DELETE.value() != item.getOperation()).collect(Collectors.toList());
            
            if (!itemsToDelete.isEmpty()) {
                PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsToDelete, controllerId).thenAccept(
                        either -> processAfterDeleteFromFolder(either, itemsToDelete, Collections.singletonList(conf), controllerId, account,
                                commitIdForDeleteFromFolder, accessToken, jocError, withoutFolderDeletion));
                // store history entries optimistically
                if (invConfigurationsToDelete.isEmpty()) {
                    invConfigurationsToDelete = new HashSet<>(
                            getInvConfigurationsForTrash(dbLayer, 
                                    storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                } else {
                    invConfigurationsToDelete.addAll(
                            getInvConfigurationsForTrash(dbLayer, 
                                    storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                }
            }
        }
        // delete configurations optimistically
        deleteConfigurations(dbLayer, Collections.singletonList(conf), invConfigurationsToDelete, commitIdForDeleteFromFolder, accessToken, jocError, 
                withoutFolderDeletion);
        return true;
    }
    
    public static boolean delete(Collection<Configuration> confs, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, boolean withoutFolderDeletion) throws SOSHibernateException {
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
        Set<DBItemInventoryConfiguration> invConfigurationsToDelete = Collections.emptySet();
        for (String controllerId : controllerIds) {

            List<DBItemDeploymentHistory> itemsFromFolderToDelete = Collections.emptyList();

            if (foldersToDelete != null && !foldersToDelete.isEmpty()) {
                itemsFromFolderToDelete = foldersToDelete.stream().map(item -> dbLayer
                        .getLatestDepHistoryItemsFromFolder(item.getPath(), controllerId, item.getRecursive())).filter(Objects::nonNull).flatMap(
                                List::stream).collect(Collectors.toList());
            }

            if (depHistoryDBItemsToDeployDelete != null && !depHistoryDBItemsToDeployDelete.isEmpty()) {
                final List<DBItemDeploymentHistory> itemsToDelete = depHistoryDBItemsToDeployDelete;
                PublishUtils.updateItemsDelete(commitId, itemsToDelete, controllerId).thenAccept(
                        either -> processAfterDelete(either, itemsToDelete, controllerId, account, commitId, accessToken, jocError));
                // store history entries optimistically
                if (invConfigurationsToDelete.isEmpty()) {
                    invConfigurationsToDelete = new HashSet<>(
                            getInvConfigurationsForTrash(dbLayer, 
                                    storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                } else {
                    invConfigurationsToDelete.addAll(
                            getInvConfigurationsForTrash(dbLayer, 
                                    storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                }
           }
            // process folder to Delete
            if (itemsFromFolderToDelete != null && !itemsFromFolderToDelete.isEmpty()) {
                // determine all (latest) entries from the given folder
                final List<Configuration> folders = foldersToDelete;
                final List<DBItemDeploymentHistory> itemsToDelete = itemsFromFolderToDelete.stream().filter(item -> item.getControllerId().equals(
                        controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation()))).collect(Collectors.toList());
                PublishUtils.updateItemsDelete(commitIdForDeleteFromFolder, itemsToDelete, controllerId)
                        .thenAccept(either -> processAfterDeleteFromFolder(either, itemsToDelete, folders, controllerId, account,
                                commitIdForDeleteFromFolder, accessToken, jocError, withoutFolderDeletion));
                // store history entries optimistically
                if (invConfigurationsToDelete.isEmpty()) {
                    invConfigurationsToDelete = new HashSet<>(
                            getInvConfigurationsForTrash(dbLayer, 
                                    storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                } else {
                    invConfigurationsToDelete.addAll(
                            getInvConfigurationsForTrash(dbLayer, 
                                    storeNewDepHistoryEntries(dbLayer, itemsToDelete, commitIdForDeleteFromFolder)));
                }
            }
        }
        // delete configurations optimistically
        deleteConfigurations(dbLayer, foldersToDelete, invConfigurationsToDelete, commitIdForDeleteFromFolder, accessToken, jocError, 
                withoutFolderDeletion);
        return true;
    }

    public static void processAfterDelete(Either<Problem, Void> either, List<DBItemDeploymentHistory> itemsToDelete, String controllerId,
            String account, String versionIdForDelete, String accessToken, JocError jocError) {
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
            final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isLeft()) {
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.warn(message);
                // updateRepo command is atomic, therefore all items are rejected
                List<DBItemDeploymentHistory> failedDeployDeleteItems = dbLayer.updateFailedDeploymentForDelete(itemsToDelete, controllerId, account,
                        versionIdForDelete, either.getLeft().message());
                // if not successful the objects and the related controllerId have to be stored
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(failedDeployDeleteItems);
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

    public static void processAfterDeleteFromFolder(Either<Problem, Void> either, List<DBItemDeploymentHistory> itemsToDelete,
            List<Configuration> foldersToDelete, String controllerId, String account, String commitId, String accessToken,
            JocError jocError, boolean withoutFolderDeletion) {
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
                    // update related inventory configuration,  Recover from trash?
//                    DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(optimistic.getInventoryConfigurationId());
//                    cfg.setDeployed(false);
//                    dbLayer.getSession().update(cfg);
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

    public static List<DBItemInventoryConfiguration> getInvConfigurationsForTrash (DBLayerDeploy dbLayer, Set<DBItemDeploymentHistory> deletedDeployItems ) {
        return dbLayer.getInventoryConfigurationsByIds(
                deletedDeployItems.stream().map(item -> item.getInventoryConfigurationId()).collect(Collectors.toSet()));
    }
    
    public static void deleteConfigurations(DBLayerDeploy dbLayer, List<Configuration> folders, Set<DBItemInventoryConfiguration> itemsToDelete, 
            String commitId, String accessToken, JocError jocError, boolean withoutFolderDeletion) {
        // add all elements from the folder(s)
        Set<String> foldersForEvent = new HashSet<>();
        if (folders != null) {
            for (Configuration folder : folders) {
                itemsToDelete.addAll(dbLayer.getInventoryConfigurationsByFolder(folder.getPath(), folder.getRecursive()));
                if ("/".equals(folder.getPath())) {
                    foldersForEvent.add("/");
                } else {
                    foldersForEvent.add(Paths.get(folder.getPath()).getParent().toString().replace('\\', '/'));
                }
            }
        }
        // delete and put to trash
        InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
        for (DBItemInventoryConfiguration invConfiguration : itemsToDelete) {
            JocInventory.deleteInventoryConfigurationAndPutToTrash(invConfiguration, invDbLayer);
            foldersForEvent.add(invConfiguration.getFolder());
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
