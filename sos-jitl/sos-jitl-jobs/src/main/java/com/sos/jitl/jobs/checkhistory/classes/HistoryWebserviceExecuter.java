package com.sos.jitl.jobs.checkhistory.classes;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryWebserviceExecuter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryWebserviceExecuter.class);
	private WebserviceCredentials webserviceCredentials;

	public HistoryWebserviceExecuter(WebserviceCredentials webserviceCredentials) {
		super();
		this.webserviceCredentials = webserviceCredentials;

	}

	public OrderHistoryItem getWorkflowHistoryEntry(OrdersFilter ordersFilter) throws Exception {
		if (webserviceCredentials.getAccessToken().isEmpty()) {
			throw new Exception("AccessToken is empty. Login not executed");
		}

		ObjectMapper objectMapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
		String body = objectMapper.writeValueAsString(ordersFilter);
		String answer = webserviceCredentials.getSosRestApiClient()
				.postRestService(new URI(webserviceCredentials.getJocUrl() + "/orders/history"), body);
		OrderHistory orderHistory = new OrderHistory();
		orderHistory = objectMapper.readValue(answer, OrderHistory.class);
		if (orderHistory.getHistory().size() == 0) {
			return null;
		}
		OrderHistoryItem h = orderHistory.getHistory().get(0);
		if (!ordersFilter.getWorkflowPath().equals(h.getWorkflow())) {
			return null;
		}
		if (h.getHistoryId() == null) {
			return null;
		} else {
			return h;
		}
	}

    public TaskHistoryItem getJobHistoryEntry(JobsFilter jobsFilter) throws Exception {
        if (webserviceCredentials.getAccessToken().isEmpty()) {
            throw new Exception("AccessToken is empty. Login not executed");
        }

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        String body = objectMapper.writeValueAsString(jobsFilter);
        String answer = webserviceCredentials.getSosRestApiClient()
                .postRestService(new URI(webserviceCredentials.getJocUrl() + "/tasks/history"), body);
        TaskHistory taskHistory = new TaskHistory();
        taskHistory = objectMapper.readValue(answer, TaskHistory.class);
        if (taskHistory.getHistory().size() == 0) {
            return null;
        }
        TaskHistoryItem h = taskHistory.getHistory().get(0);
        if (!jobsFilter.getJobName().equals(h.getJob())) {
            return null;
        }
        if (h.getTaskId() == null) {
            return null;
        } else {
            return h;
        }
    }

}
