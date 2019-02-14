package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;
import com.sos.webservices.order.classes.OrderHelper;
import com.sos.webservices.order.resource.IRemoveOrderResource;

@Path("orders")
public class RemoveOrdersImpl extends JOCResourceImpl implements IRemoveOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrdersImpl.class);
    private static final String API_CALL = "./orders/removeOrders";

    private void removeOrders(OrdersFilter ordersFilter) throws JocConfigurationException, DBConnectionRefusedException,
            JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException {
        SOSHibernateSession sosHibernateSession = null;

        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

        try {

            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            if (ordersFilter.getOrders().size() > 0) {
                FilterDailyPlan filter = new FilterDailyPlan();
                filter.setListOfOrders(ordersFilter.getOrders());
                filter.setMasterId(ordersFilter.getJobschedulerId());
                List<DBItemDailyPlan> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanList(filter, 0);
                OrderHelper orderHelper = new OrderHelper();
                String answer = orderHelper.removeFromJobSchedulerMaster(ordersFilter.getJobschedulerId(), listOfPlannedOrders);
                dbLayerDailyPlan.delete(filter);
            }
            if (ordersFilter.getDateFrom() != null && ordersFilter.getDateTo() != null) {

                Date fromDate = null;
                Date toDate = null;

                FilterDailyPlan filter = new FilterDailyPlan();
                filter.setMasterId(ordersFilter.getJobschedulerId());
                fromDate = JobSchedulerDate.getDateFrom(ordersFilter.getDateFrom(), ordersFilter.getTimeZone());
                filter.setPlannedStartFrom(fromDate);
                toDate = JobSchedulerDate.getDateTo(ordersFilter.getDateTo(), ordersFilter.getTimeZone());
                filter.setPlannedStartTo(toDate);
                List<DBItemDailyPlan> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanList(filter, 0);
                OrderHelper orderHelper = new OrderHelper();
                String answer = orderHelper.removeFromJobSchedulerMaster(ordersFilter.getJobschedulerId(), listOfPlannedOrders);
                dbLayerDailyPlan.delete(filter);
            }

            // TODO: Check answers for error

            Globals.commit(sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRemoveOrders(String xAccessToken, OrdersFilter ordersFilter) throws JocException {
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilter, xAccessToken, ordersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(ordersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            removeOrders(ordersFilter);
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
