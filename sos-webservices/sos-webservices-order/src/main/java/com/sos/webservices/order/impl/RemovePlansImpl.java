package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.time.Year;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlannedOrders;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlannedOrdersFilter;
import com.sos.webservices.order.classes.OrderHelper;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;
import com.sos.webservices.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IRemovePlansResource;

@Path("orders")
public class RemovePlansImpl extends JOCResourceImpl implements IRemovePlansResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemovePlansImpl.class);
    private static final String API_CALL = "./orders/removeOrders";

    private int getDayFrom(int year, int fromYear, int fromDayOfYear, int toYear) {
        int day = fromDayOfYear;
        if (year == toYear || (year > fromYear && year < toYear)) {
            day = 1;
        }
        return day;
    }

    private int getDayTo(int year, int fromYear, int toDayOfYear, int toYear) {
        Year thisYear = Year.of(fromYear);
        int countDaysInYear = thisYear.length();
        int day = toDayOfYear;
        if (year < toYear) {
            day = countDaysInYear;
        }
        return day;
    }

    private void removeOrdersFromPlan(PlannedOrdersFilter plannedOrdersFilter) throws JocConfigurationException, DBConnectionRefusedException,
			JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException,
			DBOpenSessionException {
        SOSHibernateSession sosHibernateSession = null;
        // TODO: Not use PlanFilter. A new Filter with MasterId, From, To must be used

        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

        try {
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            Date fromDate = null;
            Date toDate = null;

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setJobSchedulerId(plannedOrdersFilter.getJobschedulerId());
            fromDate = JobSchedulerDate.getDateFrom(plannedOrdersFilter.getDateFrom(), plannedOrdersFilter.getTimeZone());
            filter.setPlannedStartFrom(fromDate);
            toDate = JobSchedulerDate.getDateTo(plannedOrdersFilter.getDateTo(), plannedOrdersFilter.getTimeZone());
            filter.setPlannedStartTo(toDate);
            List<DBItemDailyPlannedOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
            OrderHelper orderHelper = new OrderHelper();
            String answer = orderHelper.removeFromJobSchedulerMaster(plannedOrdersFilter.getJobschedulerId(), listOfPlannedOrders);
            // TODO: Check answers for error

            dbLayerDailyPlannedOrders.delete(filter);

            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(fromDate);
            int fromDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
            int fromYear = calendar.get(java.util.Calendar.YEAR);

            calendar.setTime(toDate);
            int toDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
            int toYear = calendar.get(java.util.Calendar.YEAR);

            FilterDailyPlan filterDaysPlanned = new FilterDailyPlan();
            filterDaysPlanned.setJobschedulerId(plannedOrdersFilter.getJobschedulerId());

            for (int year = fromYear; year <= toYear; year++) {
                filterDaysPlanned.setYear(year);
                filterDaysPlanned.setDayFrom(getDayFrom(year, fromYear, fromDayOfYear, toYear));
                filterDaysPlanned.setDayTo(getDayTo(year, fromYear, toDayOfYear, toYear));
                dbLayerDailyPlan.delete(filterDaysPlanned);
            }
            Globals.commit(sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRemovePlans(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    plannedOrdersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            removeOrdersFromPlan(plannedOrdersFilter);

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

    public int testGetDayFrom(int year, int fromYear, int fromDayOfYear, int toYear) {
        return getDayFrom(year, fromYear, fromDayOfYear, toYear);
    }

    public int testGetDayTo(int year, int fromYear, int toDayOfYear, int toYear) {
        return getDayTo(year, fromYear, toDayOfYear, toYear);
    }

}
