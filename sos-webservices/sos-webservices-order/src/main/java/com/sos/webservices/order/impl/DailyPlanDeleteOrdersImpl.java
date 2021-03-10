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
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanDeleteOrderResource;

@Path("daily_plan")
public class DailyPlanDeleteOrdersImpl extends JOCResourceImpl implements IDailyPlanDeleteOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDeleteOrdersImpl.class);
    private static final String API_CALL_DELETE = "./daily_plan/orders/delete";
    private OrderInitiatorSettings settings;

    @Override
    public JOCDefaultResponse postDeleteOrders(String accessToken, byte[] filterBytes) {

        LOGGER.debug("Delete orders from the daily plan");
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(dailyPlanOrderFilter.getControllerId(), getPermissonsJocCockpit(
                    dailyPlanOrderFilter.getControllerId(), accessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            setSettings();
            deleteOrdersFromPlan(dailyPlanOrderFilter);
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

    private FilterDailyPlannedOrders getFilter(SOSHibernateSession sosHibernateSession, DailyPlanOrderFilter dailyPlanOrderFilter)
            throws SOSHibernateException {

        DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

        List<String> orderIds = new ArrayList<String>();
        if (dailyPlanOrderFilter.getFilter().getOrderIds() != null) {
            orderIds.addAll(dailyPlanOrderFilter.getFilter().getOrderIds());

            for (String orderId : orderIds) {
                dbLayerDailyPlannedOrders.addCyclicOrderIds(dailyPlanOrderFilter.getFilter().getOrderIds(), orderId, dailyPlanOrderFilter
                        .getControllerId(), settings.getTimeZone(), settings.getPeriodBegin());
            }
        }
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
        filter.setControllerId(dailyPlanOrderFilter.getControllerId());
        filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
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

            FilterDailyPlannedOrders filter = getFilter(sosHibernateSession, dailyPlanOrderFilter);
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

    private void setSettings() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            GlobalSettingsReader reader = new GlobalSettingsReader();
            this.settings = reader.getSettings(session);
        } finally {
            Globals.disconnect(session);
        }
    }

}
