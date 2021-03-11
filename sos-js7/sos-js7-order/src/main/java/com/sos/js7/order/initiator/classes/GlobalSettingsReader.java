package com.sos.js7.order.initiator.classes;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.configuration.JocClusterGlobalSettings;
import com.sos.joc.cluster.configuration.JocClusterGlobalSettings.DefaultSections;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;
import com.sos.js7.order.initiator.OrderInitiatorSettings;

public class GlobalSettingsReader {

    public OrderInitiatorSettings getSettings(SOSHibernateSession session) throws Exception {
        return getSettings(JocCluster.getStoredSettings(session, ClusterServices.dailyplan));
    }

    public OrderInitiatorSettings getSettings(GlobalSettingsSection globalSettings) throws Exception {
        OrderInitiatorSettings settings = new OrderInitiatorSettings();
        setDefaults(settings);
        if (globalSettings == null) {
            return settings;
        }

        String timeZone = JocClusterGlobalSettings.getValue(globalSettings, "time_zone");
        if (!SOSString.isEmpty(timeZone)) {
            settings.setTimeZone(timeZone);
        }

        String periodBegin = JocClusterGlobalSettings.getValue(globalSettings, "period_begin");
        if (!SOSString.isEmpty(periodBegin)) {
            settings.setPeriodBegin(periodBegin);
        }

        String startTime = JocClusterGlobalSettings.getValue(globalSettings, "start_time");
        if (!SOSString.isEmpty(startTime)) {
            settings.setDailyPlanStartTime(startTime);
        }

        String daysAheadPlan = JocClusterGlobalSettings.getValue(globalSettings, "days_ahead_plan");
        if (!SOSString.isEmpty(daysAheadPlan)) {
            settings.setDayAheadPlan(daysAheadPlan);
        }

        String daysAheadSubmit = JocClusterGlobalSettings.getValue(globalSettings, "days_ahead_submit");
        if (!SOSString.isEmpty(daysAheadSubmit)) {
            settings.setDayAheadSubmit(daysAheadSubmit);
        }
        return settings;
    }

    private void setDefaults(OrderInitiatorSettings settings) {
        GlobalSettingsSection defaultSettings = JocClusterGlobalSettings.getDefaultSettings(DefaultSections.dailyplan);
        GlobalSettingsSectionEntry timezone = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "time_zone");
        settings.setTimeZone(timezone.getDefault());

        GlobalSettingsSectionEntry periodBegin = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "period_begin");
        settings.setPeriodBegin(periodBegin.getDefault());

        GlobalSettingsSectionEntry startTime = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "start_time");
        settings.setDailyPlanStartTime(startTime.getDefault());

        GlobalSettingsSectionEntry daysAheadPlan = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "days_ahead_plan");
        settings.setDayAheadPlan(daysAheadPlan.getDefault());

        GlobalSettingsSectionEntry daysAheadSubmit = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "days_ahead_submit");
        settings.setDayAheadSubmit(daysAheadSubmit.getDefault());
    }

}
