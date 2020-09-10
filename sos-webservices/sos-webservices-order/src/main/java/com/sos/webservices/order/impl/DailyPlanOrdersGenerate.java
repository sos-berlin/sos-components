package com.sos.webservices.order.impl;

import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.PlannedOrdersFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.resource.IDailyPlanOrdersGenerateResource;

@Path("daily_plan")
public class DailyPlanOrdersGenerate extends JOCResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerate.class);
    private static final String API_CALL = "./daily_plan/orders/generate";

    @Override
    public JOCDefaultResponse postOrdersGenerate(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("dailyPlanDate", plannedOrdersFilter.getDailyPlanDate());

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setControllerId(plannedOrdersFilter.getJobschedulerId());

            LOGGER.debug("controller Url from DBItem: " + orderInitiatorSettings.getControllerId());

            orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone"));
            orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin"));

            // Will be removed when reading templates from db
            orderInitiatorSettings.setOrderTemplatesDirectory(plannedOrdersFilter.getOrderTemplatesFolder());

           /*  
     
            Wenn orderTemplates angegeben ist, dann orderInitiator setOrderTemplates.
            Sonst aus db lesen.
            Wenn orderTemplatesFolder angegeben ist, aus folder lesen, dann orderInitiator setOrderTemplates.
                  */
            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);
 
            // TODO: templates should be in post body
            orderInitiatorRunner.readTemplates();

            orderInitiatorRunner.generateDailyPlan(plannedOrdersFilter.getDailyPlanDate());

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
