package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IStoreApproverResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.foureyes.Approver;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class StoreApproverImpl extends JOCResourceImpl implements IStoreApproverResource {

    private static final String API_CALL = "./approval/approver/store";

    @Override
    public JOCDefaultResponse postStore(String xAccessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, xAccessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(filterBytes, Approver.class);
            Approver filter = Globals.objectMapper.readValue(filterBytes, Approver.class);
            JOCDefaultResponse response = initManageAccountPermissions(xAccessToken);
            if (response != null) {
                return response;
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            DBItemJocApprover dbApprover = dbLayer.getApprover(filter.getAccountName());
            if(dbApprover != null) {
                dbApprover.setFirstName(filter.getFirstName());
                dbApprover.setLastName(filter.getLastName());
                dbApprover.setEmail(filter.getEmail());
                session.update(dbApprover);
            } else {
                dbApprover = new DBItemJocApprover();
                dbApprover.setAccountName(filter.getAccountName());
                dbApprover.setFirstName(filter.getFirstName());
                dbApprover.setLastName(filter.getLastName());
                dbApprover.setEmail(filter.getEmail());
                dbApprover.setOrdering(dbLayer.getMaxOrdering() + 1);
                session.save(dbApprover);
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

}
