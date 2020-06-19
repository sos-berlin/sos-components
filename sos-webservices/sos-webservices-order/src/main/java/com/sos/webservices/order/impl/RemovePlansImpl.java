package com.sos.webservices.order.impl;

import java.net.URISyntaxException;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.orders.DBItemDailyPlan;
import com.sos.joc.db.orders.DBItemDailyPlannedOrders;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlansFilter;
import com.sos.webservices.order.classes.OrderHelper;
import com.sos.js7.order.initiator.db.DBLayerDailyPlan;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlan;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IRemovePlansResource;

@Path("plan")
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

    private void removeOrdersFromPlanAndController(OrderHelper orderHelper, FilterDailyPlan filterDailyPlan) throws JocConfigurationException, DBConnectionRefusedException,
            JobSchedulerInvalidResponseDataException, JsonProcessingException, SOSException, URISyntaxException, DBOpenSessionException {
        SOSHibernateSession sosHibernateSession = null;

        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

        try {
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
 
            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            List<DBItemDailyPlan> listOfPlans = dbLayerDailyPlan.getPlans(filterDailyPlan, 0);

            for (DBItemDailyPlan dbItemDailyPlan : listOfPlans) {

                FilterDailyPlannedOrders filterDailyPlannedOrders = new FilterDailyPlannedOrders();
                filterDailyPlannedOrders.setJobSchedulerId(filterDailyPlan.getJobschedulerId());
                filterDailyPlannedOrders.setPlanId(dbItemDailyPlan.getId());

                List<DBItemDailyPlannedOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filterDailyPlannedOrders, 0);
               
         
                String answer = orderHelper.removeFromJobSchedulerController(filterDailyPlan.getJobschedulerId(), listOfPlannedOrders);
                LOGGER.debug(answer);
                // TODO: Check answers for error

                dbLayerDailyPlannedOrders.delete(filterDailyPlannedOrders);
            }

         } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRemovePlans(String xAccessToken, PlansFilter plansFilter) throws JocException {
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plansFilter, xAccessToken, plansFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plansFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            OrderHelper orderHelper = null;
            if (Globals.jocConfigurationProperties != null && Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + plansFilter.getJobschedulerId()) != null){
                orderHelper = new OrderHelper(Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + plansFilter.getJobschedulerId()));
            } else {
                orderHelper = new OrderHelper(dbItemInventoryInstance.getUri());
            }
            
            Date fromDate = null;
            Date toDate = null;

            Calendar calendar = Calendar.getInstance();

            SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            FilterDailyPlan filterDailyPlan = new FilterDailyPlan();
            filterDailyPlan.setJobschedulerId(plansFilter.getJobschedulerId());
            if (plansFilter.getDateFrom() != null) {
                fromDate = JobSchedulerDate.getDateFrom(plansFilter.getDateFrom(), plansFilter.getTimeZone());
                calendar.setTime(fromDate);
                filterDailyPlan.setDayFrom(calendar.get(Calendar.DAY_OF_YEAR));
                filterDailyPlan.setYearFrom(calendar.get(Calendar.YEAR));

            }
            if (plansFilter.getDateTo() != null) {
                toDate = JobSchedulerDate.getDateFrom(plansFilter.getDateTo(), plansFilter.getTimeZone());
                calendar.setTime(toDate);
                filterDailyPlan.setDayTo(calendar.get(Calendar.DAY_OF_YEAR));
                filterDailyPlan.setYearTo(calendar.get(Calendar.YEAR));
            }

            filterDailyPlan.setPlanId(plansFilter.getPlanId());
            
            removeOrdersFromPlanAndController(orderHelper, filterDailyPlan);
            
            dbLayerDailyPlan.delete(filterDailyPlan);
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
        }

    }

    public int testGetDayFrom(int year, int fromYear, int fromDayOfYear, int toYear) {
        return getDayFrom(year, fromYear, fromDayOfYear, toYear);
    }

    public int testGetDayTo(int year, int fromYear, int toDayOfYear, int toYear) {
        return getDayTo(year, fromYear, toDayOfYear, toYear);
    }

}
