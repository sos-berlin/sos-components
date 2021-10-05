package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.workflow.Requirements;
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
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.orders.resource.IOrdersResource;
import com.sos.schema.JsonValidator;

import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import scala.Function1;

@Path("orders")
public class OrdersResourceImpl extends JOCResourceImpl implements IOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceImpl.class);
    private static final String API_CALL = "./orders";
    private final List<OrderStateText> orderStateWithRequirements = Arrays.asList(OrderStateText.PENDING, OrderStateText.BLOCKED,
            OrderStateText.SUSPENDED);

    @Override
    public JOCDefaultResponse postOrders(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(ordersFilter.getControllerId(), getControllerPermissions(ordersFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Set<String> orders = ordersFilter.getOrderIds();
            Set<WorkflowId> workflowIds = ordersFilter.getWorkflowIds();
            boolean withOrderIdFilter = orders != null && !orders.isEmpty();
            boolean withWorkflowIdFilter = workflowIds != null && !workflowIds.isEmpty();
            if (ordersFilter.getLimit() == null) {
                ordersFilter.setLimit(10000); 
            }
            if (withOrderIdFilter) {
                ordersFilter.setFolders(null);
                ordersFilter.setLimit(-1);
            }
            
            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            
            JControllerState currentState = Proxy.of(ordersFilter.getControllerId()).currentState();
            Instant surveyDateInstant = currentState.instant();
            Long surveyDateMillis = surveyDateInstant.toEpochMilli();
            
            List<OrderStateText> states = ordersFilter.getStates();
            // BLOCKED is not a Controller state. It needs a special handling. These are SCHEDULED with scheduledFor in the past
            final boolean withStatesFilter = states != null && !states.isEmpty();
            final boolean lookingForBlocked = withStatesFilter && states.contains(OrderStateText.BLOCKED);
            final boolean lookingForPending = withStatesFilter && states.contains(OrderStateText.PENDING);
            final boolean lookingForScheduled = withStatesFilter && states.contains(OrderStateText.SCHEDULED);
            
            Function1<Order<Order.State>, Object> cycledOrderFilter = null;
            Function1<Order<Order.State>, Object> notCycledOrderFilter = null;
            Function1<Order<Order.State>, Object> stateFilter = null;
            Function1<Order<Order.State>, Object> freshOrderFilter = null;
            
            Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                    Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                            Order.ProcessingKilled$.class));
            Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
            Function1<Order<Order.State>, Object> notSuspendFilter = JOrderPredicates.not(suspendFilter);

            
            if (!withOrderIdFilter) {
                if (withStatesFilter) {

                    states.remove(OrderStateText.SCHEDULED);
                    states.remove(OrderStateText.PENDING);
                    states.remove(OrderStateText.BLOCKED);

                    Map<OrderStateText, Set<Class<? extends Order.State>>> m = OrdersHelper.groupByStateClasses.entrySet().stream().collect(Collectors
                            .groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
                    Iterator<Function1<Order<Order.State>, Object>> stateFilters = states.stream().filter(s -> m.containsKey(s)).flatMap(s -> m.get(s)
                            .stream()).map(JOrderPredicates::byOrderState).iterator();

                    if (stateFilters.hasNext()) {
                        stateFilter = stateFilters.next();
                        while (stateFilters.hasNext()) {
                            stateFilter = JOrderPredicates.or(stateFilter, stateFilters.next());
                        }
                    }
                    
                    if (states.contains(OrderStateText.SUSPENDED)) {
                        if (stateFilter == null) {
                            stateFilter = JOrderPredicates.and(JOrderPredicates.not(JOrderPredicates.byOrderState(Order.Fresh$.class)), suspendFilter);
                        } else {
                            stateFilter = JOrderPredicates.or(JOrderPredicates.and(JOrderPredicates.not(JOrderPredicates.byOrderState(
                                    Order.Fresh$.class)), suspendFilter), stateFilter);
                        }
                    } else {
                        if (stateFilter != null) {
                            stateFilter = JOrderPredicates.and(notSuspendFilter, stateFilter);
                        }
                    }

                    if (lookingForScheduled && !lookingForBlocked && !lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() >= surveyDateMillis && o
                                .scheduledFor().get().toEpochMilli() != JobSchedulerDate.NEVER_MILLIS;
                    } else if (lookingForScheduled && lookingForBlocked && !lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() != JobSchedulerDate.NEVER_MILLIS;
                    } else if (lookingForScheduled && !lookingForBlocked && lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() >= surveyDateMillis;
                    } else if (!lookingForScheduled && lookingForBlocked && !lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() < surveyDateMillis;
                    } else if (!lookingForScheduled && !lookingForBlocked && lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
                    } else if (!lookingForScheduled && lookingForBlocked && lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && (o.scheduledFor().get().toEpochMilli() < surveyDateMillis || o
                                .scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS);
                    }
                    
                    if (freshOrderFilter != null) {
                        freshOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), freshOrderFilter);
                    } else if (lookingForScheduled && lookingForBlocked && lookingForPending) {
                        freshOrderFilter = JOrderPredicates.byOrderState(Order.Fresh$.class);
                    }
                    
                    if (freshOrderFilter != null) {
                        cycledOrderFilter = JOrderPredicates.and(freshOrderFilter, JOrderPredicates.and(o -> o.id().string().matches(".*#C[0-9]+-.*"), notSuspendFilter));
                        if (states.contains(OrderStateText.SUSPENDED)) {
                            freshOrderFilter = JOrderPredicates.and(freshOrderFilter, JOrderPredicates.or(suspendFilter, o -> !o.id().string().matches(".*#C[0-9]+-.*")));
                        } else {
                            freshOrderFilter = JOrderPredicates.and(freshOrderFilter, JOrderPredicates.and(o -> !o.id().string().matches(".*#C[0-9]+-.*"), notSuspendFilter));
                        }
                    }
                    
                    if (stateFilter == null) {
                        if (freshOrderFilter != null) {
                            notCycledOrderFilter = freshOrderFilter;
                        }
                    } else {
                        if (freshOrderFilter != null) {
                            notCycledOrderFilter = JOrderPredicates.or(stateFilter, freshOrderFilter);
                        } else {
                            notCycledOrderFilter = stateFilter;
                        }
                    }

                } else {
                    cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), JOrderPredicates.and(o -> o.id()
                            .string().matches(".*#C[0-9]+-.*"), notSuspendFilter));
                    notCycledOrderFilter = JOrderPredicates.not(cycledOrderFilter);
                }
            }
            Stream<JOrder> orderStream = Stream.empty();
            Stream<JOrder> cycledOrderStream = Stream.empty();

            if (withOrderIdFilter) {
                ordersFilter.setRegex(null);
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
            } else if (workflowIds != null && !workflowIds.isEmpty()) {
                ordersFilter.setRegex(null);
                Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
                Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                        .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
                Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(versionNotEmpty.negate()).map(w -> WorkflowPath.of(JocInventory
                        .pathToName(w.getPath()))).collect(Collectors.toSet());
                Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                        .workflowId().path()));
                if (notCycledOrderFilter != null) {
                    orderStream = currentState.ordersBy(JOrderPredicates.and(workflowFilter, notCycledOrderFilter));
                }
                if (cycledOrderFilter != null) {
                    cycledOrderStream = currentState.ordersBy(JOrderPredicates.and(workflowFilter, cycledOrderFilter));
                }
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
                // orderStream = currentState.ordersBy(JOrderPredicates.none());
            } else if (folders != null && !folders.isEmpty()) {
                if (notCycledOrderFilter != null) {
                    orderStream = currentState.ordersBy(notCycledOrderFilter);
                }
                if (cycledOrderFilter != null) {
                    cycledOrderStream = currentState.ordersBy(cycledOrderFilter);
                }
            } else {
                if (notCycledOrderFilter != null) {
                    orderStream = currentState.ordersBy(notCycledOrderFilter);
                }
                if (cycledOrderFilter != null) {
                    cycledOrderStream = currentState.ordersBy(cycledOrderFilter);
                }
            }
            
            // OrderIds beat dateTo
            if (!withOrderIdFilter && (ordersFilter.getDateTo() != null && !ordersFilter.getDateTo().isEmpty())) {
                if (!withStatesFilter || lookingForScheduled || lookingForPending) {

                    String dateTo = ordersFilter.getDateTo();
                    if ("0d".equals(dateTo)) {
                        dateTo = "1d";
                    }
                    Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, ordersFilter.getTimeZone());
                    final Instant until = (dateToInstant.isBefore(surveyDateInstant)) ? surveyDateInstant : dateToInstant;
                    Predicate<JOrder> dateToFilter = o -> {
                        if (OrderStateText.SCHEDULED.equals(OrdersHelper.getGroupedState(o.asScala().state().getClass()))) {
                            if (o.scheduledFor().isPresent() && o.scheduledFor().get().isAfter(until)) {
                                if ((!withStatesFilter || lookingForPending) && o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS) {
                                    return true;
                                }
                                return false;
                            }
                        }
                        return true;
                    };
                    cycledOrderStream = cycledOrderStream.filter(dateToFilter);
                    orderStream = orderStream.filter(dateToFilter);
                }
            }
            
            if (ordersFilter.getRegex() != null && !ordersFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(ordersFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE).asPredicate();
                cycledOrderStream = cycledOrderStream.filter(o -> regex.test(WorkflowPaths.getPath(o.workflowId().path().string()) + "/" + o.id()
                        .string()));
                orderStream = orderStream.filter(o -> regex.test(WorkflowPaths.getPath(o.workflowId().path().string()) + "/" + o.id().string()));
            }
            
            // grouping cycledOrders and return the first pending Order of the group to orderStream
            Comparator<JOrder> comp = Comparator.comparing(o -> o.id().string());
            Collection<TreeSet<JOrder>> cycledOrderColl = cycledOrderStream.collect(Collectors.groupingBy(o -> o.id().string().substring(0, 24),
                    Collectors.toCollection(() -> new TreeSet<>(comp)))).values();
            cycledOrderStream = cycledOrderColl.stream().parallel().map(t -> t.first());
            
            Function<TreeSet<JOrder>, CyclicOrderInfos> getCyclicOrderInfos = t -> {
                CyclicOrderInfos cycle = new CyclicOrderInfos();
                cycle.setCount(t.size());
                cycle.setFirstOrderId(t.first().id().string());
                if (t.first().scheduledFor().isPresent()) {
                    cycle.setFirstStart(Date.from(t.first().scheduledFor().get()));
                }
                cycle.setLastOrderId(t.last().id().string());
                if (t.last().scheduledFor().isPresent()) {
                    cycle.setLastStart(Date.from(t.last().scheduledFor().get()));
                }
                return cycle;
            };
            ConcurrentMap<String, CyclicOrderInfos> cycleInfos = cycledOrderColl.stream().parallel().filter(t -> !t.isEmpty()).map(
                    getCyclicOrderInfos).collect(Collectors.toConcurrentMap(CyclicOrderInfos::getFirstOrderId, Function.identity()));

            // merge cycledOrders to orderStream and grouping by folders for folder permissions
