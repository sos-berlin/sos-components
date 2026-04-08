package com.sos.jitl.jobs.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobException;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

public class HealthCheckJob extends Job<HealthCheckJobArguments> {

	public static final ObjectMapper objectMapper;
	private static final Scope rootScope;

	public HealthCheckJob(JobContext jobContext) {
		super(jobContext);
	}

	public void processOrder(OrderProcessStep<HealthCheckJobArguments> step) throws Exception {
		OrderProcessStepLogger logger = step.getLogger();

		String accessToken = null;
		ApiResponse response = null;
		try (ApiExecutor apiExecutor = new ApiExecutor(step)) {
			Map<String, String> headers = new HashMap<>();
			response = apiExecutor.login();
			if (response == null || response.getStatusCode() != 200) {
				String body = response != null ? response.getResponseBody() : "No response";
				throw new JobException("Login failed. " + body);
			}
			logger.debug("Login successful.");

			accessToken = response.getAccessToken();
			if (accessToken == null || accessToken.isEmpty()) {
				throw new JobException("Login succeeded but access token is missing.");
			}

			String bodyStr = "{\"controllerId\":\"" + step.getControllerId() + "\"}";
			logger.debug("Response Body: " + bodyStr);
			response = apiExecutor.post(accessToken, "/controller/components", bodyStr, headers);

			int statusCode = response.getStatusCode();

			if (statusCode < 200 || statusCode >= 300) {
				if (statusCode == 420) {
					throw new JobException("JS7 API Throttling (420): " + response.getResponseBody());
				}
				throw new JobException("Unable to fetch the data related to the JS7 Components. Status code:"
						+ statusCode + ". Response: " + response.getResponseBody());
			}

			logger.debug("REST call successful. Status Code: " + statusCode);

			String responseBody = response.getResponseBody();
			JsonNode responseJson = objectMapper.readTree(responseBody);
			logger.debug(
					"Response Body: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseJson));

			// Database Status
			logger.debug("Starting Database health check...");
			if (1 == responseJson.path("database").path("connectionState").path("severity").asInt(2)) {
				logger.error("unhealthy database connection state: "
						+ responseJson.path("database").path("connectionState").path("_text"));
				step.getOutcome().setReturnCode(1);
			} else if (1 < responseJson.path("database").path("connectionState").path("severity").asInt(2)) {
				logger.error("fatal database connection state: "
						+ responseJson.path("database").path("connectionState").path("_text"));
				step.getOutcome().setReturnCode(1);
			} else if (1 == responseJson.path("database").path("componentState").path("severity").asInt(2)) {
				logger.error("unhealthy database component state: "
						+ responseJson.path("database").path("componentState").path("_text"));
				step.getOutcome().setReturnCode(1);
			} else if (1 < responseJson.path("database").path("componentState").path("severity").asInt(2)) {
				logger.error("fatal database component state: "
						+ responseJson.path("database").path("componentState").path("_text"));
				step.getOutcome().setReturnCode(1);
			}
			logger.debug("Database health check completed successfully.");

			// JOC Status
			logger.debug("Starting JOC health check...");
			JsonNode jocs = responseJson.path("jocs");
			if (jocs.isArray()) {
				for (int i = 0; i < jocs.size(); i++) {
					JsonNode ctrl = jocs.get(i);
					String title = ctrl.path("title").asText("jocs-" + i);
					int componentSeverity = ctrl.path("componentState").path("severity").asInt(2);
					int connectionSeverity = ctrl.path("connectionState").path("severity").asInt(2);
					if (connectionSeverity > 0) {
						logger.error("Unhealthy " + title + " connection state: "
								+ ctrl.path("connectionState").path("_text").asText());
						step.getOutcome().setReturnCode(1);
					}
					if (componentSeverity > 0) {
						logger.error("Unhealthy " + title + " component state: "
								+ ctrl.path("componentState").path("_text").asText());
						step.getOutcome().setReturnCode(1);
					}
					if (jocs.size() > 1) {
						int clusterSeverity = ctrl.path("clusterNodeState").path("severity").asInt(2);
						if (clusterSeverity > 1) {
							logger.error("Unhealthy " + title + " cluster node state: "
									+ ctrl.path("clusterNodeState").path("_text").asText());
							step.getOutcome().setReturnCode(1);
						}
					}
				}
			}
			logger.debug("JOC health check completed successfully.");

			// Controller Health Check
			logger.debug("Starting Controller health check...");
			JsonNode controllers = responseJson.path("controllers");
			if (controllers.isArray()) {
				for (int i = 0; i < controllers.size(); i++) {
					JsonNode ctrl = controllers.get(i);
					String title = ctrl.path("title").asText("Controller-" + i);
					int componentSeverity = ctrl.path("componentState").path("severity").asInt(-1);
					int connectionSeverity = ctrl.path("connectionState").path("severity").asInt(-1);
					if (connectionSeverity > 0) {
						logger.error("Unhealthy " + title + " connection state: "
								+ ctrl.path("connectionState").path("_text").asText());
						step.getOutcome().setReturnCode(1);
					}
					if (componentSeverity > 0) {
						logger.error("Unhealthy  " + title + " component state: "
								+ ctrl.path("componentState").path("_text").asText());
						step.getOutcome().setReturnCode(1);
					}
					if (controllers.size() > 1) {
						int clusterSeverity = ctrl.path("clusterNodeState").path("severity").asInt(-1);

						if (clusterSeverity > 1) {
							logger.error("Unhealthy " + title + " cluster node state: "
									+ ctrl.path("clusterNodeState").path("_text").asText());
							step.getOutcome().setReturnCode(1);
						}
					}
				}
			}
			logger.debug("Controller health check completed successfully.");

			try {
				if (accessToken != null) {
					apiExecutor.logout(accessToken);
				}
			} catch (Exception e) {
				logger.error("Logout failed!");
				throw e;
			}

		} catch (SOSConnectionRefusedException | SOSBadRequestException e) {
			logger.debug("from SOSConnectionRefusedException | SOSBadRequestException e");
			if (response != null) {
				step.getOutcome().setReturnCode(response.getStatusCode());
				logger.error("Request failed: " + response.getStatusCode() + " Body: " + response.getResponseBody());
			}
			throw e;
		} catch (IOException e) {
			throw new JobException("I/O Exception occurred: " + e);
		}

	}

	static {
		rootScope = Scope.newEmptyScope();
		BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, rootScope);

		objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
	}

}
