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
            
            Predicate<JOrder> dateToFilter = o -> true;
            if (workflowsFilter.getDateTo() != null && !workflowsFilter.getDateTo().isEmpty()) {
                    String dateTo = workflowsFilter.getDateTo();
                    if ("0d".equals(dateTo)) {
                        dateTo = "1d";
                    }
                    Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, workflowsFilter.getTimeZone());
                    final Instant until = (dateToInstant.isBefore(surveyInstant)) ? surveyInstant : dateToInstant;
                    dateToFilter = o -> {
                        if (OrderStateText.SCHEDULED.equals(OrdersHelper.getGroupedState(o.asScala().state().getClass()))) {
                            if (o.scheduledFor().isPresent() && o.scheduledFor().get().isAfter(until)) {
                                if (o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS.longValue()) {
                                    return true;
                                }
                                return false;
                            }
                        }
                        return true;
                    };
            }
            
            Set<VersionedItemId<WorkflowPath>> workflows2 = workflowsFilter.getWorkflowIds().parallelStream().filter(w -> canAdd(WorkflowPaths
                    .getPath(w), permittedFolders)).map(w -> {
                        if (w.getVersionId() == null || w.getVersionId().isEmpty()) {
                            return currentstate.repo().pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                        } else {
                            return currentstate.repo().idToWorkflow(JWorkflowId.of(JocInventory.pathToName(w.getPath()), w.getVersionId()));
                        }
                    }).filter(Either::isRight).map(Either::get).map(JWorkflow::id).map(JWorkflowId::asScala).collect(Collectors.toSet());
            
            Function1<Order<Order.State>, Object> workflowFilter = o -> workflows2.contains(o.workflowId());
            Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                    Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                            Order.ProcessingKilled$.class));
            Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
            Function1<Order<Order.State>, Object> cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class),
                    JOrderPredicates.and(o -> o.id().string().matches(".*#C[0-9]+-.*"), JOrderPredicates.not(suspendFilter)));
            Function1<Order<Order.State>, Object> notCycledOrderFilter = JOrderPredicates.not(cycledOrderFilter);

            Stream<JOrder> cycledOrderStream = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, cycledOrderFilter)).filter(dateToFilter);
            Stream<JOrder> notCycledOrderStream = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, notCycledOrderFilter)).filter(
                    dateToFilter);
            Comparator<JOrder> comp = Comparator.comparing(o -> o.id().string());
            Collection<TreeSet<JOrder>> cycledOrderColl = cycledOrderStream.collect(Collectors.groupingBy(o -> o.id().string().substring(0, 24),
                    Collectors.toCollection(() -> new TreeSet<>(comp)))).values();
            cycledOrderStream = cycledOrderColl.stream().parallel().map(t -> t.first());
            ConcurrentMap<JWorkflowId, Map<OrderStateText, Integer>> groupedOrdersCount = Stream.concat(notCycledOrderStream, cycledOrderStream)
                    .collect(Collectors.groupingByConcurrent(JOrder::workflowId, Collectors.groupingBy(o -> groupingByState(o, surveyInstant),
                            Collectors.reducing(0, e -> 1, Integer::sum))));

            workflows2.forEach(w -> groupedOrdersCount.putIfAbsent(JWorkflowId.apply(w), Collections.emptyMap()));
            
            WorkflowsOrderCount workflows = new WorkflowsOrderCount();
            workflows.setSurveyDate(Date.from(surveyInstant));
            workflows.setWorkflows(groupedOrdersCount.entrySet().stream().map(e -> {
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
    
    private static OrderStateText groupingByState(JOrder order, Instant surveyInstant) {
        OrderStateText groupedState = OrdersHelper.getGroupedState(order.asScala().state().getClass());
        if (order.asScala().isSuspended() && !(OrderStateText.CANCELLED.equals(groupedState) || OrderStateText.FINISHED.equals(groupedState))) {
            groupedState = OrderStateText.SUSPENDED;
        }
        if (OrderStateText.SCHEDULED.equals(groupedState) && order.scheduledFor().isPresent()) {
            Instant scheduledInstant = order.scheduledFor().get();
            if (JobSchedulerDate.NEVER_MILLIS.longValue() == scheduledInstant.toEpochMilli()) {
                groupedState = OrderStateText.PENDING;
            } else if (scheduledInstant.isBefore(surveyInstant)) {
                groupedState = OrderStateText.BLOCKED;
            }
        }
        return groupedState;
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
