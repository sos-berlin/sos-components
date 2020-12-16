package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCPreferences;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrderHelper;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.AddOrderAudit;
import com.sos.joc.classes.audit.DailyPlanAudit;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IDailyPlanDeleteOrderResource;

import io.vavr.control.Either;

@Path("daily_plan")
public class DailyPlanDeleteOrdersImpl extends JOCResourceImpl implements IDailyPlanDeleteOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDeleteOrdersImpl.class);
    private static final String API_CALL_DELETE = "./daily_plan/orders/delete";
    private static final String API_CALL_CANCEL = "./daily_plan/orders/cancel";

    private FilterDailyPlannedOrders getFilter(DailyPlanOrderFilter dailyPlanOrderFilter) {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
        filter.setControllerId(dailyPlanOrderFilter.getControllerId());
        filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate());
        filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getDailyPlanSubmissionHistoryIds());
        filter.setListOfWorkflowPaths(dailyPlanOrderFilter.getFilter().getWorkflowPaths());
        filter.setListOfSchedules(dailyPlanOrderFilter.getFilter().getSchedulePaths());
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
            dbLayerDailyPlannedOrders.delete(filter);
            Globals.commit(sosHibernateSession);

            DailyPlanAudit orderAudit = new DailyPlanAudit(filter.getControllerId(), dailyPlanOrderFilter.getAuditLog());
            logAuditMessage(orderAudit);
            storeAuditLogEntry(orderAudit);

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void cancelOrdersFromController(DailyPlanOrderFilter dailyPlanOrderFilter) throws JocConfigurationException, DBConnectionRefusedException,
            JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException, DBOpenSessionException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, DBInvalidDataException,
            InterruptedException, ExecutionException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CANCEL);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = getFilter(dailyPlanOrderFilter);
            filter.addState(OrderStateText.PENDING);
            List<DBItemDailyPlanWithHistory> listOfPlannedOrdersWithHistory = dbLayerDailyPlannedOrders.getDailyPlanWithHistoryList(filter, 0);

            try {
                OrderHelper.removeFromJobSchedulerControllerWithHistory(dailyPlanOrderFilter.getControllerId(), listOfPlannedOrdersWithHistory);
                filter.setSubmitted(false);
                dbLayerDailyPlannedOrders.setSubmitted(filter);
                
                DailyPlanAudit orderAudit = new DailyPlanAudit(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getAuditLog());
                logAuditMessage(orderAudit);
                storeAuditLogEntry(orderAudit);
            } catch (JobSchedulerObjectNotExistException e) {
                LOGGER.warn("Order unknown in JS7 Controller");
            }
            

            Globals.commit(sosHibernateSession);
        } finally

        {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postDeleteOrders(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        LOGGER.debug("Delete orders from the daily plan");
        try {
            
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_DELETE, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(getControllerId(xAccessToken,dailyPlanOrderFilter.getControllerId()), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());
            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());

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

    @Override
    public JOCDefaultResponse postCancelOrders(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        LOGGER.debug("cancel orders from controller");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_CANCEL, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(dailyPlanOrderFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            cancelOrdersFromController(dailyPlanOrderFilter);
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
