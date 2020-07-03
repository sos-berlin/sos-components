package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyCredentialsBuilder;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.orders.resource.IOrdersResourceOverviewSnapshot;
import com.sos.schema.JsonValidator;

import js7.proxy.javaapi.JMasterProxy;
import js7.proxy.javaapi.data.JMasterState;
import js7.proxy.javaapi.data.JOrder;

@Path("orders")
public class OrdersResourceOverviewSnapshotImpl extends JOCResourceImpl implements IOrdersResourceOverviewSnapshot {

    private static final String API_CALL = "./orders/overview/snapshot";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceOverviewSnapshotImpl.class);

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
            
            JMasterProxy masterProxy = Proxies.connect(ProxyCredentialsBuilder.withUrl(this));
            return JOCDefaultResponse.responseStatus200(getSnapshot(masterProxy.currentState(), body.getWorkflows()));

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
    
    private static OrdersSnapshot getSnapshot(JMasterState masterState, List<String> workflowPaths) throws Exception {
        final Set<String> workflows = getWorkflowsWithoutDuplicates(workflowPaths);
        
        Stream<JOrder> jOrderStream = null;
        if (!workflows.isEmpty()) {
            if (workflows.size() == 1) {
                final String workflow = workflows.iterator().next();
                jOrderStream = masterState.ordersBy(o -> o.workflowId().path().string().equals(workflow));
            } else {
                jOrderStream = masterState.ordersBy(o -> workflows.contains(o.workflowId().path().string()));
            }
        } else {
            jOrderStream = masterState.ordersBy(o -> true);
        }
        
        Map<Class<? extends JOrder>, Long> map = jOrderStream.collect(Collectors.groupingBy(JOrder::getClass, Collectors.counting()));
        // TODO consider map
        map.entrySet().stream().forEach(entry -> {
            LOGGER.info(String.format("%s : %d", entry.getKey().getSimpleName(), entry.getValue()));
        });
        
        OrdersSummary summary = new OrdersSummary();
        summary.setBlacklist(0);
        summary.setPending(0);
        summary.setRunning(0);
        summary.setSetback(0);
        summary.setSuspended(0);
        summary.setWaitingForResource(0);
        
        OrdersSnapshot entity = new OrdersSnapshot();
        entity.setSurveyDate(Date.from(Instant.ofEpochMilli(masterState.eventId() / 1000)));
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
