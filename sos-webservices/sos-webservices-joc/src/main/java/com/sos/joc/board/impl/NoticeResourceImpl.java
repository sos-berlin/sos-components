package com.sos.joc.board.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.joc.Globals;
import com.sos.joc.board.resource.INoticeResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.board.BoardHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.board.ModifyNotice;
import com.sos.joc.model.security.configuration.permissions.controller.NoticeBoards;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import jakarta.ws.rs.Path;
import js7.data.board.NoticeId;
import js7.data.controller.ControllerCommand;
import js7.data_for_java.controller.JControllerCommand;
import js7.proxy.javaapi.JControllerApi;

@Path("notice")
public class NoticeResourceImpl extends JOCResourceImpl implements INoticeResource {

    private static final String API_CALL = "./notice/";

    private enum Action {
        DELETE, POST
    }

    @Override
    public JOCDefaultResponse postNotice(String accessToken, byte[] filterBytes) {
        try {
            return modifyNotice(accessToken, filterBytes, Action.POST);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse deleteNotice(String accessToken, byte[] filterBytes) {
        try {
            return modifyNotice(accessToken, filterBytes, Action.DELETE);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse modifyNotice(String accessToken, byte[] filterBytes, Action action) throws JsonParseException,
            JsonMappingException, JocException, IOException, SOSJsonSchemaException, ExecutionException {

        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, ModifyNotice.class);
        ModifyNotice filter = Globals.objectMapper.readValue(filterBytes, ModifyNotice.class);
        String controllerId = filter.getControllerId();
        NoticeBoards nb = getControllerPermissions(controllerId, accessToken).getNoticeBoards();
        boolean permission = (action.equals(Action.DELETE)) ? nb.getDelete() : nb.getPost();
        JOCDefaultResponse response = initPermissions(controllerId, permission);
        if (response != null) {
            return response;
        }

        storeAuditLog(filter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

        JControllerApi controllerApi = ControllerApi.of(controllerId);
        //BoardPath board = BoardPath.of(JocInventory.pathToName(filter.getNoticeBoardPath()));
        NoticeId noticeId = BoardHelper.getNoticeId(filter.getNoticeId(), filter.getNoticeBoardPath());
        //NoticeKey.of(filter.getNoticeId()); //TODO relabel NoticeId -> Noticekey?
        Instant now = Instant.now();
        //JControllerProxy proxy = Proxy.of(controllerId);
        //scala.collection.JavaConverters.asJava(proxy.currentState().asScala().allNoticeIds());
        
        switch (action) {
        case DELETE:
            controllerApi.executeCommand(JControllerCommand.apply(new ControllerCommand.DeleteNotice(noticeId))).thenAccept(e -> ProblemHelper
                    .postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
            break;

        case POST:
            // JobSchedulerDate.getScheduledForInUTC(filter.getEndOfLife(), filter.getTimeZone())
            // JobSchedulerDate.getDateTo(filter.getEndOfLife(), filter.getTimeZone())
            Optional<Instant> endOfLife = Optional.empty();
            if (filter.getEndOfLife() != null && !filter.getEndOfLife().isEmpty()) {
                Instant endOfLifeInstant = JobSchedulerDate.getDateFrom(filter.getEndOfLife(), filter.getTimeZone()).toInstant();
                if (endOfLifeInstant.isAfter(now)) {
                    endOfLife = Optional.of(endOfLifeInstant);
                }
            }
            controllerApi.postNotice(noticeId, endOfLife).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(),
                    controllerId));
            break;
        }

        return JOCDefaultResponse.responseStatusJSOk(Date.from(now));
    }

}
