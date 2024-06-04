package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanOrdersResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.dailyplan.PlannedOrders;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrdersImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersImpl.class);

    @Override
    public JOCDefaultResponse postDailyPlan(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {

            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderFilterDef.class);
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);

            boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
            boolean permitted = true;
            Set<String> allowedControllers = Collections.emptySet();
            if (!noControllerAvailable) {
                Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
                if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
                    controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
                }
                allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController,
                        accessToken).getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            }

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }

            boolean isDebugEnabled = LOGGER.isDebugEnabled();

            setSettings();
            if (isDebugEnabled) {
                if (in.getDailyPlanDateTo() == null) {
                    LOGGER.debug("Reading the daily plan from the day " + in.getDailyPlanDateFrom());
                } else if (in.getDailyPlanDateFrom().equals(in.getDailyPlanDateTo())) {
                    LOGGER.debug("Reading the daily plan the day " + in.getDailyPlanDateFrom());
                } else {
                    LOGGER.debug("Reading the daily plan for the days " + in.getDailyPlanDateFrom() + " - " + in.getDailyPlanDateTo());
                }
            }
            Date dateFrom = toUTCDate(in.getDailyPlanDateFrom());
            Date dateTo = toUTCDate(in.getDailyPlanDateTo());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            List<PlannedOrderItem> result = new ArrayList<>();
            for (String controllerId : allowedControllers) {
                List<Long> submissions = dbLayer.getSubmissionIds(controllerId, dateFrom, dateTo);
                if (submissions == null || submissions.size() == 0) {
                    if (isDebugEnabled) {
                        if (in.getDailyPlanDateTo() == null) {
                            LOGGER.debug(String.format("[%s][%s-][skip]couldn't find submissions", controllerId, in.getDailyPlanDateFrom()));
                        } else if (in.getDailyPlanDateFrom().equals(in.getDailyPlanDateTo())) {
                            LOGGER.debug(String.format("[%s][%s][skip]couldn't find submissions", controllerId, in.getDailyPlanDateFrom()));
                        } else {
                            LOGGER.debug(String.format("[%s][%s-%s][skip]couldn't find submissions", controllerId, in.getDailyPlanDateFrom(), in
                                    .getDailyPlanDateTo()));
                        }
                    }
                    continue;
                }
                FilterDailyPlannedOrders filter = getOrderFilter(IMPL_PATH, controllerId, in, true);
                if (filter == null) {
                    continue;
                }
                Date plannedStartFrom = filter.getOrderPlannedStartFrom();
                Date plannedStartTo = filter.getOrderPlannedStartTo();

                filter.setOrderPlannedStartFrom(null);
                filter.setOrderPlannedStartTo(null);
                // filter.setSubmissionIds(null);
                filter.setSubmissionForDateFrom(dateFrom);
                filter.setSubmissionForDateTo(dateTo);

                List<DBItemDailyPlanWithHistory> orders = getOrders(session, filter, true);
                Map<String, Set<String>> orderTags = orders == null ? Collections.emptyMap() : OrderTags.getTagsByOrderIds(controllerId, orders
                        .stream().map(DBItemDailyPlanWithHistory::getOrderId), session);
                addOrders(session, controllerId, plannedStartFrom, plannedStartTo, in, orders, result, true, orderTags);
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
