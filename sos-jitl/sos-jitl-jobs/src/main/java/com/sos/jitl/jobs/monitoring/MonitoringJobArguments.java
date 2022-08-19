package com.sos.jitl.jobs.monitoring;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class MonitoringJobArguments extends JobArguments {

    private JobArgument<String> controllerId = new JobArgument<String>("controller_id", false);
    private JobArgument<String> monitorReportDir = new JobArgument<String>("monitor_report_dir", true);
    private JobArgument<Long> monitorReportMaxFiles = new JobArgument<Long>("monitor_report_max_files", false);
    private JobArgument<Integer> alertdOnFailedOrders = new JobArgument<Integer>("alert_on_failed_orders", false);
    private JobArgument<String> from = new JobArgument<String>("from", false);

    public MonitoringJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public String getControllerId() {
        return controllerId.getValue();
    }

    public void setControllerId(String controller) {
        this.controllerId.setValue(controller);
    }

    public String getFrom() {
        return from.getValue();
    }

    public void setFrom(String from) {
        this.from.setValue(from);
    }

    public String getMonitorReportDir() {
        return monitorReportDir.getValue();
    }

    public void setMonitorReportDir(String monitorReportDir) {
        this.monitorReportDir.setValue(monitorReportDir);
    }

    public Long getMonitorReportMaxFiles() {
        return monitorReportMaxFiles.getValue();
    }

    public void setAlertdOnFailedOrders(Integer alertdOnFailedOrders) {
        this.alertdOnFailedOrders.setValue(alertdOnFailedOrders);
    }

    public Integer getAlertdOnFailedOrders() {
        return alertdOnFailedOrders.getValue();
    }

    public void setMonitorReportMaxFiles(Long monitorReportMaxFiles) {
        this.monitorReportMaxFiles.setValue(monitorReportMaxFiles);
    }

}
