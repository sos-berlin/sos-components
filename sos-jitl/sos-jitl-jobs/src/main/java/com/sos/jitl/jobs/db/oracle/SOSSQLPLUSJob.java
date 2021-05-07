package com.sos.jitl.jobs.db.oracle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.sos.jitl.jobs.common.JobArgument.Type;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

import js7.data_for_java.order.JOutcome;

public class SOSSQLPLUSJob extends ABlockingInternalJob<SOSSQLPlusJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSQLPLUSJob.class);

    public SOSSQLPLUSJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<SOSSQLPlusJobArguments> step) throws Exception {

        try {
            if (SOSString.isEmpty(step.getArguments().getCommandScriptFile())) {
                throw new SOSJobRequiredArgumentMissingException("command is empty. please check the   \"command_script_file\" parameter.");
            }
            return step.success(process(step, step.getArguments()));
        } catch (Throwable e) {
            throw e;
        }
    }

    public Map<String, Object> process(JobStep<SOSSQLPlusJobArguments> step, SOSSQLPlusJobArguments args) throws Exception {

        args.checkRequired();
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            if (args.getCredentialStoreFile() != null) {
                SOSKeePassResolver r = new SOSKeePassResolver(args.getCredentialStoreFile(), args.getCredentialStoreKeyFile(), args
                        .getCredentialStorePassword());

                r.setEntryPath(args.getCredentialStoreEntryPath());

                args.setDbUrl(r.resolve(args.getDbUrl()));
                args.setDbUser(r.resolve(args.getDbUser()));
                args.setDbPassword(r.resolve(args.getDbPassword().getValue()));
                debug(logger, args.getCredentialStoreFile());
                debug(logger, args.getCredentialStoreKeyFile());
                debug(logger, args.getCredentialStoreEntryPath());
            }

            debug(logger, "dbUrl: " + args.getDbUrl());
            debug(logger, "dbUser: " + args.getDbUser());
            debug(logger, "dbPassword: " + args.getDbPassword().getDisplayValue());

            Map<String, Object> variables = new HashMap<>();
            if (step != null) {
                variables = Job.asNameValueMap(step.getAllCurrentArguments(Type.UNKNOWN));
            }

            SOSSQLPLUSCommandHandler sqlPlusCommandHandler = new SOSSQLPLUSCommandHandler(variables, logger);
            File tempFile = File.createTempFile("sos", ".sql");
            String tempFileName = tempFile.getAbsolutePath();
            sqlPlusCommandHandler.createSqlFile(args, tempFileName);

            SOSCommandResult sosCommandResult = SOSShell.executeCommand(args.getCommandLine(tempFileName));
            final String conNL = System.getProperty("line.separator");
            String stdOut = sosCommandResult.getStdOut().toString();
            String[] stdOutStringArray = stdOut.split(conNL);

            sqlPlusCommandHandler.getVariables(args, sosCommandResult, resultMap, stdOutStringArray);
            sqlPlusCommandHandler.handleMessages(args, sosCommandResult, resultMap, stdOutStringArray);
            try {
                Files.delete(Paths.get(tempFileName));
            } catch (IOException ioException) {
                LOGGER.warn("File " + tempFileName + " could not deleted");
            }
        } catch (Exception e) {
            throw e;
        }
        return resultMap;
    }

    private void log(JobLogger logger, String log) {
        LOGGER.info(log);
        if (logger != null) {
            logger.info(log);
        }
    }

    private void debug(JobLogger logger, String log) {
        LOGGER.debug(log);
        if (logger != null) {
            logger.debug(log);
        }
    }

    public static void main(String[] args) {
        SOSSQLPLUSJob sosSQLPlusJob = new SOSSQLPLUSJob(null);
        SOSSQLPlusJobArguments arguments = new SOSSQLPlusJobArguments();
        arguments.setShellCommand("sqlplus");
        arguments.setCommandScriptFile("c:/temp/1.sql");
        arguments.setDbPassword("scheduler");
        arguments.setDbUser("scheduler");
        arguments.setDbUrl("xe");

        arguments.setDbPassword("cs://sos/db/ur/@password");
        arguments.setDbUser("cs://sos/db/ur/@user");
        arguments.setCredentialStoreFile("D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/profiles/sos.kdbx");
        arguments.setCredentialStoreKeyFile("D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/profiles/sos.key");

        try {
            sosSQLPlusJob.process(null, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}