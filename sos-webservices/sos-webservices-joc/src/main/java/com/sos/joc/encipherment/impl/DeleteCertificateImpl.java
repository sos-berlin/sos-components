package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IDeleteCertificate;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.DeleteCertificateRequestFilter;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/certificate")
public class DeleteCertificateImpl extends JOCResourceImpl implements IDeleteCertificate {

    private static final String API_CALL = "./encipherment/certificate/delete";

    @Override
    public JOCDefaultResponse postDeleteCertificate(String xAccessToken, byte[] deleteCertificateFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            deleteCertificateFilter = initLogging(API_CALL, deleteCertificateFilter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(deleteCertificateFilter, DeleteCertificateRequestFilter.class);
            DeleteCertificateRequestFilter filter = Globals.objectMapper.readValue(deleteCertificateFilter, DeleteCertificateRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            hibernateSession.setAutoCommit(false);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            DBItemEncCertificate dbCert = dbLayer.getEnciphermentCertificate(filter.getCertAlias());
            if (dbCert != null) {
                Globals.beginTransaction(hibernateSession);
                dbLayer.removeAllEnciphermentCertificateMappingsByAgent(dbCert.getAlias());
                hibernateSession.delete(dbCert);
                Globals.commit(hibernateSession);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(hibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
