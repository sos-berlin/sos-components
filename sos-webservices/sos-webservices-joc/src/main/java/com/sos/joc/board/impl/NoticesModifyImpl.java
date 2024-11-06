package com.sos.joc.board.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.joc.Globals;
import com.sos.joc.board.common.BoardHelper;
import com.sos.joc.board.common.ExpectingOrder;
import com.sos.joc.board.resource.INoticesModify;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.board.DeleteNotices;
import com.sos.joc.model.board.ExpectedNoticesPerBoard;
import com.sos.joc.model.board.ModifyNotices;
import com.sos.joc.model.board.NoticeIdsPerBoard;
import com.sos.joc.model.board.PostExpectedNotices;
import com.sos.joc.model.board.PostNotices;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.board.NoticeId;
import js7.data.board.NoticePlace;
import js7.data.controller.ControllerCommand;
import js7.data_for_java.board.JGlobalBoardState;
import js7.data_for_java.controller.JControllerCommand;
import js7.proxy.javaapi.JControllerProxy;
import scala.collection.JavaConverters;

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
                
                getBatchCommand(proxy.currentState().pathToBoardState(), in, Action.DELETE).ifPresent(command -> proxy.api().executeCommand(
                        command).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId)));

            } else { //deprecated
                
                final BoardPath board = BoardPath.of(JocInventory.pathToName(in.getNoticeBoardPath()));
                if (proxy.currentState().pathToBoard().get(board) == null) {
                    throw new ControllerObjectNotExistException("Controller '" + controllerId + "' couldn't find the Notice Board '" + board.string() + "'");
                }
                proxy.api().executeCommand(JControllerCommand.batch(in.getNoticeIds().stream().map(NoticeId::of).map(
                        n -> new ControllerCommand.DeleteNotice(board, n)).map(JControllerCommand::apply).collect(Collectors.toList()))).thenAccept(
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
            Map<BoardPath, JGlobalBoardState> boards = proxy.currentState().pathToBoardState();
            Instant now = Instant.now();
            
            if (in.getNotices() != null && !in.getNotices().isEmpty()) {
                
                Optional<Instant> endOfLife = getEndOfLife(in.getEndOfLife(), in.getTimeZone(), now);
                getBatchCommand(boards, in, endOfLife, Action.POST).ifPresent(command -> proxy.api().executeCommand(command).thenAccept(e -> ProblemHelper
                        .postProblemEventIfExist(e, accessToken, getJocError(), controllerId)));

            } else { //deprecated

                Map<Boolean, List<BoardPath>> map = in.getNoticeBoardPaths().stream().map(JocInventory::pathToName).map(BoardPath::of).collect(
                        Collectors.groupingBy(b -> boards.containsKey(b)));
                map.putIfAbsent(Boolean.FALSE, Collections.emptyList());
                map.putIfAbsent(Boolean.TRUE, Collections.emptyList());

                if (!map.get(Boolean.FALSE).isEmpty()) {
                    throw new ControllerObjectNotExistException("Controller '" + controllerId + "' couldn't find the Notice Boards " + map.get(
                            Boolean.FALSE).stream().map(BoardPath::string).collect(Collectors.joining("', '", "['", "']")));
                }
                
                if (!map.get(Boolean.TRUE).isEmpty()) {
                    Optional<Instant> endOfLife = getEndOfLife(in.getEndOfLife(), in.getTimeZone(), now);
                    NoticeId notice = NoticeId.of(in.getNoticeId());

                    proxy.api().executeCommand(JControllerCommand.batch(map.getOrDefault(Boolean.TRUE, Collections.emptyList()).stream().map(
                            b -> JControllerCommand.postNotice(b, notice, endOfLife)).collect(Collectors.toList()))).thenAccept(e -> ProblemHelper
                                    .postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
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
            
            Map<String, Set<String>> expectedNotices = in.getExpectedNotices().stream().peek(en -> en.setNoticeBoardPath(JocInventory.pathToName(en
                    .getNoticeBoardPath()))).collect(Collectors.toMap(ExpectedNoticesPerBoard::getNoticeBoardPath, en -> en.getWorkflowPaths()
                            .stream().map(JocInventory::pathToName).collect(Collectors.toSet()), (k, v) -> k));
            expectedNotices.keySet().removeIf(key -> !boardPaths.contains(BoardPath.of(key)));
            
            if (!expectedNotices.isEmpty()) {
                Stream<ExpectingOrder> expectingOrdersStream = BoardHelper.getExpectingOrdersStream(proxy.currentState(), expectedNotices.keySet(),
                        folderPermissions.getListOfFolders());
                Predicate<ExpectingOrder> filterWorkflow = o -> {
                    Set<String> workflows = expectedNotices.getOrDefault(o.getBoardPath(), Collections.emptySet());
                    return workflows.isEmpty() || workflows.contains(o.getJOrder().workflowId().path().string());
                };
                proxy.api().executeCommand(JControllerCommand.batch(expectingOrdersStream.filter(filterWorkflow).map(o -> JControllerCommand
                        .postNotice(o.getBoard(), NoticeId.of(o.getNoticeId()), endOfLife)).distinct().collect(Collectors.toList()))).thenAccept(
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
    
    private Optional<JControllerCommand> getBatchCommand(Map<BoardPath, JGlobalBoardState> boards, ModifyNotices in, Action action) {
        return getBatchCommand(boards, in, Optional.empty(), action);
    }
    
    private Optional<JControllerCommand> getBatchCommand(Map<BoardPath, JGlobalBoardState> boards, ModifyNotices in, Optional<Instant> endOfLife,
            Action action) {
        Map<Boolean, List<BoardPath>> map = in.getNotices().stream()
                .map(NoticeIdsPerBoard::getNoticeBoardPath)
                .map(JocInventory::pathToName)
                .map(BoardPath::of)
                .collect(Collectors.groupingBy(b -> boards.containsKey(b)));
        map.putIfAbsent(Boolean.FALSE, Collections.emptyList());
        map.putIfAbsent(Boolean.TRUE, Collections.emptyList());

        if (!map.get(Boolean.FALSE).isEmpty()) {
            throw new ControllerObjectNotExistException("Controller '" + in.getControllerId() + "' couldn't find the Notice Boards " + map.get(
                    Boolean.FALSE).stream().map(BoardPath::string).collect(Collectors.joining("', '", "['", "']")));
        }
        if (!map.get(Boolean.TRUE).isEmpty()) {
            Predicate<NoticePlace> isPostedNotice = n -> JavaConverters.asJava(n.expectingOrderIds()).isEmpty();
            return Optional.of(JControllerCommand.batch(in.getNotices().stream()
                    .peek(notice -> notice.setNoticeBoardPath(JocInventory.pathToName(notice.getNoticeBoardPath())))
                    .filter(notice -> boards.containsKey(BoardPath.of(notice.getNoticeBoardPath())))
                    .flatMap(notice -> {
                        if (notice.getNoticeIds() == null || notice.getNoticeIds().isEmpty()) {
                            Stream<NoticePlace> nps = JavaConverters.asJava(boards.get(BoardPath.of(notice.getNoticeBoardPath())).asScala()
                                    .idToNotice()).values().stream();
                            switch (action) {
                            case DELETE: // only posted notices
                                nps = nps.filter(isPostedNotice);
                                break;
                            case POST: // only expected notices
                                nps = nps.filter(isPostedNotice.negate());
                                break;
                            }
                            return nps.map(NoticePlace::noticeId).map(n -> getActionCommand(notice.getNoticeBoardPath(), n, endOfLife, action));
                        } else {
                            return notice.getNoticeIds().stream().map(NoticeId::of).map(n -> getActionCommand(notice.getNoticeBoardPath(), n,
                                    endOfLife, action));
                        }
                    })
                    .collect(Collectors.toList())));
        }
        return Optional.empty();
    }
    
    private JControllerCommand getActionCommand(String boardName, NoticeId noticeId, Optional<Instant> endOfLife, Action action) {
        switch (action) {
        case DELETE:
            return JControllerCommand.apply(new ControllerCommand.DeleteNotice(BoardPath.of(boardName), noticeId));
        default: //case POST:
            return JControllerCommand.postNotice(BoardPath.of(boardName), noticeId, endOfLife);
        }
    }

}
