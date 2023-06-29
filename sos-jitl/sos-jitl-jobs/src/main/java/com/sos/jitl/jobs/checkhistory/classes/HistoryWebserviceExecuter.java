package com.sos.jitl.jobs.checkhistory.classes;

import com.sos.jitl.jobs.common.JobHelper;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryWebserviceExecuter {

    private ApiExecutor apiExecutor;
    private JobLogger logger;

    public HistoryWebserviceExecuter(JobLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    public OrderHistory getWorkflowHistoryEntry(OrdersFilter ordersFilter, String accessToken) throws Exception {

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(ordersFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/orders/history/", body);
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
        logger.debug(body);
        logger.debug("answer=" + answer);
        OrderHistory orderHistory = new OrderHistory();
        orderHistory = JobHelper.OBJECT_MAPPER.readValue(answer, OrderHistory.class);
        if (orderHistory.getHistory().size() == 0) {
            return null;
        }
        OrderHistoryItem h = orderHistory.getHistory().get(0);

        if (h.getHistoryId() == null) {
            return null;
        } else {
            return orderHistory;
        }
    }

    public TaskHistory getJobHistoryEntry(JobsFilter jobsFilter, String accessToken) throws Exception {

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(jobsFilter);
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
        logger.debug(body);
        logger.debug("answer=" + answer);
        TaskHistory taskHistory = new TaskHistory();
        taskHistory = JobHelper.OBJECT_MAPPER.readValue(answer, TaskHistory.class);
        if (taskHistory.getHistory().size() == 0) {
            return null;
        }
        TaskHistoryItem h = taskHistory.getHistory().get(0);

        if (h.getTaskId() == null) {
            return null;
        } else {
            return taskHistory;
        }
    }

}
