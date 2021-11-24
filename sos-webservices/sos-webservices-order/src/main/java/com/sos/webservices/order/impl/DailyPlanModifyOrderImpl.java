package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.js7.order.initiator.DailyPlanRunner;
import com.sos.js7.order.initiator.DailyPlanSettings;
import com.sos.js7.order.initiator.classes.DailyPlanHelper;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.DBLayerInventoryReleasedConfigurations;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterInventoryReleasedConfigurations;
import com.sos.js7.order.initiator.db.FilterOrderVariables;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.resource.IDailyPlanModifyOrder;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("daily_plan")
public class DailyPlanModifyOrderImpl extends JOCOrderResourceImpl implements IDailyPlanModifyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyOrderImpl.class);
    private static final String API_CALL_MODIFY_ORDER = "./daily_plan/orders/modify";

    @Override
    public JOCDefaultResponse postModifyOrder(String accessToken, byte[] filterBytes) throws JocException {

        LOGGER.debug("Change start time for orders from the daily plan");

        try {
            initLogging(API_CALL_MODIFY_ORDER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanModifyOrder.class);
            DailyPlanModifyOrder in = Globals.objectMapper.readValue(filterBytes, DailyPlanModifyOrder.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getDailyPlan().getView());

            if (response != null) {
                return response;
            }

            // TODO this check is not necessary if schema specifies orderIds as required and with minItems:1
            // uniqueItems should also better
            this.checkRequiredParameter("orderIds", in.getOrderIds());
            boolean startTimeGiven = in.getScheduledFor() != null || (in.getCycle() != null && in.getCycle().getRepeat() != null);
            if (!startTimeGiven && in.getRemoveVariables() == null && in.getVariables() == null) {
                throw new JocMissingRequiredParameterException("variables, removeVariables, scheduledFor or cycle missing");
            }

            List<String> orderIds = in.getOrderIds();
            Set<String> tempOrderIds = orderIds.stream().filter(id -> id.matches(".*#T[0-9]+-.*")).collect(Collectors.toSet());
            orderIds.removeAll(tempOrderIds);

            CategoryType category = CategoryType.DAILYPLAN;
            if (orderIds.isEmpty()) {
                category = CategoryType.CONTROLLER;
            }
            DBItemJocAuditLog auditlog = storeAuditLog(in.getAuditLog(), in.getControllerId(), category);
            List<Err419> errors = OrdersHelper.cancelAndAddFreshOrder(tempOrderIds, in, accessToken, getJocError(), auditlog.getId(),
                    folderPermissions);

            if (!orderIds.isEmpty()) {
                setSettings();
                List<String> newOrderIds = new ArrayList<String>();
                for (String orderId : orderIds) {
                    newOrderIds.add(orderId);
                }

                DBItemDailyPlanOrder item = null;
                for (String orderId : orderIds) {
                    item = addCyclicOrderIds(newOrderIds, orderId, in.getControllerId());
                }

                final DBItemDailyPlanOrder finalItem = item;
                if (item != null && item.getStartMode() == 1 && in.getCycle() != null) {
                    recreateCyclicOrder(in, newOrderIds, finalItem, auditlog);
                } else {
                    modifyOrder(newOrderIds, accessToken, in, auditlog);
                }

                if (!errors.isEmpty()) {
                    return JOCDefaultResponse.responseStatus419(errors);
                }
            }
            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Calendar getCalendarById(Long id) throws JsonParseException, JsonMappingException, SOSHibernateException, IOException {

        SOSHibernateSession session = null;
        try {
            FilterInventoryReleasedConfigurations filter = new FilterInventoryReleasedConfigurations();
            filter.setId(id);
            filter.setType(ConfigurationType.WORKINGDAYSCALENDAR);

            session = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER + "[getCalendarById]");
            DBLayerInventoryReleasedConfigurations dbLayer = new DBLayerInventoryReleasedConfigurations(session);
            DBItemInventoryReleasedConfiguration config = dbLayer.getSingleInventoryReleasedConfigurations(filter);
            if (config == null) {
                throw new DBMissingDataException(String.format("calendar '%s' not found", id));
            }

            Calendar calendar = new ObjectMapper().readValue(config.getContent(), Calendar.class);
            calendar.setName(config.getName());
            calendar.setPath(config.getPath());
            return calendar;
        } finally {
            Globals.disconnect(session);
        }

    }

    private void removeRecreateCyclicOrder(boolean submitted, DailyPlanModifyOrder modifyOrder, List<String> orderIds, DBItemJocAuditLog auditlog) {
        SOSHibernateSession session = null;
        try {
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setOrderIds(orderIds);
            filter.setSubmitted(submitted);

            session = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER + "[removeRecreateCyclicOrder]");
            session.setAutoCommit(false);
            session.beginTransaction();

            setSettings();

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            dbLayer.deleteCascading(filter);
            Globals.commit(session);

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | SOSException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), modifyOrder.getControllerId());
        } finally {
            Globals.disconnect(session);
        }
    }

    private void executeRecreateCyclicOrder(DailyPlanModifyOrder modifyOrder, final DBItemDailyPlanOrder item,
            List<DBItemDailyPlanVariable> listOfVariables, DBItemJocAuditLog auditlog) {
        String controllerId = modifyOrder.getControllerId();
        String dDate = modifyOrder.getDailyPlanDate();
        if (dDate == null) {
            dDate = item.getOrderId().substring(1, 11);
        }

        try {
            setSettings();

            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            settings.setOverwrite(false);
            settings.setSubmit(item.getSubmitted());
            settings.setTimeZone(settings.getTimeZone());
            settings.setPeriodBegin(settings.getPeriodBegin());
            settings.setDailyPlanDate(DailyPlanHelper.stringAsDate(dDate));
            settings.setSubmissionTime(new Date());

            DailyPlanRunner runner = new DailyPlanRunner(settings, false);

            // TimeZone savT = TimeZone.getDefault();
            // TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Schedule schedule = new Schedule();
            schedule.setVersion("");
            schedule.setPath(item.getSchedulePath());
            schedule.setWorkflowName(item.getWorkflowName());
            schedule.setWorkflowPath(item.getWorkflowPath());
            schedule.setTitle("");
            schedule.setDocumentationName("");
            schedule.setSubmitOrderToControllerWhenPlanned(item.getSubmitted());
            schedule.setPlanOrderAutomatically(true);
            schedule.setVariableSets(new ArrayList<VariableSet>());
            VariableSet variableSet = new VariableSet();
            Variables variables = new Variables();
            if (listOfVariables != null && listOfVariables.size() > 0 && listOfVariables.get(0).getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(listOfVariables.get(0).getVariableValue(), Variables.class);
            }
            variableSet.setVariables(variables);
            if (variableSet.getVariables().getAdditionalProperties().size() > 0) {
                schedule.getVariableSets().add(variableSet);
            }
            schedule.setCalendars(new ArrayList<AssignedCalendars>());
            AssignedCalendars calendars = new AssignedCalendars();
            Calendar calendar = getCalendarById(item.getCalendarId());
            calendars.setCalendarName(calendar.getName());
            calendars.setPeriods(new ArrayList<Period>());
            calendars.setTimeZone(modifyOrder.getTimeZone());
            Period period = new Period();
            period.setBegin(modifyOrder.getCycle().getBegin());
            period.setEnd(modifyOrder.getCycle().getEnd());
            period.setRepeat(modifyOrder.getCycle().getRepeat());
            calendars.getPeriods().add(period);
            schedule.getCalendars().add(calendars);

            runner.addSchedule(schedule);
            Map<PlannedOrderKey, PlannedOrder> generatedOrders = runner.generateDailyPlan(controllerId, getJocError(), getAccessToken(), item
                    .getDailyPlanDate(settings.getTimeZone()), item.getSubmitted());
            // TimeZone.setDefault(savT);

            Set<AuditLogDetail> auditLogDetails = new HashSet<>();
            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId(), controllerId));
            }

            EventBus.getInstance().post(new DailyPlanEvent(dDate));

            OrdersHelper.storeAuditLogDetails(auditLogDetails, auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                    either2, getAccessToken(), getJocError(), controllerId));

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | IOException | ParseException | SOSException
                | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        }
    }

    private void recreateCyclicOrder(DailyPlanModifyOrder modifyOrder, List<String> orderIds, final DBItemDailyPlanOrder plannedOrder,
            DBItemJocAuditLog auditlog) throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        SOSHibernateSession session = null;
        try {
            removeRecreateCyclicOrder(false, modifyOrder, orderIds, auditlog);

            FilterOrderVariables filterOV = new FilterOrderVariables();
            filterOV.setPlannedOrderId(plannedOrder.getId());

            FilterDailyPlannedOrders filterPO = new FilterDailyPlannedOrders();
            filterPO.setControllerId(modifyOrder.getControllerId());
            filterPO.setOrderIds(orderIds);
            filterPO.setSubmitted(true);

            session = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER + "[recreateCyclicOrder]");
            DBLayerOrderVariables dbLayerOV = new DBLayerOrderVariables(session);
            List<DBItemDailyPlanVariable> variables = dbLayerOV.getOrderVariables(filterOV, 0);

            DBLayerDailyPlannedOrders dbLayerPO = new DBLayerDailyPlannedOrders(session);
            List<DBItemDailyPlanOrder> items = dbLayerPO.getDailyPlanList(filterPO, 0);
            Globals.disconnect(session);
            session = null;

            if (items.size() > 0) {
                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(filterPO.getControllerId(), items);
                c.thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), filterPO.getControllerId());
                    if (either.isRight()) {
                        removeRecreateCyclicOrder(true, modifyOrder, orderIds, auditlog);
                        executeRecreateCyclicOrder(modifyOrder, plannedOrder, variables, auditlog);
                    }
                });

            } else {
                executeRecreateCyclicOrder(modifyOrder, plannedOrder, variables, auditlog);
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    private void submitOrdersToController(List<DBItemDailyPlanOrder> items) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        if (items.size() > 0) {
            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            settings.setOverwrite(false);
            settings.setSubmit(true);
            settings.setTimeZone(settings.getTimeZone());
            settings.setPeriodBegin(settings.getPeriodBegin());

            DailyPlanRunner runner = new DailyPlanRunner(settings, false);
            runner.submitOrders(items.get(0).getControllerId(), items, null, getJocError(), getAccessToken());
        }
    }

    private void updateVariables(SOSHibernateSession session, DailyPlanModifyOrder modifyOrder, DBItemDailyPlanOrder plannedOrder)
            throws SOSHibernateException, IOException {

        DBLayerOrderVariables dbLayer = new DBLayerOrderVariables(session);
        FilterOrderVariables filter = new FilterOrderVariables();
        DBItemDailyPlanVariable item = new DBItemDailyPlanVariable();

        filter.setPlannedOrderId(plannedOrder.getId());
        List<DBItemDailyPlanVariable> orderVariables = dbLayer.getOrderVariables(filter, 0);
        if (orderVariables.size() > 0) {
            item = orderVariables.get(0);
        } else {
            item.setPlannedOrderId(plannedOrder.getId());
            item.setCreated(new Date());
        }

        if (modifyOrder.getVariables() != null) {
            Variables variables = new Variables();
            if (item.getVariableValue() != null) {
                try {
                    variables = Globals.objectMapper.readValue(item.getVariableValue(), Variables.class);
                } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
                    LOGGER.warn("Illegal value " + item.getVariableValue() + " in DPL_ORDER_VARIABLES for order: " + plannedOrder.getOrderId());
                    variables = new Variables();
                }
            }

            Map<String, Object> map = new HashMap<String, Object>();
            List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

            for (Entry<String, Object> variable : variables.getAdditionalProperties().entrySet()) {
                if (variable.getValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> valList = (List<Map<String, Object>>) variable.getValue();
                    values.clear();
                    for (Map<String, Object> par : valList) {
                        for (Object key : par.keySet()) {
                            if (key != null) {
                                values.add(par);
                            }
                        }
                    }
                    map.put(variable.getKey(), values);
                } else {
                    map.put(variable.getKey(), variable.getValue());
                }
            }

            for (Entry<String, Object> variable : modifyOrder.getVariables().getAdditionalProperties().entrySet()) {
                if (variable.getValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> valList = (List<Map<String, Object>>) variable.getValue();
                    values.clear();
                    for (Map<String, Object> par : valList) {
                        for (Object key : par.keySet()) {
                            if (key != null) {
                                values.add(par);
                            }
                        }
                    }
                    map.put(variable.getKey(), values);
                } else {
                    map.put(variable.getKey(), variable.getValue());
                }
            }
            // variables.setAdditionalProperties(dailyplanModifyOrder.getVariables().getAdditionalProperties());
            variables.setAdditionalProperties(map);
            item.setVariableValue(Globals.objectMapper.writeValueAsString(variables));
            item.setModified(new Date());
        }

        if (modifyOrder.getRemoveVariables() != null) {
            Variables variables;
            if (item.getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(item.getVariableValue(), Variables.class);
            } else {
                variables = new Variables();
            }
            Map<String, Object> map = new HashMap<String, Object>();
            for (Entry<String, Object> variable : variables.getAdditionalProperties().entrySet()) {
                if (modifyOrder.getRemoveVariables().getAdditionalProperties().get(variable.getKey()) == null) {
                    map.put(variable.getKey(), variable.getValue());
                }
            }
            variables.getAdditionalProperties().clear();
            variables.setAdditionalProperties(map);
            String variablesJson = Globals.objectMapper.writeValueAsString(variables);
            item.setVariableValue(variablesJson);
        }

        if (item.getVariableValue() != null && !item.getVariableValue().isEmpty()) {
            if (orderVariables.size() > 0) {
                session.update(item);
            } else {
                session.save(item);
            }
        }

    }

    private void modifyOrder(List<String> orderIds, String accessToken, DailyPlanModifyOrder modifyOrder, DBItemJocAuditLog auditlog)
            throws JocConfigurationException, DBConnectionRefusedException, ControllerInvalidResponseDataException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, IOException, ParseException, TimeoutException {

        SOSHibernateSession session = null;
        try {
            final String dailyPlanDate = modifyOrder.getDailyPlanDate();

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(modifyOrder.getControllerId());
            filter.setOrderIds(orderIds);

            session = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            List<DBItemDailyPlanOrder> plannedOrders = dbLayer.getDailyPlanList(filter, 0);
            Globals.disconnect(session);
            session = null;
            String controllerId = filter.getControllerId();

            if (plannedOrders.size() > 0) {
                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(controllerId, plannedOrders);
                c.thenAccept(either -> {

                    SOSHibernateSession newSession = null;
                    try {

                        Date scheduledForDate = null;
                        if (modifyOrder.getScheduledFor() != null) {
                            Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(modifyOrder.getScheduledFor(), modifyOrder
                                    .getTimeZone());
                            scheduledForDate = JobSchedulerDate.nowInUtc();
                            if (!scheduledFor.equals(Optional.empty())) {
                                scheduledForDate = Date.from(scheduledFor.get());
                            }
                        }

                        newSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER + "[removeFromJobSchedulerController]");
                        newSession.setAutoCommit(false);

                        for (DBItemDailyPlanOrder item : plannedOrders) {
                            Globals.beginTransaction(newSession);
                            item.setModified(new Date());
                            if ((modifyOrder.getScheduledFor() != null) && (scheduledForDate != null)) {
                                Long expectedDuration = item.getExpectedEnd().getTime() - item.getPlannedStart().getTime();
                                item.setExpectedEnd(new Date(expectedDuration + scheduledForDate.getTime()));
                                item.setPlannedStart(scheduledForDate);
                                newSession.update(item);
                            }

                            if ((modifyOrder.getVariables() != null && modifyOrder.getVariables() != null) || (modifyOrder
                                    .getRemoveVariables() != null && modifyOrder.getRemoveVariables() != null)) {
                                updateVariables(newSession, modifyOrder, item);
                            }

                            if (item.getSubmitted()) {
                                List<DBItemDailyPlanWithHistory> items = new ArrayList<DBItemDailyPlanWithHistory>();
                                DBItemDailyPlanWithHistory dailyPlanWithHistory = new DBItemDailyPlanWithHistory();
                                dailyPlanWithHistory.setOrderId(item.getOrderId());
                                dailyPlanWithHistory.setPlannedOrderId(item.getId());
                                items.add(dailyPlanWithHistory);

                                DBLayerDailyPlannedOrders dbLayerPO = new DBLayerDailyPlannedOrders(newSession);
                                DBLayerOrderVariables dbLayerOV = new DBLayerOrderVariables(newSession);

                                FilterDailyPlannedOrders filterPO = new FilterDailyPlannedOrders();
                                filterPO.setPlannedOrderId(item.getId());

                                int retryCount = 20;
                                do {
                                    try {
                                        dbLayerPO.delete(filterPO);
                                        DBItemDailyPlanOrder dailyPlanOrder = dbLayerPO.insertFrom(item);
                                        dbLayerOV.update(dailyPlanWithHistory.getPlannedOrderId(), dailyPlanOrder.getId());
                                        if (retryCount != 20) {
                                            LOGGER.info("deadlock resolved successfully update dpl_orders");
                                        }
                                        retryCount = 0;
                                    } catch (SOSHibernateLockAcquisitionException e) {
                                        LOGGER.info("Try to resolve deadlock update dpl_orders");
                                        retryCount = retryCount - 1;
                                        try {
                                            java.lang.Thread.sleep(500);
                                        } catch (InterruptedException e1) {
                                        }
                                        if (retryCount == 0) {
                                            throw e;
                                        }
                                        LOGGER.debug("Retry update orders because SOSHibernateLockAcquisitionException was raised. Retry-counter: "
                                                + retryCount);
                                    }
                                } while (retryCount > 0);

                                Globals.commit(newSession);
                                submitOrdersToController(plannedOrders);
                            } else {
                                Globals.commit(newSession);
                            }
                        }

                        EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));
                        OrdersHelper.storeAuditLogDetails(plannedOrders.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item
                                .getOrderId(), controllerId)).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper
                                        .postExceptionEventIfExist(either2, accessToken, getJocError(), modifyOrder.getControllerId()));

                    } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                            | DBOpenSessionException | ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException
                            | SOSException | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), filter.getControllerId());
                    } finally {
                        Globals.disconnect(newSession);
                    }
                });
            }
        } finally {
            Globals.disconnect(session);
        }
    }

}
