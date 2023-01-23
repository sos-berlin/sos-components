package com.sos.jitl.jobs.inventory.setjobresource;

import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.read.RequestFilter;

public class SetJobResource {
	private SetJobResourceJobArguments args;
	private JobLogger logger;

	public SetJobResource(JobLogger logger, SetJobResourceJobArguments args) {
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

			JobResourceWebserviceExecuter jobResourceWebserviceExecuter = new JobResourceWebserviceExecuter(logger,
					apiExecutor);
			jobResourceWebserviceExecuter.handleJobResource(requestFilter, args, accessToken);

		} catch (Exception e) {
			Globals.error(logger, "", e);
			throw e;
		} finally {
			if (accessToken != null) {
				apiExecutor.logout(accessToken);
			}
			apiExecutor.close();
		}
	}
}
