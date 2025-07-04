package com.sos.joc.dailyplan.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanOrdersSummaryResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.DailyPlanOrdersSummary;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrdersSummaryImpl extends JOCOrderResourceImpl implements IDailyPlanOrdersSummaryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersSummaryImpl.class);

    @Override
    public JOCDefaultResponse postDailyPlanOrdersSummary(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {

            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.DAILYPLAN);
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
                allowedControllers = controllerIds.filter(availableController -> getBasicControllerPermissions(availableController, accessToken)
                        .getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            }

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }

            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            setSettings(IMPL_PATH);

            if (isDebugEnabled) {
                // TODO LOGGER.debug("Reading the daily plan for day " + in.getFilter().getDailyPlanDate());
            }

            DailyPlanOrdersSummary answer = new DailyPlanOrdersSummary();
            answer.setFinished(0);
            answer.setSubmitted(0);
            answer.setSubmittedLate(0);
            answer.setPlanned(0);
            answer.setPlannedLate(0);

            Date dateFrom = toUTCDate(in.getDailyPlanDateFrom());
            Date dateTo = toUTCDate(in.getDailyPlanDateTo());
            if (dateTo == null) {
                dateTo = dateFrom;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            // DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            for (String controllerId : allowedControllers) {
                // List<Long> submissions = dbLayer.getSubmissionIds(controllerId, dateFrom, dateTo);
                // if (submissions == null || submissions.size() == 0) {
                // if (isDebugEnabled) {
                // LOGGER.debug(String.format("[%s][%s][skip]couldn't find submissions", controllerId, in.getFilter().getDailyPlanDate()));
                // }
                // continue;
                // }

                FilterDailyPlannedOrders filter = getOrderFilter(IMPL_PATH, controllerId, in, true);
                if (filter == null) {
                    continue;
                }
                Date plannedStartFrom = filter.getOrderPlannedStartFrom();
                Date plannedStartTo = filter.getOrderPlannedStartTo();

                filter.setOrderPlannedStartFrom(null);
                filter.setOrderPlannedStartTo(null);
                filter.setSubmissionIds(null);
                filter.setSubmissionForDateFrom(dateFrom);
                filter.setSubmissionForDateTo(dateTo);

                List<PlannedOrderItem> result = new ArrayList<>();
                List<DBItemDailyPlanWithHistory> orders = getOrders(session, filter, false);
                addOrders(session, controllerId, plannedStartFrom, plannedStartTo, in, orders, result, false, Collections.emptyMap());

                for (PlannedOrderItem p : result) {
                    String state = p.getState().get_text().value();
                    if (DailyPlanOrderStateText.SUBMITTED.value().equals(state)) {
                        if (p.getLate()) {
                            answer.setSubmittedLate(answer.getSubmittedLate() + 1);
                        } else {
                            answer.setSubmitted(answer.getSubmitted() + 1);
                        }
                    } else if (DailyPlanOrderStateText.PLANNED.value().equals(state)) {
                        if (p.getLate()) {
                            answer.setPlannedLate(answer.getPlannedLate() + 1);
                        } else {
                            boolean addPlanned = true;
                            if (p.getStartMode() == 1) {// cyclic max item
                                Date now = new Date();
                                if (p.getPlannedStartTime().getTime() > now.getTime()) {
                                    boolean selectMinPlannedStart = true;
                                    if (plannedStartFrom != null && plannedStartFrom.getTime() > now.getTime()) {
                                        selectMinPlannedStart = false;
                                    }
                                    if (selectMinPlannedStart) {
                                        Date minPlannedStart = getCyclicMinPlannedStart(session, controllerId, plannedStartFrom, plannedStartTo, p
                                                .getOrderId());
                                        if (minPlannedStart != null && now.getTime() > minPlannedStart.getTime()) {
                                            answer.setPlannedLate(answer.getPlannedLate() + 1);
                                            addPlanned = false;
                                        }
                                    }
                                } else {
                                    answer.setPlannedLate(answer.getPlannedLate() + 1);
                                    addPlanned = false;
                                }
                            }
                            if (addPlanned) {
                                answer.setPlanned(answer.getPlanned() + 1);
                            }
                        }
                    } else if (DailyPlanOrderStateText.FINISHED.value().equals(state)) {
                        answer.setFinished(answer.getFinished() + 1);
                    }
                }
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
}
