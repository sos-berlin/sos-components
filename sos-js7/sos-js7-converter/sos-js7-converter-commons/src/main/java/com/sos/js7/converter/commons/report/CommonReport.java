package com.sos.js7.converter.commons.report;

import java.nio.file.Path;
import java.util.Arrays;

import com.sos.commons.util.SOSClassUtil;

public abstract class CommonReport {

    public enum ReportHeader {
        TYPE, FILE, TITLE, MESSAGE
    }

    public enum SuccessReportHeader {
        NAME, VALUE
    }

    public enum MessageType {
        ERROR, WARNING, INFO
    }

    private CSVRecords error = new CSVRecords(ReportHeader.class);
    private CSVRecords analyzer = new CSVRecords(ReportHeader.class);
    private CSVRecords warning = new CSVRecords(ReportHeader.class);
    private CSVRecords summary = new CSVRecords(SuccessReportHeader.class);

    public void addErrorRecord(Path file, String title, Throwable e) {
        addErrorRecord(file, title, new StringBuilder(e.toString()).append("\n").append(SOSClassUtil.getStackTrace(e)).toString());
    }

    public void addErrorRecord(String msg) {
        addErrorRecord(null, null, msg);
    }

    public void addErrorRecord(Path file, String title, String msg) {
        addRecord(error, MessageType.ERROR, file, title, msg);
    }

    public void addAnalyzerRecord(String title, String msg) {
        addAnalyzerRecord(null, title, msg);
    }

    public void addAnalyzerRecord(Path file, String title, String msg) {
        addRecord(analyzer, MessageType.INFO, file, title, msg);
    }

    public void addWarningRecord(String title, String msg) {
        addWarningRecord(null, title, msg);
    }

    public void addWarningRecord(Path file, String title, String msg) {
        addRecord(warning, MessageType.WARNING, file, title, msg);
    }

    private void addRecord(CSVRecords records, MessageType type, Path file, String title, String msg) {
        records.addRecord(Arrays.asList(type.name(), file == null ? "" : file.toString(), title == null ? "" : title, msg));
    }

    public void addSummaryRecord(String val1, int val2) {
        summary.addRecord(Arrays.asList(val1, val2));
    }

    public void addSummaryRecord(String val1, String val2) {
        summary.addRecord(Arrays.asList(val1, val2));
    }

    public CSVRecords getError() {
        return error;
    }

    public CSVRecords getAnalyzer() {
        return analyzer;
    }

    public CSVRecords getWarning() {
        return warning;
    }

    public CSVRecords getSummary() {
        return summary;
    }

    public void clear() {
        error.clear();
        analyzer.clear();
        warning.clear();
        summary.clear();

    }
}
