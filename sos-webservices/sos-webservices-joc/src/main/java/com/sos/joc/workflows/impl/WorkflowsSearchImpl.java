package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.impl.SearchResourceImpl;
import com.sos.joc.inventory.resource.ISearchResource;
import com.sos.joc.model.inventory.search.RequestSearchReturnType;
import com.sos.joc.model.inventory.search.ResponseSearch;
import com.sos.joc.model.inventory.search.ResponseSearchItem;
import com.sos.joc.model.workflow.search.DeployedWorkflowSearchFilter;
import com.sos.joc.model.workflow.search.InstructionStateText;
import com.sos.joc.model.workflow.search.WorkflowSearchFilter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import scala.collection.JavaConverters;

@Path("workflows")
public class WorkflowsSearchImpl extends JOCResourceImpl implements ISearchResource {

    private static final String API_CALL = "./workflows/search";

    @Override
    public JOCDefaultResponse postSearch(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowSearchFilter.class);
            DeployedWorkflowSearchFilter in = Globals.objectMapper.readValue(filterBytes, DeployedWorkflowSearchFilter.class);
            in.setReturnType(RequestSearchReturnType.WORKFLOW);
            in.setDeployedOrReleased(true);

            boolean permission = getBasicControllerPermissions(in.getControllerId(), accessToken).getWorkflows().getView();
            JOCDefaultResponse response = initPermissions(in.getControllerId(), permission);
            if (response != null) {
                return response;
            }
            if (in.getFolders() != null) {
                for (String folder : in.getFolders()) {
                    if (!folderPermissions.isPermittedForFolder(folder)) {
                        throw new JocFolderPermissionsException(folder);
                    }
                }
            }
            
            ResponseSearch answer = new ResponseSearch();
            answer.setResults(SearchResourceImpl.getSearchResult(in, folderPermissions));
            
            if (in.getStates() != null) {
                in.getStates().remove(SyncStateText.UNKNOWN);
                in.getStates().remove(SyncStateText.NOT_DEPLOYED);
            }
            
            boolean withStatesFilter = in.getStates() != null && !in.getStates().isEmpty();
            boolean withStoppedInstructionStatesFilter = in.getInstructionStates() != null && in.getInstructionStates().contains(
                    InstructionStateText.STOPPED);
            boolean withSkippedInstructionStatesFilter = in.getInstructionStates() != null && in.getInstructionStates().contains(
                    InstructionStateText.SKIPPED);
            
            if (withStatesFilter || withStoppedInstructionStatesFilter || withSkippedInstructionStatesFilter) {
                JControllerState currentState = Proxy.of(in.getControllerId()).currentState();
                Predicate<ResponseSearchItem> filter = item -> filterStates(item.getName(), withStatesFilter, in.getStates(),
                        withStoppedInstructionStatesFilter, withSkippedInstructionStatesFilter, currentState);
                answer.setResults(answer.getResults().stream().filter(filter).collect(Collectors.toList()));
            }
            
            answer.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static boolean filterStates(String workflowName, boolean withStatesFilter, List<SyncStateText> statesFilter,
            boolean withStoppedInstructionStatesFilter, boolean withSkippedInstructionStatesFilter, JControllerState currentstate) {

        boolean hasStoppedOrSkippedInstructions = true;
        boolean hasState = true;
        
        if (withStatesFilter || withStoppedInstructionStatesFilter || withSkippedInstructionStatesFilter) {

            SyncStateText stateText = SyncStateText.NOT_IN_SYNC;
            Either<Problem, JWorkflow> either = currentstate.repo().pathToCheckedWorkflow(WorkflowPath.of(workflowName));
            if (either != null && either.isRight()) {
                boolean hasStoppedInstructions = false;
                boolean hasSkippedInstructions = false;
                
                JWorkflowId wId = either.get().id();
                if (withStoppedInstructionStatesFilter) {
                    Optional<Boolean> hasStoppedPositions = WorkflowsHelper.getWorkflowControl(currentstate, wId, false).map(c -> c.breakpoints())
                            .map(c -> !c.isEmpty());
                    hasStoppedInstructions = hasStoppedPositions.isPresent() && hasStoppedPositions.get();
                }
                if (withStatesFilter || withSkippedInstructionStatesFilter) {
                    stateText = SyncStateText.IN_SYNC;
                    Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(currentstate, wId.path(), false);
                    if (controlState.isPresent()) {
                        if (withSkippedInstructionStatesFilter) {
                            hasSkippedInstructions = !JavaConverters.asJava(controlState.get().skip()).isEmpty();
                        }
                        if (withStatesFilter) {
                            Set<AgentPath> agentsThatIgnoreCommand = currentstate.workflowPathControlToIgnorantAgent().getOrDefault(wId.path(), Collections
                                    .emptySet());
                            int numOfgentsThatIgnoreCommand = agentsThatIgnoreCommand.size();
                            if (numOfgentsThatIgnoreCommand > 0) {
                                stateText = SyncStateText.OUTSTANDING;
                            } else if (controlState.get().suspended()) {
                                stateText = SyncStateText.SUSPENDED;
                            }
                        }
                    }
                }
                if (withStoppedInstructionStatesFilter || withSkippedInstructionStatesFilter) {
                    hasStoppedOrSkippedInstructions = hasStoppedInstructions || hasSkippedInstructions;
                }
            } else {
                if (withStoppedInstructionStatesFilter || withSkippedInstructionStatesFilter) {
                    hasStoppedOrSkippedInstructions = false;
                }
            }
            if (withStatesFilter) {
                hasState = statesFilter.contains(stateText);
            }
        }
        return hasState && hasStoppedOrSkippedInstructions;
    }

}