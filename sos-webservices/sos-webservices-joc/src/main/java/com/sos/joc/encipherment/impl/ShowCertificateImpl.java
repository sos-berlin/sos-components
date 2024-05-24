package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IShowCertificate;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.encipherment.EncCertificate;
import com.sos.joc.model.encipherment.ShowCertificateRequestFilter;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment")
public class ShowCertificateImpl extends JOCResourceImpl implements IShowCertificate {

    private static final String API_CALL = "./encipherment/certificate";

    @Override
    public JOCDefaultResponse postShowCertificate(String xAccessToken, byte[] showCertificateFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, showCertificateFilter, xAccessToken);
            JsonValidator.validateFailFast(showCertificateFilter, ShowCertificateRequestFilter.class);
            ShowCertificateRequestFilter filter = Globals.objectMapper.readValue(showCertificateFilter, ShowCertificateRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            DBItemEncCertificate dbCert = dbLayer.getEnciphermentCertificate(filter.getCertAlias());
            EncCertificate cert = new EncCertificate();
            cert.setCertAlias(dbCert.getAlias());
            cert.setCertificate(dbCert.getCertificate());
            cert.setPrivateKeyPath(dbCert.getPrivateKeyPath());
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(cert));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
