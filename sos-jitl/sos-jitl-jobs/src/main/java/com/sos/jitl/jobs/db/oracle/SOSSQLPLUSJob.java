package com.sos.jitl.jobs.db.oracle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.util.SOSCommandResult;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobStep;

import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;

public class SOSSQLPLUSJob extends ABlockingInternalJob<SOSSQLPlusJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSQLPLUSJob.class);

    public SOSSQLPLUSJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep step, SOSSQLPlusJobArguments args) throws Exception {

        try {
            if (SOSString.isEmpty(args.getCommandScriptFile())) {
                throw new Exception("command is empty. please check the   \"command_script_file\" parameter.");
            }
            return Job.success(process(step, args));
        } catch (Throwable e) {
            throw e;
        }
    }

    public Map<String, Object> process(JobStep step, SOSSQLPlusJobArguments args) throws Exception {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {

            if (args.getCredentialStoreFile() != null) {
                SOSKeePassResolver r = new SOSKeePassResolver(args.getCredentialStoreFile(), args.getCredentialStoreKeyFile(), args
                        .getCredentialStorePassword());

                r.setEntryPath(args.getCredentialStoreEntryPath());

                args.setDbUrl(r.resolve(args.getDbUrl()));
                args.setDbUser(r.resolve(args.getDbUser()));
                args.setDbPassword(r.resolve(args.getDbPassword()));
                LOGGER.debug(args.getCredentialStoreFile());
                LOGGER.debug(args.getCredentialStoreKeyFile());
                LOGGER.debug(args.getCredentialStoreEntryPath());
            }

            LOGGER.debug("dbUrl: " + args.getDbUrl());
            LOGGER.debug("dbUser: " + args.getDbUser());
            LOGGER.debug("dbPassword: " + "********");

            Map<String, Value> variables = new HashMap<String, Value>();
            if (step != null) {
                variables.putAll(getJobContext().jobArguments());
                variables.putAll(step.getInternalStep().arguments());
                variables.putAll(step.getInternalStep().order().arguments());
            }

            SOSSQLPLUSCommandHandler sqlPlusCommandHandler = new SOSSQLPLUSCommandHandler(variables);
            File tempFile = File.createTempFile("sos", ".sql");
            String tempFileName = tempFile.getAbsolutePath();
            sqlPlusCommandHandler.createSqlFile(args, tempFileName);

            LOGGER.debug(args.getCommandLineForLog(tempFileName));
            if (step != null) {

                step.getLogger().info("dbUrl: " + args.getDbUrl());
                step.getLogger().info("dbUser: " + args.getDbUser());
                step.getLogger().info("dbPassword: " + "********");
                step.getLogger().info(args.getCommandLineForLog(tempFileName));
            }

            SOSCommandResult sosCommandResult = SOSShell.executeCommand(args.getCommandLine(tempFileName));
            final String conNL = System.getProperty("line.separator");
            String stdOut = sosCommandResult.getStdOut().toString();
            String[] stdOutStringArray = stdOut.split(conNL);

            sqlPlusCommandHandler.getVariables(args, sosCommandResult, resultMap, stdOutStringArray);
            sqlPlusCommandHandler.handleMessages(args, sosCommandResult, resultMap, stdOutStringArray);

        } catch (Exception e) {
            throw e;
        }
        return resultMap;
    }

    public static void main(String[] args) {
        SOSSQLPLUSJob sosSQLPlusJob = new SOSSQLPLUSJob(null);
        SOSSQLPlusJobArguments arguments = new SOSSQLPlusJobArguments();
        arguments.setShellCommand("sqlplus");
        arguments.setCommandScriptFile("c:/temp/1.sql");
        arguments.setDbPassword("scheduler");
        arguments.setDbUser("scheduler");
        arguments.setDbUrl("xe");
        try {
            sosSQLPlusJob.process(null, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}