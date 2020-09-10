package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.classes.OrderHelper;
import com.sos.webservices.order.resource.IRemoveOrderResource;

@Path("daily_plan")
public class RemoveOrdersImpl extends JOCResourceImpl implements IRemoveOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/removeOrders";

    private void removeOrdersFromPlanAndController(DailyPlanOrderFilter dailyPlanOrderFilter) throws JocConfigurationException,
            DBConnectionRefusedException, JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException,
            DBOpenSessionException, JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException,
            DBInvalidDataException, InterruptedException, ExecutionException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setListOfOrders(dailyPlanOrderFilter.getOrderKeys());
            filter.setControllerId(dailyPlanOrderFilter.getControllerId());
            filter.setDailyPlanDate(dailyPlanOrderFilter.getDailyPlanDate());
            filter.setSubmissionHistoryId(dailyPlanOrderFilter.getSubmissionHistoryId());

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
            OrderHelper orderHelper = new OrderHelper();
            orderHelper.removeFromJobSchedulerController(dailyPlanOrderFilter.getControllerId(), listOfPlannedOrders);
            dbLayerDailyPlannedOrders.delete(filter);

            Globals.commit(sosHibernateSession);
        } finally

        {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRemoveOrders(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        LOGGER.debug("Remove orders from the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(dailyPlanOrderFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            removeOrdersFromPlanAndController(dailyPlanOrderFilter);
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
