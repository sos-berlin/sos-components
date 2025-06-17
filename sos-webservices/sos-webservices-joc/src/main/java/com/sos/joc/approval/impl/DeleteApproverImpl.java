package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IDeleteApproverResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.model.approval.DeleteApproverFilter;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class DeleteApproverImpl extends JOCResourceImpl implements IDeleteApproverResource {

    private static final String API_CALL = "./approval/approver/delete";

    @Override
    public JOCDefaultResponse postDelete(String xAccessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, xAccessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(filterBytes, DeleteApproverFilter.class);
            DeleteApproverFilter filter = Globals.objectMapper.readValue(filterBytes, DeleteApproverFilter.class);
            JOCDefaultResponse response = initManageAccountPermissions(xAccessToken);
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            DBItemJocApprover dbApprover = dbLayer.getApprover(filter.getAccountName());
            if(dbApprover != null) {
                session.delete(dbApprover);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}
