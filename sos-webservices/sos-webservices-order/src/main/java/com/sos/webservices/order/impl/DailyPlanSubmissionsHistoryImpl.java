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
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissionHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionHistory;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionHistoryFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionHistoryItem;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissionHistory;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissionHistory;
import com.sos.webservices.order.resource.IDailyPlanSubmissionsHistoryResource;

@Path("daily_plan")
public class DailyPlanSubmissionsHistoryImpl extends JOCResourceImpl implements IDailyPlanSubmissionsHistoryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmissionsHistoryImpl.class);
    private static final String API_CALL = "./daily_plan/submissions";

    @Override
    public JOCDefaultResponse postDailyPlanSubmissionHistory(String xAccessToken, DailyPlanSubmissionHistoryFilter dailyPlanSubmissionHistoryFilter)
            throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanSubmissionHistoryFilter, xAccessToken, dailyPlanSubmissionHistoryFilter
                    .getControllerId(), getPermissonsJocCockpit(dailyPlanSubmissionHistoryFilter.getControllerId(), xAccessToken).getDailyPlan()
                            .getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlanSubmissionHistory dbLayerDailyPlan = new DBLayerDailyPlanSubmissionHistory(sosHibernateSession);

            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlanSubmissionHistory filter = new FilterDailyPlanSubmissionHistory();
            filter.setControllerId(dailyPlanSubmissionHistoryFilter.getControllerId());
            if (dailyPlanSubmissionHistoryFilter.getDateFrom() != null) {
                Date fromDate = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getDateFrom(), dailyPlanSubmissionHistoryFilter
                        .getTimeZone());
                filter.setDateFrom(fromDate);
            }
            if (dailyPlanSubmissionHistoryFilter.getDateTo() != null) {
                Date toDate = JobSchedulerDate.getDateTo(dailyPlanSubmissionHistoryFilter.getDateTo(), dailyPlanSubmissionHistoryFilter
                        .getTimeZone());
                filter.setDateTo(toDate);
            }

            DailyPlanSubmissionHistory dailyPlanSubmissionHistory = new DailyPlanSubmissionHistory();
            List<DailyPlanSubmissionHistoryItem> result = new ArrayList<DailyPlanSubmissionHistoryItem>();

            List<DBItemDailyPlanSubmissionHistory> listOfDailyPlanSubmissions = dbLayerDailyPlan.getDailyPlanSubmissions(filter, 0);
            for (DBItemDailyPlanSubmissionHistory dbItemDailySubmissionHistory : listOfDailyPlanSubmissions) {
                DailyPlanSubmissionHistoryItem p = new DailyPlanSubmissionHistoryItem();
                p.setControllerId(dbItemDailySubmissionHistory.getControllerId());
                p.setUserAccount(dbItemDailySubmissionHistory.getUserAccount());
                p.setDailyPlanDate(dbItemDailySubmissionHistory.getSubmissionForDate());
                result.add(p);
            }

            dailyPlanSubmissionHistory.setSubmissionHistoryItems(result);
            dailyPlanSubmissionHistory.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(dailyPlanSubmissionHistory);

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
