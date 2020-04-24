package com.sos.joc.orders.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.orders.resource.IOrdersResourceHistory;

@Path("orders")
public class OrdersResourceHistoryImpl extends JOCResourceImpl implements IOrdersResourceHistory {

    private static final String API_CALL = "./orders/history";

    @Override
    public JOCDefaultResponse postOrdersHistory(String accessToken, OrdersFilter ordersFilter) throws Exception {
        SOSHibernateSession connection = null;
        try {
            if (ordersFilter.getJobschedulerId() == null) {
                ordersFilter.setJobschedulerId("");
            }
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, accessToken, ordersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), accessToken).getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);

            List<OrderHistoryItem> listHistory = new ArrayList<OrderHistoryItem>();
            Map<Long, List<OrderHistoryItem>> historyChildren = new HashMap<Long, List<OrderHistoryItem>>();
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            List<Folder> folders = addPermittedFolder(ordersFilter.getFolders());

            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setSchedulerId(ordersFilter.getJobschedulerId());
            if (ordersFilter.getHistoryIds() != null && !ordersFilter.getHistoryIds().isEmpty()) {
                historyFilter.setHistoryIds(ordersFilter.getHistoryIds());
            } else {
                if (ordersFilter.getDateFrom() != null) {
                    historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(ordersFilter.getDateFrom(), ordersFilter.getTimeZone()));
                }
                if (ordersFilter.getDateTo() != null) {
                    historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(ordersFilter.getDateTo(), ordersFilter.getTimeZone()));
                }

                if (!ordersFilter.getHistoryStates().isEmpty()) {
                    historyFilter.setState(ordersFilter.getHistoryStates());
                }

                if (!ordersFilter.getOrders().isEmpty()) {
                    final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                    historyFilter.setOrders(ordersFilter.getOrders().stream().filter(order -> order != null && canAdd(order.getWorkflow(),
                            permittedFolders)).collect(Collectors.groupingBy(order -> normalizePath(order.getWorkflow()), Collectors.mapping(
                                    OrderPath::getOrderId, Collectors.toSet()))));
                    ordersFilter.setRegex("");
                } else {

                    if (SearchStringHelper.isDBWildcardSearch(ordersFilter.getRegex())) {
                        String[] workflows = ordersFilter.getRegex().split(",");
                        for (String workflow : workflows) {
                            historyFilter.addWorkflow(workflow);
                        }
                        ordersFilter.setRegex("");
                    }

                    if (!ordersFilter.getExcludeOrders().isEmpty()) {
                        historyFilter.setExcludedOrders(ordersFilter.getExcludeOrders().stream().collect(Collectors.groupingBy(order -> normalizePath(
                                order.getWorkflow()), Collectors.mapping(OrderPath::getOrderId, Collectors.toSet()))));
                    }

                    if (withFolderFilter && (folders == null || folders.isEmpty())) {
                        hasPermission = false;
                    } else if (folders != null && !folders.isEmpty()) {
                        historyFilter.setFolders(folders.stream().map(folder -> {
                            folder.setFolder(normalizeFolder(folder.getFolder()));
                            return folder;
                        }).collect(Collectors.toSet()));
                    }
                }
            }

            if (hasPermission) {

                if (ordersFilter.getLimit() == null) {
                    ordersFilter.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }
                historyFilter.setLimit(ordersFilter.getLimit());

                JobHistoryDBLayer jobHistoryDbLayer = new JobHistoryDBLayer(connection, historyFilter);
                List<DBItemOrder> dbOrderItems = jobHistoryDbLayer.getOrderHistoryFromTo();

                Matcher regExMatcher = null;
                if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                    regExMatcher = Pattern.compile(ordersFilter.getRegex()).matcher("");
                }

                if (dbOrderItems != null) {
                    for (DBItemOrder dbItemOrder : dbOrderItems) {

                        OrderHistoryItem history = new OrderHistoryItem();
                        if (ordersFilter.getJobschedulerId().isEmpty()) {
                            history.setJobschedulerId(dbItemOrder.getJobSchedulerId());
                            if (!getPermissonsJocCockpit(dbItemOrder.getJobSchedulerId(), accessToken).getHistory().getView().isStatus()) {
                                continue;
                            }
                        }

                        history.setEndTime(dbItemOrder.getEndTime());
                        history.setHistoryId(dbItemOrder.getId());
                        history.setOrderId(dbItemOrder.getName());
                        history.setStartTime(dbItemOrder.getStartTime());
                        history.setState(setState(dbItemOrder));
                        history.setSurveyDate(dbItemOrder.getModified());
                        history.setWorkflow(dbItemOrder.getWorkflowPath());

                        history.setChildren(historyChildren.remove(history.getHistoryId()));
                        if (dbItemOrder.getParentId() != 0L) {
                            historyChildren.putIfAbsent(dbItemOrder.getParentId(), new ArrayList<OrderHistoryItem>());
                            historyChildren.get(dbItemOrder.getParentId()).add(history);
                        } else {
                            if (regExMatcher != null && !regExMatcher.reset(dbItemOrder.getWorkflowPath() + "/" + dbItemOrder.getName()).find()) {
                                continue;
                            }
                            listHistory.add(history);
                        }
                    }
                }
            }

            OrderHistory entity = new OrderHistory();
            entity.setDeliveryDate(new Date());
            entity.setHistory(listHistory);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private HistoryState setState(DBItemOrder dbItemOrder) {
        HistoryState state = new HistoryState();
        if (dbItemOrder.isSuccessFul()) {
            state.setSeverity(0);
            state.set_text(HistoryStateText.SUCCESSFUL);
        } else if (dbItemOrder.isInComplete()) {
            state.setSeverity(1);
            state.set_text(HistoryStateText.INCOMPLETE);
        } else if (dbItemOrder.isFailed()) {
            state.setSeverity(2);
            state.set_text(HistoryStateText.FAILED);
        }
        return state;
    }

}
