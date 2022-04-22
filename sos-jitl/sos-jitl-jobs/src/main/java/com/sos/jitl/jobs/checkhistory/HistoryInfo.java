package com.sos.jitl.jobs.checkhistory;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.checkhistory.classes.AccessTokenProvider;
import com.sos.jitl.jobs.checkhistory.classes.HistoryWebserviceExecuter;
import com.sos.jitl.jobs.checkhistory.classes.JOCCredentialStoreParameters;
import com.sos.jitl.jobs.checkhistory.classes.WebserviceCredentials;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryInfo {

 	private CheckHistoryJobArguments args;
 
	public HistoryInfo(CheckHistoryJobArguments args) {
		this.args = args;
	}
  
	private boolean executeApiCall(String query) throws Exception {

		JOCCredentialStoreParameters jobSchedulerCredentialStoreJOCParameters = new JOCCredentialStoreParameters();
		jobSchedulerCredentialStoreJOCParameters.setUser(args.getAccount());
		jobSchedulerCredentialStoreJOCParameters.setPassword(args.getPassword());
		jobSchedulerCredentialStoreJOCParameters.setJocUrl(args.getJocUrl());
		AccessTokenProvider accessTokenProvider = new AccessTokenProvider(jobSchedulerCredentialStoreJOCParameters);
		WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken();

		HistoryWebserviceExecuter historyWebserviceExecuter = new HistoryWebserviceExecuter(webserviceCredentials);
		OrdersFilter ordersFilter = new OrdersFilter();

		ordersFilter.setControllerId(args.getController());
		ordersFilter.setWorkflowPath(args.getWorkflowPath());
		 
		List<OrderStateText> states = new ArrayList<OrderStateText>();
		List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
	
		switch (query.toLowerCase()) {
		case "isstartedtoday":
			ordersFilter.setDateFrom("0d");
			ordersFilter.setDateTo("0d");
			break;
		case "isstartedtodayfinished":
			ordersFilter.setDateFrom("0d");
			ordersFilter.setDateTo("0d");
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		case "isstartedtodayfinishedsuccessful":
			ordersFilter.setDateFrom("0d");
			ordersFilter.setDateTo("0d");
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.SUCCESSFUL);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "isstartedtodayfinishedfailed":
			ordersFilter.setDateFrom("0d");
			ordersFilter.setDateTo("0d");
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.FAILED);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "lastfinishedrunendedsuccessful":
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		case "lastfinishedrunendedfailed":
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		default:
			throw new SOSException("unknown query: " + query);
		}

		ordersFilter.setLimit(1);
		OrderHistoryItem orderHistoryItem = historyWebserviceExecuter.getJobHistoryEntry(ordersFilter);
		
		switch (query.toLowerCase()) {
		case "lastfinishedrunendedsuccessful":
			return (orderHistoryItem != null && orderHistoryItem.getState().get_text().value().equals(HistoryStateText.SUCCESSFUL.value()));
		case "lastfinishedrunendedfailed":
			return (orderHistoryItem != null && orderHistoryItem.getState().get_text().value().equals(HistoryStateText.FAILED.value()));
		default:
			return orderHistoryItem != null;
		}
	}

	public boolean queryHistory() throws Exception {
		String query = args.getQuery();
		boolean result = false;
		result = executeApiCall(query);

		return result;
	}

	public void executeQuery() {
		// TODO Auto-generated method stub

	}

}