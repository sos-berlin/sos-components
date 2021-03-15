package com.sos.js7.order.initiator.classes;

import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalSettingsReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingsReader.class);


    public OrderInitiatorSettings getSettings(AConfigurationSection section) {
        ConfigurationGlobalsDailyPlan configuration = (ConfigurationGlobalsDailyPlan) section;

        OrderInitiatorSettings settings = new OrderInitiatorSettings();
        settings.setTimeZone(configuration.getTimeZone().getValue());
        settings.setPeriodBegin(configuration.getPeriodBegin().getValue());
        settings.setDailyPlanStartTime(configuration.getStartTime().getValue());
        settings.setDayAheadPlan(configuration.getDaysAheadPlan().getValue());
        settings.setDayAheadSubmit(configuration.getDaysAheadSubmit().getValue());

        LOGGER.debug("Property timeZone:" + configuration.getTimeZone().getValue());
        LOGGER.debug("Property periodBegin:" + configuration.getPeriodBegin().getValue());
        LOGGER.debug("Property start_time:" + configuration.getStartTime().getValue());
        LOGGER.debug("Property day_ahead_plan:" + configuration.getDaysAheadPlan().getValue());
        LOGGER.debug("Property day_ahead_submit:" + configuration.getDaysAheadSubmit().getValue());

        return settings;
    }
}
