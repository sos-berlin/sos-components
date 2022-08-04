package com.sos.joc.inventory.convert.cron;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.MonthDays;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

public class CronUtils {

    private static final String CRON_REGEX = "-?([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.+)$";
    private static final String CRON_REGEX_ALIAS = "(@(annually|yearly|monthly|weekly|daily|hourly|reboot))\\s+(.+)$";
    private static final String CRON_REGEX_ENVIRONMENT = "^\\s*(\\w+)\\s*=\\s*(.+)";
    private static Pattern cronRegExPattern = Pattern.compile(CRON_REGEX);
    private static Pattern cronRegExAliasPattern = Pattern.compile(CRON_REGEX_ALIAS);
    private static Pattern cronRegExEnvironmentPattern = Pattern.compile(CRON_REGEX_ENVIRONMENT);
    private static Pattern currentCronPattern = cronRegExPattern;
    private static final String START_OF_DAY = "00:00:00";
    private static final String START_OF_HOUR = ":00:00";
    private static final String END_OF_DAY = "23:59:59";
    private static final String END_OF_HOUR = ":59:59";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String JOBNAME = "cronjob";
    private static final String JOBTITLE = "cron job";

    public static Map<WorkflowEdit, ScheduleEdit> cronFile2Workflows(InventoryDBLayer dbLayer, final BufferedReader in, Calendar calendar, String agentName,
            String subagentClusterId, String timezone, boolean isSystemCrontab) throws Exception {
        String workflowBaseName = "workflow_cron-";
        String scheduleBaseName = "schedule_cron-";
        Integer workflowNumber = dbLayer.getSuffixNumber("", "workflow_cron", ConfigurationType.WORKFLOW.intValue());
        Integer scheduleNumber = dbLayer.getSuffixNumber("", "schedule_cron", ConfigurationType.SCHEDULE.intValue());
        Map<WorkflowEdit, ScheduleEdit> scheduledWorkflows = new HashMap<WorkflowEdit, ScheduleEdit>();
        try {
            String cronline = null;
            while ((cronline = in.readLine()) != null) {
                if (cronline.trim().length() == 0) {
                    continue;
                }
                Environment env = new Environment();
                Vector<String> vecCronRecords = new Vector<String>();
                Matcher environmentMatcher = cronRegExEnvironmentPattern.matcher(cronline);
                if (environmentMatcher.matches()) {
                    String envName = environmentMatcher.group(1);
                    String envValue = environmentMatcher.group(2);
                    if (envValue.startsWith("\"") && envValue.endsWith("\"")) {
                        envValue = envValue.substring(1, envValue.length() - 1);
                    }
                    env.getAdditionalProperties().put(envName, envValue);
                }
                Matcher cronAliasMatcher = cronRegExAliasPattern.matcher(cronline);
                if (cronAliasMatcher.matches()) {
                    cronline = convertAlias(cronAliasMatcher);
                }
                Matcher cronMatcher = currentCronPattern.matcher(cronline);
                if (cronMatcher.matches()) {
                    // contains digits?
                    char[] cron = cronline.toCharArray();
                    boolean containsDigit = false;
                    for (char c : cron) {
                        if (Character.isDigit(c)) {
                            containsDigit = true;
                            break;
                        }
                    }
                    if (containsDigit && (cronline.startsWith("*") || Character.isDigit(cron[0]))) {
                        vecCronRecords.add(cronline);
                        Workflow workflow = new Workflow();
                        String workflowName = workflowBaseName + workflowNumber;
                        String scheduleName = scheduleBaseName + scheduleNumber;
                        try {
                            Matcher cronRegExMatcher = cronRegExPattern.matcher(cronline);
                            Integer commandIndex = 6;
                            if (!cronRegExMatcher.matches()) {
                                JocError error = new JocError("Fail to parse cron line \"" + cronline + "\"");
                                throw new JocException(error);
                            }
                            String command = cronRegExMatcher.group(commandIndex);
                            if (isSystemCrontab) {
                                command = command.split("\\s", 2)[1];
                            }
                            ExecutableScript script = new ExecutableScript(command, env, false, null, null, null);
                            script.setTYPE(ExecutableType.ShellScriptExecutable);
                            Jobs jobs = createJob(script, calendar, agentName, subagentClusterId, timezone);
                            workflow.setJobs(jobs);
                            List<Instruction> instructions = new ArrayList<Instruction>();
                            NamedJob instruction = new NamedJob(JOBNAME);
                            instruction.setLabel(JOBNAME);
                            instruction.setTYPE(InstructionType.EXECUTE_NAMED);
                            instructions.add(instruction);
                            workflow.setInstructions(instructions);
                            List<AssignedCalendars> assignedCals = new ArrayList<AssignedCalendars>();
                            assignedCals.add(assignCalendar(calendar, timezone));
                            Schedule schedule = createSchedule(cronline, cronRegExMatcher, assignedCals);
                            schedule.setWorkflowName(workflowName);
                            WorkflowEdit workflowEdit = new WorkflowEdit();
                            workflowEdit.setName(workflowName);
                            workflowEdit.setConfiguration(workflow);
                            ScheduleEdit scheduleEdit = new ScheduleEdit();
                            scheduleEdit.setName(scheduleName);
                            scheduleEdit.setConfiguration(schedule);
                            workflowNumber++;
                            scheduleNumber++;
                            scheduledWorkflows.put(workflowEdit, scheduleEdit);
                        } catch (Exception e) {
                            JocError error = new JocError("Error occured creating job from cron line: " + cronline);
                            throw new JocException(error, e);
                        }
                    }
                }
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            JocError error = new JocError("Error converting cron file upload to new workflow: " + e);
            throw new JocException(error, e);
        } finally {
            in.close();
        }
        return scheduledWorkflows;
    }

    private static Jobs createJob(ExecutableScript script, Calendar calendar, String agentName, String subagentClusterId, String timezone) throws Exception {
        Jobs jobs = new Jobs();
        Job job = new Job();
        jobs.getAdditionalProperties().put(JOBNAME, job);
        job.setTitle(JOBTITLE);
        job.setTimeout(0);
        job.setExecutable(script);
        job.setAgentName(agentName);
        job.setSubagentClusterId(subagentClusterId);
        return jobs;
    }

    private static final AssignedCalendars assignCalendar(Calendar calendar, String timezone) {
        AssignedCalendars assigned = new AssignedCalendars();
        assigned.setCalendarName(calendar.getName());
        assigned.setCalendarPath(calendar.getPath());
        assigned.setExcludes(calendar.getExcludes());
        assigned.setIncludes(calendar.getIncludes());
        assigned.setTimeZone(timezone);
        return assigned;
    }

    private static Schedule createSchedule(final String cronline, final Matcher pcronRegExMatcher, List<AssignedCalendars> assignedCals)
            throws Exception {
        Schedule schedule = new Schedule();
        schedule.setCalendars(assignedCals);
        try {
            if (!pcronRegExMatcher.matches()) {
                JocError error = new JocError("Fail to parse cron line \"" + cronline + "\", regexp is " + pcronRegExMatcher.toString());
                throw new JocException(error);
            }
            String minutes = pcronRegExMatcher.group(1);
            String hours = pcronRegExMatcher.group(2);
            String days = pcronRegExMatcher.group(3);
            String months = pcronRegExMatcher.group(4);
            String weekdays = pcronRegExMatcher.group(5);
            // create periods
            List<Period> periods = parsePeriods(minutes, hours);
            if (periods != null) {
                schedule.getCalendars().get(0).setPeriods(periods);
            }

            // create restrictions - include frequencies
            Frequencies includes = schedule.getCalendars().get(0).getIncludes();
            Frequencies includesWithRestrictions = parseRestrictions(includes, days, months, weekdays);
            schedule.getCalendars().get(0).setIncludes(includesWithRestrictions);
            return schedule;
        } catch (Exception e) {
            JocError error = new JocError("Error creating schedule: " + e);
            throw new JocException(error, e);
        }
    }

    private static final Frequencies parseRestrictions(Frequencies calIncludes, String days, String months, String weekdays) {
        if (calIncludes == null) {
            calIncludes = new Frequencies();
        }
        if (days != null && months != null && weekdays != null) {
            Set<Integer> weekdaysSet = new HashSet<Integer>();
            weekdaysSet.add(0);
            weekdaysSet.add(1);
            weekdaysSet.add(2);
            weekdaysSet.add(3);
            weekdaysSet.add(4);
            weekdaysSet.add(5);
            weekdaysSet.add(6);
            if ("*".equals(days) && "*".equals(months) && "*".equals(weekdays)) {
                Date fromDate = Date.from(Instant.now());
                String from = DATE_FORMAT.format(fromDate);
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.MONTH, 11);
                c.set(java.util.Calendar.DAY_OF_MONTH, c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                String to = DATE_FORMAT.format(c.getTime());
                WeekDays weekDays = new WeekDays();
                weekDays.setFrom(from);
                weekDays.setTo(to);
                weekDays.setDays(weekdaysSet.stream().collect(Collectors.toList()));
                if (calIncludes.getWeekdays() != null) {
                    calIncludes.getWeekdays().add(weekDays);
                } else {
                    List<WeekDays> weekDaysList = new ArrayList<WeekDays>();
                    weekDaysList.add(weekDays);
                    calIncludes.setWeekdays(weekDaysList);
                }
            } else if ("*".equals(days) && "*".equals(weekdays)) {
                if (months.contains("*/")) {
                    Integer mod = Integer.parseInt(months.replace("*/", ""));
                    for (Integer i = 1; i <= 12; i++) {
                        if (i % mod == 0) {
                            processWeekDays(calIncludes, i.toString(), null, weekdaysSet);
                        }
                    }
                } else if (months.contains("-") && months.contains("/")) {
                    String[] modSplit = months.split("/");
                    Integer mod = Integer.parseInt(modSplit[1]);
                    String[] monthSplit = modSplit[0].split("-");
                    Integer startMonth = Integer.parseInt(monthSplit[0]);
                    Integer endMonth = Integer.parseInt(monthSplit[1]);
                    for (Integer i = startMonth; i <= endMonth; i += mod) {
                        processWeekDays(calIncludes, i.toString(), null, weekdaysSet);
                    }
                } else if (months.contains("-")) {
                    String[] monthSplit = months.split("-");
                    String startMonth = monthSplit[0];
                    String endMonth = monthSplit[1];
                    processWeekDays(calIncludes, startMonth, endMonth, weekdaysSet);
                } else {
                    processWeekDays(calIncludes, months, null, weekdaysSet);
                }
            } else if ("*".equals(months) && "*".equals(weekdays)) {
                if (days.contains("*/")) {
                    Integer mod = Integer.parseInt(days.replace("*/", ""));
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    for (Integer i = 1; i <= c.getActualMaximum(java.util.Calendar.DAY_OF_YEAR); i++) {
                        if (i % mod == 0) {
                            processDates(calIncludes, null, null, i);
                        }
                    }
                } else if (days.contains("-") && days.contains("/")) {
                    String[] modSplit = days.split("/");
                    Integer mod = Integer.parseInt(modSplit[1]);
                    String[] daySplit = modSplit[0].split("-");
                    Integer startDay = Integer.parseInt(daySplit[0]);
                    Integer endDay = Integer.parseInt(daySplit[1]);
                    for (int i = 1; i <= 12; i++) {
                        for (Integer j = startDay; j <= endDay; j += mod) {
                            processDates(calIncludes, i - 1, j, null);
                        }
                    }

                } else if (days.contains("-")) {
                    String[] daySplit = days.split("-");
                    Integer startDay = Integer.parseInt(daySplit[0]);
                    Integer endDay = Integer.parseInt(daySplit[1]);
                    for (int i = 1; i <= 12; i++) {
                        for (Integer j = startDay; j <= endDay; j++) {
                            processDates(calIncludes, i - 1, j, null);
                        }
                    }
                } else {
                    for (int i = 1; i <= 12; i++) {
                        processDates(calIncludes, i - 1, Integer.parseInt(days), null);
                    }
                }
            } else if ("*".equals(months) && "*".equals(days)) {
                weekdaysSet = new HashSet<Integer>();
                if (weekdays.contains("*/")) {
                    Integer mod = Integer.parseInt(months.replace("*/", ""));
                    for (Integer i = 0; i <= 6; i++) {
                        if (i % mod == 0) {
                            weekdaysSet.add(i);
                        }
                    }
                    processWeekDays(calIncludes, "1", "12", weekdaysSet);
                } else if (weekdays.contains("-") && weekdays.contains("/")) {
                    String[] modSplit = weekdays.split("/");
                    Integer mod = Integer.parseInt(modSplit[1]);
                    String[] weekdaysSplit = modSplit[0].split("-");
                    Integer startWeekday = Integer.parseInt(weekdaysSplit[0]);
                    Integer endWeekday = Integer.parseInt(weekdaysSplit[1]);
                    for (Integer i = startWeekday; i <= endWeekday; i += mod) {
                        weekdaysSet.add(i);
                    }
                    processWeekDays(calIncludes, "1", "12", weekdaysSet);
                } else if (weekdays.contains("-")) {
                    String[] weekdaysSplit = weekdays.split("-");
                    Integer startWeekday = Integer.parseInt(weekdaysSplit[0]);
                    Integer endWeekday = Integer.parseInt(weekdaysSplit[1]);
                    for (Integer i = startWeekday; i <= endWeekday; i++) {
                        weekdaysSet.add(i);
                    }
                    processWeekDays(calIncludes, "1", "12", weekdaysSet);
                } else {
                    weekdaysSet.add(Integer.parseInt(weekdays));
                    processWeekDays(calIncludes, months, null, weekdaysSet);
                }
            } else {
                Set<MonthDays> monthDays = new HashSet<MonthDays>();
                if (months.contains(",")) {
                    String[] monthsSplit = months.split(",");
                    for (int i = 0; i < monthsSplit.length; i++) {
                        monthDays.addAll(processMonths(monthsSplit[i], days, weekdays));
                    }
                } else {
                    monthDays.addAll(processMonths(months, days, weekdays));
                }
                if (calIncludes.getMonthdays() != null) {
                    calIncludes.getMonthdays().addAll(monthDays);
                } else {
                    calIncludes.setMonthdays(monthDays.stream().collect(Collectors.toList()));
                }
            }
        }
        return calIncludes;
    }

    private static final List<MonthDays> processMonths(String months, String days, String weekdays) {
        List<MonthDays> monthDays = new ArrayList<MonthDays>();
        if (months.contains("*/")) {
            int mod = Integer.parseInt(months.replace("*/", ""));
            for (Integer i = 1; i <= 12; i += mod) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                int max = c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                if (days.contains(",")) {
                    String[] daysSplit = days.split(",");
                    for (int j = 0; j < daysSplit.length; j++) {
                        monthDays.add(processMonthDays(i.toString(), daysSplit[j], max, weekdays));
                    }
                } else {
                    monthDays.add(processMonthDays(i.toString(), days, max, weekdays));
                }
            }
        } else if (months.contains("-") && months.contains("/")) {
            String[] modSplit = months.split("/");
            Integer mod = Integer.parseInt(modSplit[1]);
            String[] monthsSplit = modSplit[0].split("-");
            Integer startMonth = Integer.parseInt(monthsSplit[0]);
            Integer endMonth = Integer.parseInt(monthsSplit[1]);
            for (Integer i = startMonth; i <= endMonth; i += mod) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.MONTH, i - 1);
                int max = c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                if (days.contains(",")) {
                    String[] daysSplit = days.split(",");
                    for (int j = 0; j < daysSplit.length; j++) {
                        monthDays.add(processMonthDays(i.toString(), daysSplit[j], max, weekdays));
                    }
                } else {
                    monthDays.add(processMonthDays(i.toString(), days, max, weekdays));
                }
            }
        } else if (months.contains("-")) {
            String[] monthsSplit = months.split("-");
            Integer startMonth = Integer.parseInt(monthsSplit[0]);
            Integer endMonth = Integer.parseInt(monthsSplit[1]);
            for (Integer i = startMonth; i <= endMonth; i++) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.MONTH, i - 1);
                int max = c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                if (days.contains(",")) {
                    String[] daysSplit = days.split(",");
                    for (int j = 0; j < daysSplit.length; j++) {
                        monthDays.add(processMonthDays(i.toString(), daysSplit[j], max, weekdays));
                    }
                } else {
                    monthDays.add(processMonthDays(i.toString(), days, max, weekdays));
                }
            }
        } else if (months.contains("*")) {
            for (Integer i = 1; i <= 12; i++) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.set(java.util.Calendar.MONTH, i - 1);
                int max = c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                if (days.contains(",")) {
                    String[] daysSplit = days.split(",");
                    for (int j = 0; j < daysSplit.length; j++) {
                        monthDays.add(processMonthDays(i.toString(), daysSplit[j], max, weekdays));
                    }
                } else {
                    monthDays.add(processMonthDays(i.toString(), days, max, weekdays));
                }
            }
        } else {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(java.util.Calendar.MONTH, Integer.parseInt(months) - 1);
            int max = c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            if (days.contains(",")) {
                String[] daysSplit = days.split(",");
                for (int i = 0; i < daysSplit.length; i++) {
                    monthDays.add(processMonthDays(months, daysSplit[i], max, weekdays));
                }
            } else {
                monthDays.add(processMonthDays(months, days, max, weekdays));
            }
        }
        return monthDays;
    }

    private static final void processDates(Frequencies calIncludes, Integer month, Integer dayOfMonth, Integer dayOfYear) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        if (month != null && dayOfMonth != null) {
            c.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
            c.set(java.util.Calendar.MONTH, month);
        } else if (dayOfYear != null) {
            c.set(java.util.Calendar.DAY_OF_YEAR, dayOfYear);
        }
        String date = DATE_FORMAT.format(c.getTime());
        if (calIncludes.getDates() != null) {
            calIncludes.getDates().add(date);
        } else {
            List<String> weekDaysList = new ArrayList<String>();
            weekDaysList.add(date);
            calIncludes.setDates(weekDaysList);
        }
    }

    private static final void processWeekDays(Frequencies calIncludes, String startMonth, String endMonth, Set<Integer> weekdaysSet) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.MONTH, Integer.parseInt(startMonth) - 1);
        c.set(java.util.Calendar.DAY_OF_MONTH, 1);
        String from = DATE_FORMAT.format(c.getTime());
        if (endMonth != null) {
            c.set(java.util.Calendar.MONTH, Integer.parseInt(endMonth) - 1);
        } else {
            c.set(java.util.Calendar.MONTH, Integer.parseInt(startMonth) - 1);
        }
        c.set(java.util.Calendar.DAY_OF_MONTH, c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        String to = DATE_FORMAT.format(c.getTime());
        WeekDays weekDays = new WeekDays();
        weekDays.setFrom(from);
        weekDays.setTo(to);
        weekDays.setDays(weekdaysSet.stream().collect(Collectors.toList()));
        if (calIncludes.getWeekdays() != null) {
            calIncludes.getWeekdays().add(weekDays);
        } else {
            List<WeekDays> weekDaysList = new ArrayList<WeekDays>();
            weekDaysList.add(weekDays);
            calIncludes.setWeekdays(weekDaysList);
        }
    }

    private static final Set<Integer> processWeekdays(String weekdays) {
        Set<Integer> result = new HashSet<Integer>();
        if (weekdays.contains("*/")) {
            Integer mod = Integer.parseInt(weekdays.replace("*/", ""));
            for (int i = 0; i <= 6; i++) {
                if (i % mod == 0) {
                    result.add(i);
                }
            }
        } else if (weekdays.contains("-") && weekdays.contains("/")) {
            String[] modSplit = weekdays.split("/");
            Integer mod = Integer.parseInt(modSplit[1]);
            String[] daySplit = modSplit[0].split("-");
            Integer startDay = Integer.parseInt(daySplit[0]);
            Integer endDay = Integer.parseInt(daySplit[1]);
            for (int i = startDay; i <= endDay; i += mod) {
                result.add(i);
            }
        } else if (weekdays.contains("-")) {
            String[] daySplit = weekdays.split("-");
            Integer startDay = Integer.parseInt(daySplit[0]);
            Integer endDay = Integer.parseInt(daySplit[1]);
            for (int i = startDay; i <= endDay; i++) {
                result.add(i);
            }
        } else if (weekdays.contains("*")) {
            for (int i = 0; i <= 6; i++) {
                result.add(i);
            }
        } else {
            result.add(Integer.parseInt(weekdays));
        }
        return result;
    }

    private static final MonthDays processMonthDays(String month, String days, int maxDays, String weekdays) {
        Set<Integer> weekDays = new HashSet<Integer>();
        if (weekdays.contains(",")) {
            String[] weekdaysSplit = weekdays.split(",");
            for (int i = 0; i < weekdaysSplit.length; i++) {
                weekDays.addAll(processWeekdays(weekdaysSplit[i]));
            }
        } else {
            weekDays.addAll(processWeekdays(weekdays));
        }
        MonthDays monthDays = new MonthDays();
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.MONTH, Integer.parseInt(month) - 1);
        c.set(java.util.Calendar.DAY_OF_MONTH, 1);
        String from = DATE_FORMAT.format(c.getTime());
        c.set(java.util.Calendar.DAY_OF_MONTH, c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        String to = DATE_FORMAT.format(c.getTime());
        monthDays.setFrom(from);
        monthDays.setTo(to);
        if (days.contains("*/")) {
            Integer mod = Integer.parseInt(days.replace("*/", ""));
            List<Integer> dayList = new ArrayList<Integer>();
            for (Integer i = 1; i <= maxDays; i += mod) {
                c.set(java.util.Calendar.DAY_OF_MONTH, i);
                if (weekDays.contains(c.get(java.util.Calendar.DAY_OF_WEEK) - 1)) {
                    int dayToAdd = c.get(java.util.Calendar.DAY_OF_MONTH);
                    dayList.add(dayToAdd);
                }
            }
            monthDays.setDays(dayList);
        } else if (days.contains("-") && days.contains("/")) {
            String[] modSplit = days.split("/");
            Integer mod = Integer.parseInt(modSplit[1]);
            String[] daysSplit = modSplit[0].split("-");
            Integer startDay = Integer.parseInt(daysSplit[0]);
            Integer endDay = Integer.parseInt(daysSplit[1]);
            List<Integer> dayList = new ArrayList<Integer>();
            for (int i = startDay; i <= endDay; i += mod) {
                c.set(java.util.Calendar.DAY_OF_MONTH, i);
                if (weekDays.contains(c.get(java.util.Calendar.DAY_OF_WEEK) - 1)) {
                    int dayToAdd = c.get(java.util.Calendar.DAY_OF_MONTH);
                    dayList.add(dayToAdd);
                }
            }
            monthDays.setDays(dayList);
        } else if (days.contains("-")) {
            String[] daysSplit = days.split("-");
            Integer startDay = Integer.parseInt(daysSplit[0]);
            Integer endDay = Integer.parseInt(daysSplit[1]);
            List<Integer> dayList = new ArrayList<Integer>();
            for (int i = startDay; i <= endDay; i++) {
                c.set(java.util.Calendar.DAY_OF_MONTH, i);
                if (weekDays.contains(c.get(java.util.Calendar.DAY_OF_WEEK) - 1)) {
                    int dayToAdd = c.get(java.util.Calendar.DAY_OF_MONTH);
                    dayList.add(dayToAdd);
                }
            }
            monthDays.setDays(dayList);
        } else if (days.contains("*")) {
            List<Integer> dayList = new ArrayList<Integer>();
            for (int i = 1; i <= c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH); i++) {
                c.set(java.util.Calendar.DAY_OF_MONTH, i);
                if (weekDays.contains(c.get(java.util.Calendar.DAY_OF_WEEK) - 1)) {
                    int dayToAdd = c.get(java.util.Calendar.DAY_OF_MONTH);
                    dayList.add(dayToAdd);
                }
            }
            monthDays.setDays(dayList);
        } else {
            Set<Integer> dayList = new HashSet<Integer>();
            dayList.add(Integer.parseInt(days));
            c.set(java.util.Calendar.DAY_OF_MONTH, 1);
            for (Integer i = 1; i <= c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH); i++) {
                c.set(java.util.Calendar.DAY_OF_MONTH, i);
                if (weekDays.contains(c.get(java.util.Calendar.DAY_OF_WEEK) - 1)) {
                    int dayToAdd = c.get(java.util.Calendar.DAY_OF_MONTH);
                    dayList.add(dayToAdd);
                }
            }
            monthDays.setDays(dayList.stream().collect(Collectors.toList()));
        }
        return monthDays;
    }

    private static final Period createPeriod(String begin, String end, String repeat, String singleStart) {
        Period period = new Period();
        period.setWhenHoliday(WhenHolidayType.SUPPRESS);
        period.setBegin(begin);
        period.setEnd(end);
        period.setRepeat(repeat);
        period.setSingleStart(singleStart);
        return period;
    }

    private static final List<Period> parsePeriods(String minutes, String hours) {
        List<Period> periods = new ArrayList<Period>();
        if (minutes != null && hours != null) {
            if (minutes.contains(",")) {
                String[] minutesSplit = minutes.split(",");
                for (int i = 0; i < minutesSplit.length; i++) {
                    String minutesGroup = minutesSplit[i];
                    periods.addAll(processMinutes(minutesGroup, hours));
                }
            } else {
                periods.addAll(processMinutes(minutes, hours));
            }
        }
        if (!periods.isEmpty()) {
            return periods;
        } else {
            return null;
        }
    }

    private static final List<Period> processMinutes(String minutes, String hours) {
        List<Period> periods = new ArrayList<Period>();
        if (minutes.contains("*/")) {
            // repeat
            String repeatMinutes = minutes.replace("*/", "");
            periods.addAll(processHours(hours, null, null, repeatMinutes));
        } else if (minutes.contains("-") && minutes.contains("/")) {
            String[] minutesSplit = minutes.split("-");
            String startMinute = minutesSplit[0];
            String[] minutesSplit2 = minutesSplit[1].split("/");
            String endMinute = minutesSplit2[0];
            String repeatMinutes = minutesSplit2[1];
            if (hours.contains(",")) {
                String[] hoursSplit = hours.split(",");
                for (int i = 0; i < hoursSplit.length; i++) {
                    periods.addAll(processHours(hoursSplit[i], startMinute, endMinute, repeatMinutes));
                }
            } else {
                periods.addAll(processHours(hours, startMinute, endMinute, repeatMinutes));
            }
        } else if (minutes.contains("-")) {
            String[] minutesSplit = minutes.split("-");
            String startMinute = minutesSplit[0];
            String endMinute = minutesSplit[1];
            for (int i = Integer.parseInt(startMinute); i <= Integer.parseInt(endMinute); i++) {
                if (hours.contains(",")) {
                    String[] hoursSplit = hours.split(",");
                    for (int j = 0; j < hoursSplit.length; j++) {
                        String hourGroup = hoursSplit[j];
                        periods.addAll(processHours(hourGroup, startMinute, endMinute, "1"));
                    }
                } else {
                    periods.addAll(processHours(hours, startMinute, endMinute, "1"));
                }
            }
        } else if (minutes.equals("*")) {
            if (hours.contains(",")) {
                String[] hoursSplit = hours.split(",");
                for (int i = 0; i < hoursSplit.length; i++) {
                    periods.addAll(processHours(hoursSplit[i], null, null, "1"));
                }
            } else {
                periods.addAll(processHours(hours, null, null, "1"));
            }
        } else {
            if (hours.contains(",")) {
                String[] hoursSplit = hours.split(",");
                for (int i = 0; i < hoursSplit.length; i++) {
                    periods.addAll(processSingleMinuteHours(hoursSplit[i], minutes));
                }
            } else {
                periods.addAll(processSingleMinuteHours(hours, minutes));
            }
        }
        return periods;
    }

    private static final List<Period> processSingleMinuteHours(String hours, String minute) {
        List<Period> periods = new ArrayList<Period>();
        if (hours.contains("*/")) {
            String hour = hours.replace("*/", "");
            int repeate = Integer.parseInt(hour);
            for (int i = 0; i < 24; i += repeate) {
                periods.add(createPeriod(null, null, null, formatTwoDigits(i) + ":" + formatTwoDigits(minute) + ":00"));
            }
        } else if (hours.contains("-") && hours.contains("/")) {
            String[] repeateSplit = hours.split("/");
            String[] hoursSplit = repeateSplit[0].split("-");
            periods.add(createPeriod(formatTwoDigits(hoursSplit[0]) + ":" + formatTwoDigits(minute) + ":00", formatTwoDigits(hoursSplit[1]) + ":"
                    + formatTwoDigits(minute) + ":00", formatTwoDigits(repeateSplit[1]) + START_OF_HOUR, null));
        } else if (hours.contains("-")) {
            String[] hoursSplit = hours.split("-");
            periods.add(createPeriod(formatTwoDigits(hoursSplit[0]) + ":" + formatTwoDigits(minute) + ":00", formatTwoDigits(hoursSplit[1])
                    + END_OF_HOUR, "01:00:00", null));
        } else if (hours.equals("*")) {
            periods.add(createPeriod("00:" + formatTwoDigits(minute) + ":00", END_OF_DAY, "01:00:00", null));
        } else {
            periods.add(createPeriod(null, null, null, formatTwoDigits(hours) + ":" + formatTwoDigits(minute) + ":00"));
        }
        return periods;
    }

    private static final List<Period> processHours(String hours, String startMinute, String endMinute, String repeatMinutes) {
        List<Period> periods = new ArrayList<Period>();
        if (hours.contains("*/")) {
            String hour = hours.replace("*/", "");
            int hourRepeat = Integer.parseInt(hour);
            String start = null;
            String end = null;
            for (int i = 0; i < 24; i += hourRepeat) {
                if (startMinute != null) {
                    start = formatTwoDigits(i) + ":" + formatTwoDigits(startMinute) + ":00";
                } else {
                    start = formatTwoDigits(i) + START_OF_HOUR;
                }
                if (endMinute != null) {
                    end = formatTwoDigits(i) + ":" + formatTwoDigits(endMinute) + ":59";
                } else {
                    end = formatTwoDigits(i) + END_OF_HOUR;
                }
                periods.add(createPeriod(start, end, "00:" + formatTwoDigits(repeatMinutes) + ":00", null));
            }
        } else if (hours.contains("-") && hours.contains("/")) {
            String[] repeatSplit = hours.split("/");
            int repeateHours = Integer.parseInt(repeatSplit[1]);
            String[] hoursSplit = repeatSplit[0].split("-");
            int startHour = Integer.parseInt(hoursSplit[0]);
            int endHour = Integer.parseInt(hoursSplit[1]);
            String start = null;
            String end = null;
            for (int i = startHour; i <= endHour; i += repeateHours) {
                if (startMinute != null) {
                    start = formatTwoDigits(i) + ":" + formatTwoDigits(startMinute) + ":00";
                } else {
                    start = formatTwoDigits(i) + START_OF_HOUR;
                }
                if (endMinute != null) {
                    end = formatTwoDigits(i) + ":" + formatTwoDigits(endMinute) + ":59";
                } else {
                    end = formatTwoDigits(i) + END_OF_HOUR;
                }
                periods.add(createPeriod(start, end, "00:" + formatTwoDigits(repeatMinutes) + ":00", null));
            }
        } else if (hours.contains("-")) {
            String[] split = hours.split("-");
            String start = null;
            String end = null;
            if (startMinute != null) {
                start = formatTwoDigits(split[0]) + ":" + formatTwoDigits(startMinute) + ":00";
            } else {
                start = formatTwoDigits(split[0]) + START_OF_HOUR;
            }
            if (endMinute != null) {
                end = formatTwoDigits(split[1]) + ":" + formatTwoDigits(endMinute) + ":59";
            } else {
                end = formatTwoDigits(split[1]) + END_OF_HOUR;
            }
            periods.add(createPeriod(start, end, "00:" + formatTwoDigits(repeatMinutes) + ":00", null));
        } else if (hours.equals("*")) {
            String start = null;
            String end = null;
            if (startMinute != null) {
                start = "00:" + formatTwoDigits(startMinute) + ":00";
            } else {
                start = START_OF_DAY;
            }
            if (endMinute != null) {
                end = "23:" + formatTwoDigits(endMinute) + ":59";
            } else {
                end = END_OF_DAY;
            }
            periods.add(createPeriod(start, end, "00:" + formatTwoDigits(repeatMinutes) + ":00", null));
        } else {
            if (repeatMinutes != null) {
                periods.add(createPeriod(formatTwoDigits(hours) + START_OF_HOUR, formatTwoDigits(hours) + END_OF_HOUR, "00:" + formatTwoDigits(
                        repeatMinutes) + ":00", null));
            } else {
                periods.add(createPeriod(formatTwoDigits(hours) + START_OF_HOUR, formatTwoDigits(hours) + END_OF_HOUR, "00:" + formatTwoDigits(
                        repeatMinutes) + ":00", null));
            }
        }
        return periods;
    }

    private static String formatTwoDigits(final String number) {
        if (number.length() == 1) {
            return "0" + number;
        }
        return number;
    }

    private static String formatTwoDigits(final int number) {
        return formatTwoDigits("" + number);
    }

    private static String convertAlias(final Matcher matcher) throws Exception {
        try {
            String alias = matcher.group(1);
            String rest = matcher.group(2);
            String result = "";
            if ("@yearly".equals(alias) || "@annually".equals(alias)) {
                result = "0 0 1 1 * ";
            } else if ("@monthly".equals(alias)) {
                result = "0 0 1 * * ";
            } else if ("@weekly".equals(alias)) {
                result = "0 0 * * 0 ";
            } else if ("@daily".equals(alias) || "@midnight".equals(alias)) {
                result = "0 0 * * * ";
            } else if ("@hourly".equals(alias)) {
                result = "0 * * * * ";
            } else if ("@reboot".equals(alias)) {
                result = "@reboot @reboot @reboot @reboot @reboot";
            }
            result += rest;
            return result;
        } catch (Exception e) {
            JocError error = new JocError("Error converting alias:");
            throw new JocException(error, e);
        }
    }

}
