package com.sos.joc.board.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.board.common.BoardHelper;
import com.sos.joc.board.resource.IBoardResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.Board;
import com.sos.joc.model.board.BoardFilter;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;

@Path("notice")
public class BoardResourceImpl extends JOCResourceImpl implements IBoardResource {

    private static final String API_CALL = "./notice/board";

    @Override
    public JOCDefaultResponse postBoard(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, BoardFilter.class);
            BoardFilter filter = Globals.objectMapper.readValue(filterBytes, BoardFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getControllerPermissions(filter.getControllerId(), accessToken)
                    .getNoticeBoards().getView());
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
            answer.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = BoardHelper.getCurrentState(filter.getControllerId());
            long surveyDateMillis = Instant.now().toEpochMilli();
            if (currentstate != null) {
                Instant surveyInstant = currentstate.instant();
                answer.setSurveyDate(Date.from(surveyInstant));
                surveyDateMillis = surveyInstant.toEpochMilli();
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DeployedContent dc = dbLayer.getDeployedInventory(filter.getControllerId(), DeployType.NOTICEBOARD.intValue(), filter.getNoticeBoardPath());
            Globals.disconnect(session);
            session = null;
            
            if (dc == null || dc.getContent() == null || dc.getContent().isEmpty()) {
                throw new DBMissingDataException(String.format("Notice board '%s' doesn't exist", filter.getNoticeBoardPath()));
            }
            checkFolderPermissions(dc.getPath());
            
            if (filter.getCompact() == Boolean.TRUE) {
                ConcurrentMap<String, Integer> numOfExpectings = BoardHelper.getNumOfExpectingOrders(currentstate, Collections.singleton(dc
                        .getName()), folderPermissions.getListOfFolders()).getOrDefault(dc.getName(), new ConcurrentHashMap<>());
                answer.setNoticeBoard(BoardHelper.getCompactBoard(currentstate, dc, numOfExpectings));
            } else {
                Integer limit = filter.getLimit() != null ? filter.getLimit() : 10000;
                ConcurrentMap<String, List<JOrder>> expectings = BoardHelper.getExpectingOrders(currentstate, Collections.singleton(dc
                        .getName()), folderPermissions.getListOfFolders()).getOrDefault(dc.getName(), new ConcurrentHashMap<>());
                answer.setNoticeBoard(BoardHelper.getBoard(currentstate, dc, expectings, limit, surveyDateMillis));
            }
            answer.setDeliveryDate(Date.from(Instant.now()));
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
