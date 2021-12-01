package com.sos.joc.dailyplan.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.FolderPermissionEvaluator;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.common.ScheduleSourceDB;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanOrdersGenerateResource;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrdersGenerateImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerateImpl.class);

    @Override
    public JOCDefaultResponse postOrdersGenerate(String accessToken, byte[] filterBytes) {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderSelector.class);
            DailyPlanOrderSelector in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderSelector.class);

            DBItemJocAuditLog auditLog = storeAuditLog(in.getAuditLog(), CategoryType.DAILYPLAN);
            if (in.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                in.setSelector(new DailyPlanOrderSelectorDef());
                in.getSelector().setFolders(new ArrayList<Folder>());
                in.getSelector().getFolders().add(root);
            }
            // TODO @uwe: why lists with names and paths. We wanted only paths in which include also names
            if (in.getSelector().getFolders() == null) {
                in.getSelector().setFolders(new ArrayList<Folder>());
            }
            if (in.getSelector().getSchedulePaths() == null) {
                in.getSelector().setSchedulePaths(new ArrayList<String>());
            }
            if (in.getSelector().getWorkflowPaths() == null) {
                in.getSelector().setWorkflowPaths(new ArrayList<String>());
            }

            Set<String> allowedControllers = getAllowedControllersOrdersView(in.getControllerId(), in.getControllerIds(), accessToken).stream()
                    .filter(availableController -> getControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                            Collectors.toSet());
            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }

            this.checkRequiredParameter("dailyPlanDate", in.getDailyPlanDate());
            setSettings();

            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            settings.setOverwrite(in.getOverwrite());
            settings.setSubmit(in.getWithSubmit());
            settings.setTimeZone(settings.getTimeZone());
            settings.setPeriodBegin(settings.getPeriodBegin());
            settings.setDailyPlanDate(DailyPlanHelper.getDailyPlanDateAsDate(DailyPlanHelper.stringAsDate(in.getDailyPlanDate()).getTime()));
            settings.setSubmissionTime(new Date());

            DailyPlanRunner runner = new DailyPlanRunner(settings, false);

            FolderPermissionEvaluator evaluator = new FolderPermissionEvaluator();
            evaluator.setScheduleFolders(in.getSelector().getFolders());
            evaluator.setSchedulePaths(in.getSelector().getSchedulePaths());
            evaluator.setWorkflowPaths(in.getSelector().getWorkflowPaths());

            Set<AuditLogDetail> auditLogDetails = new HashSet<>();

            for (String controllerId : allowedControllers) {
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

                folderPermissions.setSchedulerId(controllerId);
                evaluator.getPermittedNames(folderPermissions, controllerId, filter);

                if (evaluator.isHasPermission()) {
                    in.getSelector().getFolders().clear();
                    in.getSelector().setFolders(new ArrayList<Folder>());
                    if (filter.getScheduleFolders() != null) {
                        in.getSelector().getFolders().addAll(filter.getScheduleFolders());
                    }
                    in.getSelector().getSchedulePaths().clear();
                    in.getSelector().getWorkflowPaths().clear();
                    if (evaluator.getPermittedScheduleNames() != null) {
                        in.getSelector().getSchedulePaths().addAll(evaluator.getPermittedScheduleNames());
                    }
                    if (evaluator.getPermittedWorkflowNames() != null) {
                        in.getSelector().getWorkflowPaths().addAll(evaluator.getPermittedWorkflowNames());
                    }

                    runner.readSchedules(new ScheduleSourceDB(in));

                    Map<PlannedOrderKey, PlannedOrder> generatedOrders = runner.generateDailyPlan(controllerId, getJocError(), accessToken, in
                            .getDailyPlanDate(), in.getWithSubmit());
                    for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                        auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(),
                                controllerId));
                    }

                }
            }

            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditLog.getId()).thenAccept(either -> ProblemHelper.postExceptionEventIfExist(either,
                    accessToken, getJocError(), null));

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
