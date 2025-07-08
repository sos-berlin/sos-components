package com.sos.jitl.jobs.checkhistory.classes;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class HistoryWebserviceExecuter {

    private final ISOSLogger logger;
    private final ApiExecutor apiExecutor;

    public HistoryWebserviceExecuter(ISOSLogger logger, ApiExecutor apiExecutor) {
        this.logger = logger;
        this.apiExecutor = apiExecutor;
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
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug(body);
            logger.debug("answer=%s", answer);
        }
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
