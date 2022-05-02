package com.sos.joc.tasks.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.job.TaskIdOfOrder;
import com.sos.joc.tasks.resource.ITasksResourceHistory;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.TASKS)
public class TasksResourceHistoryImpl extends JOCResourceImpl implements ITasksResourceHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksResourceHistoryImpl.class);

    @Override
    public JOCDefaultResponse postTasksHistory(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, JobsFilter.class);
            JobsFilter in = Globals.objectMapper.readValue(inBytes, JobsFilter.class);

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }

            JOCDefaultResponse response = initPermissions(controllerId, permitted);
            if (response != null) {
                return response;
            }

            List<TaskHistoryItem> history = new ArrayList<TaskHistoryItem>();
            boolean withFolderFilter = in.getFolders() != null && !in.getFolders().isEmpty();
            boolean hasPermission = true;
            boolean getTaskFromHistoryIdAndNode = false;
            Set<Folder> permittedFolders = addPermittedFolder(in.getFolders());
            boolean folderPermissionsAreChecked = false;

            HistoryFilter dbFilter = new HistoryFilter();
            dbFilter.setControllerIds(allowedControllers);

            if (in.getTaskIds() != null && !in.getTaskIds().isEmpty()) {
                dbFilter.setHistoryIds(in.getTaskIds());
            } else {
                if (in.getHistoryIds() != null && !in.getHistoryIds().isEmpty()) {
                    getTaskFromHistoryIdAndNode = true;
                } else {

                    if (in.getDateFrom() != null) {
                        dbFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()));
                    }
                    if (in.getDateTo() != null) {
                        dbFilter.setExecutedTo(JobSchedulerDate.getDateTo(in.getDateTo(), in.getTimeZone()));
                    }
                    if (in.getEndDateFrom() != null) {
                        dbFilter.setEndFrom(JobSchedulerDate.getDateFrom(in.getEndDateFrom(), in.getTimeZone()));
                    }
                    if (in.getEndDateTo() != null) {
                        dbFilter.setEndTo(JobSchedulerDate.getDateTo(in.getEndDateTo(), in.getTimeZone()));
                    }
                    if (in.getHistoryStates() != null && !in.getHistoryStates().isEmpty()) {
                        dbFilter.setState(in.getHistoryStates());
                    }

                    if (in.getCriticalities() != null && !in.getCriticalities().isEmpty()) {
                        dbFilter.setCriticalities(in.getCriticalities());
                    }

                    if (in.getJobs() != null && !in.getJobs().isEmpty()) {
                        dbFilter.setJobs(in.getJobs().stream().filter(Objects::nonNull).filter(job -> canAdd(WorkflowPaths.getPath(job
                                .getWorkflowPath()), permittedFolders)).peek(job -> job.setWorkflowPath(JocInventory.pathToName(job
                                        .getWorkflowPath()))).collect(Collectors.groupingBy(JobPath::getWorkflowPath, Collectors.mapping(
                                                JobPath::getJob, Collectors.toSet()))));
                        folderPermissionsAreChecked = true;
                    } else {

                        if (!in.getExcludeJobs().isEmpty()) {
                            dbFilter.setExcludedJobs(in.getExcludeJobs().stream().filter(Objects::nonNull).peek(job -> job.setWorkflowPath(
                                    WorkflowPaths.getPath(job.getWorkflowPath()))).collect(Collectors.groupingBy(JobPath::getWorkflowPath, Collectors
                                            .mapping(JobPath::getJob, Collectors.toSet()))));
                        }

                        if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
                            hasPermission = false;
                        } else if (withFolderFilter && permittedFolders != null && !permittedFolders.isEmpty()) {
                            dbFilter.setFolders(in.getFolders().stream().filter(folder -> folderIsPermitted(folder.getFolder(), permittedFolders))
                                    .collect(Collectors.toSet()));
                            folderPermissionsAreChecked = true;
                        }

                        dbFilter.setJobName(in.getJobName());
                        dbFilter.setWorkflowPath(in.getWorkflowPath());
                        dbFilter.setWorkflowName(in.getWorkflowName());
                    }
                }
            }

            if (hasPermission) {

                if (in.getLimit() == null) {
                    in.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }
                dbFilter.setLimit(in.getLimit());

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);

                ScrollableResults sr = null;
                try {
                    boolean profiler = false;
                    Instant profilerStart = Instant.now();
                    if (getTaskFromHistoryIdAndNode) {
                        sr = dbLayer.getJobsFromHistoryIdAndPosition(in.getHistoryIds().stream().filter(Objects::nonNull).filter(t -> t
                                .getHistoryId() != null).collect(Collectors.groupingBy(TaskIdOfOrder::getHistoryId, Collectors.mapping(
                                        TaskIdOfOrder::getPosition, Collectors.toSet()))));
                    } else {
                        sr = dbLayer.getJobs();
                    }
                    Instant profilerAfterSelect = Instant.now();

                    if (sr != null) {
                        Instant profilerFirstEntry = null;
                        int i = 0;
                        Map<String, Boolean> checkedControllers = new HashMap<>();
                        boolean isControllerIdEmpty = in.getControllerId().isEmpty();
                        Map<String, Boolean> checkedFolders = new HashMap<>();
                        while (sr.next()) {
                            i++;

                            DBItemHistoryOrderStep item = (DBItemHistoryOrderStep) sr.get(0);
                            if (profiler && i == 1) {
                                profilerFirstEntry = Instant.now();
                            }
                            if (isControllerIdEmpty && !getControllerPermissions(item, accessToken, checkedControllers)) {
                                continue;
                            }
                            if (!folderPermissionsAreChecked && !canAdd(item, permittedFolders, checkedFolders)) {
                                continue;
                            }
                            history.add(HistoryMapper.map2TaskHistoryItem(item));
                        }
                        logProfiler(profiler, i, profilerStart, profilerAfterSelect, profilerFirstEntry);
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (sr != null) {
                        sr.close();
                    }
                }
            }

            TaskHistory answer = new TaskHistory();
            answer.setDeliveryDate(Date.from(Instant.now()));
            answer.setHistory(history);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private boolean getControllerPermissions(DBItemHistoryOrderStep item, String accessToken, Map<String, Boolean> checkedControllers) {
        Boolean result = checkedControllers.get(item.getControllerId());
        if (result == null) {
            result = getControllerPermissions(item.getControllerId(), accessToken).getOrders().getView();
            checkedControllers.put(item.getControllerId(), result);
        }
        return result;
    }

    private boolean canAdd(DBItemHistoryOrderStep item, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        Boolean result = checkedFolders.get(item.getWorkflowFolder());
        if (result == null) {
            result = canAdd(item.getWorkflowPath(), permittedFolders);
            checkedFolders.put(item.getWorkflowFolder(), result);
        }
        return result;
    }

    private void logProfiler(boolean profiler, int i, Instant start, Instant afterSelect, Instant firstEntry) {
        if (!profiler) {
            return;
        }
        Instant end = Instant.now();
        String firstEntryDuration = "0s";
        if (firstEntry != null) {
            firstEntryDuration = SOSDate.getDuration(start, firstEntry);
        }
        LOGGER.info(String.format("[task][history][%s][total=%s][select=%s, first entry=%s]", i, SOSDate.getDuration(start, end), SOSDate.getDuration(
                start, afterSelect), firstEntryDuration));
    }

}
