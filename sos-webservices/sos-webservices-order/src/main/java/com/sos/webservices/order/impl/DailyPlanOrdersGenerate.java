package com.sos.webservices.order.impl;

import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.OrderTemplateSource;
import com.sos.js7.order.initiator.OrderTemplateSourceDB;
import com.sos.js7.order.initiator.OrderTemplateSourceFile;
import com.sos.js7.order.initiator.OrderTemplateSourceList;
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

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setControllerId(dailyPlanOrderFilter.getControllerId());
            orderInitiatorSettings.setOverwrite(dailyPlanOrderFilter.getOverwrite());
            orderInitiatorSettings.setSubmit(dailyPlanOrderFilter.getWithSubmit());
  
            LOGGER.debug("controller Url from DBItem: " + orderInitiatorSettings.getControllerId());

            orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone",Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
            orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin",Globals.DEFAULT_PERIOD_DAILY_PLAN));

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            OrderTemplateSource orderTemplateSource = null;
            if (dailyPlanOrderFilter.getOrderTemplates() != null && dailyPlanOrderFilter.getOrderTemplates().size() > 0) {
                orderTemplateSource = new OrderTemplateSourceList(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getOrderTemplates());
            } else {
                if (dailyPlanOrderFilter.getOrderTemplatesFolder() != null && !dailyPlanOrderFilter.getOrderTemplatesFolder().isEmpty()) {
                    orderTemplateSource = new OrderTemplateSourceFile(dailyPlanOrderFilter.getOrderTemplatesFolder());
                } else {
                    orderTemplateSource = new OrderTemplateSourceDB(dailyPlanOrderFilter.getControllerId());
                }
            }

            orderInitiatorRunner.readTemplates(orderTemplateSource);
            orderInitiatorRunner.generateDailyPlan(dailyPlanOrderFilter.getDailyPlanDate());

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
