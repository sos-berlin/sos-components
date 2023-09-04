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
        settings.setDayAheadPlan(conf.getDaysAheadPlan().getValue());
        settings.setDayAheadSubmit(conf.getDaysAheadSubmit().getValue());
        settings.setProjectionsMonthsAhead(conf.getProjectionsAhead().getValue());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "time_zone=%s, period_begin=%s, start_time=%s, days_ahead_plan=%s, days_ahead_submit=%s, projection_ahead=%smonths(configured=%s)",
                    conf.getTimeZone().getValue(), conf.getPeriodBegin().getValue(), conf.getStartTime().getValue(), conf.getDaysAheadPlan()
                            .getValue(), conf.getDaysAheadSubmit().getValue(), settings.getProjectionsMonthsAhead(), conf.getProjectionsAhead()
                                    .getValue()));
        }
        return settings;
    }
}
