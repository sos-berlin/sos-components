package com.sos.joc.keys.auth.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.keys.auth.resource.IOnetimeToken;
import com.sos.joc.keys.auth.token.OnetimeTokens;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.auth.CreateOnetimeTokenFilter;
import com.sos.joc.model.auth.ShowOnetimeTokenFilter;
import com.sos.joc.model.auth.token.OnetimeToken;
import com.sos.joc.model.auth.token.OnetimeTokensResponse;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path("token")
public class OnetimeTokenImpl extends JOCResourceImpl implements IOnetimeToken {

    private static final String API_CALL_SHOW = "./token/show";
    private static final String API_CALL_CREATE = "./token/create";
    
    @Override
    public JOCDefaultResponse postCreateToken(String xAccessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL_CREATE, filter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validate(filter, CreateOnetimeTokenFilter.class);
            CreateOnetimeTokenFilter createOnetimeTokenFilter = Globals.objectMapper.readValue(filter, CreateOnetimeTokenFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(createOnetimeTokenFilter.getAuditLog());
            String controllerId = createOnetimeTokenFilter.getControllerId();
            List<String> agentIds = createOnetimeTokenFilter.getAgentIds();
            Date validUntil = JobSchedulerDate.getDateFrom(createOnetimeTokenFilter.getValidUntil(), createOnetimeTokenFilter.getTimezone());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_CREATE);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(hibernateSession);
            List<DBItemInventoryJSInstance> controllerClusterInstances = dbLayer.getInventoryInstancesByControllerId(controllerId);
            
            OnetimeTokensResponse response = new OnetimeTokensResponse();
            OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
            // delete invalidated tokens
            ClientServerCertificateUtil.cleanupInvalidatedTokens();
            if(controllerId != null && !controllerId.isEmpty()) {
                // first delete existing token(s)
                List<OnetimeToken> tokensToDelete = onetimeTokens.getTokens().stream().filter(token -> controllerId.equals(token.getControllerId())).collect(Collectors.toList());
                onetimeTokens.getTokens().removeAll(tokensToDelete);
                // then create new token
                if (controllerClusterInstances != null && controllerClusterInstances.size() > 1) {
                    for (DBItemInventoryJSInstance member : controllerClusterInstances) {
                        OnetimeToken token = new OnetimeToken();
                        token.setValidUntil(validUntil);
                        token.setControllerId(controllerId);
                        token.setUUID(UUID.randomUUID().toString());
                        token.setURI(member.getClusterUri());
                        onetimeTokens.getTokens().add(token);
                        response.getTokens().add(token);
                    }
                } else {
                    OnetimeToken token = new OnetimeToken();
                    token.setValidUntil(validUntil);
                    token.setControllerId(controllerId);
                    token.setUUID(UUID.randomUUID().toString());
                    onetimeTokens.getTokens().add(token);
                    response.getTokens().add(token);
                }
            }
            if (agentIds != null && !agentIds.isEmpty()) {
                // first delete existing token(s)
                List<OnetimeToken> tokensToDelete = onetimeTokens.getTokens().stream().filter(token -> agentIds.contains(token.getAgentId())).collect(Collectors.toList());
                onetimeTokens.getTokens().removeAll(tokensToDelete);
                // then create new tokens
                agentIds.stream().forEach(agentId -> {
                    OnetimeToken token = new OnetimeToken();
                    token.setValidUntil(validUntil);
                    token.setAgentId(agentId);
                    if(controllerId != null && !controllerId.isEmpty()) {
                        token.setControllerId(controllerId);
                    }
                    token.setUUID(UUID.randomUUID().toString());
                    onetimeTokens.getTokens().add(token);
                    response.getTokens().add(token);
                });
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postShowToken(String xAccessToken, byte[] filter) {
        try {
            filter = initLogging(API_CALL_SHOW, filter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(filter, ShowOnetimeTokenFilter.class);
            ShowOnetimeTokenFilter showOnetimeTokenFilter = Globals.objectMapper.readValue(filter, ShowOnetimeTokenFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getView()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String controllerId = showOnetimeTokenFilter.getControllerId();
            List<String> agentIds = showOnetimeTokenFilter.getAgentIds();
            ClientServerCertificateUtil.cleanupInvalidatedTokens();
            OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
            OnetimeTokensResponse response = new OnetimeTokensResponse();
            if (!onetimeTokens.getTokens().isEmpty() && controllerId == null && (agentIds == null || agentIds.isEmpty())) {
               response.setTokens(onetimeTokens.getTokens().stream().collect(Collectors.toList())); 
            } else {
                if (!onetimeTokens.getTokens().isEmpty() && controllerId != null && !controllerId.isEmpty()) {
                    OnetimeToken token = onetimeTokens.getTokens().stream().filter(item -> controllerId.equals(item.getControllerId())).collect(
                            Collectors.toList()).get(0);
                    if (token != null) {
                        response.getTokens().add(token);
                    }
                }
                if (!onetimeTokens.getTokens().isEmpty() && agentIds != null && !agentIds.isEmpty()) {
                    agentIds.stream().forEach(agentId -> {
                        OnetimeToken token = onetimeTokens.getTokens().stream().filter(item -> agentId.equals(item.getAgentId())).collect(Collectors
                                .toList()).get(0);
                        if (token != null) {
                            response.getTokens().add(token);
                        }
                    });
                }
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
