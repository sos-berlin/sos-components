package com.sos.js7.converter.js1.output.js7;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.js1.common.Script;
import com.sos.js7.converter.js1.common.job.ACommonJob;

public class JS7JobHelper {

    // public static final String JS1_JAVA_JITL_CHECK_HISTORY_JOB = "com.sos.jitl.checkhistory.JobSchedulerCheckHistoryJSAdapterClass";

    public static final String JS1_JAVA_JITL_DB_RESULTSET_TO_CSV_JOB = "com.sos.jitl.extract.job.ResultSet2CSVJobJSAdapterClass";
    public static final String JS1_JAVA_JITL_DB_MANAGED_DATABASE_JOB = "com.sos.jitl.managed.job.ManagedDatabaseJobJSAdapterClass";
    public static final String JS1_JAVA_JITL_DB_ORACLE_PLSQL_JOB = "sos.scheduler.db.JobSchedulerPLSQLJobJSAdapterClass";
    public static final String JS1_JAVA_JITL_DB_ORACLE_SQLPLUS_JOB = "sos.scheduler.db.SOSSQLPlusJobJSAdapterClass";

    public static final String JS1_JAVA_JITL_FO_EXISTS = "sos.scheduler.file.JobSchedulerExistsFile";
    public static final String JS1_JAVA_JITL_FO_NOT_EXISTS = "sos.scheduler.file.JobSchedulerNotExistsFile";
    public static final String JS1_JAVA_JITL_FO_CAN_WRITE = "sos.scheduler.file.JobSchedulerCanWrite";
    public static final String JS1_JAVA_JITL_FO_COPY = "sos.scheduler.file.JobSchedulerCopyFile";
    public static final String JS1_JAVA_JITL_FO_REMOVE = "sos.scheduler.file.JobSchedulerRemoveFile";
    public static final String JS1_JAVA_JITL_FO_RENAME = "sos.scheduler.file.JobSchedulerRenameFile";

    public static final String JS1_JAVA_JITL_MAIL_MANAGED_JOB = "sos.scheduler.managed.JobSchedulerManagedMailJob";
    public static final String JS1_JAVA_JITL_MAIL_PROCESS_INBOX = "sos.mail.SOSMailProcessInbox";
    public static final String JS1_JAVA_JITL_MAIL_PROCESS_INBOX_ADAPTER = "com.sos.jitl.mailprocessor.SOSMailProcessInboxJSAdapterClass";

    public static final String JS1_JAVA_JITL_SSH_JOB = "sos.scheduler.job.SOSSSHJob2JSAdapter";

    public static final String JS1_JAVA_JITL_YADE_JOB = "sos.scheduler.jade.JadeJob";
    public static final String JS1_JAVA_JITL_YADE_DMZ_JOB = "sos.scheduler.jade.SOSJade4DMZJSAdapter";

    private JavaJITLJobHelper javaJITLJob;
    private ShellJobHelper shellJob;

    private String language;

