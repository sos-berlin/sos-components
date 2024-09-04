package com.sos.jitl.jobs.encrypt;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepOutcome;

public class EncryptJob extends Job<EncryptJobArguments> {

    public EncryptJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<EncryptJobArguments> step) throws Exception {

        OrderProcessStepOutcome outcome = step.getOutcome();

        step.getLogger().debug(step.getOrderId());

        if ((step.getDeclaredArgument("infile") == null || step.getDeclaredArgument("infile").isEmpty()) && (step.getDeclaredArgument("in") == null
                || step.getDeclaredArgument("in").isEmpty())) {
            throw new SOSMissingDataException("At least one of the parameters 'in' or 'infile' is required!");
        }

        if (step.getDeclaredArgument("infile") != null && !step.getDeclaredArgument("infile").isEmpty() && step.getDeclaredArgument("in") != null
                && !step.getDeclaredArgument("in").isEmpty()) {
            throw new SOSMissingDataException("Only one of the parameters 'in' or 'infile' should be configured!");
        }

        if (step.getDeclaredArgument("infile") != null && !step.getDeclaredArgument("infile").isEmpty() && (step.getDeclaredArgument(
                "outfile") == null || step.getDeclaredArgument("outfile").isEmpty())) {
            throw new SOSMissingDataException("'outfile must be specified when 'infile' is specified");
        }

        if (step.getDeclaredArgument("outfile") != null && !step.getDeclaredArgument("outfile").isEmpty() && (step.getDeclaredArgument(
                "infile") == null || step.getDeclaredArgument("infile").isEmpty())) {
            throw new SOSMissingDataException("'infile must be specified when 'outfile' is specified");
        }

        EncryptExecuter encryptExecuter = new EncryptExecuter(step);
        String encryptedValue = encryptExecuter.execute();

        outcome.putVariable("js7EncryptedValue", encryptedValue);

    }
}
