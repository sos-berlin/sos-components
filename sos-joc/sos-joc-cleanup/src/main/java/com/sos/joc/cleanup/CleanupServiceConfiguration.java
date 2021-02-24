package com.sos.joc.cleanup;

import java.time.ZoneId;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;

import scala.collection.mutable.StringBuilder;

public class CleanupServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceConfiguration.class);

    public enum StartupMode {
        DAILY, WEEKLY
    }

    private ZoneId zoneId = ZoneId.of("UTC");
    private StartupMode startupMode = StartupMode.DAILY;
    private Period period = new Period(startupMode, "01:00-03:00");
    private Age age = new Age("30d");
    private int batchSize = 1_000;

    public CleanupServiceConfiguration(Properties properties) {
        String timeZone = properties.getProperty("cleanup_time_zone");
        if (!SOSString.isEmpty(timeZone)) {
            try {
                this.zoneId = ZoneId.of(timeZone);
            } catch (Throwable e) {
                LOGGER.error(String.format("[cleanup_time_zone=%s]%s", timeZone, e.toString()), e);
            }
        }

        String startupMode = properties.getProperty("cleanup_startup_mode");
        if (!SOSString.isEmpty(startupMode)) {
            this.startupMode = getStartupMode(startupMode);
        }
        String period = properties.getProperty("cleanup_period");
        if (!SOSString.isEmpty(period)) {
            this.period = new Period(this.startupMode, period);
        }

        String age = properties.getProperty("cleanup_age");
        if (!SOSString.isEmpty(age)) {
            this.age = new Age(age.trim());
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

    public StartupMode getStartupMode() {
        return startupMode;
    }

    public Period getPeriod() {
        return period;
    }

    public Age getAge() {
        return age;
    }

    public int getBatchSize() {
        return batchSize;
    }

    private StartupMode getStartupMode(String startupMode) {
        startupMode = startupMode.trim().toLowerCase();
        switch (startupMode) {
        case "weekly":
            return StartupMode.WEEKLY;
        default:
            return StartupMode.DAILY;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(getClass().getSimpleName());
        sb.append(" zoneId=").append(zoneId);
        sb.append(",startupMode=").append(startupMode);
        if (age == null) {
            sb.append(",age=null");
        } else {
            sb.append(",age=[configured=").append(age.getConfigured());
            sb.append(",minutes=").append(age.getMinutes()).append("]");
        }
        if (period == null) {
            sb.append(",period=null");
        } else {
            sb.append(",period=[configured=");
            sb.append(period.getConfigured() == null ? "null" : period.getConfigured());
            if (period.getFrom() == null) {
                sb.append(",from=null");
            } else {
                sb.append(",from=[");
                sb.append("configured=").append(period.getFrom().getConfigured());
                sb.append(",hours=").append(period.getFrom().getHours());
                sb.append(",minutes=").append(period.getFrom().getMinutes());
                sb.append(",seconds=").append(period.getFrom().getSeconds());
                sb.append("]");
            }
            if (period.getTo() == null) {
                sb.append(",to=null");
            } else {
                sb.append(",to=[");
                sb.append("configured=").append(period.getTo().getConfigured());
                sb.append(",hours=").append(period.getTo().getHours());
                sb.append(",minutes=").append(period.getTo().getMinutes());
                sb.append(",seconds=").append(period.getTo().getSeconds());
                sb.append("]");
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    public class Age {

        private String configured = null;
        private Long minutes;

        public Age(String configured) {
            this.configured = configured;
            try {
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

        private PeriodPart from = null;
        private PeriodPart to = null;
        private String configured = null;

        public Period(StartupMode mode, String period) {
            this.configured = period.trim();
            String[] arr = period.split("-");
            this.from = new PeriodPart(mode, arr[0].trim());
            if (arr.length > 1) {
                this.to = new PeriodPart(mode, arr[1].trim());
            } else {
                this.to = new PeriodPart(mode, this.from.getHours() - 1);
            }
        }

        public PeriodPart getFrom() {
            return from;
        }

        public PeriodPart getTo() {
            return to;
        }

        public String getConfigured() {
            return configured;
        }

    }

    public class PeriodPart {

        private String configured = null;
        private int hours = 0;
        private int minutes = 0;
        private int seconds = 0;
        private long value = 0;

        public PeriodPart(StartupMode mode, int hours) {
            this(mode, hours < 0 ? "23" : String.valueOf(hours));
        }

        public PeriodPart(StartupMode mode, String time) {
            this.configured = time.trim();
            switch (mode) {
            case WEEKLY:
                break;
            case DAILY:
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

            default:
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
