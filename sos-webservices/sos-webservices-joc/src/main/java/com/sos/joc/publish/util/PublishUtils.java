package com.sos.joc.publish.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocPGPKeyType;
import com.sos.joc.model.pgp.SOSPGPKeyPair;

public abstract class PublishUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishUtils.class);
    private static final String PRIVATE_KEY_BLOCK_TITLESTART = "-----BEGIN PGP PRIVATE";
    private static final String PUBLIC_KEY_BLOCK_TITLESTART = "-----BEGIN PGP PUBLIC";

    public static String getExtensionFromFilename(String filename) {
        String extension = filename;
        if (filename == null) {
            return "";
        }
        if (extension.contains(".")) {
            extension = extension.replaceFirst(".*\\.([^\\.]+)$", "$1");
        } else {
            extension = "";
        }
        return extension.toLowerCase();
    }

    public static void inputStream2OutputStream(InputStream inStream, OutputStream outStream) throws IOException {
        int bytesRead;
        byte[] buf = new byte[1024];
        while ((bytesRead = inStream.read(buf)) > 0) {
            outStream.write(buf, 0, bytesRead);
        }
        inStream.close();
        outStream.close();
    }
    
    private static void storeKey(SOSPGPKeyPair keyPair, SOSHibernateSession hibernateSession, String account)  throws SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocPGPKeyType.PRIVATE.ordinal(), keyPair.getPrivateKey(), account);
            } else if (keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocPGPKeyType.PUBLIC.ordinal(), keyPair.getPublicKey(), account);
            }
        }
    }

    public static void checkJocSecurityLevelAndStore (SOSPGPKeyPair keyPair, SOSHibernateSession hibernateSession, String account) 
            throws SOSHibernateException, JocUnsupportedKeyTypeException, JocMissingRequiredParameterException {
        if (keyPair != null) {
            //Check forJocSecurityLevel commented, has to be introduced when the testing can be done with changing joc.properties
            if (keyPair.getPrivateKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.MEDIUM)) {
                if (keyPair.getPrivateKey().startsWith(PUBLIC_KEY_BLOCK_TITLESTART)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
                }
                storeKey(keyPair, hibernateSession, account);
            } else if (keyPair.getPublicKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                if (keyPair.getPublicKey().startsWith(PRIVATE_KEY_BLOCK_TITLESTART)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public | received: private");
                }
                storeKey(keyPair, hibernateSession, account);
            } else if (keyPair.getPublicKey() != null && !Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
            } else if (keyPair.getPrivateKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public | received: private");
            } else if (Globals.getJocSecurityLevel().equals(JocSecurityLevel.LOW)) {
                LOGGER.info("JOC Security Level is low, no key will be stored");
            }
        } else {
            throw new JocMissingRequiredParameterException("no key was provided with the request.");
        }
    }
}
