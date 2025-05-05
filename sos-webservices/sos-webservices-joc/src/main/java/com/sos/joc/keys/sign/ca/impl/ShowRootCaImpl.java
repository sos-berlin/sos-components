package com.sos.joc.keys.sign.ca.impl;

import java.security.cert.X509Certificate;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.sign.resource.IShowKey;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.sign.JocKeyAlgorithm;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;


@Path("profile/key/ca")
public class ShowRootCaImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./profile/key/ca";

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            String accountName = "";
            if (JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                accountName = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            } else {
                accountName =  jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            }
            DBItemInventoryCertificate dbCert = dbLayerKeys.getSigningRootCaCertificate(accountName);
            JocKeyPair jocKeyPair = new JocKeyPair();
            if(dbCert != null) {
                X509Certificate certificate = KeyUtil.getX509Certificate(dbCert.getPem());
                jocKeyPair.setCertificate(dbCert.getPem());
                jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.fromValue(dbCert.getKeyAlgorithm()).name());
                jocKeyPair.setKeyType(JocKeyType.fromValue(dbCert.getKeyType()).name());
                jocKeyPair.setKeyID(certificate.getSubjectX500Principal().getName());
                jocKeyPair.setValidUntil(certificate.getNotAfter());
            }
            return JOCDefaultResponse.responseStatus200(jocKeyPair);
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
