package com.sos.joc.boards.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.board.BoardDeps;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.controller.model.workflow.WorkflowIdAndTags;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.boards.resource.IBoardsDependencies;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.deploy.items.WorkflowBoards;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.BoardsDeps;
import com.sos.joc.model.board.BoardsPathFilter;
import com.sos.joc.model.board.DepsPerBoard;
import com.sos.joc.model.common.Folder;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notice")
public class BoardsDependenciesImpl extends JOCResourceImpl implements IBoardsDependencies {

    private static final String API_CALL = "./notice/boards/dependencies";

    @Override
    public JOCDefaultResponse postBoardsDeps(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, BoardsPathFilter.class);
            BoardsPathFilter filter = Globals.objectMapper.readValue(filterBytes, BoardsPathFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getBasicControllerPermissions(filter.getControllerId(),
                    accessToken).getNoticeBoards().getView());
            if (response != null) {
                return response;
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getBoards(filter)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private BoardsDeps getBoards(BoardsPathFilter filter) throws Exception {
        SOSHibernateSession session = null;
        try {
            String controllerId = filter.getControllerId();
            Set<String> boardNames = filter.getNoticeBoardPaths().stream().map(JocInventory::pathToName).collect(Collectors.toSet());
            return getBoards(controllerId, boardNames, session);
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private BoardsDeps getBoards(String controllerId, Set<String> boardNames, SOSHibernateSession session) throws Exception {
        Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
        session = Globals.createSosHibernateStatelessConnection(API_CALL);
        
        BoardsDeps answer = new BoardsDeps();
        answer.setNoticeBoards(getDepsPerBoard(controllerId, boardNames, permittedFolders, session));
        answer.setDeliveryDate(Date.from(Instant.now()));
        return answer;
    }
    
    private static DepsPerBoard getDepsPerBoard(String controllerId, Set<String> boardNames, Set<Folder> permittedFolders,
            SOSHibernateSession session) {
        DeployedConfigurationFilter confFilter = new DeployedConfigurationFilter();
        confFilter.setControllerId(controllerId);
        confFilter.setObjectTypes(Collections.singleton(DeployType.NOTICEBOARD.intValue()));
        confFilter.setNames(boardNames);

        DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
        List<DeployedContent> dcs = dbLayer.getDeployedInventory(confFilter);
        
        List<WorkflowBoards> wbs = getWorkflowsWithBoards(controllerId, boardNames, dbLayer);

        return getDepsPerBoard(dcs.stream().filter(dc -> canAdd(dc.getPath(), permittedFolders)), wbs);
    }
    
    private static DepsPerBoard getDepsPerBoard(Stream<DeployedContent> dcs, List<WorkflowBoards> wbs) {
        DepsPerBoard dpb = new DepsPerBoard();
        dcs.map(dc -> {
            try {
                BoardDeps board = Globals.objectMapper.readValue(dc.getContent(), BoardDeps.class);
                board.setTYPE(null);
                board.setPath(dc.getPath());
                board.setVersionDate(dc.getCreated());
                board.setVersion(null);
                return withDeps(dc.getName(), board, wbs);
            } catch(Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).forEach(board -> {
            dpb.setAdditionalProperty(JocInventory.pathToName(board.getPath()), board);
        });
        return dpb;
    }
    
    public static List<WorkflowBoards> getWorkflowsWithBoards(String controllerId, Set<String> boardNames, DeployedConfigurationDBLayer dbLayer) {
        List<WorkflowBoards> wbs = dbLayer.getUsedWorkflowsByNoticeBoards(controllerId, boardNames);

        if (WorkflowsHelper.withWorkflowTagsDisplayed()) {
            Map<String, LinkedHashSet<String>> wTags = WorkflowsHelper.getMapOfTagsPerWorkflow(dbLayer.getSession(), getWorkflowNamesStream(wbs));
            wbs = addTags(wbs, wTags);
        }
        
        return wbs;
    }
    
    public static BoardDeps withDeps(String boardName, BoardDeps board, List<WorkflowBoards> wbs) {
        List<WorkflowIdAndTags> wcIds = new ArrayList<>();
        List<WorkflowIdAndTags> weIds = new ArrayList<>();
        List<WorkflowIdAndTags> wpIds = new ArrayList<>();
        for (WorkflowBoards wb : wbs) {
            WorkflowIdAndTags wId = new WorkflowIdAndTags(wb.getWorkflowTags(), wb.getPath(), wb.getVersionId());
            if (wb.hasConsumeNotice(boardName)) {
                wcIds.add(wId);
            }
            if (wb.hasExpectNotice(boardName)) {
                weIds.add(wId);
            }
            if (wb.hasPostNotice(boardName)) {
                wpIds.add(wId);
            }
        }
        if (board == null) {
            board = new BoardDeps();
        }
        board.setConsumingWorkflows(wcIds);
        board.setExpectingWorkflows(weIds);
        board.setPostingWorkflows(wpIds);
        return board;
    }
    
    private static <T extends WorkflowId> Stream<String> getWorkflowNamesStream(List<T> wIds) {
        return wIds.stream().map(T::getPath).map(JocInventory::pathToName);
    }
    
    private static <T extends WorkflowIdAndTags> List<T> addTags(List<T> wIds, Map<String, LinkedHashSet<String>> wTags) {
        return wIds.stream().peek(w -> w.setWorkflowTags(wTags.get(JocInventory.pathToName(w.getPath())))).collect(Collectors.toList());
    }

}
