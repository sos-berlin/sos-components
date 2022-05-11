package com.sos.js7.converter.commons.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;

/** <br/>
 * TODO several report writer types */
public class ReportWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportWriter.class);

    public static void write(Path outputFile, CSVRecords records) {
        if (outputFile == null) {
            LOGGER.error("[write][skip]missing outputFile");
            return;
        }
        if (records == null || records.getRecords().size() == 0) {
            LOGGER.info("[write][skip]missing records");
            return;
        }
        if (!Files.exists(outputFile.getParent())) {
            try {
                Files.createDirectories(outputFile.getParent());
            } catch (IOException e) {
                LOGGER.error(String.format("[write][%s][can't create the parent directory]%s", outputFile, e.toString()), e);
            }
        }

        File file = SOSPath.toFile(outputFile);
        FileWriter writer = null;
        CSVPrinter printer = null;
        boolean removeOutputFile = false;
        try {
            Builder builder = defaultBuilder();
            builder.setHeader(records.getHeader());

            writer = new FileWriter(file);
            printer = new CSVPrinter(writer, builder.build());
            int i = 0;
            for (Iterable<?> record : records.getRecords()) {
                printer.printRecord(record);
                i++;
            }
            LOGGER.info(String.format("[write][%s][total rows written]%s ", file, i));
        } catch (Throwable e) {
            LOGGER.error(String.format("[write][%s][removeOutputFile=true]%s", outputFile, e.toString()), e);
            removeOutputFile = true;
        } finally {
            close(printer);
            close(writer);
            removeOutputFile(file, removeOutputFile);
        }
    }

    private static void close(CSVPrinter printer) {
        if (printer != null) {
            try {
                printer.flush();
            } catch (Throwable e) {
            }
            try {
                printer.close();
            } catch (Throwable e) {
            }
        }
    }

    private static void close(FileWriter writer) {
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
    }

    private static void removeOutputFile(File file, boolean remove) {
        if (remove) {
            try {
                if (file.exists()) {
                    file.deleteOnExit();
                }
            } catch (Throwable ex) {
            }
        }
    }

    private static Builder defaultBuilder() {
        Builder builder = CSVFormat.DEFAULT.builder();
        builder.setDelimiter(",");
        builder.setQuoteMode(QuoteMode.ALL);
        builder.setQuote("\"".charAt(0));
        builder.setEscape('\\');
        builder.setRecordSeparator("\r\n");
        builder.setIgnoreEmptyLines(false);
        builder.setNullString("");
        builder.setCommentMarker('#');
        return builder;
    }
}
