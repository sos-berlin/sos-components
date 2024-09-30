package com.sos.joc.orders.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.joc.orders.resource.IOrdersResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
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
    private final List<OrderStateText> allowedStateDateStates = Arrays.asList(OrderStateText.INPROGRESS, OrderStateText.RUNNING,
            OrderStateText.FAILED, OrderStateText.FINISHED, OrderStateText.CANCELLED);

    @Override
    public JOCDefaultResponse postOrders(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
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
            boolean responseWithLabel = withWorkflowIdFilter && workflowIds.size() == 1;
            boolean withStateDate = ordersFilter.getStateDateFrom() != null || ordersFilter.getStateDateTo() != null;
            boolean withOrderTags = ordersFilter.getOrderTags() != null && !ordersFilter.getOrderTags().isEmpty();
            boolean withWorkflowTags = ordersFilter.getWorkflowTags() != null && !ordersFilter.getWorkflowTags().isEmpty();
            //boolean orderStreamIsEmpty = false;
            if (ordersFilter.getLimit() == null) {
                ordersFilter.setLimit(10000);
            }
            if (withOrderIdFilter) {
                ordersFilter.setFolders(null);
                ordersFilter.setLimit(-1);
            }

            boolean withFolderFilter = ordersFilter.getFolders() != null && !ordersFilter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(ordersFilter.getFolders());
            ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();

            JControllerState currentState = Proxy.of(ordersFilter.getControllerId()).currentState();
            Instant surveyDateInstant = currentState.instant();
            Long surveyDateMillis = surveyDateInstant.toEpochMilli();

            List<OrderStateText> states = ordersFilter.getStates();
            boolean stateDateDisallowed = states.stream().anyMatch(s -> !allowedStateDateStates.contains(s));
            // BLOCKED is not a Controller state. It needs a special handling. These are SCHEDULED with scheduledFor in the past
            final boolean withStatesFilter = states != null && !states.isEmpty();
            final boolean lookingForBlocked = withStatesFilter && states.contains(OrderStateText.BLOCKED);
            final boolean lookingForPending = withStatesFilter && states.contains(OrderStateText.PENDING);
            final boolean lookingForScheduled = withStatesFilter && states.contains(OrderStateText.SCHEDULED);
            final boolean lookingForInProgress = withStatesFilter && states.contains(OrderStateText.INPROGRESS);

            Function1<Order<Order.State>, Object> cycledOrderFilter = null;
            Function1<Order<Order.State>, Object> notCycledOrderFilter = null;
            Function1<Order<Order.State>, Object> stateFilter = null;
            Function1<Order<Order.State>, Object> freshOrderFilter = null;

            Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                    Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                            Order.ProcessingKilled$.class));
            Function1<Order<Order.State>, Object> freshFilter = JOrderPredicates.byOrderState(Order.Fresh$.class);
            Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
            Function1<Order<Order.State>, Object> notSuspendFilter = JOrderPredicates.not(suspendFilter);
            Function1<Order<Order.State>, Object> cyclicFilter = o -> OrdersHelper.isCyclicOrderId(o.id().string());
            Function1<Order<Order.State>, Object> freshCyclicFilter = JOrderPredicates.and(freshFilter, cyclicFilter);
            Function1<Order<Order.State>, Object> notCyclicFilter = JOrderPredicates.not(cyclicFilter);
            Function1<Order<Order.State>, Object> blockedFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o
                    .isSuspended() && OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis);

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
                            stateFilter = suspendFilter;
                        } else {
                            stateFilter = JOrderPredicates.or(suspendFilter, stateFilter);
                        }
                    } else {
                        if (stateFilter != null) {
                            stateFilter = JOrderPredicates.and(notSuspendFilter, stateFilter);
                        }
                    }

                    if (lookingForScheduled && !lookingForBlocked && !lookingForPending) {
                        freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) >= surveyDateMillis && o.scheduledFor().get()
                                .toEpochMilli() != JobSchedulerDate.NEVER_MILLIS;
                    } else if (lookingForScheduled && lookingForBlocked && !lookingForPending) {
                        freshOrderFilter = o -> o.scheduledFor().isEmpty() || (!o.scheduledFor().isEmpty() && o.scheduledFor().get()
                                .toEpochMilli() != JobSchedulerDate.NEVER_MILLIS);
                    } else if (lookingForScheduled && !lookingForBlocked && lookingForPending) {
                        freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) >= surveyDateMillis;
                    } else if (!lookingForScheduled && lookingForBlocked && !lookingForPending) {
                        freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis;
                    } else if (!lookingForScheduled && !lookingForBlocked && lookingForPending) {
                        freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
                    } else if (!lookingForScheduled && lookingForBlocked && lookingForPending) {
                        freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis || o.scheduledFor().get()
                                .toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
                    }

                    if (freshOrderFilter != null) {
                        freshOrderFilter = JOrderPredicates.and(freshFilter, freshOrderFilter);
                    } else if (lookingForScheduled && lookingForBlocked && lookingForPending) {
                        freshOrderFilter = freshFilter;
                    }

                    //consider that fresh orders can be suspended and SUSPENDED beats PENDING, BLOCKED, SCHEDULED
                    if (freshOrderFilter != null) {
                        if (!states.contains(OrderStateText.SUSPENDED)) {
                            freshOrderFilter = JOrderPredicates.and(freshOrderFilter, o -> !o.isSuspended()); 
                        }
                        cycledOrderFilter = JOrderPredicates.and(freshOrderFilter, cyclicFilter);
                        freshOrderFilter = JOrderPredicates.and(freshOrderFilter, notCyclicFilter);
                    }

                    if (stateFilter == null) {
                        if (freshOrderFilter != null) {
                            notCycledOrderFilter = freshOrderFilter;
                        }
                    } else {
                        if (freshOrderFilter != null) {
                            notCycledOrderFilter = JOrderPredicates.or(stateFilter, freshOrderFilter);
                        } else {
                            //consider that suspended orders can be fresh -> maybe cyclic orders exist that needs one reference
                            if (states.contains(OrderStateText.SUSPENDED)) {
                                cycledOrderFilter = JOrderPredicates.and(stateFilter, freshCyclicFilter);
                                notCycledOrderFilter = JOrderPredicates.and(stateFilter, JOrderPredicates.not(freshCyclicFilter));
                            } else {
                                notCycledOrderFilter = stateFilter;
                            }
                        }
                    }

                } else {
                    cycledOrderFilter = freshCyclicFilter;
                    notCycledOrderFilter = JOrderPredicates.not(freshCyclicFilter);
                }
            }
            
            Stream<JOrder> orderStream = Stream.empty();
            Stream<JOrder> cycledOrderStream = Stream.empty();
            Stream<JOrder> blockedOrderStream = Stream.empty();

            if (withOrderIdFilter) {
                ordersFilter.setRegex(null);
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
                blockedOrderStream = currentState.ordersBy(JOrderPredicates.and(o -> orders.contains(o.id().string()), blockedFilter));

            } else if (withWorkflowIdFilter) {
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
                if (!withStatesFilter || lookingForBlocked || lookingForInProgress) {
                    blockedOrderStream = currentState.ordersBy(JOrderPredicates.and(workflowFilter, blockedFilter));
                }
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
                // orderStream = currentState.ordersBy(JOrderPredicates.none());
                //orderStreamIsEmpty = true;
            } else {
                if (notCycledOrderFilter != null) {
                    orderStream = currentState.ordersBy(notCycledOrderFilter);
                }
                if (cycledOrderFilter != null) {
                    cycledOrderStream = currentState.ordersBy(cycledOrderFilter);
                }
                if (!withStatesFilter || lookingForBlocked || lookingForInProgress) {
                    blockedOrderStream = currentState.ordersBy(blockedFilter);
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
                        if (!o.asScala().isSuspended() && OrderStateText.SCHEDULED.equals(OrdersHelper.getGroupedState(o.asScala().state()
                                .getClass()))) {
                            Instant scheduledFor = OrdersHelper.getScheduledForInstant(o, zoneId);
                            if (scheduledFor != null && scheduledFor.isAfter(until)) {
                                if ((!withStatesFilter || lookingForPending) && scheduledFor.toEpochMilli() == JobSchedulerDate.NEVER_MILLIS) {
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
            
            Set<JOrder> blockedOrders = blockedOrderStream.collect(Collectors.toSet());
            ConcurrentMap<OrderId, JOrder> blockedButWaitingForAdmissionOrders = OrdersHelper.getWaitingForAdmissionOrders(blockedOrders, currentState);
            Set<OrderId> blockedButWaitingForAdmissionOrderIds = blockedButWaitingForAdmissionOrders.keySet();
            if (lookingForBlocked && !lookingForInProgress) {
                orderStream = orderStream.filter(o -> !blockedButWaitingForAdmissionOrderIds.contains(o.id()));
            } else if (!lookingForBlocked && lookingForInProgress) {
                orderStream = Stream.concat(orderStream, blockedButWaitingForAdmissionOrders.values().stream()).distinct();
            }
            
            // grouping cycledOrders and return the first pending Order of the group to orderStream
            Comparator<JOrder> comp = Comparator.comparing(o -> o.id().string());
            Collection<TreeSet<JOrder>> cycledOrderColl = cycledOrderStream.filter(o -> !blockedButWaitingForAdmissionOrderIds.contains(o.id()))
                    .collect(Collectors.groupingBy(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string()), Collectors.toCollection(
                            () -> new TreeSet<>(comp)))).values();
            cycledOrderStream = cycledOrderColl.stream().parallel().map(t -> t.first());

            Function<TreeSet<JOrder>, CyclicOrderInfos> getCyclicOrderInfos = t -> {
                CyclicOrderInfos cycle = new CyclicOrderInfos();
                cycle.setCount(t.size());
                cycle.setFirstOrderId(t.first().id().string());
                t.first().scheduledFor().ifPresent(opt -> cycle.setFirstStart(Date.from(opt)));
                cycle.setLastOrderId(t.last().id().string());
                t.last().scheduledFor().ifPresent(opt -> cycle.setLastStart(Date.from(opt)));
                return cycle;
            };
            ConcurrentMap<String, CyclicOrderInfos> cycleInfos = cycledOrderColl.stream().parallel().filter(t -> !t.isEmpty()).map(
                    getCyclicOrderInfos).collect(Collectors.toConcurrentMap(CyclicOrderInfos::getFirstOrderId, Function.identity()));

            // merge cycledOrders to orderStream and grouping by WorkflowId for folder permissions
            ConcurrentMap<JWorkflowId, List<JOrder>> groupedByWorkflowIds = Stream.concat(orderStream, cycledOrderStream).collect(Collectors
                    .groupingByConcurrent(JOrder::workflowId));
            
            if (withWorkflowTags) {
                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                InventoryTagDBLayer tagDbLayer = new InventoryTagDBLayer(connection);
                List<String> taggedWorkflowNames = tagDbLayer.getWorkflowNamesHavingTags(ordersFilter.getWorkflowTags().stream().map(GroupedTag::new)
                        .map(GroupedTag::getTag).collect(Collectors.toList()));

                groupedByWorkflowIds.keySet().removeIf(wId -> !taggedWorkflowNames.contains(wId.path().string()));
            }
            
            ConcurrentMap<JWorkflowId, Collection<String>> finalParamsPerWorkflow = groupedByWorkflowIds.keySet().parallelStream().collect(Collectors
                    .toConcurrentMap(Function.identity(), w -> OrdersHelper.getFinalParameters(w, currentState)));

            ToLongFunction<JOrder> compareScheduleFor = OrdersHelper.getCompareScheduledFor(zoneId, surveyDateMillis);

            if (withWorkflowIdFilter && ordersFilter.getLimit() != null && ordersFilter.getLimit() > -1) {
                // consider limit per workflow (not over all)
                orderStream = groupedByWorkflowIds.entrySet().parallelStream().filter(e -> canAdd(WorkflowPaths.getPath(e.getKey()), folders))
                        .flatMap(e -> e.getValue().stream().sorted(Comparator.comparingLong(compareScheduleFor).reversed()).limit(ordersFilter
                                .getLimit().longValue()));
            } else {
                orderStream = groupedByWorkflowIds.entrySet().parallelStream().filter(e -> canAdd(WorkflowPaths.getPath(e.getKey().path().string()),
                        folders)).flatMap(e -> e.getValue().stream()).sorted(Comparator.comparingLong(compareScheduleFor).reversed());
                if (ordersFilter.getLimit() != null && ordersFilter.getLimit() > -1) {
                    orderStream = orderStream.limit(ordersFilter.getLimit().longValue());
                }
            }
            
            OrdersV entity = new OrdersV();
            entity.setSurveyDate(Date.from(surveyDateInstant));
            entity.setOrders(Collections.emptyList());
            
            List<JOrder> jOrders = orderStream.collect(Collectors.toList());
            
            if (!jOrders.isEmpty()) {
                if (connection == null) {
                    connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                }
                
                Map<String, Set<String>> orderTags = OrderTags.getTags(withOrderTags, ordersFilter.getControllerId(), jOrders, connection);
                if (withOrderTags) {
                    orderStream = OrderTags.filter(jOrders, orderTags, ordersFilter.getOrderTags());
                } else {
                    orderStream = jOrders.stream();
                }

                if (withStateDate && !stateDateDisallowed) {
                    String regex = "[+-]?(\\d+)\\s*([smhdwMy])"; // relative date with only one unit
                    if (ordersFilter.getStateDateFrom() != null) {
                        if (ordersFilter.getStateDateFrom().trim().matches(regex)) {
                            ordersFilter.setStateDateFrom(ordersFilter.getStateDateFrom().trim().replaceFirst(regex, "-$1$2"));
                        }
                    }
                    if (ordersFilter.getStateDateTo() != null) {
                        if (ordersFilter.getStateDateTo().trim().matches(regex)) {
                            ordersFilter.setStateDateTo(ordersFilter.getStateDateTo().trim().replaceFirst(regex, "-$1$2"));
                        }
                    }
                    HistoryFilter filter = new HistoryFilter();
                    filter.setControllerIds(Collections.singleton(ordersFilter.getControllerId()));
                    filter.addFolders(folders);
                    filter.setOrderState(states);
                    filter.setStateFrom(JobSchedulerDate.getDateFrom(ordersFilter.getStateDateFrom(), ordersFilter.getTimeZone()));
                    filter.setStateTo(JobSchedulerDate.getDateTo(ordersFilter.getStateDateTo(), ordersFilter.getTimeZone()));
                    JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(connection, filter);
                    List<String> stateDateOrderIds = dbLayer.getOrderIds();

                    orderStream = orderStream.filter(o -> stateDateOrderIds.contains(o.id().string()));
                }

                Map<List<Object>, String> positionToLabelsMap = responseWithLabel ? getPositionToLabelsMap(ordersFilter.getControllerId(), workflowIds
                        .iterator().next()) : null;

                Set<String> childOrders = OrdersHelper.getChildOrders(currentState);

                Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                    try {
                        OrderV order = OrdersHelper.mapJOrderToOrderV(o, currentState, ordersFilter.getCompact(), null, orderTags,
                                blockedButWaitingForAdmissionOrderIds, finalParamsPerWorkflow, surveyDateMillis, zoneId);
                        order.setCyclicOrder(cycleInfos.get(order.getOrderId()));
                        order.setHasChildOrders(childOrders.stream().anyMatch(s -> s.startsWith(order.getOrderId() + "|")));
                        if (responseWithLabel) {
                            order.setLabel(positionToLabelsMap.get(order.getPosition()));
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
                
                if (ordersFilter.getWithoutWorkflowTags() != Boolean.TRUE && WorkflowsHelper.withWorkflowTagsDisplayed()) {
                    if (connection == null) {
                        connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                    }
                    entity.setWorkflowTagsPerWorkflow(WorkflowsHelper.getTagsPerWorkflow(connection, groupedByWorkflowIds.keySet().stream().map(
                            JWorkflowId::path).map(WorkflowPath::string)));
                }
            }
            
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static Map<List<Object>, String> getPositionToLabelsMap(String controllerId, WorkflowId workflowId) {
        try {
            return WorkflowsHelper.getPositionToLabelsMapFromDepHistory(controllerId, workflowId);
        } catch (Exception e) {
            LOGGER.warn("Cannot map order position to Workflow instruction label: ", e);
            return Collections.emptyMap();
        }
    }
    
}
