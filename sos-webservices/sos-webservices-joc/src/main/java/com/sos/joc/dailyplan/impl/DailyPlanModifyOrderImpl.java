package com.sos.joc.dailyplan.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerInventoryReleasedConfigurations;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterInventoryReleasedConfigurations;
import com.sos.joc.dailyplan.db.FilterOrderVariables;
import com.sos.joc.dailyplan.resource.IDailyPlanModifyOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
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
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanModifyOrderImpl extends JOCOrderResourceImpl implements IDailyPlanModifyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyOrderImpl.class);

    @Override
    public JOCDefaultResponse postModifyOrder(String accessToken, byte[] filterBytes) {

        LOGGER.debug("Change start time for orders from the daily plan");

        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
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

                DBItemDailyPlanOrder cyclicItem = null;
                for (String orderId : orderIds) {
                    cyclicItem = addCyclicOrderIds(newOrderIds, orderId, in.getControllerId());
                }

                if (cyclicItem != null && cyclicItem.getStartMode() == 1 && in.getCycle() != null) {
                    recreateCyclicOrder(in, newOrderIds, cyclicItem, auditlog);
                } else {
                    modifyOrder(in, newOrderIds, accessToken, auditlog);
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

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[getCalendarById]");
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

    private void recreateCyclicOrder(DailyPlanModifyOrder in, List<String> orderIds, final DBItemDailyPlanOrder plannedOrder,
            DBItemJocAuditLog auditlog) throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {
        SOSHibernateSession session = null;
        try {
            final String controllerId = plannedOrder.getControllerId();
            final Long oldSubmissionId = plannedOrder.getSubmissionHistoryId();

            // remove not submitted
            removeCyclicOrder(in, controllerId, oldSubmissionId, orderIds, false);

            FilterOrderVariables filterOV = new FilterOrderVariables();
            filterOV.setPlannedOrderId(plannedOrder.getId());

            FilterDailyPlannedOrders filterPO = new FilterDailyPlannedOrders();
            filterPO.setControllerId(in.getControllerId());
            filterPO.setOrderIds(orderIds);
            filterPO.setSubmitted(true);

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[recreateCyclicOrder]");
            DBLayerOrderVariables dbLayerOV = new DBLayerOrderVariables(session);
            List<DBItemDailyPlanVariable> variables = dbLayerOV.getOrderVariables(filterOV, 0);

            // get submitted
            DBLayerDailyPlannedOrders dbLayerPO = new DBLayerDailyPlannedOrders(session);
            List<DBItemDailyPlanOrder> items = dbLayerPO.getDailyPlanList(filterPO, 0);
            Globals.disconnect(session);
            session = null;

            // has submitted
            if (items.size() > 0) {
                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(filterPO.getControllerId(), items);
                c.thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), filterPO.getControllerId());
                    if (either.isRight()) {
                        // remove submitted
                        removeCyclicOrder(in, controllerId, oldSubmissionId, orderIds, true);
                        executeRecreateCyclicOrder(in, plannedOrder, variables, auditlog);
                    }
                });

            } else {
                executeRecreateCyclicOrder(in, plannedOrder, variables, auditlog);
            }
        } finally {
            Globals.disconnect(session);
        }
    }

    private void removeCyclicOrder(DailyPlanModifyOrder in, String controllerId, Long oldSubmissionId, List<String> orderIds, boolean submitted) {
        SOSHibernateSession session = null;
        try {
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setOrderIds(orderIds);
            filter.setSubmitted(submitted);

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[removeRecreateCyclicOrder]");
            session.setAutoCommit(false);
            session.beginTransaction();

            setSettings();

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            dbLayer.deleteCascading(filter);
            Globals.commit(session);

            if (oldSubmissionId != null) {
                Long count = dbLayer.getCountOrdersBySubmissionId(controllerId, oldSubmissionId);
                if (count.equals(0L)) {
                    session.beginTransaction();
                    dbLayer.deleteSubmission(oldSubmissionId);
                    session.commit();
                }
            }

        } catch (JocConfigurationException | DBConnectionRefusedException | ControllerConnectionResetException | ControllerConnectionRefusedException
                | DBMissingDataException | DBOpenSessionException | DBInvalidDataException | SOSException e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
        } finally {
            Globals.disconnect(session);
        }
    }

    private void executeRecreateCyclicOrder(DailyPlanModifyOrder in, final DBItemDailyPlanOrder item, List<DBItemDailyPlanVariable> listOfVariables,
            DBItemJocAuditLog auditlog) {
        String controllerId = in.getControllerId();
        String dDate = in.getDailyPlanDate();
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
            calendars.setTimeZone(in.getTimeZone());
            Period period = new Period();
            period.setBegin(in.getCycle().getBegin());
            period.setEnd(in.getCycle().getEnd());
            period.setRepeat(in.getCycle().getRepeat());
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

    private void updateVariables(SOSHibernateSession session, DailyPlanModifyOrder in, DBItemDailyPlanOrder plannedOrder)
            throws SOSHibernateException, IOException {

        DBLayerOrderVariables dbLayer = new DBLayerOrderVariables(session);
        FilterOrderVariables filter = new FilterOrderVariables();
        DBItemDailyPlanVariable item = new DBItemDailyPlanVariable();

        filter.setPlannedOrderId(plannedOrder.getId());
        List<DBItemDailyPlanVariable> items = dbLayer.getOrderVariables(filter, 0);
        if (items.size() > 0) {
            item = items.get(0);
        } else {
            item.setPlannedOrderId(plannedOrder.getId());
            item.setCreated(new Date());
        }

        if (in.getVariables() != null) {
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

            for (Entry<String, Object> variable : in.getVariables().getAdditionalProperties().entrySet()) {
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

        if (in.getRemoveVariables() != null) {
            Variables variables;
            if (item.getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(item.getVariableValue(), Variables.class);
            } else {
                variables = new Variables();
            }
            Map<String, Object> map = new HashMap<String, Object>();
            for (Entry<String, Object> variable : variables.getAdditionalProperties().entrySet()) {
                if (in.getRemoveVariables().getAdditionalProperties().get(variable.getKey()) == null) {
                    map.put(variable.getKey(), variable.getValue());
                }
            }
            variables.getAdditionalProperties().clear();
            variables.setAdditionalProperties(map);
            item.setVariableValue(Globals.objectMapper.writeValueAsString(variables));
        }

        if (item.getVariableValue() != null && !item.getVariableValue().isEmpty()) {
            if (items.size() > 0) {
                session.update(item);
            } else {
                session.save(item);
            }
        }

    }

    private void modifyOrder(DailyPlanModifyOrder in, List<String> orderIds, String accessToken, DBItemJocAuditLog auditlog)
            throws JocConfigurationException, DBConnectionRefusedException, ControllerInvalidResponseDataException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, DBInvalidDataException, SOSException,
            URISyntaxException, InterruptedException, ExecutionException, IOException, ParseException, TimeoutException {

        SOSHibernateSession session = null;
        try {
            final String dailyPlanDate = in.getDailyPlanDate();

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(in.getControllerId());
            filter.setOrderIds(orderIds);

            session = Globals.createSosHibernateStatelessConnection("modifyOrder");
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            List<DBItemDailyPlanOrder> items = dbLayer.getDailyPlanList(filter, 0);
            Globals.disconnect(session);
            session = null;
            String controllerId = filter.getControllerId();

            if (items.size() > 0) {
                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(controllerId, items);
                c.thenAccept(either -> {

                    SOSHibernateSession newSession = null;
                    try {
                        Date scheduledForDate = null;
                        DBItemDailyPlanSubmission newSubmission = null;
                        Long oldSubmissionId = items.get(0).getSubmissionHistoryId();
                        if (in.getScheduledFor() != null) {
                            Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(in.getScheduledFor(), in.getTimeZone());
                            scheduledForDate = JobSchedulerDate.nowInUtc();
                            if (!scheduledFor.equals(Optional.empty())) {
                                scheduledForDate = Date.from(scheduledFor.get());
                            }
                        }

                        newSession = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[removeFromJobSchedulerController]");
                        newSession.setAutoCommit(false);

                        if (scheduledForDate != null) {
                            newSubmission = newSubmission(controllerId, scheduledForDate);
                            newSession.save(newSubmission);
                        }

                        DBLayerDailyPlannedOrders dbLayerPO = new DBLayerDailyPlannedOrders(newSession);
                        DBLayerOrderVariables dbLayerOV = new DBLayerOrderVariables(newSession);
                        List<DBItemDailyPlanOrder> newItems = new ArrayList<>();

                        for (DBItemDailyPlanOrder item : items) {
                            Globals.beginTransaction(newSession);
                            item.setModified(new Date());
                            if (scheduledForDate != null) {
                                Long expectedDuration = item.getExpectedEnd().getTime() - item.getPlannedStart().getTime();
                                item.setExpectedEnd(new Date(expectedDuration + scheduledForDate.getTime()));
                                item.setPlannedStart(scheduledForDate);
                                item.setSubmissionHistoryId(newSubmission.getId());
                                newSession.update(item);
                            }

                            if ((in.getVariables() != null && in.getVariables() != null) || (in.getRemoveVariables() != null && in
                                    .getRemoveVariables() != null)) {
                                updateVariables(newSession, in, item);
                            }

                            if (item.getSubmitted()) {
                                Long oldId = item.getId();

                                FilterDailyPlannedOrders filterPO = new FilterDailyPlannedOrders();
                                filterPO.setPlannedOrderId(item.getId());
                                dbLayerPO.delete(filterPO);

                                DBItemDailyPlanOrder newItem = dbLayerPO.insertFrom(item);
                                dbLayerOV.update(oldId, newItem.getId());
                                Globals.commit(newSession);

                                newItems.add(newItem);
                            } else {
                                Globals.commit(newSession);
                            }
                        }
                        if (newSubmission != null) {
                            Long count = dbLayerPO.getCountOrdersBySubmissionId(controllerId, oldSubmissionId);
                            if (count.equals(0L)) {
                                newSession.beginTransaction();
                                dbLayerPO.deleteSubmission(oldSubmissionId);
                                newSession.commit();
                            }
                        }
                        newSession.close();
                        newSession = null;

                        if (newItems.size() > 0) {
                            submitOrdersToController(newItems);
                        }

                        EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));
                        OrdersHelper.storeAuditLogDetails(items.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(),
                                controllerId)).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper
                                        .postExceptionEventIfExist(either2, accessToken, getJocError(), in.getControllerId()));

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

    private DBItemDailyPlanSubmission newSubmission(String controllerId, Date scheduleForDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateWithoutTime = sdf.parse(sdf.format(scheduleForDate));

        DBItemDailyPlanSubmission item = new DBItemDailyPlanSubmission();
        item.setControllerId(controllerId);
        item.setSubmissionForDate(dateWithoutTime);
        item.setUserAccount(this.getJobschedulerUser().getSosShiroCurrentUser().getUsername());
        item.setCreated(new Date());
        return item;
    }

}
