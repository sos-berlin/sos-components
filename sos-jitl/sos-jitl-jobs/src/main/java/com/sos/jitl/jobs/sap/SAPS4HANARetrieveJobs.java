package com.sos.jitl.jobs.sap;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
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
        HttpClient httpClient = null;
        try {
            httpClient = new HttpClient(step.getDeclaredArguments(), step.getLogger());
            ResponseJobs result = httpClient.retrieveJobs();
            step.getLogger().info("result: \n" + Globals.objectMapperPrettyPrint.writeValueAsString(result));
            return step.success();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.closeHttpClient();
            }
        }
    }

}
