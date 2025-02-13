package com.sos.joc.board.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.joc.Globals;
import com.sos.joc.board.resource.INoticesModify;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.board.BoardHelper;
import com.sos.joc.classes.board.ExpectingOrder;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.board.DeleteNotices;
import com.sos.joc.model.board.ModifyNotices;
import com.sos.joc.model.board.NoticeIdsPerBoard;
import com.sos.joc.model.board.PostExpectedNotices;
import com.sos.joc.model.board.PostNotices;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.board.NoticeId;
import js7.data.board.NoticeKey;
import js7.data.controller.ControllerCommand;
import js7.data.plan.PlanId;
import js7.data_for_java.board.JBoardState;
import js7.data_for_java.board.JNoticePlace;
import js7.data_for_java.board.JPlannedBoard;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.plan.JPlan;
import js7.proxy.javaapi.JControllerProxy;

@Path("notices")
public class NoticesModifyImpl extends JOCResourceImpl implements INoticesModify {

    private static final String API_CALL = "./notices/";
    
    private enum Action {
        POST, DELETE
    }

    @Override
    public JOCDefaultResponse deleteNotices(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + Action.DELETE.name().toLowerCase(), filterBytes, accessToken);
            JsonValidator.validate(filterBytes, DeleteNotices.class, true);
            DeleteNotices in = Globals.objectMapper.readValue(filterBytes, DeleteNotices.class);
            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getDelete());
            if (response != null) {
                return response;
            }

            storeAuditLog(in.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            JControllerProxy proxy = Proxy.of(controllerId);
            
            if (in.getNotices() != null && !in.getNotices().isEmpty()) {
                
                getBatchCommand(proxy.currentState(), in, Action.DELETE).ifPresent(command -> proxy.api().executeCommand(
                        command).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId)));

            } else { //deprecated
                
                final String board = JocInventory.pathToName(in.getNoticeBoardPath());
                if (proxy.currentState().pathToBoard().get(BoardPath.of(board)) == null) {
                    throw new ControllerObjectNotExistException("Controller '" + controllerId + "' couldn't find the Notice Board '" + board + "'");
                }
                proxy.api().executeCommand(JControllerCommand.batch(in.getNoticeIds().stream().map(n -> BoardHelper.getNoticeId(n, board)).map(
                        ControllerCommand.DeleteNotice::new).map(JControllerCommand::apply).collect(Collectors.toList()))).thenAccept(
                                e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse postNotices(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + Action.POST.name().toLowerCase(), filterBytes, accessToken);
            JsonValidator.validate(filterBytes, PostNotices.class, true);
            PostNotices in = Globals.objectMapper.readValue(filterBytes, PostNotices.class);
            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getPost());
            if (response != null) {
                return response;
            }

            storeAuditLog(in.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            JControllerProxy proxy = Proxy.of(controllerId);
            Instant now = Instant.now();
            
            if (in.getNotices() != null && !in.getNotices().isEmpty()) {
                
                Optional<Instant> endOfLife = getEndOfLife(in.getEndOfLife(), in.getTimeZone(), now);
                getBatchCommand(proxy.currentState(), in, endOfLife, Action.POST).ifPresent(command -> proxy.api().executeCommand(command).thenAccept(
                        e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId)));

            } else { //deprecated

                Map<BoardPath, JBoardState> boards = proxy.currentState().pathToBoardState();
                Map<Boolean, List<String>> map = in.getNoticeBoardPaths().stream().map(JocInventory::pathToName).collect(
                        Collectors.groupingBy(b -> boards.containsKey(BoardPath.of(b))));
                map.putIfAbsent(Boolean.FALSE, Collections.emptyList());
                map.putIfAbsent(Boolean.TRUE, Collections.emptyList());

                if (!map.get(Boolean.FALSE).isEmpty()) {
                    throw new ControllerObjectNotExistException("Controller '" + controllerId + "' couldn't find the Notice Boards " + map.get(
                            Boolean.FALSE).stream().collect(Collectors.joining("', '", "['", "']")));
                }
                
                if (!map.get(Boolean.TRUE).isEmpty()) {
                    Optional<Instant> endOfLife = getEndOfLife(in.getEndOfLife(), in.getTimeZone(), now);

                    proxy.api().executeCommand(JControllerCommand.batch(map.getOrDefault(Boolean.TRUE, Collections.emptyList()).stream().map(
                            b -> BoardHelper.getNoticeId(in.getNoticeId(), b)).map(nId -> JControllerCommand.postNotice(nId, endOfLife)).collect(
                                    Collectors.toList()))).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(),
                                            controllerId));
                }
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(now));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse postExpectedNotices(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + Action.POST.name().toLowerCase() + "/expected", filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, PostExpectedNotices.class);
            PostExpectedNotices in = Globals.objectMapper.readValue(filterBytes, PostExpectedNotices.class);
            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getPost());
            if (response != null) {
                return response;
            }
            
            storeAuditLog(in.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            JControllerProxy proxy = Proxy.of(controllerId);
            Set<BoardPath> boardPaths = proxy.currentState().pathToBoard().keySet();
            Instant now = Instant.now();
            Optional<Instant> endOfLife = getEndOfLife(in.getEndOfLife(), in.getTimeZone(), now);
            
            Map<BoardPath, Set<String>> expectedNotices = in.getExpectedNotices().stream().peek(en -> en.setNoticeBoardPath(JocInventory.pathToName(en
                    .getNoticeBoardPath()))).collect(Collectors.toMap(en -> BoardPath.of(en.getNoticeBoardPath()), en -> en.getWorkflowPaths()
                            .stream().map(JocInventory::pathToName).collect(Collectors.toSet()), (k, v) -> k));
            expectedNotices.keySet().removeIf(key -> !boardPaths.contains(key));
            
            if (!expectedNotices.isEmpty()) {
                Stream<ExpectingOrder> expectingOrdersStream = BoardHelper.getExpectingOrdersStream(proxy.currentState(), expectedNotices.keySet(),
                        folderPermissions.getListOfFolders());
                Predicate<ExpectingOrder> filterWorkflow = o -> {
                    Set<String> workflows = expectedNotices.getOrDefault(BoardPath.of(o.getBoardPath()), Collections.emptySet());
                    return workflows.isEmpty() || workflows.contains(o.getJOrder().workflowId().path().string());
                };
                proxy.api().executeCommand(JControllerCommand.batch(expectingOrdersStream.filter(filterWorkflow).map(o -> JControllerCommand
                        .postNotice(o.getNoticeId(), endOfLife)).distinct().collect(Collectors.toList()))).thenAccept(
                                e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(now));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private Optional<Instant> getEndOfLife(String endOfLife, String timezone, Instant now) {
        Optional<Instant> endOfLifeOpt = Optional.empty();
        if (endOfLife != null && !endOfLife.isEmpty()) {
            Instant endOfLifeInstant = JobSchedulerDate.getInstantFromDateStr(endOfLife, false, timezone);
            if (endOfLifeInstant.isAfter(now)) {
                endOfLifeOpt = Optional.of(endOfLifeInstant);
            }
        }
        return endOfLifeOpt;
    }
    
    private Optional<JControllerCommand> getBatchCommand(JControllerState currentState, ModifyNotices in, Action action) {
        return getBatchCommand(currentState, in, Optional.empty(), action);
    }
    
    private Optional<JControllerCommand> getBatchCommand(JControllerState currentState, ModifyNotices in, Optional<Instant> endOfLife,
            Action action) {
        Set<BoardPath> boards = currentState.pathToBoardState().keySet();
        Map<Boolean, Set<BoardPath>> map = in.getNotices().stream()
                .map(NoticeIdsPerBoard::getNoticeBoardPath)
                .map(JocInventory::pathToName)
                .map(BoardPath::of)
                .collect(Collectors.groupingBy(b -> boards.contains(b), Collectors.toSet()));
        map.putIfAbsent(Boolean.FALSE, Collections.emptySet());
        map.putIfAbsent(Boolean.TRUE, Collections.emptySet());

        if (!map.get(Boolean.FALSE).isEmpty()) {
            throw new ControllerObjectNotExistException("Controller '" + in.getControllerId() + "' couldn't find the Notice Boards " + map.get(
                    Boolean.FALSE).stream().map(BoardPath::string).collect(Collectors.joining("', '", "['", "']")));
        }
        if (!map.get(Boolean.TRUE).isEmpty()) {
            
            Map<BoardPath, List<JPlannedBoard>> plannedBoards = getBoardToPlannedBoards(currentState, in, boards);
            
            Predicate<Map.Entry<NoticeKey, JNoticePlace>> isPostableNotice = n -> !n.getValue().notice().isPresent();
            return Optional.of(JControllerCommand.batch(in.getNotices().stream()
                    .peek(notice -> notice.setNoticeBoardPath(JocInventory.pathToName(notice.getNoticeBoardPath())))
                    .filter(notice -> boards.contains(BoardPath.of(notice.getNoticeBoardPath())))
                    .flatMap(notice -> {
                        if (notice.getNoticeIds() == null || notice.getNoticeIds().isEmpty()) {
                            BoardPath boardPath = BoardPath.of(notice.getNoticeBoardPath());
                            Stream<JPlannedBoard> nps = plannedBoards.getOrDefault(boardPath, Collections.emptyList()).stream();
                            switch (action) {
                            case DELETE: // only posted notices
                                return nps.map(JPlannedBoard::toNoticePlace).map(Map::values).flatMap(Collection::stream).map(JNoticePlace::notice)
                                        .filter(Optional::isPresent).map(Optional::get).map(jn -> jn.asScala().id()).map(n -> getActionCommand(n,
                                                endOfLife, action));
                            case POST: // only expected or announced notices
                                return nps.flatMap(pb -> {
                                    PlanId pId = pb.id().planId();
                                    return pb.toNoticePlace().entrySet().stream().filter(isPostableNotice).map(e -> NoticeId.of(pId, boardPath, e
                                            .getKey())).distinct().map(n -> getActionCommand(n, endOfLife, action));
                                });
                            }
                            return null;
                        } else {
                            return notice.getNoticeIds().stream().map(n -> BoardHelper.getNoticeId(n, notice.getNoticeBoardPath())).map(
                                    n -> getActionCommand(n, endOfLife, action));
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        return Optional.empty();
    }
    
    private Map<BoardPath, List<JPlannedBoard>> getBoardToPlannedBoards(JControllerState currentState, ModifyNotices in, Set<BoardPath> boards) {
        if (in.getNotices().stream().anyMatch(n -> n.getNoticeIds() == null || n.getNoticeIds().isEmpty())) {
            return currentState.toPlan().values().stream().map(JPlan::toPlannedBoard).flatMap(m -> m.entrySet().stream()).filter(e -> boards.contains(
                    e.getKey())).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }
        return Collections.emptyMap();
    }
    
    private JControllerCommand getActionCommand(NoticeId noticeId, Optional<Instant> endOfLife, Action action) {
        switch (action) {
        case DELETE:
            return JControllerCommand.apply(new ControllerCommand.DeleteNotice(noticeId));
        default: //case POST:
            return JControllerCommand.postNotice(noticeId, endOfLife);
        }
    }

}
