package com.sos.joc.classes.order;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import js7.data.order.OrderId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.workflow.position.JPositionOrLabel;

public class FreshOrder {

    private OrderId newOrderId = null;
    private OrderId oldOrderId;
    private WorkflowPath workflowPath;
    private Map<String, Value> args = Collections.emptyMap();
    private Optional<Instant> scheduledFor = Optional.empty();
    private Optional<JPositionOrLabel> startPosition = Optional.empty();
    private Set<JPositionOrLabel> endPositions = Collections.emptySet();
    private boolean forceJobAdmission = false;
    // private boolean isDeleteWhenTerminated = false;

//    public FreshOrder(OrderId oldOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
//            JPosition startPosition, Option<Position> endPositions) {
//        this.oldOrderId = oldOrderId;
//        this.newOrderId = generateNewFromOldOrderId(oldOrderId);
//        this.workflowPath = workflowPath;
//        this.args = args;
//        this.scheduledFor = scheduledFor;
//        if (startPosition.toString().equals("0")) {
//            this.startPosition = Optional.of(startPosition);
//        }
//        if (endPositions.nonEmpty()) {
//            this.endPositions = Optional.of(JPosition.apply(endPositions.get()));
//        }
//    }
//
//    public FreshOrder(OrderId oldOrderId, OrderId newOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
//            JPosition startPosition, Option<Position> endPos) {
//        this.oldOrderId = oldOrderId;
//        this.newOrderId = newOrderId;
//        this.workflowPath = workflowPath;
//        this.args = args;
//        this.scheduledFor = scheduledFor;
//        if (!startPosition.toString().equals("0")) {
//            this.startPosition = Optional.of(startPosition);
//        }
//        if (endPos.nonEmpty()) {
//            this.endPos = Optional.of(JPosition.apply(endPos.get()));
//        }
//    }
    
    public FreshOrder(OrderId oldOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            Optional<JPositionOrLabel> startPosition, Set<JPositionOrLabel> endPositions, boolean forceJobAdmission, ZoneId zoneId) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = generateNewFromOldOrderId(oldOrderId, zoneId);
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
        this.startPosition = startPosition;
        this.endPositions = endPositions;
        this.forceJobAdmission = forceJobAdmission;
    }
    
    public FreshOrder(OrderId oldOrderId, OrderId newOrderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            Optional<JPositionOrLabel> startPosition, Set<JPositionOrLabel> endPositions, boolean forceJobAdmission) {
        this.oldOrderId = oldOrderId;
        this.newOrderId = newOrderId;
        this.workflowPath = workflowPath;
        this.args = args;
        this.scheduledFor = scheduledFor;
        this.startPosition = startPosition;
        this.endPositions = endPositions;
        this.forceJobAdmission = forceJobAdmission;
    }

    public OrderId getOldOrderId() {
        return oldOrderId;
    }

    public JFreshOrder getJFreshOrder() {
        return JFreshOrder.of(newOrderId, workflowPath, scheduledFor, args, false, forceJobAdmission, startPosition, endPositions);
    }

    public JFreshOrder getJFreshOrderWithDeleteOrderWhenTerminated() {
        return JFreshOrder.of(newOrderId, workflowPath, scheduledFor, args, true, forceJobAdmission, startPosition, endPositions);
    }

    private static OrderId generateNewFromOldOrderId(OrderId orderId, ZoneId zoneId) {
        return OrderId.of(OrdersHelper.generateNewFromOldOrderId(orderId.string(), zoneId));
    }

}
