package com.sos.joc.cluster.configuration.globals;

import com.sos.commons.util.SOSDate;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsDailyPlan extends AConfigurationSection {

    private ConfigurationEntry timeZone = new ConfigurationEntry("time_zone", SOSDate.TIMEZONE_UTC, GlobalSettingsSectionValueType.TIMEZONE);
    private ConfigurationEntry periodBegin = new ConfigurationEntry("period_begin", "00:00:00", GlobalSettingsSectionValueType.TIME);
    private ConfigurationEntry startTime = new ConfigurationEntry("start_time", "", GlobalSettingsSectionValueType.TIME);

    private ConfigurationEntry daysAheadPlan = new ConfigurationEntry("days_ahead_plan", "7", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
    private ConfigurationEntry daysAheadSubmit = new ConfigurationEntry("days_ahead_submit", "3", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);

    private ConfigurationEntry projectionsMonthAhead = new ConfigurationEntry("projections_month_ahead", "6",
            GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
    
    private ConfigurationEntry submitOrdersIndividually = new ConfigurationEntry("submit_orders_individually", "false",
            GlobalSettingsSectionValueType.BOOLEAN);
    
    private ConfigurationEntry ageOfPlansToBeClosedAutomatically = new ConfigurationEntry("age_of_plans_to_be_closed_automatically", "1",
            GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);

    public ConfigurationGlobalsDailyPlan() {
        int index = -1;
        timeZone.setOrdering(++index);
        periodBegin.setOrdering(++index);
        startTime.setOrdering(++index);

        daysAheadPlan.setOrdering(++index);
        daysAheadSubmit.setOrdering(++index);

        submitOrdersIndividually.setOrdering(++index);
        
        ageOfPlansToBeClosedAutomatically.setOrdering(++index);
        
        projectionsMonthAhead.setOrdering(++index);
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

    public ConfigurationEntry getProjectionsMonthAhead() {
        return projectionsMonthAhead;
    }
    
    public ConfigurationEntry getAgeOfPlansToBeClosedAutomatically() {
        return ageOfPlansToBeClosedAutomatically;
    }
    
    public boolean getSubmitOrdersIndividually() {
        if(submitOrdersIndividually.getValue() == null) {
            return submitOrdersIndividually.getDefault().equalsIgnoreCase("true");
        }
        return submitOrdersIndividually.getValue().equalsIgnoreCase("true");
    }
}
