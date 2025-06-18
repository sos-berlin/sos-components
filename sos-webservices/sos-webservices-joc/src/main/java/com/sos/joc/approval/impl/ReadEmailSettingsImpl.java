package com.sos.joc.approval.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.impl.mail.Notifier;
import com.sos.joc.approval.resource.IReadEmailSettingsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.Path;

@Path("approval")
public class ReadEmailSettingsImpl extends JOCResourceImpl implements IReadEmailSettingsResource {

    private static final String API_CALL = "./approval/email_settings";

    @Override
    public JOCDefaultResponse postEmailSettings(String accessToken) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, "{}".getBytes(), accessToken, CategoryType.OTHERS);
            JOCDefaultResponse response = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(Notifier.readEmailSettings(session)));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}