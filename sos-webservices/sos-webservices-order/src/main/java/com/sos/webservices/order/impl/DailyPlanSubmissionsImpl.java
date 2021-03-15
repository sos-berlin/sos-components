package com.sos.webservices.order.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissions;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissions;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsItem;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanSubmissions;
import com.sos.js7.order.initiator.db.FilterDailyPlanSubmissions;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanSubmissionsResource;

@Path("daily_plan")
public class DailyPlanSubmissionsImpl extends JOCResourceImpl implements IDailyPlanSubmissionsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSubmissionsImpl.class);
    private static final String API_CALL = "./daily_plan/submissions";
    private OrderInitiatorSettings settings;

    @Override
    public JOCDefaultResponse postDailyPlanSubmissions(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanSubmissionsFilter dailyPlanSubmissionHistoryFilter = Globals.objectMapper.readValue(filterBytes,
                    DailyPlanSubmissionsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(dailyPlanSubmissionHistoryFilter.getControllerId(), getPermissonsJocCockpit(
                    dailyPlanSubmissionHistoryFilter.getControllerId(), accessToken).getDailyPlan().getView().isStatus());

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
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
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

            setSettings();
            DBLayerDailyPlanSubmissions dbLayerDailyPlan = new DBLayerDailyPlanSubmissions(sosHibernateSession);

            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(dailyPlanSubmissionHistoryFilter.getControllerId());

            if (dailyPlanSubmissionHistoryFilter.getFilter().getDateFor() != null) {
                Date date = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getFilter().getDateFor(), dailyPlanSubmissionHistoryFilter
                        .getTimeZone());
                filter.setDateFor(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(filter.getDateFor());
                calendar.add(java.util.Calendar.DATE, 1);
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calendar.set(java.util.Calendar.MINUTE, 0);
                calendar.set(java.util.Calendar.SECOND, 0);
                calendar.set(java.util.Calendar.MILLISECOND, 0);
                calendar.set(java.util.Calendar.MINUTE, 0);

                filter.setDateFor(calendar.getTime());

            } else {

                if (dailyPlanSubmissionHistoryFilter.getFilter().getDateFrom() != null) {
                    Date fromDate = JobSchedulerDate.getDateFrom(dailyPlanSubmissionHistoryFilter.getFilter().getDateFrom(),
                            dailyPlanSubmissionHistoryFilter.getTimeZone());
                    filter.setDateFrom(utc2Timezone(fromDate, dailyPlanSubmissionHistoryFilter.getTimeZone()));
                }
                if (dailyPlanSubmissionHistoryFilter.getFilter().getDateTo() != null) {
                    Date toDate = JobSchedulerDate.getDateTo(dailyPlanSubmissionHistoryFilter.getFilter().getDateTo(),
                            dailyPlanSubmissionHistoryFilter.getTimeZone());
                    filter.setDateTo(utc2Timezone(toDate, dailyPlanSubmissionHistoryFilter.getTimeZone()));
                }
            }

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

    private Date utc2Timezone(Date d, String fromTimeZone) throws ParseException {
        String timeZone;
        if (fromTimeZone == null) {
            fromTimeZone = "UTC";
        }
        if (settings == null) {
            timeZone = "Europe/Berlin";
        } else {
            timeZone = settings.getTimeZone();
        }
        SimpleDateFormat sdfFromTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfFromTimezone.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
        SimpleDateFormat sdfToTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfToTimezone.setTimeZone(TimeZone.getTimeZone(timeZone));
        return sdfFromTimezone.parse(sdfToTimezone.format(d));
    }

    private void setSettings() throws Exception {
        if (Globals.configurationGlobals != null) {

            GlobalSettingsReader reader = new GlobalSettingsReader();
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            this.settings = reader.getSettings(section);
        }
    }
}
