package com.sos.jitl.jobs.checkhistory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryJobReturn;
import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class CheckHistoryJob extends ABlockingInternalJob<CheckHistoryJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryJob.class);

    public CheckHistoryJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<CheckHistoryJobArguments> step) throws Exception {

        try {
            CheckHistoryJobReturn checkHistoryJobReturn = process(step, step.getArguments());
            return step.success(checkHistoryJobReturn.getExitCode(), checkHistoryJobReturn.getResultMap());

        } catch (Throwable e) {
            throw e;
        }
    }

    private CheckHistoryJobReturn process(JobStep<CheckHistoryJobArguments> step, CheckHistoryJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        CheckHistoryJobReturn checkHistoryJobReturn = new CheckHistoryJobReturn();
        checkHistoryJobReturn.setExitCode(0);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("js7CheckHistoryResult", false);
        resultMap.put("js7CheckHistoryWorkflow", "");
        resultMap.put("js7CheckHistoryJob", "");
        resultMap.put("js7CheckHistoryStarted", "");
        resultMap.put("js7CheckHistoryCompleted", "");

        String query = args.getQuery();

        log(logger, String.format("check history: %s will be executed.", query));

        HistoryInfo historyInfo = new HistoryInfo(args);
        HistoryItem historyItem = historyInfo.queryHistory();

        boolean result = historyItem.getResult();
        if (result) {
            log(logger, args.getQuery() + "(" + historyItem.getName() + ") ==> true");
            checkHistoryJobReturn.setExitCode(0);
            resultMap.put("js7CheckHistoryResult", true);
            resultMap.put("js7CheckHistoryControllerId", historyItem.getControllerId());
            resultMap.put("js7CheckHistoryWorkflow", historyItem.getWorkflow());
            resultMap.put("js7CheckHistoryJob", historyItem.getJob());
            resultMap.put("js7CheckHistoryStarted", historyItem.getStartTime());
            resultMap.put("js7CheckHistoryCompleted", historyItem.getEndTime());

        } else {
            log(logger, args.getQuery() + "(" + historyItem.getName() + ") ==> false");
            checkHistoryJobReturn.setExitCode(1);
            resultMap.put("js7CheckHistoryResult", false);
        }

        checkHistoryJobReturn.setResultMap(resultMap);

        return checkHistoryJobReturn;
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
        arguments.setQuery("isStarted(startedFrom=-1d,startedTo=-2d)");
        arguments.setQuery("iscompletedsuccessful");
        arguments.setAccount("root");
        arguments.setPassword("root");
        arguments.setJocUrl("http://localhost:4426");
        arguments.setWorkflow("Mail");

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