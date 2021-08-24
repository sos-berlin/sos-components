package com.sos.webservices.order.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
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
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
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
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
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
            DailyPlanModifyOrder dailyplanModifyOrder = Globals.objectMapper.readValue(filterBytes, DailyPlanModifyOrder.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(dailyplanModifyOrder.getControllerId(), getJocPermissions(accessToken)
                    .getDailyPlan().getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // TODO this check is not necessary if schema specifies orderIds as required and with minItems:1
            // uniqueItems should also better
            this.checkRequiredParameter("orderIds", dailyplanModifyOrder.getOrderIds());
            boolean startTimeGiven = dailyplanModifyOrder.getScheduledFor() != null || (dailyplanModifyOrder.getCycle() != null
                    && dailyplanModifyOrder.getCycle().getRepeat() != null);
            if (!startTimeGiven && dailyplanModifyOrder.getRemoveVariables() == null && dailyplanModifyOrder.getVariables() == null) {
                throw new JocMissingRequiredParameterException("variables, removeVariables, scheduledFor or cycle missing");
            }

            List<String> orderIds = dailyplanModifyOrder.getOrderIds();
            Set<String> temporaryOrderIds = orderIds.stream().filter(id -> id.matches(".*#T[0-9]+-.*")).collect(Collectors.toSet());
            orderIds.removeAll(temporaryOrderIds);

            CategoryType category = CategoryType.DAILYPLAN;
            if (orderIds.isEmpty()) {
                category = CategoryType.CONTROLLER;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(dailyplanModifyOrder.getAuditLog(), dailyplanModifyOrder.getControllerId(), category);

            List<Err419> errors = OrdersHelper.cancelAndAddFreshOrder(temporaryOrderIds, dailyplanModifyOrder, accessToken, getJocError(), dbAuditlog
                    .getId(), folderPermissions);

            if (!orderIds.isEmpty()) {
                setSettings();
                List<String> listOfOrderIds = new ArrayList<String>();
                for (String orderId : orderIds) {
                    listOfOrderIds.add(orderId);
                }

                DBItemDailyPlanOrders plannedOrder = null;

                for (String orderId : orderIds) {
                    plannedOrder = addCyclicOrderIds(listOfOrderIds, orderId, dailyplanModifyOrder.getControllerId());
                }

                final DBItemDailyPlanOrders finalPlannedOrder = plannedOrder;

                if (plannedOrder != null && plannedOrder.getStartMode() == 1 && dailyplanModifyOrder.getCycle() != null) {
                    recreateCyclicOrder(dailyplanModifyOrder, listOfOrderIds, finalPlannedOrder, dbAuditlog);
                } else {
                    modifyOrder(listOfOrderIds, accessToken, dailyplanModifyOrder, dbAuditlog);
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

        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("OrderInitiatorRunner");
            DBLayerInventoryReleasedConfigurations dbLayer = new DBLayerInventoryReleasedConfigurations(sosHibernateSession);
            FilterInventoryReleasedConfigurations filter = new FilterInventoryReleasedConfigurations();
            filter.setId(id);
            filter.setType(ConfigurationType.WORKINGDAYSCALENDAR);

            DBItemInventoryReleasedConfiguration config = dbLayer.getSingleInventoryReleasedConfigurations(filter);
            if (config == null) {
                throw new DBMissingDataException(String.format("calendar '%s' not found", id));
            }

            Calendar calendar = new ObjectMapper().readValue(config.getContent(), Calendar.class);
            calendar.setName(config.getName());
            calendar.setPath(config.getPath());

            return calendar;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    private void removeRecreateCyclicOrder(boolean submitted, DailyPlanModifyOrder dailyplanModifyOrder,
            final DBItemDailyPlanOrders finalPlannedOrder, List<String> listOfOrderIds, DBItemJocAuditLog dbAuditlog) {
        SOSHibernateSession sosHibernateSession = null;
        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);
        String dDate = dailyplanModifyOrder.getDailyPlanDate();
        String controllerId = dailyplanModifyOrder.getControllerId();

        if (dDate == null) {
            dDate = finalPlannedOrder.getOrderId().substring(1, 11);
        }

        try {
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            setSettings();

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filterDailyPlannedOrders = new FilterDailyPlannedOrders();
            filterDailyPlannedOrders.setListOfOrders(listOfOrderIds);
            filterDailyPlannedOrders.setSubmitted(submitted);
            dbLayerDailyPlannedOrders.deleteCascading(filterDailyPlannedOrders);
            Globals.commit(sosHibernateSession);

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | SOSException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void executeRecreateCyclicOrder(DailyPlanModifyOrder dailyplanModifyOrder, final DBItemDailyPlanOrders finalPlannedOrder,
            List<DBItemDailyPlanVariables> listOfVariables, DBItemJocAuditLog dbAuditlog) {
        SOSHibernateSession sosHibernateSession = null;
        sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);
        String dDate = dailyplanModifyOrder.getDailyPlanDate();
        String controllerId = dailyplanModifyOrder.getControllerId();

        if (dDate == null) {
            dDate = finalPlannedOrder.getOrderId().substring(1, 11);
        }

        try {

            setSettings();

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(false);
            orderInitiatorSettings.setSubmit(finalPlannedOrder.getSubmitted());

            orderInitiatorSettings.setTimeZone(settings.getTimeZone());
            orderInitiatorSettings.setPeriodBegin(settings.getPeriodBegin());

            orderInitiatorSettings.setDailyPlanDate(DailyPlanHelper.stringAsDate(dDate));
            orderInitiatorSettings.setSubmissionTime(new Date());

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            TimeZone savT = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Schedule schedule = new Schedule();

            schedule.setVersion("");
            schedule.setPath(finalPlannedOrder.getSchedulePath());
            schedule.setWorkflowName(finalPlannedOrder.getWorkflowName());
            schedule.setWorkflowPath(finalPlannedOrder.getWorkflowPath());
            schedule.setTitle("");
            schedule.setDocumentationName("");
            schedule.setSubmitOrderToControllerWhenPlanned(finalPlannedOrder.getSubmitted());
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
            AssignedCalendars assignedCalendars = new AssignedCalendars();
            Calendar calendar = getCalendarById(finalPlannedOrder.getCalendarId());
            assignedCalendars.setCalendarName(calendar.getName());
            assignedCalendars.setPeriods(new ArrayList<Period>());
            assignedCalendars.setTimeZone(dailyplanModifyOrder.getTimeZone());
            Period period = new Period();
            period.setBegin(dailyplanModifyOrder.getCycle().getBegin());
            period.setEnd(dailyplanModifyOrder.getCycle().getEnd());
            period.setRepeat(dailyplanModifyOrder.getCycle().getRepeat());
            assignedCalendars.getPeriods().add(period);
            schedule.getCalendars().add(assignedCalendars);

            orderInitiatorRunner.addSchedule(schedule);

            Map<PlannedOrderKey, PlannedOrder> generatedOrders = orderInitiatorRunner.generateDailyPlan(controllerId, getJocError(), getAccessToken(),
                    finalPlannedOrder.getDailyPlanDate(settings.getTimeZone()), finalPlannedOrder.getSubmitted());
            TimeZone.setDefault(savT);

            List<AuditLogDetail> auditLogDetails = new ArrayList<>();

            for (Entry<PlannedOrderKey, PlannedOrder> entry : generatedOrders.entrySet()) {
                auditLogDetails.add(new AuditLogDetail(entry.getValue().getWorkflowPath(), entry.getValue().getFreshOrder().getId()));
            }

            EventBus.getInstance().post(new DailyPlanEvent(dDate));

            OrdersHelper.storeAuditLogDetails(auditLogDetails, dbAuditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                    either2, getAccessToken(), getJocError(), controllerId));

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | IOException | ParseException | SOSException
                | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void recreateCyclicOrder(DailyPlanModifyOrder dailyplanModifyOrder, List<String> listOfOrderIds,
            final DBItemDailyPlanOrders finalPlannedOrder, DBItemJocAuditLog dbAuditlog) throws SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        SOSHibernateSession sosHibernateSession = null;
        String controllerId = dailyplanModifyOrder.getControllerId();
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filterOrderVariables = new FilterOrderVariables();
            filterOrderVariables.setPlannedOrderId(finalPlannedOrder.getId());

            List<DBItemDailyPlanVariables> listOfVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);

            removeRecreateCyclicOrder(false, dailyplanModifyOrder, finalPlannedOrder, listOfOrderIds, dbAuditlog);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(controllerId);
            filter.setListOfOrders(listOfOrderIds);
            filter.setSubmitted(true);

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
            if (listOfPlannedOrders.size() > 0) {
                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(controllerId, listOfPlannedOrders);
                c.thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), filter.getControllerId());
                    if (either.isRight()) {
                        removeRecreateCyclicOrder(true, dailyplanModifyOrder, finalPlannedOrder, listOfOrderIds, dbAuditlog);
                        executeRecreateCyclicOrder(dailyplanModifyOrder, finalPlannedOrder, listOfVariables, dbAuditlog);
                    }

                });

            } else {
                executeRecreateCyclicOrder(dailyplanModifyOrder, finalPlannedOrder, listOfVariables, dbAuditlog);
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void submitOrdersToController(List<DBItemDailyPlanOrders> listOfPlannedOrders) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        if (listOfPlannedOrders.size() > 0) {

            OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
            orderInitiatorSettings.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
            orderInitiatorSettings.setOverwrite(false);
            orderInitiatorSettings.setSubmit(true);

            orderInitiatorSettings.setTimeZone(settings.getTimeZone());
            orderInitiatorSettings.setPeriodBegin(settings.getPeriodBegin());
            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);

            orderInitiatorRunner.submitOrders(listOfPlannedOrders.get(0).getControllerId(), getJocError(), getAccessToken(), listOfPlannedOrders);
        }
    }

    private void updateVariables(SOSHibernateSession sosHibernateSession, DailyPlanModifyOrder dailyplanModifyOrder,
            DBItemDailyPlanOrders dbItemDailyPlanOrder) throws SOSHibernateException, IOException {

        DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
        FilterOrderVariables filter = new FilterOrderVariables();
        filter.setPlannedOrderId(dbItemDailyPlanOrder.getId());
        List<DBItemDailyPlanVariables> listOfVariables = dbLayerOrderVariables.getOrderVariables(filter, 0);
        DBItemDailyPlanVariables dbItemDailyPlanVariables = new DBItemDailyPlanVariables();
        if (listOfVariables.size() > 0) {
            dbItemDailyPlanVariables = listOfVariables.get(0);
        } else {
            dbItemDailyPlanVariables.setPlannedOrderId(dbItemDailyPlanOrder.getId());
            dbItemDailyPlanVariables.setCreated(new Date());
        }

        if (dailyplanModifyOrder.getVariables() != null) {

            Variables variables = new Variables();
            if (dbItemDailyPlanVariables.getVariableValue() != null) {
                try {
                    variables = Globals.objectMapper.readValue(dbItemDailyPlanVariables.getVariableValue(), Variables.class);
                } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
                    LOGGER.warn("Illegal value " + dbItemDailyPlanVariables.getVariableValue() + " in DPL_ORDER_VARIABLES for order: "
                            + dbItemDailyPlanOrder.getOrderId());
                    variables = new Variables();
                }
            }
            Map<String, Object> newAdditionalProperties = new HashMap<String, Object>();
            for (Entry<String, Object> variable : variables.getAdditionalProperties().entrySet()) {
                newAdditionalProperties.put(variable.getKey(), variable.getValue());
            }

            for (Entry<String, Object> variable : dailyplanModifyOrder.getVariables().getAdditionalProperties().entrySet()) {
                newAdditionalProperties.put(variable.getKey(), variable.getValue());
            }
            variables.setAdditionalProperties(dailyplanModifyOrder.getVariables().getAdditionalProperties());
            String variablesJson = Globals.objectMapper.writeValueAsString(variables);
            dbItemDailyPlanVariables.setVariableValue(variablesJson);
            dbItemDailyPlanVariables.setModified(new Date());
        }

        if (dailyplanModifyOrder.getRemoveVariables() != null) {
            Variables variables;
            if (dbItemDailyPlanVariables.getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(dbItemDailyPlanVariables.getVariableValue(), Variables.class);
            } else {
                variables = new Variables();
            }
            Map<String, Object> newAdditionalProperties = new HashMap<String, Object>();
            for (Entry<String, Object> variable : variables.getAdditionalProperties().entrySet()) {
                if (dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties().get(variable.getKey()) == null) {
                    newAdditionalProperties.put(variable.getKey(), variable.getValue());
                }
            }
            variables.getAdditionalProperties().clear();
            variables.setAdditionalProperties(newAdditionalProperties);
            String variablesJson = Globals.objectMapper.writeValueAsString(variables);
            dbItemDailyPlanVariables.setVariableValue(variablesJson);
        }

        if (dbItemDailyPlanVariables.getVariableValue() != null && !dbItemDailyPlanVariables.getVariableValue().isEmpty()) {
            if (listOfVariables.size() > 0) {
                sosHibernateSession.update(dbItemDailyPlanVariables);
            } else {
                sosHibernateSession.save(dbItemDailyPlanVariables);
            }
        }

    }

    private void modifyOrder(List<String> listOfOrderIds, String accessToken, DailyPlanModifyOrder dailyplanModifyOrder, DBItemJocAuditLog dbAuditlog)
            throws JocConfigurationException, DBConnectionRefusedException, ControllerInvalidResponseDataException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, IOException, ParseException, TimeoutException {

        SOSHibernateSession sosHibernateSession = null;
        try {

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(dailyplanModifyOrder.getControllerId());
            filter.setListOfOrders(listOfOrderIds);

            final String dailyPlanDate = dailyplanModifyOrder.getDailyPlanDate();

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
            if (listOfPlannedOrders.size() > 0) {

                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(filter.getControllerId(),
                        listOfPlannedOrders);
                c.thenAccept(either -> {

                    SOSHibernateSession sosHibernateSession2 = null;
                    try {

                        Date scheduledForDate = null;
                        if (dailyplanModifyOrder.getScheduledFor() != null) {

                            Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyplanModifyOrder.getScheduledFor(),
                                    dailyplanModifyOrder.getTimeZone());
                            scheduledForDate = JobSchedulerDate.nowInUtc();
                            if (!scheduledFor.equals(Optional.empty())) {
                                scheduledForDate = Date.from(scheduledFor.get());
                            }
                        }

                        sosHibernateSession2 = Globals.createSosHibernateStatelessConnection(API_CALL_MODIFY_ORDER);

                        for (DBItemDailyPlanOrders dbItemDailyPlanOrder : listOfPlannedOrders) {
                            sosHibernateSession2.setAutoCommit(false);
                            Globals.beginTransaction(sosHibernateSession2);
                            dbItemDailyPlanOrder.setModified(new Date());
                            if ((dailyplanModifyOrder.getScheduledFor() != null) && (scheduledForDate != null)) {
                                Long expectedDuration = dbItemDailyPlanOrder.getExpectedEnd().getTime() - dbItemDailyPlanOrder.getPlannedStart()
                                        .getTime();
                                dbItemDailyPlanOrder.setExpectedEnd(new Date(expectedDuration + scheduledForDate.getTime()));
                                dbItemDailyPlanOrder.setPlannedStart(scheduledForDate);
                                sosHibernateSession2.update(dbItemDailyPlanOrder);
                            }

                            if ((dailyplanModifyOrder.getVariables() != null && dailyplanModifyOrder.getVariables() != null) || (dailyplanModifyOrder
                                    .getRemoveVariables() != null && dailyplanModifyOrder.getRemoveVariables() != null)) {
                                updateVariables(sosHibernateSession2, dailyplanModifyOrder, dbItemDailyPlanOrder);
                            }

                            if (dbItemDailyPlanOrder.getSubmitted()) {
                                List<DBItemDailyPlanWithHistory> listOfDailyPlanItems = new ArrayList<DBItemDailyPlanWithHistory>();
                                DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory = new DBItemDailyPlanWithHistory();
                                dbItemDailyPlanWithHistory.setOrderId(dbItemDailyPlanOrder.getOrderId());
                                dbItemDailyPlanWithHistory.setPlannedOrderId(dbItemDailyPlanOrder.getId());
                                listOfDailyPlanItems.add(dbItemDailyPlanWithHistory);

                                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders2 = new DBLayerDailyPlannedOrders(sosHibernateSession2);
                                DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession2);

                                FilterDailyPlannedOrders filterDailyPlannedOrders = new FilterDailyPlannedOrders();
                                filterDailyPlannedOrders.setPlannedOrderId(dbItemDailyPlanOrder.getId());

                                int retry = 10;
                                do {
                                    try {
                                        dbLayerDailyPlannedOrders2.delete(filterDailyPlannedOrders);
                                        DBItemDailyPlanOrders dbItemDailyPlanOrders = dbLayerDailyPlannedOrders2.insertFrom(dbItemDailyPlanOrder);
                                        dbLayerOrderVariables.update(dbItemDailyPlanWithHistory.getPlannedOrderId(), dbItemDailyPlanOrders.getId());
                                        retry = 0;
                                    } catch (SOSHibernateLockAcquisitionException e) {
                                        retry = retry - 1;
                                        try {
                                            java.lang.Thread.sleep(500);
                                        } catch (InterruptedException e1) {
                                        }
                                        if (retry == 0) {
                                            throw e;
                                        }
                                    }
                                } while (retry > 0);
                                Globals.commit(sosHibernateSession2);
                                submitOrdersToController(listOfPlannedOrders);
                                EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));
                            }
                            List<AuditLogDetail> auditLogDetails = new ArrayList<>();
                            auditLogDetails.add(new AuditLogDetail(dbItemDailyPlanOrder.getWorkflowPath(), dbItemDailyPlanOrder.getOrderId()));
                            OrdersHelper.storeAuditLogDetails(auditLogDetails, dbAuditlog.getId()).thenAccept(either2 -> ProblemHelper
                                    .postExceptionEventIfExist(either2, accessToken, getJocError(), dailyplanModifyOrder.getControllerId()));
                        }

                    } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                            | DBOpenSessionException | ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException
                            | SOSException | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), filter.getControllerId());
                    } finally {
                        Globals.disconnect(sosHibernateSession2);
                    }

                });

            }

        } finally

        {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
