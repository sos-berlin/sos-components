package com.sos.webservices.order.impl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.ScheduleSource;
import com.sos.js7.order.initiator.ScheduleSourceDB;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.FolderPermissionEvaluator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanOrdersGenerateResource;

@Path("daily_plan")
public class DailyPlanOrdersGenerateImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerateImpl.class);
    private static final String API_CALL = "./daily_plan/orders/generate";

    @Override
    public JOCDefaultResponse postOrdersGenerate(String accessToken, byte[] filterBytes) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderSelector.class);
            DailyPlanOrderSelector dailyPlanOrderSelector = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderSelector.class);

            if (dailyPlanOrderSelector.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                dailyPlanOrderSelector.setSelector(new DailyPlanOrderSelectorDef());
                dailyPlanOrderSelector.getSelector().setFolders(new ArrayList<Folder>());
                dailyPlanOrderSelector.getSelector().getFolders().add(root);
            }

            if (dailyPlanOrderSelector.getSelector().getFolders() == null) {
                dailyPlanOrderSelector.getSelector().setFolders(new ArrayList<Folder>());
            }
            if (dailyPlanOrderSelector.getSelector().getScheduleNames() == null) {
                dailyPlanOrderSelector.getSelector().setScheduleNames(new ArrayList<String>());
            }
            if (dailyPlanOrderSelector.getSelector().getSchedulePaths() == null) {
                dailyPlanOrderSelector.getSelector().setSchedulePaths(new ArrayList<String>());
            }
            if (dailyPlanOrderSelector.getSelector().getWorkflowNames() == null) {
                dailyPlanOrderSelector.getSelector().setWorkflowNames(new ArrayList<String>());
            }
            if (dailyPlanOrderSelector.getSelector().getWorkflowPaths() == null) {
                dailyPlanOrderSelector.getSelector().setWorkflowPaths(new ArrayList<String>());
            }

            Set<String> allowedControllers = getAllowedControllersOrdersView(dailyPlanOrderSelector.getControllerId(), dailyPlanOrderSelector
                    .getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getOrders().getView()).collect(Collectors.toSet());
            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderSelector.getDailyPlanDate());
            setSettings();

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(dailyPlanOrderSelector.getOverwrite());
            orderInitiatorSettings.setSubmit(dailyPlanOrderSelector.getWithSubmit());

            orderInitiatorSettings.setTimeZone(settings.getTimeZone());
            orderInitiatorSettings.setPeriodBegin(settings.getPeriodBegin());

            orderInitiatorSettings.setDailyPlanDate(DailyPlanHelper.getDailyPlanDateAsDate(DailyPlanHelper.stringAsDate(dailyPlanOrderSelector
                    .getDailyPlanDate()).getTime()));
            orderInitiatorSettings.setSubmissionTime(new Date());

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            FolderPermissionEvaluator folderPermissionEvaluator = new FolderPermissionEvaluator();
            folderPermissionEvaluator.setListOfScheduleFolders(dailyPlanOrderSelector.getSelector().getFolders());
            folderPermissionEvaluator.setListOfScheduleNames(dailyPlanOrderSelector.getSelector().getScheduleNames());
            folderPermissionEvaluator.setListOfSchedulePaths(dailyPlanOrderSelector.getSelector().getSchedulePaths());
            folderPermissionEvaluator.setListOfWorkflowNames(dailyPlanOrderSelector.getSelector().getWorkflowNames());
            folderPermissionEvaluator.setListOfWorkflowPaths(dailyPlanOrderSelector.getSelector().getWorkflowPaths());

            for (String controllerId : allowedControllers) {
                DBItemJocAuditLog dbItemJocAuditLog = storeAuditLog(dailyPlanOrderSelector.getAuditLog(), dailyPlanOrderSelector.getControllerId(),
                        CategoryType.DAILYPLAN);
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

                folderPermissions.setSchedulerId(controllerId);
                folderPermissionEvaluator.getPermittedNames(folderPermissions, controllerId, filter);

                folderPermissions.setSchedulerId(controllerId);

                if (folderPermissionEvaluator.isHasPermission()) {

                    dailyPlanOrderSelector.getSelector().getFolders().clear();
                    dailyPlanOrderSelector.getSelector().setFolders(new ArrayList<Folder>());
                    if (filter.getSetOfScheduleFolders() != null) {
                        dailyPlanOrderSelector.getSelector().getFolders().addAll(filter.getSetOfScheduleFolders());
                    }
                    dailyPlanOrderSelector.getSelector().getSchedulePaths().clear();
                    dailyPlanOrderSelector.getSelector().getWorkflowPaths().clear();
                    dailyPlanOrderSelector.getSelector().getScheduleNames().clear();
                    dailyPlanOrderSelector.getSelector().getWorkflowNames().clear();
                    if (folderPermissionEvaluator.getListOfPermittedScheduleNames() != null) {
                        dailyPlanOrderSelector.getSelector().getScheduleNames().addAll(folderPermissionEvaluator.getListOfPermittedScheduleNames());
                    }
                    if (folderPermissionEvaluator.getListOfPermittedWorkflowNames() != null) {
                        dailyPlanOrderSelector.getSelector().getWorkflowNames().addAll(folderPermissionEvaluator.getListOfPermittedWorkflowNames());
                    }

                    ScheduleSource scheduleSource = null;
                    scheduleSource = new ScheduleSourceDB(dailyPlanOrderSelector);

                    orderInitiatorRunner.readSchedules(scheduleSource);
                    orderInitiatorRunner.generateDailyPlan(controllerId, getJocError(), accessToken, dailyPlanOrderSelector.getDailyPlanDate(),
                            dailyPlanOrderSelector.getWithSubmit());
                }
            }
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (

        JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
