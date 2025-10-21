package com.sos.js7.converter.commons.wokflow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Months;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WeeklyDay;
import com.sos.inventory.model.instruction.schedule.Continuous;
import com.sos.inventory.model.instruction.schedule.Repeat;
import com.sos.inventory.model.instruction.schedule.Scheme;
import com.sos.inventory.model.instruction.schedule.Ticking;
import com.sos.inventory.model.job.AdmissionRestrictedScheme;
import com.sos.inventory.model.job.AdmissionRestrictionType;
import com.sos.inventory.model.job.AdmissionTimePeriod;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.DailyPeriod;
import com.sos.inventory.model.job.MonthRestriction;
import com.sos.inventory.model.job.MonthlyDatePeriod;
import com.sos.inventory.model.job.MonthlyLastDatePeriod;
import com.sos.inventory.model.job.MonthlyWeekdayPeriod;
import com.sos.inventory.model.job.SpecificDatePeriod;
import com.sos.inventory.model.job.WeekdayPeriod;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JS7ScheduleToWorkflowAdmissionTimes {

    public static Scheme toScheme(Period schedulePeriod, Frequencies calendarIncludes) {
        AdmissionTimeScheme ats = new AdmissionTimeScheme();

        List<AdmissionTimePeriod> periods = toPeriods(ats, schedulePeriod, calendarIncludes);
        if (SOSCollection.isEmpty(periods) && SOSCollection.isEmpty(ats.getRestrictedSchemes())) {
            return null;
        }
        ats.setPeriods(periods);
        return new Scheme(getRepeat(schedulePeriod), ats);
    }

    private static Repeat getRepeat(Period schedulePeriod) {
        // Periodic repeat = new Periodic();
        // repeat.setPeriod(86_400L);// 1d
        // repeat.setOffsets(List.of(SOSDate.getTimeAsSeconds(schedulePeriod.getRepeat())));

        // js1 repeat = continue

        Repeat repeat = null;
        if (schedulePeriod.getAbsoluteRepeat()) {
            Ticking ticking = new Ticking();
            ticking.setInterval(SOSDate.getTimeAsSeconds(schedulePeriod.getRepeat()));
            repeat = ticking;
        } else {
            Continuous continuous = new Continuous();
            continuous.setPause(SOSDate.getTimeAsSeconds(schedulePeriod.getRepeat()));
            repeat = continuous;
        }

        return repeat;
    }

    private static List<AdmissionTimePeriod> toPeriods(AdmissionTimeScheme ats, Period schedulePeriod, Frequencies calendarIncludes) {
        if (schedulePeriod == null || calendarIncludes == null) {
            return null;
        }

        long begin = SOSDate.getTimeAsSeconds(schedulePeriod.getBegin());
        long duration = SOSDate.getDaySpanInSeconds(begin, SOSDate.getTimeAsSeconds(schedulePeriod.getEnd()));

        List<AdmissionTimePeriod> result = new ArrayList<>();

        List<AdmissionTimePeriod> r = convertSpecificDates(schedulePeriod.getBegin(), duration, calendarIncludes.getDates());
        if (!SOSCollection.isEmpty(r)) {
            result.addAll(r);
        }

        r = convertMonthdays(begin, duration, calendarIncludes.getMonthdays());
        if (!SOSCollection.isEmpty(r)) {
            result.addAll(r);
        }

        r = convertMonths(ats, begin, duration, calendarIncludes.getMonths());
        if (!SOSCollection.isEmpty(r)) {
            result.addAll(r);
        }

        r = convertWeekdays(begin, duration, calendarIncludes.getWeekdays());
        if (!SOSCollection.isEmpty(r)) {
            result.addAll(r);
        }

        r = convertUltimos(begin, duration, calendarIncludes.getUltimos());
        if (!SOSCollection.isEmpty(r)) {
            result.addAll(r);
        }

        return result;
    }

    private static List<AdmissionTimePeriod> convertSpecificDates(String begin, long duration, List<String> dates) {
        if (SOSCollection.isEmpty(dates)) {
            return null;
        }

        List<AdmissionTimePeriod> result = new ArrayList<>();
        for (String date : dates) {
            SpecificDatePeriod p = new SpecificDatePeriod();
            p.setSecondsSinceLocalEpoch(JS7WorkflowTimesCalculator.getSecondsSinceLocalEpoch(date, begin));
            p.setDuration(duration);

            result.add(p);
        }
        result.sort(Comparator.comparingLong(e -> ((SpecificDatePeriod) e).getSecondsSinceLocalEpoch()));

        return result;
    }

    private static List<AdmissionTimePeriod> convertMonthdays(long begin, long duration, List<MonthDays> monthDays) {
        if (SOSCollection.isEmpty(monthDays)) {
            return null;
        }

        List<AdmissionTimePeriod> result = new ArrayList<>();
        for (MonthDays md : monthDays) {
            if (!SOSCollection.isEmpty(md.getDays())) {
                for (Integer day : md.getDays()) {
                    MonthlyDatePeriod p = new MonthlyDatePeriod();
                    p.setSecondOfMonth(JS7WorkflowTimesCalculator.getSecondOfMonth(JS7ConverterHelper.toDay1to7(day), begin));
                    p.setDuration(duration);

                    result.add(p);
                }
            }
            if (!SOSCollection.isEmpty(md.getWeeklyDays()) && md.getWeeklyDays().size() < 7) {// not every day
                for (WeeklyDay wd : md.getWeeklyDays()) {
                    MonthlyWeekdayPeriod p = new MonthlyWeekdayPeriod();
                    p.setSecondOfWeeks(JS7WorkflowTimesCalculator.getSecondOfWeeks(wd.getWeekOfMonth(), JS7ConverterHelper.toDay1to7(wd.getDay()),
                            begin));
                    p.setDuration(duration);

                    result.add(p);
                }
            }
        }
        return result;
    }

    private static List<AdmissionTimePeriod> convertMonths(AdmissionTimeScheme ats, long begin, long duration, List<Months> months) {
        if (SOSCollection.isEmpty(months)) {
            return null;
        }

        ats.setRestrictedSchemes(new ArrayList<>());

        List<AdmissionTimePeriod> result = new ArrayList<>();
        for (Months ms : months) {
            MonthRestriction mr = null;
            List<AdmissionTimePeriod> mrr = new ArrayList<>();

            if (!SOSCollection.isEmpty(ms.getMonths())) {
                mr = new MonthRestriction(ms.getMonths().stream().collect(Collectors.toSet()), AdmissionRestrictionType.MONTH_RESTRICTION);
            }

            List<AdmissionTimePeriod> r = convertMonthdays(begin, duration, ms.getMonthdays());
            if (!SOSCollection.isEmpty(r)) {
                if (mr == null) {
                    result.addAll(r);
                } else {
                    mrr.addAll(r);
                }
            }

            r = convertWeekdays(begin, duration, ms.getWeekdays());
            if (!SOSCollection.isEmpty(r)) {
                if (mr == null) {
                    result.addAll(r);
                } else {
                    mrr.addAll(r);
                }
            }

            r = convertUltimos(begin, duration, ms.getUltimos());
            if (!SOSCollection.isEmpty(r)) {
                if (mr == null) {
                    result.addAll(r);
                } else {
                    mrr.addAll(r);
                }
            }

            if (mr != null) {
                ats.getRestrictedSchemes().add(new AdmissionRestrictedScheme(mr, mrr));
            }
        }

        if (SOSCollection.isEmpty(ats.getRestrictedSchemes())) {
            ats.setRestrictedSchemes(null);
        }
        return result;
    }

    private static List<AdmissionTimePeriod> convertWeekdays(long begin, long duration, List<WeekDays> weekdays) {
        if (SOSCollection.isEmpty(weekdays)) {
            return null;
        }

        List<AdmissionTimePeriod> result = new ArrayList<>();
        for (WeekDays wd : weekdays) {
            if (wd.getDays().size() == 7) {// every day
                DailyPeriod p = new DailyPeriod();
                p.setSecondOfDay(begin);
                p.setDuration(duration);

                result.add(p);
            } else {
                // wd.getDays = [0, 1, 2, 3, 4, 5, 6]
                // js1 <day day=1 Monday
                for (Integer weekday : wd.getDays()) {
                    WeekdayPeriod p = new WeekdayPeriod();
                    p.setSecondOfWeek(JS7WorkflowTimesCalculator.getSecondOfWeek(JS7ConverterHelper.toDay1to7(weekday), begin));
                    p.setDuration(duration);

                    result.add(p);
                }
            }
        }
        result.sort(Comparator.comparingLong(e -> {
            if (e instanceof WeekdayPeriod) {
                return ((WeekdayPeriod) e).getSecondOfWeek();
            }
            return 0L;
        }));

        return result;
    }

    private static List<AdmissionTimePeriod> convertUltimos(long begin, long duration, List<MonthDays> monthDays) {
        if (SOSCollection.isEmpty(monthDays)) {
            return null;
        }

        List<AdmissionTimePeriod> result = new ArrayList<>();
        for (MonthDays md : monthDays) {
            if (!SOSCollection.isEmpty(md.getDays())) {
                for (Integer day : md.getDays()) {
                    MonthlyLastDatePeriod p = new MonthlyLastDatePeriod();
                    p.setLastSecondOfMonth(JS7WorkflowTimesCalculator.getLastSecondOfMonth(JS7ConverterHelper.toDay1to7(day), begin));
                    p.setDuration(duration);

                    result.add(p);
                }
                result.sort(Comparator.comparingLong(e -> ((MonthlyLastDatePeriod) e).getLastSecondOfMonth()).reversed());
            }

            // js1 ultimos configuration hat nur days (monthdays)
            // if (!SOSCollection.isEmpty(md.getWeeklyDays()) && md.getWeeklyDays().size() < 7) {// not every day
            // for (WeeklyDay wd : md.getWeeklyDays()) {
            // MonthlyLastWeekdayPeriod p = new MonthlyLastWeekdayPeriod();
            // p.setSecondOfWeeks(-1 * JS7WorkflowTimesCalculator.getSecondOfWeeks(wd.getWeekOfMonth(), wd.getDay(), begin));
            // p.setDuration(duration);

            // result.add(p);
            // }
            // }
        }
        return result;
    }
}
