package com.sos.js7.converter.autosys.common.v12.job.attributes;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;

public class CommonJobRunTime extends AJobArguments {

    /** timezone - Define the Time Zone<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: timezone: zone<br/>
     * Examples: "IST-5:30", MountainTime (instead of Mountain Time), SolomonIs (instead of Solomon Is)<br/>
     * timezone: Chicago<br/>
     * timezone: US/Pacific<br/>
     * <br/>
     * JS7 - 100% - Time Zone Support<br/>
     */
    private SOSArgument<String> timezone = new SOSArgument<>("timezone", false);

    /** run_calendar - Identify a Custom Calendar<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The run_calendar attribute identifies a custom calendar to use to determine the days of the week on which to run the job you are defining.<br/>
     * You can use a standard calendar or an extended calendar as the run calendar.<br/>
     * Format: run_calendar: calendar_name<br/>
     * Limits: Up to 64 alphanumeric characters<br/>
     * <br/>
     * JS7 - 100% - Calendar & Schedules<br/>
     */
    private SOSArgument<String> runCalendar = new SOSArgument<>("run_calendar", false);

    /** run_window - Define an Interval for a Job to Start<br/>
     * This attribute is optional for all job types.<br/>
     *
     * Format: run_window: "time-time"<br/>
     * time - time<br/>
     * Defines the interval during which a job may start in the format "hh:mm-hh:mm", where hh denotes hours (in 24-hour format) and mm denotes minutes.<br/>
     * You must enclose the interval in quotation marks (") and separate the beginning and ending times with a hyphen (-).<br/>
     * The specified interval can overlap midnight, but cannot encompass more than 24 hours.<br/>
     * Limits: Up to 20 alphanumeric characters<br/>
     * Default: No run window is used.<br/>
     * Example: run_window: "23:00-02:00"<br/>
     * <br/>
     * JS7 - 100% - Admission Times for Job<br/>
     */
    private SOSArgument<String> runWindow = new SOSArgument<>("run_window", false);

    /** days_of_week - Specify which Days of the Week to Run a Job<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: <br/>
     * - days_of_week: day [,day...]<br/>
     * - days_of_week: all<br/>
     * day -Specifies the days of the week when the job runs. Options are the following:<br/>
     * – mo -- Specifies Monday.<br/>
     * – tue -- Specifies Tuesday.<br/>
     * – we -- Specifies Wednesday.<br/>
     * – th -- Specifies Thursday.<br/>
     * – fr -- Specifies Friday.<br/>
     * – sa -- Specifies Saturday.<br/>
     * – su -- Specifies Sunday.<br/>
     * Default: No days are selected.<br/>
     * <br/>
     * JS7 - 100% - Calendar & Schedules<br/>
     */
    private SOSArgument<String> daysOfWeek = new SOSArgument<>("days_of_week", false);

    /** start_times - Define the Time of the Day to Run a Job<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: start_times: "time [, time]..."<br/>
     * Examples:<br/>
     * start_times: "10:00, 14:00"<br/>
     * or<br/>
     * start_times: 10\:00, 14\:00<br/>
     * <br/>
     * JS7 - 100% - Calendar & Schedules<br/>
     */
    private SOSArgument<List<String>> startTimes = new SOSArgument<>("start_times", false);

    /** start_mins - Define the Minutes to Run a Job<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The start_mins attribute defines the number of minutes after each hour at which the job should start on the days specified in the days_of_week attribute
     * or on the dates specified in the calendar identified in the run_calendar attribute.<br/>
     * Format: start_mins: mins [, mins]...<br/>
     * Example: start_mins: 15, 30<br/>
     * <br/>
     * JS7 - 100% - Cycle Instruction<br/>
     */
    private SOSArgument<List<Integer>> startMins = new SOSArgument<>("start_mins", false);

    /** date_conditions - Base Job Start on Date and Time Attribute Values<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The date_conditions attribute specifies whether to use the date or time conditions defined in the following attributes to determine when to run the
     * job:<br/>
     * autocal_asc<br/>
     * days_of_week<br/>
     * exclude_calendar<br/>
     * must_complete_times<br/>
     * must_start_times<br/>
     * run_calendar<br/>
     * run_window<br/>
     * start_mins<br/>
     * start_times<br/>
     * timezone<br/>
     * Format: date_conditions: y | n<br/>
     * y - Uses the date and time conditions defined in other attributes to determine when to run the job.<br/>
     * Note:You can specify 1 instead of y.<br/>
     * n - Default, Ignores the date and time conditions defined in other attributes to determine when to run the job.<br/>
     * Note: You can specify 0 instead of n.<br/>
     * <br/>
     * JS7 - Calendar & Schedules<br/>
     */
    private SOSArgument<Boolean> dateConditions = new SOSArgument<>("date_conditions", false);

    public SOSArgument<String> getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getRunCalendar() {
        return runCalendar;
    }

    public void setRunCalendar(String val) {
        runCalendar.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getRunWindow() {
        return runWindow;
    }

    public void setRunWindow(String val) {
        runWindow.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String val) {
        daysOfWeek.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<List<String>> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(String val) {
        startTimes.setValue(AJobArguments.stringListValue(val));
    }

    public SOSArgument<List<Integer>> getStartMins() {
        return startMins;
    }

    public void setStartMins(String val) {
        startMins.setValue(AJobArguments.integerListValue(val));
    }

    public SOSArgument<Boolean> getDateConditions() {
        return dateConditions;
    }

    public void setDateConditions(String val) {
        dateConditions.setValue(AJobArguments.booleanValue(val, false));
    }
}
