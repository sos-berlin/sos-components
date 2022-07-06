package com.sos.jitl.jobs.checkhistory;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.checkhistory.classes.CheckHistoryHelper;
import com.sos.jitl.jobs.checkhistory.classes.Globals;
import com.sos.jitl.jobs.checkhistory.classes.HistoryFilter;
import com.sos.jitl.jobs.checkhistory.classes.HistoryItem;
import com.sos.jitl.jobs.checkhistory.classes.HistoryWebserviceExecuter;
import com.sos.jitl.jobs.checkhistory.classes.ParameterResolver;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrdersFilter;

public class HistoryInfo {

    private CheckHistoryJobArguments args;
    private JobLogger logger;

    public HistoryInfo(JobLogger logger, CheckHistoryJobArguments args) {
        this.args = args;
        this.logger = logger;
    }

    private HistoryItem executeApiCall(String query) throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(logger);
        String accessToken = null;
        try {
            ApiResponse apiResponse = apiExecutor.login();
            if (apiResponse.getStatusCode() == 200) {
                accessToken = apiResponse.getAccessToken();
            } else {
                String message = apiResponse.getStatusCode() + " " + apiExecutor.getClient().getHttpResponse().getStatusLine().getReasonPhrase();
                throw new SOSException(message);
            }

            HistoryWebserviceExecuter historyWebserviceExecuter = new HistoryWebserviceExecuter(logger, apiExecutor);
            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setJob(args.getJob());
            historyFilter.setWorkflow(args.getWorkflow());
            historyFilter.setControllerId(args.getControllerId());

            List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
            String queryName = CheckHistoryHelper.getQueryName(query);
            String parameter = CheckHistoryHelper.getParameter(query);

            ParameterResolver parameterResolver = new ParameterResolver(logger);
            parameterResolver.resolveParameter(parameter);

            historyFilter.setLimit(parameterResolver.getCount() + 1);
            switch (queryName.toLowerCase()) {
            case "isstarted":
                if (parameterResolver.isParamCompletedFrom() || parameterResolver.isParamCompletedTo()) {
                    historyFilter.setDateFrom(parameterResolver.getStartedFrom());
                    historyFilter.setDateTo(parameterResolver.getStartedTo());
                }
                historyFilter.setDateFrom(parameterResolver.getStartedFrom());
                historyFilter.setDateTo(parameterResolver.getStartedTo());
                break;
            case "iscompleted":
                if (parameterResolver.isParamStartedFrom() || parameterResolver.isParamStartedTo()) {
                    historyFilter.setDateFrom(parameterResolver.getStartedFrom());
                    historyFilter.setDateTo(parameterResolver.getStartedTo());
                }
                historyFilter.setEndDateFrom(parameterResolver.getCompletedFrom());
                historyFilter.setEndDateTo(parameterResolver.getCompletedTo());

                historyStates.add(HistoryStateText.SUCCESSFUL);
                historyStates.add(HistoryStateText.FAILED);
                historyFilter.setHistoryStates(historyStates);
                break;
            case "iscompletedsuccessful":
                if (parameterResolver.isParamStartedFrom() || parameterResolver.isParamStartedTo()) {
                    historyFilter.setDateFrom(parameterResolver.getStartedFrom());
                    historyFilter.setDateTo(parameterResolver.getStartedTo());
                }
                historyFilter.setEndDateFrom(parameterResolver.getCompletedFrom());
                historyFilter.setEndDateTo(parameterResolver.getCompletedTo());

                historyStates.add(HistoryStateText.SUCCESSFUL);
                historyFilter.setHistoryStates(historyStates);
                break;
            case "iscompletedfailed":
                if (parameterResolver.isParamCompletedFrom() || parameterResolver.isParamCompletedTo()) {
                    historyFilter.setDateFrom(parameterResolver.getStartedFrom());
                    historyFilter.setDateTo(parameterResolver.getStartedTo());
                }
                historyFilter.setEndDateFrom(parameterResolver.getCompletedFrom());
                historyFilter.setEndDateTo(parameterResolver.getCompletedTo());

                historyStates.add(HistoryStateText.FAILED);
                historyFilter.setHistoryStates(historyStates);
                break;

            case "lastcompletedsuccessful":
            case "lastcompletedfailed":
                if (parameterResolver.isParamStartedFrom() || parameterResolver.isParamStartedTo()) {
                    historyFilter.setDateFrom(parameterResolver.getStartedFrom());
                    historyFilter.setDateTo(parameterResolver.getStartedTo());
                }
                historyFilter.setEndDateFrom(parameterResolver.getCompletedFrom());
                historyFilter.setEndDateTo(parameterResolver.getCompletedTo());

                historyStates.add(HistoryStateText.SUCCESSFUL);
                historyStates.add(HistoryStateText.FAILED);
                historyFilter.setHistoryStates(historyStates);

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
                jobsFilter.setCompletedDateTo(historyFilter.getEndDateTo());
                jobsFilter.setCompletedDateFrom(historyFilter.getEndDateFrom());
                jobsFilter.setFolders(historyFilter.getFolders());
                jobsFilter.setHistoryStates(historyFilter.getHistoryStates());
                jobsFilter.setTimeZone(historyFilter.getTimeZone());

                TaskHistory taskHistory = historyWebserviceExecuter.getJobHistoryEntry(jobsFilter, accessToken);
                historyItem = new HistoryItem(taskHistory);
            } else {

                OrdersFilter ordersFilter = new OrdersFilter();
                ordersFilter.setLimit(historyFilter.getLimit());
                ordersFilter.setControllerId(historyFilter.getControllerId());
                ordersFilter.setWorkflowName(historyFilter.getWorkflow());
                ordersFilter.setDateFrom(historyFilter.getDateFrom());
                ordersFilter.setDateTo(historyFilter.getDateTo());
                ordersFilter.setCompletedDateTo(historyFilter.getEndDateTo());
                ordersFilter.setCompletedDateFrom(historyFilter.getEndDateFrom());
                ordersFilter.setFolders(historyFilter.getFolders());
                ordersFilter.setHistoryStates(historyFilter.getHistoryStates());
                ordersFilter.setTimeZone(historyFilter.getTimeZone());

                OrderHistory orderHistory = historyWebserviceExecuter.getWorkflowHistoryEntry(ordersFilter, accessToken);
                historyItem = new HistoryItem(orderHistory);
            }

            switch (query.toLowerCase()) {
            case "lastcompletedsuccessful":
                historyItem.setResult((historyItem.getHistoryItemFound() && historyItem.getState().get_text().value().equals(
                        HistoryStateText.SUCCESSFUL.value())));
                break;
            case "lastcompletedfailed":
                historyItem.setResult((historyItem.getHistoryItemFound() && historyItem.getState().get_text().value().equals(HistoryStateText.FAILED
                        .value())));
                break;
            default:
                historyItem.setResult(parameterResolver.getCountResult(historyItem.getCount(), historyItem.getResult()));
            }

            return historyItem;
        } catch (Exception e) {
            Globals.error(logger, "", e);
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
        return null;
    }

    public HistoryItem queryHistory() throws Exception {
        String query = args.getQuery();
        return executeApiCall(query);
    }

}