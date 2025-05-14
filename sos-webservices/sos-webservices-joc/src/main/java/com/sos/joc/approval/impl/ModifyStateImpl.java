package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IModifyStateResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.FourEyesRequestId;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class ModifyStateImpl extends JOCResourceImpl implements IModifyStateResource {

    private static final String API_CALL = "./approval/";
    private enum Action {
        APPROVE, REJECT, WITHDRAW
    }

    @Override
    public JOCDefaultResponse postApprove(String accessToken, byte[] filterBytes) {
        return postApproverState(Action.APPROVE, accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse postReject(String accessToken, byte[] filterBytes) {
        return postApproverState(Action.REJECT, accessToken, filterBytes);
    }
    
    private JOCDefaultResponse postApproverState(Action action, String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JOCDefaultResponse response = init(action, accessToken, filterBytes);
            if (response != null) {
                return response;
            }
            if (!jobschedulerUser.getSOSAuthCurrentAccount().isApprover()) {
                throw new JocAccessDeniedException("The current user is not an approver.");
            }
            
            FourEyesRequestId in = Globals.objectMapper.readValue(filterBytes, FourEyesRequestId.class);
            session = Globals.createSosHibernateStatelessConnection(API_CALL + action.name().toLowerCase());
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            
            DBItemJocApprovalRequest item = dbLayer.getApprovalRequest(in.getId());
            if (item == null) {
                throw new DBMissingDataException("Couldn't find approval request with id " + in.getId());
            }
            
            ApproverState newState = action.equals(Action.APPROVE) ? ApproverState.APPROVED : ApproverState.REJECTED;

            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            if (item.getApprover().equals(curAccountName)) {
                dbLayer.updateApproverStatus(item.getId(), newState);
            } else {
                dbLayer.updateApproverStatus(item.getId(), newState, curAccountName);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse init(Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, FourEyesRequestId.class);
        return initPermissions(null, true);
    }

}