package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.agent.impl.AgentCommandResourceImpl;
import com.sos.joc.agents.resource.IAgentsReset;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeployAgents;
import com.sos.joc.model.agent.ResetAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerCommand;
import js7.proxy.javaapi.JControllerApi;

@Path("agents")
public class AgentsResetImpl extends JOCResourceImpl implements IAgentsReset {

    private static final String API_CALL = "./agents/reset";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsResetImpl.class);

    @Override
    public JOCDefaultResponse reset(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeployAgents.class);
            ResetAgents agentParameter = Globals.objectMapper.readValue(filterBytes, ResetAgents.class);
            
            String controllerId = agentParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerApi api = ControllerApi.of(controllerId);
            storeAuditLog(agentParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            // TODO Batch command 
//            getResetCommands(agentParameter.getAgentIds(), agentParameter.getForce() == Boolean.TRUE).forEach(c -> {
//                LOGGER.debug("Reset Agent: " + c.toJson());
//                api.executeCommand(c).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(),
//                        controllerId));
//            });
            
            List<JControllerCommand> commands = getResetCommands(agentParameter.getAgentIds(), agentParameter.getForce() == Boolean.TRUE)
                    .collect(Collectors.toList());
            if (LOGGER.isDebugEnabled()) {
                commands.forEach(c -> LOGGER.debug("Reset Agent: " + c.toJson()));
            }
            api.executeCommand(JControllerCommand.batch(commands)).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(),
                    getJocError(), controllerId));

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static Stream<JControllerCommand> getResetCommands(Collection<String> agentIds, boolean force) {
        return agentIds.stream().distinct().map(agentId -> AgentCommandResourceImpl.getResetCommand(agentId, force)).map(JControllerCommand::apply);
    }
}
