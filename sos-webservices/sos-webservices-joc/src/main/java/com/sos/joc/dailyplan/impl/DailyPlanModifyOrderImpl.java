package com.sos.joc.dailyplan.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
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
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.common.PlannedOrderKey;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.db.DBLayerReleasedConfigurations;
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
import com.sos.joc.model.order.OrderIdMap;
import com.sos.joc.model.order.OrderIdMap200;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanModifyOrderImpl extends JOCOrderResourceImpl implements IDailyPlanModifyOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanModifyOrderImpl.class);

    private enum Action {
        RemoveVariables, ModifyVariables, ModifyStartTime
    }

    @Override
    public JOCDefaultResponse postModifyOrder(String accessToken, byte[] filterBytes) {
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

            Action action = getAction(in);
            List<String> orderIds = in.getOrderIds();

            // Ad Hoc Orders
            Set<String> adhocOrderIds = orderIds.stream().filter(id -> id.matches(".*#T[0-9]+-.*")).collect(Collectors.toSet());
            orderIds.removeAll(adhocOrderIds);

            CategoryType category = orderIds.isEmpty() ? CategoryType.CONTROLLER : CategoryType.DAILYPLAN;
            DBItemJocAuditLog auditlog = storeAuditLog(in.getAuditLog(), in.getControllerId(), category);

            // Ad Hoc Orders
            Either<List<Err419>, OrderIdMap> adhocCall = OrdersHelper.cancelAndAddFreshOrder(adhocOrderIds, in, accessToken, getJocError(), auditlog
                    .getId(), folderPermissions);
            OrderIdMap dailyPlanResult = null;

            if (CategoryType.DAILYPLAN.equals(category)) {
                List<DBItemDailyPlanOrder> items = getDailyPlanOrders(in.getControllerId(), getDistinctOrderIds(orderIds));
                if (items != null && items.size() > 0) {
                    setSettings();

                    switch (action) {
                    case ModifyVariables:
                    case RemoveVariables:
                        dailyPlanResult = modifyVariables(in, items, auditlog, action);
                        break;
                    case ModifyStartTime:
                        dailyPlanResult = modifyStartTime(in, items, auditlog);
                        break;
                    }
                } else {
                    LOGGER.debug("0 orders found");
                }
            }

            // TODO is not only adhocCall.isLeft() dependent ...
            if (adhocCall.isLeft()) {
                return JOCDefaultResponse.responseStatus419(adhocCall.getLeft());
            } else {
                OrderIdMap200 entity = new OrderIdMap200();
                entity.setDeliveryDate(Date.from(Instant.now()));
                entity.setOrderIds(getResult(adhocCall.get(), dailyPlanResult));
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
            }

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private List<String> getDistinctOrderIds(List<String> orderIds) {
        Set<String> result = new HashSet<>();
        Set<String> cyclic = new HashSet<>();
        for (String orderId : orderIds) {
            if (OrdersHelper.isCyclicOrderId(orderId)) {
                String mainPart = OrdersHelper.getCyclicOrderIdMainPart(orderId);
                if (!cyclic.contains(mainPart)) {
                    cyclic.add(mainPart);
                    result.add(orderId);
                }
            } else {
                if (!result.contains(orderId)) {
                    result.add(orderId);
                }
            }
        }
        return result.stream().collect(Collectors.toList());
    }

    private OrderIdMap getResult(OrderIdMap adhocResult, OrderIdMap dailyPlanResult) {
        if (adhocResult == null && dailyPlanResult == null) {
            return new OrderIdMap();
        }
        if (adhocResult != null && dailyPlanResult == null) {
            return adhocResult;
        }
        if (adhocResult == null && dailyPlanResult != null) {
            return dailyPlanResult;
        }

        OrderIdMap result = new OrderIdMap();
        adhocResult.getAdditionalProperties().entrySet().stream().forEach(e -> {
            result.getAdditionalProperties().put(e.getKey(), e.getValue());
        });
        dailyPlanResult.getAdditionalProperties().entrySet().stream().forEach(e -> {
            result.getAdditionalProperties().put(e.getKey(), e.getValue());
        });
        return result;
    }

    private List<DBItemDailyPlanOrder> getDailyPlanOrders(String controllerId, List<String> orderIds) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            return new DBLayerDailyPlannedOrders(session).getDailyPlanOrders(controllerId, orderIds);
        } finally {
            Globals.disconnect(session);
        }
    }

    private Action getAction(DailyPlanModifyOrder in) {
        Action action = null;
        if (in.getVariables() != null && in.getVariables().getAdditionalProperties().size() > 0) {
            action = Action.ModifyVariables;
        }
        if (action == null) {
            if (in.getRemoveVariables() != null && in.getRemoveVariables().getAdditionalProperties().size() > 0) {
                action = Action.RemoveVariables;
            }
        }
        if (action == null) {
            if (in.getScheduledFor() != null || (in.getCycle() != null && in.getCycle().getRepeat() != null)) {
                action = Action.ModifyStartTime;
            }
        }
        if (action == null) {
            throw new JocMissingRequiredParameterException("variables, removeVariables, scheduledFor or cycle missing");
        }
        return action;
    }

    private OrderIdMap modifyVariables(DailyPlanModifyOrder in, List<DBItemDailyPlanOrder> items, DBItemJocAuditLog auditlog, Action action)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException, SOSHibernateException, IOException {

        OrderIdMap result = new OrderIdMap();

        List<DBItemDailyPlanOrder> submitted = new ArrayList<>();
        Map<Long, DBItemDailyPlanVariable> submittedVariables = new HashMap<>();
        Map<Long, List<DBItemDailyPlanOrder>> submittedCyclic = new HashMap<>();
        Map<Long, String> submittedCyclicNewParts = new HashMap<>();

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyVariables]");
            session.setAutoCommit(false);
            session.beginTransaction();

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            DBLayerOrderVariables ovDbLayer = new DBLayerOrderVariables(session);
            // modify planned and prepare submitted
            for (DBItemDailyPlanOrder item : items) {
                DBItemDailyPlanVariable variables = ovDbLayer.getOrderVariable(in.getControllerId(), item.getOrderId(), item.isCyclic());
                // skip when variable not exists and should be removed
                if (variables == null && Action.RemoveVariables.equals(action)) {
                    // not changed
                    result.getAdditionalProperties().put(item.getOrderId(), item.getOrderId());
                    continue;
                }
                // cyclic main order
                if (item.isCyclic()) {
                    if (item.getSubmitted()) {
                        // calculate new OrderId
                        String newPart = OrdersHelper.getUniqueOrderId();
                        submittedCyclicNewParts.put(item.getId(), newPart);
                        result.getAdditionalProperties().put(item.getOrderId(), OrdersHelper.getNewFromOldOrderId(item.getOrderId(), newPart));

                        // prepare to modify later
                        List<DBItemDailyPlanOrder> cyclic = dbLayer.getDailyPlanOrdersByCyclicMainPart(item.getControllerId(), OrdersHelper
                                .getCyclicOrderIdMainPart(item.getOrderId()));
                        submittedCyclic.put(item.getId(), cyclic);
                        submitted.addAll(cyclic);
                        submittedVariables.put(item.getId(), variables);
                    } else {
                        // not changed for planned order
                        result.getAdditionalProperties().put(item.getOrderId(), item.getOrderId());

                        // modify now
                        modifyVariables(in, action, variables, session, item.getOrderId());
                    }
                } else {// single start
                    if (item.getSubmitted()) {
                        // calculate new OrderId
                        result.getAdditionalProperties().put(item.getOrderId(), OrdersHelper.generateNewFromOldOrderId(item.getOrderId()));

                        // prepare to modify later
                        submitted.add(item);
                        submittedVariables.put(item.getId(), variables);
                    } else {
                        // not changed for planned order
                        result.getAdditionalProperties().put(item.getOrderId(), item.getOrderId());

                        // modify now
                        modifyVariables(in, action, variables, session, item.getOrderId());
                    }

                }
            }
            session.commit();
        } finally {
            Globals.disconnect(session);
        }

        if (submitted.size() > 0) {// submitted - single and all cyclic
            CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), submitted);
            c.thenAccept(either -> {
                SOSHibernateSession sessionNew = null;
                try {
                    sessionNew = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyVariables]");
                    sessionNew.setAutoCommit(false);

                    sessionNew.beginTransaction();

                    List<DBItemDailyPlanOrder> toSubmit = new ArrayList<>();

                    // single & main cyclic
                    for (DBItemDailyPlanOrder item : items) {
                        DBItemDailyPlanVariable variables = submittedVariables.get(item.getId());

                        String newOrderId = result.getAdditionalProperties().get(item.getOrderId());
                        modifyVariables(in, action, variables, sessionNew, newOrderId);

                        // main cyclic
                        if (item.isCyclic()) {
                            String newPart = submittedCyclicNewParts.get(item.getId());
                            List<DBItemDailyPlanOrder> cyclic = submittedCyclic.get(item.getId());
                            for (DBItemDailyPlanOrder cyclicItem : cyclic) {
                                cyclicItem.setSubmitted(false);
                                cyclicItem.setOrderId(OrdersHelper.getNewFromOldOrderId(cyclicItem.getOrderId(), newPart));
                                cyclicItem.setModified(new Date());
                                sessionNew.update(cyclicItem);

                                toSubmit.add(cyclicItem);
                            }
                        } else { // single start
                            item.setSubmitted(false);
                            item.setOrderId(newOrderId);
                            item.setModified(new Date());
                            sessionNew.update(item);

                            toSubmit.add(item);
                        }
                    }
                    sessionNew.commit();
                    sessionNew.close();
                    sessionNew = null;

                    submitOrdersToController(toSubmit);
                    notifyAndStoreAuditLogDetails(in, items, auditlog);
                } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                        | DBOpenSessionException | ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException
                        | SOSException | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                    Globals.rollback(sessionNew);
                    ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
                } finally {
                    Globals.disconnect(sessionNew);
                }
            });
        } else {
            notifyAndStoreAuditLogDetails(in, items, auditlog);
        }
        return result;
    }

    private void modifyVariables(DailyPlanModifyOrder in, Action action, DBItemDailyPlanVariable variables, SOSHibernateSession session,
            String orderId) throws SOSHibernateException, IOException {
        String current = variables == null ? null : variables.getVariableValue();
        String modified = null;
        switch (action) {
        case ModifyVariables:
            modified = updateVariables(current, in.getVariables());
            break;
        case RemoveVariables:
            if (variables != null) {
                modified = removeVariables(current, in.getRemoveVariables());
            }
            break;
        default:
            break;
        }
        if (modified != null) {
            if (variables == null) {
                variables = new DBItemDailyPlanVariable();
                variables.setControllerId(in.getControllerId());
                variables.setOrderId(orderId);
                variables.setVariableValue(modified);
                variables.setModified(new Date());
                variables.setCreated(new Date());
                session.save(variables);
            } else {
                variables.setOrderId(orderId);
                variables.setVariableValue(modified);
                variables.setModified(new Date());
                session.update(variables);
            }
        }
    }

    private void notifyAndStoreAuditLogDetails(DailyPlanModifyOrder in, List<DBItemDailyPlanOrder> items, DBItemJocAuditLog auditlog) {
        EventBus.getInstance().post(new DailyPlanEvent(in.getDailyPlanDate()));
        OrdersHelper.storeAuditLogDetails(items.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), in
                .getControllerId())).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(
                        either2, getAccessToken(), getJocError(), in.getControllerId()));
    }

    private OrderIdMap modifyStartTime(DailyPlanModifyOrder in, List<DBItemDailyPlanOrder> mainItems, DBItemJocAuditLog auditlog) throws Exception {
        OrderIdMap result = new OrderIdMap();

        if (isCyclicOrders(mainItems)) {
            if (in.getCycle() == null || in.getCycle().getRepeat() == null) {
                throw new JocMissingRequiredParameterException("Modify Start Time: missing cyclic definition.");
            }
            DBItemDailyPlanSubmission submission = insertNewSubmission(in.getControllerId(), mainItems.get(0).getDailyPlanDate(getSettings()
                    .getTimeZone()));
            for (DBItemDailyPlanOrder mainItem : mainItems) {
                String newOrderId = modifyStartTimeCycle(in, submission, mainItem, auditlog);
                result.getAdditionalProperties().put(mainItem.getOrderId(), newOrderId == null ? "" : newOrderId);
            }
        } else {
            if (in.getScheduledFor() == null) {
                throw new JocMissingRequiredParameterException("Modify Start Time: missing start time.");
            }

            Date now = JobSchedulerDate.nowInUtc();
            Date scheduledFor = now; // TODO set default ???
            Optional<Instant> scheduledForUtc = JobSchedulerDate.getScheduledForInUTC(in.getScheduledFor(), in.getTimeZone());
            if (scheduledForUtc.isPresent()) { // TODO error if not present ???
                scheduledFor = Date.from(scheduledForUtc.get());
            }
            if (now.getTime() > scheduledFor.getTime()) {
                TimeZone timeZone = in.getTimeZone() == null ? null : TimeZone.getTimeZone(in.getTimeZone());
                String current = SOSDate.format(now, SOSDate.DATETIME_FORMAT, timeZone);
                String planned = SOSDate.format(scheduledFor, SOSDate.DATETIME_FORMAT, timeZone);
                String add = timeZone == null ? "" : "(" + in.getTimeZone() + ")";
                throw new Exception(String.format("Current date time %s greater than Planned Start %s %s", current, planned, add));
            }

            // can have multiple items - of the same schedule or workflow
            result = modifyStartTimeSingle(in, mainItems, scheduledFor, auditlog);
        }
        return result;
    }

    private OrderIdMap modifyStartTimeSingle(DailyPlanModifyOrder in, List<DBItemDailyPlanOrder> items, Date scheduledFor, DBItemJocAuditLog auditlog)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException, SOSInvalidDataException {
        List<Long> submissionIds = items.stream().filter(SOSCollection.distinctByKey(DBItemDailyPlanOrder::getSubmissionHistoryId)).map(e -> {
            return e.getSubmissionHistoryId();
        }).collect(Collectors.toList());

        // calculate new orders id
        String dailyPlanDate = SOSDate.getDateAsString(scheduledFor);
        OrderIdMap result = new OrderIdMap();
        for (DBItemDailyPlanOrder item : items) {
            // not check if now > plannedStart of already submitted orders because of cyclic workflows
            // generate for not submitted too because maybe the daily plan day was changed - use new for all - same behaviour as for cyclic orders
            result.getAdditionalProperties().put(item.getOrderId(), OrdersHelper.generateNewFromOldOrderId(item.getOrderId(), dailyPlanDate));
        }

        CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(in.getControllerId(), items);
        c.thenAccept(either -> {
            SOSHibernateSession sessionNew = null;
            try {
                sessionNew = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeSingle]");
                sessionNew.setAutoCommit(false);

                DBItemDailyPlanSubmission submission = newSubmission(in.getControllerId(), scheduledFor);
                sessionNew.beginTransaction();
                sessionNew.save(submission);

                DBLayerOrderVariables ovDbLayer = new DBLayerOrderVariables(sessionNew);
                List<DBItemDailyPlanOrder> toSubmit = new ArrayList<>();
                for (DBItemDailyPlanOrder item : items) {
                    String oldOrderId = item.getOrderId();
                    String newOrderId = result.getAdditionalProperties().get(oldOrderId);

                    // update variables
                    ovDbLayer.update(item.getControllerId(), oldOrderId, newOrderId);

                    Long expectedDuration = item.getExpectedEnd().getTime() - item.getPlannedStart().getTime();
                    item.setExpectedEnd(new Date(expectedDuration + scheduledFor.getTime()));
                    item.setPlannedStart(scheduledFor);
                    item.setSubmissionHistoryId(submission.getId());
                    item.setModified(new Date());
                    item.setOrderId(newOrderId);

                    if (item.getSubmitted()) {
                        item.setSubmitted(false);

                        toSubmit.add(item);
                    }
                    sessionNew.update(item);
                }

                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(sessionNew);
                for (Long submissionId : submissionIds) {
                    deleteNotUsedSubmission(dbLayer, in.getControllerId(), submissionId);
                }
                sessionNew.commit();
                sessionNew.close();
                sessionNew = null;

                if (toSubmit.size() > 0) {
                    submitOrdersToController(toSubmit);
                }

                EventBus.getInstance().post(new DailyPlanEvent(in.getDailyPlanDate()));

                OrdersHelper.storeAuditLogDetails(items.stream().map(item -> new AuditLogDetail(item.getWorkflowPath(), item.getOrderId(), in
                        .getControllerId())).collect(Collectors.toSet()), auditlog.getId()).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), in.getControllerId()));

            } catch (IOException | DBConnectionRefusedException | DBInvalidDataException | DBMissingDataException | JocConfigurationException
                    | DBOpenSessionException | ControllerConnectionResetException | ControllerConnectionRefusedException | ParseException
                    | SOSException | URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                Globals.rollback(sessionNew);
                ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), in.getControllerId());
            } finally {
                Globals.disconnect(sessionNew);
            }
        });
        return result;
    }

    private String modifyStartTimeCycle(DailyPlanModifyOrder in, DBItemDailyPlanSubmission newSubmission, DBItemDailyPlanOrder mainItem,
            DBItemJocAuditLog auditlog) throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException, InterruptedException {
        Long oldSubmissionId = mainItem.getSubmissionHistoryId();

        SOSHibernateSession session = null;
        Map<PlannedOrderKey, PlannedOrder> generatedOrders = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeCycle][" + mainItem.getOrderId() + "]");
            session.setAutoCommit(false);

            // get variables
            DBItemDailyPlanVariable variable = new DBLayerOrderVariables(session).getOrderVariable(mainItem.getControllerId(), mainItem.getOrderId(),
                    true);

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            // remove not submitted
            session.beginTransaction();
            dbLayer.deleteCascading(mainItem, false);
            session.commit();

            // get submitted
            List<DBItemDailyPlanOrder> submitted = dbLayer.getDailyPlanOrdersByCyclicMainPart(mainItem.getControllerId(), OrdersHelper
                    .getCyclicOrderIdMainPart(mainItem.getOrderId()), true);

            if (submitted != null && submitted.size() > 0) {
                session.close();
                session = null;

                CompletableFuture<Either<Problem, Void>> c = OrdersHelper.removeFromJobSchedulerController(mainItem.getControllerId(), submitted);
                c.thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), mainItem.getControllerId());
                    if (either.isRight()) {
                        // remove submitted & old submission
                        SOSHibernateSession sessionNew = null;
                        try {
                            sessionNew = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[modifyStartTimeCycle][removeSubmitted]");
                            sessionNew.setAutoCommit(false);
                            sessionNew.beginTransaction();
                            DBLayerDailyPlannedOrders dbLayerNew = new DBLayerDailyPlannedOrders(sessionNew);
                            dbLayerNew.deleteCascading(mainItem, true);
                            deleteNotUsedSubmission(dbLayerNew, mainItem.getControllerId(), oldSubmissionId);
                            sessionNew.commit();
                        } catch (SOSHibernateException e) {
                            Globals.rollback(sessionNew);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), mainItem.getControllerId());
                        } finally {
                            Globals.disconnect(sessionNew);
                        }
                        // can't returns result ...
                        recreateCyclicOrder(in, newSubmission, mainItem, variable, auditlog);
                    }
                });
            } else {
                // remove old submission
                session.beginTransaction();
                deleteNotUsedSubmission(dbLayer, mainItem.getControllerId(), oldSubmissionId);
                session.commit();
                session.close();
                session = null;

                // generate orders
                generatedOrders = recreateCyclicOrder(in, newSubmission, mainItem, variable, auditlog);
            }
        } finally {
            Globals.disconnect(session);
        }

        String newOrderId = null;
        if (generatedOrders != null && generatedOrders.size() > 0) {
            Optional<Map.Entry<PlannedOrderKey, PlannedOrder>> first = generatedOrders.entrySet().stream().findFirst();
            if (first.isPresent()) {
                newOrderId = first.get().getValue().getFreshOrder().getId();
            }
        }
        return newOrderId;
    }

    private void deleteNotUsedSubmission(DBLayerDailyPlannedOrders dbLayer, String controllerId, Long submissionId) throws SOSHibernateException {
        if (submissionId != null) {
            Long count = dbLayer.getCountOrdersBySubmissionId(controllerId, submissionId);
            if (count.equals(0L)) {
                dbLayer.deleteSubmission(submissionId);
            }
        }
    }

    private boolean isCyclicOrders(List<DBItemDailyPlanOrder> items) throws Exception {
        boolean hasSingle = false;
        boolean hasCyclic = false;
        for (DBItemDailyPlanOrder item : items) {
            if (hasSingle && hasCyclic) {
                break;
            }
            if (OrdersHelper.isCyclicOrderId(item.getOrderId())) {
                hasCyclic = true;
            } else {
                hasSingle = true;
            }
        }
        if (hasSingle && hasCyclic) {
            throw new Exception("Modify Start Time operation is not allowed. Single and Cyclic orders detected.");
        }
        return hasCyclic;
    }

    private Map<PlannedOrderKey, PlannedOrder> recreateCyclicOrder(DailyPlanModifyOrder in, DBItemDailyPlanSubmission newSubmission,
            final DBItemDailyPlanOrder mainItem, DBItemDailyPlanVariable variable, DBItemJocAuditLog auditlog) {
        String controllerId = in.getControllerId();
        String dDate = in.getDailyPlanDate();
        if (dDate == null) {
            dDate = OrdersHelper.getDateFromOrderId(mainItem.getOrderId());
        }

        LOGGER.debug("recreateCyclicOrder: main orderId=" + mainItem.getOrderId());

        Map<PlannedOrderKey, PlannedOrder> generatedOrders = null;
        try {
            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
            settings.setOverwrite(false);
            settings.setSubmit(mainItem.getSubmitted());
            settings.setTimeZone(getSettings().getTimeZone());
            settings.setPeriodBegin(getSettings().getPeriodBegin());
            settings.setDailyPlanDate(SOSDate.getDate(dDate));
            settings.setSubmissionTime(new Date());

            Schedule schedule = new Schedule();
            schedule.setVersion("");
            schedule.setPath(mainItem.getSchedulePath());
            schedule.setWorkflowName(mainItem.getWorkflowName());
            schedule.setWorkflowPath(mainItem.getWorkflowPath());
            schedule.setTitle("");
            schedule.setDocumentationName("");
            schedule.setSubmitOrderToControllerWhenPlanned(mainItem.getSubmitted());
            schedule.setPlanOrderAutomatically(true);
            schedule.setVariableSets(new ArrayList<VariableSet>());
            VariableSet variableSet = new VariableSet();
            variableSet.setOrderName(mainItem.getOrderName());
            Variables variables = new Variables();
            if (variable != null && variable.getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(variable.getVariableValue(), Variables.class);
            }
            variableSet.setVariables(variables);
            if (variableSet.getVariables().getAdditionalProperties().size() > 0) {
                schedule.getVariableSets().add(variableSet);
            }

            schedule.setCalendars(new ArrayList<AssignedCalendars>());
            AssignedCalendars calendars = new AssignedCalendars();
            Calendar calendar = getCalendarById(mainItem.getCalendarId());
            calendars.setCalendarName(calendar.getName());
            calendars.setPeriods(new ArrayList<Period>());
            calendars.setTimeZone(in.getTimeZone());
            Period period = new Period();
            period.setBegin(in.getCycle().getBegin());
            period.setEnd(in.getCycle().getEnd());
            period.setRepeat(in.getCycle().getRepeat());
            calendars.getPeriods().add(period);
            schedule.getCalendars().add(calendars);

            DailyPlanRunner runner = new DailyPlanRunner(settings);
            generatedOrders = runner.generateDailyPlan(StartupMode.manual, controllerId, Collections.singletonList(schedule), mainItem
                    .getDailyPlanDate(settings.getTimeZone()), newSubmission, mainItem.getSubmitted(), getJocError(), getAccessToken());

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
        return generatedOrders;
    }

    private Calendar getCalendarById(Long id) throws JsonParseException, JsonMappingException, SOSHibernateException, IOException {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[getCalendarById=" + id + "]");
            DBLayerReleasedConfigurations dbLayer = new DBLayerReleasedConfigurations(session);
            DBItemInventoryReleasedConfiguration config = dbLayer.getReleasedConfiguration(id);
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

    private void submitOrdersToController(List<DBItemDailyPlanOrder> items) throws JsonParseException, JsonMappingException,
            DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, IOException, ParseException, SOSException, URISyntaxException,
            InterruptedException, ExecutionException, TimeoutException {

        LOGGER.debug("submitOrdersToController: size=" + items.size());

        if (items.size() > 0) {
            DailyPlanSettings settings = new DailyPlanSettings();
            settings.setUserAccount(this.getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
            settings.setOverwrite(false);
            settings.setSubmit(true);
            settings.setTimeZone(getSettings().getTimeZone());
            settings.setPeriodBegin(getSettings().getPeriodBegin());

            DailyPlanRunner runner = new DailyPlanRunner(settings);
            runner.submitOrders(StartupMode.manual, items.get(0).getControllerId(), items, null, getJocError(), getAccessToken());
        }
    }

    private DBItemDailyPlanSubmission insertNewSubmission(String controllerId, String dailyPlanDate) throws SOSHibernateException,
            SOSInvalidDataException {
        DBItemDailyPlanSubmission item = newSubmission(controllerId, dailyPlanDate);
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "[insertNewSubmission][" + dailyPlanDate + "]");
            session.beginTransaction();
            session.save(item);
            session.commit();
        } finally {
            Globals.disconnect(session);
        }
        return item;
    }

    private DBItemDailyPlanSubmission newSubmission(String controllerId, Date scheduleFor) throws SOSInvalidDataException {
        return newSubmission(controllerId, SOSDate.format(scheduleFor, SOSDate.DATE_FORMAT));
    }

    private DBItemDailyPlanSubmission newSubmission(String controllerId, String dailyPlanDate) throws SOSInvalidDataException {
        DBItemDailyPlanSubmission item = new DBItemDailyPlanSubmission();
        item.setControllerId(controllerId);
        item.setSubmissionForDate(SOSDate.parse(dailyPlanDate, SOSDate.DATE_FORMAT));
        item.setUserAccount(getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname());
        item.setCreated(new Date());
        return item;
    }

    private String updateVariables(String current, Variables toUpdate) throws IOException {
        Variables vars = new Variables();
        if (!SOSString.isEmpty(current)) {
            try {
                vars = Globals.objectMapper.readValue(current, Variables.class);
            } catch (Throwable e) {
                LOGGER.warn("Illegal value " + current);
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

        for (Entry<String, Object> variable : vars.getAdditionalProperties().entrySet()) {
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

        for (Entry<String, Object> variable : toUpdate.getAdditionalProperties().entrySet()) {
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
        vars.setAdditionalProperties(map);
        return Globals.objectMapper.writeValueAsString(vars);
    }

    private String removeVariables(String current, Variables toRemove) throws IOException {
        if (SOSString.isEmpty(current)) {
            return Globals.objectMapper.writeValueAsString(new Variables());
        }

        Variables vars = Globals.objectMapper.readValue(current, Variables.class);
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry<String, Object> variable : vars.getAdditionalProperties().entrySet()) {
            if (toRemove.getAdditionalProperties().get(variable.getKey()) == null) {
                map.put(variable.getKey(), variable.getValue());
            }
        }
        vars.getAdditionalProperties().clear();
        vars.setAdditionalProperties(map);
        return Globals.objectMapper.writeValueAsString(vars);
    }

}
