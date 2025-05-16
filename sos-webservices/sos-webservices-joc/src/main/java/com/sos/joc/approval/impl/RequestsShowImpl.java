package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IRequestsShowResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.foureyes.ApprovalRequest;
import com.sos.joc.model.security.foureyes.ApprovalRequests;
import com.sos.joc.model.security.foureyes.ApprovalsFilter;
import com.sos.joc.model.security.foureyes.Approver;
import com.sos.joc.model.security.foureyes.RequestBody;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class RequestsShowImpl extends JOCResourceImpl implements IRequestsShowResource {

    private static final String API_CALL = "./approval/requests";

    @Override
    public JOCDefaultResponse postRequests(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ApprovalsFilter.class);
            ApprovalsFilter in = Globals.objectMapper.readValue(filterBytes, ApprovalsFilter.class);
            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            
            boolean isApprover = jobschedulerUser.getSOSAuthCurrentAccount().isApprover();
            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            
            // every Approver can see all approval requests
            // if the user is not an approver then he sees only its own approval requests
            if (!isApprover) {
                in.setRequestors(Collections.singleton(curAccountName));
            }

            List<DBItemJocApprovalRequest> items = dbLayer.getApprovalRequests(in);
            Map<String, Approver> approvers = items.isEmpty() ? Collections.emptyMap() : dbLayer.getApprovers().stream().map(
                    DBItemJocApprover::mapToApproverWithoutEmail).collect(Collectors.toMap(Approver::getAccountName, Function.identity(), (s1,
                            s2) -> s1));
            Set<String> usedApprovers = new HashSet<>();
            
            Function<DBItemJocApprovalRequest, ApprovalRequest> mapper = i -> {
                usedApprovers.add(i.getApprover());
                ApprovalRequest r = new ApprovalRequest();
                r.setUnknownApprover(!approvers.containsKey(i.getApprover()));
                r.setApprover(i.getApprover());
                r.setCategory(i.getCategoryTypeAsEnum());
                r.setId(i.getId());
                r.setReason(i.getComment());
                if (i.getParameters() != null) {
                    try {
                        r.setRequestBody(Globals.objectMapper.readValue(i.getParameters(), RequestBody.class));
                    } catch (Exception e) {
                        r.setRequestBody(null);
                    }
                } else {
                    r.setRequestBody(null);
                }
                r.setRequestor(i.getRequestor());
                r.setRequestUrl(i.getRequest());
                r.setApproverState(i.getApproverStateAsEnum());
                r.setRequestorState(i.getRequestorStateAsEnum());
                r.setModified(i.getModified());
                r.setCreated(i.getCreated());
                return r;
            };
            
            ApprovalRequests entity = new ApprovalRequests();
            entity.setRequests(items.stream().map(mapper).toList());
            
            if (!entity.getRequests().isEmpty()) {
                //approvers.keySet().removeIf(key -> !usedApprovers.contains(key));
                entity.setApprovers(approvers.values());
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}