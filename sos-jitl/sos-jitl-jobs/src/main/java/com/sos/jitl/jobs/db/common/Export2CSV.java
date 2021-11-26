package com.sos.jitl.jobs.db.common;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.time.Instant;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.jitl.jobs.common.JobLogger;

public class Export2CSV {

    public static void export(ResultSet resultSet, Path outputFile, JobLogger logger) {
        export(resultSet, outputFile, null, logger);
    }

    public static void export(ResultSet resultSet, Path outputFile, Builder builder, JobLogger logger) {
        if (builder == null) {
            builder = defaultBuilder();
        }

        File file = SOSPath.toFile(outputFile);
        FileWriter writer = null;
        CSVPrinter printer = null;
        boolean removeOutputFile = false;
        try {
            Instant start = Instant.now();
            int headerRows = 0;
            int dataRows = 0;

            writer = new FileWriter(file);
            printer = new CSVPrinter(writer, builder.build());
            printer.printHeaders(resultSet);
            headerRows++;

            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; ++i) {
                    printer.print(resultSet.getObject(i));
                }
                printer.println();
                dataRows++;

                if (logger != null) {
                    int count = dataRows + headerRows;
                    if (count % 1_000 == 0) {
                        logger.info("[export]%s entries processed ...", count);
                    }
                }
            }
            if (logger != null) {
                logger.info("[export][%s]total rows written = %s (header = %s, data = %s), duration = %s", file, headerRows + dataRows, headerRows,
                        dataRows, SOSDate.getDuration(start, Instant.now()));
            }
        } catch (Throwable e) {
            removeOutputFile = true;
        } finally {
            if (printer != null) {
                try {
                    printer.flush();
                } catch (Exception e) {
                }
                try {
                    printer.close();
                } catch (Exception e) {
                }
            }
            if (writer != null) {
                try {
                    writer.flush();
                } catch (Exception e) {
                }
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
            if (removeOutputFile) {
                try {
                    if (file.exists()) {
                        file.deleteOnExit();
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    public static Builder defaultBuilder() {
        Builder builder = CSVFormat.DEFAULT.builder();
        builder.setDelimiter(",");
        builder.setQuoteMode(QuoteMode.ALL);
        builder.setQuote("\"".charAt(0));
        builder.setRecordSeparator("\r\n");
        builder.setIgnoreEmptyLines(false);
        builder.setNullString("");
        builder.setCommentMarker('#');
        return builder;
    }

}
