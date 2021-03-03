package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import js7.base.time.Timestamp;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";

    @Override
    public JOCDefaultResponse postOrdersOverviewSnapshot(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV body = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(body.getControllerId(), getPermissonsJocCockpit(body.getControllerId(),
                    accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean withWorkFlowFilter = body.getWorkflowIds() != null && !body.getWorkflowIds().isEmpty();
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            return JOCDefaultResponse.responseStatus200(getSnapshot(body, checkFolderPermission(body.getWorkflowIds(),
                    permittedFolders), permittedFolders, withWorkFlowFilter));

        } catch (JobSchedulerConnectionResetException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

    private static OrdersSnapshot getSnapshot(OrdersFilterV body, Set<VersionedItemId<WorkflowPath>> workflowIds, Set<Folder> permittedFolders,
            boolean withWorkFlowFilter) throws JobSchedulerConnectionResetException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        OrdersSummary summary = new OrdersSummary();
        summary.setBlocked(0);
        summary.setPending(0);
        summary.setInProgress(0);
        summary.setRunning(0);
        summary.setFailed(0);
        summary.setSuspended(0);
        summary.setWaiting(0);
        summary.setCalling(0);
        summary.setTerminated(0);
        
        OrdersSnapshot entity = new OrdersSnapshot();
        
        try {
            JControllerState controllerState = Proxy.of(body.getControllerId()).currentState();
            final long nowMillis = controllerState.eventId() / 1000;
            final Instant now = Instant.ofEpochMilli(nowMillis);
            Map<Class<? extends Order.State>, Integer> orderStates = null;
            int suspendedOrders = 0;
            Stream<JOrder> freshOrders = null;

            if (withWorkFlowFilter) {
                if (!workflowIds.isEmpty()) {

                    orderStates = controllerState.orderStateToCount(o -> !o.isSuspended() && workflowIds.contains(o.workflowId()));
                    if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
                        freshOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> !o.asScala().isSuspended()
                                && workflowIds.contains(o.workflowId()));
                    }
                    suspendedOrders = controllerState.ordersBy(o -> o.isSuspended() && workflowIds.contains(o.workflowId())).mapToInt(e -> 1).sum();
                } else {
                    // no folder permissions
                    orderStates = Collections.emptyMap();
                }
            } else if (permittedFolders != null && !permittedFolders.isEmpty()) {
                orderStates = controllerState.orderStateToCount(o -> !o.isSuspended() && orderIsPermitted(o.workflowId().path().string(),
                        permittedFolders));
                if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
                    freshOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> !o.asScala().isSuspended()
                            && orderIsPermitted(o.workflowId().path().string(), permittedFolders));
                }
                suspendedOrders = controllerState.ordersBy(o -> o.isSuspended() && orderIsPermitted(o.workflowId().path().string(), permittedFolders))
                        .mapToInt(e -> 1).sum();
            } else {
                orderStates = controllerState.orderStateToCount(o -> !o.isSuspended());
                if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
                    freshOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> !o.asScala().isSuspended());
                }
                suspendedOrders = controllerState.ordersBy(o -> o.isSuspended()).mapToInt(e -> 1).sum();
            }
            
            int numOfBlockedOrders = 0;
            int numOfFreshOrders = 0;
            if (freshOrders != null) {
                Set<JOrder> freshOrderSet = freshOrders.collect(Collectors.toSet());
                if (body.getDateTo() != null && !body.getDateTo().isEmpty()) {
                    String dateTo = body.getDateTo();
                    if ("0d".equals(dateTo)) {
                        dateTo = "1d";
                    }
                    Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, body.getTimeZone());
                    final Instant until = (dateToInstant.isBefore(Instant.now())) ? Instant.now() : dateToInstant;
                    Predicate<JOrder> dateToFilter = o -> {
                        scala.Option<Timestamp> scheduledFor = o.asScala().state().maybeDelayedUntil();
                        return scheduledFor.isEmpty() || !scheduledFor.get().toInstant().isAfter(until);
                    };
                    numOfFreshOrders = freshOrderSet.stream().filter(dateToFilter).map(o -> o.id().string().substring(0, 24)).distinct().mapToInt(
                            e -> 1).sum();
                } else {
                    numOfFreshOrders = freshOrderSet.stream().map(o -> o.id().string().substring(0, 24)).distinct().mapToInt(e -> 1).sum();
                }
                numOfBlockedOrders = freshOrderSet.stream().filter(o -> {
                    scala.Option<Timestamp> scheduledFor = o.asScala().state().maybeDelayedUntil();
                    return !scheduledFor.isEmpty() && scheduledFor.get().toInstant().isBefore(now);
                }).map(o -> o.id().string().substring(0, 24)).distinct().mapToInt(item -> 1).sum();
            }
            
            final Map<OrderStateText, Integer> map = orderStates.entrySet().stream().collect(Collectors.groupingBy(
                    entry -> OrdersHelper.groupByStateClasses.get(entry.getKey()), Collectors.summingInt(entry -> entry.getValue())));
            map.put(OrderStateText.BLOCKED, numOfBlockedOrders);
            map.put(OrderStateText.PENDING, numOfFreshOrders - numOfBlockedOrders);
            OrdersHelper.groupByStateClasses.values().stream().distinct().forEach(state -> map.putIfAbsent(state, 0));
            
            summary.setBlocked(map.getOrDefault(OrderStateText.BLOCKED, 0));
            summary.setPending(map.getOrDefault(OrderStateText.PENDING, 0));
            summary.setInProgress(map.getOrDefault(OrderStateText.INPROGRESS, 0));
            summary.setRunning(map.getOrDefault(OrderStateText.RUNNING, 0));
            summary.setFailed(map.getOrDefault(OrderStateText.FAILED, 0));
            summary.setSuspended(suspendedOrders);
            summary.setWaiting(map.getOrDefault(OrderStateText.WAITING, 0));
            summary.setCalling(0);
            summary.setTerminated(map.getOrDefault(OrderStateText.CANCELLED, 0) + map.getOrDefault(OrderStateText.FINISHED, 0));
            
            entity.setSurveyDate(Date.from(now));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
        } catch (JobSchedulerConnectionRefusedException e) {
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setSurveyDate(entity.getDeliveryDate());
        }

        entity.setOrders(summary);
        return entity;
    }

    private static Set<VersionedItemId<WorkflowPath>> checkFolderPermission(Set<WorkflowId> workflowIds, Set<Folder> permittedFolders) {
        Set<VersionedItemId<WorkflowPath>> wIds = new HashSet<>();
        if (workflowIds != null) {
            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                wIds = workflowIds.stream().filter(w -> folderIsPermitted(w.getPath(), permittedFolders)).map(w -> JWorkflowId.of(JocInventory
                        .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
            } else {
                wIds = workflowIds.stream().map(w -> JWorkflowId.of(JocInventory.pathToName(w.getPath()), w.getVersionId()).asScala()).collect(
                        Collectors.toSet());
            }
        }
        return wIds;
    }

    private static boolean orderIsPermitted(String orderPath, Set<Folder> listOfFolders) {
        // TODO order.workflowId().path().string() is only a name
        return true;
//      if (listOfFolders == null || listOfFolders.isEmpty()) {
//      return true;
//  }
//      return folderIsPermitted(Paths.get(order.workflowId().path().string()).getParent().toString().replace('\\', '/'), listOfFolders);
    }

    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }
}
