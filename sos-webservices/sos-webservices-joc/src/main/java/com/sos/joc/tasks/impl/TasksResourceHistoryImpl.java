package com.sos.joc.tasks.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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
import com.sos.joc.classes.proxy.Proxies;
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
            
            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                                Collectors.toSet());
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
            boolean getTaskFromOrderHistory = false;
            Set<Folder> permittedFolders = addPermittedFolder(in.getFolders());
            boolean folderPermissionsAreChecked = false;

            HistoryFilter dbFilter = new HistoryFilter();
            dbFilter.setControllerIds(allowedControllers);

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
                        dbFilter.setJobs(in.getJobs().stream().filter(job -> job != null && canAdd(job.getWorkflowPath(), permittedFolders)).collect(
                                Collectors.groupingBy(job -> job.getWorkflowPath(), Collectors.mapping(JobPath::getJob, Collectors.toSet()))));
                        in.setRegex("");
                        folderPermissionsAreChecked = true;
                    } else {

                        if (SearchStringHelper.isDBWildcardSearch(in.getRegex())) {
                            dbFilter.setWorkflows(Arrays.asList(in.getRegex().split(",")));
                            in.setRegex("");
                        }

                        if (!in.getExcludeJobs().isEmpty()) {
                            dbFilter.setExcludedJobs(in.getExcludeJobs().stream().collect(Collectors.groupingBy(job -> job.getWorkflowPath(),
                                    Collectors.mapping(JobPath::getJob, Collectors.toSet()))));
                        }

                        if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
                            hasPermission = false;
                        } else if (withFolderFilter && permittedFolders != null && !permittedFolders.isEmpty()) {
                            dbFilter.setFolders(in.getFolders().stream().filter(folder -> folderIsPermitted(folder.getFolder(), permittedFolders))
                                    .collect(Collectors.toSet()));
                            folderPermissionsAreChecked = true;
                        }
                    }
                }
            }

            if (hasPermission) {

                if (in.getLimit() == null) {
                    in.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
                }
                dbFilter.setLimit(in.getLimit());

                if (dbFilter.getExecutedFrom() == null) {
                    dbFilter.setExecutedFrom(WebserviceConstants.HISTORY_DEFAULT_EXECUTED_FROM);
                }

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);

                ScrollableResults sr = null;
                try {
                    if (getTaskFromHistoryIdAndNode) {
                        sr = dbLayer.getJobsFromHistoryIdAndPosition(in.getHistoryIds().stream().filter(Objects::nonNull).filter(t -> t
                                .getHistoryId() != null).collect(Collectors.groupingBy(TaskIdOfOrder::getHistoryId, Collectors.mapping(
                                        TaskIdOfOrder::getPosition, Collectors.toSet()))));
                    } else if (getTaskFromOrderHistory) {
                        sr = dbLayer.getJobsFromOrder(in.getOrders().stream().filter(Objects::nonNull).filter(order -> canAdd(order.getWorkflowPath(),
                                permittedFolders)).collect(Collectors.groupingBy(order -> normalizePath(order.getWorkflowPath()), Collectors
                                        .groupingBy(o -> o.getOrderId() == null ? "" : o.getOrderId(), Collectors.mapping(OrderPath::getPosition,
                                                Collectors.toSet())))));
                        folderPermissionsAreChecked = true;
                    } else {
                        sr = dbLayer.getJobs();
                    }
                    
                    Predicate<String> predicate = null;
                    if (in.getRegex() != null && !in.getRegex().isEmpty()) {
                        predicate = Pattern.compile(in.getRegex()).asPredicate();
                    }

                    if (sr != null) {
                        // tmp outputs to check performance...
                        // int i = 0;
                        // int logStep = 1_000;
                        // String range = "task";
                        // LOGGER.info(String.format("[%s]start read and map ..", range));
                        while (sr.next()) {
                            // i++;

                            DBItemHistoryOrderStep step = (DBItemHistoryOrderStep) sr.get(0);
                            // if (i == 1) {
                            // LOGGER.info(String.format(" [%s][%s]first entry retrieved", range, i));
                            // }

                            if (in.getControllerId().isEmpty() && !getControllerPermissions(step.getControllerId(), accessToken).getOrders()
                                    .getView()) {
                                continue;
                            }
                            if (predicate != null && !predicate.test(step.getWorkflowPath() + "," + step.getJobName())) {
                                continue;
                            }
                            if (!folderPermissionsAreChecked && !canAdd(step.getWorkflowPath(), permittedFolders)) {
                                continue;
                            }
                            history.add(HistoryMapper.map2TaskHistoryItem(step));

                            // if (i == 1 || i % logStep == 0) {
                            // LOGGER.info(String.format(" [%s][%s]entries processed", range, i));
                            // }

                        }
                        // LOGGER.info(String.format("[%s][%s]end read and map", range, i));
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

}
