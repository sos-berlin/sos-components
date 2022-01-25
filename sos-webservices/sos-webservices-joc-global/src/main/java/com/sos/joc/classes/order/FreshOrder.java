package com.sos.joc.classes.order;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import js7.data.order.OrderId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.order.JFreshOrder;

public class FreshOrder {

    private OrderId newOrderId = null;
    private OrderId oldOrderId;
    private WorkflowPath workflowPath;
    private Map<String, Value> args = Collections.emptyMap();
    private Optional<Instant> scheduledFor = Optional.empty();
    // private boolean isDeleteWhenTerminated = false;

    public FreshOrder(OrderId oldOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = generateNewFromOldOrderId(oldOrderId);
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
    }

    public FreshOrder(OrderId oldOrderId, OrderId newOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = newOrderId;
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
    }

    public OrderId getOldOrderId() {
        return oldOrderId;
    }

    public JFreshOrder getJFreshOrder() {
        return JFreshOrder.of(newOrderId, workflowPath, scheduledFor, args);
    }

    public JFreshOrder getJFreshOrderWithDeleteOrderWhenTerminated() {
        return JFreshOrder.of(newOrderId, workflowPath, scheduledFor, args, true);
    }

    private static OrderId generateNewFromOldOrderId(OrderId orderId) {
        return OrderId.of(OrdersHelper.generateNewFromOldOrderId(orderId.string()));
    }

}
