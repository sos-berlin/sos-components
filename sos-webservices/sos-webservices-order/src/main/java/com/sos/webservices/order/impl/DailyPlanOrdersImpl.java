package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.dailyplan.PlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanOrdersResource;

@Path("daily_plan")
public class DailyPlanOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/orders";

    @Override
    public JOCDefaultResponse postDailyPlan(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession session = null;
        try {

            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderSelector.class);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);

            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            Set<String> allowedControllers = getAllowedControllersOrdersView(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getFilter()
                    .getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getOrders().getView()).collect(Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            setSettings();
            LOGGER.debug("Reading the daily plan for day " + dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);

            ArrayList<PlannedOrderItem> result = new ArrayList<PlannedOrderItem>();

            for (String controllerId : allowedControllers) {
                FilterDailyPlannedOrders filter = getOrderFilter(controllerId, dailyPlanOrderFilter, true);

                List<DBItemDailyPlanWithHistory> orders = getOrders(session, controllerId, filter, true);
                addOrders(session, filter, controllerId, dailyPlanOrderFilter, orders, result, true);
            }

            PlannedOrders answer = new PlannedOrders();
            answer.setPlannedOrderItems(result);
            answer.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(answer);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}
