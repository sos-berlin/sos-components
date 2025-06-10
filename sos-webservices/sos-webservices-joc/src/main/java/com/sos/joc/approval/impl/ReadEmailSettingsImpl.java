package com.sos.joc.approval.impl;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.approval.impl.mail.Notifier;
import com.sos.joc.approval.resource.IReadEmailSettingsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.Path;

@Path("approval")
public class ReadEmailSettingsImpl extends JOCResourceImpl implements IReadEmailSettingsResource {

    private static final String API_CALL = "./approval/email_settings";

    @Override
    public JOCDefaultResponse postEmailSettings(String accessToken) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, null, accessToken, CategoryType.OTHERS);
            JOCDefaultResponse response = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(Notifier.readEmailSettings(session)));
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