package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
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
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.schema.JsonValidator;
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

            if (dailyPlanOrderSelector.getControllerIds() == null) {
                dailyPlanOrderSelector.setControllerIds(new ArrayList<String>());
                dailyPlanOrderSelector.getControllerIds().add(dailyPlanOrderSelector.getControllerId());
            } else {
                if (!dailyPlanOrderSelector.getControllerIds().contains(dailyPlanOrderSelector.getControllerId())) {
                    dailyPlanOrderSelector.getControllerIds().add(dailyPlanOrderSelector.getControllerId());
                }
            }

            Set<String> allowedControllers = Collections.emptySet();
            allowedControllers = dailyPlanOrderSelector.getControllerIds().stream().filter(availableController -> getControllerPermissions(
                    availableController, accessToken).getOrders().getCreate()).collect(Collectors.toSet());
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

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            OrderInitiatorGlobals.dailyPlanDate = DailyPlanHelper.getDailyPlanDateAsDate(DailyPlanHelper.stringAsDate(dailyPlanOrderSelector
                    .getDailyPlanDate()).getTime());
            OrderInitiatorGlobals.submissionTime = new Date();

            boolean withFolderSelector = (dailyPlanOrderSelector.getSelector().getFolders() != null && !dailyPlanOrderSelector.getSelector()
                    .getFolders().isEmpty());

            Set<Folder> inFolders = new HashSet<Folder>();
            if (dailyPlanOrderSelector.getSelector().getSchedulePaths() != null) {
                for (String schedulePath : dailyPlanOrderSelector.getSelector().getSchedulePaths()) {
                    Folder folder = new Folder();
                    folder.setFolder(schedulePath);
                    folder.setRecursive(false);
                    inFolders.add(folder);
                }
            }

            for (String controllerId : allowedControllers) {
                DBItemJocAuditLog dbItemJocAuditLog = storeAuditLog(dailyPlanOrderSelector.getAuditLog(), dailyPlanOrderSelector.getControllerId(),
                        CategoryType.DAILYPLAN);
                OrderInitiatorGlobals.orderInitiatorSettings.setAuditLogId(dbItemJocAuditLog.getId());

                boolean hasPermission = true;
                folderPermissions.setSchedulerId(controllerId);
                if (withFolderSelector) {
                    Set<Folder> permittedFolders = addPermittedFolder(inFolders);
                    if (withFolderSelector && (permittedFolders == null || permittedFolders.isEmpty())) {
                        hasPermission = false;
                    }
                    dailyPlanOrderSelector.getSelector().getFolders().clear();
                    for (Folder permittedFolder : permittedFolders) {
                        dailyPlanOrderSelector.getSelector().getFolders().add(permittedFolder);
                    }
                }

                if (hasPermission) {
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
