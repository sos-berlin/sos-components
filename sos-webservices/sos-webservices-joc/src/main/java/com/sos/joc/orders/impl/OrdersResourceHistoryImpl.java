package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.orders.resource.IOrdersResourceHistory;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.ORDERS)
public class OrdersResourceHistoryImpl extends JOCResourceImpl implements IOrdersResourceHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceHistoryImpl.class);

    @Override
    public JOCDefaultResponse postOrdersHistory(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, OrdersFilter.class);
            OrdersFilter in = Globals.objectMapper.readValue(inBytes, OrdersFilter.class);

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }

            JOCDefaultResponse response = initPermissions(controllerId, permitted);
            if (response != null) {
                return response;
            }

            List<OrderHistoryItem> history = new ArrayList<OrderHistoryItem>();
            boolean withFolderFilter = in.getFolders() != null && !in.getFolders().isEmpty();
            boolean hasPermission = true;
            Set<Folder> permittedFolders = addPermittedFolder(in.getFolders());
            boolean folderPermissionsAreChecked = false;

            HistoryFilter dbFilter = new HistoryFilter();
            dbFilter.setControllerIds(allowedControllers);
            if (in.getHistoryIds() != null && !in.getHistoryIds().isEmpty()) {
                dbFilter.setHistoryIds(in.getHistoryIds());
            } else {
                if (in.getDateFrom() != null) {
                    dbFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(JobSchedulerDate.setRelativeDateIntoPast(in.getDateFrom()), in.getTimeZone()));
                }
                if (in.getDateTo() != null) {
                    dbFilter.setExecutedTo(JobSchedulerDate.getDateTo(JobSchedulerDate.setRelativeDateIntoPast(in.getDateTo()), in.getTimeZone()));
                }
                if (in.getCompletedDateFrom() != null) {
                    dbFilter.setEndFrom(JobSchedulerDate.getDateFrom(JobSchedulerDate.setRelativeDateIntoPast(in.getCompletedDateFrom()), in.getTimeZone()));
                }
                if (in.getCompletedDateTo() != null) {
                    dbFilter.setEndTo(JobSchedulerDate.getDateTo(JobSchedulerDate.setRelativeDateIntoPast(in.getCompletedDateTo()), in.getTimeZone()));
                }

                if (in.getHistoryStates() != null && !in.getHistoryStates().isEmpty()) {
                    dbFilter.setState(in.getHistoryStates());
                }

                if (in.getOrders() != null && !in.getOrders().isEmpty()) {
                    // TODO consider workflowId in groupingby???
                    dbFilter.setOrders(in.getOrders().stream().filter(Objects::nonNull).filter(order -> canAdd(WorkflowPaths.getPath(order
                            .getWorkflowPath()), permittedFolders)).peek(order -> order.setWorkflowPath(JocInventory.pathToName(order
                                    .getWorkflowPath()))).collect(Collectors.groupingBy(OrderPath::getWorkflowPath, Collectors.mapping(
                                            OrderPath::getOrderId, Collectors.toSet()))));
                    folderPermissionsAreChecked = true;
                } else {

                    dbFilter.setExcludedWorkflows(in.getExcludeWorkflows());

                    if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
                        hasPermission = false;
                    } else if (withFolderFilter && permittedFolders != null && !permittedFolders.isEmpty()) {
                        dbFilter.setFolders(in.getFolders().stream().filter(folder -> folderIsPermitted(folder.getFolder(), permittedFolders))
                                .collect(Collectors.toSet()));
                        folderPermissionsAreChecked = true;
                    }

                    // TODO consider these parameter in DB
                    dbFilter.setOrderId(in.getOrderId());
                    dbFilter.setWorkflowPath(in.getWorkflowPath());
                    dbFilter.setWorkflowName(in.getWorkflowName());
                }
            }

            if (hasPermission) {
                if (in.getLimit() == null) {
                    in.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }
                dbFilter.setLimit(in.getLimit());

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);
                ScrollableResults sr = null;

                try {
                    boolean profiler = false;
                    Instant profilerStart = Instant.now();
                    sr = dbLayer.getMainOrders();
                    Instant profilerAfterSelect = Instant.now();
                    Instant profilerFirstEntry = null;

                    int i = 0;
                    Map<String, Boolean> checkedFolders = new HashMap<>();
                    while (sr.next()) {
                        i++;

                        DBItemHistoryOrder item = (DBItemHistoryOrder) sr.get(0);
                        if (profiler && i == 1) {
                            profilerFirstEntry = Instant.now();
                        }
                        if (!folderPermissionsAreChecked && !canAdd(item, permittedFolders, checkedFolders)) {
                            continue;
                        }
                        history.add(HistoryMapper.map2OrderHistoryItem(item));
                    }
                    logProfiler(profiler, i, profilerStart, profilerAfterSelect, profilerFirstEntry);
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (sr != null) {
                        sr.close();
                    }
                }
            }

            OrderHistory answer = new OrderHistory();
            answer.setDeliveryDate(new Date());
            answer.setHistory(history);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private boolean canAdd(DBItemHistoryOrder item, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        Boolean result = checkedFolders.get(item.getWorkflowFolder());
        if (result == null) {
            result = canAdd(item.getWorkflowPath(), permittedFolders);
            checkedFolders.put(item.getWorkflowFolder(), result);
        }
        return result;
    }

    private void logProfiler(boolean profiler, int i, Instant start, Instant afterSelect, Instant firstEntry) {
        if (!profiler) {
            return;
        }
        Instant end = Instant.now();
        String firstEntryDuration = "0s";
        if (firstEntry != null) {
            firstEntryDuration = SOSDate.getDuration(start, firstEntry);
        }
        LOGGER.info(String.format("[order][history][%s][total=%s][select=%s, first entry=%s]", i, SOSDate.getDuration(start, end), SOSDate
                .getDuration(start, afterSelect), firstEntryDuration));
    }
}
