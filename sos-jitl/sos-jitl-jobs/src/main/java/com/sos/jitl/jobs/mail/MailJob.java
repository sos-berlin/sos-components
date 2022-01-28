package com.sos.jitl.jobs.mail;

import java.util.HashMap;
import java.util.Map;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class MailJob extends ABlockingInternalJob<MailJobArguments> {

    public MailJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MailJobArguments> step) throws Exception {
        try {
            return step.success(process(step, step.getArguments()));
        } catch (Throwable e) {
            throw e;
        }

    }

    public Map<String, Object> process(JobStep<MailJobArguments> step, MailJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Map<String, Object> variables = null;
            SOSCredentialStoreArguments csArgs = null;
            if (step != null) {
                csArgs = step.getAppArguments(SOSCredentialStoreArguments.class);
                variables = Job.asNameValueMap(step.getAllCurrentArguments());
            } else {
                variables = new HashMap<String, Object>();
            }
            MailHandler sosMailHandler = new MailHandler(args, variables, logger);
            sosMailHandler.sendMail(csArgs);

        } catch (Exception e) {
            throw e;
        }
        return resultMap;
    }

}