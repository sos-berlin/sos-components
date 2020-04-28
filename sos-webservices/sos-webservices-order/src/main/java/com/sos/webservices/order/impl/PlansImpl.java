package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlanItem;
import com.sos.joc.model.plan.Plans;
import com.sos.joc.model.plan.PlansFilter;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;
import com.sos.webservices.order.resource.IPlansResource;

@Path("plan")
public class PlansImpl extends JOCResourceImpl implements IPlansResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlansImpl.class);
    private static final String API_CALL = "./plan/list";

    @Override
    public JOCDefaultResponse postPlan(String xAccessToken, PlansFilter plansFilter) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plansFilter, xAccessToken, plansFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plansFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlan dbLayerDailyPlan = new DBLayerDailyPlan(sosHibernateSession);
            boolean hasPermission = true;

            Globals.beginTransaction(sosHibernateSession);

            Date fromDate = null;
            Date toDate = null;
           
            FilterDailyPlan filter = new FilterDailyPlan();
            filter.setJobschedulerId(plansFilter.getJobschedulerId());
            Calendar calendar = Calendar.getInstance();
            if (plansFilter.getDateFrom() != null) {
                fromDate = JobSchedulerDate.getDateFrom(plansFilter.getDateFrom(), plansFilter.getTimeZone());
                calendar.setTime(fromDate);
                filter.setDayFrom(calendar.get(Calendar.DAY_OF_YEAR));
                filter.setYearFrom(calendar.get(Calendar.YEAR));
            }
            if (plansFilter.getDateTo() != null) {
                toDate = JobSchedulerDate.getDateTo(plansFilter.getDateTo(), plansFilter.getTimeZone());
                calendar.setTime(toDate);
                filter.setDayFrom(calendar.get(Calendar.DAY_OF_YEAR));
                filter.setYearTo(calendar.get(Calendar.YEAR));
            }

            List<PlanItem> result = new ArrayList<PlanItem>();
            Plans entity = new Plans();

            if (hasPermission) {
                List<DBItemDailyPlan> listOfPlans = dbLayerDailyPlan.getPlans(filter, 0);
                for (DBItemDailyPlan dbItemDailyPlan : listOfPlans) {
                    PlanItem p = new PlanItem();
                    p.setJobschedulerId( dbItemDailyPlan.getJobschedulerId());
                    calendar.set(Calendar.DAY_OF_YEAR, dbItemDailyPlan.getDay());
                    calendar.set(Calendar.YEAR, dbItemDailyPlan.getYear());
                    p.setPlanDay(calendar.getTime());
                    result.add(p);
                }
            }

            entity.setPlanItems(result);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);

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

}
