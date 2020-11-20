package com.sos.joc.tasks.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
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
import com.sos.joc.model.job.TaskIdOfOrder;
import com.sos.joc.tasks.resource.ITasksResourceHistory;
import com.sos.schema.JsonValidator;

@Path("tasks")
public class TasksResourceHistoryImpl extends JOCResourceImpl implements ITasksResourceHistory {

    private static final String API_CALL = "./tasks/history";

    @Override
    public JOCDefaultResponse postTasksHistory(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobsFilter.class);
            JobsFilter jobsFilter = Globals.objectMapper.readValue(filterBytes, JobsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(jobsFilter.getControllerId(), getPermissonsJocCockpit(jobsFilter
                    .getControllerId(), accessToken).getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<TaskHistoryItem> listOfHistory = new ArrayList<TaskHistoryItem>();
            boolean withFolderFilter = jobsFilter.getFolders() != null && !jobsFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            boolean getTaskFromHistoryIdAndNode = false;
            boolean getTaskFromOrderHistory = false;
            Set<Folder> folders = addPermittedFolder(jobsFilter.getFolders());

            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setSchedulerId(jobsFilter.getControllerId());

            if (jobsFilter.getTaskIds() != null && !jobsFilter.getTaskIds().isEmpty()) {
                historyFilter.setHistoryIds(jobsFilter.getTaskIds());
            } else {
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

                    if (jobsFilter.getHistoryStates() != null && !jobsFilter.getHistoryStates().isEmpty()) {
                        historyFilter.setState(jobsFilter.getHistoryStates());
                    }

                    if (jobsFilter.getCriticalities() != null && !jobsFilter.getCriticalities().isEmpty()) {
                        historyFilter.setCriticalities(jobsFilter.getCriticalities());
                    }

                    if (jobsFilter.getJobs() != null && !jobsFilter.getJobs().isEmpty()) {
                        final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        historyFilter.setJobs(jobsFilter.getJobs().stream().filter(job -> job != null && canAdd(job.getWorkflowPath(),
                                permittedFolders)).collect(Collectors.groupingBy(job -> normalizePath(job.getWorkflowPath()), Collectors.mapping(
                                        JobPath::getJob, Collectors.toSet()))));
                        jobsFilter.setRegex("");

                    } else {

                        if (SearchStringHelper.isDBWildcardSearch(jobsFilter.getRegex())) {
                            historyFilter.setWorkflows(Arrays.asList(jobsFilter.getRegex().split(",")));
                            jobsFilter.setRegex("");
                        }

                        if (!jobsFilter.getExcludeJobs().isEmpty()) {
                            historyFilter.setExcludedJobs(jobsFilter.getExcludeJobs().stream().collect(Collectors.groupingBy(job -> normalizePath(job
                                    .getWorkflowPath()), Collectors.mapping(JobPath::getJob, Collectors.toSet()))));
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
            }

            if (hasPermission) {

                if (jobsFilter.getLimit() == null) {
                    jobsFilter.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }

                historyFilter.setLimit(jobsFilter.getLimit());

                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                JobHistoryDBLayer jobHistoryDbLayer = new JobHistoryDBLayer(connection, historyFilter);

                ScrollableResults sr = null;
                try {
                    if (getTaskFromHistoryIdAndNode) {
                        sr = jobHistoryDbLayer.getJobsFromHistoryIdAndPosition(jobsFilter.getHistoryIds().stream().filter(Objects::nonNull).filter(
                                t -> t.getHistoryId() != null).collect(Collectors.groupingBy(TaskIdOfOrder::getHistoryId, Collectors.mapping(
                                        TaskIdOfOrder::getPosition, Collectors.toSet()))));
                    } else if (getTaskFromOrderHistory) {
                        final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        sr = jobHistoryDbLayer.getJobsFromOrder(jobsFilter.getOrders().stream().filter(Objects::nonNull).filter(order -> canAdd(order
                                .getWorkflowPath(), permittedFolders)).collect(Collectors.groupingBy(order -> normalizePath(order.getWorkflowPath()),
                                        Collectors.groupingBy(o -> o.getOrderId() == null ? "" : o.getOrderId(), Collectors.mapping(
                                                OrderPath::getPosition, Collectors.toSet())))));
                    } else {
                        sr = jobHistoryDbLayer.getJobs();
                    }

                    Matcher regExMatcher = null;
                    if (jobsFilter.getRegex() != null && !jobsFilter.getRegex().isEmpty()) {
                        regExMatcher = Pattern.compile(jobsFilter.getRegex()).matcher("");
                    }

                    if (sr != null) {
                        while (sr.next()) {
                            DBItemHistoryOrderStep step = (DBItemHistoryOrderStep) sr.get(0);

                            if (jobsFilter.getControllerId().isEmpty() && !getPermissonsJocCockpit(step.getJobSchedulerId(), accessToken).getHistory()
                                    .getView().isStatus()) {
                                continue;
                            }
                            if (regExMatcher != null && !regExMatcher.reset(step.getWorkflowPath() + "," + step.getJobName()).find()) {
                                continue;
                            }
                            TaskHistoryItem item = new TaskHistoryItem();
                            item.setControllerId(step.getJobSchedulerId());
                            item.setAgentUrl(step.getAgentUri());
                            item.setStartTime(step.getStartTime());
                            item.setEndTime(step.getEndTime());
                            item.setError(setError(step));
                            item.setJob(step.getJobName());
                            item.setOrderId(step.getOrderKey());
                            item.setExitCode(step.getReturnCode());
                            item.setState(setState(step));
                            item.setCriticality(step.getCriticalityAsEnum().value().toLowerCase());
                            item.setSurveyDate(step.getModified());
                            item.setTaskId(step.getId());
                            item.setWorkflow(step.getWorkflowPath());
                            item.setPosition(step.getWorkflowPosition());

                            listOfHistory.add(item);
                        }
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (sr != null) {
                        sr.close();
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

    private HistoryState setState(DBItemHistoryOrderStep step) {
        HistoryState state = new HistoryState();
        if (step.isSuccessFul()) {
            state.setSeverity(0);
            state.set_text(HistoryStateText.SUCCESSFUL);
        } else if (step.isInComplete()) {
            state.setSeverity(1);
            state.set_text(HistoryStateText.INCOMPLETE);
        } else if (step.isFailed()) {
            state.setSeverity(2);
            state.set_text(HistoryStateText.FAILED);
        }
        return state;
    }

    private Err setError(DBItemHistoryOrderStep step) {
        if (step.getError()) {
            Err error = new Err();
            // TODO maybe use step.getErrorState()
            error.setCode(step.getErrorCode());
            if (step.getErrorText() != null && step.getErrorText().isEmpty()) {
                error.setMessage(step.getErrorText());
            } else {
                error.setMessage(step.getErrorReason());
            }
            return error;
        }
        return null;
    }
}
