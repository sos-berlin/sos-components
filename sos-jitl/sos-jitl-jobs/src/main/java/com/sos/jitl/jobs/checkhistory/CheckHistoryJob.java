package com.sos.jitl.jobs.checkhistory;

import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.common.JobStepOutcome;

import js7.data_for_java.order.JOutcome;

public class CheckHistoryJob extends ABlockingInternalJob<CheckHistoryJobArguments> {

    public CheckHistoryJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<CheckHistoryJobArguments> step) throws Exception {
        return step.success(process(step, step.getDeclaredArguments()));
    }

    private JobStepOutcome process(JobStep<CheckHistoryJobArguments> step, CheckHistoryJobArguments args) throws Exception {
        JobStepOutcome outcome = step.newJobStepOutcome();

        outcome.putVariable("js7CheckHistoryResult", false);
        outcome.putVariable("js7CheckHistoryResultControllerId", "");
        outcome.putVariable("js7CheckHistoryResultWorkflow", "");
        outcome.putVariable("js7CheckHistoryResultJob", "");
        outcome.putVariable("js7CheckHistoryResultStarted", "");
        outcome.putVariable("js7CheckHistoryResultCompleted", "");

        String query = args.getQuery();
        outcome.putVariable("js7CheckHistoryQuery", query);
        outcome.putVariable("js7CheckHistoryQueryControllerId", args.getControllerId());
        outcome.putVariable("js7CheckHistoryQueryWorkflow", args.getWorkflow());
        outcome.putVariable("js7CheckHistoryQueryJob", args.getJob());
        step.getLogger().debug(String.format("check history: %s will be executed.", query));

        HistoryInfo historyInfo = new HistoryInfo(step.getLogger(), args);
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
                step.getLogger().debug(s);
                outcome.putVariable("js7CheckHistoryResultString", s);
                outcome.putVariable("js7CheckHistoryResult", true);
                outcome.putVariable("js7CheckHistoryResultControllerId", historyItem.getControllerId());
                outcome.putVariable("js7CheckHistoryResultStarted", historyItem.getStartTime());
                outcome.putVariable("js7CheckHistoryResultCompleted", historyItem.getEndTime());
                outcome.putVariable("js7CheckHistoryResultWorkflow", historyItem.getWorkflow());
                outcome.putVariable("js7CheckHistoryResultJob", historyItem.getJob());

                // outcome.setReturnCode(0); //default
            } else {
                String s = args.getQuery() + "(" + name + ") ==> false";
                step.getLogger().debug(s);

                outcome.putVariable("js7CheckHistoryResultString", s);
                outcome.putVariable("js7CheckHistoryResult", false);

                outcome.setReturnCode(1);
            }
        }
        return outcome;
    }
}