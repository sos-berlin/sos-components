package com.sos.joc.orders.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.classes.proxy.Proxies;
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
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                                Collectors.toSet());
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
            Set<Folder> folders = addPermittedFolder(in.getFolders());

            HistoryFilter dbFilter = new HistoryFilter();
            dbFilter.setControllerIds(allowedControllers);
            if (in.getHistoryIds() != null && !in.getHistoryIds().isEmpty()) {
                dbFilter.setHistoryIds(in.getHistoryIds());
            } else {
                if (in.getDateFrom() != null) {
                    dbFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()));
                }
                if (in.getDateTo() != null) {
                    dbFilter.setExecutedTo(JobSchedulerDate.getDateTo(in.getDateTo(), in.getTimeZone()));
                }

                if (in.getHistoryStates() != null && !in.getHistoryStates().isEmpty()) {
                    dbFilter.setState(in.getHistoryStates());
                }

                if (in.getOrders() != null && !in.getOrders().isEmpty()) {
                    final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                    // TODO consider workflowId in groupingby???
                    dbFilter.setOrders(in.getOrders().stream().filter(order -> order != null && canAdd(order.getWorkflowPath(), permittedFolders))
                            .collect(Collectors.groupingBy(order -> order.getWorkflowPath(), Collectors.mapping(OrderPath::getOrderId, Collectors
                                    .toSet()))));
                    in.setRegex("");
                } else {

                    if (SearchStringHelper.isDBWildcardSearch(in.getRegex())) {
                        dbFilter.setWorkflows(Arrays.asList(in.getRegex().split(",")));
                        in.setRegex("");
                    }

                    if (in.getExcludeOrders() != null && !in.getExcludeOrders().isEmpty()) {
                        dbFilter.setExcludedOrders(in.getExcludeOrders().stream().collect(Collectors.groupingBy(order -> order.getWorkflowPath(),
                                Collectors.mapping(OrderPath::getOrderId, Collectors.toSet()))));
                    }

                    if (withFolderFilter && (folders == null || folders.isEmpty())) {
                        hasPermission = false;
                    } else if (folders != null && !folders.isEmpty()) {
                        dbFilter.setFolders(folders.stream().map(folder -> {
                            folder.setFolder(normalizeFolder(folder.getFolder()));
                            return folder;
                        }).collect(Collectors.toSet()));
                    }
                }
            }

            if (hasPermission) {
                if (in.getLimit() == null) {
                    in.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }
                dbFilter.setLimit(in.getLimit());

                if (dbFilter.getExecutedFrom() == null) {
                    dbFilter.setExecutedFrom(WebserviceConstants.HISTORY_DEFAULT_EXECUTED_FROM);
                }

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);
                ScrollableResults sr = null;
                try {
                    Predicate<String> predicate = null;
                    if (in.getRegex() != null && !in.getRegex().isEmpty()) {
                        predicate = Pattern.compile(in.getRegex()).asPredicate();
                    }
                    sr = dbLayer.getMainOrders();

                    // tmp outputs to check performance ...
                    // int i = 0;
                    // int logStep = 1_000;
                    // String range = "order";
                    // LOGGER.info(String.format("[%s]start read and map ..", range));
                    while (sr.next()) {
                        // i++;

                        DBItemHistoryOrder item = (DBItemHistoryOrder) sr.get(0);
                        // if (i == 1) {
                        // LOGGER.info(String.format(" [%s][%s]first entry retrieved", range, i));
                        // }

                        if (predicate != null && !predicate.test(item.getWorkflowPath() + "," + item.getOrderId())) {
                            continue;
                        }
                        history.add(HistoryMapper.map2OrderHistoryItem(item));

                        // if (i == 1 || i % logStep == 0) {
                        // LOGGER.info(String.format(" [%s][%s]entries processed", range, i));
                        // }
                    }
                    // LOGGER.info(String.format("[%s][%s]end read and map", range, i));
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
}
