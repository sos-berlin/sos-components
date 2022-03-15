package com.sos.jitl.jobs.db.oracle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments.SOSCredentialStoreResolver;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument.Type;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

import js7.data_for_java.order.JOutcome;

public class SQLPLUSJob extends ABlockingInternalJob<SQLPlusJobArguments> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLPLUSJob.class);

	public SQLPLUSJob(JobContext jobContext) {
		super(jobContext);
	}

	@Override
	public JOutcome.Completed onOrderProcess(JobStep<SQLPlusJobArguments> step) throws Exception {

		try {
			if (SOSString.isEmpty(step.getArguments().getCommandScriptFile()) & SOSString.isEmpty(step.getArguments().getCommand())) {
				throw new SOSJobRequiredArgumentMissingException(
						"command is empty. please check the   \"command_script_file\" or \"command\"parameter.");
			}
			return step.success(
					process(step, step.getArguments(), step.getAppArguments(SOSCredentialStoreArguments.class)));
		} catch (Throwable e) {
			throw e;
		}
	}

	public Map<String, Object> process(JobStep<SQLPlusJobArguments> step, SQLPlusJobArguments args,
			SOSCredentialStoreArguments csArgs) throws Exception {

		args.checkRequired();
		JobLogger logger = null;
		if (step != null) {
			logger = step.getLogger();
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (csArgs.getFile() != null) {
				SOSCredentialStoreResolver r = csArgs.newResolver();

				args.setDbUrl(r.resolve(args.getDbUrl()));
				args.setDbUser(r.resolve(args.getDbUser()));
				args.setDbPassword(r.resolve(args.getDbPassword().getValue()));
			}

			debug(logger, "dbUrl: " + args.getDbUrl());
			debug(logger, "dbUser: " + args.getDbUser());
			debug(logger, "dbPassword: " + args.getDbPassword().getDisplayValue());

			Map<String, Object> variables = new HashMap<>();
			if (step != null) {
				variables = Job.asNameValueMap(step.getAllCurrentArguments(Type.UNKNOWN));
			}

			SQLPLUSCommandHandler sqlPlusCommandHandler = new SQLPLUSCommandHandler(variables, logger);
			File tempFile = File.createTempFile("sos", ".sql");
			String tempFileName = tempFile.getAbsolutePath();
			sqlPlusCommandHandler.createSqlFile(args, tempFileName);

			SOSCommandResult sosCommandResult = SOSShell.executeCommand(args.getCommandLine(tempFileName));
			final String conNL = System.getProperty("line.separator");
			String stdOut = sosCommandResult.getStdOut();
			log(logger, String.format("[stdout]%s", stdOut));
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
		if (logger != null) {
			logger.info(log);
		} else {
			LOGGER.info(log);
		}
	}

	private void debug(JobLogger logger, String log) {
		if (logger != null) {
			logger.debug(log);
		} else {
			LOGGER.debug(log);
		}
	}

	public static void main(String[] args) {
		SQLPLUSJob sosSQLPlusJob = new SQLPLUSJob(null);
		SQLPlusJobArguments arguments = new SQLPlusJobArguments();
		arguments.setShellCommand("sqlplus");
		arguments.setCommandScriptFile("c:/temp/1.sql");
	    arguments.setCommand("select 1 from dual;");
		// arguments.setDbPassword("scheduler");
		// arguments.setDbUser("scheduler");
		arguments.setDbUrl("xe");

		// arguments.setDbPassword("cs://sos/db/ur/@password");
		// arguments.setDbUser("cs://sos/db/ur/@user");

		SOSCredentialStoreArguments csArgs = new SOSCredentialStoreArguments();
		csArgs.setFile("D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/profiles/sos.kdbx");
		csArgs.setKeyFile("D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/profiles/sos.key");

		try {
			sosSQLPlusJob.process(null, arguments, csArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}