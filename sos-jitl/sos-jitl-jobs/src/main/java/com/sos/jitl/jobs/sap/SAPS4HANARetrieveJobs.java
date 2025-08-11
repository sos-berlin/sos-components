package com.sos.jitl.jobs.sap;

import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.ResponseJobs;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class SAPS4HANARetrieveJobs extends Job<CommonJobArguments> {

    public SAPS4HANARetrieveJobs(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<CommonJobArguments> step) throws Exception {
        HttpClient httpClient = null;
        try {
            httpClient = new HttpClient(step.getDeclaredArguments(), step.getLogger());
            ResponseJobs result = httpClient.retrieveJobs();
            step.getLogger().info("result: \n" + Globals.objectMapperPrettyPrint.writeValueAsString(result));
        } catch (Throwable e) {
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

}
