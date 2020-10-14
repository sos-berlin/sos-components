package com.sos.joc.keys.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;
import javax.xml.bind.DatatypeConverter;

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
import com.sos.joc.model.pgp.JocKeyAlgorithm;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;


@Path("publish")
public class ShowKeyImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./publish/show_key";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowKeyImpl.class);

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, xAccessToken, "",
                    getPermissonsJocCockpit(null, xAccessToken).getInventory().getConfigurations().getPublish().isShowKey());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair jocKeyPair = dbLayerKeys.getKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername(), Globals.getJocSecurityLevel());
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
                    jocKeyPair.setKeyType(JocKeyType.PRIVATE.name());
                    // determine if key is a PGP or RSA key
                    if (SOSPGPConstants.PGP_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        // restore public key from private key
                        jocKeyPair.setPublicKey(KeyUtil.extractPublicKey(jocKeyPair.getPrivateKey()));
                        // calculate validity period
                        InputStream privateKeyStream = IOUtils.toInputStream(jocKeyPair.getPrivateKey());
                        publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
                    } else if (SOSPGPConstants.RSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        // restore public key from private key
//                        try {
//                            PrivateKey pk = KeyUtil.getPrivateKeyFromString(jocKeyPair.getPrivateKey());
//                        } catch (ClassCastException e) {
//                            try {
//                                KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
//                            } catch (NoSuchAlgorithmException|InvalidKeySpecException|IOException e1) {
//                                LOGGER.error(e.getMessage(), e);
//                            }
//                        }catch (NoSuchAlgorithmException|InvalidKeySpecException|IOException e) {
//                            LOGGER.error(e.getMessage(), e);
//                        }
                        keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
                        publicKey = keyPair.getPublic();
                        jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                SOSPGPConstants.PUBLIC_RSA_KEY_HEADER, 
                                SOSPGPConstants.PUBLIC_RSA_KEY_FOOTER));
                    } else if (SOSPGPConstants.ECDSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        keyPair = KeyUtil.getKeyPairFromECDSAPrivatKeyString(jocKeyPair.getPrivateKey());
                        publicKey = keyPair.getPublic();
                        jocKeyPair.setPublicKey(KeyUtil.formatEncodedDataString(
                                new String(Base64.encode(publicKey.getEncoded()), StandardCharsets.UTF_8), 
                                SOSPGPConstants.PUBLIC_EC_KEY_HEADER, 
                                SOSPGPConstants.PUBLIC_EC_KEY_FOOTER));
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
                    // determine if public key is a PGP, RSA or ECDSA key
                    jocKeyPair.setKeyType(JocKeyType.PUBLIC.name());
                    if (SOSPGPConstants.PGP_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        publicPGPKey = KeyUtil.getPGPPublicKeyFromString(jocKeyPair.getPublicKey());                          
                    } else if (SOSPGPConstants.RSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        publicKey = (PublicKey)KeyUtil.getSubjectPublicKeyInfo(jocKeyPair.getPublicKey());
                    } else if (SOSPGPConstants.ECDSA_ALGORITHM_NAME.equals(jocKeyPair.getKeyAlgorithm())) {
                        publicKey = KeyUtil.convertToPublicKey(KeyUtil.decodePublicKeyString(jocKeyPair.getPublicKey()));
                    }
                }
                if(publicPGPKey != null) {
                    jocKeyPair.setKeyID(KeyUtil.getKeyIDAsHexString(publicPGPKey).toUpperCase());
                    jocKeyPair.setValidUntil(KeyUtil.getValidUntil(publicPGPKey)); 
                    jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.PGP.name());
                } else if (publicKey instanceof RSAPublicKey){
                    jocKeyPair.setKeyID(KeyUtil.getRSAKeyIDAsHexString(publicKey).toUpperCase());
                    jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.RSA.name());
                    jocKeyPair.setValidUntil(null);
                } else if (publicKey instanceof ECPublicKey) {
                    jocKeyPair.setKeyAlgorithm(JocKeyAlgorithm.ECDSA.name());
                    jocKeyPair.setValidUntil(null);
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
        }
    }

}
