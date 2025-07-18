package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.impl.mail.Notifier;
import com.sos.joc.approval.resource.IRequestEditResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.FourEyesRequestEdit;
import com.sos.joc.model.security.foureyes.RequestorState;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class RequestEditImpl extends JOCResourceImpl implements IRequestEditResource {

    private static final String API_CALL = "./approval/edit";

    @Override
    public JOCDefaultResponse postEdit(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(filterBytes, FourEyesRequestEdit.class);
            FourEyesRequestEdit in = Globals.objectMapper.readValue(filterBytes, FourEyesRequestEdit.class);
            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            
            DBItemJocApprovalRequest item = dbLayer.getApprovalRequest(in.getId());
            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            if (!item.getRequestor().equals(curAccountName)) {
                throw new JocBadRequestException("The current user is not the requestor of the approval request with id " + in.getId());
            }
            
            if (!ApproverState.PENDING.equals(item.getApproverStateAsEnum())) {
                throw new JocBadRequestException("The approval request is already " + item.getApproverStateAsEnum().value().toLowerCase());
            }
            
            if (!RequestorState.REQUESTED.equals(item.getRequestorStateAsEnum())) {
                throw new JocBadRequestException("The approval request is already " + item.getRequestorStateAsEnum().value().toLowerCase());
            }
            
            AuditParams ap = new AuditParams();
            ap.setComment(in.getTitle());
            storeAuditLog(ap);
            
            boolean updateDB = false;
            boolean approverIsChanged = false;
            if (in.getApprover() != null && !in.getApprover().equals(item.getApprover())) {
                updateDB = true;
                approverIsChanged = true;
                item.setApprover(in.getApprover());
            }
            if (in.getTitle() != null && !in.getTitle().equals(item.getTitle())) {
                updateDB = true;
                item.setTitle(in.getTitle());
            }
            if (in.getReason() != null && !in.getReason().equals(item.getComment())) {
                updateDB = true;
                item.setComment(in.getReason());
            }
            Date now = Date.from(Instant.now());
            if (updateDB) {
                item.setRequestorStateDate(now);
                session.update(item);
                
                if (approverIsChanged) {
                    EventBus.getInstance().post(new ApprovalUpdatedEvent(null, Collections.singletonMap(item.getApprover(), dbLayer
                            .getNumOfPendingApprovals(item.getApprover()))));
                    
                    Notifier.send(item, dbLayer, getJocError());
                } else {
                    EventBus.getInstance().post(new ApprovalUpdatedEvent());
                }
            }
            
            return responseStatusJSOk(now);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}