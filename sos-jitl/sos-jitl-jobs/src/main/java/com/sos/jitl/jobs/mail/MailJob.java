package com.sos.jitl.jobs.mail;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;

public class MailJob extends ABlockingInternalJob<MailJobArguments> {

    public MailJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<MailJobArguments> step) throws Exception {
        MailHandler handler = new MailHandler(step.getDeclaredArguments(), step.getAllArgumentsAsNameValueMap(), step.getLogger());
        handler.sendMail(step.getIncludedArguments(SOSCredentialStoreArguments.class));
    }

}