package com.sos.joc.keys.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.IShowKey;
import com.sos.joc.model.pgp.JocKeyAlgorythm;
import com.sos.joc.model.pgp.JocKeyPair;


@Path("publish")
public class ShowKeyImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./publish/show_key";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowKeyImpl.class);

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, xAccessToken, "",
//                    getPermissonsJocCockpit(null, xAccessToken).getJS7Controller().getAdministration().getConfigurations().getPublish().isShowKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair jocKeyPair = dbLayerKeys.getKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername());
            if (jocKeyPair == null 
                    || (jocKeyPair != null && jocKeyPair.getPublicKey() == null && jocKeyPair.getPrivateKey() == null) 
                    || (jocKeyPair != null && "".equals(jocKeyPair.getPublicKey()) && "".equals(jocKeyPair.getPrivateKey()))
                ) {
                jocKeyPair = new JocKeyPair();
            } else {
                PublicKey publicKey = null;
                PGPPublicKey publicPGPKey = null;
                KeyPair keyPair = null;
                if (jocKeyPair.getPublicKey() == null) {
                    // determine if key is a PGP or RSA key
                    if (jocKeyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                        // restore public key from private key
                        jocKeyPair.setPublicKey(KeyUtil.extractPublicKey(jocKeyPair.getPrivateKey()));
                        // calculate validity period
                        InputStream privateKeyStream = IOUtils.toInputStream(jocKeyPair.getPrivateKey());
                        publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
                    } else if (jocKeyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                        // restore public key from private key
                        keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
                        publicKey = keyPair.getPublic();
                        jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                SOSPGPConstants.PUBLIC_RSA_KEY_HEADER, 
                                SOSPGPConstants.PUBLIC_RSA_KEY_FOOTER));
                    } else {
                        // restore public key from private key
                        keyPair = KeyUtil.getKeyPairFromPrivatKeyString(jocKeyPair.getPrivateKey());
                        publicKey = keyPair.getPublic();
                        jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                SOSPGPConstants.PUBLIC_KEY_HEADER, 
                                SOSPGPConstants.PUBLIC_KEY_FOOTER));
                    }
                } else {
                    // determine if key is a PGP or RSA key
                    if (jocKeyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
                        publicPGPKey = KeyUtil.getPGPPublicKeyFromString(jocKeyPair.getPublicKey());                          
                    } else if (jocKeyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER)) {
                        publicKey = (PublicKey)KeyUtil.getSubjectPublicKeyInfo(jocKeyPair.getPublicKey());
                    } else if (jocKeyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_KEY_HEADER)) {
                        publicKey = KeyUtil.convertToPublicKey(KeyUtil.decodePublicKeyString(jocKeyPair.getPublicKey()));
                    }
                }
                if(publicPGPKey != null) {
                    jocKeyPair.setKeyID(KeyUtil.getKeyIDAsHexString(publicPGPKey).toUpperCase());
                    jocKeyPair.setValidUntil(KeyUtil.getValidUntil(publicPGPKey)); 
                    jocKeyPair.setKeyType(JocKeyAlgorythm.PGP.name());
                } else {
                    jocKeyPair.setKeyID(KeyUtil.getRSAKeyIDAsHexString(publicKey).toUpperCase());
                    jocKeyPair.setKeyType(JocKeyAlgorythm.RSA.name());
                    jocKeyPair.setValidUntil(null);
                }
                if (jocKeyPair.getValidUntil() == null) {
                    LOGGER.trace("Key does not expire!");
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
        }
    }

}
