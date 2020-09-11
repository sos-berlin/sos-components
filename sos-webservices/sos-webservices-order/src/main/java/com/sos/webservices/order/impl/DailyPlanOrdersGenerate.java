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
import com.sos.js7.order.initiator.OrderTemplateSource;
import com.sos.js7.order.initiator.OrderTemplateSourceDB;
import com.sos.js7.order.initiator.OrderTemplateSourceFile;
import com.sos.js7.order.initiator.OrderTemplateSourceList;
import com.sos.js7.order.initiator.classes.OrderInitiatorGlobals;
import com.sos.webservices.order.resource.IDailyPlanOrdersGenerateResource;

@Path("daily_plan")
public class DailyPlanOrdersGenerate extends JOCResourceImpl implements IDailyPlanOrdersGenerateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersGenerate.class);
    private static final String API_CALL = "./daily_plan/orders/generate";

    @Override
    public JOCDefaultResponse postOrdersGenerate(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getControllerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("dailyPlanDate", plannedOrdersFilter.getDailyPlanDate());

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setControllerId(plannedOrdersFilter.getControllerId());

            LOGGER.debug("controller Url from DBItem: " + orderInitiatorSettings.getControllerId());

            orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone"));
            orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin"));

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            OrderTemplateSource orderTemplateSource = null;
            if (plannedOrdersFilter.getOrderTemplates() != null && plannedOrdersFilter.getOrderTemplates().size() > 0) {
                orderTemplateSource = new OrderTemplateSourceList(plannedOrdersFilter.getControllerId(), plannedOrdersFilter.getOrderTemplates());
            } else {
                if (plannedOrdersFilter.getOrderTemplatesFolder() != null && !plannedOrdersFilter.getOrderTemplatesFolder().isEmpty()) {
                    orderTemplateSource = new OrderTemplateSourceFile(plannedOrdersFilter.getOrderTemplatesFolder());
                } else {
                    orderTemplateSource = new OrderTemplateSourceDB(plannedOrdersFilter.getControllerId());
                }
            }

            orderInitiatorRunner.readTemplates(orderTemplateSource);
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
