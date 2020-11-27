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
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.OrderPath;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.job.TaskIdOfOrder;
import com.sos.joc.tasks.resource.ITasksResourceHistory;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.TASKS)
public class TasksResourceHistoryImpl extends JOCResourceImpl implements ITasksResourceHistory {

    @Override
    public JOCDefaultResponse postTasksHistory(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, JobsFilter.class);
            JobsFilter in = Globals.objectMapper.readValue(inBytes, JobsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(in.getControllerId(), getPermissonsJocCockpit(in.getControllerId(), accessToken)
                    .getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<TaskHistoryItem> listOfHistory = new ArrayList<TaskHistoryItem>();
            boolean withFolderFilter = in.getFolders() != null && !in.getFolders().isEmpty();
            boolean hasPermission = true;
            boolean getTaskFromHistoryIdAndNode = false;
            boolean getTaskFromOrderHistory = false;
            Set<Folder> folders = addPermittedFolder(in.getFolders());

            HistoryFilter dbFilter = new HistoryFilter();
            dbFilter.setSchedulerId(in.getControllerId());

            if (in.getTaskIds() != null && !in.getTaskIds().isEmpty()) {
                dbFilter.setHistoryIds(in.getTaskIds());
            } else {
                if (in.getHistoryIds() != null && !in.getHistoryIds().isEmpty()) {
                    getTaskFromHistoryIdAndNode = true;
                } else if (in.getOrders() != null && !in.getOrders().isEmpty()) {
                    getTaskFromOrderHistory = true;
                } else {

                    if (in.getDateFrom() != null) {
                        dbFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()));
                    }
                    if (in.getDateTo() != null) {
                        dbFilter.setExecutedTo(JobSchedulerDate.getDateTo(in.getDateTo(), in.getTimeZone()));
                    }

                    if (in.getHistoryStates() != null && !in.getHistoryStates().isEmpty()) {
                        dbFilter.setState(in.getHistoryStates());
                    }

                    if (in.getCriticalities() != null && !in.getCriticalities().isEmpty()) {
                        dbFilter.setCriticalities(in.getCriticalities());
                    }

                    if (in.getJobs() != null && !in.getJobs().isEmpty()) {
                        final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        dbFilter.setJobs(in.getJobs().stream().filter(job -> job != null && canAdd(job.getWorkflowPath(), permittedFolders)).collect(
                                Collectors.groupingBy(job -> normalizePath(job.getWorkflowPath()), Collectors.mapping(JobPath::getJob, Collectors
                                        .toSet()))));
                        in.setRegex("");

                    } else {

                        if (SearchStringHelper.isDBWildcardSearch(in.getRegex())) {
                            dbFilter.setWorkflows(Arrays.asList(in.getRegex().split(",")));
                            in.setRegex("");
                        }

                        if (!in.getExcludeJobs().isEmpty()) {
                            dbFilter.setExcludedJobs(in.getExcludeJobs().stream().collect(Collectors.groupingBy(job -> normalizePath(job
                                    .getWorkflowPath()), Collectors.mapping(JobPath::getJob, Collectors.toSet()))));
                        }

                        if (withFolderFilter && (folders == null || folders.isEmpty())) {
                            hasPermission = false;
                        } else if (folders != null && !folders.isEmpty()) {
                            dbFilter.setFolders(folders.stream().map(folder -> {
                                folder.setFolder(normalizeFolder(folder.getFolder()));
                                return folder;
                            }).collect(Collectors.toSet()));
                        }
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
                    if (getTaskFromHistoryIdAndNode) {
                        sr = dbLayer.getJobsFromHistoryIdAndPosition(in.getHistoryIds().stream().filter(Objects::nonNull).filter(t -> t
                                .getHistoryId() != null).collect(Collectors.groupingBy(TaskIdOfOrder::getHistoryId, Collectors.mapping(
                                        TaskIdOfOrder::getPosition, Collectors.toSet()))));
                    } else if (getTaskFromOrderHistory) {
                        final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        sr = dbLayer.getJobsFromOrder(in.getOrders().stream().filter(Objects::nonNull).filter(order -> canAdd(order.getWorkflowPath(),
                                permittedFolders)).collect(Collectors.groupingBy(order -> normalizePath(order.getWorkflowPath()), Collectors
                                        .groupingBy(o -> o.getOrderId() == null ? "" : o.getOrderId(), Collectors.mapping(OrderPath::getPosition,
                                                Collectors.toSet())))));
                    } else {
                        sr = dbLayer.getJobs();
                    }

                    Matcher matcher = null;
                    if (in.getRegex() != null && !in.getRegex().isEmpty()) {
                        matcher = Pattern.compile(in.getRegex()).matcher("");
                    }

                    if (sr != null) {
                        while (sr.next()) {
                            DBItemHistoryOrderStep step = (DBItemHistoryOrderStep) sr.get(0);

                            if (in.getControllerId().isEmpty() && !getPermissonsJocCockpit(step.getJobSchedulerId(), accessToken).getHistory()
                                    .getView().isStatus()) {
                                continue;
                            }
                            if (matcher != null && !matcher.reset(step.getWorkflowPath() + "," + step.getJobName()).find()) {
                                continue;
                            }
                            listOfHistory.add(HistoryMapper.map2TaskHistoryItem(step));
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
            Globals.disconnect(session);
        }
    }

}
