package com.sos.js7.order.initiator.classes;

import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalSettingsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingsReader.class);

    public OrderInitiatorSettings getSettings(AConfigurationSection section) {
        ConfigurationGlobalsDailyPlan conf = (ConfigurationGlobalsDailyPlan) section;

        OrderInitiatorSettings settings = new OrderInitiatorSettings();
        settings.setTimeZone(conf.getTimeZone().getValue());
        settings.setPeriodBegin(conf.getPeriodBegin().getValue());
        settings.setDailyPlanStartTime(conf.getStartTime().getValue());
        settings.setDayAheadPlan(conf.getDaysAheadPlan().getValue());
        settings.setDayAheadSubmit(conf.getDaysAheadSubmit().getValue());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("time_zone=%s, period_begin=%s, start_time=%s, day_ahead_plan=%s, day_ahead_submit=%s", conf.getTimeZone()
                    .getValue(), conf.getPeriodBegin().getValue(), conf.getStartTime().getValue(), conf.getDaysAheadPlan().getValue(), conf
                            .getDaysAheadSubmit().getValue()));
        }
        return settings;
    }
}
