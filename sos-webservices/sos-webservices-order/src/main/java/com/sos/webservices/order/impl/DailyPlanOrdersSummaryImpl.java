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
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.DailyPlanOrdersSummary;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanOrdersSummaryResource;

@Path("daily_plan")
public class DailyPlanOrdersSummaryImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersSummaryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersSummaryImpl.class);
    private static final String API_CALL = "./daily_plan/orders/summary";

    @Override
    public JOCDefaultResponse postDailyPlanOrdersSummary(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
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

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DailyPlanOrdersSummary dailyPlanOrdersSummary = new DailyPlanOrdersSummary();
            dailyPlanOrdersSummary.setFinished(0);
            dailyPlanOrdersSummary.setSubmitted(0);
            dailyPlanOrdersSummary.setSubmittedLate(0);
            dailyPlanOrdersSummary.setPlanned(0);
            dailyPlanOrdersSummary.setPlannedLate(0);

            for (String controllerId : allowedControllers) {
                ArrayList<PlannedOrderItem> listOfPlannedOrderItems = new ArrayList<PlannedOrderItem>();

                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = getOrders(sosHibernateSession, controllerId, dailyPlanOrderFilter);
                addOrders(controllerId, dailyPlanOrderFilter, listOfPlannedOrders, listOfPlannedOrderItems);

                for (PlannedOrderItem p : listOfPlannedOrderItems) {
                    if (DailyPlanOrderStateText.SUBMITTED.value().equals(p.getState().get_text().value())) {
                        dailyPlanOrdersSummary.setSubmitted(dailyPlanOrdersSummary.getSubmitted() + 1);

                        if (p.getLate()) {
                            dailyPlanOrdersSummary.setSubmittedLate(dailyPlanOrdersSummary.getSubmittedLate() + 1);
                        } else {
                            dailyPlanOrdersSummary.setSubmitted(dailyPlanOrdersSummary.getSubmitted() + 1);
                        }
                    }
                    if (DailyPlanOrderStateText.PLANNED.value().equals(p.getState().get_text().value())) {
                        if (p.getLate()) {
                            dailyPlanOrdersSummary.setPlannedLate(dailyPlanOrdersSummary.getPlannedLate() + 1);
                        } else {
                            dailyPlanOrdersSummary.setPlanned(dailyPlanOrdersSummary.getPlanned() + 1);
                        }
                    }
                    if (DailyPlanOrderStateText.FINISHED.value().equals(p.getState().get_text().value())) {
                        dailyPlanOrdersSummary.setFinished(dailyPlanOrdersSummary.getFinished() + 1);
                    }
                }
            }

            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatus200(dailyPlanOrdersSummary);

        } catch (

        JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
}
