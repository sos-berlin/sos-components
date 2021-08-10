package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import javax.naming.InvalidNameException;

import org.bouncycastle.cert.CertException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;

public abstract class ClientServerCertificateUtil {

    public static RolloutResponse createClientServerAuthKeyPair (SOSHibernateSession hibernateSession, CreateCSRFilter createCsrFilter)
            throws SOSHibernateException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CertException, OperatorCreationException, IOException, InvalidKeySpecException, InvalidNameException {
        DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
        JocKeyPair rootKeyPair = dbLayer.getAuthRootCaKeyPair();
        X509Certificate rootCert = KeyUtil.getX509Certificate(rootKeyPair.getCertificate());
        KeyPair newClientKeyPair = KeyUtil.createECDSAKeyPair();
        String userDN = CAUtils.createUserSubjectDN(createCsrFilter.getDn(), rootCert, createCsrFilter.getHostname());
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, newClientKeyPair, userDN);
        X509Certificate clientCert = CAUtils.signCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, 
                KeyUtil.getKeyPairFromECDSAPrivatKeyString(rootKeyPair.getPrivateKey()).getPrivate(), csr, rootCert, createCsrFilter.getSan());
        JocKeyPair clientServerAuthKeyPair = KeyUtil.createECDSAJOCKeyPair(newClientKeyPair);
        clientServerAuthKeyPair.setKeyAlgorithm(SOSKeyConstants.ECDSA_ALGORITHM_NAME);
        clientServerAuthKeyPair.setKeyType(JocKeyType.X509.name());
        clientServerAuthKeyPair.setCertificate(CertificateUtils.asPEMString(clientCert));
        RolloutResponse response = new RolloutResponse();
        response.setJocKeyPair(clientServerAuthKeyPair);
        response.setCaCert(rootKeyPair.getCertificate());
        return response;
    }
}
