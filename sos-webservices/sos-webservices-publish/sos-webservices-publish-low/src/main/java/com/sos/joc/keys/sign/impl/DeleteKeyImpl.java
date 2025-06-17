package com.sos.joc.keys.sign.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.keys.sign.resource.IDeleteKey;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.DeleteKeyFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("profile/key")
public class DeleteKeyImpl extends JOCResourceImpl implements IDeleteKey {

    private static final String API_CALL = "./profile/key/delete";

    @Override
    public JOCDefaultResponse postDeleteKey(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(filter, DeleteKeyFilter.class);
            DeleteKeyFilter deleteKeyFilter = Globals.objectMapper.readValue(filter, DeleteKeyFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(deleteKeyFilter.getAuditLog());
            String account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            DBItemDepKeys signatureKeyPair = dbLayerKeys.getDbItemDepKeys(account, JocSecurityLevel.LOW);
            if (signatureKeyPair != null) {
                hibernateSession.delete(signatureKeyPair);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
