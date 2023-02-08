package com.sos.joc.board.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.board.resource.INoticesModify;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.board.ModifyNotices;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.board.BoardPath;
import js7.data.board.NoticeId;
import js7.data.controller.ControllerCommand;
import js7.data_for_java.controller.JControllerCommand;
import js7.proxy.javaapi.JControllerProxy;

@Path("notices")
public class NoticesModifyImpl extends JOCResourceImpl implements INoticesModify {

    private static final String API_CALL = "./notices/delete";

    @Override
    public JOCDefaultResponse deleteNotices(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
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

}
