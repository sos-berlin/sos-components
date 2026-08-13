package com.sos.joc.bean;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.event.EventServiceFactory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.order.OrderEvent;
import com.sos.joc.event.bean.proxy.ProxyCoupled;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersSummary;

import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import scala.Function1;

public class OrdersSnapshot implements OrdersSnapshotMBean, IJocMBean {

//    private static Map<String, OrdersSnapshot> instances = new HashMap<>();
    private final String controllerId;
    private OrdersSummary summary = init();
    private boolean initialized = false;
    private static final boolean onlyNext24hours = true;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersSnapshotMBean.class);
    
    public OrdersSnapshot(String controllerId) {
        this.controllerId = controllerId;
        EventBus.getInstance().register(this);
        if (Proxies.isCoupled(controllerId)) {
            update();
        }
    }

//    public static OrdersSnapshot getInstance(String controllerId) {
//        if (!instances.containsKey(controllerId)) {
//            instances.putIfAbsent(controllerId, new OrdersSnapshot(controllerId));
//        }
//        return instances.get(controllerId);
//    }

    @Subscribe({ OrderEvent.class })
    public void update(OrderEvent evt) {
        if ("OrderEvent".equals(evt.getKey()) && controllerId.equals(evt.getControllerId())) {
            JControllerState controllerState = null;
            try {
                controllerState = Proxy.of(controllerId).currentState();
            } catch (Exception e) {
                // Proxy-Connection-Problems
            }
            if (controllerState != null) {
                try {
                    setSnapshot(controllerState);
                } catch (Exception e) {
                    LOGGER.warn("Error at updating OrdersSnapshot metrics: ", e);
                }
            }
        }
    }
    
    @Subscribe({ ProxyCoupled.class })
    public void update(ProxyCoupled evt) {
        if (controllerId.equals(evt.getControllerId()) && evt.isCoupled()) {
            update();
        }
    }
    
    private void update() {
        if (!initialized) {
            JControllerState controllerState = null;
            try {
                controllerState = Proxy.of(controllerId).currentState();
            } catch (Exception e) {
                // Proxy-Connection-Problems
            }
            if (controllerState != null) {
                try {
                    setSnapshot(controllerState);
                    EventServiceFactory.startEventService(controllerId);
                    initialized = true;
                } catch (Exception e) {
                    LOGGER.warn("Error at updating OrdersSnapshot metrics: ", e);
                }
            }
        }
    }

    @Override
    public String objectName() {
        return getClass().getSimpleName();
    }
    
    private static OrdersSummary init() {
        OrdersSummary summary = new OrdersSummary();
        summary.setBlocked(0);
        summary.setPending(0);
        summary.setInProgress(0);
        summary.setRunning(0);
        summary.setFailed(0);
        summary.setSuspended(0);
        summary.setWaiting(0);
        summary.setTerminated(0);
        summary.setPrompting(0);
        return summary;
    }

    private void setSnapshot(JControllerState controllerState) {
        synchronized (summary) {
            final Instant now = controllerState.instant();
            Map<Class<? extends Order.State>, Integer> orderStates = null;
            int suspendedOrders = 0;
            Stream<JOrder> freshOrders = null;

            Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                    Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                            Order.ProcessingKilled$.class));
            Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
            Function1<Order<Order.State>, Object> notSuspendFilter = JOrderPredicates.not(suspendFilter);
            Function<JOrder, String> collapseCyclicOrders = o -> OrdersHelper.isFresh(o) ? OrdersHelper.getCyclicOrderIdMainPart(o.id().string()) : o
                    .id().string();

            orderStates = controllerState.orderStateToCount(notSuspendFilter);
            if (orderStates.getOrDefault(Order.Fresh.class, 0) > 0) {
                freshOrders = controllerState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class), notSuspendFilter));
            }
            suspendedOrders = controllerState.ordersBy(suspendFilter).map(collapseCyclicOrders).distinct().mapToInt(e -> 1).sum();

            int numOfBlockedOrders = 0;
            int numOfPendingOrders = 0;
            int numOfFreshOrders = 0;
            int numOfWaitingForAdmissionOrders = 0;
            if (freshOrders != null) {
                Set<JOrder> freshOrderSet = freshOrders.collect(Collectors.toSet());

                Set<OrderId> blockedOrderIds = freshOrderSet.stream().filter(o -> {
                    Instant scheduledFor = OrdersHelper.getScheduledForInstant(o);
                    return scheduledFor != null && scheduledFor.isBefore(now);
                }).map(o -> o.id()).collect(Collectors.toSet());

                Set<OrderId> waitingForAdmissionOrderIds = OrdersHelper.getWaitingForAdmissionOrderIds(blockedOrderIds, controllerState);
                waitingForAdmissionOrderIds.forEach(i -> blockedOrderIds.remove(i));
                numOfWaitingForAdmissionOrders = waitingForAdmissionOrderIds.size();

                numOfBlockedOrders = blockedOrderIds.stream().map(oId -> OrdersHelper.getCyclicOrderIdMainPart(oId.string())).distinct().mapToInt(
                        item -> 1).sum();

                numOfPendingOrders = freshOrderSet.stream().filter(o -> {
                    Optional<Instant> scheduledFor = o.scheduledFor();
                    return scheduledFor.isPresent() && scheduledFor.get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS.longValue();
                }).map(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string())).distinct().mapToInt(item -> 1).sum();

                Stream<JOrder> freshOrderStream = freshOrderSet.stream();
                if (onlyNext24hours) {
                    final Instant until = now.plusSeconds(60 * 60 * 24);
                    Predicate<JOrder> dateToFilter = o -> {
                        Instant scheduledFor = OrdersHelper.getScheduledForInstant(o);
                        return scheduledFor == null || scheduledFor.isBefore(until);
                    };
                    freshOrderStream = freshOrderSet.stream().filter(dateToFilter);
                }
                numOfFreshOrders = freshOrderStream.map(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string())).distinct().mapToInt(
                        e -> 1).sum() - numOfPendingOrders;

            }

            final Map<OrderStateText, Integer> map = orderStates.entrySet().stream().collect(Collectors.groupingBy(
                    entry -> OrdersHelper.groupByStateClasses.getOrDefault(entry.getKey(), OrderStateText.UNKNOWN), Collectors.summingInt(
                            entry -> entry.getValue())));
            map.put(OrderStateText.BLOCKED, numOfBlockedOrders);
            map.put(OrderStateText.PENDING, numOfPendingOrders);
            map.put(OrderStateText.SCHEDULED, Math.max(0, numOfFreshOrders - numOfBlockedOrders - numOfWaitingForAdmissionOrders));
            OrdersHelper.groupByStateClasses.values().stream().distinct().forEach(state -> map.putIfAbsent(state, 0));

            summary.setBlocked(map.getOrDefault(OrderStateText.BLOCKED, 0));
            summary.setScheduled(map.getOrDefault(OrderStateText.SCHEDULED, 0));
            summary.setPending(map.getOrDefault(OrderStateText.PENDING, 0));
            summary.setInProgress(map.getOrDefault(OrderStateText.INPROGRESS, 0) + numOfWaitingForAdmissionOrders);
            summary.setRunning(map.getOrDefault(OrderStateText.RUNNING, 0));
            summary.setFailed(map.getOrDefault(OrderStateText.FAILED, 0));
            summary.setSuspended(suspendedOrders);
            summary.setWaiting(map.getOrDefault(OrderStateText.WAITING, 0));
            summary.setTerminated(map.getOrDefault(OrderStateText.CANCELLED, 0) + map.getOrDefault(OrderStateText.FINISHED, 0));
            summary.setPrompting(map.getOrDefault(OrderStateText.PROMPTING, 0));
        }
    }

    @Override
    public int getBlockedOrders() {
        return summary.getBlocked();
    }

    @Override
    public int getPendingOrders() {
        return summary.getPending();
    }

    @Override
    public int getInProgessOrders() {
        return summary.getInProgress();
    }

    @Override
    public int getRunningOrders() {
        return summary.getRunning();
    }

    @Override
    public int getFailedOrders() {
        return summary.getFailed();
    }

    @Override
    public int getWaitingOrders() {
        return summary.getWaiting();
    }
    
    @Override
    public int getSuspendedOrders() {
        return summary.getSuspended();
    }

    @Override
    public int getTerminatedOrders() {
        return summary.getTerminated();
    }

    @Override
    public int getPromptingOrders() {
        return summary.getPrompting();
    }

}
