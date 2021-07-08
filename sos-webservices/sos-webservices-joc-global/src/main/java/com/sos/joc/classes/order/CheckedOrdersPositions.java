package com.sos.joc.classes.order;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrdersPositions;
import com.sos.joc.model.order.PositionChange;
import com.sos.joc.model.order.PositionChangeCode;
import com.sos.joc.model.order.Positions;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;

public class CheckedOrdersPositions extends OrdersPositions {
    
    @JsonIgnore
    private Set<JOrder> notSuspendedOrFailedOrders = Collections.emptySet();
    
    @JsonIgnore
    private boolean singleOrder = false;
    
    @JsonIgnore
    private Set<JOrder> jOrders = Collections.emptySet();
    
    public CheckedOrdersPositions() {
        //
    }
    
    @JsonIgnore
    public CheckedOrdersPositions get(Set<String> orders, JControllerState currentState, Set<Folder> permittedFolders) throws JsonParseException,
            JsonMappingException, IOException, JocException {
        
        if (orders.size() == 1) {
            singleOrder = true;
            return get(orders.iterator().next(), currentState, permittedFolders, null, true);
        }

        Stream<JOrder> orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));

        Map<Boolean, Set<JOrder>> suspendedOrFailedOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isSuspendedOrFailed(o),
                Collectors.toSet()));

        if (!suspendedOrFailedOrders.containsKey(Boolean.TRUE)) {
            throw new JocBadRequestException("The orders are neither failed nor suspended");
        }

        orderStream = suspendedOrFailedOrders.getOrDefault(Boolean.TRUE, Collections.emptySet()).stream().filter(o -> OrdersHelper.canAdd(WorkflowPaths.getPath(o
                .workflowId()), permittedFolders));

        Map<JWorkflowId, Set<JOrder>> map = orderStream.collect(Collectors.groupingBy(o -> o.workflowId(), Collectors.toSet()));

        if (map.isEmpty()) {
            throw new JocFolderPermissionsException("access denied");
        }
        
        if (map.size() > 1) {
            PositionChange pc = new PositionChange();
            pc.setCode(PositionChangeCode.NOT_ONE_WORKFLOW);
            pc.setMessage("The orders must be from the same workflow. Found workflows are: " + map.keySet().toString());
            setDisabledPositionChange(pc);
            setOrderIds(orders);
            //throw new JocBadRequestException("The orders must be from the same workflow. Found workflows are: " + map.keySet().toString());
        } else {

            JWorkflowId workflowId = map.keySet().iterator().next();
            Either<Problem, JWorkflow> e = currentState.repo().idToWorkflow(workflowId);
            ProblemHelper.throwProblemIfExist(e);
            Workflow workflow = Globals.objectMapper.readValue(e.get().withPositions().toJson(), Workflow.class);
            Set<String> implicitEnds = WorkflowsHelper.extractImplicitEnds(workflow.getInstructions());

            setWorkflowId(new WorkflowId(WorkflowPaths.getPath(workflowId), workflowId.versionId().string()));

            final Map<String, Integer> counterPerPos = new HashMap<>();
            final Set<Positions> pos = new LinkedHashSet<>();
            final Set<String> orderIds = new HashSet<>();
            jOrders = map.get(workflowId);
            jOrders.forEach(o -> {
                orderIds.add(o.id().string());
                e.get().reachablePositions(o.workflowPosition().position()).stream().forEachOrdered(jPos -> {
                    String positionString = jPos.toString();
                    Positions p = new Positions();
                    p.setPosition(jPos.toList());
                    p.setPositionString(positionString);
                    if (!implicitEnds.contains(p.getPositionString())) {
                        pos.add(p);
                        counterPerPos.putIfAbsent(positionString, 0);
                        counterPerPos.computeIfPresent(positionString, (key, value) -> value + 1);
                    }
                });
            });

            setOrderIds(orderIds);
            int countOrders = map.get(workflowId).size();
            Set<String> commonPos = counterPerPos.entrySet().stream().filter(entry -> entry.getValue() == countOrders).map(Map.Entry::getKey).collect(
                    Collectors.toSet());

            setPositions(pos.stream().filter(p -> commonPos.contains(p.getPositionString())).collect(Collectors.toCollection(LinkedHashSet::new)));
            disableIfNoCommonAllowedPositionsExist();
        }

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        
        if (suspendedOrFailedOrders.containsKey(Boolean.FALSE)) {
            notSuspendedOrFailedOrders = suspendedOrFailedOrders.get(Boolean.FALSE);
        }
        
        return this;
    }
    
    @JsonIgnore
    public CheckedOrdersPositions get(String order, JControllerState currentState, Set<Folder> permittedFolders, String position,
            boolean withStatusCheck) throws JsonParseException, JsonMappingException, IOException, JocBadRequestException,
            JocFolderPermissionsException {

        Either<Problem, JOrder> orderE = currentState.idToCheckedOrder(OrderId.of(order));
        ProblemHelper.throwProblemIfExist(orderE);
        
        JOrder jOrder = orderE.get();
        jOrders = Collections.singleton(jOrder);
        boolean isSuspendedOrFailed = OrdersHelper.isSuspendedOrFailed(jOrder);
        if (withStatusCheck && !isSuspendedOrFailed) {
            throw new JocBadRequestException("The order are neither failed nor suspended"); 
        }
        
        if (!OrdersHelper.canAdd(WorkflowPaths.getPath(jOrder.workflowId()), permittedFolders)) {
            throw new JocFolderPermissionsException("access denied");
        }

        JWorkflowId workflowId = jOrder.workflowId();
        Either<Problem, JWorkflow> e = currentState.repo().idToWorkflow(workflowId);
        ProblemHelper.throwProblemIfExist(e);
        Workflow workflow = Globals.objectMapper.readValue(e.get().withPositions().toJson(), Workflow.class);
        Set<String> implicitEnds = WorkflowsHelper.extractImplicitEnds(workflow.getInstructions());
        
        setWorkflowId(new WorkflowId(WorkflowPaths.getPath(workflowId), workflowId.versionId().string()));

        final Set<Positions> pos = new LinkedHashSet<>();
        e.get().reachablePositions(jOrder.workflowPosition().position()).stream().forEachOrdered(jPos -> {
            Positions p = new Positions();
            p.setPosition(jPos.toList());
            p.setPositionString(jPos.toString());
            if (!implicitEnds.contains(p.getPositionString())) {
                pos.add(p);
            }
        });
        
        setOrderIds(Collections.singleton(jOrder.id().string()));
        setPositions(pos);

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        if (position == null || position.isEmpty()) {
            position = JPosition.apply(jOrder.asScala().position()).toString();
        }
        setVariables(getVariables(jOrder, position));
        
        return this;
    }
    
    public void disableIfNoCommonAllowedPositionsExist() throws JocBadRequestException {
        if (getPositions().isEmpty() && getOrderIds().size() > 1) {
            PositionChange pc = new PositionChange();
            pc.setCode(PositionChangeCode.NO_COMMON_POSITIONS);
            pc.setMessage("The orders " + getOrderIds().toString() + " don't have common allowed positions.");
            setDisabledPositionChange(pc);
            //throw new JocBadRequestException("The orders " + getOrderIds().toString() + " don't have common allowed positions.");
        }
    }
    
    public boolean hasNotSuspendedOrFailedOrders() {
        return !notSuspendedOrFailedOrders.isEmpty();
    }
    
    @JsonIgnore
    public boolean isSingleOrder() {
        return singleOrder;
    }
    
    @JsonIgnore
    public Set<JOrder> getJOrders() {
        return jOrders;
    }
    
    @JsonIgnore
    public String getNotSuspendedOrFailedOrdersMessage() {
        if (hasNotSuspendedOrFailedOrders()) {
            return notSuspendedOrFailedOrders.stream().map(o -> o.id().string()).collect(Collectors.joining("', '", "Orders '",
                    "' not failed or suspended"));
        }
        return "";
    }
    
    @JsonIgnore
    public Variables getVariables(JOrder jOrder, List<Object> position) throws JsonParseException, JsonMappingException, IOException,
            JocBadRequestException {
        Either<Problem, JPosition> posFromList = JPosition.fromList(position);
        if (posFromList.isLeft()) {
            new JocBadRequestException(ProblemHelper.getErrorMessage(posFromList.getLeft()));
        }
        return getVariables(jOrder, posFromList.get().toString());
    }
    
    @JsonIgnore
    public Variables getVariables(JOrder jOrder, String positionString) throws JsonParseException, JsonMappingException, IOException,
            JocBadRequestException {
        Set<String> allowedPositions = getPositions().stream().map(Positions::getPositionString).collect(Collectors.toCollection(LinkedHashSet::new));
        Variables variables = new Variables();
        
        if (allowedPositions.contains(positionString)) {
            OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
            List<HistoricOutcome> outcomes = oItem.getHistoricOutcomes();
            for (HistoricOutcome outcome : outcomes) {
                String outcomePositionString = JPosition.fromList(outcome.getPosition()).get().toString();
                if (outcomePositionString.equals(positionString)) {
                    break;
                }
                if (outcome.getOutcome() == null || outcome.getOutcome().getNamedValues() == null || outcome.getOutcome().getNamedValues()
                        .getAdditionalProperties() == null) {
                    continue;
                }
                if (!allowedPositions.contains(outcomePositionString)) {
                    continue;
                }
                variables.setAdditionalProperties(outcome.getOutcome().getNamedValues().getAdditionalProperties());
            }
        } else {
            throw new JocBadRequestException("Disallowed position '" + positionString + "'. Allowed positions are: " + allowedPositions.toString());
        }
        
        return variables;
    }

}
