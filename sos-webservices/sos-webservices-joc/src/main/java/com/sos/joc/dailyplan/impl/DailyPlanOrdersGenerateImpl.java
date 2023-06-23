package com.sos.joc.dailyplan.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.common.FolderPath;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBBeanReleasedSchedule2DeployedWorkflow;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.dailyplan.resource.IDailyPlanOrdersGenerateResource;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.generate.GenerateRequest;
import com.sos.joc.model.dailyplan.generate.items.PathItem;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrdersGenerateImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerateImpl.class);

    @Override
    public JOCDefaultResponse postOrdersGenerate(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, GenerateRequest.class);
            GenerateRequest in = Globals.objectMapper.readValue(filterBytes, GenerateRequest.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), true);
            if (response != null) {
                return response;
            }
            if (!generateOrders(in, accessToken, true)) {
                return accessDeniedResponse();
            }

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            JocClusterServiceLogger.clearAllLoggers();
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            JocClusterServiceLogger.clearAllLoggers();
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public boolean generateOrders(GenerateRequest in, String accessToken, boolean withAudit) throws SOSInvalidDataException, SOSHibernateException,
            IOException, DBMissingDataException, DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException,
            DBOpenSessionException, ControllerConnectionResetException, ControllerConnectionRefusedException, SOSMissingDataException, ParseException,
            ExecutionException {

        String controllerId = in.getControllerId();
        if (!getControllerPermissions(controllerId, accessToken).getOrders().getCreate()) {
            return false;
        }

        Long auditLogId = withAudit ? storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN).getId() : 0L;

        if (folderPermissions == null) {
            folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        }
        folderPermissions.setSchedulerId(controllerId);

        Set<Folder> scheduleFolders = null;
        Set<String> scheduleSingles = null;
        Set<Folder> workflowFolders = null;
        Set<String> workflowSingles = null;
        if (in.getSchedulePaths() != null) {
            scheduleFolders = FolderPath.filterByUniqueFolder(in.getSchedulePaths().getFolders());
            // Plaster, um Schedulenamen statt Pfade zu akzeptieren
            scheduleSingles = FolderPath.filterByFolders(scheduleFolders, getScheduleSinglePaths(in.getSchedulePaths().getSingles()));
        }

        final Set<Folder> permittedFolders = addPermittedFolder(null);
        Map<String, Boolean> checkedFolders = new HashMap<>();
        if (in.getWorkflowPaths() != null) {
            workflowFolders = FolderPath.filterByUniqueFolder(in.getWorkflowPaths().getFolders());
            // Plaster, um Workflownamen statt Pfade zu akzeptieren
            workflowSingles = FolderPath.filterByFolders(workflowFolders, getWorflowSinglePaths(in.getWorkflowPaths().getSingles()));

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
        JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());

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
        Collection<DailyPlanSchedule> dailyPlanSchedules = getSchedules(runner, controllerId, scheduleFolders, scheduleSingles, workflowFolders,
                workflowSingles, permittedFolders, checkedFolders);

        Map<PlannedOrderKey, PlannedOrder> generatedOrders = runner.generateDailyPlan(StartupMode.manual, controllerId, dailyPlanSchedules, in
                .getDailyPlanDate(), in.getWithSubmit(), getJocError(), accessToken);
        JocClusterServiceLogger.clearAllLoggers();

        if (withAudit) {
            Set<AuditLogDetail> auditLogDetails = new HashSet<>();

            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), controllerId));
            }

            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditLogId).thenAccept(either -> ProblemHelper.postExceptionEventIfExist(either,
                    accessToken, getJocError(), null));
        }

        return true;
    }
    
    private List<String> getScheduleSinglePaths(List<String> scheduleSingles) throws SOSHibernateException {
        if (scheduleSingles == null) {
            return null;
        }
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            return dbLayer.getReleasedConfigurationPaths(scheduleSingles.stream().map(JocInventory::pathToName).distinct().collect(Collectors.toList()), ConfigurationType.SCHEDULE);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private List<String> getWorflowSinglePaths(List<String> workflowSingles) throws SOSHibernateException {
        if (workflowSingles == null) {
            return null;
        }
        return workflowSingles.stream().map(JocInventory::pathToName).map(WorkflowPaths::getPath).filter(Objects::nonNull).distinct().collect(
                Collectors.toList());
    }

    private Collection<DailyPlanSchedule> getSchedules(DailyPlanRunner runner, String controllerId, Set<Folder> scheduleFolders,
            Set<String> scheduleSingles, Set<Folder> workflowFolders, Set<String> workflowSingles, Set<Folder> permittedFolders,
            Map<String, Boolean> checkedFolders) throws IOException, SOSHibernateException {

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
            List<DBBeanReleasedSchedule2DeployedWorkflow> scheduleItems = null;
            if (!hasSelectedSchedules && !hasSelectedWorkflows) {// ALL
                scheduleItems = dbLayer.getReleasedSchedule2DeployedWorkflows(controllerId, null);
            } else {
                // selected schedules
                if (hasSelectedSchedules) {
                    scheduleItems = dbLayer.getReleasedSchedule2DeployedWorkflows(controllerId, scheduleFolders, scheduleSingles, true);
                    if (isDebugEnabled && scheduleItems != null) {
                        List<String> l = scheduleItems.stream().map(item -> {
                            return item.getSchedulePath();
                        }).distinct().collect(Collectors.toList());
                        LOGGER.debug(String.format("[%s][getSchedules][%s]schedules=%s", IMPL_PATH, controllerId, String.join(",", l)));
                    }
                }
                // selected workflows
                if (hasSelectedWorkflows) {
                    List<DBBeanReleasedSchedule2DeployedWorkflow> workflowItems = dbLayer.getReleasedSchedule2DeployedWorkflows(controllerId,
                            workflowFolders, workflowSingles, false);
                    if (workflowItems != null && workflowItems.size() > 0) {
                        if (scheduleItems == null || scheduleItems.size() == 0) {
                            scheduleItems = workflowItems;
                        } else {
                            scheduleItems.addAll(workflowItems);
                        }
                    }
                }
            }
            session.close();
            session = null;

            if (scheduleItems == null || scheduleItems.size() == 0) {
                return new ArrayList<DailyPlanSchedule>();
            }

            return runner.convert(scheduleItems, permittedFolders, checkedFolders, true);
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

    public boolean generateOrders(List<GenerateRequest> requests, String accessToken, boolean withAudit) throws DBMissingDataException,
            DBConnectionRefusedException, DBInvalidDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, SOSInvalidDataException, SOSHibernateException,
            SOSMissingDataException, IOException, ParseException, ExecutionException {
        boolean successful = true;
        for (GenerateRequest req : requests) {
            if (!generateOrders(req, accessToken, withAudit)) {
                successful = false;
            }
        }
        return successful;
    }

    public List<GenerateRequest> getGenerateRequests(String date, List<String> workflowPaths, List<String> schedulePaths, String controllerId)
            throws ParseException {
        return getGenerateRequests(date, workflowPaths, schedulePaths, controllerId, true);
    }

    public List<GenerateRequest> getGenerateRequests(String date, List<String> workflowPaths, List<String> schedulePaths, String controllerId,
            Boolean withSubmit) throws ParseException {
        setSettings();
        int planDaysAhead = getSettings().getDayAheadPlan();
        int submitDaysAhead = getSettings().getDayAheadSubmit();
        List<GenerateRequest> generateRequests = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Instant now = Instant.now();
        Instant instant = "now".equals(date) ? now : sdf.parse(date).toInstant();
        if (instant.isBefore(now)) {
            instant = now;
        }

        PathItem workflowsPathItem = new PathItem();
        PathItem schedulesPathItem = new PathItem();
        workflowsPathItem.setSingles(workflowPaths);
        schedulesPathItem.setSingles(schedulePaths);

        for (int i = 0; i < planDaysAhead; i++) {
            if (now.isBefore(instant)) {
                continue;
            }
            GenerateRequest req = new GenerateRequest();
            req.setControllerId(controllerId);
            if (withSubmit) {
                req.setWithSubmit(i < submitDaysAhead);
            } else {
                req.setWithSubmit(false);
            }
            req.setOverwrite(true);
            req.setDailyPlanDate(sdf.format(Date.from(now)));
            req.setWorkflowPaths(workflowsPathItem);
            req.setSchedulePaths(schedulesPathItem);
            generateRequests.add(req);
            now = now.plusSeconds(TimeUnit.DAYS.toSeconds(1));
        }
        return generateRequests;
    }
}
