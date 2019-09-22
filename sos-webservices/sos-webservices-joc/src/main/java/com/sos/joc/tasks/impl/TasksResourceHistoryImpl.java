package com.sos.joc.tasks.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.OrderPath;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.tasks.resource.ITasksResourceHistory;

@Path("tasks")
public class TasksResourceHistoryImpl extends JOCResourceImpl implements ITasksResourceHistory {

    private static final String API_CALL = "./tasks/history";

    @Override
    public JOCDefaultResponse postTasksHistory(String accessToken, JobsFilter jobsFilter) {
        SOSHibernateSession connection = null;
        try {
            if (jobsFilter.getJobschedulerId() == null) {
                jobsFilter.setJobschedulerId("");
            }
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobsFilter, accessToken, jobsFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    jobsFilter.getJobschedulerId(), accessToken).getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            Globals.beginTransaction(connection);

            List<TaskHistoryItem> listOfHistory = new ArrayList<>();
            boolean withFolderFilter = jobsFilter.getFolders() != null && !jobsFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            boolean getTaskFromHistoryIdAndNode = false;
            boolean getTaskFromOrderHistory = false;
            List<Folder> folders = addPermittedFolder(jobsFilter.getFolders());

            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setSchedulerId(jobsFilter.getJobschedulerId());
            if (jobsFilter.getHistoryIds() != null && !jobsFilter.getHistoryIds().isEmpty()) {
                getTaskFromHistoryIdAndNode = true;
            } else if (jobsFilter.getOrders() != null && !jobsFilter.getOrders().isEmpty()) {
                getTaskFromOrderHistory = true;
            } else {
                if (jobsFilter.getDateFrom() != null) {
                    historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(jobsFilter.getDateFrom(), jobsFilter.getTimeZone()));
                }
                if (jobsFilter.getDateTo() != null) {
                    historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(jobsFilter.getDateTo(), jobsFilter.getTimeZone()));
                }

                if (!jobsFilter.getHistoryStates().isEmpty()) {
                    historyFilter.setState(jobsFilter.getHistoryStates());
                }

                if (!jobsFilter.getJobs().isEmpty()) {
                    final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                    historyFilter.setJobs(jobsFilter.getJobs().stream().filter(job -> job != null && canAdd(job.getJob(), permittedFolders)).map(
                            JobPath::getJob).collect(Collectors.toSet()));
                    jobsFilter.setRegex("");
                } else {
                    if (SearchStringHelper.isDBWildcardSearch(jobsFilter.getRegex())) {
                        String[] jobs = jobsFilter.getRegex().split(",");
                        for (String j : jobs) {
                            historyFilter.addJob(j);
                        }
                        jobsFilter.setRegex("");
                    }

                    if (!jobsFilter.getExcludeJobs().isEmpty()) {
                        historyFilter.setExcludedJobs(jobsFilter.getExcludeJobs().stream().map(JobPath::getJob).collect(Collectors.toSet()));
                    }

                    if (withFolderFilter && (folders == null || folders.isEmpty())) {
                        hasPermission = false;
                    } else if (folders != null && !folders.isEmpty()) {
                        historyFilter.setFolders(folders.stream().map(folder -> {
                            folder.setFolder(normalizeFolder(folder.getFolder()));
                            return folder;
                        }).collect(Collectors.toSet()));
                    }
                }
            }

            if (hasPermission) {

                if (jobsFilter.getLimit() == null) {
                    jobsFilter.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }

                historyFilter.setLimit(jobsFilter.getLimit());
                List<DBItemOrderStep> dbOrderStepItems = new ArrayList<>();

                JobHistoryDBLayer jobHistoryDbLayer = new JobHistoryDBLayer(connection, historyFilter);

                if (getTaskFromHistoryIdAndNode) {
                    // dbOrderStepItems = jobHistoryDbLayer.getSchedulerHistoryListFromHistoryIdAndNode(jobsFilter
                    // .getHistoryIds());
                } else if (getTaskFromOrderHistory) {
                    for (OrderPath orderPath : jobsFilter.getOrders()) {
                        checkRequiredParameter("workflow", orderPath.getWorkflow());
                        orderPath.setWorkflow(normalizePath(orderPath.getWorkflow()));
                    }
                    // dbOrderStepItems = jobHistoryDbLayer.getSchedulerHistoryListFromOrder(jobsFilter.getOrders());
                } else {
                    dbOrderStepItems = jobHistoryDbLayer.getJobHistoryFromTo();
                }

                Matcher regExMatcher = null;
                if (jobsFilter.getRegex() != null && !jobsFilter.getRegex().isEmpty()) {
                    regExMatcher = Pattern.compile(jobsFilter.getRegex()).matcher("");
                }

                if (dbOrderStepItems != null) {
                    for (DBItemOrderStep dbItemOrderStep : dbOrderStepItems) {
                        if (!getPermissonsJocCockpit(dbItemOrderStep.getMasterId(), accessToken).getHistory().getView().isStatus()) {
                            continue;
                        }
                        if (regExMatcher != null && !regExMatcher.reset(dbItemOrderStep.getWorkflowPath() + "/" + dbItemOrderStep.getJobName())
                                .find()) {
                            continue;
                        }
                        TaskHistoryItem taskHistoryItem = new TaskHistoryItem();
                        taskHistoryItem.setJobschedulerId(dbItemOrderStep.getMasterId());
                        taskHistoryItem.setAgent(dbItemOrderStep.getAgentUri());
                        taskHistoryItem.setEndTime(dbItemOrderStep.getEndTime());
                        taskHistoryItem.setError(setError(dbItemOrderStep));
                        taskHistoryItem.setJob(dbItemOrderStep.getJobName());
                        taskHistoryItem.setOrderId(dbItemOrderStep.getOrderKey());
                        taskHistoryItem.setReturnCode(dbItemOrderStep.getReturnCode().intValue());
                        taskHistoryItem.setState(setState(dbItemOrderStep));
                        taskHistoryItem.setSurveyDate(dbItemOrderStep.getModified());
                        taskHistoryItem.setTaskId(dbItemOrderStep.getId());
                        taskHistoryItem.setWorkflow(dbItemOrderStep.getWorkflowPath());
                        
                        listOfHistory.add(taskHistoryItem);
                    }
                }
            }

            TaskHistory entity = new TaskHistory();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setHistory(listOfHistory);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private HistoryState setState(DBItemOrderStep dbItemOrderStep) {
        HistoryState state = new HistoryState();
        if (dbItemOrderStep.isSuccessFul()) {
            state.setSeverity(0);
            state.set_text(HistoryStateText.SUCCESSFUL);
        } else if (dbItemOrderStep.isInComplete()) {
            state.setSeverity(1);
            state.set_text(HistoryStateText.INCOMPLETE);
        } else if (dbItemOrderStep.isFailed()) {
            state.setSeverity(2);
            state.set_text(HistoryStateText.FAILED);
        }
        return state;
    }
    
    private Err setError(DBItemOrderStep dbItemOrderStep) {
        if (dbItemOrderStep.getError()) {
            Err error = new Err();
            error.setCode(dbItemOrderStep.getErrorCode());
            error.setMessage(dbItemOrderStep.getErrorText());
            return error;
        }
        return null;
    }
}
