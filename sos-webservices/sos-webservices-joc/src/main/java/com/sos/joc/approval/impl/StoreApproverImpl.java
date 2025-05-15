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
import com.sos.joc.model.approval.StoreApproverFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval/approver")
public class StoreApproverImpl extends JOCResourceImpl implements IStoreApproverResource {

    private static final String API_CALL = "./approval/approver/store";

    @Override
    public JOCDefaultResponse postStore(String xAccessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, xAccessToken);
            JsonValidator.validateFailFast(filterBytes, StoreApproverFilter.class);
            StoreApproverFilter filter = Globals.objectMapper.readValue(filterBytes, StoreApproverFilter.class);
            JOCDefaultResponse response = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getAccounts().getManage(), false);
            if (response != null) {
                return response;
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            DBItemJocApprover dbApprover = dbLayer.getApprover(filter.getApprover().getAccountName());
            if(dbApprover != null) {
                dbApprover.setFirstName(filter.getApprover().getFirstName());
                dbApprover.setLastName(filter.getApprover().getLastName());
                dbApprover.setEmail(filter.getApprover().getEmail());
                session.update(dbApprover);
            } else {
                dbApprover = new DBItemJocApprover();
                dbApprover.setAccountName(filter.getApprover().getAccountName());
                dbApprover.setFirstName(filter.getApprover().getFirstName());
                dbApprover.setLastName(filter.getApprover().getLastName());
                dbApprover.setEmail(filter.getApprover().getEmail());
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
