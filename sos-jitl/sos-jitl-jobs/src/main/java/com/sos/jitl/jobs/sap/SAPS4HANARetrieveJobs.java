package com.sos.jitl.jobs.sap;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.OrderProcessStep;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.ResponseJobs;

public class SAPS4HANARetrieveJobs extends ABlockingInternalJob<CommonJobArguments> {

    public SAPS4HANARetrieveJobs(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<CommonJobArguments> step) throws Exception {
        HttpClient httpClient = null;
        try {
            httpClient = new HttpClient(step.getDeclaredArguments(), step.getLogger());
            ResponseJobs result = httpClient.retrieveJobs();
            step.getLogger().info("result: \n" + Globals.objectMapperPrettyPrint.writeValueAsString(result));
        } catch (Throwable e) {
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.closeHttpClient();
            }
        }
    }

}
