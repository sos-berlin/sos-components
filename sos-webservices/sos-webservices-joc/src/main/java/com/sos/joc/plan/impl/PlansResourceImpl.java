package com.sos.joc.plan.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.board.Board;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.BoardsFilter;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.plan.Plan;
import com.sos.joc.model.plan.Plans;
import com.sos.joc.model.plan.PlansFilter;
import com.sos.joc.model.plan.PlansOpenCloseFilter;
import com.sos.joc.plan.common.PlanHelper;
import com.sos.joc.plan.common.PlannedBoards;
import com.sos.joc.plan.resource.IPlansResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.order.OrderId;
import js7.data.plan.PlanId;
import js7.data_for_java.board.JNoticePlace;
import js7.data_for_java.board.JPlannedBoard;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.plan.JPlan;

@Path("plans")
public class PlansResourceImpl extends JOCResourceImpl implements IPlansResource {

    private static final String API_CALL = "./plans";
    private static final String API_CALL_IDS = "./plans/ids";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlansResourceImpl.class);
    private static final ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
//    private final static int limitOrdersDefault = 10000;
    private SOSHibernateSession session = null;
    private JControllerState currentState = null;

    @Override
    public JOCDefaultResponse postPlans(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, PlansFilter.class);
            PlansFilter filter = Globals.objectMapper.readValue(filterBytes, PlansFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getControllerPermissions(filter.getControllerId(), accessToken)
                    .getNoticeBoards().getView());
            if (response != null) {
                return response;
            }
            currentState = Proxy.of(filter.getControllerId()).currentState();
            Plans entity = new Plans();
            entity.setSurveyDate(Date.from(currentState.instant()));
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            entity.setPlans(get(filter));
            
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    @Override
    public JOCDefaultResponse postPlanIds(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_IDS, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, PlansOpenCloseFilter.class);
            PlansFilter filter = Globals.objectMapper.readValue(filterBytes, PlansFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getControllerPermissions(filter.getControllerId(), accessToken)
                    .getNoticeBoards().getView());
            if (response != null) {
                return response;
            }
            currentState = Proxy.of(filter.getControllerId()).currentState();
            Plans entity = new Plans();
            entity.setSurveyDate(Date.from(currentState.instant()));
            session = Globals.createSosHibernateStatelessConnection(API_CALL_IDS);

            entity.setPlans(currentState.toPlan().entrySet().stream().map(e -> getPlanIds(e.getKey(), e.getValue(), filter)).filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private com.sos.joc.model.plan.Plan getPlanIds(PlanId pId, JPlan jp, PlansFilter filter) {
        filter.setNoticeSpaceKeys(null);
        filter.setPlanSchemaIds(null);
        return PlanHelper.getFilteredPlan(pId, jp, filter);
    }
    
    private com.sos.joc.model.plan.Plan getPlan(PlanId pId, JPlan jp, PlansFilter filter) {
        Plan plan = PlanHelper.getFilteredPlan(pId, jp, filter);
        if (plan == null) {
            return null;
        }
        
//        Stream<JOrder> jOrders = OrdersHelper.getPermittedJOrdersFromOrderIds(jp.orderIds(), folderPermissions.getListOfFolders(), currentState);
        Map<OrderId, OrderV> orders = Collections.emptyMap();
        
//        Map<JWorkflowId, Set<JOrder>> workflowToOrder = jOrders.collect(Collectors.groupingBy(JOrder::workflowId, Collectors.toSet()));
        
        // TODO check what workflow has (plannable?) boards -> filter workflowToOrder
        // TODO check position of expecting/consuming notice instruction on top level after order position
        
//        jOrders = workflowToOrder.values().stream().flatMap(Set::stream);
//        boolean compact = Boolean.TRUE == filter.getCompact();
//        
//        if (compact) {
//            plan.setOrders(null);
//            plan.setNumOfOrders(jOrders.mapToInt(o -> 1).sum());
//        } else {
//            Map<String, Set<String>> orderTags = getOrderTags(filter.getControllerId(), jp); // TODO 
//            Long surveyDateMillis = currentState.instant().toEpochMilli();
//            AtomicInteger counter = new AtomicInteger(0);
//
//            Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
//                try {
//                    counter.incrementAndGet();
//                    return OrdersHelper.mapJOrderToOrderV(o, currentState, true, orderTags, null, surveyDateMillis, zoneId);
//                } catch (Exception e) {
//                    return null;
//                }
//            };
//
//            Stream<OrderV> ordersStream = jOrders.map(mapJOrderToOrderV).filter(Objects::nonNull);
//            
//            Integer limitOrders = filter.getLimit() == null ? limitOrdersDefault : filter.getLimit();
//            if (limitOrders > -1 && jp.orderIds().size() > limitOrders) {
//                ordersStream = ordersStream.sorted(Comparator.comparingLong(OrderV::getScheduledFor).reversed()).limit(limitOrders.longValue()); 
//            }
//            orders = ordersStream.collect(Collectors.toMap(o -> OrderId.of(o.getOrderId()), Function.identity()));
//
//            plan.setOrders(orders.values());
//            plan.setNumOfOrders(counter.get());
//        }
        
        // Orders are too much for this API (decrease performance significantly). It should be sent by another API, e.g. ./plans/orders
        plan.setOrders(null);
        plan.setNumOfOrders(null);

        plan.setNoticeBoards(getBoards(jp.toPlannedBoard(), filter, orders));
        
        return plan;
    }
    
//    private Map<String, Set<String>> getOrderTags(String controllerId, JPlan jp) {
//        Map<String, Set<String>> orderTags = null;
//        try {
//            orderTags = OrderTags.getTagsByOrderIds(controllerId, jp.orderIds().stream().map(OrderId::string), session);
//        } catch (SOSHibernateException e) {
//            if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
//                LOGGER.info(getJocError().printMetaInfo());
//                getJocError().clearMetaInfo();
//            }
//            LOGGER.error("", e);
//        }
//        return orderTags;
//    }
    
    private List<Board> getBoards(Map<BoardPath, JPlannedBoard> jBoards, PlansFilter filter, Map<OrderId, OrderV> orders) {
        try {
            
            Stream<String> availableBoardNames = jBoards.keySet().stream().map(BoardPath::string);
            if (filter.getNoticeBoardPaths() != null && !filter.getNoticeBoardPaths().isEmpty()) {
                Set<String> requestedBoardNames = filter.getNoticeBoardPaths().stream().map(JocInventory::pathToName).collect(Collectors.toSet());
                availableBoardNames = availableBoardNames.filter(bp -> requestedBoardNames.contains(bp));
            }
            List<String> boardNames = availableBoardNames.collect(Collectors.toList()); 
            if (boardNames.isEmpty()) {
                return null;
            }
            
            BoardsFilter bFilter = new BoardsFilter();
            bFilter.setControllerId(filter.getControllerId());
            bFilter.setNoticeBoardPaths(boardNames);
            bFilter.setLimit(filter.getLimit());
            
            return getBoards(bFilter, jBoards, orders, Boolean.TRUE == filter.getCompact());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<Plan> get(PlansFilter filter) throws Exception {
        return currentState.toPlan().entrySet().stream().map(e -> getPlan(e.getKey(), e.getValue(), filter)).filter(Objects::nonNull).collect(
                Collectors.toList());
    }
    
    private List<Board> getBoards(BoardsFilter filter, Map<BoardPath, JPlannedBoard> jBoards, Map<OrderId, OrderV> orders, boolean compact)
            throws Exception {
        try {
            String controllerId = filter.getControllerId();
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(controllerId);
            dbFilter.setObjectTypes(Collections.singleton(DeployType.NOTICEBOARD.intValue()));

            List<String> paths = filter.getNoticeBoardPaths();
            if (paths != null && !paths.isEmpty()) {
                filter.setFolders(null);
            }
            boolean withFolderFilter = filter.getFolders() != null && !filter.getFolders().isEmpty();
            final Set<Folder> folders = addPermittedFolder(filter.getFolders());

            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            List<DeployedContent> contents = null;
            if (paths != null && !paths.isEmpty()) {
                dbFilter.setNames(paths.stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
                contents = dbLayer.getDeployedInventory(dbFilter);

            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                // no folder permissions
            } else if (folders != null && !folders.isEmpty()) {
                dbFilter.setFolders(folders);
                contents = dbLayer.getDeployedInventory(dbFilter);
            } else {
                contents = dbLayer.getDeployedInventory(dbFilter);
            }
            
            final Set<Folder> permittedFolders = withFolderFilter ? null : folders;
            
            JocError jocError = getJocError();
            if (contents != null) {
                
                if ((orders == null || orders.isEmpty()) && !compact) {
                    orders = Collections.emptyMap();
                    if (filter.getCompact() != Boolean.TRUE) {
                        
                        Set<OrderId> eos = jBoards.values().stream().map(JPlannedBoard::toNoticePlace).map(Map::values)
                                .flatMap(Collection::stream).map(JNoticePlace::expectingOrderIds).flatMap(Collection::stream).collect(Collectors.toSet());

                        Map<String, Set<String>> orderTags = OrderTags.getTagsByOrderIds(controllerId, eos.stream().map(OrderId::string), session);
                        final Long surveyDateMillis = currentState.instant().toEpochMilli();
                                
                        Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                            try {
                                return OrdersHelper.mapJOrderToOrderV(o, currentState, true, orderTags, null, surveyDateMillis, zoneId);
                            } catch (Exception e) {
                                return null;
                            }
                        };

                        orders = OrdersHelper.getPermittedJOrdersFromOrderIds(eos, permittedFolders, currentState).map(mapJOrderToOrderV).filter(
                                Objects::nonNull).collect(Collectors.toMap(o -> OrderId.of(o.getOrderId()), Function.identity()));
                    }
                }
                
                
                PlannedBoards plB = new PlannedBoards(jBoards, orders, compact, filter.getLimit());
                
                return contents.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)).map(dc -> {
                    try {
                        if (dc.getContent() == null || dc.getContent().isEmpty()) {
                            throw new DBMissingDataException("doesn't exist");
                        }
                        return plB.getPlannedBoardDeps(dc);
                    } catch (Throwable e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] %s", dc.getPath(), e.toString()));
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
            
            return Collections.emptyList();
        } catch (Throwable e) {
            throw e;
        }

    }

}