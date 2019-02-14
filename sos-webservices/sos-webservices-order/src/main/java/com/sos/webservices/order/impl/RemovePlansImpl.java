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
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlanFilter;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.DBLayerDaysPlanned;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;
import com.sos.webservices.order.initiator.db.FilterDaysPlanned;
import com.sos.webservices.order.classes.OrderHelper;
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

    private void removeOrdersFromPlan(PlanFilter planFilter) throws JocConfigurationException, DBConnectionRefusedException,
            JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException {
        SOSHibernateSession sosHibernateSession = null;
//TODO: Not use PlanFilter. A new Filter with MasterId, From, To must be used
        
        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

        try {
            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            Date fromDate = null;
            Date toDate = null;

            FilterDailyPlan filter = new FilterDailyPlan();
            filter.setMasterId(planFilter.getJobschedulerId());
            fromDate = JobSchedulerDate.getDateFrom(planFilter.getDateFrom(), planFilter.getTimeZone());
            filter.setPlannedStartFrom(fromDate);
            toDate = JobSchedulerDate.getDateTo(planFilter.getDateTo(), planFilter.getTimeZone());
            filter.setPlannedStartTo(toDate);
            List<DBItemDailyPlan> listOfPlannedOrders = dbLayerDailyPlan.getDailyPlanList(filter, 0);
            OrderHelper orderHelper = new OrderHelper();
            String answer = orderHelper.removeFromJobSchedulerMaster(planFilter.getJobschedulerId(), listOfPlannedOrders);
            //TODO: Check answers for error

            
            dbLayerDailyPlan.delete(filter);

            DBLayerDaysPlanned dbLayerDaysPlanned = new DBLayerDaysPlanned(sosHibernateSession);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(fromDate);
            int fromDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
            int fromYear = calendar.get(java.util.Calendar.YEAR);

            calendar.setTime(toDate);
            int toDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
            int toYear = calendar.get(java.util.Calendar.YEAR);

            FilterDaysPlanned filterDaysPlanned = new FilterDaysPlanned();
            filterDaysPlanned.setMasterId(planFilter.getJobschedulerId());

            for (int year = fromYear; year <= toYear; year++) {
                filterDaysPlanned.setYear(year);
                filterDaysPlanned.setDayFrom(getDayFrom(year, fromYear, fromDayOfYear, toYear));
                filterDaysPlanned.setDayTo(getDayTo(year, fromYear, toDayOfYear, toYear));
                dbLayerDaysPlanned.delete(filterDaysPlanned);
            }
            Globals.commit(sosHibernateSession);
        } finally

        {
            Globals.disconnect(sosHibernateSession);
        }
    }

  

    @Override
    public JOCDefaultResponse postRemovePlans(String xAccessToken, PlanFilter planFilter) throws JocException {
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, planFilter, xAccessToken, planFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    planFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            removeOrdersFromPlan(planFilter);

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
