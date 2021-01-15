package com.sos.joc.orders.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
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

    // tmp , to remove ..
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceHistoryImpl.class);

    @Override
    public JOCDefaultResponse postOrdersHistory(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, OrdersFilter.class);
            OrdersFilter in = Globals.objectMapper.readValue(inBytes, OrdersFilter.class);
            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermissonsJocCockpit(in.getControllerId(), accessToken)
                    .getHistory().getView().isStatus());
            if (response != null) {
                return response;
            }

            List<OrderHistoryItem> history = new ArrayList<OrderHistoryItem>();
            boolean withFolderFilter = in.getFolders() != null && !in.getFolders().isEmpty();
            boolean hasPermission = true;
            Set<Folder> folders = addPermittedFolder(in.getFolders());

            HistoryFilter dbFilter = new HistoryFilter();
            dbFilter.setSchedulerId(in.getControllerId());
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
                            .collect(Collectors.groupingBy(order -> normalizePath(order.getWorkflowPath()), Collectors.mapping(OrderPath::getOrderId,
                                    Collectors.toSet()))));
                    in.setRegex("");
                } else {

                    if (SearchStringHelper.isDBWildcardSearch(in.getRegex())) {
                        dbFilter.setWorkflows(Arrays.asList(in.getRegex().split(",")));
                        in.setRegex("");
                    }

                    if (in.getExcludeOrders() != null && !in.getExcludeOrders().isEmpty()) {
                        dbFilter.setExcludedOrders(in.getExcludeOrders().stream().collect(Collectors.groupingBy(order -> normalizePath(order
                                .getWorkflowPath()), Collectors.mapping(OrderPath::getOrderId, Collectors.toSet()))));
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

                // TODO to remove
                if (dbFilter.getExecutedFrom() == null) {
                    // 1970-01-01 01:00:00 + 1 day
                    dbFilter.setExecutedFrom(SOSDate.add(Instant.ofEpochMilli(new Date(0).getTime()), 1, ChronoUnit.DAYS));
                }

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);
                ScrollableResults sr = null;
                try {
                    Matcher matcher = null;
                    if (in.getRegex() != null && !in.getRegex().isEmpty()) {
                        matcher = Pattern.compile(in.getRegex()).matcher("");
                    }
                    sr = dbLayer.getMainOrders();

                    // tmp outputs to remove...
                    int i = 0;
                    int logStep = 1_000;
                    String range = "order";
                    LOGGER.info(String.format("[%s]start read and map ..", range));
                    while (sr.next()) {
                        i++;

                        DBItemHistoryOrder item = (DBItemHistoryOrder) sr.get(0);
                        if (i == 1) {
                            LOGGER.info(String.format(" [%s][%s]first entry retrieved", range, i));
                        }

                        if (in.getControllerId().isEmpty() && !getPermissonsJocCockpit(item.getControllerId(), accessToken).getHistory().getView()
                                .isStatus()) {
                            continue;
                        }
                        if (matcher != null && !matcher.reset(item.getWorkflowPath() + "," + item.getOrderId()).find()) {
                            continue;
                        }
                        history.add(HistoryMapper.map2OrderHistoryItem(item));

                        if (i == 1 || i % logStep == 0) {
                            LOGGER.info(String.format(" [%s][%s]entries processed", range, i));
                        }
                    }
                    LOGGER.info(String.format("[%s][%s]end read and map", range, i));
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
            return JOCDefaultResponse.responseStatus200(answer);
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
