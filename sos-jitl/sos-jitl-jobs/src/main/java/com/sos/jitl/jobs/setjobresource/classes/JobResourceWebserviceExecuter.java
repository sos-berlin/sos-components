package com.sos.jitl.jobs.setjobresource.classes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.jitl.jobs.setjobresource.SetJobResourceJobArguments;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;

public class JobResourceWebserviceExecuter {

	private ApiExecutor apiExecutor;
	private JobLogger logger;

	public JobResourceWebserviceExecuter(JobLogger logger, ApiExecutor apiExecutor) {
		super();
		this.apiExecutor = apiExecutor;
		this.logger = logger;
	}

	private ConfigurationObject getInventoryItem(RequestFilter requestFilter, String accessToken)
			throws JsonProcessingException, SOSConnectionRefusedException, SOSBadRequestException {
		Globals.debug(logger, ".. getInventoryItem: path: " + requestFilter.getPath() + " , object type: "
				+ requestFilter.getObjectType());

		String body = Globals.objectMapper.writeValueAsString(requestFilter);
		ApiResponse apiResponse = apiExecutor.post(accessToken, "/inventory/read/configuration", body);
		String answer = null;
		if (apiResponse.getStatusCode() == 200) {
			answer = apiResponse.getResponseBody();
		} else {
			// error handling here - apiResponse.getException();
		}
		Globals.debug(logger, ".... request body: " + body);
		Globals.debug(logger, "answer=" + answer);
		ConfigurationObject configurationObjectReturn = new ConfigurationObject();
		if (answer != null) {
			configurationObjectReturn = Globals.objectMapper.readValue(answer, ConfigurationObject.class);
		} else {
			configurationObjectReturn.setPath(requestFilter.getPath());
			configurationObjectReturn.setObjectType(requestFilter.getObjectType());
			configurationObjectReturn.setConfiguration(new JobResource());
		}
		return configurationObjectReturn;
	}

	private ConfigurationObject setInventoryItem(ConfigurationObject configurationObject, String accessToken)
			throws JsonMappingException, JsonProcessingException, SOSConnectionRefusedException,
			SOSBadRequestException {

		Globals.debug(logger, ".. setInventoryItem: path: " + configurationObject.getPath() + " , object type: "
				+ configurationObject.getObjectType());

		String body = Globals.objectMapper.writeValueAsString(configurationObject);
		ApiResponse apiResponse = apiExecutor.post(accessToken, "/inventory/store", body);
		String answer = null;
		if (apiResponse.getStatusCode() == 200) {
			answer = apiResponse.getResponseBody();
		} else {
			// error handling here - apiResponse.getException();
		}
		Globals.debug(logger, ".... request body: " + body);
		Globals.debug(logger, "answer=" + answer);
		ConfigurationObject configurationObjectReturn = new ConfigurationObject();
		if (answer != null) {
			configurationObjectReturn = Globals.objectMapper.readValue(answer, ConfigurationObject.class);
		}
		return configurationObjectReturn;
	}

	private void publishDeployableItem(ConfigurationObject configurationObject, SetJobResourceJobArguments args,
			String accessToken) throws JsonProcessingException, SOSConnectionRefusedException, SOSBadRequestException {

		Globals.debug(logger, ".. publishDeployableItem: path: " + configurationObject.getPath() + " , object type: "
				+ configurationObject.getObjectType());
		DeployFilter deployFilter = new DeployFilter();
		deployFilter.setControllerIds(new ArrayList<String>());
		deployFilter.getControllerIds().add(args.getControllerId());

		DeployablesValidFilter deployablesValidFilter = new DeployablesValidFilter();
		List<Config> draftConfigurations = new ArrayList<Config>();
		com.sos.joc.model.publish.Config config = new Config();
		Configuration configuration = new Configuration();
		configuration.setObjectType(configurationObject.getObjectType());
		configuration.setPath(configurationObject.getPath());
		configuration.setRecursive(false);
		config.setConfiguration(configuration);
		draftConfigurations.add(config);

		config.setConfiguration(configuration);
		deployablesValidFilter.setDraftConfigurations(draftConfigurations);
		deployFilter.setStore(deployablesValidFilter);

		String body = Globals.objectMapper.writeValueAsString(deployFilter);
		ApiResponse apiResponse = apiExecutor.post(accessToken, "/inventory/deployment/deploy", body);
		String answer = null;
		if (apiResponse.getStatusCode() == 200) {
			answer = apiResponse.getResponseBody();
		} else {
			// error handling here - apiResponse.getException();
		}
		Globals.debug(logger, ".... request body: " + body);
		Globals.debug(logger, "answer=" + answer);

	}

	public void handleJobResource(RequestFilter requestFilter, SetJobResourceJobArguments args, String accessToken)
			throws JsonMappingException, JsonProcessingException, SOSConnectionRefusedException,
			SOSBadRequestException {
		ConfigurationObject configurationObject = this.getInventoryItem(requestFilter, accessToken);
		JobResource jobResource = (JobResource) configurationObject.getConfiguration();
		if (jobResource.getArguments() == null) {
			jobResource.setArguments(new Environment());
		}
		if (jobResource.getEnv() == null) {
			jobResource.setEnv(new Environment());
		}
		jobResource.getArguments().getAdditionalProperties().put(args.getKey(), "\"" + args.getValue() + "\"");
		if (args.getEnvironmentVariable() == null || args.getEnvironmentVariable().isEmpty()) {
			jobResource.getEnv().getAdditionalProperties().put(args.getKey(), "$" + args.getKey());
		} else {
			jobResource.getEnv().getAdditionalProperties().put(args.getKey(), "$" + args.getEnvironmentVariable());
		}
		configurationObject.setConfiguration(jobResource);
		configurationObject = this.setInventoryItem(configurationObject, accessToken);
		publishDeployableItem(configurationObject, args, accessToken);
	}

}
