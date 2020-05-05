package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderPath;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.joc.model.order.OrdersHistoricSummary;
import com.sos.joc.model.order.OrdersOverView;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSummary;
import com.sos.schema.JsonValidator;

@Path("orders")
public class OrdersResourceOverviewSummaryImpl extends JOCResourceImpl implements IOrdersResourceOverviewSummary {

    private static final String API_CALL = "./orders/overview/summary";

    @Override
    public JOCDefaultResponse postOrdersOverviewSummary(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            JsonValidator.validateFailFast(filterBytes, OrdersFilter.class);
            OrdersFilter ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilter.class);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, accessToken, ordersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            List<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            
            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setSchedulerId(ordersFilter.getJobschedulerId());
            historyFilter.setMainOrder(true);
            
            if (ordersFilter.getDateFrom() != null) {
                historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(ordersFilter.getDateFrom(), ordersFilter.getTimeZone()));
            }
            if (ordersFilter.getDateTo() != null) {
                historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(ordersFilter.getDateTo(), ordersFilter.getTimeZone()));
            }
            
            if (ordersFilter.getOrders() != null && !ordersFilter.getOrders().isEmpty()) {
                final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                historyFilter.setOrders(ordersFilter.getOrders().stream().filter(order -> order != null && canAdd(order.getWorkflow(),
                        permittedFolders)).collect(Collectors.groupingBy(order -> normalizePath(order.getWorkflow()), Collectors.mapping(
                                OrderPath::getOrderId, Collectors.toSet()))));
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                historyFilter.setFolders(folders.stream().map(folder -> {
                    folder.setFolder(normalizeFolder(folder.getFolder()));
                    return folder;
                }).collect(Collectors.toSet()));
            }
            
            OrdersHistoricSummary ordersHistoricSummary = new OrdersHistoricSummary();
            OrdersOverView entity = new OrdersOverView();
            entity.setSurveyDate(Date.from(Instant.now()));
            entity.setOrders(ordersHistoricSummary);
            if (hasPermission) {
                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                JobHistoryDBLayer jobHistoryDBLayer = new JobHistoryDBLayer(connection, historyFilter);
                ordersHistoricSummary.setFailed(jobHistoryDBLayer.getCountOrders(HistoryStateText.FAILED));
                ordersHistoricSummary.setSuccessful(jobHistoryDBLayer.getCountOrders(HistoryStateText.SUCCESSFUL));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));

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

}
