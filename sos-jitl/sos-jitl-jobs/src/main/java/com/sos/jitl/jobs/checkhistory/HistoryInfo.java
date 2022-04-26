package com.sos.jitl.jobs.checkhistory;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.checkhistory.classes.AccessTokenProvider;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryHelper;
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
		String queryName = CheckHistoryHelper.getQueryName(query);
		String parameter = CheckHistoryHelper.getParameter(query);
		String[] parameters = parameter.split(",");
		String startedFrom = "0d";
		String startedTo = "0d";
		String completedFrom = "0d";
		String completedTo = "0d";
		boolean paramStartedFrom = false;
		boolean paramStartedTo = false;

		if (parameters.length > 0) {
			for (String parameterAssignment : parameters) {
				String[] p = parameterAssignment.split("=");
				String pName = p[0];
				String pValue = "";
				if (p.length > 1) {
					pValue = p[1];
				}
				switch (pName.toLowerCase()) {
				case "startedfrom":
					paramStartedFrom = true;
					startedFrom = pValue;
					break;
				case "startedto":
					paramStartedTo = true;
					startedTo = pValue;
					break;
				case "completedfrom":
					completedTo = pValue;
					break;
				case "completedto":
					startedFrom = pValue;
					break;
				default:
					if (!pName.isEmpty()) {
						throw new SOSException("unknown parameter name: " + pName);
					}
				}
			}
		}

		switch (queryName.toLowerCase())

		{
		case "isstarted":
			ordersFilter.setDateFrom(startedFrom);
			ordersFilter.setDateTo(startedTo);
			break;
		case "iscompleted":
			if (paramStartedFrom || paramStartedTo) {
				ordersFilter.setDateFrom(startedFrom);
				ordersFilter.setDateTo(startedTo);
			}
			ordersFilter.setEndDateFrom(completedFrom);
			ordersFilter.setEndDateTo(completedTo);
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		case "iscompletedsuccessful":
			if (paramStartedFrom || paramStartedTo) {
				ordersFilter.setDateFrom(startedFrom);
				ordersFilter.setDateTo(startedTo);
			}
			ordersFilter.setEndDateFrom(completedFrom);
			ordersFilter.setEndDateTo(completedTo);

			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.SUCCESSFUL);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "iscompletedfailed":
			if (paramStartedFrom || paramStartedTo) {
				ordersFilter.setDateFrom(startedFrom);
				ordersFilter.setDateTo(startedTo);
			}
			ordersFilter.setEndDateFrom(completedFrom);
			ordersFilter.setEndDateTo(completedTo);
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.FAILED);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;

		case "lastcompletedsuccessful":
		case "lastcompletedfailed":
			if (paramStartedFrom || paramStartedTo) {
				ordersFilter.setDateFrom(startedFrom);
				ordersFilter.setDateTo(startedTo);
			}
			ordersFilter.setEndDateFrom(completedFrom);
			ordersFilter.setEndDateTo(completedTo);
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		default:
			throw new SOSException("unknown query: " + query);
		}

		ordersFilter.setLimit(1);
		OrderHistoryItem orderHistoryItem = historyWebserviceExecuter.getJobHistoryEntry(ordersFilter);

		switch (query.toLowerCase()) {
		case "lastcompletedsuccessful":
			return (orderHistoryItem != null
					&& orderHistoryItem.getState().get_text().value().equals(HistoryStateText.SUCCESSFUL.value()));
		case "lastcompletedfailed":
			return (orderHistoryItem != null
					&& orderHistoryItem.getState().get_text().value().equals(HistoryStateText.FAILED.value()));
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

}