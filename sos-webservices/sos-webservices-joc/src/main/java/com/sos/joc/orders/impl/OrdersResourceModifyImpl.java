package com.sos.joc.orders.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.common.Outcome;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.CheckedOrdersPositions;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.CancelDailyPlanOrders;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.Positions;
import com.sos.joc.orders.resource.IOrdersResourceModify;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.command.JSuspensionMode;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JHistoricOutcome;
import js7.data_for_java.order.JHistoryOperation;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import js7.proxy.javaapi.JControllerApi;
import scala.Function1;
import scala.collection.JavaConverters;

@Path("orders")
public class OrdersResourceModifyImpl extends JOCResourceImpl implements IOrdersResourceModify {

    private static final String API_CALL = "./orders";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceModifyImpl.class);

    private enum Action {
        CANCEL, CANCEL_DAILYPLAN, SUSPEND, RESUME, REMOVE_WHEN_TERMINATED, ANSWER_PROMPT
    }

    @Override
    public JOCDefaultResponse postOrdersSuspend(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.SUSPEND, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.SUSPEND, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersResume(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.RESUME, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postResumeOrders(modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersCancel(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.CANCEL, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.CANCEL, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersConfirm(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.ANSWER_PROMPT, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.ANSWER_PROMPT, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersDailyPlanCancel(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/dailyplan/cancel", filterBytes, accessToken);
            JsonValidator.validate(filterBytes, CancelDailyPlanOrders.class);
            CancelDailyPlanOrders cancelDailyPlanOrders = Globals.objectMapper.readValue(filterBytes, CancelDailyPlanOrders.class);

            boolean perm = getControllerPermissions(cancelDailyPlanOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(cancelDailyPlanOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            addSubmittedOrderIdsFromDailyplanDate(cancelDailyPlanOrders);

            ModifyOrders modifyOrders = new ModifyOrders();
            modifyOrders.setControllerId(cancelDailyPlanOrders.getControllerId());
            modifyOrders.setKill(false);
            modifyOrders.setOrderIds(cancelDailyPlanOrders.getOrderIds());
            modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);
            modifyOrders.setAuditLog(cancelDailyPlanOrders.getAuditLog());

            postOrdersModify(Action.CANCEL_DAILYPLAN, modifyOrders);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersRemoveWhenTerminated(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.REMOVE_WHEN_TERMINATED, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getView();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.REMOVE_WHEN_TERMINATED, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public void postOrdersModify(Action action, ModifyOrders modifyOrders) throws Exception {
        CategoryType category = CategoryType.CONTROLLER;
        if (Action.CANCEL_DAILYPLAN.equals(action)) {
            category = CategoryType.DAILYPLAN;
        }
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), modifyOrders.getControllerId(), category);

        Set<String> orders = modifyOrders.getOrderIds();
        List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
        boolean withOrders = orders != null && !orders.isEmpty();

        String controllerId = modifyOrders.getControllerId();
        JControllerState currentState = Proxy.of(controllerId).currentState();
        Stream<JOrder> orderStream = Stream.empty();

        if (withOrders) {
            orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
            // determine possibly fresh cyclic orders in case of CANCEL
            if (Action.CANCEL.equals(action) || Action.CANCEL_DAILYPLAN.equals(action)) {
                // determine cyclic ids
                orderStream = Stream.concat(orderStream, cyclicFreshOrderIds(orders, currentState));
            }
        } else if (workflowIds != null && !workflowIds.isEmpty()) {
            Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
            Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                    .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
            Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(w -> !versionNotEmpty.test(w)).map(w -> WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).collect(Collectors.toSet());
            Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                    .workflowId().path()));
            orderStream = currentState.ordersBy(workflowFilter);
        }

        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, folderPermissions.getListOfFolders(), withOrders);

        if (!jOrders.isEmpty() || Action.CANCEL_DAILYPLAN.equals(action)) {
            command(currentState, action, modifyOrders, dbAuditLog, jOrders.stream().map(JOrder::id).collect(Collectors.toSet())).thenAccept(
                    either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId()).thenAccept(either2 -> ProblemHelper
                                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else {
            throwControllerObjectNotExistException(action);
        }
    }
    
    public void postResumeOrders(ModifyOrders modifyOrders) throws Exception {
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), modifyOrders.getControllerId(), CategoryType.CONTROLLER);

        Set<String> orders = modifyOrders.getOrderIds();
        checkRequiredParameter("orderIds", orders);
        
        Optional<JPosition> positionOpt = Optional.empty();
        if (modifyOrders.getPosition() != null && !modifyOrders.getPosition().isEmpty()) {
            Either<Problem, JPosition> posFromList = JPosition.fromList(modifyOrders.getPosition());
            if (posFromList.isLeft()) {
                ProblemHelper.throwProblemIfExist(posFromList);
            }
            positionOpt = Optional.of(posFromList.get());
        }
        
        boolean withVariables = modifyOrders.getVariables() != null && modifyOrders.getVariables().getAdditionalProperties() != null
                && !modifyOrders.getVariables().getAdditionalProperties().isEmpty();
        
        String controllerId = modifyOrders.getControllerId();
        JControllerState currentState = Proxy.of(controllerId).currentState();
        CheckedOrdersPositions cop = new CheckedOrdersPositions().get(orders, currentState, folderPermissions.getListOfFolders());
        final Set<JOrder> jOrders = cop.getJOrders();
        List<JHistoryOperation> historyOperations = Collections.emptyList();
        Set<String> allowedPositions = cop.getPositions().stream().map(Positions::getPositionString).collect(Collectors.toCollection(LinkedHashSet::new));
        
        if (positionOpt.isPresent()) {
            if (!cop.isSingleOrder() && cop.getDisabledPositionChange() != null) {
                throw new JocBadRequestException(cop.getDisabledPositionChange().getMessage());
            }

            if (!allowedPositions.contains(positionOpt.get().toString())) {
                if (cop.isSingleOrder() && cop.getCurrentPosition().toString().equals(positionOpt.get().toString())) {
                    positionOpt = Optional.empty();
                } else {
                    throw new JocBadRequestException("Disallowed position '" + positionOpt.get().toString() + "'. Allowed positions are: "
                            + allowedPositions.toString());
                }
            }
        }
        
        if (withVariables) {
            if (!cop.isSingleOrder()) {
                throw new JocBadRequestException("Variables can only be set for resuming a single order.");
            } else if (cop.getVariablesNotSettable() == Boolean.TRUE) {
                throw new JocBadRequestException("Variables can only be set if the order starts from the beginning in its scope.");
            }
        }
        
        if (!positionOpt.isPresent() && !withVariables) {
            command(currentState, Action.RESUME, modifyOrders, dbAuditLog, jOrders.stream().map(JOrder::id).collect(Collectors.toSet())).thenAccept(
                    either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId()).thenAccept(either2 -> ProblemHelper
                                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else if (cop.isSingleOrder()) {
            
            if (withVariables) {
                Set<String> allowedPositionsWithImplicitEnds = cop.getPositionsWithImplicitEnds().stream().map(Positions::getPositionString).collect(
                        Collectors.toCollection(LinkedHashSet::new));
                final String positionString = positionOpt.isPresent() ? positionOpt.get().toString() : "";
                boolean isNotFuturePosition = true;
                if (positionOpt.isPresent()) {
                    int posIndex = getIndex(allowedPositionsWithImplicitEnds, positionString);
                    int curPosIndex = getIndex(allowedPositionsWithImplicitEnds, cop.getCurrentPosition().toString());
                    isNotFuturePosition = posIndex <= curPosIndex;
                }
                
                // TODO for the time being: quick and dirty solution by replacing historicOutcome of previous allowed position
                List<Object> prevPos = null;
                String prevPosString = null;
                if (isNotFuturePosition) {
                    JOrder currentJOrder = jOrders.iterator().next();
                    Set<String> historicPositions = JavaConverters.asJava(currentJOrder.asScala().historicOutcomes()).stream().map(h -> JPosition
                            .apply(h.position())).map(p -> p.toString()).collect(Collectors.toCollection(LinkedHashSet::new));
                    Positions prevP = getPrevious(historicPositions, cop.getPositionsWithImplicitEnds(), positionString);
                    if (prevP != null) {
                        prevPos = prevP.getPosition();
                        prevPosString = prevP.getPositionString();
                    }
                } else {
                    prevPos = cop.getCurrentPosition().toList();
                    prevPosString = cop.getCurrentPosition().toString();
                }
                if (prevPos != null) {
                    final String prevPString = prevPosString;
                    Optional<HistoricOutcome> hoOpt = cop.getHistoricOutcomes().stream().filter(ho -> JPosition.fromList(ho.getPosition()).get()
                            .toString().equals(prevPString)).findFirst();
                    if (hoOpt.isPresent()) {
                        HistoricOutcome h = hoOpt.get();
                        Variables v = h.getOutcome().getNamedValues();
                        if (v == null) {
                            v = new Variables();
                        }
                        v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                        h.getOutcome().setNamedValues(v);
                        
                        String json = Globals.objectMapper.writeValueAsString(h);
                        JHistoricOutcome jH = JHistoricOutcome.fromJson(json).get();
                        historyOperations = Collections.singletonList(JHistoryOperation.replace(jH.asScala()));
                    } else {
                        Variables v = new Variables();
                        v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                        HistoricOutcome h = new HistoricOutcome(prevPos, new Outcome("Succeeded", v, null)); 
                        
                        String json = Globals.objectMapper.writeValueAsString(h);
                        JHistoricOutcome jH = JHistoricOutcome.fromJson(json).get();
                        historyOperations = Collections.singletonList(JHistoryOperation.insert(JPosition.fromList(prevPos).get(), jH.asScala()));
                    }
                }
            }
            
            ControllerApi.of(controllerId).resumeOrder(jOrders.iterator().next().id(), positionOpt, historyOperations).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                if (either.isRight()) {
                    OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId()).thenAccept(either2 -> ProblemHelper
                            .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                }
            });
        } else {
            for (JOrder jOrder : jOrders) {
                ControllerApi.of(controllerId).resumeOrder(jOrder.id(), positionOpt, historyOperations).thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                    if (either.isRight()) {
                        OrdersHelper.storeAuditLogDetailsFromJOrder(jOrder, dbAuditLog.getId()).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                    }
                });
            }
        }
        
        if (cop.hasNotSuspendedOrFailedOrders()) {
            String msg = cop.getNotSuspendedOrFailedOrdersMessage();
            ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
        }
    }
    
    private static int getIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry : set) {
            if (entry.equals(value)) {
                return result;
            }
            result++;
        }
        return result;
    }
    
    private static Positions getPrevious(Set<String> historicPositions, Set<Positions> allowedPositions, String value) {
        Positions result = null;
        for (Positions entry : allowedPositions) {
            if (entry.getPositionString().equals(value)) {
                break;
            }
            if (historicPositions.contains(entry.getPositionString())) {
                result = entry;
            }
        }
        return result;
    }

