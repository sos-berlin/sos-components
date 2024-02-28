package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBLayer;
import com.sos.joc.model.reporting.Report;

public abstract class AReporting {
    
    protected enum ReportingType {
        JOBS,
        ORDERS
    }
    
    protected static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    protected static final Path reportingDir = Paths.get("reporting").toAbsolutePath();
    protected static final Path dataDir = reportingDir.resolve("data");
    protected static final Path tmpDir = reportingDir.resolve("tmp");
    private static final Logger LOGGER = LoggerFactory.getLogger(AReporting.class);
    
    protected static final Map<ReportingType, Collection<CSVColumns>> CSV_COLUMNS = Collections.unmodifiableMap(
            new HashMap<ReportingType, Collection<CSVColumns>>() {

                private static final long serialVersionUID = 1L;

                {
                    put(ReportingType.JOBS, getJobsColumns());
                    put(ReportingType.ORDERS, getOrdersColumns());
                }
            });

    protected static final Map<ReportingType, String> DB_TABLE = Collections.unmodifiableMap(new HashMap<ReportingType, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(ReportingType.JOBS, DBLayer.DBITEM_HISTORY_ORDER_STEPS);
            put(ReportingType.ORDERS, DBLayer.DBITEM_HISTORY_ORDERS);
        }
    });

    protected static Path getDataDirectory(ReportingType type) throws IOException {
        Path subD = dataDir.resolve(type.name().toLowerCase());
        Files.createDirectories(subD);
        return subD;
    }
    
    protected static Path createTempDirectory() throws IOException {
        Files.createDirectories(tmpDir);
        return Files.createTempDirectory(tmpDir, "report");
    }
    
    protected static Path createTempDirectoryRelativize() throws IOException {
        return reportingDir.relativize(createTempDirectory());
    }
    
    private static Collection<CSVColumns> getOrdersColumns() {
        Collection<CSVColumns> es = EnumSet.allOf(CSVColumns.class);
        es.removeAll(EnumSet.of(CSVColumns.POSITION, CSVColumns.JOB_NAME, CSVColumns.CRITICALITY, CSVColumns.AGENT_ID,
                CSVColumns.AGENT_NAME));
        return es;
    }

    private static Collection<CSVColumns> getJobsColumns() {
        Collection<CSVColumns> es = EnumSet.allOf(CSVColumns.class);
        es.removeAll(EnumSet.of(CSVColumns.PLANNED_TIME, CSVColumns.ORDER_STATE));
        return es;
    }
    
    protected static byte[] getCsvBytes(Object csv) {
        return getCsvBytes((String) csv);
    }
    
    protected static byte[] getCsvBytes(String csv) {
        return (csv + "\n").getBytes(StandardCharsets.UTF_8);
    }
    
    protected static byte[] getCsvHeadLine(ReportingType type) {
        return getCsvBytes(CSV_COLUMNS.get(type).stream().map(CSVColumns::name).collect(Collectors.joining(";")));
    }
    
    protected static String getCsvHQL(ReportingType type) {
        return CSV_COLUMNS.get(type).stream().map(CSVColumns::hqlValue).collect(Collectors.joining(",';',", "concat(", ")"));
    }
    
    public static void deleteTmpFolder() {
        LOGGER.info("cleanup temporary report directory: " + tmpDir.toString());
        try {
            if (Files.exists(tmpDir)) {
                Files.walk(tmpDir).sorted(Comparator.reverseOrder()).forEach(entry -> {
                    try {
                        if (!tmpDir.equals(entry)) {
                            Files.delete(entry);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(String.format("%1$s couldn't be deleted: %2$s", entry.toString(), e.toString()), e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.warn("", e);
        }
    }
    
    protected static LocalDateTime getLocalDateFrom(final String monthFrom) { //yyyy-MM[-dd]
        if (monthFrom == null) {
            return null; //should not occur
        }
        String[] yearMonthFrom = monthFrom.split("-");
        return LocalDate.of(Integer.valueOf(yearMonthFrom[0]).intValue(), Integer.valueOf(yearMonthFrom[1]).intValue(), 1)
                .atStartOfDay();
    }
    
    protected static LocalDateTime getLocalDateTo(final String monthTo) { //yyyy-MM[-dd]
        if (monthTo != null) {
            return getLocalDateFrom(monthTo).plusMonths(1).minusSeconds(1);
        }
        return null;
    }
    
    protected static LocalDateTime getLocalDateToOrNowIfNull(final String monthTo) { //yyyy-MM[-dd]
        if (monthTo == null) {
            LocalDate now = LocalDate.now();
            return LocalDate.of(now.getYear(), now.getMonth(), 1).atStartOfDay().plusMonths(1).minusSeconds(1);
        }
        return getLocalDateFrom(monthTo).plusMonths(1).minusSeconds(1);
    }
    
}
