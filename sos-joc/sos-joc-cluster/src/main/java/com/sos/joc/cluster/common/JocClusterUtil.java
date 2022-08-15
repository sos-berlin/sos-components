package com.sos.joc.cluster.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;

public class JocClusterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterUtil.class);

    public static final String HISTORY_LOG_NEW_LINE = "\r\n";
    public static final int HISTORY_LOG_TRUNCATE_FIRST_LAST_BYTES = 100 * 1_024;

    public static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(eventId2Instant(eventId));
    }

    public static Instant eventId2Instant(Long eventId) {
        return Instant.ofEpochMilli(eventId / 1000);
    }

    public static Instant timestamp2Instant(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Long getDateAsEventId(Date date) {
        return date == null ? null : date.getTime() * 1_000;
    }

    public static String getBasenameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

    public static int mb2bytes(int mb) {
        return mb * 1_024 * 1_024;
    }

    public static Path truncateHistoryOriginalLogFile(String caller, Path file, Long fileSizeUncomressed, int logApplicableMBSize) {
        StringBuilder prefix = new StringBuilder();
        prefix.append("[JOC][History][").append(file).append("]");

        StringBuilder banner = new StringBuilder();
        banner.append("LOG OUTPUT ").append(SOSShell.byteCountToDisplaySize(fileSizeUncomressed)).append(" ");
        banner.append("EXCEEDS APPLICABLE SIZE OF ").append(logApplicableMBSize).append(" MB ");
        banner.append("AND IS TRUNCATED TO THE FIRST AND LAST 100 KB.");

        LOGGER.warn(String.format("[%s][%s]%s", caller, file, banner));

        StringBuilder between = new StringBuilder();
        between.append(HISTORY_LOG_NEW_LINE).append(prefix).append("LAST 100 KB.").append(HISTORY_LOG_NEW_LINE);
        try {
            StringBuilder result = new StringBuilder();
            result.append(prefix);
            result.append(banner);
            result.append(SOSPath.readFirstLastBytes(file, HISTORY_LOG_TRUNCATE_FIRST_LAST_BYTES, HISTORY_LOG_TRUNCATE_FIRST_LAST_BYTES, between
                    .toString()));

            file = Files.write(file, result.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.warn(String.format("[%s][truncateOriginalLogFile][%s]%s", caller, file, e.toString()), e);
            return null;
        }
        return file;
    }
}
