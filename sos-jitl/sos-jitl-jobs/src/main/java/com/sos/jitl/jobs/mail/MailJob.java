package com.sos.jitl.jobs.mail;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class MailJob extends Job<MailJobArguments> {

    public MailJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<MailJobArguments> step) throws Exception {
        MailHandler handler = new MailHandler(step.getDeclaredArguments(), step.getAllArgumentsAsNameValueMap(), step.getLogger());
        handler.sendMail(step.getIncludedArguments(SOSCredentialStoreArguments.class));
    }

}