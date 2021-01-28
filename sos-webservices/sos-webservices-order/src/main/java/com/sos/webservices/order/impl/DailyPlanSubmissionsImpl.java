package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.joc.db.orders.DBItemDailyPlanSubmissions;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissions;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsItem;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissions;
import com.sos.webservices.order.resource.IDailyPlanSubmissionsResource;

@Path("daily_plan")
public class DailyPlanSubmissionsImpl extends JOCResourceImpl implements IDailyPlanSubmissionsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmissionsImpl.class);
    private static final String API_CALL = "./daily_plan/submissions";

    @Override
    public JOCDefaultResponse postDailyPlanSubmissions(String xAccessToken, DailyPlanSubmissionsFilter dailyPlanSubmissionHistoryFilter)
            throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanSubmissionHistoryFilter, xAccessToken, getControllerId(xAccessToken,
                    dailyPlanSubmissionHistoryFilter.getControllerId()), getPermissonsJocCockpit(dailyPlanSubmissionHistoryFilter.getControllerId(),
                            xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("filter", dailyPlanSubmissionHistoryFilter.getFilter());
            this.checkRequiredParameter("dateTo", dailyPlanSubmissionHistoryFilter.getFilter().getDateTo());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlanSubmissions dbLayerDailyPlan = new DBLayerDailyPlanSubmissions(sosHibernateSession);

            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(dailyPlanSubmissionHistoryFilter.getControllerId());
            if (dailyPlanSubmissionHistoryFilter.getFilter().getDateFrom() != null) {
                Date fromDate = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getFilter().getDateFrom(),
                        dailyPlanSubmissionHistoryFilter.getTimeZone());
                filter.setDateFrom(fromDate);
            }
            if (dailyPlanSubmissionHistoryFilter.getFilter().getDateTo() != null) {
                Date toDate = JobSchedulerDate.getDateTo(dailyPlanSubmissionHistoryFilter.getFilter().getDateTo(), dailyPlanSubmissionHistoryFilter
                        .getTimeZone());
                filter.setDateTo(toDate);
            }

            filter.setSortMode("desc");
            filter.setOrderCriteria("id");
            DailyPlanSubmissions dailyPlanSubmissions = new DailyPlanSubmissions();
            List<DailyPlanSubmissionsItem> result = new ArrayList<DailyPlanSubmissionsItem>();

            List<DBItemDailyPlanSubmissions> listOfDailyPlanSubmissions = dbLayerDailyPlan.getDailyPlanSubmissions(filter, 0);
            for (DBItemDailyPlanSubmissions dbItemDailySubmissionHistory : listOfDailyPlanSubmissions) {
                DailyPlanSubmissionsItem p = new DailyPlanSubmissionsItem();
                p.setSubmissionHistoryId(dbItemDailySubmissionHistory.getId());
                p.setControllerId(dbItemDailySubmissionHistory.getControllerId());
                p.setDailyPlanDate(dbItemDailySubmissionHistory.getSubmissionForDate());
                p.setSubmissionTime(dbItemDailySubmissionHistory.getCreated());
                result.add(p);
            }

            dailyPlanSubmissions.setSubmissionHistoryItems(result);
            dailyPlanSubmissions.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(dailyPlanSubmissions);

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
