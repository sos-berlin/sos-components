package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DailyPlanAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.ScheduleSource;
import com.sos.js7.order.initiator.ScheduleSourceDB;
import com.sos.webservices.order.resource.IDailyPlanOrdersGenerateResource;

@Path("daily_plan")
public class DailyPlanOrdersGenerateImpl extends JOCResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerateImpl.class);
    private static final String API_CALL = "./daily_plan/orders/generate";

    @Override
    public JOCDefaultResponse postOrdersGenerate(String xAccessToken, DailyPlanOrderSelector dailyPlanOrderSelector) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderSelector, xAccessToken, dailyPlanOrderSelector.getControllerId(),
                    getPermissonsJocCockpit(getControllerId(xAccessToken, dailyPlanOrderSelector.getControllerId()), xAccessToken).getDailyPlan()
                            .getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderSelector.getDailyPlanDate());

            if (dailyPlanOrderSelector.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                dailyPlanOrderSelector.setSelector(new DailyPlanOrderSelectorDef());
                dailyPlanOrderSelector.getSelector().setFolders(new ArrayList<Folder>());
                dailyPlanOrderSelector.getSelector().getFolders().add(root);
            }
            
            Set<Folder> folders = addPermittedFolder(dailyPlanOrderSelector.getSelector().getFolders());
            dailyPlanOrderSelector.getSelector().setFolders(new ArrayList<Folder>());
            for (Folder folder : folders) {
                dailyPlanOrderSelector.getSelector().getFolders().add(folder);
            }

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(dailyPlanOrderSelector.getOverwrite());
            orderInitiatorSettings.setSubmit(dailyPlanOrderSelector.getWithSubmit());

            orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone", Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
            orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin",
                    Globals.DEFAULT_PERIOD_DAILY_PLAN));

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);
            if (dailyPlanOrderSelector.getControllerIds() == null) {
                dailyPlanOrderSelector.setControllerIds(new ArrayList<String>());
                dailyPlanOrderSelector.getControllerIds().add(dailyPlanOrderSelector.getControllerId());
            } else {
                if (!dailyPlanOrderSelector.getControllerIds().contains(dailyPlanOrderSelector.getControllerId())) {
                    dailyPlanOrderSelector.getControllerIds().add(dailyPlanOrderSelector.getControllerId());
                }
            }

            for (String controllerId : dailyPlanOrderSelector.getControllerIds()) {
                orderInitiatorSettings.setControllerId(controllerId);

                ScheduleSource scheduleSource = null;
                scheduleSource = new ScheduleSourceDB(dailyPlanOrderSelector);
                orderInitiatorRunner.readSchedules(scheduleSource);
                orderInitiatorRunner.generateDailyPlan(dailyPlanOrderSelector.getDailyPlanDate(), dailyPlanOrderSelector.getWithSubmit());

                DailyPlanAudit orderAudit = new DailyPlanAudit(controllerId, dailyPlanOrderSelector.getAuditLog());
                logAuditMessage(orderAudit);
                storeAuditLogEntry(orderAudit);

            }

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            //LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            //e.printStackTrace();
            //LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
