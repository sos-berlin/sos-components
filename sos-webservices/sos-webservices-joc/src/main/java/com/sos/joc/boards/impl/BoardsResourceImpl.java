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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.board.Board;
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
import com.sos.joc.model.plan.PlanSchemaId;
import com.sos.joc.plan.common.PlannedBoards;
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
            Instant surveyInstant = Instant.now();
            Date now = Date.from(surveyInstant);
            answer.setSurveyDate(now);
            final JControllerState controllerState = BoardHelper.getCurrentState(controllerId);
            if (controllerState != null) {
                surveyInstant = controllerState.instant();
                answer.setSurveyDate(Date.from(surveyInstant));
            }
            final Set<Folder> permittedFolders = withFolderFilter ? null : folders;
            ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
            
            JocError jocError = getJocError();
            if (contents != null) {
                Set<BoardPath> boardNames = contents.stream().map(DeployedContent::getName).map(BoardPath::of).collect(Collectors.toSet());
                Map<BoardPath, List<JPlannedBoard>> jBoards = getPathToBoard(controllerState, boardNames, filter);
                
                Map<OrderId, OrderV> orders = Collections.emptyMap();
                if (filter.getCompact() != Boolean.TRUE) {
                    
                    Set<OrderId> eos = jBoards.values().stream().flatMap(Collection::stream).map(JPlannedBoard::toNoticePlace).map(Map::values)
                            .flatMap(Collection::stream).map(JNoticePlace::expectingOrderIds).flatMap(Collection::stream).collect(Collectors.toSet());

                    Map<String, Set<String>> orderTags = OrderTags.getTagsByOrderIds(controllerId, eos.stream().map(OrderId::string), session);
                    final long surveyDateMillis = surveyInstant.toEpochMilli();
                            
                    Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                        try {
                            return OrdersHelper.mapJOrderToOrderV(o, controllerState, true, orderTags, null, surveyDateMillis, zoneId);
                        } catch (Exception e) {
                            return null;
                        }
                    };

                    orders = OrdersHelper.getPermittedJOrdersFromOrderIds(eos, permittedFolders, controllerState).map(mapJOrderToOrderV).filter(
                            Objects::nonNull).collect(Collectors.toMap(o -> OrderId.of(o.getOrderId()), Function.identity()));
                }
                
                PlannedBoards plB = new PlannedBoards(jBoards, orders, filter.getCompact() == Boolean.TRUE, filter.getLimit(), controllerState);
                
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
            }
            answer.setDeliveryDate(now);
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Map<BoardPath, List<JPlannedBoard>> getPathToBoard(JControllerState currentState, Set<BoardPath> boards, BoardsFilter filter) {
        if (currentState == null) {
            return Collections.emptyMap();
        }
        
        Stream<Map.Entry<PlanId, JPlan>> plansStream = currentState.toPlan().entrySet().stream();
        
        if (filter.getPlanSchemaIds() != null && !filter.getPlanSchemaIds().isEmpty()) {
            Set<String> schemaIds = filter.getPlanSchemaIds().stream().map(PlanSchemaId::name).collect(Collectors.toSet());
            plansStream = plansStream.filter(e -> schemaIds.contains(e.getKey().planSchemaId().string()));
        }
        if (filter.getPlanKeys() != null && !filter.getPlanKeys().isEmpty()) {
            Predicate<Map.Entry<PlanId, JPlan>> planKeyFilter = e -> e.getKey().isGlobal() || filter.getPlanKeys().stream().map(pk -> pk.replace("*",
                    ".*").replace("?", ".")).anyMatch(pk -> e.getKey().planKey().string().matches(pk));
            plansStream = plansStream.filter(planKeyFilter);
        }
        
        Stream<Map.Entry<BoardPath, JPlannedBoard>> stream = plansStream.map(Map.Entry::getValue).map(JPlan::toPlannedBoard).flatMap(m -> m
                .entrySet().stream());
        if (boards != null) {
            stream = stream.filter(e -> boards.contains(e.getKey()));
        }
        return stream.collect(Collectors.groupingBy(e -> e.getKey(), Collectors.mapping(e -> e.getValue(), Collectors.toList())));
    }

}