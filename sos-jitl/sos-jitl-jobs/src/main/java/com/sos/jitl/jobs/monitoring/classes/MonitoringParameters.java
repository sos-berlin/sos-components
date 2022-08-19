package com.sos.jitl.jobs.monitoring.classes;

public class MonitoringParameters {

    private String monitorFileReportDate;
    private String monitorSubjectReportDate;
    private String monitorReportFile;
    private Integer maxFailedOrders = -1;

    public Integer getMaxFailedOrders() {
        return maxFailedOrders;
    }

    public void setMaxFailedOrders(Integer maxFailedOrders) {
        if (maxFailedOrders != null) {
            this.maxFailedOrders = maxFailedOrders;
        }
    }

    public String getMonitorFileReportDate() {
        return monitorFileReportDate;
    }

    public void setMonitorFileReportDate(String monitorFileReportDate) {
        this.monitorFileReportDate = monitorFileReportDate;
    }

    public String getMonitorSubjectReportDate() {
        return monitorSubjectReportDate;
    }

    public void setMonitorSubjectReportDate(String monitorSubjectReportDate) {
        this.monitorSubjectReportDate = monitorSubjectReportDate;
    }

    public String getMonitorReportFile() {
        return monitorReportFile;
    }

    public void setMonitorReportFile(String monitorReportFile) {
        this.monitorReportFile = monitorReportFile;
    }

}
