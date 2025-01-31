package com.sos.joc.plan.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.board.Board;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.board.common.BoardHelper;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
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
import com.sos.joc.model.plan.Plan;
import com.sos.joc.model.plan.PlanSchemaId;
import com.sos.joc.model.plan.Plans;
import com.sos.joc.model.plan.PlansFilter;
import com.sos.joc.plan.resource.IPlansResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.order.OrderId;
import js7.data.plan.PlanId;
import js7.data_for_java.board.JPlannedBoard;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.plan.JPlan;

@Path("plans")
public class PlansResourceImpl extends JOCResourceImpl implements IPlansResource {

    private static final String API_CALL = "./plans";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlansResourceImpl.class);

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
            
            JControllerState currentState = Proxy.of(filter.getControllerId()).currentState();
            Plans entity = new Plans();
            entity.setSurveyDate(Date.from(currentState.instant()));
            entity.setPlans(get(currentState.toPlan(), filter));
            
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private com.sos.joc.model.plan.Plan getPlan(PlanId pId, JPlan jp) {
        Plan plan = new Plan();
        com.sos.joc.model.plan.PlanId planId = new com.sos.joc.model.plan.PlanId();
        planId.setPlanSchemaId(PlanSchemaId.fromValue(pId.planSchemaId().string()));
        planId.setPlanKey(pId.planKey() == null ? null : pId.planKey().string());
        plan.setPlanId(planId);
        plan.setOrderIds(jp.orderIds().stream().map(OrderId::string).collect(Collectors.toList()));
        plan.setClosed(jp.isClosed());
        return plan;
    }
    
    private List<Board> getBoards(PlanId planId, Map<BoardPath, JPlannedBoard> jBoards, PlansFilter filter) {
        try {
            List<String> boardPaths = jBoards.keySet().stream().map(BoardPath::string).collect(Collectors.toList());
            if (boardPaths.isEmpty()) {
                return null;
            }
            
            BoardsFilter bFilter = new BoardsFilter();
            bFilter.setControllerId(filter.getControllerId());
            bFilter.setNoticeBoardPaths(boardPaths);
            
            List<Board> boards = getBoards(bFilter, planId, jBoards);
            
            return boards;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<Plan> get(Map<PlanId, JPlan> map, PlansFilter filter) throws Exception {
        return map.entrySet().stream().map(e -> {
            Plan plan = getPlan(e.getKey(), e.getValue());
            plan.setNoticeBoards(getBoards(e.getKey(), e.getValue().toPlannedBoard(), filter));
            return plan;
        }).collect(Collectors.toList());
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

    private List<Board> getBoards(BoardsFilter filter, PlanId planId, Map<BoardPath, JPlannedBoard> jBoards) throws Exception {
        SOSHibernateSession session = null;
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

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
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
            
            final JControllerState controllerState = BoardHelper.getCurrentState(controllerId);
            final long surveyDateMillis = controllerState != null ? controllerState.instant().toEpochMilli() : Instant.now().toEpochMilli();
            final Set<Folder> permittedFolders = withFolderFilter ? null : folders;
            ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
            
            JocError jocError = getJocError();
            if (contents != null) {
                
                Integer limit = filter.getLimit() != null ? filter.getLimit() : 10000;
                //TODO Map<String, Set<String>> orderTags = OrderTags.getTags(controllerId, eos.stream().map(ExpectingOrder::getJOrder), session);
                Map<String, Set<String>> orderTags = null;

                return contents.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)).map(dc -> {
                    try {
                        if (dc.getContent() == null || dc.getContent().isEmpty()) {
                            throw new DBMissingDataException("doesn't exist");
                        }
                        return BoardHelper.getPlannedBoard(controllerState, dc, planId, jBoards.get(BoardPath.of(dc.getName())).toNoticePlace(),
                                folders, orderTags, limit, zoneId, surveyDateMillis, dbLayer.getSession());
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
        } finally {
            Globals.disconnect(session);
        }

    }

}