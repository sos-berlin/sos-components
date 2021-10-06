package com.sos.joc.boards.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.board.common.BoardHelper;
import com.sos.joc.boards.resource.IBoardsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.Boards;
import com.sos.joc.model.board.BoardsFilter;
import com.sos.joc.model.common.Folder;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;

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
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(filter.getControllerId());
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
                dbFilter.setNames(paths.stream().map(p -> JocInventory.pathToName(p)).collect(Collectors.toSet()));
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
            final JControllerState controllerState = BoardHelper.getCurrentState(filter.getControllerId());
            if (controllerState != null) {
                answer.setSurveyDate(Date.from(controllerState.instant()));
            }
            final long surveyDateMillis = controllerState != null ? controllerState.instant().toEpochMilli() : Instant.now().toEpochMilli();
            
            JocError jocError = getJocError();
            if (contents != null) {
                Set<String> boardNames = contents.stream().map(DeployedContent::getName).collect(Collectors.toSet());
                if (filter.getCompact() == Boolean.TRUE) {
                    ConcurrentMap<String, ConcurrentMap<String, Integer>> numOfExpectings = BoardHelper.getNumOfExpectingOrders(controllerState,
                            boardNames, folderPermissions.getListOfFolders());
                    answer.setNoticeBoards(contents.stream().filter(dc -> canAdd(dc.getPath(), folders)).map(dc -> {
                        try {
                            if (dc.getContent() == null || dc.getContent().isEmpty()) {
                                throw new DBMissingDataException("doesn't exist");
                            }
                            return BoardHelper.getCompactBoard(controllerState, dc, numOfExpectings.getOrDefault(dc.getName(),
                                    new ConcurrentHashMap<>()));
                        } catch (Throwable e) {
                            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                                LOGGER.info(jocError.printMetaInfo());
                                jocError.clearMetaInfo();
                            }
                            LOGGER.error(String.format("[%s] %s", dc.getPath(), e.toString()));
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                } else {
                    Integer limit = filter.getLimit() != null ? filter.getLimit() : 10000;
                    ConcurrentMap<String, ConcurrentMap<String, List<JOrder>>> expectings = BoardHelper.getExpectingOrders(controllerState,
                            boardNames, folderPermissions.getListOfFolders());
                    answer.setNoticeBoards(contents.stream().filter(dc -> canAdd(dc.getPath(), folders)).map(dc -> {
                        try {
                            if (dc.getContent() == null || dc.getContent().isEmpty()) {
                                throw new DBMissingDataException("doesn't exist");
                            }
                            return BoardHelper.getBoard(controllerState, dc, expectings.getOrDefault(dc.getName(), new ConcurrentHashMap<>()), limit,
                                    surveyDateMillis);
                        } catch (Throwable e) {
                            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                                LOGGER.info(jocError.printMetaInfo());
                                jocError.clearMetaInfo();
                            }
                            LOGGER.error(String.format("[%s] %s", dc.getPath(), e.toString()));
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
            }
            answer.setDeliveryDate(now);
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }

    }

}