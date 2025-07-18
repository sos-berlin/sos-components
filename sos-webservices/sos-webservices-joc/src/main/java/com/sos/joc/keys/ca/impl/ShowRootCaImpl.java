package com.sos.joc.keys.ca.impl;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.keys.sign.resource.IShowKey;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.util.PublishUtils;

import jakarta.ws.rs.Path;


@Path("profile/ca")
public class ShowRootCaImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./profile/ca";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowRootCaImpl.class);

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, "{}".getBytes(), xAccessToken, CategoryType.CERTIFICATES);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair jocKeyPair = dbLayerKeys.getAuthRootCaKeyPair();
            if (!PublishUtils.jocKeyPairNotEmpty(jocKeyPair)) {
                jocKeyPair = new JocKeyPair();
            } else {
                KeyPair keyPair = null;
                if (jocKeyPair.getPrivateKey() != null) {
                    keyPair = KeyUtil.getKeyPairFromECDSAPrivatKeyString(jocKeyPair.getPrivateKey());
                    // determine if key is a PGP or RSA key
                }
                if (jocKeyPair.getCertificate() != null) {
                    X509Certificate cert = KeyUtil.getX509Certificate(jocKeyPair.getCertificate());
                    jocKeyPair.setKeyID(cert.getSubjectX500Principal().getName());
                    jocKeyPair.setValidUntil(cert.getNotAfter());
                }
                if (jocKeyPair.getValidUntil() == null) {
                    LOGGER.trace("Key does not expire or not readable from key!");
                } else {
                    if (jocKeyPair.getValidUntil().getTime() < Date.from(Instant.now()).getTime()) {
                        LOGGER.trace("Key has expired on: " + jocKeyPair.getValidUntil().toString()); 
                    } else {
                        LOGGER.trace("valid until: " + jocKeyPair.getValidUntil().toString()); 
                    }
                }
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(jocKeyPair));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
