package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IRequestEditResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.foureyes.FourEyesRequestEdit;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class RequestEditImpl extends JOCResourceImpl implements IRequestEditResource {

    private static final String API_CALL = "./approval/edit";

    @Override
    public JOCDefaultResponse postEdit(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
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
            
            boolean updateDB = false;
            if (in.getApprover() != null && !in.getApprover().equals(item.getApprover())) {
                updateDB = true;
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
                item.setModified(now);
                session.update(item);
                
                // TODO send events if approver is changed
            }
            
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