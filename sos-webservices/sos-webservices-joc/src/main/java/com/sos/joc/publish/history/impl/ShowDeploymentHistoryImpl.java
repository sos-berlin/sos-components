package com.sos.joc.publish.history.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DepHistory;
import com.sos.joc.model.publish.DepHistoryItem;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.history.resource.IShowDeploymentHistory;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class ShowDeploymentHistoryImpl extends JOCResourceImpl implements IShowDeploymentHistory {

    private static final String API_CALL = "./inventory/deployment/history";

    @Override
    public JOCDefaultResponse postShowDeploymentHistory(String xAccessToken, byte[] showDepHistoryFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            showDepHistoryFilter = initLogging(API_CALL, showDepHistoryFilter, xAccessToken, CategoryType.DEPLOYMENT);
            JsonValidator.validate(showDepHistoryFilter, ShowDepHistoryFilter.class);
            ShowDepHistoryFilter filter = Globals.objectMapper.readValue(showDepHistoryFilter, ShowDepHistoryFilter.class);
            
            String controllerId = null;
            if (filter.getCompactFilter() != null) {
                controllerId = filter.getCompactFilter().getControllerId();
            } else {
                controllerId = filter.getDetailFilter().getControllerId();
            }
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                if (Proxies.getControllerDbInstances().isEmpty()) {
                    permitted = getBasicControllerDefaultPermissions(xAccessToken).getDeployments().getView();
                } else {
                    allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                            availableController -> getBasicControllerPermissions(availableController, xAccessToken).getDeployments().getView())
                            .collect(Collectors.toSet());
                    permitted = !allowedControllers.isEmpty();
                    if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                        allowedControllers = Collections.emptySet();
                    }
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getBasicControllerPermissions(controllerId, xAccessToken).getDeployments().getView();
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            if (Proxies.getControllerDbInstances().isEmpty()) {
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(getDepHistoryFromDBItems(Stream.empty())));
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemDeploymentHistory> dbHistoryItems = new ArrayList<DBItemDeploymentHistory>();
            Map<String, Set<Folder>> permittedFolders = folderPermissions.getListOfFolders(allowedControllers.isEmpty() ? Proxies
                    .getControllerDbInstances().keySet() : allowedControllers);

            Predicate<DBItemDeploymentHistory> canAdd = item -> {
                Set<Folder> pFolders = permittedFolders.get(item.getControllerId());
                return pFolders != null && canAdd(item.getPath(), pFolders);
            };
            
            Stream<DepHistoryItem> dbHistoryItemStream = Stream.empty();
            if (filter.getCompactFilter() != null) {
                dbHistoryItems = dbLayer.getDeploymentHistoryCommits(filter, allowedControllers);
                Map<String, List<DBItemDeploymentHistory>> groupedDbHistoryItems = dbHistoryItems.stream().filter(canAdd).collect(Collectors
                        .groupingBy(DBItemDeploymentHistory::getCommitId));
                dbHistoryItemStream = groupedDbHistoryItems.entrySet().stream().map(item -> {
                    Map<Integer, Long> counts = item.getValue().stream().collect(Collectors.groupingBy(DBItemDeploymentHistory::getType, Collectors
                            .counting()));
                    DBItemDeploymentHistory entry = item.getValue().get(0);
                    entry.setId(null);
                    entry.setType(null);
                    entry.setPath(null);
                    entry.setFolder(null);
                    entry.setInventoryConfigurationId(null);
                    DepHistoryItem dhItem = mapDBItemToDepHistoryItem(entry);
                    dhItem.setWorkflowCount(counts.get(DeployType.WORKFLOW.intValue()));
                    dhItem.setFileOrderSourceCount(counts.get(DeployType.FILEORDERSOURCE.intValue()));
                    dhItem.setJobResourceCount(counts.get(DeployType.JOBRESOURCE.intValue()));
                    dhItem.setBoardCount(counts.get(DeployType.NOTICEBOARD.intValue()));
                    dhItem.setLockCount(counts.get(DeployType.LOCK.intValue()));
                    return dhItem;
                });
                if (filter.getCompactFilter().getLimit() > -1) {
                    dbHistoryItemStream = dbHistoryItemStream.limit(filter.getCompactFilter().getLimit());
                }
            } else {
                dbHistoryItemStream = dbLayer.getDeploymentHistoryDetails(filter, allowedControllers).stream().filter(canAdd).map(
                        this::mapDBItemToDepHistoryItem);
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(getDepHistoryFromDBItems(dbHistoryItemStream)));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
             if (hibernateSession != null) {
                 hibernateSession.close();
             }
        }
    }
    
    private DepHistory getDepHistoryFromDBItems(Stream<DepHistoryItem> dbHistoryItems) {
        DepHistory depHistory = new DepHistory();
        depHistory.setDepHistory(dbHistoryItems.toList());
        depHistory.setDeliveryDate(Date.from(Instant.now()));
        return depHistory;
    }

    private DepHistoryItem mapDBItemToDepHistoryItem(DBItemDeploymentHistory dbItem) {
        DepHistoryItem depHistoryItem = new DepHistoryItem();
        depHistoryItem.setAccount(dbItem.getAccount());
        depHistoryItem.setCommitId(dbItem.getCommitId());
        depHistoryItem.setControllerId(dbItem.getControllerId());
        depHistoryItem.setDeleteDate(dbItem.getDeleteDate());
        depHistoryItem.setDeploymentDate(dbItem.getDeploymentDate());
        depHistoryItem.setDeploymentId(dbItem.getId());
        if (dbItem.getType() != null) {
            depHistoryItem.setDeployType(ConfigurationType.fromValue(dbItem.getType()));
        }
        depHistoryItem.setFolder(dbItem.getFolder());
        depHistoryItem.setInvConfigurationId(dbItem.getInventoryConfigurationId());
        if(dbItem.getOperation() != null) {
            depHistoryItem.setOperation(OperationType.fromValue(dbItem.getOperation()));
        }
        depHistoryItem.setPath(dbItem.getPath());
        if(dbItem.getState() != null) {
            depHistoryItem.setState(DeploymentState.fromValue(dbItem.getState()));
        }
        if (dbItem.getErrorMessage() != null && !dbItem.getErrorMessage().isEmpty()) {
            depHistoryItem.setErrorMessage(dbItem.getErrorMessage());
        }
        depHistoryItem.setVersion(dbItem.getVersion());
        return depHistoryItem;
        
    }

}
