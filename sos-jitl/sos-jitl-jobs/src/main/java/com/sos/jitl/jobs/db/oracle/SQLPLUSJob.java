package com.sos.jitl.jobs.db.oracle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.credentialstore.CredentialStoreArguments.CredentialStoreResolver;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepOutcome;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

public class SQLPLUSJob extends Job<SQLPlusJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPLUSJob.class);

    public SQLPLUSJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<SQLPlusJobArguments> step) throws Exception {
        if (SOSString.isEmpty(step.getDeclaredArguments().getCommandScriptFile()) & SOSString.isEmpty(step.getDeclaredArguments().getCommand())) {
            throw new JobRequiredArgumentMissingException("command is empty. please check the   \"command_script_file\" or \"command\"parameter.");
        }
        process(step, step.getDeclaredArguments(), step.getIncludedArguments(CredentialStoreArguments.class));
    }

    private void process(OrderProcessStep<SQLPlusJobArguments> step, SQLPlusJobArguments args, CredentialStoreArguments csArgs) throws Exception {
        args.checkRequired();

        if (csArgs.getFile().getValue() != null) {
            CredentialStoreResolver r = csArgs.newResolver();
            args.setDbUrl(r.resolve(args.getDbUrl()));
            args.setDbUser(r.resolve(args.getDbUser()));
            args.setDbPassword(r.resolve(args.getDbPassword().getValue()));
        }

        step.getLogger().debug("dbUrl: " + args.getDbUrl());
        step.getLogger().debug("dbUser: " + args.getDbUser());
        step.getLogger().debug("dbPassword: " + args.getDbPassword().getDisplayValue());

        Map<String, Object> variables = step.getUndeclaredArgumentsAsNameValueMap();

        SQLPLUSCommandHandler sqlPlusCommandHandler = new SQLPLUSCommandHandler(variables, step.getLogger());
        File tempFile = File.createTempFile("sos", ".sql");
        String tempFileName = tempFile.getAbsolutePath();
        sqlPlusCommandHandler.createSqlFile(args, tempFileName);

        SOSCommandResult sosCommandResult = null;
        if (args.getTimeout() == 0) {
            sosCommandResult = SOSShell.executeCommand(args.getCommandLine(tempFileName), getJobEnvironment().getSystemEncoding());
        } else {
            SOSTimeout sosTimeout = new SOSTimeout(args.getTimeout(), TimeUnit.SECONDS);
            sosCommandResult = SOSShell.executeCommand(args.getCommandLine(tempFileName), getJobEnvironment().getSystemEncoding(), sosTimeout);
        }

        final String conNL = System.getProperty("line.separator");
        String stdOut = sosCommandResult.getStdOut();
        step.getLogger().debug(String.format("[command encoding]%s", sosCommandResult.getEncoding()));
        step.getLogger().info(String.format("[stdout]%s", stdOut));
        String[] stdOutStringArray = stdOut.split(conNL);

        OrderProcessStepOutcome outcome = step.getOutcome();
        sqlPlusCommandHandler.getVariables(args, sosCommandResult, outcome, stdOutStringArray);
        sqlPlusCommandHandler.handleMessages(args, sosCommandResult, outcome, stdOutStringArray);
        try {
            Files.delete(Paths.get(tempFileName));
        } catch (IOException ioException) {
            LOGGER.warn("File " + tempFileName + " could not deleted");
        }
    }

}