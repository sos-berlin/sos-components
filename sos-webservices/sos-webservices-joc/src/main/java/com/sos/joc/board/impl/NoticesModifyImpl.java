package com.sos.joc.board.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.Globals;
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
import com.sos.joc.model.board.ModifyNotices;
import com.sos.joc.model.board.PostNotices;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.board.NoticeId;
import js7.data.controller.ControllerCommand;
import js7.data_for_java.controller.JControllerCommand;
import js7.proxy.javaapi.JControllerProxy;

@Path("notices")
public class NoticesModifyImpl extends JOCResourceImpl implements INoticesModify {

    private static final String API_CALL_DELETE = "./notices/delete";
    private static final String API_CALL_POST = "./notices/post";

    @Override
    public JOCDefaultResponse deleteNotices(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ModifyNotices.class);
            ModifyNotices filter = Globals.objectMapper.readValue(filterBytes, ModifyNotices.class);
            String controllerId = filter.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getDelete());
            if (response != null) {
                return response;
            }

            storeAuditLog(filter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            JControllerProxy proxy = Proxy.of(controllerId);
            final BoardPath board = BoardPath.of(JocInventory.pathToName(filter.getNoticeBoardPath()));
            if (proxy.currentState().pathToBoard().get(board) == null) {
                throw new ControllerObjectNotExistException("Controller '" + controllerId + "' couldn't find the Notice Board '" + board.string() + "'");
            }
            // TODO Batch command
            filter.getNoticeIds().stream().map(NoticeId::of).map(n -> new ControllerCommand.DeleteNotice(board, n)).map(JControllerCommand::apply)
                    .forEach(command -> proxy.api().executeCommand(command).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken,
                            getJocError(), controllerId)));

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
            initLogging(API_CALL_POST, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, PostNotices.class);
            PostNotices in = Globals.objectMapper.readValue(filterBytes, PostNotices.class);
            String controllerId = in.getControllerId();
            JOCDefaultResponse response = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getNoticeBoards()
                    .getPost());
            if (response != null) {
                return response;
            }

            storeAuditLog(in.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            JControllerProxy proxy = Proxy.of(controllerId);
            Set<BoardPath> boardPaths = proxy.currentState().pathToBoard().keySet();
            
            Map<Boolean, List<BoardPath>> map = in.getNoticeBoardPaths().stream().map(JocInventory::pathToName).map(BoardPath::of).collect(Collectors
                    .groupingBy(b -> boardPaths.contains(b)));
            map.putIfAbsent(Boolean.FALSE, Collections.emptyList());
            
            if (!map.get(Boolean.FALSE).isEmpty()) {
                throw new ControllerObjectNotExistException("Controller '" + controllerId + "' couldn't find the Notice Boards " + map.get(
                        Boolean.FALSE).stream().map(BoardPath::string).collect(Collectors.joining("', '", "['", "']")));
            }
            
            Instant now = Instant.now();
            Optional<Instant> endOfLife = getEndOfLife(in.getEndOfLife(), in.getTimeZone(), now);
            NoticeId notice = NoticeId.of(in.getNoticeId());

            // TODO Batch command
            map.getOrDefault(Boolean.TRUE, Collections.emptyList()).stream().forEach(b -> proxy.api().postNotice(b, notice, endOfLife).thenAccept(
                    e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId)));

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
            Instant endOfLifeInstant = JobSchedulerDate.getDateFrom(endOfLife, timezone).toInstant();
            if (endOfLifeInstant.isAfter(now)) {
                endOfLifeOpt = Optional.of(endOfLifeInstant);
            }
        }
        return endOfLifeOpt;
    }

}
