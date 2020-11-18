package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceStore;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.ModifyAgentClusterAudit;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.StoreAgents;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentName;
import js7.data.agent.AgentRef;
import js7.proxy.javaapi.data.agent.JAgentRef;

@Path("agents")
public class AgentsResourceStoreImpl extends JOCResourceImpl implements IAgentsResourceStore {

    private static String API_CALL = "./agents/store";

    @Override
    public JOCDefaultResponse update(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, StoreAgents.class);
            StoreAgents agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreAgents.class);
            // TODO permissions
            boolean permission = true; //getPermissonsJocCockpit(agentStoreParameter.getControllerId(), accessToken).getJS7Controller().getExecute().isContinue();

            JOCDefaultResponse jocDefaultResponse = initPermissions(agentStoreParameter.getControllerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(agentStoreParameter.getAuditLog());
            ModifyAgentClusterAudit jobschedulerAudit = new ModifyAgentClusterAudit(agentStoreParameter);
            logAuditMessage(jobschedulerAudit);
            
            // check at least one Agent is watcher for a controller cluster
            // check AgentId must be java variable conform

            List<JAgentRef> agentRefs = agentStoreParameter.getAgents().stream().map(a -> JAgentRef.apply(AgentRef.apply(AgentName.of(a.getAgentId()),
                    Uri.of(a.getUrl())))).collect(Collectors.toList());
            ControllerApi.of(agentStoreParameter.getControllerId()).updateAgentRefs(agentRefs).thenAccept(e -> ProblemHelper.postProblemEventIfExist(
                    e, getJocError(), agentStoreParameter.getControllerId()));

            storeAuditLogEntry(jobschedulerAudit);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
