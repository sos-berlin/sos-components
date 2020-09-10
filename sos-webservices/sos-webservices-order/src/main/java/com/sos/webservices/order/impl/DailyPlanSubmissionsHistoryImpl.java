package com.sos.webservices.order.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissionHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionHistoryFilter;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissionHistory;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissionHistory;
import com.sos.webservices.order.resource.IDailyPlanSubmissionsHistoryResource;

@Path("daily_plan")
public class DailyPlanSubmissionsHistoryImpl extends JOCResourceImpl implements IDailyPlanSubmissionsHistoryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmissionsHistoryImpl.class);
    private static final String API_CALL = "./daily_plan/submissions";

    @Override
    public JOCDefaultResponse postDailyPlanSubmissionHistory(String xAccessToken, DailyPlanSubmissionHistoryFilter dailyPlanSubmissionHistoryFilter) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanSubmissionHistoryFilter, xAccessToken, dailyPlanSubmissionHistoryFilter.getControllerId(),
                    getPermissonsJocCockpit(dailyPlanSubmissionHistoryFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlanSubmissionHistory dbLayerDailyPlan = new DBLayerDailyPlanSubmissionHistory(sosHibernateSession);
            boolean hasPermission = true;

            Globals.beginTransaction(sosHibernateSession);

            Date fromDate = null;
            Date toDate = null;
           
            FilterDailyPlanSubmissionHistory filter = new FilterDailyPlanSubmissionHistory();
            filter.setControllerId(dailyPlanSubmissionHistoryFilter.getControllerId());
            Calendar calendar = Calendar.getInstance();
            if (dailyPlanSubmissionHistoryFilter.getDateFrom() != null) {
                fromDate = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getDateFrom(), dailyPlanSubmissionHistoryFilter.getTimeZone());
                calendar.setTime(fromDate);
            }
            if (dailyPlanSubmissionHistoryFilter.getDateTo() != null) {
                toDate = JobSchedulerDate.getDateTo(dailyPlanSubmissionHistoryFilter.getDateTo(), dailyPlanSubmissionHistoryFilter.getTimeZone());
                calendar.setTime(toDate);
            }

            if (hasPermission) {
                List<DBItemDailyPlanSubmissionHistory> listOfPlans = dbLayerDailyPlan.getPlans(filter, 0);
                for (DBItemDailyPlanSubmissionHistory dbItemDailyPlan : listOfPlans) {
        /*            PlanItem p = new PlanItem();
                    p.setJobschedulerId( dbItemDailyPlan.getJobschedulerId());
                    calendar.set(Calendar.DAY_OF_YEAR, dbItemDailyPlan.getDay());
                    calendar.set(Calendar.YEAR, dbItemDailyPlan.getYear());
                    p.setPlanDay(calendar.getTime());
                    p.setPlanId(dbItemDailyPlan.getId());
                    result.add(p);
                    */
                }
            }

            //entity.setPlanItems(result);
            //entity.setDeliveryDate(Date.from(Instant.now()));

            //return JOCDefaultResponse.responseStatus200(entity);
            return JOCDefaultResponse.responseStatus200(null);

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