//    private static List<HistoricOutcome> subList(List<HistoricOutcome> hOutcomes, String positionString, String curPositionString,
//            Set<String> allowedPositions) {
//        if (hOutcomes.isEmpty()) {
//            return hOutcomes;
//        }
//        String posString = positionString;
//        if (posString == null || posString.isEmpty()) {
//            posString = curPositionString;
//        }
//        List<HistoricOutcome> subList = new ArrayList<>();
//        for (HistoricOutcome hOutcome : hOutcomes) {
//            String hOutcomePos = JPosition.fromList(hOutcome.getPosition()).get().toString();
//            if (hOutcomePos.equals(posString)) {
//                break;
//            }
//            if (!allowedPositions.contains(hOutcomePos)) {
//                continue;
//            }
//            subList.add(hOutcome);
//        }
//        return subList;
//    }

    private void throwControllerObjectNotExistException(Action action) throws ControllerObjectNotExistException {
        switch (action) {
        case RESUME:
            throw new ControllerObjectNotExistException("No failed or suspended orders found.");
        case ANSWER_PROMPT:
            throw new ControllerObjectNotExistException("No prompting orders found.");
        default:
            throw new ControllerObjectNotExistException("No orders found.");
        }

    }

    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, boolean withPostProblem) {
        switch (action) {
        case RESUME:
            Map<Boolean, Set<JOrder>> suspendedOrFailedOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isSuspendedOrFailed(o),
                    Collectors.toSet()));
            if (suspendedOrFailedOrders.containsKey(Boolean.FALSE)) {
                String msg = suspendedOrFailedOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '",
                        "Orders '", "' not failed or suspended"));
                if (withPostProblem) {
                    ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                }
            }
            return suspendedOrFailedOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case ANSWER_PROMPT:
            Map<Boolean, Set<JOrder>> promptingOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isPrompting(o), Collectors
                    .toSet()));
            if (promptingOrders.containsKey(Boolean.FALSE)) {
                String msg = promptingOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '", "Orders '",
                        "' not prompting"));
                if (withPostProblem) {
                    ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                }
            }
            return promptingOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case CANCEL_DAILYPLAN:
            Map<Boolean, Set<JOrder>> freshOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isPendingOrScheduledOrBlocked(o),
                    Collectors.toSet()));
            if (freshOrders.containsKey(Boolean.FALSE)) {
                String msg = freshOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '", "Orders '",
                        "' not pending, scheduled or blocked"));
                if (withPostProblem) {
                    ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                }
            }
            return freshOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        default:
            return orderStream.collect(Collectors.toSet());
        }
    }

    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, Set<Folder> permittedFolders,
            boolean withPostProblem) {
        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, withPostProblem);
        return jOrders.stream().filter(o -> canAdd(WorkflowPaths.getPath(o.workflowId()), permittedFolders)).collect(Collectors.toSet());
    }

    private static Stream<JOrder> cyclicFreshOrderIds(Collection<String> orderIds, JControllerState currentState) {
        Stream<JOrder> cyclicOrderStream = Stream.empty();
        // determine cyclic ids
        Set<String> freshCyclicIds = orderIds.stream().filter(s -> s.matches(".*#C[0-9]+-.*")).map(s -> currentState.idToOrder(OrderId.of(s))).filter(
                Optional::isPresent).map(Optional::get).filter(o -> Order.Fresh$.class.isInstance(o.asScala().state())).map(o -> o.id().string()
                        .substring(0, 24)).collect(Collectors.toSet());
        if (!freshCyclicIds.isEmpty()) {
            cyclicOrderStream = currentState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> freshCyclicIds
                    .contains(o.id().string().substring(0, 24))));
        }
        return cyclicOrderStream;
    }

    private CompletableFuture<Either<Problem, Void>> command(JControllerState currentState, Action action, ModifyOrders modifyOrders,
            DBItemJocAuditLog dbAuditLog, Set<OrderId> oIds) throws SOSHibernateException {

        String controllerId = modifyOrders.getControllerId();

        switch (action) {
        case CANCEL_DAILYPLAN:
            if (modifyOrders.getOrderIds() != null) {
                Set<String> orders = modifyOrders.getOrderIds().stream().filter(s -> !s.matches(".*#(T|F)[0-9]+-.*")).collect(Collectors.toSet());
                orders.removeAll(oIds.stream().map(OrderId::string).collect(Collectors.toSet()));

                updateDailyPlan(orders);
            }

            if (oIds.isEmpty()) {
                return CompletableFuture.supplyAsync(() -> Either.right(null));
            } else {
                return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                    if (either.isRight()) {
                        updateDailyPlan(oIds, controllerId);
                    }
                    return either;
                });
            }

        case CANCEL:
            return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                // TODO @uwe: This update must be removed when dailyplan service receives events for order state changes
                if (either.isRight()) {
                    updateDailyPlan(oIds, controllerId);
                }
                return either;
            });

        case RESUME:
            return ControllerApi.of(controllerId).resumeOrders(oIds);

        case SUSPEND:
            if (modifyOrders.getKill() == Boolean.TRUE) {
                return ControllerApi.of(controllerId).suspendOrders(oIds, JSuspensionMode.kill());
            } else {
                return ControllerApi.of(controllerId).suspendOrders(oIds);
            }

        case ANSWER_PROMPT:
            // No bulk operation in API
            JControllerApi api = ControllerApi.of(controllerId);
            oIds.stream().map(oId -> JControllerCommand.apply(new ControllerCommand.AnswerOrderPrompt(oId))).forEach(command -> api.executeCommand(
                    command).thenAccept(either -> ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId)));
            return CompletableFuture.supplyAsync(() -> Either.right(null));

        default: // case REMOVE_WHEN_TERMINATED
            return ControllerApi.of(controllerId).deleteOrdersWhenTerminated(oIds);
        }
    }

    private void updateDailyPlan(Set<OrderId> oIds, String controllerId) {
        try {
            // only for non-temporary and non-file orders
            LOGGER.debug("Cancel orders. Calling updateDailyPlan");
            updateDailyPlan(oIds.stream().map(OrderId::string).filter(s -> !s.matches(".*#(T|F)[0-9]+-.*")).collect(Collectors.toSet()));
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        }
    }

    private static void updateDailyPlan(Collection<String> orderIds) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        if (!orderIds.isEmpty()) {
            try {
                GlobalSettingsReader reader = new GlobalSettingsReader();
                OrderInitiatorSettings settings;
                if (Globals.configurationGlobals != null) {

                    AConfigurationSection configuration = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
                    settings = reader.getSettings(configuration);
                } else {
                    settings = new OrderInitiatorSettings();
                    settings.setTimeZone("Europe/Berlin");
                    settings.setPeriodBegin("00:00");
                }
                
                
                
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL + "/cancel");
                sosHibernateSession.setAutoCommit(false);
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setListOfOrders(orderIds);
                filter.setSubmitted(false);
                dbLayerDailyPlannedOrders.setSubmitted(filter);
                Globals.commit(sosHibernateSession);
                List<DBItemDailyPlanOrders> listOfOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                Set<String> dailyPlanDays = new HashSet<String>();
                for (DBItemDailyPlanOrders order : listOfOrders) {
                    String dailyPlanDate = order.getDailyPlanDate(settings.getTimeZone());
                    if (!dailyPlanDays.contains(dailyPlanDate)) {
                        dailyPlanDays.add(dailyPlanDate);
                        EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));
                    }
                }

            } catch (Exception e) {
                Globals.rollback(sosHibernateSession);
                throw e;
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        } else {
            LOGGER.debug("No orderIds to be updated in daily plan");
        }
    }

    private ModifyOrders initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyOrders.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
    }

    private void addSubmittedOrderIdsFromDailyplanDate(CancelDailyPlanOrders cancelDailyPlanOrders) throws Exception {
        if (cancelDailyPlanOrders.getDailyPlanDate() != null) {
            SOSHibernateSession sosHibernateSession = null;
            if (cancelDailyPlanOrders.getOrderIds() == null) {
                cancelDailyPlanOrders.setOrderIds(new LinkedHashSet<String>());
            }

            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                sosHibernateSession.setAutoCommit(false);
                GlobalSettingsReader reader = new GlobalSettingsReader();
                OrderInitiatorSettings settings;
                if (Globals.configurationGlobals != null) {

                    AConfigurationSection configuration = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
                    settings = reader.getSettings(configuration);
                } else {
                    settings = new OrderInitiatorSettings();
                    settings.setTimeZone("Europe/Berlin");
                    settings.setPeriodBegin("00:00");
                }
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setControllerId(cancelDailyPlanOrders.getControllerId());
                filter.setDailyPlanDate(cancelDailyPlanOrders.getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
                filter.setSubmitted(true);

                List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                if (listOfPlannedOrders != null) {
                    cancelDailyPlanOrders.getOrderIds().addAll(listOfPlannedOrders.stream().map(DBItemDailyPlanOrders::getOrderId).collect(Collectors
                            .toSet()));
                }
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

}