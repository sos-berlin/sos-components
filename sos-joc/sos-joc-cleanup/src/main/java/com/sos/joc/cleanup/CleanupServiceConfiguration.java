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
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;

import scala.collection.mutable.StringBuilder;

public class CleanupServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceConfiguration.class);

    public static final int MIN_MAX_POOL_SIZE = 5;

    private ZoneId zoneId;
    private Period period;
    private Age orderHistoryAge;
    private Age orderHistoryLogsAge;
    private Age dailyPlanHistoryAge;
    private Age fileTransferHistoryAge;
    private Age auditLogAge;
    private Age monitoringHistoryAge;
    private Age notificationHistoryAge;
    private Age profileAge;
    private Age failedLoginHistoryAge;
    private Age reportingAge;
    private int deploymentHistoryVersions;
    private int batchSize;
    private int maxPoolSize;
    private ForceCleanup forceCleanup;

    private Path hibernateConfiguration;

    public CleanupServiceConfiguration(ConfigurationGlobalsCleanup configuration) {
        this.zoneId = ZoneId.of(configuration.getTimeZone().getValue());
        this.period = new Period(configuration.getPeriod().getValue(), configuration.getPeriodBegin().getValue(), configuration.getPeriodEnd()
                .getValue());
        this.orderHistoryAge = new Age(configuration.getOrderHistoryAge());

        this.orderHistoryLogsAge = new Age(configuration.getOrderHistoryLogsAge());
        if (this.orderHistoryAge.getMinutes() != 0 && this.orderHistoryLogsAge.getMinutes() != 0) {
            if (this.orderHistoryAge.getMinutes() < this.orderHistoryLogsAge.getMinutes()) {
                LOGGER.info(String.format("[change][%s(%s) < %s(%s)][order_history_logs_age=%s]", this.orderHistoryAge.getPropertyName(),
                        this.orderHistoryAge.getConfigured(), this.orderHistoryLogsAge.getPropertyName(), this.orderHistoryLogsAge.getConfigured(),
                        this.orderHistoryAge.getConfigured()));
                this.orderHistoryLogsAge = this.orderHistoryAge.clone(configuration.getOrderHistoryLogsAge().getName());
            }
        }
        this.dailyPlanHistoryAge = new Age(configuration.getDailyPlanHistoryAge());
        this.fileTransferHistoryAge = new Age(configuration.getFileTransferHistoryAge());
        this.auditLogAge = new Age(configuration.getAuditLogAge());
        this.monitoringHistoryAge = new Age(configuration.getMonitoringHistoryAge());
        this.notificationHistoryAge = new Age(configuration.getNotificationHistoryAge());
        this.profileAge = new Age(configuration.getProfileAge());
        this.failedLoginHistoryAge = new Age(configuration.getFailedLoginHistoryAge());
        this.reportingAge = new Age(configuration.getReportingAge());
        try {
            this.deploymentHistoryVersions = Integer.parseInt(configuration.getDeploymentHistoryVersions().getValue());
        } catch (Throwable e) {
            LOGGER.error(String.format("[deployment_history_versions=%s]%s", deploymentHistoryVersions, e.toString()), e);
        }

        try {
            int bz = Integer.parseInt(configuration.getBatchSize().getValue());
            if (bz > 0) {
                this.batchSize = bz;
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s configured=%s]%s", configuration.getBatchSize().getName(), configuration.getBatchSize().getValue(), e
                    .toString()), e);
        }

        try {
            int mpz = Integer.parseInt(configuration.getMaxPoolSize().getValue());
            if (mpz > 0) {
                this.maxPoolSize = mpz;
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[configured %s=%s]%s", configuration.getMaxPoolSize().getName(), configuration.getMaxPoolSize().getValue(), e
                    .toString()), e);
        }

        if (maxPoolSize < MIN_MAX_POOL_SIZE) {
            LOGGER.info(String.format("[configured %s=%s][skip]use MIN_MAX_POOL_SIZE=%s", configuration.getMaxPoolSize().getName(), configuration
                    .getMaxPoolSize().getValue(), MIN_MAX_POOL_SIZE));
            maxPoolSize = MIN_MAX_POOL_SIZE;
        }

        forceCleanup = new ForceCleanup(configuration.getForceCleanup());
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

    public Age getAuditLogAge() {
        return auditLogAge;
    }

    public Age getFileTransferHistoryAge() {
        return fileTransferHistoryAge;
    }

    public Age getMonitoringHistoryAge() {
        return monitoringHistoryAge;
    }

    public Age getNotificationHistoryAge() {
        return notificationHistoryAge;
    }

    public Age getProfileAge() {
        return profileAge;
    }

    public Age getFailedLoginHistoryAge() {
        return failedLoginHistoryAge;
    }

    public Age getReportingAge() {
        return reportingAge;
    }

    public int getDeploymentHistoryVersions() {
        return deploymentHistoryVersions;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public ForceCleanup getForceCleanup() {
        return forceCleanup;
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
        sb.append(" forceCleanup=").append(forceCleanup.force);
        if (forceCleanup.force) {
            sb.append("[");
            sb.append("historyPauseDuration=[");
            sb.append("configured=").append(forceCleanup.historyPauseDurationAge.getConfigured());
            sb.append(",seconds=").append(forceCleanup.historyPauseDurationAge.getSeconds());
            sb.append("]");
            sb.append("historyPauseDelayAge=[");
            sb.append("configured=").append(forceCleanup.historyPauseDelayAge.getConfigured());
            sb.append(",seconds=").append(forceCleanup.historyPauseDelayAge.getSeconds());
            sb.append("]");
            sb.append("]");
        }
        sb.append(",batchSize=").append(batchSize);
        sb.append(",maxPoolSize=").append(maxPoolSize);
        sb.append(",zoneId=").append(zoneId);
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
        sb.append(",auditLogAge=[configured=").append(auditLogAge.getConfigured()).append(",minutes=").append(auditLogAge.getMinutes()).append("]");
        sb.append(",fileTransferHistoryAge=[configured=").append(fileTransferHistoryAge.getConfigured()).append(",minutes=").append(
                fileTransferHistoryAge.getMinutes()).append("]");
        sb.append(",monitoringHistoryAge=[configured=").append(monitoringHistoryAge.getConfigured()).append(",minutes=").append(monitoringHistoryAge
                .getMinutes()).append("]");
        sb.append(",notificationHistoryAge=[configured=").append(notificationHistoryAge.getConfigured()).append(",minutes=").append(
                notificationHistoryAge.getMinutes()).append("]");
        sb.append(",profileAge=[configured=").append(profileAge.getConfigured()).append(",minutes=").append(profileAge.getMinutes()).append("]");
        sb.append(",failedLoginHistoryAge=[configured=").append(failedLoginHistoryAge.getConfigured()).append(",minutes=").append(
                failedLoginHistoryAge.getMinutes()).append("]");
        sb.append(",deploymentHistoryVersions=").append(deploymentHistoryVersions);
        sb.append("]");
        sb.append("]");
        return sb.toString();
    }

    public class ForceCleanup {

        private boolean force = false;
        private Age historyPauseDurationAge;
        private Age historyPauseDelayAge;

        private ForceCleanup(com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup.ForceCleanup entry) {
            try {
                force = Boolean.parseBoolean(entry.getForce());
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s configured=%s]%s", entry.getArgNameForce(), entry.getForce(), e.toString()), e);
            }
            historyPauseDurationAge = new Age(entry.getArgNameHistoryPauseDuration(), entry.getHistoryPauseDuration());
            historyPauseDelayAge = new Age(entry.getArgNameHistoryPauseDelay(), entry.getHistoryPauseDelay());
        }

        public boolean force() {
            return force;
        }

        public Age getHistoryPauseDurationAge() {
            return historyPauseDurationAge;
        }

        public Age getHistoryPauseDelayAge() {
            return historyPauseDelayAge;
        }
    }

    public class Age {

        private String propertyName = null;
        private String configured = null;
        private long minutes = 0;
        private long seconds = 0;

        public Age(ConfigurationEntry entry) {
            this(entry.getName(), entry.getValue());
        }

        public Age(String propertyName, String configured) {
            this.propertyName = propertyName;
            this.configured = configured == null ? "" : configured;
            try {
                minutes = 0;
                seconds = 0;

                if (SOSString.isEmpty(this.configured) || this.configured.equals("0")) {
                } else {
                    if (StringUtils.isNumeric(this.configured)) {
                        this.configured = this.configured + "d";
                    }

                    if (this.configured.endsWith("s")) {
                        seconds = SOSDate.resolveAge("s", this.configured).longValue();
                        if (seconds < 0) {
                            seconds = 0;
                        }
                    } else {
                        minutes = SOSDate.resolveAge("m", this.configured).longValue();
                        if (minutes < 0) {
                            minutes = 0;
                        }
                    }
                    if (seconds == 0 && minutes > 0) {
                        seconds = minutes * 60;
                    }
                }
            } catch (Exception e) {
                minutes = 0;
                seconds = 0;
                LOGGER.error(String.format("[%s]%s", this.configured, e.toString()));
            }
        }

        private Age(String propertyName, String configured, long minutes, long seconds) {
            this.propertyName = propertyName;
            this.configured = configured;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getConfigured() {
            return configured;
        }

        public Age clone(String propertyName) {
            return new Age(propertyName, this.configured, this.minutes, this.seconds);
        }

        public long getMinutes() {
            return minutes;
        }

        public long getSeconds() {
            return seconds;
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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(Period.class.getSimpleName());
            sb.append(" configured=").append(configured);
            sb.append(",weekdays=[");
            if (weekDays == null) {
                sb.append("null");
            } else {
                sb.append(StringUtils.join(weekDays, ","));
            }
            sb.append("]");
            sb.append(",begin=").append(begin);
            sb.append(",end=").append(end);
            sb.append("]");
            return sb.toString();
        }
    }

    public class PeriodTime {

        private String configured = null;
        private int hours = 0;
        private int minutes = 0;
        private int seconds = 0;

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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(PeriodTime.class.getSimpleName());
            sb.append(" configured=").append(configured);
            sb.append(",hours=").append(hours);
            sb.append(",minutes=").append(minutes);
            sb.append(",seconds=").append(seconds);
            sb.append("]");
            return sb.toString();
        }
    }

}
