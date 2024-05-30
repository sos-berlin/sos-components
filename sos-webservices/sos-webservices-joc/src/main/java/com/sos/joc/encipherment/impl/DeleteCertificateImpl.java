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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.encipherment.DeleteCertificateRequestFilter;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/certificate")
public class DeleteCertificateImpl extends JOCResourceImpl implements IDeleteCertificate {

    private static final String API_CALL = "./encipherment/certificate/delete";

    @Override
    public JOCDefaultResponse postDeleteCertificate(String xAccessToken, byte[] deleteCertificateFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, deleteCertificateFilter, xAccessToken);
            JsonValidator.validateFailFast(deleteCertificateFilter, DeleteCertificateRequestFilter.class);
            DeleteCertificateRequestFilter filter = Globals.objectMapper.readValue(deleteCertificateFilter, DeleteCertificateRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            hibernateSession.setAutoCommit(false);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            DBItemEncCertificate dbCert = dbLayer.getEnciphermentCertificate(filter.getCertAlias());
            Globals.beginTransaction(hibernateSession);
            dbLayer.removeAllEnciphermentCertificateMappingsByAgent(dbCert.getAlias());
            hibernateSession.delete(dbCert);
            Globals.commit(hibernateSession);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(hibernateSession);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(hibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
