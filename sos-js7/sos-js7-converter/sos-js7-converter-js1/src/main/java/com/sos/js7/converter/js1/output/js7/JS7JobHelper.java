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
    public static final String JS1_JAVA_JITL_YADE_JOB_ADAPTER = "sos.scheduler.job.SOSDExJSAdapterClass";
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
            case JS1_JAVA_JITL_YADE_JOB_ADAPTER:
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
        private Map<String, String> mappingWhenTrue = new HashMap<>();
        private Map<String, String> mappingBoolean = new HashMap<>();
        private MappingDynamic mappingDynamic = null;

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
            case JS1_JAVA_JITL_MAIL_PROCESS_INBOX:
            case JS1_JAVA_JITL_MAIL_PROCESS_INBOX_ADAPTER:
                toRemove.add("create_order");
                toRemove.add("mail_jobchain");
                toRemove.add("mail_order_id");
                toRemove.add("mail_order_state");
                toRemove.add("mail_order_title");
                toRemove.add("mail_scheduler_host");
                toRemove.add("mail_scheduler_port");
                toRemove.add("execute_command");

                mapping.put("mail_server_type", "mail.store.protocol");
                mapping.put("after_process_email", "mail_post_action");
                mapping.put("after_process_email_directory_name", "mail_target_folder");
                mapping.put("mail_directory_name", "mail_file_directory");
                mapping.put("mail_message_folder", "mail_source_folders");
                mapping.put("attachment_directory_name", "mail_attachments_directory");
                mapping.put("max_mails_to_process", "max_processed_mails");
                mapping.put("min_age", "min_mail_age");

                // when true
                mappingWhenTrue.put("delete_message", "mail_post_action=delete");
                mappingWhenTrue.put("copy_mail_to_file", "mail_action=dump");
                mappingWhenTrue.put("copy_attachments_to_file", "mail_action=dump_attachments");

                // convert boolean value
                mappingBoolean.put("mail_use_seen", "only_unread_mails");
                mappingBoolean.put("save_body_as_attachments", "body_as_attachment");

                Map<String, String> mr = new HashMap<>();
                mr.put("mail_host", "mail." + MappingDynamic.TO_REPLACE + ".host");
                mr.put("mail_port", "mail." + MappingDynamic.TO_REPLACE + ".port");
                mr.put("mail_user", "mail." + MappingDynamic.TO_REPLACE + ".user");
                mr.put("mail_password", "mail." + MappingDynamic.TO_REPLACE + ".password");
                mr.put("mail_ssl", "mail." + MappingDynamic.TO_REPLACE + ".ssl.enable");
                mr.put("mail_server_timeout", "mail." + MappingDynamic.TO_REPLACE + ".connectiontimeout");
                mappingDynamic = new MappingDynamic("mail_server_type", mr);
                break;
            case JS1_JAVA_JITL_SSH_JOB:
                toRemove.add("cleanupJobchain");
                toRemove.add("runWithWatchdog");
                toRemove.add("ssh_provider");
                toRemove.add("use_keyagent");
                toRemove.add("simulate_shell_inactivity_timeout");
                toRemove.add("simulate_shell_login_timeout");
                toRemove.add("simulate_shell_prompt_trigger");
                toRemove.add("ignore_hangup_signal");
                toRemove.add("ssh_job_kill_pid_command");
                toRemove.add("ssh_job_terminate_pid_command");
                toRemove.add("ssh_job_get_pid_command");
                toRemove.add("ssh_job_get_child_processes_command");
                toRemove.add("ssh_job_get_active_processes_command");
                toRemove.add("ssh_job_timeout_kill_after");
                toRemove.add("auto_detect_os");
                toRemove.add("ignore_signal");

                mappingBoolean.put("create_environment_variables", "create_env_vars");
                mappingBoolean.put("raise_exception_on_error", "raise_exception_on_error");
                mappingBoolean.put("ignore_error", "ignore_error");
                mappingBoolean.put("ignore_stderr", "ignore_stderr");

                // command
                // command_delimiter
                // command_script
                // command_script_file
                // command_script_param
                mapping.put("ignore_exit_code", "exit_codes_to_ignore");
                mapping.put("preCommand", "pre_command");
                mapping.put("postCommandRead", "post_command_read");
                mapping.put("postCommandDelete", "post_command_delete");
                mapping.put("temp_dir", "tmp_dir");
                break;

            case JS1_JAVA_JITL_YADE_JOB:
                toRemove.add("Scheduler_Transfer_Method");
                toRemove.add("scheduler_file_name");
                toRemove.add("scheduler_file_parent");
                toRemove.add("scheduler_file_path");
                toRemove.add("scheduler_host");
                toRemove.add("scheduler_port");
                toRemove.add("scheduler_job_chain");

                toRemove.add("Background_Service_Host");
                toRemove.add("Background_Service_Port");
                toRemove.add("BackgroundService_Job_Chain_Name");

                toRemove.add("create_order");
                toRemove.add("order_jobscheduler_host");
                toRemove.add("order_jobscheduler_port");
                toRemove.add("order_jobchain_name");
                toRemove.add("create_orders_for_all_files");
                toRemove.add("create_orders_for_new_files");
                toRemove.add("MergeOrderParameter");
                toRemove.add("next_state");

                toRemove.add("async_history");
                toRemove.add("HistoryEntries");

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

        public Map<String, String> getMappingWhenTrue() {
            return mappingWhenTrue;
        }

        public Map<String, String> getMappingBoolean() {
            return mappingBoolean;
        }

        public MappingDynamic getMappingDynamic() {
            return mappingDynamic;
        }
    }

    public class MappingDynamic {

        private static final String TO_REPLACE = "<to_replace>";

        private String paramName;
        private Map<String, String> mapping = new HashMap<>();

        private MappingDynamic(String paramName, Map<String, String> mapping) {
            this.paramName = paramName;
            this.mapping = mapping;
        }

        public Map<String, String> replace(String replace) {
            // return name.replaceAll(TO_REPLACE, replacement);
            Map<String, String> result = new HashMap<>();

            mapping.entrySet().stream().forEach(e -> {
                result.put(e.getKey(), e.getValue().replaceAll(TO_REPLACE, replace));
            });

            return result;
        }

        public String getParamName() {
            return paramName;
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

        private final JavaJITLJobParams params;
        private final boolean dmz;
        private final String bin;

        private YADE(boolean dmz) {
            this.params = new JavaJITLJobParams(dmz ? JS1_JAVA_JITL_YADE_DMZ_JOB : JS1_JAVA_JITL_YADE_JOB);
            this.dmz = dmz;
            this.bin = this.dmz ? "JS7_YADE_DMZ_BIN" : "JS7_YADE_BIN";
        }

        public JavaJITLJobParams getParams() {
            return params;
        }

        public boolean isDMZ() {
            return dmz;
        }

        public String getBin() {
            return bin;
        }
    }

}
