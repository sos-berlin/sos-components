package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.controller.model.board.PlannedBoard;
import com.sos.inventory.model.board.BoardType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.db.deploy.items.WorkflowBoards;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderIdToOrder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.plan.PlansFilter;
import com.sos.joc.model.workflow.WorkflowsBoardsV;
import com.sos.joc.plan.common.PlanHelper;
import com.sos.joc.plan.common.PlannedBoards;
import com.sos.joc.workflows.resource.IWorkflowsBoardsSnapshotResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.plan.PlanId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JRepo;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.plan.JPlan;
import scala.Function1;

@Path("workflows")
public class WorkflowsBoardsSnapshotImpl extends JOCResourceImpl implements IWorkflowsBoardsSnapshotResource {

    private static final String API_CALL = "./workflows/boards/snapshot";
    // private static final Logger LOGGER = LoggerFactory.getLogger(PlansResourceImpl2.class);
    private static final ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
    private JControllerState currentState = null;

    @Override
    public JOCDefaultResponse postWorkflowBoards(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, PlansFilter.class);
            PlansFilter filter = Globals.objectMapper.readValue(filterBytes, PlansFilter.class);
            String controllerId = filter.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getView());
            if (response != null) {
                return response;
            }

            currentState = Proxy.of(controllerId).currentState();
            Set<OrderId> orderIds = new HashSet<>();
            Set<String> expectingOrderIds = new HashSet<>();
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            WorkflowsBoardsV entity = new WorkflowsBoardsV();
            entity.setSurveyDate(Date.from(currentState.instant()));

            com.sos.controller.model.board.PlannedBoards pbs = new com.sos.controller.model.board.PlannedBoards();
            pbs.setAdditionalProperties(get(filter, currentState).flatMap(jp -> {
                orderIds.addAll(jp.orderIds());
                return jp.toPlannedBoard().values().stream().map(b -> {
                    js7.data.board.PlannedBoard pb = b.asScala();
                    PlannedBoards plB = new PlannedBoards(Collections.singletonMap(pb.boardPath(), b), true, filter.getCompact(), null);
                    PlannedBoard bd = plB.getPlannedBoard(new PlannedBoard());
                    bd.setTYPE(null);
                    bd.setVersion(null);
                    bd.setBoardType((PlanId.Global.equals(pb.planId())) ? BoardType.GLOBAL : BoardType.PLANNABLE);
                    bd.setName(pb.boardPath().string()); // only name
                    bd.setPlanId(new com.sos.joc.model.plan.PlanId(pb.planId().planKey().string(), pb.planId().planSchemaId().string()));
                    if (bd.getNotices() != null) {
                        expectingOrderIds.addAll(bd.getNotices().stream().map(n -> n.getExpectingOrderIds()).filter(Objects::nonNull).flatMap(
                                Set::stream).collect(Collectors.toSet()));
                    }
                    return bd;
                });
            }).collect(Collectors.toMap(PlannedBoard::getName, Function.identity(), (b1, b2) -> b1)));
            //.collect(Collectors.groupingBy(PlannedBoard::getName)));
            
            entity.setNoticeBoards(pbs);
            
            Map<String, WorkflowBoards> wbsMap = WorkflowRefs.getWorkflowNamesWithBoards(controllerId);
            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                wbsMap = wbsMap.entrySet().stream().filter(e -> canAdd(e.getValue().getPath(), permittedFolders)).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue));
            }
            // only with clone: wbsMap.keySet().removeIf(wn -> !canAdd(WorkflowPaths.getPath(wn), permittedFolders));
            
            Map<WorkflowPath, List<JOrder>> criticalOrdersPerWorkflow = getCriticalOrdersPerWorkflow(orderIds, wbsMap.keySet());
            Map<WorkflowPath, Set<String>> expectedlOrderIdsPerWorkflow = getExpectingOrdersPerWorkflow(orderIds, wbsMap.keySet());
            
            JRepo jRepo = currentState.repo();
            Predicate<WorkflowBoards> onlySynchronized = w -> jRepo == null ? true : jRepo.pathToCheckedWorkflow(WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).isRight();
            
            List<com.sos.controller.model.workflow.WorkflowBoardsV> postingWorkflows = new ArrayList<>();
            List<com.sos.controller.model.workflow.WorkflowBoardsV> expectingWorkflows = new ArrayList<>();
            List<com.sos.controller.model.workflow.WorkflowBoardsV> consumingWorkflows = new ArrayList<>();
            OrderIdToOrder idToOrder = new OrderIdToOrder();
            
            wbsMap.values().stream().filter(w -> canAdd(w.getPath(), permittedFolders)).filter(onlySynchronized).forEach(w -> {

                AtomicInteger numOfAnnouncements = new AtomicInteger(0);
                AtomicInteger numOfExpectedNotices = new AtomicInteger(0);
                AtomicInteger numOfPostedNotices = new AtomicInteger(0);
                //AtomicInteger numOfExpectingOrders = new AtomicInteger(0);
                if (w.getNoticeBoardNames() != null) {
                    w.getNoticeBoardNames().stream().map(bName -> pbs.getAdditionalProperties().get(bName)).filter(Objects::nonNull).forEach(
                            pBoard -> {
                                if (pBoard.getNumOfAnnouncements() != null) {
                                    numOfAnnouncements.addAndGet(pBoard.getNumOfAnnouncements());
                                }
                                if (pBoard.getNumOfExpectedNotices() != null) {
                                    numOfExpectedNotices.addAndGet(pBoard.getNumOfExpectedNotices());
                                }
                                if (pBoard.getNumOfPostedNotices() != null) {
                                    numOfPostedNotices.addAndGet(pBoard.getNumOfPostedNotices());
                                }
//                                if (pBoard.getNumOfExpectingOrders() != null) {
//                                    numOfExpectingOrders.addAndGet(pBoard.getNumOfExpectingOrders());
//                                }
                            });
                    w.setNoticeBoardNames(null);
                }
                w.setNumOfAnnouncements(numOfAnnouncements.get());
                w.setNumOfExpectedNotices(numOfExpectedNotices.get());
                w.setNumOfPostedNotices(numOfPostedNotices.get());
                w.setExpectingOrderIds(expectedlOrderIdsPerWorkflow.getOrDefault(WorkflowPath.of(JocInventory.pathToName(w.getPath())), Collections
                        .emptySet()));
                w.setNumOfExpectingOrders(w.getExpectingOrderIds().size());

                if (w.hasConsumeNotice() > 0) {
                    consumingWorkflows.add(w);
                }
                if (w.hasExpectNotice() > 0) {
                    expectingWorkflows.add(w);
                }
                if (w.hasPostNotice() > 0) {
                    postingWorkflows.add(w);
                }

                Map<String, OrderV> jOrdersPerWorkflowDuePresent = getDuePresentOrdersPerWorkflowWithPositionBeforeBoard(criticalOrdersPerWorkflow,
                        w);
                w.setPresentDueOrderIds(jOrdersPerWorkflowDuePresent.keySet());

                Map<String, OrderV> jOrdersPerWorkflowFuturePresent = getDueFutureOrdersPerWorkflowWithPositionBeforeBoard(criticalOrdersPerWorkflow,
                        w);
                w.setFutureDueOrderIds(jOrdersPerWorkflowFuturePresent.keySet());

                idToOrder.getAdditionalProperties().putAll(jOrdersPerWorkflowDuePresent);
                idToOrder.getAdditionalProperties().putAll(jOrdersPerWorkflowFuturePresent);
            });
            
            idToOrder.getAdditionalProperties().putAll(getExpectingOrders(OrdersHelper.getPermittedJOrdersFromOrderIds(expectingOrderIds.stream().map(
                    OrderId::of), permittedFolders, currentState)));

            entity.setPostingWorkflows(postingWorkflows);
            entity.setExpectingWorkflows(expectingWorkflows);
            entity.setConsumingWorkflows(consumingWorkflows);
            entity.setOrders(idToOrder);

            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private static boolean orderPosIsBeforeBoardPos(JOrder jOrder, String boardPos) {
        return orderPosIsBeforeBoardPos(new ArrayList<>(jOrder.workflowPosition().position().toList()), OrdersHelper.stringPositionToList(boardPos));
    }

    private static boolean orderPosIsBeforeBoardPos(List<Object> orderPos, List<Object> boardPos) {
        int firstOrderPos = (int) orderPos.remove(0);
        int firstBoardPos = (int) boardPos.remove(0);
        if (firstOrderPos < firstBoardPos) {
            return true;
        }
        if (firstOrderPos == firstBoardPos && !orderPos.isEmpty() && !boardPos.isEmpty()) {
            String secondOrderPos = (String) orderPos.remove(0);
            String secondBoardPos = (String) boardPos.remove(0);
            if (secondOrderPos.equals(secondBoardPos)) {
                return orderPosIsBeforeBoardPos(orderPos, boardPos);
            }
        }
        return false;
    }

    private Map<WorkflowPath, List<JOrder>> getCriticalOrdersPerWorkflow(Set<OrderId> orderIds, Set<String> workflowNames) {
        // maybe forked orders too?
        Function1<Order<Order.State>, Object> suspendFilter = o -> o.isSuspended();
        Function1<Order<Order.State>, Object> promptingFilter = JOrderPredicates.byOrderState(Order.Prompting.class);
        Iterator<Function1<Order<Order.State>, Object>> failedFilters = OrdersHelper.groupByStateClasses.entrySet().stream().filter(e -> e.getValue()
                .equals(OrderStateText.FAILED)).map(Map.Entry::getKey).map(JOrderPredicates::byOrderState).iterator();
        Function1<Order<Order.State>, Object> stateFilter = JOrderPredicates.or(suspendFilter, promptingFilter);
        while (failedFilters.hasNext()) {
            stateFilter = JOrderPredicates.or(stateFilter, failedFilters.next());
        }
        stateFilter = JOrderPredicates.and(stateFilter, JOrderPredicates.byOrderIdPredicate(oId -> orderIds.contains(oId)));

        return currentState.ordersBy(stateFilter).filter(o -> workflowNames.contains(o.workflowId().path().string())).collect(Collectors.groupingBy(
                o -> o.workflowId().path()));
    }
    
    private Map<WorkflowPath, Set<String>> getExpectingOrdersPerWorkflow(Set<OrderId> orderIds, Set<String> workflowNames) {
        Function1<Order<Order.State>, Object> stateFilter = JOrderPredicates.byOrderState(Order.ExpectingNotices.class);
        stateFilter = JOrderPredicates.and(stateFilter, JOrderPredicates.byOrderIdPredicate(oId -> orderIds.contains(oId)));
        return currentState.ordersBy(stateFilter).filter(o -> workflowNames.contains(o.workflowId().path().string())).collect(Collectors.groupingBy(
                o -> o.workflowId().path(), Collectors.mapping(o -> o.id().string(), Collectors.toSet())));
    }

    private static Stream<JPlan> get(PlansFilter filter, JControllerState currentState) throws Exception {
        return currentState.toPlan().entrySet().stream().map(e -> PlanHelper.getFilteredJPlan(e.getKey(), e.getValue(), filter)).filter(
                Objects::nonNull);
    }
    
    private Map<String, OrderV> getDuePresentOrdersPerWorkflowWithPositionBeforeBoard(Map<WorkflowPath, List<JOrder>> criticalOrdersPerWorkflow,
            WorkflowBoards w) {
        List<JOrder> criticalOrders = criticalOrdersPerWorkflow.get(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
        return getDuePresentOrdersPerWorkflowWithPositionBeforeBoard(criticalOrders, w);
    }
    
    private Map<String, OrderV> getDuePresentOrdersPerWorkflowWithPositionBeforeBoard(List<JOrder> criticalOrders, WorkflowBoards w) {
        if (criticalOrders != null && !criticalOrders.isEmpty()) {
            Set<String> positions = new HashSet<>();
            if (w.getExpectPositions() != null) {
                positions.addAll(w.getExpectPositions().keySet());
            }
            if (w.getConsumePositions() != null) {
                positions.addAll(w.getConsumePositions().keySet());
            }
            final Long surveyDateMillis = currentState.instant().toEpochMilli();

            Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                try {
                    return OrdersHelper.mapJOrderToOrderV(o, currentState, true, null, null, surveyDateMillis, zoneId);
                } catch (Exception e) {
                    return null;
                }
            };

            return positions.stream().flatMap(boardPos -> criticalOrders.stream().filter(o -> orderPosIsBeforeBoardPos(o, boardPos))).distinct().map(
                    mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toMap(OrderV::getOrderId, Function.identity(), (o1, o2) -> o1));
        }
        return Collections.emptyMap();
    }
    
    private Map<String, OrderV> getDueFutureOrdersPerWorkflowWithPositionBeforeBoard(Map<WorkflowPath, List<JOrder>> criticalOrdersPerWorkflow,
            WorkflowBoards w) {
        List<JOrder> criticalOrders = criticalOrdersPerWorkflow.get(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
        return getDueFutureOrdersPerWorkflowWithPositionBeforeBoard(criticalOrders, w);
    }
    
    private Map<String, OrderV> getDueFutureOrdersPerWorkflowWithPositionBeforeBoard(List<JOrder> criticalOrders, WorkflowBoards w) {
        if (criticalOrders != null && !criticalOrders.isEmpty()) {
            Set<String> positions = new HashSet<>();
            if (w.getPostPositions() != null) {
                positions.addAll(w.getPostPositions().keySet());
            }
            final Long surveyDateMillis = currentState.instant().toEpochMilli();

            Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                try {
                    return OrdersHelper.mapJOrderToOrderV(o, currentState, true, null, null, surveyDateMillis, zoneId);
                } catch (Exception e) {
                    return null;
                }
            };

            return positions.stream().flatMap(boardPos -> criticalOrders.stream().filter(o -> orderPosIsBeforeBoardPos(o, boardPos))).distinct().map(
                    mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toMap(OrderV::getOrderId, Function.identity(), (o1, o2) -> o1));
        }
        return Collections.emptyMap();
    }
    
    private Map<String, OrderV> getExpectingOrders(Stream<JOrder> expectingOrders) {
        final Long surveyDateMillis = currentState.instant().toEpochMilli();

        Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
            try {
                return OrdersHelper.mapJOrderToOrderV(o, currentState, true, null, null, surveyDateMillis, zoneId);
            } catch (Exception e) {
                return null;
            }
        };

        return expectingOrders.map(mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toMap(OrderV::getOrderId, Function.identity(), (o1,
                o2) -> o1));
    }

}