package com.sos.jitl.jobs.checkhistory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryJobReturn;
import com.sos.jitl.jobs.checkhistory.classes.Globals;
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
        resultMap.put("js7CheckHistoryControllerId", "");
        resultMap.put("js7CheckHistoryWorkflow", "");
        resultMap.put("js7CheckHistoryJob", "");
        resultMap.put("js7CheckHistoryStarted", "");
        resultMap.put("js7CheckHistoryCompleted", "");
        resultMap.put("js7CheckHistoryStarted", "");
        resultMap.put("js7CheckHistoryCompleted", "");

        String query = args.getQuery();
        Globals.debug(logger, String.format("check history: %s will be executed.", query));

        HistoryInfo historyInfo = new HistoryInfo(logger, args);
        HistoryItem historyItem = historyInfo.queryHistory();

        boolean result = historyItem.getResult();
        if (result) {
            Globals.debug(logger, args.getQuery() + "(" + historyItem.getName() + ") ==> true");
            checkHistoryJobReturn.setExitCode(0);
            resultMap.put("js7CheckHistoryResult", true);
            resultMap.put("js7CheckHistoryControllerId", historyItem.getControllerId());
            resultMap.put("js7CheckHistoryWorkflow", historyItem.getWorkflow());
            resultMap.put("js7CheckHistoryJob", historyItem.getJob());
            resultMap.put("js7CheckHistoryStarted", historyItem.getStartTime());
            resultMap.put("js7CheckHistoryCompleted", historyItem.getEndTime());

        } else {
            Globals.debug(logger, args.getQuery() + "(" + historyItem.getName() + ") ==> false");
            checkHistoryJobReturn.setExitCode(1);
            resultMap.put("js7CheckHistoryResult", false);
        }

        checkHistoryJobReturn.setResultMap(resultMap);

        return checkHistoryJobReturn;
    }

    public static void main(String[] args) {
        CheckHistoryJobArguments arguments = new CheckHistoryJobArguments();
        arguments.setQuery("isStarted(startedFrom=-1d,startedTo=-1d)");
        
        arguments.setQuery("isCompletedSuccessful(startedFrom=-1d,startedTo=-1d)");
     //   arguments.setQuery("isCompleted(startedFrom=-100d, count>5)");
          
        arguments.setQuery("lastCompletedSuccessful");
        arguments.setAccount("root");
        arguments.setPassword("root");
        arguments.setJocUrl("http://localhost:4426");
        // arguments.setJob("job2");
        // arguments.setJob("jobCheckHistory2");
        arguments.setWorkflow("fork");

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