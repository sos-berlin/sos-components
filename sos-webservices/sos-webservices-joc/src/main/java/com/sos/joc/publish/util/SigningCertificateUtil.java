package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.naming.InvalidNameException;

import org.bouncycastle.cert.CertException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;

public class SigningCertificateUtil {

    public static JocKeyPair createSigningKeyPair (SOSHibernateSession hibernateSession, CreateCSRFilter createCsrFilter, String keyAlgorithm,
            Date validUntil) throws SOSHibernateException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CertException, OperatorCreationException, IOException, InvalidKeySpecException, 
            InvalidNameException, SOSMissingDataException {
        DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
        JocKeyPair jocRootKeyPair = dbLayer.getAuthRootCaKeyPair();
        if (jocRootKeyPair == null) {
            throw new SOSMissingDataException("No CA found for signing.");
        }
        X509Certificate rootCert = KeyUtil.getX509Certificate(jocRootKeyPair.getCertificate());
        String userDN = CAUtils.createUserSubjectDN(createCsrFilter.getDn(), rootCert);
        String signerAlgorithm = SOSKeyConstants.ECDSA_SIGNER_ALGORITHM;
        KeyPair newClientKeyPair = null;
        X509Certificate clientCert = null;
        JocKeyPair signingKeyPair = new JocKeyPair();
        if(SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyAlgorithm)) {
            signerAlgorithm = keyAlgorithm;
            newClientKeyPair = KeyUtil.createRSAKeyPair();
            KeyPair rootKeyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocRootKeyPair.getPrivateKey());
            PKCS10CertificationRequest csr = CAUtils.createCSR(signerAlgorithm, newClientKeyPair, rootKeyPair, userDN);
            clientCert = CAUtils.signCSR(signerAlgorithm, rootKeyPair.getPrivate(), newClientKeyPair, csr, rootCert,
                    null, validUntil);
            signingKeyPair = KeyUtil.createJOCKeyPair(newClientKeyPair);
        } else {
            signerAlgorithm = SOSKeyConstants.ECDSA_SIGNER_ALGORITHM;
            newClientKeyPair = KeyUtil.createECDSAKeyPair();
            KeyPair rootKeyPair = KeyUtil.getKeyPairFromECDSAPrivatKeyString(jocRootKeyPair.getPrivateKey());
            PKCS10CertificationRequest csr = CAUtils.createCSR(signerAlgorithm, newClientKeyPair, rootKeyPair, userDN);
            clientCert = CAUtils.signCSR(signerAlgorithm, rootKeyPair.getPrivate(), newClientKeyPair, csr, rootCert,
                    createCsrFilter.getSan(), validUntil);
            signingKeyPair = KeyUtil.createECDSAJOCKeyPair(newClientKeyPair);
        }
        signingKeyPair.setKeyAlgorithm(keyAlgorithm);
        signingKeyPair.setKeyType(JocKeyType.X509.name());
        signingKeyPair.setCertificate(CertificateUtils.asPEMString(clientCert));
        return signingKeyPair;
    }

}
