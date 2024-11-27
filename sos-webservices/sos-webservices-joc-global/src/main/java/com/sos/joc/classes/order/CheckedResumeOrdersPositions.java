package com.sos.joc.classes.order;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrdersResumePositions;
import com.sos.joc.model.order.Position;
import com.sos.joc.model.order.PositionChange;
import com.sos.joc.model.order.PositionChangeCode;
import com.sos.sign.model.workflow.OrderPreparation;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.Workflow;
import js7.data.workflow.position.Label;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import scala.Function1;
import scala.jdk.javaapi.OptionConverters;

public class CheckedResumeOrdersPositions extends OrdersResumePositions {
    
    @JsonIgnore
    private boolean singleOrder = false;
    
    @JsonIgnore
    private Set<JOrder> jOrders = Collections.emptySet();
    
    @JsonIgnore
    private List<HistoricOutcome> historicOutcomes = Collections.emptyList();
    
    @JsonIgnore
    private JPosition currentOrderPosition = null;
    
    @JsonIgnore
    private JPosition currentWorkflowPosition = null;
    
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
        
        ConcurrentMap<JWorkflowId, Set<JOrder>> suspendedOrFailedOrders = getResumableOrders(orders, currentState, permittedFolders);
        
        jOrders = suspendedOrFailedOrders.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        
        if (suspendedOrFailedOrders.size() > 1) {
            PositionChange pc = new PositionChange();
            pc.setCode(PositionChangeCode.NOT_ONE_WORKFLOW);
            pc.setMessage("The orders must be from the same workflow. Found workflows are: " + suspendedOrFailedOrders.keySet().toString());
            setDisabledPositionChange(pc);
            setOrderIds(orders);
            //throw new JocBadRequestException("The orders must be from the same workflow. Found workflows are: " + map.keySet().toString());
        } else {

            JWorkflowId workflowId = suspendedOrFailedOrders.keySet().iterator().next();
            Either<Problem, JWorkflow> e = currentState.repo().idToCheckedWorkflow(workflowId);
            ProblemHelper.throwProblemIfExist(e);
            JWorkflow w = e.get();
            
            JsonNode node = Globals.objectMapper.readTree(w.withPositions().toJson());
            List<Instruction> instructions = Globals.objectMapper.reader().forType(new TypeReference<List<Instruction>>() {}).readValue(node.get("instructions"));
            Set<String> implicitEnds = WorkflowsHelper.extractDisallowedImplicitEnds(instructions);

            setWorkflowId(new WorkflowId(WorkflowPaths.getPath(workflowId), workflowId.versionId().string()));
            
            Set<String> caseWhenPositions = WorkflowsHelper.getCaseWhenPositions(getInstructions(w, currentState.asScala().controllerId().string()));

            final Map<String, Integer> counterPerPos = new HashMap<>();
            final Set<Position> pos = new LinkedHashSet<>();
            final Set<String> orderIds = new HashSet<>();
            //final Set<JPosition> jPositions = new HashSet<>();
            
            jOrders.forEach(o -> {
                orderIds.add(o.id().string());
                //jPositions.add(JPosition.apply(o.asScala().position()));
                w.reachablePositions(o.workflowPosition().position()).stream().forEachOrdered(jPos -> {
                    Position p = createPosition(jPos, w.asScala(), caseWhenPositions);
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
            setWithCyclePosition(getPositions().stream().anyMatch(p -> p.getPositionString().contains("cycle")));
            setVariablesNotSettable(orderIds.size() > 1);
        }

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        
        return this;
    }
    
    @JsonIgnore
    public CheckedResumeOrdersPositions get(String order, JControllerState currentState, Set<Folder> permittedFolders, JPosition position,
            boolean withStatusCheck) throws JsonParseException, JsonMappingException, IOException, JocBadRequestException,
            JocFolderPermissionsException {

        JOrder jOrder = currentState.idToOrder().get(OrderId.of(order));
        if (jOrder == null) {
            throw new JocObjectNotExistException(String.format("Unknown OrderId: %s", order));
        }
        jOrders = Collections.singleton(jOrder);
        if (withStatusCheck && !OrdersHelper.isResumable(jOrder)) {
            throw new JocBadRequestException("The order is not resumable."); 
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
        
        OrderPreparation orderPreparation = null;
        if (node.get("orderPreparation") != null) {
            orderPreparation = Globals.objectMapper.reader().forType(new TypeReference<OrderPreparation>() {
            }).readValue(node.get("orderPreparation"));
        }
        
        Set<String> implicitEnds = WorkflowsHelper.extractDisallowedImplicitEnds(instructions);

        setWorkflowId(new WorkflowId(WorkflowPaths.getPath(workflowId), workflowId.versionId().string()));
        
        Set<String> caseWhenPositions = WorkflowsHelper.getCaseWhenPositions(getInstructions(w, currentState.asScala().controllerId().string()));

        final Set<Position> pos = new LinkedHashSet<>();
        w.reachablePositions(jOrder.workflowPosition().position()).stream().forEachOrdered(jPos -> {
            Position p = createPosition(jPos, w.asScala(), caseWhenPositions);
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
        currentOrderPosition = JPosition.apply(jOrder.asScala().position());
        currentWorkflowPosition = orderPositionToWorkflowPosition(currentOrderPosition);
        if (pos.isEmpty()) {
            Position p = createPosition(currentWorkflowPosition, w.asScala(), caseWhenPositions);
            pos.add(p);
            // TODO + ImplicitEnd of the Order's scope
            setVariablesNotSettable(true);
        } else {
//            String firstPos = pos.iterator().next().getPositionString();
//            if (position == null) {
//                setVariablesNotSettable(firstPos.equals(currentWorkflowPosition.toString()));
//            } else {
//                setVariablesNotSettable(firstPos.equals(position.toString()));
//            }
            setVariablesNotSettable(false);
        }
        setWithCyclePosition(getPositions().stream().anyMatch(p -> p.getPositionString().contains("cycle")));
        
        Variables constants = OrdersHelper.scalaValuedArgumentsToVariables(jOrder.arguments());
        if (orderPreparation != null && orderPreparation.getParameters() != null && orderPreparation.getParameters().getAdditionalProperties() != null) {
            orderPreparation.getParameters().getAdditionalProperties().forEach((k, v) -> {
                if (v.getDefault() != null && !constants.getAdditionalProperties().containsKey(k)) {
                    constants.setAdditionalProperty(k, v.getDefault());
                } else if (v.getFinal() != null && !constants.getAdditionalProperties().containsKey(k)) {
                    constants.setAdditionalProperty(k, v.getFinal());
                }
            });
        }
        setVariables(getVariables(jOrder, position, implicitEnds, constants.getAdditionalProperties().keySet()));
        
        setConstants(constants);
        
        return this;
    }
    
    @JsonIgnore
    public void disableIfNoCommonAllowedPositionsExist() {
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
    public JPosition getCurrentOrderPosition() {
        return currentOrderPosition;
    }
    
    @JsonIgnore
    public JPosition getCurrentWorkflowPosition() {
        return currentWorkflowPosition;
    }
    
    @JsonIgnore
    public List<HistoricOutcome> getHistoricOutcomes() {
        if (historicOutcomes == null) {
            return Collections.emptyList();
        }
        return historicOutcomes;
    }

    @JsonIgnore
    public Variables getVariables(JOrder jOrder, JPosition position, Set<String> implicitEnds, Set<String> orderArgs) throws JsonParseException,
            JsonMappingException, IOException, JocBadRequestException {
        if (position == null) {
            position = currentOrderPosition;
        }
        Set<String> allowedPositions = getPositions().stream().map(Position::getPositionString).collect(Collectors.toCollection(LinkedHashSet::new));
        Variables variables = new Variables();
        String positionString = position.toString();
        String positionStringWithoutCounter = orderPositionToWorkflowPosition(positionString);

        if (allowedPositions.contains(positionStringWithoutCounter) || allowedPositions.contains(positionString) || implicitEnds.contains(
                positionStringWithoutCounter) || implicitEnds.contains(positionString)) {
            OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
            historicOutcomes = oItem.getHistoricOutcomes();
            if (historicOutcomes != null) {
                
                // determine orderPosition from workflowPosition
                positionString = workflowPositionToOrderPositionFromHistoricOutcome(positionString);
                
                for (HistoricOutcome outcome : historicOutcomes) {
                    String outcomePositionString = JPosition.fromList(outcome.getPosition()).get().toString();
                    String outcomePositionStringWithoutCounter = orderPositionToWorkflowPosition(outcomePositionString);
//                    if (outcomePositionStringWithoutCounter.equals(positionStringWithoutCounter)) {
//                        break;
//                    }
                    if (outcomePositionString.equals(positionString)) {
                        break;
                    }
                    if (outcome.getOutcome() == null || outcome.getOutcome().getNamedValues() == null || outcome.getOutcome().getNamedValues()
                            .getAdditionalProperties() == null) {
                        continue;
                    }
                    if (!allowedPositions.contains(outcomePositionStringWithoutCounter)) {
                        continue;
                    }
                    Map<String, Object> vars = outcome.getOutcome().getNamedValues().getAdditionalProperties();
                    if (orderArgs != null) {
                        orderArgs.forEach(arg -> vars.remove(arg));
                    }
                    variables.setAdditionalProperties(vars);
                }
            }
        } else {
            throw new JocBadRequestException("Disallowed position '" + positionString + "'. Allowed positions are: " + allowedPositions.toString());
        }

        return variables;
    }
    
    public static ConcurrentMap<JWorkflowId, Set<JOrder>> getResumableOrders(Set<String> orders, JControllerState currentState,
            Set<Folder> permittedFolders) {
//        Function1<Order<Order.State>, Object> stateFilter = o -> o.isSuspended();
//        Iterator<Function1<Order<Order.State>, Object>> failedStates = OrdersHelper.groupByStateClasses.entrySet().stream().filter(e -> e.getValue()
//                .equals(OrderStateText.FAILED)).map(Map.Entry::getKey).map(JOrderPredicates::byOrderState).iterator();
//
//        while (failedStates.hasNext()) {
//            stateFilter = JOrderPredicates.or(stateFilter, failedStates.next());
//        }
//        stateFilter = JOrderPredicates.and(stateFilter, o -> orders.contains(o.id().string()));

        Function1<Order<Order.State>, Object> stateFilter = JOrderPredicates.and(o -> orders.contains(o.id().string()), OrdersHelper::isResumable);
        ConcurrentMap<JWorkflowId, Set<JOrder>> resumableOrders = currentState.ordersBy(stateFilter).collect(Collectors.groupingByConcurrent(
                JOrder::workflowId, Collectors.toSet()));

        if (resumableOrders.isEmpty()) {
            throw new JocBadRequestException("The orders are neither failed nor suspended");
        }

        Set<JWorkflowId> notPermittedWorkflows = resumableOrders.keySet().stream().filter(wId -> !OrdersHelper.canAdd(WorkflowPaths.getPath(
                wId), permittedFolders)).collect(Collectors.toSet());
        for (JWorkflowId notPermittedWorkflow : notPermittedWorkflows) {
            resumableOrders.remove(notPermittedWorkflow);
        }

        if (resumableOrders.isEmpty()) {
            throw new JocFolderPermissionsException("Access denied");
        }

        return resumableOrders;
    }
    
    public Map<JOrder, Optional<JPosition>> filterOrdersbyLabelOrPosition(Set<String> orders, Object positionOrLabel, boolean force,
            JControllerState currentState, Set<Folder> permittedFolders) {
        if (positionOrLabel != null) {
            if (positionOrLabel instanceof String) {
                return filterOrdersbyLabel(orders, (String) positionOrLabel, force, currentState, permittedFolders);
            } else if (positionOrLabel instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> position = (List<Object>) positionOrLabel;
                if (!position.isEmpty()) {
                    return filterOrdersbyPosition(orders, position, force, currentState, permittedFolders);
                }
            }
        }
        return Collections.emptyMap();
    }
    
    private Map<JOrder, Optional<JPosition>> filterOrdersbyLabel(Set<String> orders, String label, boolean force, JControllerState currentState,
            Set<Folder> permittedFolders) {
        return filterOrdersbyLabel(getResumableOrders(orders, currentState, permittedFolders), label, force, currentState);
    }
    
    private Map<JOrder, Optional<JPosition>> filterOrdersbyPosition(Set<String> orders, List<Object> position, boolean force, JControllerState currentState,
            Set<Folder> permittedFolders) {
        return filterOrdersbyPosition(getResumableOrders(orders, currentState, permittedFolders), position, force, currentState);
    }
    
    private Map<JOrder, Optional<JPosition>> filterOrdersbyLabel(ConcurrentMap<JWorkflowId, Set<JOrder>> resumableOrders, String label,
            boolean force, JControllerState currentState) {
        Label l = Label.fromString(label);

        jOrders = resumableOrders.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<JOrder, Optional<JPosition>> allowedOrders = new HashMap<>();
        for (Map.Entry<JWorkflowId, Set<JOrder>> entry : resumableOrders.entrySet()) {
            Optional<JWorkflow> jWorkflowOpt = currentState.repo().idToCheckedWorkflow(entry.getKey()).toJavaOptional();
            if (jWorkflowOpt.isPresent()) {
                Optional<JPosition> pos = labelToPosition(jWorkflowOpt.get(), l, currentState);
                if (pos.isPresent()) {
                    entry.getValue().stream().filter(o -> jWorkflowOpt.get().reachablePositions(o.workflowPosition().position()).stream().anyMatch(
                            p -> p.equals(pos.get()))).forEach(o -> allowedOrders.put(o, forceOrderPosition(workflowPositionToOrderPosition(pos, o,
                                    null), o, force)));

                }
            }
        }
        return allowedOrders;
    }
    
    private static Optional<JPosition> labelToPosition(JWorkflow workflow, final Label label, JControllerState currentState) {
        return OptionConverters.toJava(workflow.asScala().labelToPosition(label).toOption()).map(JPosition::apply);
    }
    
    private Map<JOrder, Optional<JPosition>> filterOrdersbyPosition(ConcurrentMap<JWorkflowId, Set<JOrder>> resumableOrders, List<Object> position,
            boolean force, JControllerState currentState) {
        Optional<JPosition> pos = position == null || position.isEmpty() ? Optional.empty() : JPosition.fromList(position).toJavaOptional();

        jOrders = resumableOrders.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<JOrder, Optional<JPosition>> allowedOrders = new HashMap<>();
        for (Map.Entry<JWorkflowId, Set<JOrder>> entry : resumableOrders.entrySet()) {
            Optional<JWorkflow> jWorkflowOpt = currentState.repo().idToCheckedWorkflow(entry.getKey()).toJavaOptional();
            if (jWorkflowOpt.isPresent()) {
                if (pos.isPresent()) {
                    entry.getValue().stream().filter(o -> jWorkflowOpt.get().reachablePositions(o.workflowPosition().position()).stream().anyMatch(
                            p -> p.equals(pos.get()))).forEach(o -> allowedOrders.put(o, forceOrderPosition(workflowPositionToOrderPosition(pos, o,
                                    null), o, force)));

                }
            }
        }
        return allowedOrders;
    }
    
    public String orderPositionToWorkflowPosition(String pos) {
        return pos.replaceAll("/(try|catch|cycle)\\+?[^:]*", "/$1");
    }
    
    private JPosition orderPositionToWorkflowPosition(JPosition pos) {
        return JPosition.fromList(pos.toList().stream().map(o -> {
            if (o instanceof String) {
                return ((String) o).replaceAll("(try|catch|cycle)\\+?.*", "$1");
            } else {
                return o;
            }
        }).collect(Collectors.toList())).get();
    }
    
    public Optional<JPosition> forceOrderPosition(final Optional<JPosition> orderPosition, boolean force) {
        return forceOrderPosition(orderPosition, null, force);
    }
    
    public Optional<JPosition> forceOrderPosition(final Optional<JPosition> orderPosition, JOrder jOrder, boolean force) {
        JPosition curOrderPosition = null;
        if (jOrder == null) {
            curOrderPosition = getCurrentOrderPosition();
        } else {
            curOrderPosition = JPosition.apply(jOrder.asScala().position());
        }
        if (orderPosition.isPresent()) {
            if (curOrderPosition.equals(orderPosition.get()) && !force) {
                return Optional.empty();
            }
        } else if (force) {
            return Optional.of(curOrderPosition);
        }
        return orderPosition;
    }
    
    public Optional<JPosition> workflowPositionToOrderPosition(final Optional<JPosition> workflowPosition, Long cycleEndTime) {
        if (cycleEndTime != null && workflowPosition.isEmpty()) { //consider cycleEndTime if workflowPosition is empty
            return Optional.of(workflowPositionToOrderPosition(orderPositionToWorkflowPosition(getCurrentOrderPosition()), null, cycleEndTime)).map(
                    JPosition::fromList).map(Either::get);
        }
        return workflowPosition.map(l -> workflowPositionToOrderPosition(l, null, cycleEndTime)).map(JPosition::fromList).map(Either::get);
    }

    public Optional<JPosition> workflowPositionToOrderPosition(final Optional<JPosition> workflowPosition, JOrder jOrder, Long cycleEndTime) {
        if (cycleEndTime != null && workflowPosition.isEmpty()) { // consider cycleEndTime if workflowPosition is empty
            return Optional.of(workflowPositionToOrderPosition(orderPositionToWorkflowPosition(JPosition.apply(jOrder.asScala().position())), jOrder,
                    cycleEndTime)).map(JPosition::fromList).map(Either::get);
        }
        return workflowPosition.map(l -> workflowPositionToOrderPosition(l, jOrder, cycleEndTime)).map(JPosition::fromList).map(Either::get);
    }

    private List<Object> workflowPositionToOrderPosition(final JPosition workflowJPosition, JOrder jOrder, Long cycleEndTime) {
        List<Object> curOrderPosition = null;
        if (jOrder == null) {
            curOrderPosition = getCurrentOrderPosition().toList();
        } else {
            curOrderPosition = JPosition.apply(jOrder.asScala().position()).toList();
        }
        List<Object> result = new LinkedList<>();
        int numOfCurPos = curOrderPosition == null ? 0 : curOrderPosition.size();
        int lastInstructionInPos = workflowJPosition.toList().size() - 2;
        int index = 0;
        
        // 0 <= cycleEndTime <= 24h
        Optional<Long> cEndTime = cycleEndTime != null ? Optional.of(Instant.now().plusSeconds(Math.min(TimeUnit.DAYS.toSeconds(1), Math.max(0,
                cycleEndTime)))).map(Instant::toEpochMilli) : Optional.empty();

        Integer indexOfImplicitEndCyclePosition = getPositions().stream().filter(p -> workflowJPosition.toString().equals(p.getPositionString()))
                .filter(p -> "ImplicitEnd".equals(p.getType())).map(Position::getPosition).findAny().map(l -> (l.lastIndexOf("cycle") == l.size() - 2)
                        ? l.lastIndexOf("cycle") : -1).orElse(-1);

        for (Object pos : workflowJPosition.toList()) {
            boolean posIsAdded = false;
            if (index < numOfCurPos && pos instanceof String) {
                String posStr = (String) pos;
                boolean isWorkflowPosition = posStr.equals(posStr.replaceAll("(try|catch|cycle)\\+?.*", "$1"));
                if (isWorkflowPosition) {
                    if (posStr.equals("cycle") || posStr.equals("try") || posStr.equals("catch")) {
                        String curOrderPos = (String) curOrderPosition.get(index);
                        if (curOrderPos != null) {
                            if (posStr.equals(curOrderPos.replaceAll("(try|catch|cycle)\\+?.*", "$1"))) {
                                if (posStr.equals("cycle")) {
                                    if (indexOfImplicitEndCyclePosition == index) {
                                        curOrderPos = posStr;
                                    } else if (cEndTime.isPresent()) {
                                        curOrderPos = curOrderPos.replaceAll("end=\\d+", "end=" + cEndTime.get());
                                    }
                                }
                                result.add(curOrderPos);
                                posIsAdded = true;
                            } else if (index == lastInstructionInPos && ((posStr + curOrderPos).startsWith("trycatch") || (posStr + curOrderPos)
                                    .startsWith("catchtry"))) {
                                //new position from try to catch or from catch to try in the same instruction
                                String stack = curOrderPos.replaceAll("(?:try|catch)(\\+?.*)", "$1");
                                result.add(posStr + stack);
                                posIsAdded = true;
                            }
                        }
                    }
                }
            }
            if (!posIsAdded) {
                result.add(pos); 
            }
            index++;
        }
        return result;
    }
    
    private String workflowPositionToOrderPositionFromHistoricOutcome(String positionString) {
        // determine orderPosition from workflowPosition
        String workflowPositionString = orderPositionToWorkflowPosition(positionString);
        if (positionString.equals(workflowPositionString)) { // position is workflowPosition
            if (positionString.contains("cycle") || positionString.contains("try") || positionString.contains("catch")) {
                String orderPosition = getHistoricOutcomes().stream().map(HistoricOutcome::getPosition).map(JPosition::fromList).map(Either::get).map(
                        JPosition::toString).filter(s -> orderPositionToWorkflowPosition(s).equals(workflowPositionString)).collect(
                                Collectors.toCollection(LinkedList::new)).peekLast();
                if (orderPosition != null) {
                    return orderPosition;  
                }
            }
        }
        return positionString;
    }
    
    private JPosition getJPositionFromString(String positionString) {
        return JPosition.fromList(Arrays.asList(positionString.split("[:/]")).stream().map(str -> str.matches("\\d+") ? Integer.valueOf(str) : str)
                .collect(Collectors.toCollection(LinkedList::new))).get();
    }
    
    private static Position createPosition(JPosition jPos, Workflow w, Set<String> caseWhenPositions) {
        Position p = new Position();
        p.setPosition(jPos.toList());
        p.setPositionString(jPos.toString());
        // If could be CaseWhen but Controller doesn't know a CaseWhen instruction
        if (caseWhenPositions != null && caseWhenPositions.contains(jPos.toString())) {
            p.setType(InstructionType.CASE_WHEN.value());
        } else {
            p.setType(w.instruction(jPos.asScala()).instructionName().replace("Execute.Named", "Job"));
        }
        //if ("Job".equals(p.getType())) { //not longer only JObs have labels
        try {
            p.setLabel(w.labeledInstruction(jPos.asScala()).toOption().map(l -> l.labelString().trim().replaceFirst(":$", "")).filter(s -> !s
                    .isBlank()).getOrElse(null));
        } catch (Throwable e) {
            //
        }
        //}
        return p;
    }
    
    public static Map<OrderId, Optional<JPosition>> moveToBeginOfBlock(final Set<JOrder> jOrders, final boolean force) {
        return jOrders.stream().collect(Collectors.toMap(JOrder::id, o -> CheckedResumeOrdersPositions.moveToBeginOfBlock(o, force)));
    }
    
    public static Optional<JPosition> moveToBeginOfBlock(JOrder jOrder, boolean force) {
        return moveToBeginOfBlock(JPosition.apply(jOrder.asScala().position()), force);
    }
    
    public static Optional<JPosition> moveToBeginOfBlock(JPosition curPos, boolean force) {
        /*
         * position on top level will be unchanged
         * [n] -> [n]
         * [n, "(fork|forklist)+branchId", m] -> [n, "(fork|forklist)+branchId", m]
         * otherwise
         * [n, "notForkInstruction", m] -> [n]
         */
        boolean unchanged = false;
        List<Object> curPosition = new ArrayList<>(curPos.toList());
        if (curPosition.size() == 1) {
            unchanged = true;
        } else if (curPosition.size() > 2) {
            String instructionBlock = (String) curPosition.get(curPosition.size() - 2);
            if (instructionBlock.startsWith("fork")) {
                unchanged = true;
            } else {
                // delete last two items
                curPosition.remove(curPosition.size() - 1);
                curPosition.remove(curPosition.size() - 1);
            }
        }
        if (unchanged && !force) {
            return Optional.empty();
        }
        return Optional.of(JPosition.fromList(curPosition).get());
    }
    
    private static List<Instruction> getInstructions(JWorkflow workflow, String controllerId) throws IOException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./orders/resume/positions");
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(controllerId);
            dbFilter.setWorkflowIds(Collections.singleton(new WorkflowId(workflow.id().path().string(), workflow.id().versionId().string())));
            dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
            List<DeployedContent> dbWorkflows = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
            if (dbWorkflows != null && !dbWorkflows.isEmpty() && dbWorkflows.get(0).getContent() != null) {
                return JocInventory.workflowContent2Workflow(dbWorkflows.get(0).getContent()).getInstructions();
            } else {
                throw new DBMissingDataException("Couldn't find workflow '" + workflow.id().path().string() + "' as deployed object in database");
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

}
