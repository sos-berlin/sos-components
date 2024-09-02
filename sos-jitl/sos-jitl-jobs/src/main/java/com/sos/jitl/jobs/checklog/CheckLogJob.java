package com.sos.jitl.jobs.checklog;

import com.sos.jitl.jobs.checklog.classes.CheckLog;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepOutcome;

public class CheckLogJob extends Job<CheckLogJobArguments> {

    public CheckLogJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<CheckLogJobArguments> step) throws Exception {

        OrderProcessStepOutcome outcome = step.getOutcome();

        step.getLogger().debug(step.getOrderId());
        
        CheckLog checkLog = new CheckLog(step);
        checkLog.execute();

        outcome.putVariable("js7CheckLogMatchedGroups", checkLog.getCheckLogMatchedGroups());
        outcome.putVariable("js7CheckLogMatches", checkLog.getCheckLogMatches());
        outcome.putVariable("js7CheckLogMatchCount", checkLog.getCheckLogMatchCount());
        outcome.putVariable("js7CheckLogGroupsMatchesCount", checkLog.getCheckLogGroupsMatchesCount());
        outcome.putVariable("js7CheckLogGroupCount", checkLog.getCheckLogGroupCount());

        if (checkLog.isMatchFound()) {
            outcome.setReturnCode(0);
        } else {
            outcome.setReturnCode(1);
        }

    }
}
