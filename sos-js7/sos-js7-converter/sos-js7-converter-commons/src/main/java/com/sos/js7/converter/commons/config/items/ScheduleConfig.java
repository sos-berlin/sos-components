package com.sos.js7.converter.commons.config.items;

import com.sos.commons.util.SOSDate;

public class ScheduleConfig extends AConfigItem {

    private static final String CONFIG_KEY = "scheduleConfig";

    private String forcedWorkingDayCalendarName;
    private String forcedNonWorkingDayCalendarName;
    private String defaultWorkingDayCalendarName = "AnyDays";
    private String defaultNonWorkingDayCalendarName = "AnyDays";

    private String defaultTimeZone = SOSDate.TIMEZONE_UTC;
    private Boolean planOrders;
    private Boolean submitOrders;

    public ScheduleConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        // FORCED
        case "forced.workingdaycalendarname":
            withForcedWorkingDayCalendarName(val);
            break;
        case "forced.nonworkingdaycalendarname":
            withForcedNonWorkingDayCalendarName(val);
            break;
        // DEFAULT
        case "default.workingdaycalendarname":
            withDefaultWorkingDayCalendarName(val);
            break;
        case "default.nonworkingdaycalendarname":
            withDefaultNonWorkingDayCalendarName(val);
            break;
        case "default.timezone":
            withDefaultTimeZone(val);
            break;
        // PLAN/SUBMIT Orders
        case "forced.planorders":
            withPlanOrders(Boolean.parseBoolean(val));
            break;
        case "forced.submitorders":
            withSubmitOrders(Boolean.parseBoolean(val));
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return forcedWorkingDayCalendarName == null && forcedNonWorkingDayCalendarName == null && defaultWorkingDayCalendarName == null
                && defaultNonWorkingDayCalendarName == null && planOrders == null && submitOrders == null;
    }

    public ScheduleConfig withForcedWorkingDayCalendarName(String val) {
        this.forcedWorkingDayCalendarName = val;
        return this;
    }

    public ScheduleConfig withForcedNonWorkingDayCalendarName(String val) {
        this.forcedNonWorkingDayCalendarName = val;
        return this;
    }

    public ScheduleConfig withDefaultWorkingDayCalendarName(String val) {
        this.defaultWorkingDayCalendarName = val;
        return this;
    }

    public ScheduleConfig withDefaultNonWorkingDayCalendarName(String val) {
        this.defaultNonWorkingDayCalendarName = val;
        return this;
    }

    public ScheduleConfig withDefaultTimeZone(String val) {
        this.defaultTimeZone = val;
        return this;
    }

    public ScheduleConfig withPlanOrders(boolean val) {
        this.planOrders = val;
        return this;
    }

    public ScheduleConfig withSubmitOrders(boolean val) {
        this.submitOrders = val;
        return this;
    }

    public String getForcedWorkingDayCalendarName() {
        return forcedWorkingDayCalendarName;
    }

    public String getForcedNonWorkingDayCalendarName() {
        return forcedNonWorkingDayCalendarName;
    }

    public String getDefaultWorkingDayCalendarName() {
        return defaultWorkingDayCalendarName;
    }

    public String getDefaultNonWorkingDayCalendarName() {
        return defaultNonWorkingDayCalendarName;
    }

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    public boolean planOrders() {
        return planOrders == null ? false : planOrders;
    }

    public boolean submitOrders() {
        return submitOrders == null ? false : submitOrders;
    }

}
