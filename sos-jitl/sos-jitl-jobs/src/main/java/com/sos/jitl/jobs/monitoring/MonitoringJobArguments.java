package com.sos.jitl.jobs.monitoring;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class MonitoringJobArguments extends JobArguments {

    private JobArgument<String> controllerId = new JobArgument<String>("controller_id", true);
    private JobArgument<String> monitorReportDir = new JobArgument<String>("monitor_report_dir", true);
    private JobArgument<Long> monitorReportMaxFiles = new JobArgument<Long>("monitor_report_max_files", true);
    private JobArgument<String> mailSmtpFrom = new JobArgument<String>("mail_smtp_from", false);

    public MonitoringJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public String getControllerId() {
        return controllerId.getValue();
    }

    public void setControllerId(String controller) {
        this.controllerId.setValue(controller);
    }

    public String getMailSmtpFrom() {
        return mailSmtpFrom.getValue();
    }

    public void setMailSmtpFrom(String mailSmtpFrom) {
        this.mailSmtpFrom.setValue(mailSmtpFrom);
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

    public void setMonitorReportMaxFiles(Long monitorReportMaxFiles) {
        this.monitorReportMaxFiles.setValue(monitorReportMaxFiles);
    }

}
