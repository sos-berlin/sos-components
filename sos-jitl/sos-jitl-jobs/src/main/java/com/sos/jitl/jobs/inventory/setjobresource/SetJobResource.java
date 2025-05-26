package com.sos.jitl.jobs.inventory.setjobresource;

import java.util.Map;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class SetJobResource {

    private final SetJobResourceJobArguments args;
    private final Map<String, DetailValue> jobResources;
    private final OrderProcessStep<SetJobResourceJobArguments> step;

    public SetJobResource(OrderProcessStep<SetJobResourceJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.jobResources = step.getJobResourcesArgumentsAsNameDetailValueMap();
        this.step = step;
    }

    public void execute() throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(step);
        apiExecutor.setJobResources(jobResources);

        String accessToken = null;
        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();

            RequestFilter requestFilter = new RequestFilter();
            requestFilter.setPath(args.getJobResource());
            requestFilter.setObjectType(ConfigurationType.JOBRESOURCE);
            requestFilter.setControllerId(args.getControllerId());

            JobResourceWebserviceExecuter jobResourceWebserviceExecuter = new JobResourceWebserviceExecuter(apiExecutor);
            jobResourceWebserviceExecuter.handleJobResource(requestFilter, args, accessToken);

        } catch (Exception e) {
            step.getLogger().error(e);
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }
}
