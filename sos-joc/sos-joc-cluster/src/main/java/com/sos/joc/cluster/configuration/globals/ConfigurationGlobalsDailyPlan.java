package com.sos.joc.cluster.configuration.globals;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsDailyPlan extends AConfigurationSection {

    private ConfigurationEntry timeZone = new ConfigurationEntry("time_zone", "Etc/UTC", GlobalSettingsSectionValueType.TIMEZONE);
    private ConfigurationEntry periodBegin = new ConfigurationEntry("period_begin", "00:00:00", GlobalSettingsSectionValueType.TIME);
    private ConfigurationEntry startTime = new ConfigurationEntry("start_time", "", GlobalSettingsSectionValueType.TIME);

    private ConfigurationEntry daysAheadPlan = new ConfigurationEntry("days_ahead_plan", "7", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
    private ConfigurationEntry daysAheadSubmit = new ConfigurationEntry("days_ahead_submit", "3", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);

    // 6 months, 6 month, 6 m
    // 2 years, 2 year, 2 y - see com.sos.joc.dailyplan.common.DailyPlanSettings.setProjectionsMonthsAhead
    private ConfigurationEntry projectionsAhead = new ConfigurationEntry("projections_ahead", "6 months", GlobalSettingsSectionValueType.STRING);

    public ConfigurationGlobalsDailyPlan() {
        int index = -1;
        timeZone.setOrdering(++index);
        periodBegin.setOrdering(++index);
        startTime.setOrdering(++index);

        daysAheadPlan.setOrdering(++index);
        daysAheadSubmit.setOrdering(++index);

        projectionsAhead.setOrdering(++index);
    }

    public ConfigurationEntry getTimeZone() {
        return timeZone;
    }

    public ConfigurationEntry getPeriodBegin() {
        return periodBegin;
    }

    public ConfigurationEntry getStartTime() {
        return startTime;
    }

    public ConfigurationEntry getDaysAheadPlan() {
        return daysAheadPlan;
    }

    public ConfigurationEntry getDaysAheadSubmit() {
        return daysAheadSubmit;
    }

    public ConfigurationEntry getProjectionsAhead() {
        return projectionsAhead;
    }
}
