package com.sos.js7.converter.commons.report.simple;

import java.nio.file.Path;

import org.slf4j.Logger;

import com.sos.commons.util.SOSPath;

public class SimpleReportWriter {

    private static final String LINE_DELIMITER = "###################################################################################";

    private final Path file;

    private Logger logger;
    private String caller;

    public SimpleReportWriter(Path file) {
        this(null, null, file);
    }

    public SimpleReportWriter(Logger logger, String caller, Path file) {
        this.logger = logger;
        this.caller = caller;
        this.file = file;
    }

    public void cleanup() {
        try {
            SOSPath.deleteIfExists(file);
        } catch (Exception e) {

        }
    }

    public void resetLogger() {
        logger = null;
        caller = null;
    }

    public void writeLine(String msg) {
        if (logger != null && caller != null) {
            logger.info("[" + caller + "]" + msg);
        }
        writeLineToFile(msg);
    }

    public void writeEmptyLine() {
        writeLine("");
    }

    public void writeLine(Logger logger, String caller, String msg) {
        logger.info("[" + caller + "]" + msg);
        writeLineToFile(msg);
    }

    public void writeDelimiterLine() {
        try {
            SOSPath.appendLine(file, LINE_DELIMITER);
        } catch (Exception e) {
        }
    }

    private void writeLineToFile(String msg) {
        try {
            SOSPath.appendLine(file, msg);
        } catch (Exception e) {
        }
    }
}
