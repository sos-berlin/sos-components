package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.configuration.JocClusterGlobalSettings;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;

import scala.collection.mutable.StringBuilder;

public class CleanupServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceConfiguration.class);

    public static final String PROPERTY_NAME_PERIOD = "period";

    private ZoneId zoneId;
    private Period period;
    private Age orderHistoryAge;
    private Age orderHistoryLogsAge;
    private Age dailyPlanHistoryAge;
    private int deploymentHistoryVersions;
    private int batchSize;

    private Path hibernateConfiguration;

    public CleanupServiceConfiguration(GlobalSettingsSection settings) {
        setDefaults();

        if (settings == null) {
            return;
        }
        String timeZone = JocClusterGlobalSettings.getValue(settings, "time_zone");
        if (!SOSString.isEmpty(timeZone)) {
            try {
                this.zoneId = ZoneId.of(timeZone);
            } catch (Throwable e) {
                LOGGER.error(String.format("[time_zone=%s]%s", timeZone, e.toString()), e);
            }
        }

        String period = JocClusterGlobalSettings.getValue(settings, PROPERTY_NAME_PERIOD);
        if (!SOSString.isEmpty(period)) {
            String periodBegin = JocClusterGlobalSettings.getValue(settings, "period_begin");
            String periodEnd = JocClusterGlobalSettings.getValue(settings, "period_end");

            if (SOSString.isEmpty(periodBegin)) {
                periodBegin = this.period.getBegin().getConfigured();
                if (SOSString.isEmpty(periodEnd)) {
                    periodEnd = this.period.getEnd().getConfigured();
                }
            }
            this.period = new Period(period, periodBegin, periodEnd);
        }

        String orderHistoryAge = JocClusterGlobalSettings.getValue(settings, "order_history_age");
        if (!SOSString.isEmpty(orderHistoryAge)) {
            this.orderHistoryAge = new Age("order_history_age", orderHistoryAge.trim());
        }

        String orderHistoryLogsAge = JocClusterGlobalSettings.getValue(settings, "order_history_logs_age");
        if (!SOSString.isEmpty(orderHistoryLogsAge)) {
            this.orderHistoryLogsAge = new Age("cleanup_order_history_logs_age", orderHistoryLogsAge.trim());
            if (this.orderHistoryAge.getMinutes() != 0 && this.orderHistoryLogsAge.getMinutes() != 0) {
                if (this.orderHistoryAge.getMinutes() < this.orderHistoryLogsAge.getMinutes()) {
                    LOGGER.info(String.format("[change][%s(%s) < %s(%s)][order_history_logs_age=%s]", this.orderHistoryAge.getPropertyName(),
                            this.orderHistoryAge.getConfigured(), this.orderHistoryLogsAge.getPropertyName(), this.orderHistoryLogsAge
                                    .getConfigured(), this.orderHistoryAge.getConfigured()));
                    this.orderHistoryLogsAge = this.orderHistoryAge.clone("order_history_logs_age");
                }
            }
        }

        String dailyPlanHistoryAge = JocClusterGlobalSettings.getValue(settings, "daily_plan_history_age");
        if (!SOSString.isEmpty(dailyPlanHistoryAge)) {
            this.dailyPlanHistoryAge = new Age("daily_plan_history_age", dailyPlanHistoryAge.trim());
        }

        String deploymentHistoryVersions = JocClusterGlobalSettings.getValue(settings, "deployment_history_versions");
        if (!SOSString.isEmpty(deploymentHistoryVersions)) {
            try {
                this.deploymentHistoryVersions = Integer.parseInt(deploymentHistoryVersions);
            } catch (Throwable e) {
                LOGGER.error(String.format("[deployment_history_versions=%s]%s", deploymentHistoryVersions, e.toString()), e);
            }
        }

        String batchSize = JocClusterGlobalSettings.getValue(settings, "batch_size");
        if (!SOSString.isEmpty(batchSize)) {
            try {
                int bz = Integer.parseInt(batchSize.trim());
                if (bz > 0) {
                    this.batchSize = bz;
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[batch_size=%s]%s", batchSize, e.toString()), e);
            }
        }
    }

    private void setDefaults() {
        try {
            GlobalSettingsSection defaultSettings = JocClusterGlobalSettings.getDefaultSettings(ClusterServices.cleanup);

            GlobalSettingsSectionEntry timezone = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "time_zone");
            this.zoneId = ZoneId.of(timezone.getDefault());

            GlobalSettingsSectionEntry period = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "period");
            GlobalSettingsSectionEntry periodBegin = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "period_begin");
            GlobalSettingsSectionEntry periodEnd = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "period_end");
            this.period = new Period(period.getDefault(), periodBegin.getDefault(), periodEnd.getDefault());

            GlobalSettingsSectionEntry orderHistoryAge = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "order_history_age");
            this.orderHistoryAge = new Age("order_history_age", orderHistoryAge.getDefault());

            GlobalSettingsSectionEntry orderHistoryLogsAge = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "order_history_logs_age");
            this.orderHistoryLogsAge = new Age("order_history_logs_age", orderHistoryLogsAge.getDefault());

            GlobalSettingsSectionEntry dailyPlanHistoryAge = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "daily_plan_history_age");
            this.dailyPlanHistoryAge = new Age("daily_plan_history_age", dailyPlanHistoryAge.getDefault());

            GlobalSettingsSectionEntry deploymentHistoryVersions = JocClusterGlobalSettings.getSectionEntry(defaultSettings,
                    "deployment_history_versions");
            this.deploymentHistoryVersions = Integer.parseInt(deploymentHistoryVersions.getDefault());

            GlobalSettingsSectionEntry batchSize = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "batch_size");
            this.batchSize = Integer.parseInt(batchSize.getDefault());
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public Period getPeriod() {
        return period;
    }

    public Age getOrderHistoryAge() {
        return orderHistoryAge;
    }

    public Age getOrderHistoryLogsAge() {
        return orderHistoryLogsAge;
    }

    public Age getDailyPlanHistoryAge() {
        return dailyPlanHistoryAge;
    }

    public int getDeploymentHistoryVersions() {
        return deploymentHistoryVersions;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setHibernateConfiguration(Path val) {
        hibernateConfiguration = val;
    }

    public Path getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(getClass().getSimpleName());
        sb.append(" zoneId=").append(zoneId);
        sb.append(",period=[");
        sb.append("configured=").append(period.getConfigured());
        sb.append(",begin=[");
        sb.append("configured=").append(period.getBegin().getConfigured());
        sb.append(",hours=").append(period.getBegin().getHours());
        sb.append(",minutes=").append(period.getBegin().getMinutes());
        sb.append(",seconds=").append(period.getBegin().getSeconds());
        sb.append("]");
        sb.append(",end=[");
        sb.append("configured=").append(period.getEnd().getConfigured());
        sb.append(",hours=").append(period.getEnd().getHours());
        sb.append(",minutes=").append(period.getEnd().getMinutes());
        sb.append(",seconds=").append(period.getEnd().getSeconds());
        sb.append("]");
        sb.append("]");
        sb.append(",age=[");
        sb.append("orderHistory=[configured=").append(orderHistoryAge.getConfigured()).append(",minutes=").append(orderHistoryAge.getMinutes())
                .append("]");
        sb.append("orderHistoryLogs=[configured=").append(orderHistoryLogsAge.getConfigured()).append(",minutes=").append(orderHistoryLogsAge
                .getMinutes()).append("]");
        sb.append(",dailyPlanHistory=[configured=").append(dailyPlanHistoryAge.getConfigured()).append(",minutes=").append(dailyPlanHistoryAge
                .getMinutes()).append("]");
        sb.append(",deploymentHistoryVersions=").append(deploymentHistoryVersions);
        sb.append("]");
        sb.append("]");
        return sb.toString();
    }

    public class Age {

        private String propertyName = null;
        private String configured = null;
        private long minutes = 0;

        public Age(String propertyName, String configured) {
            this.propertyName = propertyName;
            this.configured = configured == null ? "" : configured;
            try {
                if (SOSString.isEmpty(this.configured) || this.configured.equals("0")) {
                    minutes = 0;
                } else {
                    if (StringUtils.isNumeric(this.configured)) {
                        this.configured = this.configured + "d";
                    }
                    minutes = SOSDate.resolveAge("m", this.configured).longValue();
                    if (minutes < 0) {
                        minutes = 0;
                    }
                }
            } catch (Exception e) {
                minutes = 0;
                LOGGER.error(String.format("[%s]%s", this.configured, e.toString()));
            }
        }

        private Age(String propertyName, String configured, long minutes) {
            this.propertyName = propertyName;
            this.configured = configured;
            this.minutes = minutes;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getConfigured() {
            return configured;
        }

        public Age clone(String propertyName) {
            return new Age(propertyName, this.configured, this.minutes);
        }

        public long getMinutes() {
            return minutes;
        }
    }

    public class Period {

        private List<Integer> weekDays = null;
        private PeriodTime begin = null;
        private PeriodTime end = null;
        private String configured = null;

        public Period() {

        }

        public Period(String periodWeekdays, String periodBegin, String periodEnd) {
            set(periodWeekdays, periodBegin, periodEnd);
        }

        private void set(String periodWeekdays, String periodBegin, String periodEnd) {
            if (periodWeekdays == null) {
                periodWeekdays = "";
                weekDays = new ArrayList<Integer>();
            } else {
                periodWeekdays = periodWeekdays.trim();
                weekDays = Stream.of(periodWeekdays.split(",", -1)).map(f -> {
                    try {
                        int val = Integer.parseInt(f.trim());
                        if (val <= 0 || val > 7) {
                            return null;
                        }
                        return val;
                    } catch (Throwable e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
                if (weekDays.size() > 0) {
                    weekDays.sort(Comparator.comparing(Integer::valueOf));
                }
            }
            periodBegin = periodBegin.trim();
            if (periodEnd != null) {
                periodEnd = periodEnd.trim();
            }
            configured = getConfiguredPeriod(periodWeekdays, periodBegin, periodEnd);
            begin = new PeriodTime(periodBegin);
            end = periodEnd == null ? new PeriodTime(this.begin.getHours() - 1) : new PeriodTime(periodEnd);
        }

        private String getConfiguredPeriod(String period, String periodBegin, String periodEnd) {
            StringBuilder sb = new StringBuilder(period.replaceAll(" ", "")).append(";");
            sb.append(periodBegin);
            if (!SOSString.isEmpty(periodEnd)) {
                sb.append("-").append(periodEnd);
            }
            return sb.toString();
        }

        public boolean parse(String val) {
            try {
                String period;
                String[] times;

                String[] arr = val.split(";"); // 3,7;10:20:00-19:10:00
                if (arr.length == 1) {// tmp - support old format(10:20:00-19:10:00)
                    period = "1,2,3,4,5,6,7";
                    times = val.split("-");
                } else {
                    period = arr[0];// 3,7
                    times = arr[1].split("-");// 10:20:00-19:10:00
                }
                set(period, times[0], times.length > 1 ? times[1] : null);
                return true;
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
            }
            return false;
        }

        public PeriodTime getBegin() {
            return begin;
        }

        public PeriodTime getEnd() {
            return end;
        }

        public String getConfigured() {
            return configured;
        }

        public List<Integer> getWeekDays() {
            return weekDays;
        }
    }

    public class PeriodTime {

        private String configured = null;
        private int hours = 0;
        private int minutes = 0;
        private int seconds = 0;
        private long value = 0;

        public PeriodTime(int hours) {
            this(hours < 0 ? "23" : String.valueOf(hours));
        }

        public PeriodTime(String time) {
            this.configured = time.trim();

            String[] s = this.configured.split(":");
            switch (s.length) {
            case 1:
                this.hours = Integer.parseInt(s[0]);
                break;
            case 2:
                this.hours = Integer.parseInt(s[0]);
                this.minutes = Integer.parseInt(s[1]);
                break;
            default:
                this.hours = Integer.parseInt(s[0]);
                this.minutes = Integer.parseInt(s[1]);
                this.seconds = Integer.parseInt(s[2]);
                break;
            }

        }

        public String getConfigured() {
            return configured;
        }

        public int getHours() {
            return hours;
        }

        public int getMinutes() {
            return minutes;
        }

        public int getSeconds() {
            return seconds;
        }

        public long getValue() {
            return value;
        }
    }

}
