package com.sos.joc.classes.order;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrdersResumePositions;
import com.sos.joc.model.order.Position;
import com.sos.joc.model.order.PositionChange;
import com.sos.joc.model.order.PositionChangeCode;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.Workflow;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import scala.Function1;

public class CheckedResumeOrdersPositions extends OrdersResumePositions {
    
    @JsonIgnore
    private boolean singleOrder = false;
    
    @JsonIgnore
    private Set<JOrder> jOrders = Collections.emptySet();
    
    @JsonIgnore
    private List<HistoricOutcome> historicOutcomes = Collections.emptyList();
    
    @JsonIgnore
    private JPosition currentPosition = null;
    
    @JsonIgnore
    private Set<Position> positionsWithImplicitEnds = new LinkedHashSet<>();
    
    public CheckedResumeOrdersPositions() {
        //
    }
    
    @JsonIgnore
    public CheckedResumeOrdersPositions get(Set<String> orders, JControllerState currentState, Set<Folder> permittedFolders) throws JsonParseException,
            JsonMappingException, IOException, JocException {
        
        if (orders.size() == 1) {
            singleOrder = true;
            return get(orders.iterator().next(), currentState, permittedFolders, null, true);
        }
        
        Function1<Order<Order.State>, Object> stateFilter = o -> o.isSuspended();
        Iterator<Function1<Order<Order.State>, Object>> failedStates = OrdersHelper.groupByStateClasses.entrySet().stream().filter(e -> e.getValue()
                .equals(OrderStateText.FAILED)).map(Map.Entry::getKey).map(JOrderPredicates::byOrderState).iterator();
        
        while (failedStates.hasNext()) {
            stateFilter = JOrderPredicates.or(stateFilter, failedStates.next());
        }
        
        stateFilter = JOrderPredicates.and(stateFilter, o -> orders.contains(o.id().string()));
        ConcurrentMap<JWorkflowId, Set<JOrder>> suspendedOrFailedOrders = currentState.ordersBy(stateFilter).collect(Collectors.groupingByConcurrent(
                JOrder::workflowId, Collectors.toSet()));

        if (suspendedOrFailedOrders.isEmpty()) {
            throw new JocBadRequestException("The orders are neither failed nor suspended");
        }
        
        Set<JWorkflowId> notPermittedWorkflows = suspendedOrFailedOrders.keySet().stream().filter(wId -> !OrdersHelper.canAdd(WorkflowPaths.getPath(
                wId), permittedFolders)).collect(Collectors.toSet());
        for (JWorkflowId notPermittedWorkflow : notPermittedWorkflows) {
            suspendedOrFailedOrders.remove(notPermittedWorkflow);
        }

        if (suspendedOrFailedOrders.isEmpty()) {
            throw new JocFolderPermissionsException("Access denied");
        }
        
        if (suspendedOrFailedOrders.size() > 1) {
            PositionChange pc = new PositionChange();
            pc.setCode(PositionChangeCode.NOT_ONE_WORKFLOW);
            pc.setMessage("The orders must be from the same workflow. Found workflows are: " + suspendedOrFailedOrders.keySet().toString());
            setDisabledPositionChange(pc);
            setOrderIds(orders);
            jOrders = suspendedOrFailedOrders.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
            //throw new JocBadRequestException("The orders must be from the same workflow. Found workflows are: " + map.keySet().toString());
        } else {

            JWorkflowId workflowId = suspendedOrFailedOrders.keySet().iterator().next();
            Either<Problem, JWorkflow> e = currentState.repo().idToCheckedWorkflow(workflowId);
            ProblemHelper.throwProblemIfExist(e);
            JWorkflow w = e.get();
            
            JsonNode node = Globals.objectMapper.readTree(w.withPositions().toJson());
            List<Instruction> instructions = Globals.objectMapper.reader().forType(new TypeReference<List<Instruction>>() {}).readValue(node.get("instructions"));
            //List<Instruction> instructions = Arrays.asList(Globals.objectMapper.reader().treeToValue(node.get("instructions"), Instruction[].class));
            Set<String> implicitEnds = WorkflowsHelper.extractImplicitEnds(instructions);

            setWorkflowId(new WorkflowId(WorkflowPaths.getPath(workflowId), workflowId.versionId().string()));

            final Map<String, Integer> counterPerPos = new HashMap<>();
            final Set<Position> pos = new LinkedHashSet<>();
            final Set<String> orderIds = new HashSet<>();
            jOrders = suspendedOrFailedOrders.get(workflowId);
            jOrders.forEach(o -> {
                orderIds.add(o.id().string());
                w.reachablePositions(o.workflowPosition().position()).stream().forEachOrdered(jPos -> {
                    Position p = createPosition(jPos, w.asScala());
                    //positionsWithImplicitEnds.add(p);
                    if (!implicitEnds.contains(p.getPositionString())) {
                        pos.add(p);
                        counterPerPos.putIfAbsent(p.getPositionString(), 0);
                        counterPerPos.computeIfPresent(p.getPositionString(), (key, value) -> value + 1);
                    }
                });
            });

            setOrderIds(orderIds);
            int countOrders = suspendedOrFailedOrders.get(workflowId).size();
            Set<String> commonPos = counterPerPos.entrySet().stream().filter(entry -> entry.getValue() == countOrders).map(Map.Entry::getKey).collect(
                    Collectors.toSet());

            setPositions(pos.stream().filter(p -> commonPos.contains(p.getPositionString())).collect(Collectors.toCollection(LinkedHashSet::new)));
            disableIfNoCommonAllowedPositionsExist();
        }

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        
        return this;
    }
    
