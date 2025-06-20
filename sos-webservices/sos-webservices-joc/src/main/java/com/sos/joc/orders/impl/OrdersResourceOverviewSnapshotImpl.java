package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import scala.Function1;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";

    @Override
    public JOCDefaultResponse postOrdersOverviewSnapshot(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV body = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(body.getControllerId(), getBasicControllerPermissions(body.getControllerId(),
                    accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean withWorkFlowFilter = body.getWorkflowIds() != null && !body.getWorkflowIds().isEmpty();
            boolean withFolderFilter = body.getFolders() != null && !body.getFolders().isEmpty();
            Set<Folder> permittedFolders = addPermittedFolder(body.getFolders());

            JControllerState controllerState = Proxy.of(body.getControllerId()).currentState();
            Set<VersionedItemId<WorkflowPath>> checkedWorkflows = checkFolderPermission(controllerState, body.getWorkflowIds(), permittedFolders);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(getSnapshot(controllerState, body, checkedWorkflows, permittedFolders,
                    withWorkFlowFilter, withFolderFilter)));

        } catch (ControllerConnectionResetException e) {
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }

    }

    private static OrdersSnapshot getSnapshot(JControllerState controllerState, OrdersFilterV body, Set<VersionedItemId<WorkflowPath>> workflowIds,
            Set<Folder> permittedFolders, boolean withWorkFlowFilter, boolean withFolderFilter) throws ControllerConnectionResetException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {

        OrdersSummary summary = new OrdersSummary();
        summary.setBlocked(0);
        summary.setPending(0);
        summary.setInProgress(0);
        summary.setRunning(0);
        summary.setFailed(0);
        summary.setSuspended(0);
        summary.setWaiting(0);
        summary.setTerminated(0);
        summary.setPrompting(0);

        OrdersSnapshot entity = new OrdersSnapshot();

        try {
            final Instant now = controllerState.instant();
            Map<Class<? extends Order.State>, Integer> orderStates = null;
            int suspendedOrders = 0;
            Stream<JOrder> freshOrders = null;

            Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                    Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                            Order.ProcessingKilled$.class));
            Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
            Function1<Order<Order.State>, Object> notSuspendFilter = JOrderPredicates.not(suspendFilter);
            Function<JOrder, String> collapseCyclicOrders = o -> OrdersHelper.isFresh(o) ? OrdersHelper.getCyclicOrderIdMainPart(o.id().string()) : o
                    .id().string();

            if (withWorkFlowFilter) {
                if (!workflowIds.isEmpty()) {
                    orderStates = controllerState.orderStateToCount(JOrderPredicates.and(notSuspendFilter, o -> workflowIds.contains(o
                            .workflowId())));
                    if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
                        freshOrders = controllerState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class),
                                JOrderPredicates.and(notSuspendFilter, o -> workflowIds.contains(o.workflowId()))));
                    }
                    suspendedOrders = controllerState.ordersBy(JOrderPredicates.and(suspendFilter, o -> workflowIds.contains(o.workflowId()))).map(
                            collapseCyclicOrders).distinct().mapToInt(e -> 1).sum();
                } else {
                    // no folder permissions
                    orderStates = Collections.emptyMap();
                }
            } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
                // no permission
            } else if (permittedFolders != null && !permittedFolders.isEmpty()) {
//                Set<VersionedItemId<WorkflowPath>> workflowIds2 = WorkflowsHelper.getWorkflowIdsFromFolders(body.getControllerId(), permittedFolders
//                        .stream().collect(Collectors.toList()), controllerState, permittedFolders);
//                if (!workflowIds2.isEmpty()) {
//                    orderStates = controllerState.orderStateToCount(JOrderPredicates.and(notSuspendFilter, o -> workflowIds2.contains(o
//                            .workflowId())));
                    
                    orderStates = controllerState.orderStateToCount(JOrderPredicates.and(notSuspendFilter, o -> canAdd(WorkflowPaths.getPath(o
                            .workflowId().path().string()), permittedFolders)));
                    
                    if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
//                        freshOrders = controllerState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class),
//                                JOrderPredicates.and(notSuspendFilter, o -> workflowIds2.contains(o.workflowId()))));
                        
                        freshOrders = controllerState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class),
                                JOrderPredicates.and(notSuspendFilter, o -> canAdd(WorkflowPaths.getPath(o
                                        .workflowId().path().string()), permittedFolders))));
                    }
//                    suspendedOrders = controllerState.ordersBy(JOrderPredicates.and(suspendFilter, o -> workflowIds2.contains(o.workflowId()))).map(
//                            collapseCyclicOrders).distinct().mapToInt(e -> 1).sum();
                    
                    suspendedOrders = controllerState.ordersBy(JOrderPredicates.and(suspendFilter, o -> canAdd(WorkflowPaths.getPath(o
                            .workflowId().path().string()), permittedFolders))).map(
                            collapseCyclicOrders).distinct().mapToInt(e -> 1).sum();
