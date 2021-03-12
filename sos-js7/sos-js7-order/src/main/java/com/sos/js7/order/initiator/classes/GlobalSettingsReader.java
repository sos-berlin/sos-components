package com.sos.js7.order.initiator.classes;

import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.js7.order.initiator.OrderInitiatorSettings;

public class GlobalSettingsReader {

    public OrderInitiatorSettings getSettings(AConfigurationSection section) {
        ConfigurationGlobalsDailyPlan configuration = (ConfigurationGlobalsDailyPlan) section;

        OrderInitiatorSettings settings = new OrderInitiatorSettings();
        settings.setTimeZone(configuration.getTimeZone().getValue());
        settings.setPeriodBegin(configuration.getPeriodBegin().getValue());
        settings.setDailyPlanStartTime(configuration.getStartTime().getValue());
        settings.setDayAheadPlan(configuration.getDaysAheadPlan().getValue());
        settings.setDayAheadSubmit(configuration.getDaysAheadSubmit().getValue());

        return settings;
    }
}
