package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersSummary;
import com.sos.joc.model.workflow.WorkflowIdsFilter;
import com.sos.joc.model.workflow.WorkflowOrderCount;
import com.sos.joc.model.workflow.WorkflowsOrderCount;
import com.sos.joc.workflows.resource.IWorkflowsOrderCountResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import scala.Function1;

@Path("workflows")
public class WorkflowsOrderCountResourceImpl extends JOCResourceImpl implements IWorkflowsOrderCountResource {

    private static final String API_CALL = "./workflows/order_count";

    @Override
    public JOCDefaultResponse postOrderCount(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowIdsFilter.class);
            WorkflowIdsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowIdsFilter.class);
            String controllerId = workflowsFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            final JControllerState currentstate = Proxy.of(controllerId).currentState();;
            final Instant surveyInstant = currentstate.instant();
            
            WorkflowsOrderCount workflows = new WorkflowsOrderCount();
            workflows.setSurveyDate(Date.from(surveyInstant));
            workflows.setWorkflows(WorkflowsHelper.getGroupedOrdersCountPerWorkflow(currentstate, workflowsFilter, permittedFolders).entrySet()
                    .stream().map(e -> {
                        WorkflowOrderCount w = new WorkflowOrderCount();
                        w.setPath(e.getKey().path().string());
                        w.setVersionId(e.getKey().versionId().string());
                        w.setNumOfOrders(getNumOfOrders(e.getValue()));
                        return w;
                    }).collect(Collectors.toList()));
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(workflows);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static OrdersSummary getNumOfOrders(Map<OrderStateText, Integer> map) {
        OrdersSummary summary = new OrdersSummary();
        if (map != null && !map.isEmpty()) {
            summary.setBlocked(map.getOrDefault(OrderStateText.BLOCKED, 0));
            summary.setScheduled(map.getOrDefault(OrderStateText.SCHEDULED, 0));
            summary.setPending(map.getOrDefault(OrderStateText.PENDING, 0));
            summary.setInProgress(map.getOrDefault(OrderStateText.INPROGRESS, 0));
            summary.setRunning(map.getOrDefault(OrderStateText.RUNNING, 0));
            summary.setFailed(map.getOrDefault(OrderStateText.FAILED, 0));
            summary.setSuspended(map.getOrDefault(OrderStateText.SUSPENDED, 0));
            summary.setWaiting(map.getOrDefault(OrderStateText.WAITING, 0));
            summary.setTerminated(map.getOrDefault(OrderStateText.CANCELLED, 0) + map.getOrDefault(OrderStateText.FINISHED, 0));
            summary.setPrompting(map.getOrDefault(OrderStateText.PROMPTING, 0));
        } else {
            summary.setBlocked(0);
            summary.setScheduled(0);
            summary.setPending(0);
            summary.setInProgress(0);
            summary.setRunning(0);
            summary.setFailed(0);
            summary.setSuspended(0);
            summary.setWaiting(0);
            summary.setTerminated(0);
            summary.setPrompting(0);
        }
        return summary;
    }
}
