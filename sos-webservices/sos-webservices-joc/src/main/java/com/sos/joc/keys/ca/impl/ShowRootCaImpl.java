package com.sos.joc.keys.ca.impl;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.sign.resource.IShowKey;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.util.PublishUtils;


@Path("profile/ca")
public class ShowRootCaImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./profile/ca";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowRootCaImpl.class);

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates()
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
                    jocKeyPair.setKeyID(cert.getSubjectDN().getName());
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
