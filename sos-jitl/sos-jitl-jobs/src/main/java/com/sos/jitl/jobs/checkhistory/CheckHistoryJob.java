package com.sos.jitl.jobs.checkhistory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class CheckHistoryJob extends ABlockingInternalJob<CheckHistoryJobArguments> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryJob.class);
	private Integer exitCode = 0;

	public CheckHistoryJob(JobContext jobContext) {
		super(jobContext);
	}

	@Override
	public JOutcome.Completed onOrderProcess(JobStep<CheckHistoryJobArguments> step) throws Exception {

		try {
			return step.success(exitCode, process(step, step.getArguments()));

		} catch (Throwable e) {
			throw e;
		}
	}

	private Map<String, Object> process(JobStep<CheckHistoryJobArguments> step, CheckHistoryJobArguments args)
			throws Exception {
		JobLogger logger = null;
		if (step != null) {
			logger = step.getLogger();
		}
		exitCode = 0;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("RETURN", "");
		String query = args.getQuery();

		log(logger, String.format("check history: %s will be executed.", query));

		HistoryInfo historyInfo = new HistoryInfo(args);
		boolean result = historyInfo.queryHistory();
		if (result) {
			log(logger,args.getQuery() + "(" + args.getWorkflowPath() + ") ==> true");
			exitCode = 0;
			resultMap.put("RETURN", "true");
		} else {
			log(logger,args.getQuery() + "(" + args.getWorkflowPath() + ") ==> false");
			exitCode = 1;
			resultMap.put("RETURN", "false");
		}

		/*
		 * Intervall: today: 0d,0d lastHour: dateFrom: "-1h" last12Hours: dateFrom:
		 * "-12h" last24Hours: dateFrom: "-24h" last7Days: dateFrom: "-7d"
		 * 
		 * dateFrom, dateTo
		 */

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
		CheckHistoryJobArguments arguments = new CheckHistoryJobArguments();
		arguments.setQuery("lastfinishedrunendedfailed");
		arguments.setAccount("root");
		arguments.setPassword("root");
		arguments.setJocUrl("http://localhost:4426");
		arguments.setWorkflowPath("/Examples.Windows/Jitl/Mail");

		SOSCredentialStoreArguments csArgs = new SOSCredentialStoreArguments();
		CheckHistoryJob checkHistoryJob = new CheckHistoryJob(null);

		try {
			checkHistoryJob.process(null, arguments);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}