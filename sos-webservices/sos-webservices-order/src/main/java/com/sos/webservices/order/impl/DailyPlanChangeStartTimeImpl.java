package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyChangeStartTime;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.dailyplan.PlannedOrders;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IDailyPlanChangeStartTimeResource;
import com.sos.webservices.order.resource.IDailyPlanSubmitOrderResource;

@Path("daily_plan")
public class DailyPlanChangeStartTimeImpl extends JOCResourceImpl implements IDailyPlanChangeStartTimeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanChangeStartTimeImpl.class);
    private static final String API_CALL_STARTTIME = "./daily_plan/orders/starttime";

    @Override
    public JOCDefaultResponse postChangeStartime(String xAccessToken, DailyChangeStartTime dailyChangeStartTime) throws JocException {
        LOGGER.debug("Change start time for orders from the daily plan");
        SOSHibernateSession sosHibernateSession = null;

        try {

            JOCDefaultResponse jocDefaultResponse = init(API_CALL_STARTTIME, dailyChangeStartTime, xAccessToken, dailyChangeStartTime
                    .getControllerId(), getPermissonsJocCockpit(getControllerId(xAccessToken, dailyChangeStartTime.getControllerId()), xAccessToken)
                            .getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("orderIds", dailyChangeStartTime.getOrderIds());
            this.checkRequiredParameter("startTime", dailyChangeStartTime.getStartTime());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_STARTTIME);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(dailyChangeStartTime.getControllerId());

            for (String orderId : dailyChangeStartTime.getOrderIds()) {
                filter.setOrderId(orderId);
                changeStartTime(sosHibernateSession, dbLayerDailyPlannedOrders, filter, dailyChangeStartTime.getStartTime());
            }
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    private void changeStartTime(SOSHibernateSession sosHibernateSession, DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders,
            FilterDailyPlannedOrders filter, Date startTime) throws SOSHibernateException {

        List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
        if (listOfPlannedOrders.size() == 1) {
            DBItemDailyPlanOrders dbItemDailyPlanOrders = listOfPlannedOrders.get(0);
            String orderId = dbItemDailyPlanOrders.getOrderId();

            dbItemDailyPlanOrders.setModified(new Date());
            dbItemDailyPlanOrders.setOrderId("orderId");
            dbItemDailyPlanOrders.setPlannedStart(new Date());
            dbItemDailyPlanOrders.setSubmitted(false);
            sosHibernateSession.update(dbItemDailyPlanOrders);
            // wenn submitted --> cancel order
            // --> submit new Order, t yyyy-mm-dd#Pnnnnnnnnnn-name
        }

    }

}
