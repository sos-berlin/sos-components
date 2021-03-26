package com.sos.joc.cluster.configuration.globals;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsCleanup extends AConfigurationSection {

    public final static String INITIAL_PERIOD = "1,2,3,4,5,6,7";

    private ConfigurationEntry timeZone = new ConfigurationEntry("time_zone", "UTC", GlobalSettingsSectionValueType.TIMEZONE);
    private ConfigurationEntry period = new ConfigurationEntry("period", null, GlobalSettingsSectionValueType.WEEKDAYS);
    private ConfigurationEntry periodBegin = new ConfigurationEntry("period_begin", "01:00", GlobalSettingsSectionValueType.TIME);
    private ConfigurationEntry periodEnd = new ConfigurationEntry("period_end", "04:00", GlobalSettingsSectionValueType.TIME);
    private ConfigurationEntry batchSize = new ConfigurationEntry("batch_size", "1000", GlobalSettingsSectionValueType.POSITIVEINTEGER);

    private ConfigurationEntry orderHistoryAge = new ConfigurationEntry("order_history_age", "90d", GlobalSettingsSectionValueType.DURATION);
    private ConfigurationEntry orderHistoryLogsAge = new ConfigurationEntry("order_history_logs_age", "90d", GlobalSettingsSectionValueType.DURATION);
    private ConfigurationEntry auditLogAge = new ConfigurationEntry("audit_log_age", "90d", GlobalSettingsSectionValueType.DURATION);
    private ConfigurationEntry yadeAge = new ConfigurationEntry("yade_age", "90d", GlobalSettingsSectionValueType.DURATION);
    private ConfigurationEntry dailyPlanHistoryAge = new ConfigurationEntry("daily_plan_history_age", "30d", GlobalSettingsSectionValueType.DURATION);
    private ConfigurationEntry deploymentHistoryVersions = new ConfigurationEntry("deployment_history_versions", "10",
            GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);

    public ConfigurationGlobalsCleanup() {
        int index = -1;
        timeZone.setOrdering(++index);
        period.setOrdering(++index);
        periodBegin.setOrdering(++index);
        periodEnd.setOrdering(++index);
        batchSize.setOrdering(++index);

        orderHistoryAge.setOrdering(++index);
        orderHistoryLogsAge.setOrdering(++index);
        auditLogAge.setOrdering(++index);
        yadeAge.setOrdering(++index);
        dailyPlanHistoryAge.setOrdering(++index);
        deploymentHistoryVersions.setOrdering(++index);
    }

    public ConfigurationEntry getTimeZone() {
        return timeZone;
    }

    public ConfigurationEntry getPeriod() {
        return period;
    }

    public ConfigurationEntry getPeriodBegin() {
        return periodBegin;
    }

    public ConfigurationEntry getPeriodEnd() {
        return periodEnd;
    }

    public ConfigurationEntry getBatchSize() {
        return batchSize;
    }

    public ConfigurationEntry getOrderHistoryAge() {
        return orderHistoryAge;
    }

    public ConfigurationEntry getOrderHistoryLogsAge() {
        return orderHistoryLogsAge;
    }

    public ConfigurationEntry getAuditLogAge() {
        return auditLogAge;
    }

    public ConfigurationEntry getYadeAge() {
        return yadeAge;
    }

    public ConfigurationEntry getDailyPlanHistoryAge() {
        return dailyPlanHistoryAge;
    }

    public ConfigurationEntry getDeploymentHistoryVersions() {
        return deploymentHistoryVersions;
    }

}
