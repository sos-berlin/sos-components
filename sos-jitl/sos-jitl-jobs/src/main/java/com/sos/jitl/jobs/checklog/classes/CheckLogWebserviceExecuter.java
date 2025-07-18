package com.sos.jitl.jobs.checklog.classes;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.RunningTaskLogFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.OrderHistoryItemChildren;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class CheckLogWebserviceExecuter {

    private final ISOSLogger logger;
    private final ApiExecutor apiExecutor;

    public CheckLogWebserviceExecuter(ISOSLogger logger, ApiExecutor apiExecutor) {
        this.logger = logger;
        this.apiExecutor = apiExecutor;
    }

    public Workflow getWorkflow(WorkflowFilter workflowFilter, String accessToken) throws Exception {

        String body = Globals.objectMapper.writeValueAsString(workflowFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/workflow", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }

        Workflow workflow = new Workflow();
        workflow = Globals.objectMapper.readValue(answer, Workflow.class);
        if (workflow == null || workflow.getWorkflow() == null || workflow.getWorkflow().getJobs() == null || workflow.getWorkflow().getJobs()
                .getAdditionalProperties() == null) {
            return null;
        }
        return workflow;
    }

    public OrderV getOrder(OrderFilter orderFilter, String accessToken) throws Exception {

        String body = Globals.objectMapper.writeValueAsString(orderFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/order", body);

        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                JsonReader jsonReaderConfigurationResponse = Json.createReader(new StringReader(apiResponse.getResponseBody()));
                JsonObject jsonConfigurationResponse = jsonReaderConfigurationResponse.readObject();
                JsonObject jsonConfigurationResponseError = jsonConfigurationResponse.getJsonObject("error");

                if (jsonConfigurationResponseError != null) {
                    String code = jsonConfigurationResponseError.getString("code", "");
                    String message = jsonConfigurationResponseError.getString("message", "");
                    logger.info("code: %s", code);
                    logger.info("message: %s", message);
                    if (code.equals("JOC-400") && message.startsWith("ControllerObjectNotExistException:")) {
                        throw new com.sos.commons.exception.SOSMissingDataException(apiResponse.getResponseBody());
                    }
                }
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }

        OrderV order = new OrderV();
        order = Globals.objectMapper.readValue(answer, OrderV.class);

        return order;

    }

    public OrderHistoryItemChildren getOrderHistory(OrderHistoryFilter orderHistoryFilter, String accessToken) throws Exception {

        String body = Globals.objectMapper.writeValueAsString(orderHistoryFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/order/history", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }

        OrderHistoryItemChildren orderHistoryItemChildren = new OrderHistoryItemChildren();
        orderHistoryItemChildren = Globals.objectMapper.readValue(answer, OrderHistoryItemChildren.class);

        return orderHistoryItemChildren;

    }

    public TaskHistory getTaskHistory(JobsFilter jobsFilter, String accessToken) throws Exception {

        String body = Globals.objectMapper.writeValueAsString(jobsFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/tasks/history", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }

        TaskHistory taskHistory = new TaskHistory();
        taskHistory = Globals.objectMapper.readValue(answer, TaskHistory.class);

        return taskHistory;

    }

    public String getTaskLog(RunningTaskLogFilter runningTaskLogFilter, String accessToken) throws Exception {

        String body = Globals.objectMapper.writeValueAsString(runningTaskLogFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/task/log", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }
        return answer;

    }

}
