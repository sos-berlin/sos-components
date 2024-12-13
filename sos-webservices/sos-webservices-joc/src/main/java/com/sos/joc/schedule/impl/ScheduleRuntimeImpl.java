package com.sos.joc.schedule.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.RunTime;
import com.sos.joc.model.order.ScheduleDatesFilter;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.schedule.resource.IScheduleRuntimeResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("schedule")
public class ScheduleRuntimeImpl extends JOCResourceImpl implements IScheduleRuntimeResource {

    private static final String API_CALL = "./schedule/runtime";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRuntimeImpl.class);
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);
    private static DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;

    // true (new behavior) - create a preview per day in the date range, like the DailyPlan does
    // - TODO in some cases performance problems(next year view), e.g. Every 2nd day starting with day 01.12.2024 of Dec ...
    // -- because Every/Repetitions is calculated from a specific start day - so if start=2024.. there is more to calculate for 2030 than for 2025...
    // false (previous behavior) - create a preview for the entire date range with one call
    private static final boolean DAILY_PLAN_MODE = false;

    @Override
    public JOCDefaultResponse postScheduleRuntime(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, ScheduleDatesFilter.class);
            ScheduleDatesFilter in = Globals.objectMapper.readValue(filterBytes, ScheduleDatesFilter.class);
            JocPermissions perms = getJocPermissions(accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, perms.getCalendars().getView() || perms.getDailyPlan().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            RunTime entity = new RunTime();
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            if (in.getCalendars() != null && !in.getCalendars().isEmpty()) {

                if (in.getTimeZone() == null || in.getTimeZone().isEmpty()) {
                    in.setTimeZone("Etc/UTC");
                }
                final ZoneId timezone = ZoneId.of(in.getTimeZone());

                FrequencyResolver fr = new FrequencyResolver();
                if (in.getDateFrom() == null) {
                    in.setDateFrom(fr.getToday());
                }
                if (in.getDateTo() == null) {
                    in.setDateTo(fr.getLastDayOfCurrentYear());
                }

                session = Globals.createSosHibernateStatelessConnection(API_CALL);
                InventoryDBLayer dbLayer = new InventoryDBLayer(session);

                final List<String> nonWorkingDays = getNonWorkingDays(dbLayer, in);

                List<DBItemInventoryReleasedConfiguration> workingDbCalendars = dbLayer.getReleasedCalendarsByNames(in.getCalendars().stream().map(
                        AssignedCalendars::getCalendarName).distinct().collect(Collectors.toList()));

                SortedSet<Period> periods = new TreeSet<>(Comparator.comparing(p -> p.getSingleStart() == null ? p.getBegin() : p.getSingleStart()));

                if (workingDbCalendars != null && !workingDbCalendars.isEmpty()) {
                    Map<String, DBItemInventoryReleasedConfiguration> nameContentMap = workingDbCalendars.stream().collect(Collectors.toMap(
                            DBItemInventoryReleasedConfiguration::getName, Function.identity()));

                    for (AssignedCalendars c : in.getCalendars()) {
                        DBItemInventoryReleasedConfiguration item = nameContentMap.get(c.getCalendarName());
                        if (item == null) {
                            continue;
                        }
                        if (!folderIsPermitted(item.getFolder(), permittedFolders)) {
                            continue;
                        }
                        Calendar restrictions = new Calendar();
                        restrictions.setIncludes(c.getIncludes());
                        // restrictions.setExcludes(c.getExcludes());
                        Calendar basedOn = Globals.objectMapper.readValue(item.getContent(), Calendar.class);
                        if (DAILY_PLAN_MODE) {
                            for (String asDailyPlanSingleDay : SOSDate.getDatesInRange(in.getDateFrom(), in.getDateTo())) {
                                new FrequencyResolver().resolveRestrictions(basedOn, restrictions, asDailyPlanSingleDay, asDailyPlanSingleDay)
                                        .getDates().stream().flatMap(date -> getPeriods(c.getPeriods(), nonWorkingDays, date, timezone,
                                                getJocError())).collect(Collectors.toCollection(() -> periods));
                            }
                        } else {
                            fr.resolveRestrictions(basedOn, restrictions, in.getDateFrom(), in.getDateTo()).getDates().stream().flatMap(
                                    date -> getPeriods(c.getPeriods(), nonWorkingDays, date, timezone, getJocError())).collect(Collectors
                                            .toCollection(() -> periods));
                        }
                    }
                }
                entity.setPeriods(new ArrayList<>(periods));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private List<String> getNonWorkingDays(InventoryDBLayer dbLayer, ScheduleDatesFilter in) throws SOSHibernateException, JsonParseException,
            JsonMappingException, IOException, SOSMissingDataException, SOSInvalidDataException {
        FrequencyResolver fr = new FrequencyResolver();
        List<String> nonWorkingDays = new ArrayList<>();

        if (in.getNonWorkingDayCalendars() != null && !in.getNonWorkingDayCalendars().isEmpty()) {
            List<DBItemInventoryReleasedConfiguration> nonWorkingDbCalendars = dbLayer.getReleasedCalendarsByNames(in.getNonWorkingDayCalendars()
                    .stream().map(AssignedNonWorkingDayCalendars::getCalendarName).distinct().collect(Collectors.toList()));

            if (nonWorkingDbCalendars != null && !nonWorkingDbCalendars.isEmpty()) {

                Map<String, String> nameContentMap = nonWorkingDbCalendars.stream().collect(Collectors.toMap(
                        DBItemInventoryReleasedConfiguration::getName, DBItemInventoryReleasedConfiguration::getContent));

                for (AssignedNonWorkingDayCalendars c : in.getNonWorkingDayCalendars()) {
                    if (!nameContentMap.containsKey(c.getCalendarName())) {
                        continue;
                    }
                    Calendar basedOn = Globals.objectMapper.readValue(nameContentMap.get(c.getCalendarName()), Calendar.class);
                    nonWorkingDays.addAll(fr.resolve(basedOn, in.getDateFrom(), in.getDateTo()).getDates());
                }
            }
        }
        return nonWorkingDays;
    }

    private static Stream<Period> getPeriods(List<Period> periods, List<String> holidays, String date, ZoneId timezone, JocError jocError) {
        if (periods == null) {
            return Stream.empty();
        }
        return periods.stream().map(p -> getPeriod(p, holidays, date, timezone, jocError)).filter(Objects::nonNull);
    }

    private static Period getPeriod(Period period, List<String> holidays, String date, ZoneId timezone, JocError jocError) {
        Period p = new Period();

        if (holidays.contains(date)) {
            if (period.getWhenHoliday() != null) {
                switch (period.getWhenHoliday()) {
                case SUPPRESS:
                    return null;
                case NEXTNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, 1);
                        date = dateFormatter.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, 1);
                            date = dateFormatter.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] %s", period.toString(), e.toString()));
                        return null;
                    }
                    break;
                case PREVIOUSNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, -1);
                        date = dateFormatter.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, -1);
                            date = dateFormatter.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] %s", period.toString(), e.toString()));
                        return null;
                    }
                    break;
                case IGNORE:
                    break;
                }
            } else {
                return null;
            }
        }

        if (period.getSingleStart() != null) {
            p.setSingleStart(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + normalizeTime(period.getSingleStart()),
                    dateTimeFormatter), timezone)));
            return p;
        }
        if (period.getRepeat() != null && !period.getRepeat().isEmpty()) {
            p.setRepeat(period.getRepeat());
            String begin = period.getBegin();
            if (begin == null || begin.isEmpty()) {
                begin = "00:00:00";
            } else {
                begin = normalizeTime(begin);
            }

            p.setBegin(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + begin, dateTimeFormatter), timezone)));
            String end = period.getEnd();
            if (end == null || end.isEmpty()) {
                end = "24:00:00";
            } else {
                end = normalizeTime(end);
            }
            if (end.startsWith("24:00")) {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T23:59:59", dateTimeFormatter).plusSeconds(1L), timezone)));
            } else {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + end, dateTimeFormatter), timezone)));
            }
            return p;
        }
        return null;
    }

    private static String normalizeTime(String time) {
        String[] ss = (time + ":00:00:00").split(":", 3);
        ss[2] = ss[2].substring(0, 2);
        return String.format("%2s:%2s:%2s", ss[0], ss[1], ss[2]).replace(' ', '0');
    }

}
