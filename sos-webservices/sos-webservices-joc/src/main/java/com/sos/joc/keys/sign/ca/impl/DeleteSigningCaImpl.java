package com.sos.joc.keys.sign.ca.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.keys.sign.resource.IDeleteSigningCa;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.DeleteCaFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("profile/key/ca")
public class DeleteSigningCaImpl extends JOCResourceImpl implements IDeleteSigningCa {

    private static final String API_CALL = "./profile/key/ca/delete";

    @Override
    public JOCDefaultResponse postDeleteSigningCa(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(filter, DeleteCaFilter.class);
            DeleteCaFilter deleteCaFilter = Globals.objectMapper.readValue(filter, DeleteCaFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(deleteCaFilter.getAuditLog());

            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            
            String accountName = "";
            if (JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                accountName = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            } else {
                accountName =  jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            }
            
            DBItemInventoryCertificate dbCert = dbLayerKeys.getSigningRootCaCertificate(accountName);
            if(dbCert != null) {
                hibernateSession.delete(dbCert);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
