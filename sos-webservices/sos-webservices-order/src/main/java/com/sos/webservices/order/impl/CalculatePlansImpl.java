package com.sos.webservices.order.impl;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlannedOrdersFilter;
import com.sos.webservices.order.initiator.OrderInitiatorRunner;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.resource.ICalculatePlansResource;

@Path("plan")
public class CalculatePlansImpl extends JOCResourceImpl implements ICalculatePlansResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatePlansImpl.class);
    private static final String API_CALL = "./plan/calculate";

    @Override
    public JOCDefaultResponse postCalculatePlans(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        LOGGER.debug("Calculate the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

            if (!"".equals(plannedOrdersFilter.getMasterUri())) {
                orderInitiatorSettings.setJobschedulerUrl(plannedOrdersFilter.getMasterUri());
            } else {
                orderInitiatorSettings.setJobschedulerUrl(this.dbItemInventoryInstance.getUri());
            }
            // Will be removed when reading templates from db
            orderInitiatorSettings.setOrderTemplatesDirectory(plannedOrdersFilter.getOrderTemplatesFolder());

            Date fromDate = JobSchedulerDate.getDateFrom(plannedOrdersFilter.getDateFrom(), plannedOrdersFilter.getTimeZone());
            Date toDate = JobSchedulerDate.getDateFrom(plannedOrdersFilter.getDateTo(), plannedOrdersFilter.getTimeZone());
            Calendar from = Calendar.getInstance();
            from.setTime(fromDate);
            Calendar to = Calendar.getInstance();
            to.setTime(toDate);

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings);
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
