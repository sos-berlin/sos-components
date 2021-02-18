package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DailyPlanAudit;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanDeleteOrderResource;

@Path("daily_plan")
public class DailyPlanDeleteOrdersImpl extends JOCResourceImpl implements IDailyPlanDeleteOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDeleteOrdersImpl.class);
    private static final String API_CALL_DELETE = "./daily_plan/orders/delete";

    private void addCyclicOrderIds(List<String> orderIds, String orderId, DailyPlanOrderFilter dailyPlanOrderFilter) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(dailyPlanOrderFilter.getControllerId());
            filter.setOrderId(orderId);

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

            if (listOfPlannedOrders.size() == 1) {
                DBItemDailyPlanOrders dbItemDailyPlanOrder = listOfPlannedOrders.get(0);
                if (dbItemDailyPlanOrder.getStartMode() == 1) {

                    FilterDailyPlannedOrders filterCyclic = new FilterDailyPlannedOrders();
                    filterCyclic.setControllerId(dailyPlanOrderFilter.getControllerId());
                    filterCyclic.setRepeatInterval(dbItemDailyPlanOrder.getRepeatInterval());
                    filterCyclic.setPeriodBegin(dbItemDailyPlanOrder.getPeriodBegin());
                    filterCyclic.setPeriodEnd(dbItemDailyPlanOrder.getPeriodEnd());
                    filterCyclic.setWorkflowName(dbItemDailyPlanOrder.getWorkflowName());
                    filterCyclic.setScheduleName(dbItemDailyPlanOrder.getScheduleName());
                    filterCyclic.setDailyPlanDate(dbItemDailyPlanOrder.getDailyPlanDate());

                    List<DBItemDailyPlanOrders> listOfPlannedCyclicOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filterCyclic, 0);
                    for (DBItemDailyPlanOrders dbItemDailyPlanOrders : listOfPlannedCyclicOrders) {
                        if (!dbItemDailyPlanOrders.getOrderId().equals(orderId)) {
                            orderIds.add(dbItemDailyPlanOrders.getOrderId());
                        }
                    }
                }

            } else {
                LOGGER.warn("Expected one record for order-id " + filter.getOrderId());
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private FilterDailyPlannedOrders getFilter(DailyPlanOrderFilter dailyPlanOrderFilter) throws SOSHibernateException {

        List<String> orderIds = new ArrayList<String>();
        if (dailyPlanOrderFilter.getFilter().getOrderIds() != null) {
            orderIds.addAll(dailyPlanOrderFilter.getFilter().getOrderIds());

            for (String orderId : orderIds) {
                addCyclicOrderIds(dailyPlanOrderFilter.getFilter().getOrderIds(), orderId, dailyPlanOrderFilter);
            }
        }
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
        filter.setControllerId(dailyPlanOrderFilter.getControllerId());
        filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate());
        filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getSubmissionHistoryIds());

        if (dailyPlanOrderFilter.getFilter().getSchedulePaths() != null) {
            if (dailyPlanOrderFilter.getFilter().getScheduleNames() == null) {
                dailyPlanOrderFilter.getFilter().setScheduleNames(new ArrayList<String>());
            }
            for (String path : dailyPlanOrderFilter.getFilter().getSchedulePaths()) {
                String name = Paths.get(path).getFileName().toString();
                dailyPlanOrderFilter.getFilter().getScheduleNames().add(name);
            }
        }
        if (dailyPlanOrderFilter.getFilter().getWorkflowPaths() != null) {
            if (dailyPlanOrderFilter.getFilter().getWorkflowNames() == null) {
                dailyPlanOrderFilter.getFilter().setWorkflowNames(new ArrayList<String>());
            }

            for (String path : dailyPlanOrderFilter.getFilter().getWorkflowPaths()) {
                String name = Paths.get(path).getFileName().toString();
                dailyPlanOrderFilter.getFilter().getWorkflowNames().add(name);
            }
        }

        filter.setListOfWorkflowNames(dailyPlanOrderFilter.getFilter().getWorkflowNames());
        filter.setListOfScheduleNames(dailyPlanOrderFilter.getFilter().getScheduleNames());

        return filter;
    }

    private void deleteOrdersFromPlan(DailyPlanOrderFilter dailyPlanOrderFilter) throws JocConfigurationException, DBConnectionRefusedException,
            JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException, DBOpenSessionException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, DBInvalidDataException,
            InterruptedException, ExecutionException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = getFilter(dailyPlanOrderFilter);
            filter.addState(OrderStateText.PLANNED);
            dbLayerDailyPlannedOrders.deleteCascading(filter);
            Globals.commit(sosHibernateSession);

            DailyPlanAudit orderAudit = new DailyPlanAudit(filter.getControllerId(), dailyPlanOrderFilter.getAuditLog());
            logAuditMessage(orderAudit);
            storeAuditLogEntry(orderAudit);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postDeleteOrders(String accessToken, byte[] filterBytes) {

        LOGGER.debug("Delete orders from the daily plan");
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL_DELETE, dailyPlanOrderFilter, accessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(getControllerId(accessToken, dailyPlanOrderFilter.getControllerId()), accessToken).getDailyPlan()
                            .getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            deleteOrdersFromPlan(dailyPlanOrderFilter);
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
