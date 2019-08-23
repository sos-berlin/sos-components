package com.sos.joc.tasks.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.db.history.OrderStepFilter;
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
            
            OrderStepFilter orderStepFilter = new OrderStepFilter();
            orderStepFilter.setSchedulerId(jobsFilter.getJobschedulerId());
            if (jobsFilter.getHistoryIds() != null && !jobsFilter.getHistoryIds().isEmpty()) {
                getTaskFromHistoryIdAndNode = true;
            } else if (jobsFilter.getOrders() != null && !jobsFilter.getOrders().isEmpty()) {
                getTaskFromOrderHistory = true;
            } else {
                if (jobsFilter.getDateFrom() != null) {
                    orderStepFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(jobsFilter.getDateFrom(), jobsFilter.getTimeZone()));
                }
                if (jobsFilter.getDateTo() != null) {
                    orderStepFilter.setExecutedTo(JobSchedulerDate.getDateTo(jobsFilter.getDateTo(), jobsFilter.getTimeZone()));
                }

                if (jobsFilter.getHistoryStates().size() > 0) {
                    for (HistoryStateText historyStateText : jobsFilter.getHistoryStates()) {
                        orderStepFilter.addState(historyStateText.toString());
                    }
                }

                if (jobsFilter.getJobs().size() > 0) {
                    Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                    for (JobPath jobPath : jobsFilter.getJobs()) {
                        if (jobPath != null && canAdd(jobPath.getJob(), permittedFolders)) {
                            orderStepFilter.addJob(jobPath.getJob());
                        }
                    }
                    jobsFilter.setRegex("");
                } else {
                    if (SearchStringHelper.isDBWildcardSearch(jobsFilter.getRegex())) {
                        String[] jobs = jobsFilter.getRegex().split(",");
                        for (String j : jobs) {
                            orderStepFilter.addJob(j);
                        }
                        jobsFilter.setRegex("");
                    }
                    
                    if (jobsFilter.getExcludeJobs().size() > 0) {
                        for (JobPath jobPath : jobsFilter.getExcludeJobs()) {
                            orderStepFilter.addExcludedJob(jobPath.getJob());
                        }
                    }

                    if (withFolderFilter && (folders == null || folders.isEmpty())) {
                        hasPermission = false;
                    } else if (folders != null && !folders.isEmpty()) {
                        for (Folder folder : folders) {
                            folder.setFolder(normalizeFolder(folder.getFolder()));
                            orderStepFilter.addFolder(folder);
                        }
                    }
                }
            }
            
            if (hasPermission) {

                if (jobsFilter.getLimit() == null) {
                    jobsFilter.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }
                
                orderStepFilter.setLimit(jobsFilter.getLimit());
                List<DBItemOrderStep> listOfDBItemReportTaskDBItems = new ArrayList<>();
                
                JobHistoryDBLayer jobHistoryDbLayer = new JobHistoryDBLayer(connection);

                if (getTaskFromHistoryIdAndNode) {
                    //listOfDBItemReportTaskDBItems = jobHistoryDbLayer.getSchedulerHistoryListFromHistoryIdAndNode(jobsFilter
                    //        .getHistoryIds());
                } else if (getTaskFromOrderHistory) {
                    for (OrderPath orderPath : jobsFilter.getOrders()) {
                        checkRequiredParameter("workflow", orderPath.getWorkflow());
                        orderPath.setWorkflow(normalizePath(orderPath.getWorkflow()));
                    }
                    //listOfDBItemReportTaskDBItems = jobHistoryDbLayer.getSchedulerHistoryListFromOrder(jobsFilter.getOrders());
                } else {
                    listOfDBItemReportTaskDBItems = jobHistoryDbLayer.getJobHistoryFromTo(orderStepFilter);
                }
                
                Matcher regExMatcher = null;
                if (jobsFilter.getRegex() != null && !jobsFilter.getRegex().isEmpty()) {
                    regExMatcher = Pattern.compile(jobsFilter.getRegex()).matcher("");
                }

                if (listOfDBItemReportTaskDBItems != null) {
                    for (DBItemOrderStep dbItemOrderStep : listOfDBItemReportTaskDBItems) {
                        TaskHistoryItem taskHistoryItem = new TaskHistoryItem();
                        if (!getPermissonsJocCockpit(dbItemOrderStep.getMasterId(), accessToken).getHistory().getView().isStatus()) {
                            continue;
                        }
                        taskHistoryItem.setJobschedulerId(dbItemOrderStep.getMasterId());
                        taskHistoryItem.setAgent(dbItemOrderStep.getAgentUri());
                        //TODO??? taskHistoryItem.setClusterMember(dbItemOrderStep.getClusterMemberId());
                        taskHistoryItem.setEndTime(dbItemOrderStep.getEndTime());
                        if (dbItemOrderStep.getError()) {
                            Err error = new Err();
                            error.setCode(dbItemOrderStep.getErrorCode());
                            error.setMessage(dbItemOrderStep.getErrorText());
                            taskHistoryItem.setError(error);
                        }

                        taskHistoryItem.setExitCode(dbItemOrderStep.getReturnCode().intValue());
                        taskHistoryItem.setJob(dbItemOrderStep.getJobName());
                        taskHistoryItem.setStartTime(dbItemOrderStep.getStartTime());

                        HistoryState state = new HistoryState();
                        if (dbItemOrderStep.isSuccessFul()) {
                            state.setSeverity(0);
                            state.set_text(HistoryStateText.SUCCESSFUL);
                        }
                        if (dbItemOrderStep.isInComplete()) {
                            state.setSeverity(1);
                            state.set_text(HistoryStateText.INCOMPLETE);
                        }
                        if (dbItemOrderStep.isFailed()) {
                            state.setSeverity(2);
                            state.set_text(HistoryStateText.FAILED);
                        }
                        taskHistoryItem.setState(state);
                        taskHistoryItem.setSurveyDate(dbItemOrderStep.getModified());

                        taskHistoryItem.setSteps(dbItemOrderStep.getPosition().intValue());  //TODO workflow position maybe better?
                        taskHistoryItem.setTaskId(dbItemOrderStep.getId());

                        if (regExMatcher != null) {
                            regExMatcher.reset(dbItemOrderStep.getWorkflowPath() + "/" + dbItemOrderStep.getJobName());
                            if (!regExMatcher.find()) {
                                continue;
                            }
                        }
                        listOfHistory.add(taskHistoryItem);
                    }
                }
            }
            

            TaskHistory entity = new TaskHistory();
            entity.setDeliveryDate(new Date());
            entity.setHistory(listOfHistory);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
