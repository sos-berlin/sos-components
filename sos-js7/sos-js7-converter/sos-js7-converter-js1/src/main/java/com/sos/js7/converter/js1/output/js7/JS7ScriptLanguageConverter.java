package com.sos.js7.converter.js1.output.js7;

import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.js1.common.job.ACommonJob;

public class JS7ScriptLanguageConverter {

    private final ACommonJob job;
    private final String language;

    private String className;
    private String javaClassName;
    private YADE yade;

    public JS7ScriptLanguageConverter(ACommonJob job) {
        this.job = job;
        this.language = job.getScript().getLanguage() == null ? "shell" : job.getScript().getLanguage().toLowerCase();
    }

    public void process() {
        switch (language) {
        case "java":
            String jc = job.getScript().getJavaClass();
            switch (jc) {
            // DB
            case "com.sos.jitl.extract.job.ResultSet2CSVJobJSAdapterClass":
            case "com.sos.jitl.managed.job.ManagedDatabaseJob":
                javaClassName = "com.sos.jitl.jobs.db.SQLExecutorJob";
                break;
            case "sos.scheduler.db.JobSchedulerPLSQLJobJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.db.oracle.PLSQLJob";
                break;
            case "sos.scheduler.db.SOSSQLPlusJobJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.db.oracle.SQLPLUSJob";
                break;
            // CHECKHISTORY:
            case "com.sos.jitl.checkhistory.JobSchedulerCheckHistoryJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.checkhistory.CheckHistoryJob";
                break;
            // FILE Operations
            case "sos.scheduler.file.JobSchedulerExistsFile":
                javaClassName = "com.sos.jitl.jobs.file.FileExistsJob";
                break;
            case "sos.scheduler.file.JobSchedulerCanWrite":
                javaClassName = "com.sos.jitl.jobs.file.CanWriteJob";
                break;
            case "sos.scheduler.file.JobSchedulerCopyFile":
                javaClassName = "com.sos.jitl.jobs.file.CopyFileJob";
                break;
            case "sos.scheduler.file.JobSchedulerNotExistsFile":
                javaClassName = "com.sos.jitl.jobs.file.FileNotExistsJob";
                break;
            case "sos.scheduler.file.JobSchedulerRemoveFile":
                javaClassName = "com.sos.jitl.jobs.file.RemoveFileJob";
                break;
            case "sos.scheduler.file.JobSchedulerRenameFile":
                javaClassName = "com.sos.jitl.jobs.file.RenameFileJob";
                break;
            // MAIL
            case "sos.mail.SOSMailProcessInbox":
            case "com.sos.jitl.mailprocessor.SOSMailProcessInboxJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.mail.MailInboxJob";
                break;
            case "sos.scheduler.job.JobSchedulerDequeueMailJob":
            case "com.sos.jitl.housekeeping.dequeuemail.JobSchedulerDequeueMailJobJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.mail.MailJob";
                break;
            // SSH
            case "sos.scheduler.job.SOSSSHJob2JSAdapter":
                javaClassName = "com.sos.jitl.jobs.ssh.SSHJob";
                break;
            // YADE
            case "sos.scheduler.jade.JadeJob":
                className = jc;
                yade = new YADE(false);
                break;
            case "sos.scheduler.jade.SOSJade4DMZJSAdapter":
                className = jc;
                yade = new YADE(true);
                break;
            // SPLIT/JOIN
            case "com.sos.jitl.splitter.JobChainSplitterJSAdapterClass":
            case "com.sos.jitl.join.JobSchedulerJoinOrdersJSAdapterClass":
                className = jc;
                ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "[job " + className + "]", "not implemented yet");
                break;
            default:
                className = jc;
                ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "[job " + className + "]", "not implemented yet");
                break;
            }

            break;
        }
    }

    public String getLanguage() {
        return language;
    }

    public String getClassName() {
        return className;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public YADE getYADE() {
        return yade;
    }

    public class YADE {

        private final boolean dmz;
        private final String bin;

        private YADE(boolean dmz) {
            this.dmz = dmz;
            this.bin = this.dmz ? "JS7_YADE_DMZ_BIN" : "JS7_YADE_BIN";
        }

        public boolean isDMZ() {
            return dmz;
        }

        public String getBin() {
            return bin;
        }
    }

}
