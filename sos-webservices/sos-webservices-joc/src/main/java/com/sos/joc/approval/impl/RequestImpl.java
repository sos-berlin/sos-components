package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IRequestResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.FourEyesRequest;
import com.sos.joc.model.security.foureyes.RequestorState;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class RequestImpl extends JOCResourceImpl implements IRequestResource {

    private static final String API_CALL = "./approval/request";

    @Override
    public JOCDefaultResponse postRequest(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, FourEyesRequest.class);
            FourEyesRequest in = Globals.objectMapper.readValue(filterBytes, FourEyesRequest.class);
            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            
            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            String accountNameFromRequest = in.getRequestor();
            if (!accountNameFromRequest.equals(curAccountName)) {
                throw new JocBadRequestException("The current user is not the requestor of the approval request");
            }
            
            Date now = Date.from(Instant.now());
            DBItemJocApprovalRequest item = new DBItemJocApprovalRequest();
            item.setId(null);
            item.setApprover(in.getApprover());
            item.setApproverState(ApproverState.PENDING.intValue());
            item.setCategory(in.getCategory().intValue());
            item.setComment(in.getReason());
            item.setCreated(now);
            item.setModified(now);
            item.setParameters(Globals.objectMapper.writeValueAsString(in.getRequestBody()));
            item.setRequest(in.getRequestUrl());
            item.setRequestor(curAccountName);
            item.setRequestorState(RequestorState.REQUESTED.intValue());
            item.setTitle(in.getTitle());
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            session.save(item);
            
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            EventBus.getInstance().post(new ApprovalUpdatedEvent(item.getApprover(), dbLayer.getNumOfPendingApprovals(item.getApprover())));

            return JOCDefaultResponse.responseStatusJSOk(now);
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