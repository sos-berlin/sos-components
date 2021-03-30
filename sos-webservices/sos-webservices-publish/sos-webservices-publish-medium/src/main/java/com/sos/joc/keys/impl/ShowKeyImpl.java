package com.sos.joc.keys.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.IShowKey;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.sign.JocKeyAlgorithm;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.joc.publish.util.PublishUtils;


@Path("profile/key")
public class ShowKeyImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./profile/key";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowKeyImpl.class);

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
            JocKeyPair jocKeyPair = dbLayerKeys.getKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername(), JocSecurityLevel.MEDIUM);
            if (!PublishUtils.jocKeyPairNotEmpty(jocKeyPair)) {
                jocKeyPair = new JocKeyPair();
            } else {
                PublicKey publicKey = null;
                PGPPublicKey publicPGPKey = null;
                KeyPair keyPair = null;
                if (jocKeyPair.getPublicKey() == null) {
                    if (jocKeyPair.getPrivateKey() != null) {
                        jocKeyPair.setKeyType(JocKeyType.PRIVATE.name());
                        // determine if key is a PGP or RSA key
                        if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                            // restore public key from private key
                            jocKeyPair.setPublicKey(KeyUtil.extractPublicKey(jocKeyPair.getPrivateKey()));
                            // calculate validity period
                            InputStream privateKeyStream = IOUtils.toInputStream(jocKeyPair.getPrivateKey());
                            publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
                        } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                            // restore public key from private key
                            keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
                            publicKey = keyPair.getPublic();
                            jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                    new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                    SOSKeyConstants.PUBLIC_RSA_KEY_HEADER, 
                                    SOSKeyConstants.PUBLIC_RSA_KEY_FOOTER));
                        } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                            keyPair = KeyUtil.getKeyPairFromECDSAPrivatKeyString(jocKeyPair.getPrivateKey());
                            publicKey = keyPair.getPublic();
                            jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                    new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                    SOSKeyConstants.PUBLIC_EC_KEY_HEADER, 
                                    SOSKeyConstants.PUBLIC_EC_KEY_FOOTER));
                        } else {
                            // restore public key from private key
                            keyPair = KeyUtil.getKeyPairFromPrivatKeyString(jocKeyPair.getPrivateKey());
                            publicKey = keyPair.getPublic();
                            jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                    new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                    SOSKeyConstants.PUBLIC_KEY_HEADER, 
                                    SOSKeyConstants.PUBLIC_KEY_FOOTER));
                        }
                    } else {
                        // only certificate is stored in the DB
                        jocKeyPair.setKeyType(JocKeyType.PUBLIC.name());
                        if (jocKeyPair.getCertificate() != null) {
                            X509Certificate cert = KeyUtil.getX509Certificate(jocKeyPair.getCertificate());
                            PublicKey pub = cert.getPublicKey();
                            if (pub instanceof RSAPublicKey){
                                jocKeyPair.setKeyID(KeyUtil.getRSAKeyIDAsHexString(pub).toUpperCase());
                                jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.RSA.name());
                                jocKeyPair.setValidUntil(null);
                            } else if (pub instanceof ECPublicKey) {
                                jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.name());
                                jocKeyPair.setValidUntil(null);
                            }
                        }
                    }
                } else {
                    // determine if public key is a PGP, RSA or ECDSA key
                    jocKeyPair.setKeyType(JocKeyType.PUBLIC.name());
                    if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        publicPGPKey = KeyUtil.getPGPPublicKeyFromString(jocKeyPair.getPublicKey());                          
                    } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        publicKey = (PublicKey)KeyUtil.getSubjectPublicKeyInfo(jocKeyPair.getPublicKey());
                    } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        publicKey = KeyUtil.convertToRSAPublicKey(KeyUtil.decodePublicKeyString(jocKeyPair.getPublicKey()));
                    }
                }
                if(publicPGPKey != null) {
                    jocKeyPair.setKeyID(KeyUtil.getKeyIDAsHexString(publicPGPKey).toUpperCase());
                    jocKeyPair.setValidUntil(KeyUtil.getValidUntil(publicPGPKey)); 
                    jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.PGP.name());
                } else if (publicKey != null) {
                    if (publicKey instanceof RSAPublicKey){
                        jocKeyPair.setKeyID(KeyUtil.getRSAKeyIDAsHexString(publicKey).toUpperCase());
                        jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.RSA.name());
                        jocKeyPair.setValidUntil(null);
                    } else if (publicKey instanceof ECPublicKey) {
                        jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.name());
                        jocKeyPair.setValidUntil(null);
                    }
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
