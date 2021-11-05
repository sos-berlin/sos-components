package com.sos.jitl.jobs.sap;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.ResponseJobs;

import js7.data_for_java.order.JOutcome.Completed;

public class SAPS4HANARetrieveJobs extends ABlockingInternalJob<CommonJobArguments> {

    public SAPS4HANARetrieveJobs(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public Completed onOrderProcess(JobStep<CommonJobArguments> step) throws Exception {
        JobLogger logger = step.getLogger();
        HttpClient httpClient = new HttpClient(step.getArguments(), logger);
        ResponseJobs result = httpClient.retrieveJobs();
        logger.info("result: \n" + Globals.objectMapperPrettyPrint.writeValueAsString(result));
        httpClient.closeHttpClient();
        return step.success(0);
    }

}
