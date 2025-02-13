package com.sos.joc.plan.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
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
import com.sos.joc.model.plan.PlanSchemaId;
import com.sos.joc.model.plan.Plans;
import com.sos.joc.model.plan.PlansFilter;
import com.sos.joc.plan.common.PlannedBoards;
import com.sos.joc.plan.resource.IPlansResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.order.OrderId;
import js7.data.plan.PlanId;
import js7.data_for_java.board.JPlannedBoard;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.plan.JPlan;

@Path("plans")
public class PlansResourceImpl extends JOCResourceImpl implements IPlansResource {

    private static final String API_CALL = "./plans";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlansResourceImpl.class);
    private static final ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
    private final static int limitOrdersDefault = 10000;
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
    
    private com.sos.joc.model.plan.Plan getPlan(PlanId pId, JPlan jp, PlansFilter filter) {
        Plan plan = new Plan();
        boolean isClosed = jp.isClosed();
        if (isClosed && filter.getOnlyOpenPlans()) {
            return null;
        }
        if (!isClosed && filter.getOnlyClosedPlans()) {
            return null;
        }
        plan.setClosed(isClosed);
        
        PlanSchemaId pSchemaId = PlanSchemaId.fromValue(pId.planSchemaId().string());
        if (filter.getPlanSchemaIds() != null && !filter.getPlanSchemaIds().isEmpty() && !filter.getPlanSchemaIds().contains(pSchemaId)) {
            return null;
        }
        
        String plankey = pId.planKey() == null ? null : pId.planKey().string(); //is maybe null for global schema
        // keys are ignored for global plan schema
        if (filter.getPlanKeys() != null && !filter.getPlanKeys().isEmpty() && !PlanSchemaId.Global.equals(pSchemaId)) {
            if (!filter.getPlanKeys().contains(plankey)) {
                // looking for globs inside filter.getPlanKeys()
                if (!filter.getPlanKeys().stream().filter(pk -> pk.contains("*") || pk.contains("?")).anyMatch(pk -> plankey.matches(pk.replace("*",
                        ".*").replace("?", ".")))) {
                    return null;
                }
            }
        }
        
        com.sos.joc.model.plan.PlanId planId = new com.sos.joc.model.plan.PlanId();
        planId.setPlanSchemaId(pSchemaId);
        planId.setPlanKey(plankey);
        plan.setPlanId(planId);
        
        Stream<JOrder> jOrders = OrdersHelper.getPermittedJOrdersFromOrderIds(jp.orderIds(), folderPermissions.getListOfFolders(), currentState);
        Map<String, OrderV> orders = Collections.emptyMap();
        
        if (filter.getCompact()) {
            plan.setOrders(null);
            plan.setNumOfOrders(jOrders.mapToInt(o -> 1).sum());
        } else {
            Map<String, Set<String>> orderTags = getOrderTags(filter.getControllerId(), jp);
            Long surveyDateMillis = currentState.instant().toEpochMilli();
            AtomicInteger counter = new AtomicInteger(0);

            Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                try {
                    counter.incrementAndGet();
                    return OrdersHelper.mapJOrderToOrderV(o, currentState, true, orderTags, null, surveyDateMillis, zoneId);
                } catch (Exception e) {
                    return null;
                }
            };

            Stream<OrderV> ordersStream = jOrders.map(mapJOrderToOrderV).filter(Objects::nonNull);
            
            Integer limitOrders = filter.getLimit() == null ? limitOrdersDefault : filter.getLimit();
            if (limitOrders > -1 && jp.orderIds().size() > limitOrders) {
                ordersStream = ordersStream.sorted(Comparator.comparingLong(OrderV::getScheduledFor).reversed()).limit(limitOrders.longValue()); 
            }
            orders = ordersStream.collect(Collectors.toMap(OrderV::getOrderId, Function.identity()));

            plan.setOrders(orders.values());
            plan.setNumOfOrders(counter.get());
        }

        plan.setNoticeBoards(getBoards(jp.toPlannedBoard(), filter, orders));
        
        return plan;
    }
    
    private Map<String, Set<String>> getOrderTags(String controllerId, JPlan jp) {
        Map<String, Set<String>> orderTags = null;
        try {
            orderTags = OrderTags.getTagsByOrderIds(controllerId, jp.orderIds().stream().map(OrderId::string), session);
        } catch (SOSHibernateException e) {
            if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                LOGGER.info(getJocError().printMetaInfo());
                getJocError().clearMetaInfo();
            }
            LOGGER.error("", e);
        }
        return orderTags;
    }
    
    private List<Board> getBoards(Map<BoardPath, JPlannedBoard> jBoards, PlansFilter filter, Map<String, OrderV> orders) {
        try {
            
            Stream<String> availableBoardNames = jBoards.keySet().stream().map(BoardPath::string);
            if (filter.getNoticeBoardPaths() != null && !filter.getNoticeBoardPaths().isEmpty()) {
                Set<String> requestedBoardNames = filter.getNoticeBoardPaths().stream().map(JocInventory::pathToName).collect(Collectors.toSet());
                availableBoardNames.filter(bp -> requestedBoardNames.contains(bp));
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
    
//    private void print(Map<PlanId, JPlan> map) {
//        System.out.println("PlanIds: " + map.keySet().stream().map(PlanId::shortString).collect(Collectors.toSet()));
//        map.entrySet().stream().forEach(e -> {
//                    System.out.println("PlanId: " + e.getKey().shortString());
//                    System.out.println("isGlobal? " + e.getKey().isGlobal());
//                    System.out.println("isClosed? " + e.getValue().isClosed());
//                    System.out.println("OrderIds: " + e.getValue().orderIds().stream().map(OrderId::string).collect(Collectors.toSet()));
//                    AtomicInteger index = new AtomicInteger(1);
//                    e.getValue().toPlannedBoard().forEach((bPath, pboard) -> {
//                        int i = index.getAndIncrement();
//                        System.out.println("BoardPath[" + i + "]: " + bPath.string());
//                        System.out.println("PlannedBoardId[" + i + "]: " + pboard.id());
//                        AtomicInteger index2 = new AtomicInteger(1);
//                        pboard.toNoticePlace().forEach((nKey, nPlace) -> {
//                            int j = index2.getAndIncrement();
//                            System.out.println("NoticeKey[" + i + "," + j + "]: " + nKey.string());
//                            System.out.println("ExpectingOrderIds[" + i + "," + j + "]: " + nPlace.expectingOrderIds().stream().map(
//                                    OrderId::string).collect(Collectors.toSet()));
//                            System.out.println("isAnnounced[" + i + "," + j + "]? " + nPlace.isAnnounced());
//                            System.out.println("Notice[" + i + "," + j + "]? " + nPlace.notice());
//                        });
//                        
//                    });
//                });
//    }
    

    private List<Board> getBoards(BoardsFilter filter, Map<BoardPath, JPlannedBoard> jBoards, Map<String, OrderV> orders, boolean compact)
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
                
                PlannedBoards plB = new PlannedBoards(jBoards, orders, compact, filter.getLimit());
                
                return contents.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)).map(dc -> {
                    try {
                        if (dc.getContent() == null || dc.getContent().isEmpty()) {
                            throw new DBMissingDataException("doesn't exist");
                        }
                        return plB.getPlannedBoard(dc);
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