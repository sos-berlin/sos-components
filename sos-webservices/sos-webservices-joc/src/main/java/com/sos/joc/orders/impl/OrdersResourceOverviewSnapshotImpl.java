package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.JControllerState;
import js7.proxy.javaapi.data.JOrderPredicates;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";
    private static final List<String> groupStates = Arrays.asList("pending", "waiting", "blocked", "suspended", "failed", "running");
    private static final Map<Class<? extends Order.State>, String> groupStatesMap = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, String>() {

                /*
                 * +PENDING: Fresh 
                 * +WAITING: Forked, Offering, Awaiting, DelayedAfterError 
                 * -BLOCKED: Fresh late +RUNNING: Ready, Processing, Processed
                 * ---FAILED: Failed, FailedWhileFresh, FailedInFork, Broken 
                 * --SUSPENDED any state+Suspended Annotation
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
		    JsonValidator.validateFailFast(filterBytes, JobSchedulerId.class);
		    WorkflowsFilter body = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
		    
		    JOCDefaultResponse jocDefaultResponse = init(API_CALL, body, accessToken, body.getJobschedulerId(),
                    getPermissonsJocCockpit(body.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JControllerProxy controllerProxy = Proxy.of(this.getUrl());
            return JOCDefaultResponse.responseStatus200(getSnapshot(controllerProxy.currentState(), body.getWorkflows()));

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
    
    private static OrdersSnapshot getSnapshot(JControllerState controllerState, List<String> workflowPaths) throws Exception {
        
        Map<Class<? extends Order.State>, Integer> orderStates = null;
        if (!workflowPaths.isEmpty()) {
            if (workflowPaths.size() == 1) {
                orderStates = controllerState.orderStateToCount(JOrderPredicates.byWorkflowPath(WorkflowPath.of(workflowPaths.get(0))));
            } else {
                orderStates = controllerState.orderStateToCount(o -> workflowPaths.contains(o.workflowId().path().string()));
            }
        } else {
            orderStates = controllerState.orderStateToCount();
        }

        final Map<String, Integer> map = orderStates.entrySet().stream().collect(Collectors.groupingBy(entry -> groupStatesMap.get(entry.getKey()),
                Collectors.summingInt(entry -> entry.getValue())));
        groupStates.stream().forEach(state -> map.putIfAbsent(state, 0));
        
        //TODO blocked comes from DailyPlan
        //TODO suspended is not yet supported

        OrdersSummary summary = new OrdersSummary();
        summary.setBlocked(map.get("blocked"));
        summary.setPending(map.get("pending"));
        summary.setRunning(map.get("running"));
        summary.setFailed(map.get("failed"));
        summary.setSuspended(map.get("suspended"));
        summary.setWaiting(map.get("waiting"));
        
        OrdersSnapshot entity = new OrdersSnapshot();
        entity.setSurveyDate(Date.from(Instant.ofEpochMilli(controllerState.eventId() / 1000)));
        entity.setOrders(summary);
        entity.setDeliveryDate(Date.from(Instant.now()));
        return entity;
    }
}
