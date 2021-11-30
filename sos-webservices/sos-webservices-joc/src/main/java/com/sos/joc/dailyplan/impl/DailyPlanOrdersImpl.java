package com.sos.joc.dailyplan.impl;

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
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanOrdersResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.dailyplan.PlannedOrders;
import com.sos.schema.JsonValidator;

@Path("daily_plan")
public class DailyPlanOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/orders";

    @Override
    public JOCDefaultResponse postDailyPlan(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession session = null;
        try {

            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilter.class);
            DailyPlanOrderFilter in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);

            this.checkRequiredParameter("filter", in.getFilter());
            this.checkRequiredParameter("dailyPlanDate", in.getFilter().getDailyPlanDate());

            Set<String> allowedControllers = getAllowedControllersOrdersView(in.getControllerId(), in.getFilter().getControllerIds(), accessToken)
                    .stream().filter(availableController -> getControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                            Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean isDebugEnabled = LOGGER.isDebugEnabled();

            setSettings();
            if (isDebugEnabled) {
                LOGGER.debug("Reading the daily plan for day " + in.getFilter().getDailyPlanDate());
            }
            Date date = toUTCDate(in.getFilter().getDailyPlanDate());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            ArrayList<PlannedOrderItem> result = new ArrayList<PlannedOrderItem>();
            for (String controllerId : allowedControllers) {
                List<Long> submissions = dbLayer.getSubmissionIds(controllerId, date);
                if (submissions == null || submissions.size() == 0) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip]submissions not found", controllerId, in.getFilter().getDailyPlanDate()));
                    }
                    continue;
                }
                FilterDailyPlannedOrders filter = getOrderFilter(API_CALL, controllerId, in, true);
                if (filter == null) {
                    continue;
                }
                Date plannedStartFrom = filter.getOrderPlannedStartFrom();
                Date plannedStartTo = filter.getOrderPlannedStartTo();

                filter.setOrderPlannedStartFrom(null);
                filter.setOrderPlannedStartTo(null);
                filter.setSubmissionIds(null);
                filter.setSubmissionForDate(date);

                List<DBItemDailyPlanWithHistory> orders = getOrders(session, controllerId, filter, true);
                addOrders(session, controllerId, plannedStartFrom, plannedStartTo, in, orders, result, true);
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
