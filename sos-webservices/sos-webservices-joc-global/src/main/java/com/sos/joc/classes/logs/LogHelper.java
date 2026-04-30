package com.sos.joc.classes.logs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Predicate;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.model.log.LogBaseRequest;
import com.sos.joc.model.log.LogResponse;
import com.sos.joc.model.log.RequestLevel;

import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;

public class LogHelper {
    
    private static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss,SSS").withZone(ZoneId.of("UTC"));

    public static LogLevel getLogLevel(RequestLevel level) {
        if (level == null) {
            return LogLevel.info();
        }
        switch (level) {
        case ERROR:
            return LogLevel.error();
        case DEBUG:
            return LogLevel.debug();
        default:
            return LogLevel.info();
        }
    }

    public static String getControllerDownloadFilename(DBItemInventoryJSInstance dbItem, RequestLevel level, Instant dateFrom,
            Optional<Instant> dateTo, Instant now, OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(getControllerPrefix(dbItem), level, dateFrom, dateTo, now, numOfLines, compressed);
    }

    public static String getAgentDownloadFilename(String agentId, Integer isDirector, RequestLevel level, Instant dateFrom,
            Optional<Instant> dateTo, Instant now, OptionalLong numOfLines, boolean compressed) {
        return getDownloadFilename(getAgentPrefix(agentId, isDirector), level, dateFrom, dateTo, now, numOfLines, compressed);
    }
    
    public static Instant getInstantFromZoneId(LogBaseRequest in, ZoneId zoneId, boolean dateTo) {
        Instant instant;
        if (dateTo) {
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateTo()), dateTo, in.getTimeZone());
        } else {
            instant = JobSchedulerDate.getInstantFromDateStr(JobSchedulerDate.setRelativeDateIntoPast(in.getDateFrom()), dateTo, in.getTimeZone());
            Instant now = Instant.now();
            if (now.isBefore(instant)) {
                instant = now;
            }
        }
        return JobSchedulerDate.getInstantFromZoneId(instant, zoneId);
    }
    
    public static Predicate<byte[]> dateToIsReached(Optional<Instant> instantTo, ZoneId zoneId) {
        return logLine -> {
            if (instantTo.isPresent()) {
                String first35Chars = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(logLine, 0, Math.min(35, logLine.length))).toString();
                try {
                    Instant instant = JobSchedulerDate.getInstantOfZoneId(first35Chars, zoneId);
                    return instant.isBefore(instantTo.get());
                } catch (DateTimeException e) {
                    return true;
                }
            }
            return true;
        };
    }
    
    public static Predicate<KeyedLogLine> dateToIsReached(Optional<Instant> instantTo, ZoneId zoneId, LogResponse entity) {
        return keyedLogLine -> {
            if (instantTo.isPresent()) {
                try {
                    Instant instant = JobSchedulerDate.getInstantOfZoneId(keyedLogLine.line().substring(0, 35), zoneId);
                    entity.setIsComplete(!instant.isBefore(instantTo.get()));
                    return !entity.getIsComplete();
                } catch (DateTimeException e) {
                    return true;
                }
            }
            return true;
        };
    }
    
    public static String formattedTimeStamp(Instant instant) {
        return dateTimeFormat.format(instant);
    }

    private static String getDownloadFilename(String productPrefix, RequestLevel level, Instant dateFrom, Optional<Instant> dateTo, Instant now,
            OptionalLong numOfLines, boolean compressed) {
        String dtToOrLines = getDateToOrLines(dateTo, now, numOfLines);
        // length: prefix + 15 + <=15 + <=6 + 4 + <=3 --> max. prefix + 43 (<=162)
        return String.format("%s%s%s%s.log%s", productPrefix, getFirst14Digits(dateFrom), dtToOrLines, getLogLevelSuffix(level), compressed ? ".gz"
                : "");
    }

    private static String getControllerPrefix(DBItemInventoryJSInstance dbItem) {
        String serverRoleSuffix = !dbItem.getIsCluster() ? "" : (dbItem.getIsPrimary() ? "-primary" : "-backup");
        // length: <=100 + <=8 --> max. 119
        return String.format("%s%s-", dbItem.getControllerId(), serverRoleSuffix);
    }

    private static String getAgentPrefix(final String agentId, Integer isDirector) {
        // truncate AgentId!!! because of filename length
        // length: <=100 + <=8 --> max. 119
        return String.format("%s%s-", (agentId.length() > 100) ? agentId.substring(0, 100) : agentId, getAgentRoleSuffix(isDirector));
    }
    
    private static String getAgentRoleSuffix(Integer isDirector) {
        if (isDirector == null || isDirector == 0) {
            return "";
        } else if (isDirector == 1) {
            return "-primary";
        } else {
            return "-backup";
        }
    }

    private static String getDateToOrLines(Optional<Instant> dateTo, Instant now, OptionalLong numOfLines) {
        Optional<String> dtTo = dateTo.map(LogHelper::getFirst14Digits);
        String dtToOrLines = "";
        if (dtTo.isPresent()) {
            dtToOrLines = "-" + dtTo.get();
        } else if (numOfLines.isPresent()) {
            dtToOrLines = "-l" + numOfLines.getAsLong();
        } else {
            dtToOrLines = "-" + getFirst14Digits(now);
        }
        return dtToOrLines;
    }

    private static String getLogLevelSuffix(RequestLevel level) {
        return Optional.ofNullable(level).filter(l -> !l.equals(RequestLevel.INFO)).map(RequestLevel::value).map(String::toLowerCase).map(s -> "-"
                + s).orElse("");
    }

    private static String getFirst14Digits(Instant instant) {
        return instant.toString().replaceAll("[^0-9]", "").substring(0, 14);
    }
    
}
