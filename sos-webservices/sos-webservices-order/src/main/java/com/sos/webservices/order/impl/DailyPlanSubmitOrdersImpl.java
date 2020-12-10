package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IDailyPlanSubmitOrderResource;

@Path("daily_plan")
public class DailyPlanSubmitOrdersImpl extends JOCResourceImpl implements IDailyPlanSubmitOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmitOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/submit_orders";

    private void submitOrdersToController(DailyPlanOrderFilter dailyPlanOrderFilter) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, IOException, ParseException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
        orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
        orderInitiatorSettings.setControllerId(dailyPlanOrderFilter.getControllerId());
        orderInitiatorSettings.setOverwrite(dailyPlanOrderFilter.getOverwrite());
        orderInitiatorSettings.setSubmit(dailyPlanOrderFilter.getWithSubmit());

        orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone", Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
        orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin", Globals.DEFAULT_PERIOD_DAILY_PLAN));
        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setListOfOrders(dailyPlanOrderFilter.getOrderIds());
            filter.setSubmitted(false);
            filter.setControllerId(dailyPlanOrderFilter.getControllerId());
            filter.setDailyPlanDate(dailyPlanOrderFilter.getDailyPlanDate());
            filter.setListOfSchedules(dailyPlanOrderFilter.getSchedulePaths());
           

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
            Globals.commit(sosHibernateSession);
            orderInitiatorRunner.submitOrders(listOfPlannedOrders);
        } finally

        {
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public JOCDefaultResponse postSubmitOrders(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        LOGGER.debug("Submit orders to JS7 controller");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(dailyPlanOrderFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            submitOrdersToController(dailyPlanOrderFilter);
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
