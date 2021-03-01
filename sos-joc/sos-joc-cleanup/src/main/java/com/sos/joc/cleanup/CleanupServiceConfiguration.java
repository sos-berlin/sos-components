package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;

import scala.collection.mutable.StringBuilder;

public class CleanupServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceConfiguration.class);

    public static final String PROPERTY_NAME_PERIOD = "cleanup_period";

    private Period period = new Period("01:00", "04:00");// no default weekdays - if not configured the service will not start
    private ZoneId zoneId = ZoneId.of("UTC");
    private Age orderHistoryAge = new Age("90d");
    private Age dailyPlanHistoryAge = new Age("30d");
    private int deploymentHistoryVersions = 10;
    private int batchSize = 1_000;

    private Path hibernateConfiguration;

    public CleanupServiceConfiguration(Properties properties) {
        String timeZone = properties.getProperty("cleanup_time_zone");
        if (!SOSString.isEmpty(timeZone)) {
            try {
                this.zoneId = ZoneId.of(timeZone);
            } catch (Throwable e) {
                LOGGER.error(String.format("[cleanup_time_zone=%s]%s", timeZone, e.toString()), e);
            }
        }

        String period = properties.getProperty(PROPERTY_NAME_PERIOD);
        if (!SOSString.isEmpty(period)) {
            String periodBegin = properties.getProperty("cleanup_period_begin");
            String periodEnd = properties.getProperty("cleanup_period_end");

            if (SOSString.isEmpty(periodBegin)) {
                periodBegin = this.period.getBegin().getConfigured();
                if (SOSString.isEmpty(periodEnd)) {
                    periodEnd = this.period.getEnd().getConfigured();
                }
            }
            this.period = new Period();
            this.period.set(period, periodBegin, periodEnd);

        }

        String orderHistoryAge = properties.getProperty("cleanup_order_history_age");
        if (!SOSString.isEmpty(orderHistoryAge)) {
            this.orderHistoryAge = new Age(orderHistoryAge.trim());
        }

        String dailyPlanHistoryAge = properties.getProperty("cleanup_daily_plan_history_age");
        if (!SOSString.isEmpty(dailyPlanHistoryAge)) {
            this.dailyPlanHistoryAge = new Age(dailyPlanHistoryAge.trim());
        }

        String deploymentHistoryVersions = properties.getProperty("cleanup_deployment_history_versions");
        if (!SOSString.isEmpty(deploymentHistoryVersions)) {
            try {
                this.deploymentHistoryVersions = Integer.parseInt(deploymentHistoryVersions);
            } catch (Throwable e) {
                LOGGER.error(String.format("[cleanup_deployment_history_versions=%s]%s", deploymentHistoryVersions, e.toString()), e);
            }
        }

        String batchSize = properties.getProperty("cleanup_batch_size");
        if (!SOSString.isEmpty(batchSize)) {
            try {
                int bz = Integer.parseInt(batchSize.trim());
                if (bz > 0) {
                    this.batchSize = bz;
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[cleanup_batch_size=%s]%s", batchSize, e.toString()), e);
            }
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
        sb.append(",dailyPlanHistory=[configured=").append(dailyPlanHistoryAge.getConfigured()).append(",minutes=").append(dailyPlanHistoryAge
                .getMinutes()).append("]");
        sb.append(",deploymentHistoryVersions=").append(deploymentHistoryVersions);
        sb.append("]");
        sb.append("]");
        return sb.toString();
    }

    public class Age {

        private String configured = null;
        private Long minutes;

        public Age(String configured) {
            this.configured = configured;
            try {
                if (StringUtils.isNumeric(this.configured)) {
                    this.configured = this.configured + "d";
                }
                this.minutes = SOSDate.resolveAge("m", this.configured);
            } catch (Exception e) {
                LOGGER.error(String.format("[%s]%s", this.configured, e.toString()));
            }
        }

        public String getConfigured() {
            return configured;
        }

        public Long getMinutes() {
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

        public Period(String periodBegin, String periodEnd) {
            set(null, periodBegin, periodEnd);
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
