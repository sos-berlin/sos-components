package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;

public abstract class AReporting {
    
    protected enum ReportingType {
        JOBS,
        ORDERS
    }
    
    protected static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    protected static final Path dataDir = Globals.sosCockpitProperties.resolvePath("reporting/data");
    
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
    
}
