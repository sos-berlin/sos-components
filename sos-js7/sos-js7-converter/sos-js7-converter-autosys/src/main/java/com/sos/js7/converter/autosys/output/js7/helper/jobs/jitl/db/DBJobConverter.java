package com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.job.Job;
import com.sos.js7.converter.autosys.common.v12.job.JobSQL;
import com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.JITLJobConverter;

public class DBJobConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBJobConverter.class);

    private static final String JITL_JOB_ORACLE_PLSQL_CLASSNAME = "com.sos.jitl.jobs.db.oracle.PLSQLJob";
    private static final String JITL_JOB_SQLEXECUTER_CLASSNAME = "com.sos.jitl.jobs.db.SQLExecutorJob";

    public static void clear() {

    }

    public static Job setExecutable(Job j, JobSQL jilJob, String platform) {
        if (isOraclePLSQLJob(jilJob)) {
            return toOraclePLSQLJob(j, jilJob);
        } else {
            LOGGER.warn("[setExecutable][Job=" + jilJob.getName() + "][NOT SUPPORTED YET][the conversion result must be adjusted manually]" + jilJob
                    .getConnectString());
            j = toSQLExecutorJob(j, jilJob);
        }
        return j;
    }

    private static boolean isOraclePLSQLJob(JobSQL jilJob) {
        if (jilJob.getConnectString().isEmpty()) {
            return false;
        }
        return jilJob.getConnectString().getValue().startsWith("jdbc:oracle");
    }

    // db_user, db_password can't be specified (are not specified in the jil file)
    private static Job toOraclePLSQLJob(Job j, JobSQL jilJob) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_ORACLE_PLSQL_CLASSNAME);

        JITLJobConverter.addArgument(j, "db_url", jilJob.getConnectString().getValue());
        JITLJobConverter.addArgument(j, "db_user", JITLJobConverter.DEFAULT_USER);
        JITLJobConverter.addArgument(j, "db_password", JITLJobConverter.DEFAULT_PASSWORD);

        if (!jilJob.getSqlCommand().isEmpty()) {
            String command = jilJob.getSqlCommand().getValue();
            JITLJobConverter.addArgument(j, "command", command);

            if (!jilJob.getDestinationFile().isEmpty()) {
                String c = command.toLowerCase();
                if (c.startsWith("delete") || c.startsWith("update") || c.startsWith("insert")) {
                    LOGGER.warn("[toOraclePLSQLJob][Job=" + jilJob.getName() + "][destination_file=" + jilJob.getDestinationFile().getValue()
                            + " is ignored because the sql_command does not produce a result set]sql_command=" + command);
                } else {
                    JITLJobConverter.addArgument(j, "resultset_as", "CSV");
                    JITLJobConverter.addArgument(j, "result_file", jilJob.getDestinationFile().getValue());
                }
            }
        }
        return j;
    }

    private static Job toSQLExecutorJob(Job j, JobSQL jilJob) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_SQLEXECUTER_CLASSNAME);

        JITLJobConverter.addArgument(j, "hibernate_configuration_file", jilJob.getConnectString().getValue());
        if (!jilJob.getSqlCommand().isEmpty()) {
            String command = jilJob.getSqlCommand().getValue();
            JITLJobConverter.addArgument(j, "command", command);

            if (!jilJob.getDestinationFile().isEmpty()) {
                String c = command.toLowerCase();
                if (c.startsWith("delete") || c.startsWith("update") || c.startsWith("insert")) {
                    LOGGER.warn("[toSQLExecutorJob][Job=" + jilJob.getName() + "][destination_file=" + jilJob.getDestinationFile().getValue()
                            + " is ignored because the sql_command does not produce a result set]sql_command=" + command);
                } else {
                    JITLJobConverter.addArgument(j, "resultset_as_variables", "CSV");
                    JITLJobConverter.addArgument(j, "result_file", jilJob.getDestinationFile().getValue());
                }
            }
        }
        return j;
    }
}
