package com.sos.joc.boards.impl;

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
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.boards.resource.IBoardsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.board.BoardHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.Boards;
import com.sos.joc.model.board.BoardsFilter;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.plan.common.PlannedBoards;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.order.OrderId;
import js7.data_for_java.board.JNoticePlace;
import js7.data_for_java.board.JPlannedBoard;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.plan.JPlan;

@Path("notice")
public class BoardsResourceImpl extends JOCResourceImpl implements IBoardsResource {

    private static final String API_CALL = "./notice/boards";
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardsResourceImpl.class);

    @Override
    public JOCDefaultResponse postBoards(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, BoardsFilter.class);
            BoardsFilter filter = Globals.objectMapper.readValue(filterBytes, BoardsFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getControllerPermissions(filter.getControllerId(), accessToken)
                    .getNoticeBoards().getView());
            if (response != null) {
                return response;
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(getBoards(filter)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Boards getBoards(BoardsFilter filter) throws Exception {
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
            
            Boards answer = new Boards();
            Date now = Date.from(Instant.now());
            answer.setSurveyDate(now);
            final JControllerState controllerState = BoardHelper.getCurrentState(controllerId);
            if (controllerState != null) {
                answer.setSurveyDate(Date.from(controllerState.instant()));
            }
            final long surveyDateMillis = controllerState != null ? controllerState.instant().toEpochMilli() : Instant.now().toEpochMilli();
            final Set<Folder> permittedFolders = withFolderFilter ? null : folders;
            ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
            
            JocError jocError = getJocError();
            if (contents != null) {
                Set<BoardPath> boardNames = contents.stream().map(DeployedContent::getName).map(BoardPath::of).collect(Collectors.toSet());
                Map<BoardPath, List<JPlannedBoard>> jBoards = getPathToBoard(controllerState, boardNames);
                
                Map<String, OrderV> orders = Collections.emptyMap();
                if (filter.getCompact() != Boolean.TRUE) {
                    Integer limit = filter.getLimit() != null ? filter.getLimit() : 10000;
                    
                    Set<OrderId> eos = jBoards.values().stream().flatMap(Collection::stream).map(JPlannedBoard::toNoticePlace).map(Map::values)
                            .flatMap(Collection::stream).map(JNoticePlace::expectingOrderIds).flatMap(Collection::stream).collect(Collectors.toSet());
                    //List<JOrder> eos = BoardHelper.getAllExpectingOrdersStream(controllerState, folders).collect(Collectors.toList());

                    Map<String, Set<String>> orderTags = OrderTags.getTagsByOrderIds(controllerId, eos.stream().map(OrderId::string), session);

                    Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                        try {
                            return OrdersHelper.mapJOrderToOrderV(o, controllerState, true, orderTags, null, null, zoneId);
                        } catch (Exception e) {
                            return null;
                        }
                    };

                    orders = OrdersHelper.getPermittedJOrdersFromOrderIds(eos, permittedFolders, controllerState).map(mapJOrderToOrderV).filter(
                            Objects::nonNull).collect(Collectors.toMap(OrderV::getOrderId, Function.identity()));
                    
//                    orders = eos.stream().map(mapJOrderToOrderV).filter(Objects::nonNull).collect(Collectors.toMap(
//                            OrderV::getOrderId, Function.identity()));
                }
                
                PlannedBoards plB = new PlannedBoards(jBoards, orders, filter.getCompact() == Boolean.TRUE, controllerState);
                
                answer.setNoticeBoards(contents.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)).map(dc -> {
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
                        LOGGER.error(String.format("[%s] %s", dc.getPath(), e));
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
                
//                Set<String> boardNames = contents.stream().map(DeployedContent::getName).collect(Collectors.toSet());
//                if (filter.getCompact() == Boolean.TRUE) {
//                    ConcurrentMap<String, ConcurrentMap<NoticeId, Integer>> numOfExpectings = BoardHelper.getNumOfExpectingOrders(controllerState,
//                            boardNames, folders);
//                    answer.setNoticeBoards(contents.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)).map(dc -> {
//                        try {
//                            if (dc.getContent() == null || dc.getContent().isEmpty()) {
//                                throw new DBMissingDataException("doesn't exist");
//                            }
//                            return BoardHelper.getCompactBoard(controllerState, dc, numOfExpectings.getOrDefault(dc.getName(),
//                                    new ConcurrentHashMap<>()));
//                        } catch (Throwable e) {
//                            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
//                                LOGGER.info(jocError.printMetaInfo());
//                                jocError.clearMetaInfo();
//                            }
//                            LOGGER.error(String.format("[%s] %s", dc.getPath(), e.toString()));
//                            return null;
//                        }
//                    }).filter(Objects::nonNull).collect(Collectors.toList()));
//                } else {
//                    Integer limit = filter.getLimit() != null ? filter.getLimit() : 10000;
//                    List<ExpectingOrder> eos = BoardHelper.getExpectingOrdersStream(controllerState, boardNames, folders).collect(Collectors
//                            .toList());
//                    ConcurrentMap<String, ConcurrentMap<NoticeId, List<JOrder>>> expectings = BoardHelper.getExpectingOrders(eos.stream());
//                    Map<String, Set<String>> orderTags = OrderTags.getTags(controllerId, eos.stream().map(ExpectingOrder::getJOrder), session);
//
//                    answer.setNoticeBoards(contents.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)).map(dc -> {
//                        try {
//                            if (dc.getContent() == null || dc.getContent().isEmpty()) {
//                                throw new DBMissingDataException("doesn't exist");
//                            }
//                            return BoardHelper.getBoard(controllerState, dc, expectings.getOrDefault(dc.getName(), new ConcurrentHashMap<>()),
//                                    orderTags, limit, zoneId, surveyDateMillis, dbLayer.getSession());
//                        } catch (Throwable e) {
//                            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
//                                LOGGER.info(jocError.printMetaInfo());
//                                jocError.clearMetaInfo();
//                            }
//                            LOGGER.error(String.format("[%s] %s", dc.getPath(), e.toString()));
//                            return null;
//                        }
//                    }).filter(Objects::nonNull).collect(Collectors.toList()));
//                }
            }
            answer.setDeliveryDate(now);
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Map<BoardPath, List<JPlannedBoard>> getPathToBoard(JControllerState currentState, Set<BoardPath> boards) {
        if (currentState == null) {
            return Collections.emptyMap();
        }
        Stream<Map.Entry<BoardPath, JPlannedBoard>> stream = currentState.toPlan().values().stream().map(JPlan::toPlannedBoard).flatMap(m -> m
                .entrySet().stream());
        if (boards != null) {
            stream = stream.filter(e -> boards.contains(e.getKey()));
        }
        return stream.collect(Collectors.groupingBy(e -> e.getKey(), Collectors.mapping(e -> e.getValue(), Collectors.toList())));
    }

}