package com.sos.jitl.jobs.monitoring.classes;

public class MonitoringParameters {

    private String monitorFileReportDate;
    private String monitorSubjectReportDate;
    private String monitorReportFile;
    private Integer alertdOnFailedOrders = -1;

    public Integer getAlertdOnFailedOrders() {
        return alertdOnFailedOrders;
    }

    public void setAlertdOnFailedOrders(Integer alertdOnFailedOrders) {
        if (alertdOnFailedOrders != null) {
            this.alertdOnFailedOrders = alertdOnFailedOrders;
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
