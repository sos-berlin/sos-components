package com.sos.joc.schedule.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.OrderListSynchronizer;
import com.sos.joc.dailyplan.common.AbsoluteMainPeriod;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.schedule.runtime.ScheduleRunTimeRequest;
import com.sos.joc.model.schedule.runtime.ScheduleRunTimeResponse;
import com.sos.joc.model.schedule.runtime.items.DailyPlanDate;
import com.sos.joc.model.schedule.runtime.items.DailyPlanDates;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.schedule.resource.IScheduleRuntimeResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("schedule")
public class ScheduleRuntimeImpl extends JOCResourceImpl implements IScheduleRuntimeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRuntimeImpl.class);
    private static final String API_CALL = "./schedule/runtime";

    @Override
    public JOCDefaultResponse postScheduleRuntime(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.DAILYPLAN);
            JsonValidator.validate(filterBytes, ScheduleRunTimeRequest.class);
            ScheduleRunTimeRequest in = Globals.objectMapper.readValue(filterBytes, ScheduleRunTimeRequest.class);
            JocPermissions perms = getBasicJocPermissions(accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, perms.getCalendars().getView() || perms.getDailyPlan().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ScheduleRunTimeResponse entity = new ScheduleRunTimeResponse();
            entity.setDates(new DailyPlanDates());

            if (!SOSCollection.isEmpty(in.getCalendars()) && in.getDateFrom() != null && in.getDateTo() != null) {
                DailyPlanSettings settings = JOCOrderResourceImpl.getDailyPlanSettings(API_CALL);
                settings.setCalculateAbsoluteMainPeriodsOnly(true);
                settings.setPermittedFolders(folderPermissions.getListOfFolders());
                settings.setStartMode(StartupMode.webservice);

                final DailyPlanRunner runner = new DailyPlanRunner(settings);
                List<DailyPlanSchedule> dailyPlanSchedules = List.of(toDailyPlanSchedule(in));

                SOSDate.getDatesInRange(in.getDateFrom(), in.getDateTo()).stream().forEach(asDailyPlanSingleDate -> {
                    // SOSDate.getDatesInRange(dateFrom, dateTo).stream().parallel().forEach(asDailyPlanSingleDate -> {
                    try {
                        settings.setDailyPlanDate(SOSDate.getDate(asDailyPlanSingleDate));

                        DBItemDailyPlanSubmission dummySubmission = new DBItemDailyPlanSubmission();
                        dummySubmission.setId(-1L);
                        dummySubmission.setSubmissionForDate(settings.getDailyPlanDate());

                        OrderListSynchronizer synchronizer = runner.calculateStartTimes(settings.getStartMode(), "controllerId", dailyPlanSchedules,
                                asDailyPlanSingleDate, dummySubmission);

                        List<AbsoluteMainPeriod> absPeriods = synchronizer.getAbsoluteMainPeriods();
                        if (absPeriods.size() > 0) {
                            DailyPlanDate d = new DailyPlanDate();
                            d.setPeriods(absPeriods.stream().map(p -> p.getPeriod()).collect(Collectors.toList()));

                            entity.getDates().getAdditionalProperties().put(asDailyPlanSingleDate, d);
                        }
                    } catch (Exception e) {
                        LOGGER.info("[" + asDailyPlanSingleDate + "]" + e, e);
                    }
                });
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

    private DailyPlanSchedule toDailyPlanSchedule(ScheduleRunTimeRequest in) {
        Schedule schedule = new Schedule();
        schedule.setPath(API_CALL + "_tmp");
        if (in.getTimeZone() == null) {
            // AssignedCalendars with default Etc/Utc time zone
            schedule.setCalendars(in.getCalendars());
        } else {
            // AssignedCalendars overwrite default Etc/Utc time zone
            schedule.setCalendars(in.getCalendars().stream().map(c -> {
                c.setTimeZone(in.getTimeZone());
                return c;
            }).collect(Collectors.toList()));
        }
        schedule.setNonWorkingDayCalendars(in.getNonWorkingDayCalendars());
        return new DailyPlanSchedule(schedule);
    }

}
