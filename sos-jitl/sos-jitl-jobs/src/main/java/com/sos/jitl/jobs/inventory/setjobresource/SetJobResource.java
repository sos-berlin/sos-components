package com.sos.jitl.jobs.inventory.setjobresource;

import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class SetJobResource {

    private SetJobResourceJobArguments args;
    private OrderProcessStepLogger logger;

    public SetJobResource(OrderProcessStepLogger logger, SetJobResourceJobArguments args) {
        this.args = args;
        this.logger = logger;
    }

    public void execute() throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(logger);
        String accessToken = null;
        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();

            RequestFilter requestFilter = new RequestFilter();
            requestFilter.setPath(args.getJobResource());
            requestFilter.setObjectType(ConfigurationType.JOBRESOURCE);
            requestFilter.setControllerId(args.getControllerId());

            JobResourceWebserviceExecuter jobResourceWebserviceExecuter = new JobResourceWebserviceExecuter(logger, apiExecutor);
            jobResourceWebserviceExecuter.handleJobResource(requestFilter, args, accessToken);

        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }
}
