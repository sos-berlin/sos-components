package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RevokeFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IRevoke;
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("inventory/deployment")
public class RevokeImpl extends JOCResourceImpl implements IRevoke {

    private static final String API_CALL = "./inventory/deployment/revoke";
    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeImpl.class);
    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postRevoke(String xAccessToken, byte[] filter) throws Exception {
        return postRevoke(xAccessToken, filter, false);
    }

    public JOCDefaultResponse postRevoke(String xAccessToken, byte[] filter, boolean withoutFolderDeletion) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** revoke started ***" + started);
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validate(filter, RevokeFilter.class);
            RevokeFilter revokeFilter = Globals.objectMapper.readValue(filter, RevokeFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(revokeFilter.getAuditLog(), CategoryType.DEPLOYMENT);
            Set<String> allowedControllerIds = Collections.emptySet();
            allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream()
            		.filter(availableController -> getControllerPermissions(availableController, xAccessToken).getDeployments().getDeploy()).collect(Collectors.toSet());
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
                    Map<String, List<DBItemDeploymentHistory>> grouped = depHistoryDBItemsToRevoke.stream()
                            .collect(Collectors.groupingBy(DBItemDeploymentHistory::getPath));
                    depHistoryDBItemsToRevoke = grouped.keySet().stream().map(item -> grouped.get(item).get(0)).collect(Collectors.toList());
                }
            }
            // collect Items for set of folders
            List<DBItemDeploymentHistory> itemsFromFolderToRevoke = new ArrayList<DBItemDeploymentHistory>();
            for (String controllerId : controllerIds) {
                if (!foldersToRevoke.isEmpty()) {
                    foldersToRevoke.stream()
                    .map(folder -> dbLayer.getLatestDepHistoryItemsFromFolder(folder.getPath(), controllerId, folder.getRecursive()))
                    .forEach(itemsList -> itemsFromFolderToRevoke.addAll(itemsList));
                }
            }
            Map<String, List<DBItemDeploymentHistory>> itemsPerControllerToRevokeFromFolder = 
                    itemsFromFolderToRevoke.stream().collect(Collectors.groupingBy(DBItemDeploymentHistory::getControllerId));
            Date collectingItemsFinished = Date.from(Instant.now());
            LOGGER.trace("*** collecting items finished ***" + collectingItemsFinished);
            // Delete from all allowed controllers from filter
            final String commitIdForRevoke = UUID.randomUUID().toString();
            final String commitIdForRevokeFromFolder = UUID.randomUUID().toString();
            List<DBItemDeploymentHistory> filteredDepHistoryItemsToRevoke = Collections.emptyList();
            List<DBItemDeploymentHistory> filteredItemsFromFolderToRevoke = Collections.emptyList();
            // loop 1: store db entries optimistically
            for (String controllerId : controllerIds) {
                if (!allowedControllerIds.contains(controllerId)) {
                    continue;
                }
            	folderPermissions.setSchedulerId(controllerId);
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                // store history entries for delete operation optimistically
                if (depHistoryDBItemsToRevoke != null && !depHistoryDBItemsToRevoke.isEmpty()) {
                	filteredDepHistoryItemsToRevoke = depHistoryDBItemsToRevoke.stream()
                			.filter(history -> canAdd(history.getPath(), permittedFolders)).collect(Collectors.toList());
                	DeleteDeployments.storeNewDepHistoryEntriesForRevoke(dbLayer, filteredDepHistoryItemsToRevoke, commitIdForRevoke, dbAuditlog.getId());
                }
                if (itemsPerControllerToRevokeFromFolder != null && !itemsPerControllerToRevokeFromFolder.isEmpty()) {
                    if(itemsPerControllerToRevokeFromFolder.containsKey(controllerId)) {
                        // first filter for folder permissions
                        // remember filtered items for later
                        filteredItemsFromFolderToRevoke = itemsPerControllerToRevokeFromFolder.get(controllerId).stream()
                                .filter(fromFolder -> canAdd(fromFolder.getPath(), permittedFolders)).collect(Collectors.toList());
                        // second filter for not already deleted
                        final List<DBItemDeploymentHistory> itemsToDelete = filteredItemsFromFolderToRevoke.stream()
                                .filter(item -> item.getControllerId().equals(controllerId) && !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation())))
                                .collect(Collectors.toList());
                        itemsPerControllerToRevokeFromFolder.put(controllerId, itemsToDelete);
                        // store history entries for delete operation optimistically
                        DeleteDeployments.storeNewDepHistoryEntriesForRevoke(dbLayer, itemsToDelete, commitIdForRevokeFromFolder, dbAuditlog.getId());
                    }
                }
            }
            // loop 2: send commands to controllers
            for (String controllerId : allowedControllerIds) {
                if (filteredDepHistoryItemsToRevoke != null && !filteredDepHistoryItemsToRevoke.isEmpty()) {
                    // set new versionId for second round (delete items)
                    // call updateRepo command via Proxy of given controllers
                    final List<DBItemDeploymentHistory> toRevoke = filteredDepHistoryItemsToRevoke;
                    PublishUtils.updateItemsDelete(commitIdForRevoke, toRevoke, controllerId).thenAccept(either -> {
                        DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForRevoke, getAccessToken(), getJocError());
                    });
                }
                // process folder to Delete
                if (filteredItemsFromFolderToRevoke != null && !filteredItemsFromFolderToRevoke.isEmpty()) {
                    PublishUtils.updateItemsDelete(commitIdForRevokeFromFolder, itemsPerControllerToRevokeFromFolder.get(controllerId), controllerId)
                        .thenAccept(either -> {
                            DeleteDeployments.processAfterDelete(either, controllerId, account, commitIdForRevokeFromFolder, getAccessToken(), getJocError());
                        }); 
                } 
            }
            Date deployWSFinished = Date.from(Instant.now());
            LOGGER.trace("*** revoke finished ***" + deployWSFinished);
            LOGGER.trace("complete WS time : " + (deployWSFinished.getTime() - started.getTime()) + " ms");
            LOGGER.trace("collecting items took: " + (collectingItemsFinished.getTime() - started.getTime()) + " ms");
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
    
}