package com.sos.jitl.jobs.checkhistory;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.checkhistory.classes.AccessTokenProvider;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryHelper;
import com.sos.jitl.jobs.checkhistory.classes.Globals;
import com.sos.jitl.jobs.checkhistory.classes.HistoryFilter;
import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.checkhistory.classes.HistoryWebserviceExecuter;
import com.sos.jitl.jobs.checkhistory.classes.JOCCredentialStoreParameters;
import com.sos.jitl.jobs.checkhistory.classes.WebserviceCredentials;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryInfo {

    private static final int EQ = 1;
    private static final int GE = 2;
    private static final int GT = 3;
    private static final int LE = 4;
    private static final int LT = 5;
    private CheckHistoryJobArguments args;
    private JobLogger logger;

    public HistoryInfo(JobLogger logger, CheckHistoryJobArguments args) {
        this.args = args;
        this.logger = logger;
    }

    private HistoryItem executeApiCall(String query) throws Exception {

        JOCCredentialStoreParameters jobSchedulerCredentialStoreJOCParameters = new JOCCredentialStoreParameters();
        jobSchedulerCredentialStoreJOCParameters.setUser(args.getAccount());
        jobSchedulerCredentialStoreJOCParameters.setPassword(args.getPassword());
        jobSchedulerCredentialStoreJOCParameters.setJocUrl(args.getJocUrl());
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(jobSchedulerCredentialStoreJOCParameters);
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken();

        HistoryWebserviceExecuter historyWebserviceExecuter = new HistoryWebserviceExecuter(logger, webserviceCredentials);
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
        boolean paramCompletedFrom = false;
        boolean paramCompletedTo = false;
        boolean paramCount = false;

        int countCommand = 0;
        int count = 0;

        if (parameters.length > 0) {
            for (String parameterAssignment : parameters) {
                String[] p = parameterAssignment.split("=");
                String pName = p[0].trim();
                if (pName.startsWith("count")) {
                    pName = "count";
                }
                String pValue = "";
                if (p.length > 1) {
                    pValue = p[1].trim();
                }
                switch (pName.toLowerCase()) {
                case "startedfrom":
                    paramStartedFrom = true;
                    startedFrom = pValue;
                    Globals.debug(logger, "startedFrom=" + startedFrom);
                    break;
                case "startedto":
                    paramStartedTo = true;
                    startedTo = pValue;
                    Globals.debug(logger, "startedTo=" + startedTo);
                    break;
                case "completedfrom":
                    paramCompletedFrom = true;
                    completedFrom = pValue;
                    Globals.debug(logger, "completedFrom=" + completedFrom);
                    break;
                case "completedto":
                    paramCompletedTo = true;
                    completedTo = pValue;
                    Globals.debug(logger, "completedto=" + completedTo);
                    break;
                case "count":
                    try {
                        String[] pEq = parameterAssignment.split("=");
                        String[] pLe = parameterAssignment.split("<=");
                        String[] pLt = parameterAssignment.split("<");
                        String[] pGe = parameterAssignment.split(">=");
                        String[] pGt = parameterAssignment.split(">");
                        if (pEq.length > 1) {
                            count = Integer.valueOf(pEq[1]);
                            countCommand = EQ;
                        }
                        if (pLe.length > 1) {
                            count = Integer.valueOf(pLe[1]);
                            countCommand = LE;
                        }
                        if (pLt.length > 1) {
                            count = Integer.valueOf(pLt[1]);
                            countCommand = LT;
                        }
                        if (pGe.length > 1) {
                            count = Integer.valueOf(pGe[1]);
                            countCommand = GE;
                        }
                        if (pGt.length > 1) {
                            count = Integer.valueOf(pGt[1]);
                            countCommand = GT;
                        }
                        paramCount = true;

                    } catch (NumberFormatException e) {
                        Globals.log(logger, "Not a valid number:" + pValue);
                        count = 0;
                    }
                    Globals.debug(logger, "completedto=" + completedTo);
                    break;
                default:
                    if (!pName.isEmpty()) {
                        throw new SOSException("unknown parameter name: " + pName);
                    }
                }
            }
        }

        historyFilter.setLimit(count + 1);
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
            if (paramCompletedFrom || paramCompletedTo) {
                historyFilter.setEndDateFrom(completedFrom);
                historyFilter.setEndDateTo(completedTo);
            }
            states.add(OrderStateText.FINISHED);
            historyFilter.setStates(states);
            break;
        case "iscompletedsuccessful":
            if (paramStartedFrom || paramStartedTo) {
                historyFilter.setDateFrom(startedFrom);
                historyFilter.setDateTo(startedTo);
            }
            if (paramCompletedFrom || paramCompletedTo) {
                historyFilter.setEndDateFrom(completedFrom);
                historyFilter.setEndDateTo(completedTo);
            }

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
            if (paramCompletedFrom || paramCompletedTo) {
                historyFilter.setEndDateFrom(completedFrom);
                historyFilter.setEndDateTo(completedTo);
            }
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
            if (paramCompletedFrom || paramCompletedTo) {
                historyFilter.setEndDateFrom(completedFrom);
                historyFilter.setEndDateTo(completedTo);
            }
            states.add(OrderStateText.FINISHED);
            historyFilter.setStates(states);
            break;
        default:
            throw new SOSException("unknown query: " + query);
        }

        HistoryItem historyItem;
        if (args.getJob() != null && !args.getJob().isEmpty()) {
            JobsFilter jobsFilter = new JobsFilter();
            jobsFilter.setLimit(historyFilter.getLimit());
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

            TaskHistory taskHistory = historyWebserviceExecuter.getJobHistoryEntry(jobsFilter);
            historyItem = new HistoryItem(taskHistory);
            historyItem.setJob(historyFilter.getJob());

        } else {

            OrdersFilter ordersFilter = new OrdersFilter();
            ordersFilter.setLimit(historyFilter.getLimit());
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

            OrderHistory orderHistory = historyWebserviceExecuter.getWorkflowHistoryEntry(ordersFilter);
            historyItem = new HistoryItem(orderHistory);
            historyItem.setWorkflow(historyFilter.getWorkflow());
        }

        switch (query.toLowerCase()) {
        case "lastcompletedsuccessful":
            historyItem.setResult((historyItem.getHistoryItemFound() && historyItem.getState().get_text().value().equals(HistoryStateText.SUCCESSFUL
                    .value())));
            break;
        case "lastcompletedfailed":
            historyItem.setResult((historyItem.getHistoryItemFound() && historyItem.getState().get_text().value().equals(HistoryStateText.FAILED
                    .value())));
            break;
        default:
            if (paramCount && historyItem.getResult()) {
                switch (countCommand) {
                case EQ:
                    historyItem.setResult(historyItem.getCount() == count);
                    break;
                case GE:
                    historyItem.setResult(historyItem.getCount() >= count);
                    break;
                case GT:
                    historyItem.setResult(historyItem.getCount() > count);
                    break;
                case LE:
                    historyItem.setResult(historyItem.getCount() <= count);
                    break;
                case LT:
                    historyItem.setResult(historyItem.getCount() < count);
                    break;
                default:
                    throw new SOSException("unknown operator in count parameter");
                }

            }
        }

        return historyItem;
    }

    public HistoryItem queryHistory() throws Exception {
        String query = args.getQuery();
        return executeApiCall(query);
    }

}