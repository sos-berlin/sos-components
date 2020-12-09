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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.ScheduleSource;
import com.sos.js7.order.initiator.ScheduleSourceDB;
import com.sos.js7.order.initiator.ScheduleSourceFile;
import com.sos.js7.order.initiator.ScheduleSourceList;
import com.sos.webservices.order.resource.IDailyPlanOrdersGenerateResource;

@Path("daily_plan")
public class DailyPlanOrdersGenerate extends JOCResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerate.class);
    private static final String API_CALL = "./daily_plan/orders/generate";

    @Override
    public JOCDefaultResponse postOrdersGenerate(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(dailyPlanOrderFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getDailyPlanDate());

            boolean withFolderFilter = dailyPlanOrderFilter.getFolders() != null && !dailyPlanOrderFilter.getFolders().isEmpty();
            boolean hasPermission = true;

            Set<Folder> folders = addPermittedFolder(dailyPlanOrderFilter.getFolders());
            dailyPlanOrderFilter.setFolders(new ArrayList<Folder>());
            for (Folder folder : folders) {
                dailyPlanOrderFilter.getFolders().add(folder);
            }

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(dailyPlanOrderFilter.getOverwrite());
            orderInitiatorSettings.setSubmit(dailyPlanOrderFilter.getWithSubmit());

            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            }

            if (hasPermission) {

                orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone",
                        Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
                orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin",
                        Globals.DEFAULT_PERIOD_DAILY_PLAN));

                OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);
                if (dailyPlanOrderFilter.getControllerIds() == null) {
                    dailyPlanOrderFilter.setControllerIds(new ArrayList<String>());
                    dailyPlanOrderFilter.getControllerIds().add(dailyPlanOrderFilter.getControllerId());
                } else {
                    if (!dailyPlanOrderFilter.getControllerIds().contains(dailyPlanOrderFilter.getControllerId())) {
                        dailyPlanOrderFilter.getControllerIds().add(dailyPlanOrderFilter.getControllerId());
                    }
                }

                for (String controllerId : dailyPlanOrderFilter.getControllerIds()) {
                    orderInitiatorSettings.setControllerId(controllerId);

                    ScheduleSource scheduleSource = null;
                    if (dailyPlanOrderFilter.getSchedulePaths() != null && dailyPlanOrderFilter.getSchedulePaths().size() > 0) {
                        scheduleSource = new ScheduleSourceList(controllerId, dailyPlanOrderFilter.getSchedulePaths());
                    } else {
                        if (dailyPlanOrderFilter.getSchedulesFolder() != null && !dailyPlanOrderFilter.getSchedulesFolder().isEmpty()) {
                            scheduleSource = new ScheduleSourceFile(dailyPlanOrderFilter.getSchedulesFolder());
                        } else {
                            scheduleSource = new ScheduleSourceDB(controllerId);
                        }
                    }

                    orderInitiatorRunner.readTemplates(scheduleSource);
                    orderInitiatorRunner.generateDailyPlan(dailyPlanOrderFilter.getDailyPlanDate(), dailyPlanOrderFilter.getWithSubmit());
                }
            }

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
