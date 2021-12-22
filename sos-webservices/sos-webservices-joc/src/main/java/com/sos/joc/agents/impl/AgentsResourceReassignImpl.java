package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceReassign;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentRef;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsResourceReassignImpl extends JOCResourceImpl implements IAgentsResourceReassign {

    private static final String API_CALL = "./agents/reassign";

    @Override
    public JOCDefaultResponse reAssign(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter body = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = body.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(body.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            Map<JAgentRef, List<JSubagentRef>> agents = Proxies.getAgents(controllerId, null);
            if (!agents.isEmpty()) {
                Stream<JUpdateItemOperation> a = agents.keySet().stream().map(JUpdateItemOperation::addOrChangeSimple);
                Stream<JUpdateItemOperation> s = agents.values().stream().flatMap(l -> l.stream().map(JUpdateItemOperation::addOrChangeSimple));
                ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromStream(a), Flux.fromStream(s)))
                    .thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
