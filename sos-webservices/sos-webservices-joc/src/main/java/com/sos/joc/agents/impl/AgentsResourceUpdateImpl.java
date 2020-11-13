package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceUpdate;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.UpdateParameter;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentName;
import js7.data.agent.AgentRef;
import js7.proxy.javaapi.data.agent.JAgentRef;

@Path("agents")
public class AgentsResourceUpdateImpl extends JOCResourceImpl implements IAgentsResourceUpdate {

    private static String API_CALL = "./agents/update";

    @Override
    public JOCDefaultResponse update(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UpdateParameter.class);
            UpdateParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UpdateParameter.class);
            // TODO permissions
            boolean permission = getPermissonsJocCockpit(urlParameter.getControllerId(), accessToken).getJS7Controller().getExecute().isContinue();

            JOCDefaultResponse jocDefaultResponse = initPermissions(urlParameter.getControllerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            // TODO auditlog
            // ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            // logAuditMessage(jobschedulerAudit);

            List<JAgentRef> agentRefs = urlParameter.getAgentRefs().stream().map(a -> JAgentRef.apply(AgentRef.apply(AgentName.of(a.getName()), Uri
                    .of(a.getUri())))).collect(Collectors.toList());
            ControllerApi.of(urlParameter.getControllerId()).updateAgentRefs(agentRefs).thenAccept(e -> ProblemHelper
                    .postProblemEventIfExist(e, urlParameter.getControllerId()));

            // storeAuditLogEntry(jobschedulerAudit);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
