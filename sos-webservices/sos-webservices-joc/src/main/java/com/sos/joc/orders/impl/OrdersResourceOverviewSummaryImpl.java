package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
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
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilter.class);
            OrdersFilter ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilter.class);
            
            String controllerId = ordersFilter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                                Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Map<String, Set<Folder>> permittedFoldersMap = null;
            if (controllerId.isEmpty()) {
                if (!folderPermissions.noFolderRestrictionAreSpecified(allowedControllers)) {
                    permittedFoldersMap = folderPermissions.getListOfFolders(allowedControllers);
                }
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            }

            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setControllerIds(allowedControllers);
            if (!controllerId.isEmpty()) {
                historyFilter.setFolders(folderPermissions.getListOfFolders(controllerId));
            }
            historyFilter.setMainOrder(true);

            if (ordersFilter.getDateFrom() != null) {
                historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(JobSchedulerDate.setRelativeDateIntoPast(ordersFilter.getDateFrom()),
                        ordersFilter.getTimeZone()));
            }
            if (ordersFilter.getDateTo() != null) {
                historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(JobSchedulerDate.setRelativeDateIntoPast(ordersFilter.getDateTo()),
                        ordersFilter.getTimeZone()));
            }
            
            OrdersHistoricSummary ordersHistoricSummary = new OrdersHistoricSummary();
            OrdersOverView entity = new OrdersOverView();
            entity.setSurveyDate(Date.from(Instant.now()));
            entity.setOrders(ordersHistoricSummary);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JobHistoryDBLayer jobHistoryDBLayer = new JobHistoryDBLayer(connection, historyFilter);
            ordersHistoricSummary.setFailed(jobHistoryDBLayer.getCountOrders(HistoryStateText.FAILED, permittedFoldersMap));
            ordersHistoricSummary.setSuccessful(jobHistoryDBLayer.getCountOrders(HistoryStateText.SUCCESSFUL, permittedFoldersMap));
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
