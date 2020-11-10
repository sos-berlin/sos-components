package com.sos.joc.orders.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import js7.data.item.ItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;
import js7.proxy.javaapi.data.workflow.JWorkflowId;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";
    
    @Override
    public JOCDefaultResponse postOrdersOverviewSnapshot(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter body = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(body.getJobschedulerId(), getPermissonsJocCockpit(body.getJobschedulerId(),
                    accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean withWorkFlowFilter = body.getWorkflowIds() != null && !body.getWorkflowIds().isEmpty();
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            return JOCDefaultResponse.responseStatus200(getSnapshot(Proxy.of(body.getJobschedulerId()).currentState(), checkFolderPermission(body
                    .getWorkflowIds(), permittedFolders), permittedFolders, withWorkFlowFilter));

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

    private static OrdersSnapshot getSnapshot(JControllerState controllerState, Set<ItemId<WorkflowPath>> workflowIds, Set<Folder> permittedFolders,
            boolean withWorkFlowFilter) {

        final long nowMillis = controllerState.eventId() / 1000;
        final Instant now = Instant.ofEpochMilli(nowMillis);
        Map<Class<? extends Order.State>, Integer> orderStates = null;
        int suspendedOrders = 0;
        Stream<JOrder> blockedOrders = null;

        if (withWorkFlowFilter) {
            if (!workflowIds.isEmpty()) {

                orderStates = controllerState.orderStateToCount(o -> !o.isSuspended() && workflowIds.contains(o.workflowId()));
                if (orderStates.containsKey(Order.Fresh.class) && orderStates.get(Order.Fresh.class) > 0) {
                    blockedOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> !o.asScala().isSuspended()
                            && workflowIds.contains(o.workflowId()));
                }
                suspendedOrders = controllerState.ordersBy(o -> o.isSuspended() && workflowIds.contains(o.workflowId())).mapToInt(e -> 1).sum();
            } else {
                // no folder permissions
                orderStates = Collections.emptyMap();
            }
        } else if (permittedFolders != null && !permittedFolders.isEmpty()) {
            orderStates = controllerState.orderStateToCount(o -> !o.isSuspended() && orderIsPermitted(o.workflowId().path().string(), permittedFolders));
            if (orderStates.containsKey(Order.Fresh.class) && orderStates.get(Order.Fresh.class) > 0) {
                blockedOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> !o.asScala().isSuspended() && orderIsPermitted(o.workflowId()
                        .path().string(), permittedFolders));
            }
            suspendedOrders = controllerState.ordersBy(o -> o.isSuspended() && orderIsPermitted(o.workflowId().path().string(), permittedFolders)).mapToInt(e -> 1).sum();
        } else {
            orderStates = controllerState.orderStateToCount(o -> !o.isSuspended());
            if (orderStates.containsKey(Order.Fresh.class) && orderStates.get(Order.Fresh.class) > 0) {
                blockedOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> !o.asScala().isSuspended());
            }
            suspendedOrders = controllerState.ordersBy(o -> o.isSuspended()).mapToInt(e -> 1).sum();
        }

        int numOfBlockedOrders = 0;
        if (blockedOrders != null) {
            // numOfBlockedOrders = blockedOrders.map(o -> {
            // try {
            // OrderItem item = Globals.objectMapper.readValue(o.toJson(), OrderItem.class);
            // if (item.getState().getScheduledFor() != null) {
            // return item.getState().getScheduledFor();
            // } else {
            // return null;
            // }
            // } catch (Exception e1) {
            // return null;
            // }
            // }).filter(tstamp -> tstamp != null && tstamp < nowMillis).mapToInt(e -> 1).sum();

            numOfBlockedOrders = blockedOrders.map(order -> order.asScala().state().maybeDelayedUntil()).filter(tstamp -> !tstamp.isEmpty()
                    && tstamp.get().toInstant().isBefore(now)).mapToInt(item -> 1).sum();
        }

        final Map<OrderStateText, Integer> map = orderStates.entrySet().stream().collect(Collectors.groupingBy(entry -> OrdersHelper.groupByStateClasses.get(
                entry.getKey()), Collectors.summingInt(entry -> entry.getValue())));
        map.put(OrderStateText.BLOCKED, numOfBlockedOrders);
        OrdersHelper.groupByStateClasses.values().stream().distinct().forEach(state -> map.putIfAbsent(state, 0));

        // TODO suspended is not yet supported

        OrdersSummary summary = new OrdersSummary();
        summary.setBlocked(map.get(OrderStateText.BLOCKED));
        summary.setPending(map.get(OrderStateText.PENDING) - map.get(OrderStateText.BLOCKED));
        summary.setRunning(map.get(OrderStateText.RUNNING));
        summary.setFailed(map.get(OrderStateText.FAILED));
        summary.setSuspended(suspendedOrders);
        summary.setWaiting(map.get(OrderStateText.WAITING));

        OrdersSnapshot entity = new OrdersSnapshot();
        entity.setSurveyDate(Date.from(now));
        entity.setOrders(summary);
        entity.setDeliveryDate(Date.from(Instant.now()));
        return entity;
    }
    
    private static Set<ItemId<WorkflowPath>> checkFolderPermission(List<WorkflowId> workflowIds, Set<Folder> permittedFolders) {
        Set<ItemId<WorkflowPath>> wIds = new HashSet<>();
        if (workflowIds != null) {
            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                wIds = workflowIds.stream().filter(w -> folderIsPermitted(w.getPath(), permittedFolders)).map(w -> JWorkflowId.of(w.getPath(), w
                        .getVersionId()).asScala()).collect(Collectors.toSet());
            } else {
                wIds = workflowIds.stream().map(w -> JWorkflowId.of(w.getPath(), w.getVersionId()).asScala()).collect(Collectors.toSet());
            }
        }
        return wIds;
    }
    
    private static boolean orderIsPermitted(String orderPath, Set<Folder> listOfFolders) {
        return folderIsPermitted(Paths.get(orderPath).getParent().toString().replace('\\', '/'), listOfFolders);
    }

    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }
}
