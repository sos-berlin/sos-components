package com.sos.webservices.order.impl;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlannedOrdersFilter;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.resource.IGenerateOrdersResource;

@Path("daily_plan")
public class GenerateOrder extends JOCResourceImpl implements IGenerateOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateOrder.class);
    private static final String API_CALL = "./daily_plan/orders/generate";

    @Override
    public JOCDefaultResponse postGenerateOrders(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        LOGGER.debug("Generate the orders for the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

            if (Globals.jocConfigurationProperties != null && Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + plannedOrdersFilter.getJobschedulerId()) != null) {
                orderInitiatorSettings.setJobschedulerUrl(Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + plannedOrdersFilter.getJobschedulerId()));
                LOGGER.debug("controller Url from properties: " + orderInitiatorSettings.getJobschedulerUrl());
            } else {
                orderInitiatorSettings.setJobschedulerUrl(this.dbItemInventoryInstance.getUri());
                LOGGER.debug("controller Url from DBItem: " + orderInitiatorSettings.getJobschedulerUrl());
            }
            

            // Will be removed when reading templates from db
            orderInitiatorSettings.setOrderTemplatesDirectory(plannedOrdersFilter.getOrderTemplatesFolder());

            Date fromDate = JobSchedulerDate.getDateFrom(plannedOrdersFilter.getDateFrom(), plannedOrdersFilter.getTimeZone());
            Date toDate = JobSchedulerDate.getDateFrom(plannedOrdersFilter.getDateTo(), plannedOrdersFilter.getTimeZone());
            Calendar from = Calendar.getInstance();
            from.setTime(fromDate);
            Calendar to = Calendar.getInstance();
            to.setTime(toDate);

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings,false);
            
            
            //TODO: templates should be in post body
            orderInitiatorRunner.readTemplates();
            
            while ((from.before(to)) || (from.get(Calendar.DATE) == to.get(Calendar.DATE))) {
                orderInitiatorRunner.calculatePlan(from);
                from.add(java.util.Calendar.DATE, 1);
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
