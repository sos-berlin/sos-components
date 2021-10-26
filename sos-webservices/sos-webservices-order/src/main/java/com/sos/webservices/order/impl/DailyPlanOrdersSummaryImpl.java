package com.sos.webservices.order.impl;

import java.util.ArrayList;
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
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.DailyPlanOrdersSummary;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanOrdersSummaryResource;

@Path("daily_plan")
public class DailyPlanOrdersSummaryImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersSummaryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersSummaryImpl.class);
    private static final String API_CALL = "./daily_plan/orders/summary";

    @Override
    public JOCDefaultResponse postDailyPlanOrdersSummary(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession session = null;
        try {

            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderSelector.class);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);
            Set<String> allowedControllers = getAllowedControllersOrdersView(dailyPlanOrderFilter.getControllerId(), dailyPlanOrderFilter.getFilter()
                    .getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getOrders().getView()).collect(Collectors.toSet());
            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());
            setSettings();
            LOGGER.debug("Reading the daily plan for day " + dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);

            DailyPlanOrdersSummary answer = new DailyPlanOrdersSummary();
            answer.setFinished(0);
            answer.setSubmitted(0);
            answer.setSubmittedLate(0);
            answer.setPlanned(0);
            answer.setPlannedLate(0);

            for (String controllerId : allowedControllers) {
                FilterDailyPlannedOrders filter = getOrderFilter(controllerId, dailyPlanOrderFilter, true);

                ArrayList<PlannedOrderItem> plannedOrders = new ArrayList<PlannedOrderItem>();
                List<DBItemDailyPlanWithHistory> orders = getOrders(session, controllerId, filter, false);
                addOrders(session, filter, controllerId, dailyPlanOrderFilter, orders, plannedOrders, false);

                for (PlannedOrderItem p : plannedOrders) {
                    if (DailyPlanOrderStateText.SUBMITTED.value().equals(p.getState().get_text().value()) && !(DailyPlanOrderStateText.FINISHED
                            .value().equals(p.getState().get_text().value()))) {
                        if (p.getLate()) {
                            answer.setSubmittedLate(answer.getSubmittedLate() + 1);
                        } else {
                            answer.setSubmitted(answer.getSubmitted() + 1);
                        }
                    }
                    if (DailyPlanOrderStateText.PLANNED.value().equals(p.getState().get_text().value())) {
                        if (p.getLate()) {
                            answer.setPlannedLate(answer.getPlannedLate() + 1);
                        } else {
                            answer.setPlanned(answer.getPlanned() + 1);
                        }
                    }
                    if (DailyPlanOrderStateText.FINISHED.value().equals(p.getState().get_text().value())) {
                        answer.setFinished(answer.getFinished() + 1);
                    }
                }
            }
            return JOCDefaultResponse.responseStatus200(answer);

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
}
