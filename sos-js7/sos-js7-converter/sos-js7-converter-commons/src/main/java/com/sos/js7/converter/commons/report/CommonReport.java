package com.sos.js7.converter.commons.report;

import java.nio.file.Path;
import java.util.Arrays;

import com.sos.commons.util.SOSClassUtil;

public abstract class CommonReport {

    public enum ErrorReportHeader {
        FILE, JOB, TYPE, ERROR
    }

    public enum SuccessReportHeader {
        NAME, VALUE
    }

    public enum ErrorType {
        ERROR, WARNING
    }

    private CSVRecords error = new CSVRecords(ErrorReportHeader.class);
    private CSVRecords success = new CSVRecords(SuccessReportHeader.class);

    public void addErrorRecord(Path file, String job, String msg, Throwable e) {
        addErrorRecord(file, job, ErrorType.ERROR, new StringBuilder(msg).append(":").append(e.toString()).append("\n").append(SOSClassUtil
                .getStackTrace(e)).toString());
    }

    public void addErrorRecord(Path file, String job, Throwable e) {
        addErrorRecord(file, job, ErrorType.ERROR, new StringBuilder(e.toString()).append("\n").append(SOSClassUtil.getStackTrace(e)).toString());
    }

    public void addErrorRecord(String msg) {
        addErrorRecord(null, null, ErrorType.ERROR, msg);
    }

    public void addErrorRecord(ErrorType type, String msg) {
        addErrorRecord(null, null, type, msg);
    }

    public void addErrorRecord(Path file, String job, ErrorType type, String msg) {
        error.addRecord(Arrays.asList(file == null ? "" : file.toString(), job == null ? "" : job, type.name(), msg));
    }

    public void addSuccessRecord(String val1, int val2) {
        success.addRecord(Arrays.asList(val1, val2));
    }

    public void addSuccessRecord(String val1, String val2) {
        success.addRecord(Arrays.asList(val1, val2));
    }

    public CSVRecords getError() {
        return error;
    }

    public CSVRecords getSuccess() {
        return success;
    }
}
