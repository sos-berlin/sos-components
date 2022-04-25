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
		String parameter1 = "";
		String parameter2 = "";
		boolean haveParameter = false;

		switch (queryName.toLowerCase()) {
		case "isstartedtoday":
		case "iscompletedtoday":
		case "iscompletedtodaysuccessful":
		case "iscompletedtodaywitherror":
		case "isstartedtodaycompleted":
		case "isstartedtodaycompletedsuccessful":
		case "isstartedtodaycompletedwitherror":
		case "lastcompletedtodaysuccessful":
		case "lastcompletedtodaywitherror":
			parameter1 = "0d";
			parameter2 = "0d";
			haveParameter = true;
			break;
		default:
			if (parameters.length > 0) {
				haveParameter = true;
				parameter1 = parameters[0];
				if (parameters.length > 1) {
					parameter2 = parameters[1];
				} else {
					parameter2 = "0d";
				}
			}
		}

		switch (queryName.toLowerCase()) {
		case "isstarted":
		case "isstartedtoday":
			if (haveParameter) {
				ordersFilter.setDateFrom(parameter1);
				ordersFilter.setDateTo(parameter2);
			}
			break;
		case "iscompleted":
		case "iscompletedtoday":
			if (haveParameter) {
				ordersFilter.setEndDateFrom(parameter1);
				ordersFilter.setEndDateTo(parameter2);
			}
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		case "iscompletedsuccessful":
		case "iscompletedtodaysuccessful":
			if (haveParameter) {
				ordersFilter.setEndDateFrom(parameter1);
				ordersFilter.setEndDateTo(parameter2);
			}
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.SUCCESSFUL);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "iscompletedwitherror":
		case "iscompletedtodaywitherror":
			if (haveParameter) {
				ordersFilter.setEndDateFrom(parameter1);
				ordersFilter.setEndDateTo(parameter2);
			}
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.FAILED);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "isstartedcompleted":
		case "isstartedtodaycompleted":
			if (haveParameter) {
				ordersFilter.setDateFrom(parameter1);
				ordersFilter.setDateTo(parameter2);
			}
			states.add(OrderStateText.FINISHED);
			ordersFilter.setStates(states);
			break;
		case "isstartedcompletedsuccessful":
		case "isstartedtodaycompletedsuccessful":
			if (haveParameter) {
				ordersFilter.setDateFrom(parameter1);
				ordersFilter.setDateTo(parameter1);
			}
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.SUCCESSFUL);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "isstartedcompletedwitherror":
		case "isstartedtodaycompletedwitherror":
			if (haveParameter) {
				ordersFilter.setDateFrom(parameter1);
				ordersFilter.setDateTo(parameter2);
			}
			states.add(OrderStateText.FINISHED);
			historyStates.add(HistoryStateText.FAILED);
			ordersFilter.setHistoryStates(historyStates);
			ordersFilter.setStates(states);
			break;
		case "lastcompletedsuccessful":
		case "lastcompletedwitherror":
		case "lastcompletedtodaysuccessful":
		case "lastcompletedtodaywitherror":
			if (haveParameter) {
				ordersFilter.setEndDateFrom(parameter1);
				ordersFilter.setEndDateTo(parameter2);
			}

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
		case "lastcompletedtodaysuccessful":
			return (orderHistoryItem != null
					&& orderHistoryItem.getState().get_text().value().equals(HistoryStateText.SUCCESSFUL.value()));
		case "lastcompletedwitherror":
		case "lastcompletedtodaywitherror":
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