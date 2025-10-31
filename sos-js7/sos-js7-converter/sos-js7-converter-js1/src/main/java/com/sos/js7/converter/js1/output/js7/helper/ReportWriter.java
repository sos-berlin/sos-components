package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;

import org.slf4j.Logger;

import com.sos.js7.converter.commons.report.simple.SimpleReportWriter;

public class ReportWriter extends SimpleReportWriter {

    public static final String FILE_NAME_CALENDARS = "Report-Calendars.txt";
    public static final String FILE_NAME_CYCLIC_WORKFLOWS = "Report-CyclicWorkflows.txt";

    public ReportWriter(Logger logger, String caller, Path file) {
        super(logger, caller, file);
    }

    public ReportWriter(Path file) {
        super(file);
    }
}
