package com.sos.joc.publish.history.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DepHistory;
import com.sos.joc.model.publish.DepHistoryItem;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.history.DepHistoryItemsCount;
import com.sos.joc.publish.history.resource.IShowDeploymentHistory;
import com.sos.schema.JsonValidator;

@Path("inventory/deployment")
public class ShowDeploymentHistoryImpl extends JOCResourceImpl implements IShowDeploymentHistory {

    private static final String API_CALL = "./inventory/deployment/history";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowDeploymentHistoryImpl.class);

    @Override
    public JOCDefaultResponse postShowDeploymentHistory(String xAccessToken, byte[] showDepHistoryFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, showDepHistoryFilter, xAccessToken);
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
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, xAccessToken).getDeployments().getView()).collect(
                                Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet(); 
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, xAccessToken).getDeployments().getView();
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemDeploymentHistory> dbHistoryItems = new ArrayList<DBItemDeploymentHistory>();
            folderPermissions.setSchedulerId(controllerId);
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            if (filter.getCompactFilter() != null) {
                dbHistoryItems = dbLayer.getDeploymentHistoryCommits(filter, allowedControllers);
                Map<String, List<DBItemDeploymentHistory>> groupedDbHistoryItems = dbHistoryItems.stream()
                        .collect(Collectors.groupingBy(DBItemDeploymentHistory::getCommitId));
                dbHistoryItems = groupedDbHistoryItems.entrySet().stream().map(item -> {
                    DepHistoryItemsCount counter = countItems(item.getValue());
                    DBItemDeploymentHistory entry = item.getValue().get(0);
                    entry.setWorkflowCount(counter.getCountWorkflows());
                    entry.setFosCount(counter.getCountFileOrderSources());
                    entry.setJobResourceCount(counter.getCountJobResources());
                    entry.setBoardCount(counter.getCountBoards());
                    entry.setLockCount(counter.getCountLocks());
                    return entry;
                }).filter(item -> canAdd(item.getPath(), permittedFolders))
                        .filter(Objects::nonNull).collect(Collectors.toList());
                if (dbHistoryItems.size() > filter.getCompactFilter().getLimit()) {
                    // reduce list size to limit from filter
                    dbHistoryItems = dbHistoryItems.subList(0, filter.getCompactFilter().getLimit());
                }
                dbHistoryItems.stream().forEachOrdered(item -> {
                    item.setId(null);
                    item.setType(null);
                    item.setPath(null);
                    item.setFolder(null);
                    item.setContent(null);
                    item.setInvContent(null);
                    item.setSignedContent(null);
                    item.setInventoryConfigurationId(null);
                });
            } else {
                dbHistoryItems = dbLayer.getDeploymentHistoryDetails(filter, allowedControllers).stream()
                		.filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toList());
            }
            return JOCDefaultResponse.responseStatus200(getDepHistoryFromDBItems(dbHistoryItems));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
             if (hibernateSession != null) {
                 hibernateSession.close();
             }
        }
    }
    
    private DepHistoryItemsCount countItems (List<DBItemDeploymentHistory> groupedDbHistoryItems) {
        DepHistoryItemsCount counter = new DepHistoryItemsCount();
        counter.setCountWorkflows(groupedDbHistoryItems.stream().filter(item -> ConfigurationType.WORKFLOW.intValue() == item.getType()).count());
        counter.setCountLocks(groupedDbHistoryItems.stream().filter(item -> ConfigurationType.LOCK.intValue() == item.getType()).count());
        counter.setCountFileOrderSources(groupedDbHistoryItems.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.intValue() == item.getType()).count());
        counter.setCountJobResources(groupedDbHistoryItems.stream().filter(item -> ConfigurationType.JOBRESOURCE.intValue() == item.getType()).count());
        counter.setCountBoards(groupedDbHistoryItems.stream().filter(item -> ConfigurationType.NOTICEBOARD.intValue() == item.getType()).count());
        return counter;
    }
    
    private DepHistory getDepHistoryFromDBItems(List<DBItemDeploymentHistory> dbHistoryItems) {
        DepHistory depHistory = new DepHistory();
        depHistory.setDepHistory(dbHistoryItems.stream().map(item -> mapDBItemToDepHistoryItem(item)).collect(Collectors.toList()));
        depHistory.setDeliveryDate(Date.from(Instant.now()));
        return depHistory;
    }

    private DepHistoryItem mapDBItemToDepHistoryItem (DBItemDeploymentHistory dbItem) {
        DepHistoryItem depHistoryItem = new DepHistoryItem();
        depHistoryItem.setAccount(dbItem.getAccount());
        depHistoryItem.setCommitId(dbItem.getCommitId());
        depHistoryItem.setControllerId(dbItem.getControllerId());
        depHistoryItem.setDeleteDate(dbItem.getDeleteDate());
        depHistoryItem.setDeploymentDate(dbItem.getDeploymentDate());
        depHistoryItem.setDeploymentId(dbItem.getId());
        if (dbItem.getType() != null) {
            depHistoryItem.setDeployType(DeployType.fromValue(dbItem.getType()).value());
        }
        depHistoryItem.setFolder(dbItem.getFolder());
        depHistoryItem.setInvConfigurationId(dbItem.getInventoryConfigurationId());
        if(dbItem.getOperation() != null) {
            depHistoryItem.setOperation(OperationType.fromValue(dbItem.getOperation()).name());
        }
        depHistoryItem.setPath(dbItem.getPath());
        if(dbItem.getState() != null) {
            depHistoryItem.setState(DeploymentState.fromValue(dbItem.getState()).name());
        }
        if (dbItem.getErrorMessage() != null && !dbItem.getErrorMessage().isEmpty()) {
            depHistoryItem.setErrorMessage(dbItem.getErrorMessage());
        }
        depHistoryItem.setVersion(dbItem.getVersion());
        if (dbItem.getWorkflowCount() > 0L) {
            depHistoryItem.setWorkflowCount(dbItem.getWorkflowCount());
        }
        if (dbItem.getFosCount() > 0L) {
            depHistoryItem.setFileOrderSourceCount(dbItem.getFosCount());
        }
        if (dbItem.getJobResourceCount() > 0L) {
            depHistoryItem.setJobResourceCount(dbItem.getJobResourceCount());
        }
        if (dbItem.getBoardCount() > 0L) {
            depHistoryItem.setBoardCount(dbItem.getBoardCount());
        }
        if (dbItem.getLockCount() > 0L) {
            depHistoryItem.setLockCount(dbItem.getLockCount());
        }
        return depHistoryItem;
        
    }

}
