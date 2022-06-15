package com.sos.joc.classes.order;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import js7.data.order.OrderId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.position.Position;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.workflow.position.JPosition;
import scala.Option;

public class FreshOrder {

    private OrderId newOrderId = null;
    private OrderId oldOrderId;
    private WorkflowPath workflowPath;
    private Map<String, Value> args = Collections.emptyMap();
    private Optional<Instant> scheduledFor = Optional.empty();
    private Optional<JPosition> startPos = Optional.empty();
    private Optional<JPosition> endPos = Optional.empty();
    // private boolean isDeleteWhenTerminated = false;

    public FreshOrder(OrderId oldOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            JPosition startPos, Option<Position> endPos) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = generateNewFromOldOrderId(oldOrderId);
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
        if (startPos.toString().equals("0")) {
            this.startPos = Optional.of(startPos);
        }
        if (endPos.nonEmpty()) {
            this.endPos = Optional.of(JPosition.apply(endPos.get()));
        }
    }

    public FreshOrder(OrderId oldOrderId, OrderId newOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            JPosition startPos, Option<Position> endPos) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = newOrderId;
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
        if (!startPos.toString().equals("0")) {
            this.startPos = Optional.of(startPos);
        }
        if (endPos.nonEmpty()) {
            this.endPos = Optional.of(JPosition.apply(endPos.get()));
        }
    }
    
    public FreshOrder(OrderId oldOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            Optional<JPosition> startPos, Optional<JPosition> endPos) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = generateNewFromOldOrderId(oldOrderId);
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public FreshOrder(OrderId oldOrderId, OrderId newOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            Optional<JPosition> startPos, Optional<JPosition> endPos) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = newOrderId;
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public OrderId getOldOrderId() {
        return oldOrderId;
    }

    public JFreshOrder getJFreshOrder() {
        return JFreshOrder.of(newOrderId, workflowPath, scheduledFor, args, false, startPos, endPos);
    }

    public JFreshOrder getJFreshOrderWithDeleteOrderWhenTerminated() {
        return JFreshOrder.of(newOrderId, workflowPath, scheduledFor, args, true, startPos, endPos);
    }

    private static OrderId generateNewFromOldOrderId(OrderId orderId) {
        return OrderId.of(OrdersHelper.generateNewFromOldOrderId(orderId.string()));
    }

}
