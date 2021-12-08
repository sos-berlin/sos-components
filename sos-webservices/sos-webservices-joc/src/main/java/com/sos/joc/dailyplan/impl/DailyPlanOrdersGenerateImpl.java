package com.sos.joc.dailyplan.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.common.FolderPath;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.dailyplan.resource.IDailyPlanOrdersGenerateResource;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.generate.GenerateRequest;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrdersGenerateImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerateImpl.class);

    @Override
    public JOCDefaultResponse postOrdersGenerate(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, GenerateRequest.class);
            GenerateRequest in = Globals.objectMapper.readValue(filterBytes, GenerateRequest.class);

            DBItemJocAuditLog auditLog = storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN);

            this.checkRequiredParameter("controllerId", in.getControllerId());
            this.checkRequiredParameter("dailyPlanDate", in.getDailyPlanDate());

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

            Set<Folder> scheduleFolders = null;
            Set<String> scheduleSingles = null;
            Set<Folder> workflowFolders = null;
            Set<String> workflowSingles = null;
            if (in.getSchedulePaths() != null) {
                scheduleFolders = FolderPath.filterByUniqueFolder(in.getSchedulePaths().getFolders());
                scheduleSingles = FolderPath.filterByFolders(scheduleFolders, in.getSchedulePaths().getSingles());
            }

            final Set<Folder> permittedFolders = addPermittedFolder(null);
            Map<String, Boolean> checkedFolders = new HashMap<>();
            if (in.getWorkflowPaths() != null) {
                workflowFolders = FolderPath.filterByUniqueFolder(in.getWorkflowPaths().getFolders());
                workflowSingles = FolderPath.filterByFolders(workflowFolders, in.getWorkflowPaths().getSingles());

                if (workflowFolders != null && workflowFolders.size() > 0) {
                    Set<String> toRemove = new HashSet<>();
                    for (Folder folder : workflowFolders) {
                        if (!isPermitted(folder, permittedFolders, checkedFolders)) {
                            toRemove.add(folder.getFolder());
                        }
                    }
                    if (toRemove.size() > 0) {
                        workflowFolders = workflowFolders.stream().filter(f -> !toRemove.contains(f.getFolder())).collect(Collectors.toSet());
                    }
                }
            }

            // log to service log file
            AJocClusterService.setLogger(ClusterServices.dailyplan.name());

            setSettings();

            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
            settings.setOverwrite(in.getOverwrite());
            settings.setSubmit(in.getWithSubmit());
            settings.setTimeZone(getSettings().getTimeZone());
            settings.setPeriodBegin(getSettings().getPeriodBegin());
            settings.setDailyPlanDate(DailyPlanHelper.getDailyPlanDateAsDate(SOSDate.getDate(in.getDailyPlanDate()).getTime()));
            settings.setSubmissionTime(new Date());

            DailyPlanRunner runner = new DailyPlanRunner(settings);
            Collection<Schedule> schedules = getSchedules(runner, controllerId, scheduleFolders, scheduleSingles, workflowFolders, workflowSingles,
                    permittedFolders, checkedFolders);

            Map<PlannedOrderKey, PlannedOrder> generatedOrders = runner.generateDailyPlan(StartupMode.manual, controllerId, schedules, in
                    .getDailyPlanDate(), in.getWithSubmit(), getJocError(), accessToken);
            AJocClusterService.removeLogger(ClusterServices.dailyplan.name());

            Set<AuditLogDetail> auditLogDetails = new HashSet<>();

            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), controllerId));
            }

            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditLog.getId()).thenAccept(either -> ProblemHelper.postExceptionEventIfExist(either,
                    accessToken, getJocError(), null));

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            AJocClusterService.removeLogger(ClusterServices.dailyplan.name());
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            AJocClusterService.removeLogger(ClusterServices.dailyplan.name());
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Collection<Schedule> getSchedules(DailyPlanRunner runner, String controllerId, Set<Folder> scheduleFolders, Set<String> scheduleSingles,
            Set<Folder> workflowFolders, Set<String> workflowSingles, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders)
            throws IOException, SOSHibernateException {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean hasSelectedSchedules = (scheduleFolders != null && scheduleFolders.size() > 0) || (scheduleSingles != null && scheduleSingles
                .size() > 0);
        boolean hasSelectedWorkflows = (workflowFolders != null && workflowFolders.size() > 0) || (workflowSingles != null && workflowSingles
                .size() > 0);
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerSchedules dbLayer = new DBLayerSchedules(session);

            // selected schedules
            List<DBItemInventoryReleasedConfiguration> scheduleItems = null;
            if (!hasSelectedSchedules && !hasSelectedWorkflows) {// ALL
                scheduleItems = dbLayer.getAllSchedules();
            } else {
                // selected schedules
                if (hasSelectedSchedules) {
                    scheduleItems = dbLayer.getSchedules(scheduleFolders, scheduleSingles);
                    if (isDebugEnabled && scheduleItems != null) {
                        List<String> l = scheduleItems.stream().map(item -> {
                            return item.getPath();
                        }).collect(Collectors.toList());
                        LOGGER.debug(String.format("[%s][getSchedules][%s]schedules=%s", IMPL_PATH, controllerId, String.join(",", l)));
                    }
                }
                // selected workflows
                if (hasSelectedWorkflows) {
                    List<String> workflowNames = dbLayer.getWorkflowNames(controllerId, workflowFolders, workflowSingles);
                    if (workflowNames != null && workflowNames.size() > 0) {
                        InventoryDBLayer dbLayerINV = new InventoryDBLayer(session);
                        List<DBItemInventoryReleasedConfiguration> result = dbLayerINV.getUsedReleasedSchedulesByWorkflowNames(workflowNames);
                        if ((result != null && result.size() > 0)) {
                            if (isDebugEnabled) {
                                List<String> l = result.stream().map(item -> {
                                    return item.getPath();
                                }).collect(Collectors.toList());
                                LOGGER.debug(String.format("[%s][getSchedules][%s][workflowNames=%s]schedules=%s", IMPL_PATH, controllerId, String
                                        .join(",", workflowNames), String.join(",", l)));
                            }

                            if (scheduleItems == null || scheduleItems.size() == 0) {
                                scheduleItems = result;
                            } else {
                                scheduleItems.addAll(result);
                                scheduleItems = scheduleItems.stream().filter(SOSCollection.distinctByKey(
                                        DBItemInventoryReleasedConfiguration::getId)).collect(Collectors.toList());
                            }
                        }
                    }
                }
            }
            session.close();
            session = null;

            if (scheduleItems == null || scheduleItems.size() == 0) {
                return new ArrayList<Schedule>();
            }

            return runner.convert(scheduleItems, permittedFolders, checkedFolders);
        } finally {
            Globals.disconnect(session);
        }
    }

    private boolean isPermitted(Folder folder, Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) {
        Boolean result = checkedFolders.get(folder.getFolder());
        if (result == null) {
            result = canAdd(folder.getFolder(), permittedFolders);
            checkedFolders.put(folder.getFolder(), result);
        }
        return result;
    }
}
