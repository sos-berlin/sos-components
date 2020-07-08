package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import js7.data.order.Order;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.JControllerState;
import js7.proxy.javaapi.data.JOrder;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";
    private static final List<String> groupStates = Arrays.asList("pending", "waiting", "failed", "running", "finished");
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
        final Set<String> workflows = getWorkflowsWithoutDuplicates(workflowPaths);
        
        Stream<JOrder> jOrderStream = null;
        if (!workflows.isEmpty()) {
            if (workflows.size() == 1) {
                final String workflow = workflows.iterator().next();
                jOrderStream = controllerState.ordersBy(o -> o.workflowId().path().string().equals(workflow));
            } else {
                jOrderStream = controllerState.ordersBy(o -> workflows.contains(o.workflowId().path().string()));
            }
        } else {
            jOrderStream = controllerState.ordersBy(o -> true);
        }
//        Map<String, Long> map = jOrderStream.map(jOrder -> groupStatesMap.get(jOrder.underlying().state().getClass())).collect(Collectors.groupingBy(
//                Function.identity(), Collectors.counting()));
        Map<String, Long> map = jOrderStream.collect(Collectors.groupingBy(jOrder -> groupStatesMap.get(jOrder.underlying().state().getClass()),
                Collectors.counting()));
        
        groupStates.stream().forEach(state -> map.putIfAbsent(state, 0L));
        
        OrdersSummary summary = new OrdersSummary();
        summary.setBlacklist(map.get("finished").intValue());
        summary.setPending(map.get("pending").intValue());
        summary.setRunning(map.get("running").intValue());
        summary.setSetback(0);
        summary.setSuspended(0);
        summary.setWaitingForResource(map.get("waiting").intValue());
        
        OrdersSnapshot entity = new OrdersSnapshot();
        entity.setSurveyDate(Date.from(Instant.ofEpochMilli(controllerState.eventId() / 1000)));
        entity.setOrders(summary);
        entity.setDeliveryDate(Date.from(Instant.now()));
        return entity;
    }
    
    private static Set<String> getWorkflowsWithoutDuplicates(List<String> workflowPaths) throws JocMissingRequiredParameterException {
        if (workflowPaths == null || workflowPaths.isEmpty()) {
            return new HashSet<String>();
        } else {
            return workflowPaths.stream().collect(Collectors.toSet());
        }
    }
}
