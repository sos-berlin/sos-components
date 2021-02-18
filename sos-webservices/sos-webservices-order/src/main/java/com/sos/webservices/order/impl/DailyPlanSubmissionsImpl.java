package com.sos.webservices.order.impl;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanSubmissionsResource;

@Path("daily_plan")
public class DailyPlanSubmissionsImpl extends JOCResourceImpl implements IDailyPlanSubmissionsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmissionsImpl.class);
    private static final String API_CALL = "./daily_plan/submissions";

    @Override
    public JOCDefaultResponse postDailyPlanSubmissions(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanSubmissionsFilter dailyPlanSubmissionHistoryFilter = Globals.objectMapper.readValue(filterBytes,
                    DailyPlanSubmissionsFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanSubmissionHistoryFilter, accessToken, getControllerId(accessToken,
                    dailyPlanSubmissionHistoryFilter.getControllerId()), getPermissonsJocCockpit(dailyPlanSubmissionHistoryFilter.getControllerId(),
                            accessToken).getDailyPlan().getView().isStatus());

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


    @Override
    public JOCDefaultResponse postDeleteDailyPlanSubmissions(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanSubmissionsFilter dailyPlanSubmissionHistoryFilter = Globals.objectMapper.readValue(filterBytes,
                    DailyPlanSubmissionsFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanSubmissionHistoryFilter, accessToken, getControllerId(accessToken,
                    dailyPlanSubmissionHistoryFilter.getControllerId()), getPermissonsJocCockpit(dailyPlanSubmissionHistoryFilter.getControllerId(),
                            accessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("filter", dailyPlanSubmissionHistoryFilter.getFilter());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlanSubmissions dbLayerDailyPlan = new DBLayerDailyPlanSubmissions(sosHibernateSession);

            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(dailyPlanSubmissionHistoryFilter.getControllerId());

            if (dailyPlanSubmissionHistoryFilter.getFilter().getDateFor() != null) {
                Date date = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getFilter().getDateFor(), dailyPlanSubmissionHistoryFilter
                        .getTimeZone());
                filter.setDateFor(date);

            } else {

                if (dailyPlanSubmissionHistoryFilter.getFilter().getDateFrom() != null) {
                    Date fromDate = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getFilter().getDateFrom(),
                            dailyPlanSubmissionHistoryFilter.getTimeZone());
                    filter.setDateFrom(fromDate);
                    filter.setDateFor(fromDate);

                }
                if (dailyPlanSubmissionHistoryFilter.getFilter().getDateTo() != null) {
                    Date toDate = JobSchedulerDate.getDateTo(dailyPlanSubmissionHistoryFilter.getFilter().getDateTo(),
                            dailyPlanSubmissionHistoryFilter.getTimeZone());
                    filter.setDateTo(toDate);
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(filter.getDateFor());
            calendar.add(java.util.Calendar.DATE, 1);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            
            filter.setDateFor(calendar.getTime());
            
            dbLayerDailyPlan.delete(filter);
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
}