//                } else {
//                    // no folder permissions
//                    orderStates = Collections.emptyMap();
//                }
            } else {
                orderStates = controllerState.orderStateToCount(notSuspendFilter);
                if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
                    freshOrders = controllerState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class), notSuspendFilter));
                }
                suspendedOrders = controllerState.ordersBy(suspendFilter).map(collapseCyclicOrders).distinct().mapToInt(e -> 1).sum();
            }

            int numOfBlockedOrders = 0;
            int numOfPendingOrders = 0;
            int numOfFreshOrders = 0;
            int numOfWaitingForAdmissionOrders = 0;
            if (freshOrders != null) {
                Set<JOrder> freshOrderSet = freshOrders.collect(Collectors.toSet());

                Set<OrderId> blockedOrderIds = freshOrderSet.stream().filter(o -> {
                    Instant scheduledFor = OrdersHelper.getScheduledForInstant(o);
                    return scheduledFor != null && scheduledFor.isBefore(now);
                }).map(o -> o.id()).collect(Collectors.toSet());

                Set<OrderId> waitingForAdmissionOrderIds = OrdersHelper.getWaitingForAdmissionOrderIds(blockedOrderIds, controllerState);
                waitingForAdmissionOrderIds.forEach(i -> blockedOrderIds.remove(i));
                numOfWaitingForAdmissionOrders = waitingForAdmissionOrderIds.size();

                numOfBlockedOrders = blockedOrderIds.stream().map(oId -> OrdersHelper.getCyclicOrderIdMainPart(oId.string())).distinct()
                        .mapToInt(item -> 1).sum();

                numOfPendingOrders = freshOrderSet.stream().filter(o -> {
                    Optional<Instant> scheduledFor = o.scheduledFor();
                    return scheduledFor.isPresent() && scheduledFor.get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
                }).map(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string())).distinct().mapToInt(item -> 1).sum();

                if (body.getDateTo() != null && !body.getDateTo().isEmpty()) {
                    String dateTo = body.getDateTo();
                    if ("0d".equals(dateTo)) {
                        dateTo = "1d";
                    }
                    Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, body.getTimeZone());
                    final Instant until = (dateToInstant.isBefore(now)) ? now : dateToInstant;
                    Predicate<JOrder> dateToFilter = o -> {
                        Instant scheduledFor = OrdersHelper.getScheduledForInstant(o);
                        return scheduledFor == null || scheduledFor.isBefore(until);
                    };
                    numOfFreshOrders = freshOrderSet.stream().filter(dateToFilter).map(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string()))
                            .distinct().mapToInt(e -> 1).sum();
                } else {
                    numOfFreshOrders = freshOrderSet.stream().map(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string())).distinct().mapToInt(
                            e -> 1).sum() - numOfPendingOrders;
                }

            }

            final Map<OrderStateText, Integer> map = orderStates.entrySet().stream().collect(Collectors.groupingBy(
                    entry -> OrdersHelper.groupByStateClasses.getOrDefault(entry.getKey(), OrderStateText.UNKNOWN), Collectors.summingInt(
                            entry -> entry.getValue())));
            map.put(OrderStateText.BLOCKED, numOfBlockedOrders);
            map.put(OrderStateText.PENDING, numOfPendingOrders);
            map.put(OrderStateText.SCHEDULED, Math.max(0, numOfFreshOrders - numOfBlockedOrders - numOfWaitingForAdmissionOrders));
            OrdersHelper.groupByStateClasses.values().stream().distinct().forEach(state -> map.putIfAbsent(state, 0));

            summary.setBlocked(map.getOrDefault(OrderStateText.BLOCKED, 0));
            summary.setScheduled(map.getOrDefault(OrderStateText.SCHEDULED, 0));
            summary.setPending(map.getOrDefault(OrderStateText.PENDING, 0));
            summary.setInProgress(map.getOrDefault(OrderStateText.INPROGRESS, 0) + numOfWaitingForAdmissionOrders);
            summary.setRunning(map.getOrDefault(OrderStateText.RUNNING, 0));
            summary.setFailed(map.getOrDefault(OrderStateText.FAILED, 0));
            summary.setSuspended(suspendedOrders);
            summary.setWaiting(map.getOrDefault(OrderStateText.WAITING, 0));
            summary.setTerminated(map.getOrDefault(OrderStateText.CANCELLED, 0) + map.getOrDefault(OrderStateText.FINISHED, 0));
            summary.setPrompting(map.getOrDefault(OrderStateText.PROMPTING, 0));

            entity.setSurveyDate(Date.from(now));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
//            if (map.get(OrderStateText.UNKNOWN) != null) {
//                //LOGGER
//            }

        } catch (ControllerConnectionRefusedException e) {
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setSurveyDate(entity.getDeliveryDate());
        }

        entity.setOrders(summary);
        return entity;
    }

    private static Set<VersionedItemId<WorkflowPath>> checkFolderPermission(JControllerState controllerState, Set<WorkflowId> workflowIds,
            Set<Folder> permittedFolders) {
        Stream<WorkflowId> workflowStream = workflowIds != null ? workflowIds.stream() : Stream.empty();
        if (permittedFolders != null && !permittedFolders.isEmpty()) {
            workflowStream = workflowStream.filter(w -> folderIsPermitted(w.getPath(), permittedFolders));
        }
        return workflowStream.map(wId -> {
            if (wId.getVersionId() == null) {
                Either<Problem, JWorkflow> wE = controllerState.repo().pathToCheckedWorkflow(WorkflowPath.of(JocInventory.pathToName(wId.getPath())));
                if (wE.isRight()) {
                    return wE.get().id().asScala();
                } else {
                    return null;
                }
            } else {
                return JWorkflowId.of(JocInventory.pathToName(wId.getPath()), wId.getVersionId()).asScala();
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