    @JsonIgnore
    public CheckedResumeOrdersPositions get(String order, JControllerState currentState, Set<Folder> permittedFolders, String position,
            boolean withStatusCheck) throws JsonParseException, JsonMappingException, IOException, JocBadRequestException,
            JocFolderPermissionsException {

        JOrder jOrder = currentState.idToOrder().get(OrderId.of(order));
        if (jOrder == null) {
            throw new JocObjectNotExistException(String.format("Unknown OrderId: %s", order));
        }
        jOrders = Collections.singleton(jOrder);
        boolean isSuspendedOrFailed = OrdersHelper.isSuspendedOrFailed(jOrder);
        if (withStatusCheck && !isSuspendedOrFailed) {
            throw new JocBadRequestException("The order are neither failed nor suspended"); 
        }
        
        if (!OrdersHelper.canAdd(WorkflowPaths.getPath(jOrder.workflowId()), permittedFolders)) {
            throw new JocFolderPermissionsException("Access denied");
        }

        JWorkflowId workflowId = jOrder.workflowId();
        Either<Problem, JWorkflow> e = currentState.repo().idToCheckedWorkflow(workflowId);
        ProblemHelper.throwProblemIfExist(e);
        JWorkflow w = e.get();
        
        JsonNode node = Globals.objectMapper.readTree(w.withPositions().toJson());
        List<Instruction> instructions = Globals.objectMapper.reader().forType(new TypeReference<List<Instruction>>() {
        }).readValue(node.get("instructions"));
        // List<Instruction> instructions = Arrays.asList(Globals.objectMapper.reader().treeToValue(node.get("instructions"), Instruction[].class));
        Set<String> implicitEnds = WorkflowsHelper.extractImplicitEnds(instructions);

        setWorkflowId(new WorkflowId(WorkflowPaths.getPath(workflowId), workflowId.versionId().string()));

        final Set<Position> pos = new LinkedHashSet<>();
        w.reachablePositions(jOrder.workflowPosition().position()).stream().forEachOrdered(jPos -> {
            Position p = createPosition(jPos, w.asScala());
            boolean notImplicitEnd = !implicitEnds.contains(p.getPositionString());
            if (notImplicitEnd || jOrder.workflowPosition().position().toString().equals(jPos.toString())) {
                positionsWithImplicitEnds.add(p);
                if (notImplicitEnd) {
                    pos.add(p);
                }
            }
        });
        
        setOrderIds(Collections.singleton(jOrder.id().string()));
        setPositions(pos);

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        currentPosition = JPosition.apply(jOrder.asScala().position());
        if (position == null || position.isEmpty()) {
            position = currentPosition.toString();
        }
        if (pos.isEmpty()) {
            Position p = createPosition(currentPosition, w.asScala());
            pos.add(p);
            // TODO + ImplicitEnd of the Order's scope
            setVariablesNotSettable(true);
        } else {
            String firstPos = pos.iterator().next().getPositionString();
            setVariablesNotSettable(firstPos.equals(position));
        }
        
        setVariables(getVariables(jOrder, position, implicitEnds));
        
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
    
    @JsonIgnore
    public boolean isSingleOrder() {
        return singleOrder;
    }
    
    @JsonIgnore
    public Set<Position> getPositionsWithImplicitEnds() {
        return positionsWithImplicitEnds;
    }
    
    @JsonIgnore
    public Set<JOrder> getJOrders() {
        return jOrders;
    }
    
    @JsonIgnore
    public JPosition getCurrentPosition() {
        return currentPosition;
    }
    
    public int currentPositionCompareTo(JPosition pos) {
        // TODO that's wrong
        List<Object> position = pos.toList();
        List<Object> cPos = currentPosition.toList();
        int result = 0;
        for (int i = 0; i < cPos.size(); i += 2) {
            Integer c = (Integer) cPos.get(i);
            Integer p = (Integer) position.get(i);
            result = c.compareTo(p);
            if (result != 0) {
                break;
            }
        }
        return result;
    }
    
    @JsonIgnore
    public List<HistoricOutcome> getHistoricOutcomes() {
        if (historicOutcomes == null) {
            return Collections.emptyList();
        }
        return historicOutcomes;
    }
    
    @JsonIgnore
    public Variables getVariables(JOrder jOrder, List<Object> position, Set<String> implicitEnds) throws JsonParseException, JsonMappingException,
            IOException, JocBadRequestException {
        Either<Problem, JPosition> posFromList = JPosition.fromList(position);
        if (posFromList.isLeft()) {
            new JocBadRequestException(ProblemHelper.getErrorMessage(posFromList.getLeft()));
        }
        return getVariables(jOrder, posFromList.get().toString(), implicitEnds);
    }
    
    @JsonIgnore
    public Variables getVariables(JOrder jOrder, String positionString, Set<String> implicitEnds) throws JsonParseException, JsonMappingException,
            IOException, JocBadRequestException {
        Set<String> allowedPositions = getPositions().stream().map(Position::getPositionString).collect(Collectors.toCollection(LinkedHashSet::new));
        Variables variables = new Variables();
        String positionStringWithoutCounter = positionString.replaceAll("/(try|catch|cycle)\\+?[^:]*", "/$1");

        if (allowedPositions.contains(positionString) || allowedPositions.contains(positionStringWithoutCounter) || implicitEnds.contains(
                positionString) || implicitEnds.contains(positionStringWithoutCounter)) {
            OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
            historicOutcomes = oItem.getHistoricOutcomes();
            if (historicOutcomes != null) {
                for (HistoricOutcome outcome : historicOutcomes) {
                    String outcomePositionString = JPosition.fromList(outcome.getPosition()).get().toString();
                    String outcomePositionStringWithoutCounter = outcomePositionString.replaceAll("/(try|catch|cycle)\\+?[^:]*", "/$1");
                    if (outcomePositionStringWithoutCounter.equals(positionStringWithoutCounter)) {
                        break;
                    }
                    if (outcome.getOutcome() == null || outcome.getOutcome().getNamedValues() == null || outcome.getOutcome().getNamedValues()
                            .getAdditionalProperties() == null) {
                        continue;
                    }
                    if (!allowedPositions.contains(outcomePositionStringWithoutCounter)) {
                        continue;
                    }
                    variables.setAdditionalProperties(outcome.getOutcome().getNamedValues().getAdditionalProperties());
                }
            }
        } else {
            throw new JocBadRequestException("Disallowed position '" + positionString + "'. Allowed positions are: " + allowedPositions.toString());
        }

        return variables;
    }
    
    private static Position createPosition(JPosition jPos, Workflow w) {
        Position p = new Position();
        p.setPosition(jPos.toList());
        p.setPositionString(jPos.toString());
        p.setType(w.instruction(jPos.asScala()).instructionName().replace("Execute.Named", "Job"));
        if ("Job".equals(p.getType())) {
            try {
                p.setLabel(w.labeledInstruction(jPos.asScala()).toOption().get().labelString().trim().replaceFirst(":$", ""));
            } catch (Throwable e) {
                //
            }
        }
        return p;
    }

}