//            ConcurrentMap<String, ConcurrentMap<JWorkflowId, List<JOrder>>> groupedByFolders = Stream.concat(orderStream, cycledOrderStream).collect(
//                    Collectors.groupingByConcurrent(o -> o.workflowId().path().string(), Collectors.groupingByConcurrent(JOrder::workflowId)));
//            ConcurrentMap<JWorkflowId, Collection<String>> finalParamsPerWorkflow = groupedByFolders.values().parallelStream().flatMap(e -> e.keySet()
//                    .stream()).collect(Collectors.toConcurrentMap(Function.identity(), w -> OrdersHelper.getFinalParameters(w, currentState)));
//            orderStream = groupedByFolders.entrySet().parallelStream().filter(e -> canAdd(WorkflowPaths.getPath(e
//                    .getKey()), folders)).flatMap(e -> e.getValue().values().stream().flatMap(l -> l.stream()));
            
            ConcurrentMap<JWorkflowId, List<JOrder>> groupedByFolders = Stream.concat(orderStream, cycledOrderStream).collect(
                  Collectors.groupingByConcurrent(JOrder::workflowId));
            ConcurrentMap<JWorkflowId, Collection<String>> finalParamsPerWorkflow = groupedByFolders.keySet().parallelStream().collect(Collectors
                    .toConcurrentMap(Function.identity(), w -> OrdersHelper.getFinalParameters(w, currentState)));
            
            ToLongFunction<JOrder> compareScheduleFor = o -> o.scheduledFor().isPresent() ? o.scheduledFor().get().toEpochMilli() : surveyDateMillis;
            
            if (withWorkflowIdFilter && ordersFilter.getLimit() != null && ordersFilter.getLimit() > -1) {
                // consider limit per workflow (not over all)
                orderStream = groupedByFolders.entrySet().parallelStream().filter(e -> canAdd(WorkflowPaths.getPath(e.getKey().path().string()),
                        folders)).flatMap(e -> e.getValue().stream().sorted(Comparator.comparingLong(compareScheduleFor).reversed()).limit(
                                ordersFilter.getLimit().longValue()));
            } else {
                orderStream = groupedByFolders.entrySet().parallelStream().filter(e -> canAdd(WorkflowPaths.getPath(e.getKey().path().string()),
                        folders)).flatMap(e -> e.getValue().stream()).sorted(Comparator.comparingLong(compareScheduleFor).reversed());
                if (ordersFilter.getLimit() != null && ordersFilter.getLimit() > -1) {
                    orderStream = orderStream.limit(ordersFilter.getLimit().longValue());
                }
            }

            OrdersV entity = new OrdersV();
            entity.setSurveyDate(Date.from(surveyDateInstant));
            
            Map<JWorkflowId, Requirements> orderPreparations = new HashMap<>();
            
            Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                try {
                    OrderV order = OrdersHelper.mapJOrderToOrderV(o, ordersFilter.getCompact(), null, finalParamsPerWorkflow, surveyDateMillis);
                    order.setCyclicOrder(cycleInfos.get(order.getOrderId()));
                    if (orderStateWithRequirements.contains(order.getState().get_text())) {
                        if (!orderPreparations.containsKey(o.workflowId())) {
                            orderPreparations.put(o.workflowId(), OrdersHelper.getRequirements(o, currentState));
                        }
                        order.setRequirements(orderPreparations.get(o.workflowId()));
                    }
                    return order;
                } catch (Exception e) {
                    if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                        LOGGER.info(getJocError().printMetaInfo());
                        getJocError().clearMetaInfo();
                    }
                    LOGGER.error(String.format("[%s] %s", o.id().string(), e.toString()));
                    return null;
                }   
            };
            
            entity.setOrders(orderStream.parallel().map(mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
