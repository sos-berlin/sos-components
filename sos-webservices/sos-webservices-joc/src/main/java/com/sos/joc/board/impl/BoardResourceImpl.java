package com.sos.joc.board.impl;

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

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.board.resource.IBoardResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.board.BoardHelper;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.Board;
import com.sos.joc.model.board.BoardFilter;
import com.sos.joc.model.order.OrderV;
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
public class BoardResourceImpl extends JOCResourceImpl implements IBoardResource {

    private static final String API_CALL = "./notice/board";

    @Override
    public JOCDefaultResponse postBoard(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, BoardFilter.class);
            BoardFilter filter = Globals.objectMapper.readValue(filterBytes, BoardFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getBasicControllerPermissions(filter.getControllerId(),
                    accessToken).getNoticeBoards().getView());
            if (response != null) {
                return response;
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getBoard(filter)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private Board getBoard(BoardFilter filter) throws Exception {
        SOSHibernateSession session = null;
        try {
            Board answer = new Board();
            String controllerId = filter.getControllerId();
            Instant surveyInstant = Instant.now();
            answer.setSurveyDate(Date.from(surveyInstant));
            final JControllerState currentState = BoardHelper.getCurrentState(controllerId);
            List<JPlannedBoard> pbs = Collections.emptyList();
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DeployedContent dc = dbLayer.getDeployedInventory(controllerId, DeployType.NOTICEBOARD.intValue(), filter.getNoticeBoardPath());
            
            if (dc == null || dc.getContent() == null || dc.getContent().isEmpty()) {
                throw new DBMissingDataException(String.format("Notice board '%s' doesn't exist", filter.getNoticeBoardPath()));
            }
            checkFolderPermissions(dc.getPath());
            
            BoardPath boardPath = BoardPath.of(dc.getName());
            if (currentState != null) {
                surveyInstant = currentState.instant();
                answer.setSurveyDate(Date.from(surveyInstant));
                
                Stream<Map.Entry<PlanId, JPlan>> plansStream = currentState.toPlan().entrySet().stream();
                
                if (filter.getPlanSchemaIds() != null && !filter.getPlanSchemaIds().isEmpty()) {
                    Set<String> schemaIds = filter.getPlanSchemaIds();
                    plansStream = plansStream.filter(e -> schemaIds.contains(e.getKey().planSchemaId().string()));
                }
                if (filter.getNoticeSpaceKeys() != null && !filter.getNoticeSpaceKeys().isEmpty()) {
                    Predicate<Map.Entry<PlanId, JPlan>> planKeyFilter = e -> e.getKey().isGlobal() || filter.getNoticeSpaceKeys().stream().map(
                            pk -> pk.replace("*", ".*").replace("?", ".")).anyMatch(pk -> e.getKey().planKey().string().matches(pk));
                    plansStream = plansStream.filter(planKeyFilter);
                }
                
                pbs = plansStream.map(Map.Entry::getValue).map(JPlan::toPlannedBoard).map(m -> m.get(boardPath)).filter(Objects::nonNull).collect(
                        Collectors.toList());
            }
            
            Map<OrderId, OrderV> orders = Collections.emptyMap();
            if (filter.getCompact() != Boolean.TRUE) {
                ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();

                Set<OrderId> eos = pbs.stream().map(JPlannedBoard::toNoticePlace).map(Map::values).flatMap(Collection::stream).map(
                        JNoticePlace::expectingOrderIds).flatMap(Collection::stream).collect(Collectors.toSet());

                Map<String, Set<String>> orderTags = OrderTags.getTagsByOrderIds(controllerId, eos.stream().map(OrderId::string), session);
                long surveyDateMillis = surveyInstant.toEpochMilli();
                
                Function<JOrder, OrderV> mapJOrderToOrderV = o -> {
                    try {
                        return OrdersHelper.mapJOrderToOrderV(o, currentState, true, orderTags, null, surveyDateMillis, zoneId);
                    } catch (Exception e) {
                        return null;
                    }
                };

                orders = OrdersHelper.getPermittedJOrdersFromOrderIds(eos, folderPermissions.getListOfFolders(), currentState).map(mapJOrderToOrderV)
                        .filter(Objects::nonNull).collect(Collectors.toMap(o -> OrderId.of(o.getOrderId()), Function.identity()));
            }
            
            PlannedBoards plB = new PlannedBoards(Collections.singletonMap(boardPath, pbs), orders, filter.getCompact() == Boolean.TRUE, filter
                    .getLimit(), currentState);
            answer.setNoticeBoard(plB.getPlannedBoard(dc));
            answer.setDeliveryDate(Date.from(Instant.now()));
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
