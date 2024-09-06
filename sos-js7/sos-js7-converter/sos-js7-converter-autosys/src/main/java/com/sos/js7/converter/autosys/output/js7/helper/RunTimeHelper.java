package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.schedule.CycleSchedule;
import com.sos.inventory.model.instruction.schedule.Periodic;
import com.sos.inventory.model.instruction.schedule.Scheme;
import com.sos.inventory.model.job.AdmissionTimePeriod;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.DailyPeriod;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.WorkflowResult;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig;
import com.sos.js7.converter.commons.report.ConverterReport;

public class RunTimeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunTimeHelper.class);
    public static final Set<String> JS7_CALENDARS = new HashSet<>();

    public static void clear() {
        JS7_CALENDARS.clear();
    }

    public static Schedule toSchedule(AutosysConverterConfig config, WorkflowResult wr, ACommonJob j) {

        boolean log = false;
        if (j.getName().equals("icma.icm_ut_c_edwmg_ld_model_pii_predict")) {
            log = true;
        }

        if (!j.hasRunTime()) {
            return null;
        }

        String workingDayCalendar = getWorkingDayCalendarName(config, j);
        if (workingDayCalendar == null) {
            ConverterReport.INSTANCE.addWarningRecord(null, j.getName(), "[convertSchedule][job without " + j.getRunTime().getRunCalendar().getName()
                    + "][missing callendar]scheduleConfig.forced- or defaultWorkingDayCalendarName is not configured");

            return null;
        }

        AssignedCalendars calendar = new AssignedCalendars();
        calendar.setCalendarName(workingDayCalendar);
        calendar.setTimeZone(j.getRunTime().getTimezone().getValue() == null ? config.getScheduleConfig().getDefaultTimeZone() : j.getRunTime()
                .getTimezone().getValue());
        Frequencies includes = null;
        if (j.getRunTime().getDaysOfWeek().getValue() != null) {
            includes = new Frequencies();
            WeekDays weekDays = new WeekDays();
            weekDays.setDays(JS7ConverterHelper.getDays(j.getRunTime().getDaysOfWeek().getValue().getDays()));
            includes.setWeekdays(Collections.singletonList(weekDays));
        }
        calendar.setIncludes(includes);

        List<Period> periods = new ArrayList<>();
        if (j.getRunTime().isSingleStarts()) {
            for (String time : j.getRunTime().getStartTimes().getValue()) {
                Period p = new Period();
                p.setSingleStart(time);
                p.setWhenHoliday(WhenHolidayType.SUPPRESS);
                periods.add(p);
            }
        } else if (j.getRunTime().isCyclic()) {
            if (config.getGenerateConfig().getCyclicOrders()) {
                Period p = new Period();
                p.setBegin(JS7ConverterHelper.toMins(j.getRunTime().getStartMins().getValue().get(0)));
                p.setEnd("24:00");
                p.setRepeat(JS7ConverterHelper.toRepeat(j.getRunTime().getStartMins().getValue()));
                p.setWhenHoliday(WhenHolidayType.SUPPRESS);
                periods.add(p);
            }
        }

        if (log) {
            LOGGER.info("[periords]" + periods);
        }

        // Runtime provides only Timezone for Example
        if (periods.size() == 0) {
            Period p = new Period();
            p.setSingleStart("00:00");
            p.setWhenHoliday(WhenHolidayType.SUPPRESS);
            periods.add(p);
        }

        calendar.setPeriods(periods);

        Schedule s = new Schedule();
        // s.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle("xxxx"));
        s.setWorkflowNames(Collections.singletonList(wr.getName()));
        s.setPlanOrderAutomatically(config.getScheduleConfig().planOrders());
        s.setSubmitOrderToControllerWhenPlanned(config.getScheduleConfig().submitOrders());
        s.setCalendars(Collections.singletonList(calendar));

        return s;
    }

    private static String getWorkingDayCalendarName(JS7ConverterConfig config, ACommonJob j) {
        String name = null;
        if (config.getScheduleConfig().getForcedWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getForcedWorkingDayCalendarName();
        } else if (j.getRunTime().getRunCalendar().getValue() != null) {
            name = JS7ConverterHelper.getJS7ObjectName(j.getRunTime().getRunCalendar().getValue());
        } else if (config.getScheduleConfig().getDefaultWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getDefaultWorkingDayCalendarName();
        }
        if (!JS7_CALENDARS.contains(name)) {
            JS7_CALENDARS.add(name);
        }
        return name;
    }

    private static String getNonWorkingDayCalendarName(JS7ConverterConfig config, ACommonJob j) {
        String name = null;
        if (config.getScheduleConfig().getForcedNonWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getForcedNonWorkingDayCalendarName();
        } else if (j.getRunTime().getExcludeCalendar().getValue() != null) {
            name = JS7ConverterHelper.getJS7ObjectName(j.getRunTime().getExcludeCalendar().getValue());
        } else if (config.getScheduleConfig().getDefaultNonWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getDefaultNonWorkingDayCalendarName();
        }
        if (!JS7_CALENDARS.contains(name)) {
            JS7_CALENDARS.add(name);
        }
        return name;
    }

    public static Scheme toCyclicInstruction(WorkflowResult wr, ACommonJob j, BoardTryCatchHelper btch) {
        if (!convert2cyclic(j)) {
            return null;
        }

        Scheme scheme = new Scheme();
        AdmissionTimeScheme atScheme = new AdmissionTimeScheme();
        List<AdmissionTimePeriod> periods = new ArrayList<>();

        // TODO
        return scheme;
    }

    // TODO replace by toCyclicInstruction method ...
    public static List<Instruction> getCyclicWorkflowInstructions(ACommonJob j, List<Instruction> in, BoardTryCatchHelper btch) {
        if (!convert2cyclic(j)) {
            return in;
        }

        Periodic p = new Periodic();
        p.setPeriod(3_600L);
        // TODO
        if (j.getRunTime().getStartMins().getValue().size() == 60) {
            p.setOffsets(Collections.singletonList(60L));
        } else {
            p.setOffsets(j.getRunTime().getStartMins().getValue().stream().map(e -> Long.valueOf(e * 60L)).collect(Collectors.toList()));
        }

        DailyPeriod dp = new DailyPeriod();
        dp.setSecondOfDay(0L);
        dp.setDuration(86_400L);

        CycleSchedule cs = new CycleSchedule(Collections.singletonList(new Scheme(p, new AdmissionTimeScheme(Collections.singletonList(dp)))));

        if (btch != null) {
            if (btch.getTryPostNotices() != null) {
                in.add(btch.getTryPostNotices());
                btch.resetTryPostNotices();
            }
            ConsumeNotices cn = btch.getConsumeNotices();
            if (cn != null) {
                cn.setSubworkflow(new Instructions(in));
                in = Collections.singletonList(cn);
                btch.resetConsumeNotices();
            }
        }

        Instructions ci = new Instructions(in);

        in = new ArrayList<>();
        in.add(new Cycle(ci, cs));

        return in;
    }

    private static boolean convert2cyclic(ACommonJob j) {
        if (!j.hasRunTime() || !j.getRunTime().isCyclic()) {
            return false;
        }
        if (Autosys2JS7Converter.CONFIG.getGenerateConfig().getCyclicOrders()) {
            return false;
        }
        return true;
    }

}