    public JS7JobHelper(ACommonJob job) {
        this.language = job.getScript().getLanguage() == null ? "shell" : job.getScript().getLanguage().toLowerCase();
        switch (language) {
        case "java":
            String jc = job.getScript().getJavaClass();
            switch (jc) {
            // DB
            case JS1_JAVA_JITL_DB_RESULTSET_TO_CSV_JOB:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_DB_RESULTSET_TO_CSV_JOB, "com.sos.jitl.jobs.db.SQLExecutorJob");
                break;
            case JS1_JAVA_JITL_DB_MANAGED_DATABASE_JOB:
                // parameters 1:1
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_DB_MANAGED_DATABASE_JOB, "com.sos.jitl.jobs.db.SQLExecutorJob");
                break;
            case JS1_JAVA_JITL_DB_ORACLE_PLSQL_JOB:
                // parameters 1:1
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_DB_ORACLE_PLSQL_JOB, "com.sos.jitl.jobs.db.oracle.PLSQLJob");
                break;
            case JS1_JAVA_JITL_DB_ORACLE_SQLPLUS_JOB:
                // parameters 1:1
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_DB_ORACLE_SQLPLUS_JOB, "com.sos.jitl.jobs.db.oracle.SQLPLUSJob");
                break;

            // FILE Operations
            case JS1_JAVA_JITL_FO_EXISTS:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_FO_EXISTS, "com.sos.jitl.jobs.file.FileExistsJob");
                break;
            case JS1_JAVA_JITL_FO_NOT_EXISTS:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_FO_NOT_EXISTS, "com.sos.jitl.jobs.file.FileNotExistsJob");
                break;
            case JS1_JAVA_JITL_FO_CAN_WRITE:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_FO_CAN_WRITE, "com.sos.jitl.jobs.file.CanWriteJob");
                break;
            case JS1_JAVA_JITL_FO_COPY:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_FO_COPY, "com.sos.jitl.jobs.file.CopyFileJob");
                break;
            case JS1_JAVA_JITL_FO_REMOVE:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_FO_REMOVE, "com.sos.jitl.jobs.file.RemoveFileJob");
                break;
            case JS1_JAVA_JITL_FO_RENAME:
                // parameters adjusted
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_FO_RENAME, "com.sos.jitl.jobs.file.RenameFileJob");
                break;

            // MAIL
            case JS1_JAVA_JITL_MAIL_PROCESS_INBOX:
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_MAIL_PROCESS_INBOX, "com.sos.jitl.jobs.mail.MailInboxJob");
                break;
            case JS1_JAVA_JITL_MAIL_PROCESS_INBOX_ADAPTER:
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_MAIL_PROCESS_INBOX_ADAPTER, "com.sos.jitl.jobs.mail.MailInboxJob");
                break;
            case JS1_JAVA_JITL_MAIL_MANAGED_JOB:
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_MAIL_MANAGED_JOB, "com.sos.jitl.jobs.mail.MailJob");
                break;
            // SSH
            case JS1_JAVA_JITL_SSH_JOB:
                javaJITLJob = new JavaJITLJobHelper(JS1_JAVA_JITL_SSH_JOB, "com.sos.jitl.jobs.ssh.SSHJob");
                break;
            // YADE
            case JS1_JAVA_JITL_YADE_JOB:
                shellJob = new ShellJobHelper(language, jc, new YADE(false));
                break;
            case JS1_JAVA_JITL_YADE_DMZ_JOB:
                shellJob = new ShellJobHelper(language, jc, new YADE(true));
                break;
            // SPLIT/JOIN
            case Script.JAVA_JITL_SPLITTER_JOB:
            case Script.JAVA_JITL_JOIN_JOB:
                break;
            default:
                shellJob = new ShellJobHelper(language, jc);
                ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "[job " + jc + "]", "not implemented yet");
                break;
            }
            break;
        default:
            shellJob = new ShellJobHelper(language, job.getScript() == null ? null : job.getScript().getComClass());
            break;
        }
    }

    public String getLanguage() {
        return language;
    }

    public boolean createJS7Job() {
        return javaJITLJob != null || shellJob != null;
    }

    public JavaJITLJobHelper getJavaJITLJob() {
        return javaJITLJob;
    }

    public ShellJobHelper getShellJob() {
        return shellJob;
    }

    public class JavaJITLJobHelper {

        private final String oldJavaClass;
        private final String newJavaClass;
        private final JavaJITLJobParams params;

        private JavaJITLJobHelper(String oldJavaClass, String newJavaClass) {
            this.oldJavaClass = oldJavaClass;
            this.newJavaClass = newJavaClass;
            this.params = new JavaJITLJobParams(oldJavaClass);
        }

        public String getOldJavaClass() {
            return oldJavaClass;
        }

        public String getNewJavaClass() {
            return newJavaClass;
        }

        public JavaJITLJobParams getParams() {
            return params;
        }
    }

    public class JavaJITLJobParams {

        private Set<String> toRemove = new HashSet<>();
        private Map<String, String> toAdd = new HashMap<>();
        private Map<String, String> mapping = new HashMap<>();

        private JavaJITLJobParams(String oldClassName) {
            switch (oldClassName) {
            case JS1_JAVA_JITL_DB_RESULTSET_TO_CSV_JOB:
                toRemove.add("delimiter");
                toRemove.add("quote_character");
                toRemove.add("escape_character");
                toRemove.add("record_separator");
                toRemove.add("skip_header");

                toAdd.put("resultset_as_variables", "CSV");
                toAdd.put("exec_returns_resultset", "true");

                mapping.put("output_file", "result_file");
                mapping.put("statement", "command");
                break;
            case JS1_JAVA_JITL_FO_EXISTS:
            case JS1_JAVA_JITL_FO_NOT_EXISTS:
            case JS1_JAVA_JITL_FO_CAN_WRITE:
            case JS1_JAVA_JITL_FO_COPY:
            case JS1_JAVA_JITL_FO_REMOVE:
            case JS1_JAVA_JITL_FO_RENAME:
                toRemove.add("count_files");
                toRemove.add("create_order");
                toRemove.add("create_orders_for_all_files");
                toRemove.add("create_orders_for_new_files");
                toRemove.add("param_name_file_path");
                toRemove.add("order_jobchain_name");
                toRemove.add("next_state");
                toRemove.add("merge_order_parameter");
                toRemove.add("on_empty_result_set");

                mapping.put("file", "source_file");
                break;
            case JS1_JAVA_JITL_MAIL_MANAGED_JOB:
                toRemove.add("queue_failed_prefix");
                toRemove.add("queue_directory");
                toRemove.add("queue_mail_on_error");

                mapping.put("host", "mail.smtp.host");
                mapping.put("port", "mail.smtp.port");
                mapping.put("smtp_user", "mail.smtp.user");
                mapping.put("smtp_password", "mail.smtp.password");
                break;
            case "XXXX":
                // case JS1_JAVA_JITL_MAIL_PROCESS_INBOX:
                // case JS1_JAVA_JITL_MAIL_PROCESS_INBOX_ADAPTER:
                toRemove.add("create_order");
                toRemove.add("mail_jobchain");
                toRemove.add("mail_order_id");
                toRemove.add("mail_order_state");
                toRemove.add("mail_order_title");
                toRemove.add("mail_scheduler_host");
                toRemove.add("mail_scheduler_port");

                toRemove.add("execute_command");
                // toRemove.add("mail_action");
                // toRemove.add("mail_body_pattern");
                toRemove.add("mail_directory_name");
                toRemove.add("mail_dump_dir");// alias für mail_directory_name
                toRemove.add("mail_message_folder");
                toRemove.add("mail_server_timeout");
                // toRemove.add("mail_subject_filter");
                toRemove.add("mail_from_filter");
                // toRemove.add("mail_subject_pattern");
                // toRemove.add("attachment_file_name_pattern");
                toRemove.add("mail_use_seen");
                toRemove.add("copy_attachments_to_file");
                toRemove.add("delete_message");// delete_mail

                mapping.put("mail_server_type", "mail.store.protocol");
                mapping.put("mail_host", "mail.imap.host");
                mapping.put("mail_port", "mail.imap.port");
                mapping.put("mail_user", "mail.imap.user");
                mapping.put("mail_password", "mail.imap.password");
                mapping.put("mail_ssl", "mail.imap.ssl.enable");

                mapping.put("max_mails_to_process", "max_processed_mails");
                mapping.put("min_age", "min_mail_age");

                toRemove.add("after_process_email");// mail_post_action?
                toRemove.add("after_process_email_directory_name");// mail_target_folder?
                toRemove.add("attachment_directory_name");// mail_attachments_directory
                toRemove.add("copy_mail_to_file");// dump
                toRemove.add("save_body_as_attachments");// body_as_attachment

                break;
            }
        }

        public Set<String> getToRemove() {
            return toRemove;
        }

        public Map<String, String> getToAdd() {
            return toAdd;
        }

        public Map<String, String> getMapping() {
            return mapping;
        }
    }

    public class ShellJobHelper {

        private final String language;
        private final String className;
        private final YADE yade;

        private ShellJobHelper(String language, String className) {
            this(language, className, null);
        }

        private ShellJobHelper(String language, String className, YADE yade) {
            this.language = language;
            this.className = className;
            this.yade = yade;
        }

        public String getLanguage() {
            return this.language;
        }

        public String getClassName() {
            return className;
        }

        public YADE getYADE() {
            return yade;
        }

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
