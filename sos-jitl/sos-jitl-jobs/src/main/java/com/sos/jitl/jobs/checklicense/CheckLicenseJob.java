package com.sos.jitl.jobs.checklicense;

import com.sos.jitl.jobs.checklicense.classes.CheckLicense;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepOutcome;

public class CheckLicenseJob extends Job<CheckLicenseJobArguments> {

    public CheckLicenseJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<CheckLicenseJobArguments> step) throws Exception {
        OrderProcessStepOutcome outcome = step.getOutcome();
        CheckLicense checklicense = new CheckLicense(step);
        try {
            checklicense.execute();
            
        } finally {
            outcome.putVariable("subject", checklicense.getSubject());
            outcome.putVariable("body", checklicense.getBody());
            outcome.setReturnCode(checklicense.getExit());
        }
    }
}