package com.sos.jitl.jobs.setjobresource;

import java.util.HashMap;
import java.util.Map;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JitlJobReturn;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class SetJobResourceJob extends ABlockingInternalJob<SetJobResourceJobArguments> {

    public SetJobResourceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<SetJobResourceJobArguments> step) throws Exception {

        try {
            JitlJobReturn jitlJobReturn = process(step, step.getArguments());
            return step.success(jitlJobReturn.getExitCode(), jitlJobReturn.getResultMap());

        } catch (Throwable e) {
            throw e;
        }
    }

    private JitlJobReturn process(JobStep<SetJobResourceJobArguments> step, SetJobResourceJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        JitlJobReturn jobReturn = new JitlJobReturn();
        jobReturn.setExitCode(0);
       
//        SetJobResource setJobResource = new SetJobResource(logger, args);
//        setJobResource.execute();
 
        return jobReturn;
    }

    public static void main(String[] args) {

    	SetJobResourceJobArguments arguments = new SetJobResourceJobArguments();
        arguments.setControllerId("controller"); 
        arguments.setEnvironmentVariable("env");
        arguments.setJobResource("/tttt");
        arguments.setKey("x");
        arguments.setValue("y");
          
 
        SetJobResourceJob setJobResourceJob = new SetJobResourceJob(null);

        try {
            setJobResourceJob.process(null, arguments);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}