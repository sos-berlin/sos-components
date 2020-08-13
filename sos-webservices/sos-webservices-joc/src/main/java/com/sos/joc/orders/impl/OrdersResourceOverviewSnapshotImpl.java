package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import js7.data.order.Order;
import js7.proxy.javaapi.data.JControllerState;
import js7.proxy.javaapi.data.JOrder;
import js7.proxy.javaapi.data.JOrderPredicates;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";
    private static final Map<Class<? extends Order.State>, String> groupStatesMap = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, String>() {

                /*
                 * +PENDING: Fresh +WAITING: Forked, Offering, Awaiting, DelayedAfterError -BLOCKED: Fresh late +RUNNING: Ready, Processing, Processed
                 * ---FAILED: Failed, FailedWhileFresh, FailedInFork, Broken --SUSPENDED any state+Suspended Annotation
                 */
                private static final long serialVersionUID = 1L;

                {
                    put(Order.Fresh.class, "pending");
                    put(Order.Awaiting.class, "waiting");
                    put(Order.DelayedAfterError.class, "waiting");
                    put(Order.Forked.class, "waiting");
                    put(Order.Offering.class, "waiting");
                    put(Order.Broken.class, "failed");
                    put(Order.Failed.class, "failed");
                    put(Order.FailedInFork.class, "failed");
                    put(Order.FailedWhileFresh$.class, "failed");
                    put(Order.Ready$.class, "running");
                    put(Order.Processed$.class, "running");
                    put(Order.Processing$.class, "running");
                    put(Order.Finished$.class, "finished");
                    put(Order.Cancelled$.class, "finished");
                    put(Order.ProcessingCancelled$.class, "finished");
                }
            });

    @Override
    public JOCDefaultResponse postOrdersOverviewSnapshot(String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter body = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, body, accessToken, body.getJobschedulerId(), getPermissonsJocCockpit(body
                    .getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean withWorkFlowFilter = body.getWorkflows() != null && !body.getWorkflows().isEmpty();

            return JOCDefaultResponse.responseStatus200(getSnapshot(Proxy.of(body.getJobschedulerId()).currentState(), checkFolderPermission(body
                    .getWorkflows(), folderPermissions.getListOfFolders()), withWorkFlowFilter));

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

    private static Set<String> checkFolderPermission(List<String> _workflowPaths, Set<Folder> permittedFolders) {
        Set<String> workflowPaths = new HashSet<>();
        if (_workflowPaths != null) {
            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                workflowPaths = _workflowPaths.stream().filter(w -> folderIsPermitted(w, permittedFolders)).collect(Collectors.toSet());
            } else {
                workflowPaths = _workflowPaths.stream().collect(Collectors.toSet());
            }
        }
        return workflowPaths;
    }

    private static OrdersSnapshot getSnapshot(JControllerState controllerState, Set<String> workflowPaths, boolean withWorkFlowFilter) {

        final long nowMillis = controllerState.eventId() / 1000;
        final Instant now = Instant.ofEpochMilli(nowMillis);
        Map<Class<? extends Order.State>, Integer> orderStates = null;
        Stream<JOrder> blockedOrders = null;

        if (withWorkFlowFilter) {
            if (!workflowPaths.isEmpty()) {
                orderStates = controllerState.orderStateToCount(o -> workflowPaths.contains(o.workflowId().path().string()));
                if (orderStates.containsKey(Order.Fresh.class) && orderStates.get(Order.Fresh.class) > 0) {
                    blockedOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class)).filter(o -> workflowPaths.contains(o
                            .workflowId().path().string()));
                }
            } else {
                // no folder permissions
                orderStates = new HashMap<Class<? extends Order.State>, Integer>();
            }
        } else {
            orderStates = controllerState.orderStateToCount();
            if (orderStates.containsKey(Order.Fresh.class) && orderStates.get(Order.Fresh.class) > 0) {
                blockedOrders = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class));
            }
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

            numOfBlockedOrders = blockedOrders.map(order -> order.underlying().state().maybeDelayedUntil()).filter(tstamp -> !tstamp.isEmpty()
                    && tstamp.get().toInstant().isBefore(now)).mapToInt(item -> 1).sum();
        }

        final Map<String, Integer> map = orderStates.entrySet().stream().collect(Collectors.groupingBy(entry -> groupStatesMap.get(entry.getKey()),
                Collectors.summingInt(entry -> entry.getValue())));
        map.put("blocked", numOfBlockedOrders);
        groupStatesMap.values().stream().distinct().forEach(state -> map.putIfAbsent(state, 0));

        // TODO suspended is not yet supported

        OrdersSummary summary = new OrdersSummary();
        summary.setBlocked(map.get("blocked"));
        summary.setPending(map.get("pending") - map.get("blocked"));
        summary.setRunning(map.get("running"));
        summary.setFailed(map.get("failed"));
        summary.setSuspended(map.get("suspended"));
        summary.setWaiting(map.get("waiting"));

        OrdersSnapshot entity = new OrdersSnapshot();
        entity.setSurveyDate(Date.from(now));
        entity.setOrders(summary);
        entity.setDeliveryDate(Date.from(Instant.now()));
        return entity;
    }

    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }
}
