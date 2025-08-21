package com.sos.joc.dailyplan.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public class GlobalSettingsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingsReader.class);

    public DailyPlanSettings getSettings(AConfigurationSection section) {
        ConfigurationGlobalsDailyPlan conf = (ConfigurationGlobalsDailyPlan) section;

        DailyPlanSettings settings = new DailyPlanSettings();
        settings.setTimeZone(conf.getTimeZone().getValue());
        settings.setPeriodBegin(conf.getPeriodBegin().getValue());
        settings.setDailyPlanStartTime(conf.getStartTime().getValue());
        settings.setDaysAheadPlan(conf.getDaysAheadPlan().getValue());
        settings.setDaysAheadSubmit(conf.getDaysAheadSubmit().getValue());
        settings.setProjectionsMonthBefore(conf.getProjectionsMonthBefore().getValue());
        settings.setProjectionsMonthAhead(conf.getProjectionsMonthAhead().getValue());
        settings.setAgeOfPlansToBeClosedAutomatically(conf.getAgeOfPlansToBeClosedAutomatically().getValue());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(settings.toString());
        }
        return settings;
    }
}
