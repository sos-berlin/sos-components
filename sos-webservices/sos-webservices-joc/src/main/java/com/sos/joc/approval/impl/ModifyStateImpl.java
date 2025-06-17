package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IModifyStateResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.FourEyesRequestId;
import com.sos.joc.model.security.foureyes.RequestorState;
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
    
    @Override
    public JOCDefaultResponse postWithdraw(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JOCDefaultResponse response = init(Action.WITHDRAW, accessToken, filterBytes);
            if (response != null) {
                return response;
            }
            
            FourEyesRequestId in = Globals.objectMapper.readValue(filterBytes, FourEyesRequestId.class);
            
            storeAuditLog(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL + Action.WITHDRAW.name().toLowerCase());
            session.setAutoCommit(false);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);

            DBItemJocApprovalRequest item = dbLayer.getApprovalRequest(in.getId());
            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            if (!item.getRequestor().equals(curAccountName)) {
                throw new JocBadRequestException("The current user is not the requestor of the approval request with id " + in.getId());
            }
            
            if (!RequestorState.REQUESTED.equals(item.getRequestorStateAsEnum())) {
                throw new JocBadRequestException("The approval request is already " + item.getRequestorStateAsEnum().value().toLowerCase().replace(
                        '_', ' '));
            }
            
            dbLayer.updateRequestorStatusInclusiveTransaction(item.getId(), RequestorState.WITHDRAWN);
            
            if (item.getApproverState().equals(ApproverState.PENDING.intValue())) {
                long numOfPendingApprovals = dbLayer.getNumOfPendingApprovals(item.getApprover());
                EventBus.getInstance().post(new ApprovalUpdatedEvent(null, Collections.singletonMap(item.getApprover(), numOfPendingApprovals)));
            } else {
                EventBus.getInstance().post(new ApprovalUpdatedEvent());
            }

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
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
            
            storeAuditLog(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL + action.name().toLowerCase());
            session.setAutoCommit(false);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            
            DBItemJocApprovalRequest item = dbLayer.getApprovalRequest(in.getId());
            ApproverState prevApproverState = item.getApproverStateAsEnum();
            RequestorState requestorState = item.getRequestorStateAsEnum();
            
            switch (requestorState) {
            case EXECUTED:
                throw new JocBadRequestException("The approval request has already been used by the requestor");
            case WITHDRAWN:
                // throw new JocBadRequestException("The approval request is already withdrawn by the requestor");
            default: //REQUESTED
                break;
            }
            
            ApproverState newState = action.equals(Action.APPROVE) ? ApproverState.APPROVED : ApproverState.REJECTED;
            
            if (newState.equals(prevApproverState)) {
                switch(prevApproverState) {
                case APPROVED:
                    throw new JocBadRequestException("The approval request is already approved");
                case REJECTED:
                    throw new JocBadRequestException("The approval request is already rejected");
                default: //PENDING never reached
                    break;
                }
            }

            Map<String, Long> approversForEvents = new HashMap<>();
            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            if (item.getApprover().equals(curAccountName)) {
                dbLayer.updateApproverStatusInclusiveTransaction(item.getId(), newState);
            } else {
                // with take over
                String prevApprover = item.getApprover();
                dbLayer.updateApproverStatusInclusiveTransaction(item.getId(), newState, curAccountName);
                approversForEvents.put(prevApprover, dbLayer.getNumOfPendingApprovals(prevApprover));
            }
            approversForEvents.put(item.getApprover(), dbLayer.getNumOfPendingApprovals(item.getApprover()));
            EventBus.getInstance().post(new ApprovalUpdatedEvent(Collections.singletonMap(item.getRequestor(), dbLayer
                    .getNumOfApprovedRejectedRequests(item.getRequestor())), approversForEvents));

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse init(Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken, CategoryType.OTHERS);
        JsonValidator.validateFailFast(filterBytes, FourEyesRequestId.class);
        return initPermissions(null, true);
    }

}