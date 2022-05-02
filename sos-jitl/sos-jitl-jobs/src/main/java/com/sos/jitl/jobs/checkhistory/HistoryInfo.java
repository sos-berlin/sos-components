package com.sos.jitl.jobs.checkhistory;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.checkhistory.classes.AccessTokenProvider;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryHelper;
import com.sos.jitl.jobs.checkhistory.classes.HistoryFilter;
import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.checkhistory.classes.HistoryWebserviceExecuter;
import com.sos.jitl.jobs.checkhistory.classes.JOCCredentialStoreParameters;
import com.sos.jitl.jobs.checkhistory.classes.WebserviceCredentials;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryInfo {

    private CheckHistoryJobArguments args;

    public HistoryInfo(CheckHistoryJobArguments args) {
        this.args = args;
    }

    private HistoryItem executeApiCall(String query) throws Exception {

        JOCCredentialStoreParameters jobSchedulerCredentialStoreJOCParameters = new JOCCredentialStoreParameters();
        jobSchedulerCredentialStoreJOCParameters.setUser(args.getAccount());
        jobSchedulerCredentialStoreJOCParameters.setPassword(args.getPassword());
        jobSchedulerCredentialStoreJOCParameters.setJocUrl(args.getJocUrl());
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(jobSchedulerCredentialStoreJOCParameters);
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken();

        HistoryWebserviceExecuter historyWebserviceExecuter = new HistoryWebserviceExecuter(webserviceCredentials);
        HistoryFilter historyFilter = new HistoryFilter();
        historyFilter.setJob(args.getJob());
        historyFilter.setWorkflow(args.getWorkflow());

        historyFilter.setControllerId(args.getController());

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

        switch (queryName.toLowerCase()) {
        case "isstarted":
            historyFilter.setDateFrom(startedFrom);
            historyFilter.setDateTo(startedTo);
            break;
        case "iscompleted":
            if (paramStartedFrom || paramStartedTo) {
                historyFilter.setDateFrom(startedFrom);
                historyFilter.setDateTo(startedTo);
            }
            historyFilter.setEndDateFrom(completedFrom);
            historyFilter.setEndDateTo(completedTo);
            states.add(OrderStateText.FINISHED);
            historyFilter.setStates(states);
            break;
        case "iscompletedsuccessful":
            if (paramStartedFrom || paramStartedTo) {
                historyFilter.setDateFrom(startedFrom);
                historyFilter.setDateTo(startedTo);
            }
            historyFilter.setEndDateFrom(completedFrom);
            historyFilter.setEndDateTo(completedTo);

            states.add(OrderStateText.FINISHED);
            historyStates.add(HistoryStateText.SUCCESSFUL);
            historyFilter.setHistoryStates(historyStates);
            historyFilter.setStates(states);
            break;
        case "iscompletedfailed":
            if (paramStartedFrom || paramStartedTo) {
                historyFilter.setDateFrom(startedFrom);
                historyFilter.setDateTo(startedTo);
            }
            historyFilter.setEndDateFrom(completedFrom);
            historyFilter.setEndDateTo(completedTo);
            states.add(OrderStateText.FINISHED);
            historyStates.add(HistoryStateText.FAILED);
            historyFilter.setHistoryStates(historyStates);
            historyFilter.setStates(states);
            break;

        case "lastcompletedsuccessful":
        case "lastcompletedfailed":
            if (paramStartedFrom || paramStartedTo) {
                historyFilter.setDateFrom(startedFrom);
                historyFilter.setDateTo(startedTo);
            }
            historyFilter.setEndDateFrom(completedFrom);
            historyFilter.setEndDateTo(completedTo);
            states.add(OrderStateText.FINISHED);
            historyFilter.setStates(states);
            break;
        default:
            throw new SOSException("unknown query: " + query);
        }

        HistoryItem historyItem;
        if (args.getJob() != null && !args.getJob().isEmpty()) {
            JobsFilter jobsFilter = new JobsFilter();
            jobsFilter.setLimit(1);
            jobsFilter.setControllerId(historyFilter.getControllerId());
            jobsFilter.setJobName(historyFilter.getJob());
            jobsFilter.setWorkflowName(historyFilter.getWorkflow());
            jobsFilter.setDateFrom(historyFilter.getDateFrom());
            jobsFilter.setDateTo(historyFilter.getDateTo());
            jobsFilter.setEndDateTo(historyFilter.getEndDateTo());
            jobsFilter.setEndDateFrom(historyFilter.getEndDateFrom());
            jobsFilter.setFolders(historyFilter.getFolders());
            jobsFilter.setHistoryStates(historyFilter.getHistoryStates());
            jobsFilter.setTimeZone(historyFilter.getTimeZone());

            TaskHistoryItem taskHistoryItem = historyWebserviceExecuter.getJobHistoryEntry(jobsFilter);
            historyItem = new HistoryItem(taskHistoryItem);
        } else {

            OrdersFilter ordersFilter = new OrdersFilter();
            ordersFilter.setLimit(1);
            ordersFilter.setControllerId(historyFilter.getControllerId());
            ordersFilter.setWorkflowName(historyFilter.getWorkflow());
            ordersFilter.setDateFrom(historyFilter.getDateFrom());
            ordersFilter.setDateTo(historyFilter.getDateTo());
            ordersFilter.setEndDateTo(historyFilter.getEndDateTo());
            ordersFilter.setEndDateFrom(historyFilter.getEndDateFrom());
            ordersFilter.setFolders(historyFilter.getFolders());
            ordersFilter.setHistoryStates(historyFilter.getHistoryStates());
            ordersFilter.setStates(historyFilter.getStates());
            ordersFilter.setTimeZone(historyFilter.getTimeZone());

            OrderHistoryItem orderHistoryItem = historyWebserviceExecuter.getWorkflowHistoryEntry(ordersFilter);
            historyItem = new HistoryItem(orderHistoryItem);
        }

        switch (query.toLowerCase()) {
        case "lastcompletedsuccessful":
            historyItem.setResult((historyItem.getHistoryItemFound() && historyItem.getState().get_text().value().equals(HistoryStateText.SUCCESSFUL
                    .value())));
        case "lastcompletedfailed":
            historyItem.setResult((historyItem.getHistoryItemFound() && historyItem.getState().get_text().value().equals(HistoryStateText.FAILED
                    .value())));
        }
        return historyItem;

    }

    public HistoryItem queryHistory() throws Exception {
        String query = args.getQuery();
        return executeApiCall(query);
    }

}