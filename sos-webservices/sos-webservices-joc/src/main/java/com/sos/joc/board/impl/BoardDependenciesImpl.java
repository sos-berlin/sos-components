package com.sos.joc.board.impl;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowIdAndTags;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.board.resource.IBoardDependencies;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.board.BoardDeps;
import com.sos.joc.model.board.BoardFilter;
import com.sos.joc.model.board.BoardPathFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notice")
public class BoardDependenciesImpl extends JOCResourceImpl implements IBoardDependencies {

    private static final String API_CALL = "./notice/board/dependencies";

    @Override
    public JOCDefaultResponse postBoardDeps(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, BoardFilter.class);
            BoardPathFilter filter = Globals.objectMapper.readValue(filterBytes, BoardPathFilter.class);
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
    
    private BoardDeps getBoard(BoardPathFilter filter) throws Exception {
        SOSHibernateSession session = null;
        try {
            String controllerId = filter.getControllerId();
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DeployedContent dc = dbLayer.getDeployedInventory(controllerId, DeployType.NOTICEBOARD.intValue(), filter
                    .getNoticeBoardPath());

            if (dc == null || dc.getContent() == null || dc.getContent().isEmpty()) {
                throw new DBMissingDataException(String.format("Notice board '%s' doesn't exist", filter.getNoticeBoardPath()));
            }
            checkFolderPermissions(dc.getPath());

            com.sos.controller.model.board.BoardDeps board = Globals.objectMapper.readValue(dc.getContent(),
                    com.sos.controller.model.board.BoardDeps.class);
            board.setTYPE(null);
            board.setPath(dc.getPath());
            board.setVersionDate(dc.getCreated());
            board.setVersion(null);
            
            List<WorkflowIdAndTags> wcIds = dbLayer.getUsedWorkflowsByConsumeNoticeBoard(dc.getName(), controllerId);
            List<WorkflowIdAndTags> weIds = dbLayer.getUsedWorkflowsByExpectedNoticeBoard(dc.getName(), controllerId);
            List<WorkflowIdAndTags> wpIds = dbLayer.getUsedWorkflowsByPostNoticeBoard(dc.getName(), controllerId);
            
            if (WorkflowsHelper.withWorkflowTagsDisplayed()) {
                Map<String, LinkedHashSet<String>> wTags = WorkflowsHelper.getMapOfTagsPerWorkflow(session, Stream.concat(Stream.concat(
                        getWorkflowNamesStream(wcIds), getWorkflowNamesStream(weIds)), getWorkflowNamesStream(wpIds)));
                wcIds = addTags(wcIds, wTags);
                weIds = addTags(weIds, wTags);
                wpIds = addTags(wpIds, wTags);
            }
            
            board.setConsumingWorkflows(wcIds);
            board.setExpectingWorkflows(weIds);
            board.setPostingWorkflows(wpIds);

            BoardDeps answer = new BoardDeps();
            answer.setNoticeBoard(board);
            answer.setDeliveryDate(Date.from(Instant.now()));
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Stream<String> getWorkflowNamesStream(List<WorkflowIdAndTags> wIds) {
        return wIds.stream().map(WorkflowIdAndTags::getPath).map(JocInventory::pathToName);
    }
    
    private List<WorkflowIdAndTags> addTags(List<WorkflowIdAndTags> wIds, Map<String, LinkedHashSet<String>> wTags) {
        return wIds.stream().peek(w -> w.setWorkflowTags(wTags.get(JocInventory.pathToName(w.getPath())))).collect(Collectors
                .toList());
    }

}
