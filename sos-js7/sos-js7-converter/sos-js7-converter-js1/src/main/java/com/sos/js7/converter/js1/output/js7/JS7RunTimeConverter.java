package com.sos.js7.converter.js1.output.js7;

import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Months;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WeeklyDay;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.inventory.model.instruction.schedule.CycleSchedule;
import com.sos.inventory.model.instruction.schedule.Scheme;
import com.sos.inventory.model.job.AdmissionTimePeriod;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.DailyPeriod;
import com.sos.inventory.model.job.WeekdayPeriod;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.commons.wokflow.JS7ScheduleToWorkflowAdmissionTimes;
import com.sos.js7.converter.js1.common.job.ACommonJob;
import com.sos.js7.converter.js1.common.runtime.RunTime;

public class JS7RunTimeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7RunTimeConverter.class);

    private static final int DAY_SECONDS = 24 * 3_600;

    private enum DaysType {
        WEEKDAYS, MONTHDAYS, ULTIMOS;
    }

    public static CycleSchedule toWorkflowCyclicInstructions(Map<Path, Schedule> workflowSchedules) {
        if (JS12JS7Converter.CONFIG.getGenerateConfig().getCyclicOrders()) {
            return null;
        }
        if (SOSCollection.isEmpty(workflowSchedules)) {
            return null;
        }
        if (workflowSchedules.size() == 1) {
            return toWorkflowCyclicInstructions(workflowSchedules.values().iterator().next(), workflowSchedules.keySet().iterator().next());
        }

        Schedule lastSchedule = null;
        Path lastSchedulePath = null;
        for (Map.Entry<Path, Schedule> se : workflowSchedules.entrySet()) {
            if (lastSchedule == null) {
                lastSchedule = se.getValue();
                lastSchedulePath = se.getKey();
                continue;
            }

            if (!areEqualsForCyclic(lastSchedule, se.getValue())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[toWorkflowCyclicInstructions][skip][areEqualsForCyclic=fasle][s1=" + lastSchedule.getCalendars() + "]s2=" + se
                            .getValue().getCalendars());
                }
                return null;
            }
            lastSchedule = se.getValue();
            lastSchedulePath = se.getKey();
        }

        return toWorkflowCyclicInstructions(lastSchedule, lastSchedulePath);
    }

    private static boolean areEqualsForCyclic(Schedule s1, Schedule s2) {
        if (s1.getCalendars() == null || s2.getCalendars() == null) {
            return false;
        }
        return s1.getCalendars().equals(s2.getCalendars());
    }

    private static CycleSchedule toWorkflowCyclicInstructions(Schedule schedule, Path schedulePath) {
        if (schedule.getCalendars() == null) {
            return null;
        }

        List<Scheme> schemas = new ArrayList<>();
        for (AssignedCalendars cal : schedule.getCalendars()) {
            if (cal.getPeriods() == null) {
                return null;
            }
            if (cal.getIncludes() == null && cal.getExcludes() == null) {
                LOGGER.info("[toWorkflowCyclicInstructions][skip][can't be converted][missing includes, excludes]" + schedulePath);
                return null;
            }
            if (cal.getExcludes() != null) {
                // LOGGER.info("[toWorkflowCyclicInstructions][skip][can't be converted][excludes]" + schedulePath);
                // return null;
            }

            long countCyclic = cal.getPeriods().stream().filter(p -> p.getRepeat() != null).count();
            if (countCyclic == 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[toWorkflowCyclicInstructions][skip][singleStarts only]" + schedulePath);
                }
                return null;
            } else if (countCyclic != cal.getPeriods().size()) {
                long singleStarts = cal.getPeriods().size() - countCyclic;
                LOGGER.info("[toWorkflowCyclicInstructions][skip][can't be converted][mix cyclic(repeat)/singleStarts detected][singleStarts="
                        + singleStarts + ", repeat(cyclic)=" + countCyclic + "]" + schedulePath);
                return null;
            }

            if (cal.getIncludes().getHolidays() != null) {
                LOGGER.info("[toWorkflowCyclicInstructions][skip][can't be converted][includes/Holidays]" + schedulePath);
                return null;
            }

            boolean groupByRepeat = false;
            if (groupByRepeat) {
                Map<String, List<Period>> grouped = cal.getPeriods().stream().collect(Collectors.groupingBy(Period::getRepeat));
                grouped.forEach((key, list) -> {
                    for (Period p : list) {
                        Scheme scheme = JS7ScheduleToWorkflowAdmissionTimes.toScheme(p, cal.getIncludes());
                        if (scheme != null) {
                            schemas.add(scheme);
                        }
                    }
                });
            } else {
                for (Period p : cal.getPeriods()) {
                    Scheme scheme = JS7ScheduleToWorkflowAdmissionTimes.toScheme(p, cal.getIncludes());
                    if (scheme != null) {
                        schemas.add(scheme);
                    }
                }
            }
        }
        return SOSCollection.isEmpty(schemas) ? null : new CycleSchedule(schemas);
    }

    public static AdmissionTimeScheme toJobAdmissionTimeScheme(ACommonJob js1Job) {
        if (js1Job == null || js1Job.getRunTime() == null || js1Job.getRunTime().isEmpty()) {
            return null;
        }

        RunTime js1RunTime = js1Job.getRunTime().getSchedule() == null ? js1Job.getRunTime() : js1Job.getRunTime().getSchedule().getRunTime();

        boolean hasSingleStart = js1RunTime.getSingleStart() != null;
        boolean hasRepeat = js1RunTime.getRepeat() != null;
        boolean hasBeginEnd = !hasSingleStart && !hasRepeat && (js1RunTime.getBegin() != null || js1RunTime.getEnd() != null) && !js1RunTime
                .hasChildElements();
        boolean hasPeriods = js1RunTime.getPeriods() != null;
        boolean hasAts = js1RunTime.getAts() != null;
        boolean hasDates = js1RunTime.getDates() != null;
        boolean hasWeekDays = js1RunTime.getWeekDays() != null;
        boolean hasMonthDays = js1RunTime.getMonthDays() != null;
        boolean hasUltimos = js1RunTime.getUltimos() != null;
        boolean hasHolidays = js1RunTime.getHolidays() != null;

        if (hasRepeat) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasRepeat=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }
        if (hasPeriods) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasPeriods=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }
        if (hasAts) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasAts=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }
        if (hasDates) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasDates=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }
        if (hasMonthDays) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasMonthDays=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }
        if (hasUltimos) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasUltimos=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }
        if (hasHolidays) {
            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                    "[convert2AdmissionTimeScheme][hasHolidays=true][not implemented yet]" + js1Job.getRunTime().getNodeText());
            return null;
        }

        if (!hasSingleStart && !hasBeginEnd && !hasWeekDays) {
            return null;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[convert2AdmissionTimeScheme][%s]hasSingleStart=%s,hasBeginEnd=%s,hasWeekDays=%s", js1RunTime
                    .getCurrentPath(), hasSingleStart, hasBeginEnd, hasWeekDays));
        }

        List<AdmissionTimePeriod> periods = new ArrayList<>();
        if (hasSingleStart) {
            try {
                TimeHelper th = newTimeHelper(js1RunTime.getSingleStart());
                LocalTime lt = LocalTime.of(th.hours, th.minutes, th.seconds);
                DailyPeriod p = new DailyPeriod(Long.valueOf(lt.toSecondOfDay()));
                p.setDuration(Long.valueOf(DAY_SECONDS));
                periods.add(p);
                return new AdmissionTimeScheme(periods);
            } catch (Exception e) {
                ConverterReport.INSTANCE.addErrorRecord(js1Job.getPath(), "Order Job=" + js1Job.getName()
                        + "[convert2AdmissionTimeScheme][hasSingleStart=true][" + js1Job.getRunTime().getNodeText() + "]" + e.toString(), e);
                return null;
            }
        }

        if (hasBeginEnd) {
            try {
                TimeHelper thBegin = newTimeHelper(getPeriodBegin(js1RunTime.getBegin()));
                TimeHelper thEnd = newTimeHelper(getPeriodEnd(js1RunTime.getEnd()));
                LocalTime lt = LocalTime.of(thBegin.hours, thBegin.minutes, thBegin.seconds);
                DailyPeriod p = new DailyPeriod(Long.valueOf(lt.toSecondOfDay()));
                p.setDuration(Long.valueOf(thEnd.toSeconds() - thBegin.toSeconds()));
                periods.add(p);
                return new AdmissionTimeScheme(periods);
            } catch (Throwable e) {
                ConverterReport.INSTANCE.addErrorRecord(js1Job.getPath(), "Order Job=" + js1Job.getName()
                        + "[convert2AdmissionTimeScheme][hasSingleStart=true][" + js1Job.getRunTime().getNodeText() + "]" + e.toString(), e);
                return null;
            }
        }

        if (hasWeekDays) {
            Map<com.sos.js7.converter.js1.common.runtime.Day, List<String>> starts = new HashMap<>();
            List<String> lastStartPeriods = null;
            for (com.sos.js7.converter.js1.common.runtime.WeekDays wd : js1RunTime.getWeekDays()) {
                if (wd.getDays() != null) {
                    for (com.sos.js7.converter.js1.common.runtime.Day d : wd.getDays()) {
                        List<Integer> days = d.getDays();
                        if (days != null && days.size() > 0) {
                            if (d.getPeriods() != null) {
                                List<String> startPeriods = new ArrayList<>();
                                for (com.sos.js7.converter.js1.common.runtime.Period p : d.getPeriods()) {
                                    if (p.getRepeat() != null || p.getAbsoluteRepeat() != null) {
                                        ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                                                "[convert2AdmissionTimeScheme][hasWeekDays=true][period repeat or absolute_repeat][not implemented yet]"
                                                        + js1Job.getRunTime().getNodeText());
                                        return null;
                                    }

                                    String startPeriod = null;
                                    if (p.getSingleStart() == null) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(normalizeTime(getPeriodBegin(p.getBegin())));
                                        sb.append("=");
                                        sb.append(normalizeTime(getPeriodEnd(p.getEnd())));
                                        startPeriod = sb.toString();
                                    } else {
                                        startPeriod = normalizeTime(p.getSingleStart());
                                    }
                                    startPeriods.add(startPeriod);
                                }
                                if (startPeriods.size() > 0) {
                                    if (lastStartPeriods != null) {
                                        if (lastStartPeriods.size() != startPeriods.size()) {
                                            ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                                                    "[convert2AdmissionTimeScheme][hasWeekDays=true][multiple periods][not implemented yet]" + js1Job
                                                            .getRunTime().getNodeText());
                                            return null;
                                        }

                                        for (String s : startPeriods) {
                                            if (!lastStartPeriods.contains(s)) {
                                                ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Order Job=" + js1Job.getName(),
                                                        "[convert2AdmissionTimeScheme][hasWeekDays=true][different periods][not implemented yet]"
                                                                + js1Job.getRunTime().getNodeText());
                                                return null;
                                            }
                                        }

                                    }
                                    starts.put(d, startPeriods);
                                    lastStartPeriods = startPeriods;
                                }
                            }

                        }
                    }
                }
            }
            if (starts.size() > 0) {
                List<Integer> l = starts.entrySet().stream().map(e -> e.getKey().getDays()).flatMap(e -> e.stream()).distinct().collect(Collectors
                        .toList());
                List<Integer> weekDays = convertWeekDays(l);
                List<String> wdPeriods = starts.entrySet().stream().map(e -> e.getValue()).findFirst().get();
                for (Integer weekDay : weekDays) {
                    for (String singleStart : wdPeriods) {
                        String[] arr = singleStart.split("=");
                        boolean isSingleStart = arr.length == 1;
                        TimeHelper thBegin = isSingleStart ? newTimeHelper(singleStart) : newTimeHelper(arr[0]);
                        LocalTime lt = LocalTime.of(thBegin.hours, thBegin.minutes, thBegin.seconds);
                        int secondsOfDay = lt.toSecondOfDay();

                        WeekdayPeriod p = new WeekdayPeriod(weekdayToSeconds(weekDay, secondsOfDay));
                        if (isSingleStart) {
                            p.setDuration(Long.valueOf(DAY_SECONDS));
                        } else {
                            TimeHelper thEnd = newTimeHelper(arr[1]);
                            p.setDuration(Long.valueOf(thEnd.toSeconds() - thBegin.toSeconds()));
                        }
                        periods.add(p);
                    }
                }
            }

        }
        return periods.size() > 0 ? new AdmissionTimeScheme(periods) : null;
    }

    public static TimeHelper newTimeHelper(String val) {
        return new JS7RunTimeConverter().new TimeHelper(val);
    }

    public static Long weekdayToSeconds(int day, LocalTime time) {
        return weekdayToSeconds(day, time.toSecondOfDay());
    }

    public static Long weekdayToSeconds(int day, int secondsOfDay) {
        return Long.valueOf(((day - 1) * DAY_SECONDS) + secondsOfDay);
    }

    public static Schedule toSchedule(JS12JS7Converter js7Converter, Path js7SchedulePath,
            com.sos.js7.converter.js1.common.runtime.Schedule js1Schedule, String timeZone, List<String> workflowNames) {
        if (js1Schedule == null) {
            return null;
        }
        return toSchedule(js7Converter, js7SchedulePath, js1Schedule.getRunTime(), timeZone, workflowNames);
    }

    private static Schedule toSchedule(JS12JS7Converter js7Converter, Path js7SchedulePath,
            com.sos.js7.converter.js1.common.runtime.RunTime js1RunTime, String timeZone, List<String> workflowNames) {

        if (js1RunTime == null || !js1RunTime.isConvertableWithoutCalendars()) {
            if (LOGGER.isDebugEnabled()) {
                if (js1RunTime == null) {
                    LOGGER.debug(String.format("[toSchedule][skip][js1RunTime is null]workflows=%s", workflowNames));
                } else {
                    boolean hasSingleStart = js1RunTime.getSingleStart() != null;
                    boolean hasRepeat = js1RunTime.getRepeat() != null;
                    boolean hasPeriods = js1RunTime.getPeriods() != null;
                    boolean hasAts = js1RunTime.getAts() != null;
                    boolean hasDates = js1RunTime.getDates() != null;
                    boolean hasWeekDays = js1RunTime.getWeekDays() != null;
                    boolean hasMonths = js1RunTime.getMonths() != null;
                    boolean hasMonthDays = js1RunTime.getMonthDays() != null;
                    boolean hasUltimos = js1RunTime.getUltimos() != null;
                    boolean hasHolidays = js1RunTime.getHolidays() != null;
                    LOGGER.debug(String.format(
                            "[toSchedule][%s][workflows=%s]hasRepeat=%s,hasSingleStart=%s,hasPeriods=%s,hasAts=%s,hasDates=%s, hasWeekDays=%s,hasMonths=%s,hasMonthDays=%s,hasUltimos=%s,hasHolidays=%s",
                            js1RunTime.getCurrentPath(), workflowNames, hasRepeat, hasSingleStart, hasPeriods, hasAts, hasDates, hasWeekDays,
                            hasMonths, hasMonthDays, hasUltimos, hasHolidays));
                }
            }
            return null;
        }

        boolean hasSingleStart = js1RunTime.getSingleStart() != null;
        boolean hasRepeat = js1RunTime.getRepeat() != null;
        boolean hasPeriods = js1RunTime.getPeriods() != null;
        boolean hasAts = js1RunTime.getAts() != null;
        boolean hasDates = js1RunTime.getDates() != null;
        boolean hasWeekDays = js1RunTime.getWeekDays() != null;
        boolean hasMonths = js1RunTime.getMonths() != null;
        boolean hasMonthDays = js1RunTime.getMonthDays() != null;
        boolean hasUltimos = js1RunTime.getUltimos() != null;
        boolean hasHolidays = js1RunTime.getHolidays() != null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "[convert2schedule][%s][workflows=%s]hasRepeat=%s,hasSingleStart=%s,hasPeriods=%s,hasAts=%s,hasDates=%s, hasWeekDays=%s,hasMonths=%s,hasMonthDays=%s,hasUltimos=%s,hasHolidays=%s",
                    js1RunTime.getCurrentPath(), workflowNames, hasRepeat, hasSingleStart, hasPeriods, hasAts, hasDates, hasWeekDays, hasMonths,
                    hasMonthDays, hasUltimos, hasHolidays));
        }
        List<AssignedCalendars> working = new ArrayList<>();
        List<AssignedNonWorkingDayCalendars> nonWorking = new ArrayList<>();

        WhenHolidayType whenHolidayType = getWhenHolidayType(js1RunTime.getWhenHoliday());
        if (hasSingleStart) {
            convertSingleStart(working, timeZone, js1RunTime.getSingleStart(), whenHolidayType);
        }
        if (hasRepeat) {
            convertRepeat(working, timeZone, js1RunTime.getRepeat(), js1RunTime.getBegin(), js1RunTime.getEnd(), whenHolidayType);
        }
        if (hasPeriods) {
            convertPeriods(working, timeZone, js1RunTime.getPeriods());
        }
        if (hasAts) {
            convertAts(working, timeZone, js1RunTime.getAts());
        }
        if (hasDates) {
            convertDates(working, timeZone, js1RunTime.getDates());
        }
        if (hasWeekDays) {
            for (com.sos.js7.converter.js1.common.runtime.WeekDays wd : js1RunTime.getWeekDays()) {
                convertDays(working, timeZone, wd.getDays(), DaysType.WEEKDAYS);
            }
        }

        if (hasMonths) {
            convertMonths(js7SchedulePath, working, timeZone, js1RunTime.getMonths());
        }
        if (hasMonthDays) {
            for (com.sos.js7.converter.js1.common.runtime.MonthDays wd : js1RunTime.getMonthDays()) {
                convertDays(working, timeZone, wd.getDays(), DaysType.MONTHDAYS);
                convertWeekDays(working, timeZone, wd.getWeekDays());
            }
        }
        if (hasUltimos) {
            convertUltimos(working, timeZone, js1RunTime.getUltimos());
        }
        if (hasHolidays) {
            // js1RunTime.getHolidays().

            // AssignedNonWorkingDayCalendars nwc = new AssignedNonWorkingDayCalendars();

            // String name = getNonWorkingDayCalendarName(js1RunTime.getHolidays());
            // nwc.setCalendarName(JS7ConverterHelper.getJS7ObjectName(name));
            // nonWorking.add(nwc);
        }

        // try to reduce n-calendars if all periods are equal
        working = tryReduceWorkingCalendars(working);
        Schedule schedule = null;
        if (working.size() > 0 || nonWorking.size() > 0) {
            if (hasHolidays) {
                for (AssignedCalendars c : working) {
                    trySetNonWorkingDayCalendarAsExcluded(js7Converter, js7SchedulePath, js1RunTime, c);
                }
            }

            schedule = new Schedule();
            schedule.setCalendars(working.size() == 0 ? null : working);
            schedule.setNonWorkingDayCalendars(nonWorking.size() == 0 ? null : nonWorking);
            schedule.setWorkflowNames(workflowNames);
            schedule.setPlanOrderAutomatically(JS12JS7Converter.CONFIG.getScheduleConfig().planOrders());
            schedule.setSubmitOrderToControllerWhenPlanned(JS12JS7Converter.CONFIG.getScheduleConfig().submitOrders());
        } else {
            ConverterReport.INSTANCE.addWarningRecord(js7SchedulePath, "Schedule was not converted", "Please check/create manually:" + js1RunTime
                    .getNodeText());
        }
        return schedule;
    }

    private static List<AssignedCalendars> tryReduceWorkingCalendars(List<AssignedCalendars> working) {
        if (working.size() == 0) {
            return working;
        }

        Map<Period, Frequencies> grouped = new HashMap<>();
        for (AssignedCalendars cal : working) {
            if (cal.getPeriods() == null) {
                continue;
            }
            // reduce periods
            HashSet<Period> hp = new HashSet<>(cal.getPeriods());
            cal.setPeriods(hp.stream().collect(Collectors.toList()));

            for (Period p : cal.getPeriods()) {
                grouped.put(p, mergeIncludes(grouped, p, cal.getIncludes()));
            }
        }

        AssignedCalendars convertedCal = working.get(0);
        working.clear();
        grouped.entrySet().forEach(e -> {
            AssignedCalendars cal = new AssignedCalendars();
            cal.setCalendarName(convertedCal.getCalendarName());
            cal.setCalendarPath(convertedCal.getCalendarPath());
            cal.setTimeZone(convertedCal.getTimeZone());

            cal.setPeriods(new ArrayList<>());
            cal.getPeriods().add(e.getKey());

            cal.setIncludes(e.getValue());

            working.add(cal);
        });

        return working;
    }

    private static Frequencies mergeIncludes(Map<Period, Frequencies> grouped, Period p, Frequencies current) {
        Frequencies stored = grouped.get(p);
        if (stored == null) {
            return current;
        }

        HashSet<String> dates = new HashSet<>();
        if (stored.getDates() != null) {
            dates.addAll(stored.getDates());
        }
        HashSet<WeekDays> weekdays = new HashSet<>();
        if (stored.getWeekdays() != null) {
            weekdays.addAll(stored.getWeekdays());
        }
        HashSet<MonthDays> monthdays = new HashSet<>();
        if (stored.getMonthdays() != null) {
            monthdays.addAll(stored.getMonthdays());
        }
        HashSet<MonthDays> ultimos = new HashSet<>();
        if (stored.getUltimos() != null) {
            ultimos.addAll(stored.getUltimos());
        }
        HashSet<Months> months = new HashSet<>();
        if (stored.getMonths() != null) {
            months.addAll(stored.getMonths());
        }

        if (!SOSCollection.isEmpty(current.getDates())) {
            dates.addAll(current.getDates());

            stored.setDates(dates.stream().sorted().collect(Collectors.toList()));
        }
        if (!SOSCollection.isEmpty(current.getWeekdays())) {
            weekdays.addAll(current.getWeekdays());

            WeekDays wd = new WeekDays();
            // wd.setDays(weekdays.stream().flatMap(w -> w.getDays().stream()).sorted().collect(Collectors.toList()));
            wd.setDays(weekdays.stream().flatMap(w -> Optional.ofNullable(w.getDays()).orElseGet(Collections::emptyList).stream()).sorted().collect(
                    Collectors.toList()));

            stored.setWeekdays(new ArrayList<>());
            stored.getWeekdays().add(wd);
        }
        if (!SOSCollection.isEmpty(current.getMonthdays())) {
            monthdays.addAll(current.getMonthdays());

            // md.setDays(monthdays.stream().flatMap(w -> Optional.ofNullable(w.getDays()).orElseGet(Collections::emptyList).stream()).sorted().collect(
            // Collectors.toList()));

            Set<Integer> days = new HashSet<>();
            Set<WeeklyDay> weeklyDays = new HashSet<>();
            for (MonthDays mdd : monthdays) {
                if (mdd.getDays() != null) {
                    days.addAll(mdd.getDays());
                }
                if (mdd.getWeeklyDays() != null) {
                    weeklyDays.addAll(mdd.getWeeklyDays());
                }
            }

            List<MonthDays> monthDays = new ArrayList<>();
            if (days.size() > 0) {
                MonthDays md = new MonthDays();
                md.setDays(days.stream().sorted().collect(Collectors.toList()));
                monthDays.add(md);
            }
            if (weeklyDays.size() > 0) {
                MonthDays md = new MonthDays();
                md.setWeeklyDays(weeklyDays.stream().collect(Collectors.toList()));
                monthDays.add(md);
            }

            stored.setMonthdays(monthDays);
        }
        if (!SOSCollection.isEmpty(current.getUltimos())) {
            ultimos.addAll(current.getUltimos());

            stored.setUltimos(ultimos.stream().collect(Collectors.toList()));
        }
        if (!SOSCollection.isEmpty(current.getMonths())) {
            months.addAll(current.getMonths());

            stored.setMonths(months.stream().collect(Collectors.toList()));
        }

        return stored;
    }

    private static void convertMonths(Path js7SchedulePath, List<AssignedCalendars> working, String timeZone,
            List<com.sos.js7.converter.js1.common.runtime.Month> months) {
        if (SOSCollection.isEmpty(months)) {
            return;
        }

        List<AssignedCalendars> cl = new ArrayList<>();
        AssignedCalendars c = createAssignedCalendar(timeZone);
        c.setPeriods(new ArrayList<>());

        Months m = new Months();
        m.setMonths(new ArrayList<>());

        for (com.sos.js7.converter.js1.common.runtime.Month month : months) {
            if (SOSString.isEmpty(month.getMonth()) && SOSCollection.isEmpty(month.getMonthMonths())) {
                continue;
            }
            if (SOSString.isEmpty(month.getMonth())) {
                m.getMonths().addAll(toMonthNumbers(Arrays.asList(month.getMonthMonths())));
            } else {
                m.getMonths().addAll(toMonthNumbers(List.of(month.getMonth())));
            }

            if (!SOSCollection.isEmpty(month.getPeriods())) {
                c.getPeriods().addAll(convertPeriods(month.getPeriods()));
            }
            if (!SOSCollection.isEmpty(month.getWeekDays())) {
                for (com.sos.js7.converter.js1.common.runtime.WeekDays wd : month.getWeekDays()) {
                    convertDays(cl, timeZone, wd.getDays(), DaysType.WEEKDAYS);
                }
            }
            if (!SOSCollection.isEmpty(month.getMonthDays())) {
                for (com.sos.js7.converter.js1.common.runtime.MonthDays wd : month.getMonthDays()) {
                    convertDays(cl, timeZone, wd.getDays(), DaysType.MONTHDAYS);
                    convertWeekDays(cl, timeZone, wd.getWeekDays());
                }
            }
            if (!SOSCollection.isEmpty(month.getUltimos())) {
                convertUltimos(cl, timeZone, month.getUltimos());
            }

        }

        HashSet<WeekDays> weekdays = new HashSet<>();
        HashSet<MonthDays> monthdays = new HashSet<>();
        HashSet<MonthDays> ultimos = new HashSet<>();
        // HashSet<Months> months = new HashSet<>();

        for (AssignedCalendars ac : cl) {
            if (!SOSCollection.isEmpty(ac.getPeriods())) {
                c.getPeriods().addAll(ac.getPeriods());
            }
            if (ac.getIncludes() != null) {
                if (!SOSCollection.isEmpty(ac.getIncludes().getWeekdays())) {
                    weekdays.addAll(ac.getIncludes().getWeekdays());
                }
                if (!SOSCollection.isEmpty(ac.getIncludes().getMonthdays())) {
                    monthdays.addAll(ac.getIncludes().getMonthdays());
                }
                if (!SOSCollection.isEmpty(ac.getIncludes().getUltimos())) {
                    ultimos.addAll(ac.getIncludes().getUltimos());
                }
            }
        }
        if (weekdays.size() > 0) {
            m.setWeekdays(weekdays.stream().collect(Collectors.toList()));
        }
        if (monthdays.size() > 0) {
            m.setMonthdays(monthdays.stream().collect(Collectors.toList()));
        }
        if (ultimos.size() > 0) {
            m.setUltimos(ultimos.stream().collect(Collectors.toList()));
        }
        // remove duplicated and sort
        m.setMonths(new HashSet<>(m.getMonths()).stream().sorted().collect(Collectors.toList()));
        c.getIncludes().setMonths(Collections.singletonList(m));
        working.add(c);
    }

    private static List<Integer> toMonthNumbers(List<String> months) {
        // java.time.Month enum: // 1 = JANUARY, ..., 12 = DECEMBER
        return months.stream().map(x -> java.time.Month.valueOf(x.toUpperCase()).getValue()).collect(Collectors.toList());
    }

    private static void convertUltimos(List<AssignedCalendars> working, String timeZone,
            List<com.sos.js7.converter.js1.common.runtime.Ultimos> ultimos) {
        if (ultimos == null || SOSCollection.isEmpty(ultimos)) {
            return;
        }
        for (com.sos.js7.converter.js1.common.runtime.Ultimos wd : ultimos) {
            convertDays(working, timeZone, wd.getDays(), DaysType.ULTIMOS);
        }
    }

    private static void convertSingleStart(List<AssignedCalendars> working, String timeZone, String singleStart, WhenHolidayType whenHolidayType) {
        if (singleStart == null) {
            return;
        }

        AssignedCalendars c = createAssignedCalendar(timeZone);
        WeekDays wds = new WeekDays();
        wds.setDays(JS7ConverterHelper.allWeekDays());
        c.getIncludes().setWeekdays(Collections.singletonList(wds));

        List<Period> periods = new ArrayList<>();
        Period period = new Period();
        period.setWhenHoliday(whenHolidayType);
        period.setSingleStart(normalizeTime(singleStart));
        periods.add(period);
        c.setPeriods(periods);

        working.add(c);
    }

    private static void trySetNonWorkingDayCalendarAsExcluded(JS12JS7Converter js7Converter, Path js7SchedulePath,
            com.sos.js7.converter.js1.common.runtime.RunTime js1RunTime, AssignedCalendars c) {
        if (!js1RunTime.hasHolidaysOrNonWorkingDayCalendars()) {
            return;
        }

        List<com.sos.inventory.model.calendar.Calendar> nwCalendars = JS7CalendarConverter.convertToNonWorkingDayCalendars(js7Converter, js1RunTime
                .getCurrentPath(), js7SchedulePath, js1RunTime.getHolidays(), js1RunTime.getNonWorkingDayCalendars(), null);
        if (!SOSCollection.isEmpty(nwCalendars)) {
            if (c.getExcludes() == null) {
                c.setExcludes(new Frequencies());
            }
            Set<String> set = new HashSet<>(); // remove duplicates
            for (com.sos.inventory.model.calendar.Calendar nw : nwCalendars) {
                set.add(nw.getName());
            }
            c.getExcludes().setNonWorkingDayCalendars(set);
        }
    }

    private static void convertRepeat(List<AssignedCalendars> working, String timeZone, String repeat, String begin, String end,
            WhenHolidayType whenHolidayType) {
        if (repeat == null) {
            return;
        }

        AssignedCalendars c = createAssignedCalendar(timeZone);
        WeekDays wds = new WeekDays();
        wds.setDays(JS7ConverterHelper.allWeekDays());
        c.getIncludes().setWeekdays(Collections.singletonList(wds));

        List<Period> periods = new ArrayList<>();
        Period period = new Period();
        period.setWhenHoliday(whenHolidayType);
        period.setRepeat(normalizeTime(repeat));
        period.setBegin(normalizeTime(getPeriodBegin(begin)));
        period.setEnd(normalizeTime(getPeriodEnd(end)));
        periods.add(period);
        c.setPeriods(periods);
        working.add(c);
    }

    private static void convertPeriods(List<AssignedCalendars> working, String timeZone,
            List<com.sos.js7.converter.js1.common.runtime.Period> runTimeDays) {
        if (runTimeDays == null || SOSCollection.isEmpty(runTimeDays)) {
            return;
        }

        AssignedCalendars c = createAssignedCalendar(timeZone);
        WeekDays wds = new WeekDays();
        wds.setDays(JS7ConverterHelper.allWeekDays());
        c.getIncludes().setWeekdays(Collections.singletonList(wds));
        c.setPeriods(convertPeriods(runTimeDays));
        working.add(c);
    }

    private static void convertAts(List<AssignedCalendars> working, String timeZone, List<com.sos.js7.converter.js1.common.runtime.At> runTimeDays) {
        if (runTimeDays == null || SOSCollection.isEmpty(runTimeDays)) {
            return;
        }

        // <at at="2006-03-24 12:00"/>
        Map<String, List<String>> starts = new HashMap<>();
        for (com.sos.js7.converter.js1.common.runtime.At d : runTimeDays) {
            String at = d.getAt();
            if (SOSString.isEmpty(at)) {
                continue;
            }
            String[] arr = at.split(" ");
            if (arr.length < 2) {
                continue;
            }
            String day = arr[0];
            String singleStart = normalizeTime(arr[1]);

            List<String> days = starts.get(singleStart);
            if (days == null) {
                days = new ArrayList<>();
            }
            if (!days.contains(day)) {
                days.add(day);
            }
            starts.put(singleStart, days);
        }

        for (Map.Entry<String, List<String>> e : starts.entrySet()) {
            AssignedCalendars c = createAssignedCalendar(timeZone);
            c.getIncludes().setDates(e.getValue());

            Period p = new Period();
            p.setSingleStart(e.getKey());
            p.setWhenHoliday(WhenHolidayType.SUPPRESS);
            c.setPeriods(Collections.singletonList(p));

            working.add(c);
        }
    }

    private static void convertDates(List<AssignedCalendars> working, String timeZone,
            List<com.sos.js7.converter.js1.common.runtime.Date> runTimeDates) {
        if (runTimeDates == null || SOSCollection.isEmpty(runTimeDates)) {
            return;
        }

        List<com.sos.js7.converter.js1.common.runtime.Period> lastPeriods = null;
        boolean equals = true;
        List<String> dates = new ArrayList<>();
        for (com.sos.js7.converter.js1.common.runtime.Date d : runTimeDates) {
            String date = d.getDate();
            if (!SOSString.isEmpty(date)) {
                dates.add(date);

                if (lastPeriods != null) {
                    equals = periodsEquals(lastPeriods, d.getPeriods());
                }

                if (!equals) {
                    break;
                }
                lastPeriods = d.getPeriods();
            }
        }
        if (equals) {
            AssignedCalendars c = createAssignedCalendar(timeZone);
            c.getIncludes().setDates(dates);
            c.setPeriods(convertPeriods(lastPeriods));

            working.add(c);
        } else {
            for (com.sos.js7.converter.js1.common.runtime.Date d : runTimeDates) {
                String date = d.getDate();
                if (!SOSString.isEmpty(date)) {
                    AssignedCalendars c = createAssignedCalendar(timeZone);
                    c.getIncludes().setDates(Collections.singletonList(date));
                    c.setPeriods(convertPeriods(d.getPeriods()));
                    working.add(c);
                }
            }
        }
    }

    private static AssignedCalendars createAssignedCalendar(String timeZone) {
        AssignedCalendars c = new AssignedCalendars();
        c.setCalendarName(JS12JS7Converter.CONFIG.getScheduleConfig().getDefaultWorkingDayCalendarName());
        c.setTimeZone(timeZone);
        if (c.getTimeZone() == null) {
            c.setTimeZone(JS12JS7Converter.CONFIG.getScheduleConfig().getDefaultTimeZone());
        }
        c.setIncludes(new Frequencies());
        return c;
    }

    private static void convertDays(List<AssignedCalendars> working, String timeZone, List<com.sos.js7.converter.js1.common.runtime.Day> runTimeDays,
            DaysType daysType) {
        if (runTimeDays == null || SOSCollection.isEmpty(runTimeDays)) {
            return;
        }
        for (com.sos.js7.converter.js1.common.runtime.Day d : runTimeDays) {
            List<Integer> days = d.getDays();
            if (days != null && days.size() > 0) {
                AssignedCalendars c = createAssignedCalendar(timeZone);
                switch (daysType) {
                case WEEKDAYS:
                    WeekDays wds = new WeekDays();
                    wds.setDays(convertWeekDays(days));

                    c.getIncludes().setWeekdays(Collections.singletonList(wds));
                    break;
                case MONTHDAYS:
                    MonthDays md = new MonthDays();
                    md.setDays(days);

                    c.getIncludes().setMonthdays(Collections.singletonList(md));
                    break;
                case ULTIMOS:
                    MonthDays umd = new MonthDays();
                    umd.setDays(days);

                    c.getIncludes().setUltimos(Collections.singletonList(umd));
                    break;
                }

                c.setPeriods(convertPeriods(d.getPeriods()));
                working.add(c);
            }
        }
    }

    private static void convertWeekDays(List<AssignedCalendars> working, String timeZone,
            List<com.sos.js7.converter.js1.common.runtime.WeekDay> runTimeDays) {
        if (runTimeDays == null || SOSCollection.isEmpty(runTimeDays)) {
            return;
        }
        for (com.sos.js7.converter.js1.common.runtime.WeekDay d : runTimeDays) {
            if (!SOSString.isEmpty(d.getDay()) && d.getWhich() != null) {
                AssignedCalendars c = createAssignedCalendar(timeZone);

                WeeklyDay wDay = new WeeklyDay();
                wDay.setDay(JS7ConverterHelper.getScheduleDay0to6(d.getDay()));
                wDay.setWeekOfMonth(d.getWhich());

                MonthDays md = new MonthDays();
                md.setWeeklyDays(Collections.singletonList(wDay));
                c.getIncludes().setMonthdays(Collections.singletonList(md));
                c.setPeriods(convertPeriods(d.getPeriods()));
                working.add(c);
            }
        }
    }

    private static boolean periodsEquals(List<com.sos.js7.converter.js1.common.runtime.Period> js1Periods1,
            List<com.sos.js7.converter.js1.common.runtime.Period> js1Periods2) {
        try {
            if (js1Periods1 == null && js1Periods2 == null) {
                return true;
            }
            if (js1Periods1 != null && js1Periods2 == null) {
                return false;
            }
            if (js1Periods1 == null && js1Periods2 != null) {
                return false;
            }
            if (js1Periods1.size() != js1Periods2.size()) {
                return false;
            }

            List<String> p1 = js1Periods1.stream().map(e -> e.toString()).sorted().collect(Collectors.toList());
            List<String> p2 = js1Periods2.stream().map(e -> e.toString()).sorted().collect(Collectors.toList());

            for (int i = 0; i < p1.size(); i++) {
                if (!p1.get(i).equals(p2.get(i))) {
                    return false;
                }
            }

            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static List<Period> convertPeriods(List<com.sos.js7.converter.js1.common.runtime.Period> js1Periods) {
        List<Period> periods = new ArrayList<>();
        if (js1Periods != null) {
            for (com.sos.js7.converter.js1.common.runtime.Period p : js1Periods) {
                if (!SOSString.isEmpty(p.getSingleStart())) {
                    Period period = new Period();
                    period.setWhenHoliday(getWhenHolidayType(p.getWhenHoliday()));
                    period.setSingleStart(normalizeTime(p.getSingleStart()));
                    if (!periods.contains(period)) {
                        periods.add(period);
                    }
                } else if (!SOSString.isEmpty(p.getAbsoluteRepeat())) {
                    Period period = new Period();
                    period.setWhenHoliday(getWhenHolidayType(p.getWhenHoliday()));
                    period.setRepeat(normalizeTime(p.getAbsoluteRepeat()));
                    period.setBegin(normalizeTime(getPeriodBegin(p.getBegin())));
                    period.setEnd(normalizeTime(getPeriodEnd(p.getEnd())));
                    period.setAbsoluteRepeat(true);
                    if (!periods.contains(period)) {
                        periods.add(period);
                    }
                } else if (!SOSString.isEmpty(p.getRepeat()) && !p.getRepeat().equals("0")) {
                    Period period = new Period();
                    period.setWhenHoliday(getWhenHolidayType(p.getWhenHoliday()));
                    period.setRepeat(normalizeTime(p.getRepeat()));
                    period.setBegin(normalizeTime(getPeriodBegin(p.getBegin())));
                    period.setEnd(normalizeTime(getPeriodEnd(p.getEnd())));
                    period.setAbsoluteRepeat(false);
                    if (!periods.contains(period)) {
                        periods.add(period);
                    }
                }
            }
        }
        if (periods.size() == 0) {
            Period period = new Period();
            period.setWhenHoliday(WhenHolidayType.SUPPRESS);
            period.setSingleStart("00:00:00");
            periods.add(period);
        }
        return periods;
    }

    private static String getPeriodBegin(String val) {
        return SOSString.isEmpty(val) ? "00:00:00" : val;
    }

    private static String getPeriodEnd(String val) {
        return SOSString.isEmpty(val) ? "24:00:00" : val;
    }

    private static List<Integer> convertWeekDays(List<Integer> l) {
        if (l == null) {
            return l;
        }
        return l.stream().map(e -> {
            if (e != null && e.intValue() == 7) {
                return Integer.valueOf(0);
            }
            return e;
        }).sorted().collect(Collectors.toList());
    }

    private static WhenHolidayType getWhenHolidayType(String whenHoliday) {
        if (SOSString.isEmpty(whenHoliday)) {
            return WhenHolidayType.SUPPRESS;
        }
        switch (whenHoliday.toLowerCase()) {
        case "ignore_holiday":
            return WhenHolidayType.IGNORE;
        case "previous_non_holiday":
            return WhenHolidayType.PREVIOUSNONWORKINGDAY;
        case "next_non_holiday":
            return WhenHolidayType.NEXTNONWORKINGDAY;
        default:
            return WhenHolidayType.SUPPRESS;
        }
    }

    public static String normalizeTime(String val) {
        int idx = val.indexOf(":");
        if (idx > -1) {
            String[] arr = val.split(":");
            switch (arr.length) {
            case 1:
                return JS7ConverterHelper.toTimePart(val) + ":00:00";
            case 2:
                return JS7ConverterHelper.toTimePart(arr[0]) + ":" + JS7ConverterHelper.toTimePart(arr[1]) + ":00";
            default:
                return JS7ConverterHelper.toTimePart(arr[0]) + ":" + JS7ConverterHelper.toTimePart(arr[1]) + ":" + JS7ConverterHelper.toTimePart(
                        arr[2]);
            }
        } else {
            String v = LocalTime.MIN.plusSeconds(Long.parseLong(val)).toString();
            String[] arr = v.split(":");
            return arr.length == 2 ? v + ":00" : v;
        }
    }

    public class TimeHelper {

        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        private TimeHelper(String val) {
            String n = normalizeTime(val);
            String[] arr = n.split(":");

            this.hours = Integer.parseInt(arr[0]);
            this.minutes = Integer.parseInt(arr[1]);
            this.seconds = Integer.parseInt(arr[2]);
        }

        public int toSeconds() {
            return this.hours * 60 * 60 + this.minutes * 60 + this.seconds;
        }
    }

}
