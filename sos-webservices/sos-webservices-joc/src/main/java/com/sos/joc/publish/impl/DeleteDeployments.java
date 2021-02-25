package com.sos.joc.publish.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.PublishUtils;

import io.vavr.control.Either;
import js7.base.problem.Problem;

public class DeleteDeployments {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDeployments.class);

    // account: LOW -> Globals.defaultProfileAccount, MEDIUM, HIGH -> jobschedulerUser.getSosShiroCurrentUser().getUsername()

    public static boolean delete(List<DBItemDeploymentHistory> dbItems, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, boolean withoutFolderDeletion) throws SOSHibernateException {
        if (dbItems == null || dbItems.isEmpty()) {
            return true;
        }
        for (String controllerId : controllerIds) {
            final String commitId = UUID.randomUUID().toString();
            final List<DBItemDeploymentHistory> itemsToDelete = 
                    dbItems.stream().filter(item -> controllerId.equals(item.getControllerId())).collect(Collectors.toList());
            // Call ControllerApi 
            PublishUtils.updateItemsDelete(commitId, itemsToDelete, controllerId).thenAccept(
                    either -> processAfterDelete(either, itemsToDelete, controllerId, account, commitId, accessToken, jocError));
            // store history entries and delete configurations optimistically
            storeDepHistoryAndDeleteSetOfConfigurations(dbLayer, itemsToDelete, commitId);
        }
        return true;
    }
    
    public static boolean delete(Collection<Configuration> confs, Collection<String> controllerIds, DBLayerDeploy dbLayer, String account,
            String accessToken, JocError jocError, JocSecurityLevel secLevel, boolean withoutFolderDeletion) throws SOSHibernateException {
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
        
        for (String controllerId : controllerIds) {
            final String commitId = UUID.randomUUID().toString();
            final String commitIdForDeleteFromFolder = UUID.randomUUID().toString();

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
                storeDepHistoryAndDeleteSetOfConfigurations(dbLayer, itemsToDelete, commitId);
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
                storeDepHistoryAndDeleteConfigurationsFromFolder(dbLayer, foldersToDelete, itemsToDelete, commitIdForDeleteFromFolder, accessToken, 
                        jocError, withoutFolderDeletion);
            }
        }
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
            List<Configuration> foldersToDelete, String controllerId, String account, String versionIdForDelete, String accessToken,
            JocError jocError, boolean withoutFolderDeletion) {
        SOSHibernateSession newHibernateSession = null;
        try {
            if (either.isLeft()) {
                newHibernateSession = Globals.createSosHibernateStatelessConnection("./inventory/deployment/deploy");
                final DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
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

    private static boolean checkDeploymentItemsStillExist(Set<DBItemDeploymentHistory> deployments) {
        if (deployments != null && !deployments.isEmpty()) {
            return true;
        }
        return false;
    }
    
    public static void storeDepHistoryAndDeleteSetOfConfigurations(DBLayerDeploy dbLayer, List<DBItemDeploymentHistory> itemsToDelete,
            String commitId) {
        Set<DBItemInventoryConfiguration> configurationsToDelete = itemsToDelete.stream().map(item -> 
            dbLayer.getInventoryConfigurationByNameAndType(item.getName(), item.getType())).collect(Collectors.toSet());
        Set<DBItemDeploymentHistory> deletedDeployItems = 
                PublishUtils.updateDeletedDepHistoryAndPutToTrash(itemsToDelete, dbLayer, commitId);
        configurationsToDelete.stream().forEach(item -> 
            JocInventory.deleteInventoryConfigurationAndPutToTrash(item, new InventoryDBLayer(dbLayer.getSession())));
        configurationsToDelete.stream().map(DBItemInventoryConfiguration::getFolder).distinct().forEach(item -> JocInventory.postEvent(item));
        JocInventory.handleWorkflowSearch(dbLayer.getSession(), deletedDeployItems, true);
    }

    public static void storeDepHistoryAndDeleteConfigurationsFromFolder(DBLayerDeploy dbLayer, List<Configuration> folders, 
            List<DBItemDeploymentHistory> itemsToDelete, String commitId, String accessToken, JocError jocError, boolean withoutFolderDeletion) {
        Stream<DBItemInventoryConfiguration> configurationsToDeleteStream = itemsToDelete.stream().map(item -> dbLayer
                .getInventoryConfigurationByNameAndType(item.getName(), item.getType()));

        Stream<DBItemInventoryConfiguration> foldersToDeleteStream = folders.stream().map(item -> dbLayer
                .getInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())).filter(
                        Objects::nonNull).flatMap(List::stream);

        List<DBItemInventoryConfiguration> configurationsToDelete = Stream.concat(configurationsToDeleteStream, foldersToDeleteStream)
                .collect(Collectors.toList());
        Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistoryAndPutToTrash(itemsToDelete, dbLayer,
                commitId);
        InventoryDBLayer invDbLayer = new InventoryDBLayer(dbLayer.getSession());
        configurationsToDelete.stream().forEach(item -> JocInventory.deleteInventoryConfigurationAndPutToTrash(item, invDbLayer));
        configurationsToDelete.stream().map(item -> item.getFolder()).distinct().forEach(item -> JocInventory.postEvent(item));
        JocInventory.handleWorkflowSearch(dbLayer.getSession(), deletedDeployItems, true);
        if (!withoutFolderDeletion) {
            if (folders != null && !folders.isEmpty()) {
                for (Configuration folder : folders) {
                    // check if deployable objects still exist in the folder
                    Set<DBItemDeploymentHistory> stillActiveDeployments = PublishUtils.getLatestDepHistoryEntriesActiveForFolder(folder,
                            dbLayer);
                    if (checkDeploymentItemsStillExist(stillActiveDeployments)) {
                        String controllersFormatted = stillActiveDeployments.stream().map(item -> item.getControllerId()).collect(Collectors
                                .joining(", "));
                        LOGGER.warn(String.format(
                                "removed folder \"%1$s\" can´t be deleted from inventory. Deployments still exist on controllers %2$s.",
                                folder.getPath(), controllersFormatted));
                    } else {
                        try {
                            JocInventory.deleteEmptyFolders(invDbLayer, folder.getPath());
                        } catch (SOSHibernateException e) {
                            ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(e.toString())), accessToken, jocError, null);
                        }
                    }
                }
            }
        }
    }
}
