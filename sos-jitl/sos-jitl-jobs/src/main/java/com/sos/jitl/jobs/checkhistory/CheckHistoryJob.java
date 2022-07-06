package com.sos.jitl.jobs.checkhistory;

import java.util.HashMap;
import java.util.Map;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryJobReturn;
import com.sos.jitl.jobs.checkhistory.classes.Globals;
import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class CheckHistoryJob extends ABlockingInternalJob<CheckHistoryJobArguments> {

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
        resultMap.put("js7CheckHistoryResultControllerId", "");
        resultMap.put("js7CheckHistoryResultWorkflow", "");
        resultMap.put("js7CheckHistoryResultJob", "");
        resultMap.put("js7CheckHistoryResultStarted", "");
        resultMap.put("js7CheckHistoryResultCompleted", "");

        String query = args.getQuery();
        resultMap.put("js7CheckHistoryQuery", query);
        resultMap.put("js7CheckHistoryQueryControllerId", args.getControllerId());
        resultMap.put("js7CheckHistoryQueryWorkflow", args.getWorkflow());
        resultMap.put("js7CheckHistoryQueryJob", args.getJob());
        Globals.debug(logger, String.format("check history: %s will be executed.", query));

        HistoryInfo historyInfo = new HistoryInfo(logger, args);
        HistoryItem historyItem = historyInfo.queryHistory();

        String name = args.getWorkflow();
        if (name == null) {
            name = args.getJob();
        } else {
            if (args.getJob() != null) {
                name = name + "<" + args.getJob() + ">";
            }
        }

        if (historyItem != null) {
            boolean result = historyItem.getResult();
            if (result) {
                String s = args.getQuery() + "(" + name + ") ==> true";
                Globals.debug(logger, s);
                resultMap.put("js7CheckHistoryResultString", s);
                checkHistoryJobReturn.setExitCode(0);
                resultMap.put("js7CheckHistoryResult", true);
                resultMap.put("js7CheckHistoryResultControllerId", historyItem.getControllerId());
                resultMap.put("js7CheckHistoryResultStarted", historyItem.getStartTime());
                resultMap.put("js7CheckHistoryResultCompleted", historyItem.getEndTime());
                resultMap.put("js7CheckHistoryResultWorkflow", historyItem.getWorkflow());
                resultMap.put("js7CheckHistoryResultJob", historyItem.getJob());

            } else {
                String s = args.getQuery() + "(" + name + ") ==> false";
                Globals.debug(logger, s);
                resultMap.put("js7CheckHistoryResultString", s);
                checkHistoryJobReturn.setExitCode(1);
                resultMap.put("js7CheckHistoryResult", false);
            }
        }

        checkHistoryJobReturn.setResultMap(resultMap);

        return checkHistoryJobReturn;
    }

    public static void main(String[] args) {

        CheckHistoryJobArguments arguments = new CheckHistoryJobArguments();
        arguments.setQuery("isStarted(startedFrom=-1d,startedTo=-1d)");

        // arguments.setQuery("isCompletedSuccessful(startedFrom=-1d,startedTo=-1d)");
        // arguments.setQuery("isCompleted(startedFrom=-100d, count>5)");

        // arguments.setQuery("lastCompletedSuccessful");
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