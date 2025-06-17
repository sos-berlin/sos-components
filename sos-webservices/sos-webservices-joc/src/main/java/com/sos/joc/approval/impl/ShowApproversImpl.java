package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IShowApproversResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.foureyes.Approvers;

import jakarta.ws.rs.Path;

@Path("approval")
public class ShowApproversImpl extends JOCResourceImpl implements IShowApproversResource{

    private static final String API_CALL = "./approval/approvers";

    @Override
    public JOCDefaultResponse postShow(String xAccessToken) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, null, xAccessToken, CategoryType.OTHERS);
            JOCDefaultResponse response = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getAccounts().getView());
            if (response != null) {
                return response;
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            List<DBItemJocApprover> dbApprovers =  dbLayer.getApprovers();
            Approvers approvers = new Approvers();
            approvers.setApprovers(dbApprovers.stream().map(DBItemJocApprover::mapToApprover).toList());
            approvers.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(approvers));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}
