package com.sos.js7.converter.commons.report;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConverterReportWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterReportWriter.class);

    public static void writeParserReport(Path summaryReport, Path errorReport, Path warningReport, Path analyzerReport) {
        String method = "writeParserReport";
        if (ParserReport.INSTANCE.getSummary().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JIL][summary][start]...", method));
            ReportWriter.write(summaryReport, ParserReport.INSTANCE.getSummary());
            LOGGER.info(String.format("[%s][JIL][summary][end]", method));
        }
        if (ParserReport.INSTANCE.getError().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JIL][error][start]...", method));
            ReportWriter.write(errorReport, ParserReport.INSTANCE.getError());
            LOGGER.info(String.format("[%s][JIL][error][end]", method));
        }
        if (ParserReport.INSTANCE.getWarning().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JIL][warning][start]...", method));
            ReportWriter.write(warningReport, ParserReport.INSTANCE.getWarning());
            LOGGER.info(String.format("[%s][JIL][warning][end]", method));
        }
        if (ParserReport.INSTANCE.getAnalyzer().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JIL][analyzer][start]...", method));
            ReportWriter.write(analyzerReport, ParserReport.INSTANCE.getAnalyzer());
            LOGGER.info(String.format("[%s][JIL][analyzer][end]", method));
        }
    }

    public static void writeConverterReport(Path errorReport, Path warningReport, Path analyzerReport) {
        String method = "writeConverterReport";
        if (ConverterReport.INSTANCE.getError().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][error][start]...", method));
            ReportWriter.write(errorReport, ConverterReport.INSTANCE.getError());
            LOGGER.info(String.format("[%s][JS7][error][end]", method));
        }
        if (ConverterReport.INSTANCE.getWarning().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][warning][start]...", method));
            ReportWriter.write(warningReport, ConverterReport.INSTANCE.getWarning());
            LOGGER.info(String.format("[%s][JS7][warning][end]", method));
        }
        if (ConverterReport.INSTANCE.getAnalyzer().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][analyzer][start]...", method));
            ReportWriter.write(analyzerReport, ConverterReport.INSTANCE.getAnalyzer());
            LOGGER.info(String.format("[%s][JS7][analyzer][end]", method));
        }
    }

    public static void writeSummaryReport(Path summaryReport) {
        ReportWriter.write(summaryReport, ConverterReport.INSTANCE.getSummary());
    }
}
