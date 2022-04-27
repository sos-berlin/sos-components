package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import com.sos.joc.keys.auth.token.OnetimeTokens;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.auth.token.OnetimeToken;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;

public class ClientServerCertificateUtil {

    public static RolloutResponse createClientServerAuthKeyPair (SOSHibernateSession hibernateSession, CreateCSRFilter createCsrFilter)
            throws SOSHibernateException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CertException, OperatorCreationException, IOException, InvalidKeySpecException, 
            InvalidNameException, SOSMissingDataException {
        DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
        JocKeyPair rootKeyPair = dbLayer.getAuthRootCaKeyPair();
        if (rootKeyPair == null) {
            throw new SOSMissingDataException("No CA found for signing.");
        }
        X509Certificate rootCert = KeyUtil.getX509Certificate(rootKeyPair.getCertificate());
        KeyPair newClientKeyPair = KeyUtil.createECDSAKeyPair();
        String userDN = CAUtils.createUserSubjectDN(createCsrFilter.getDn(), rootCert, createCsrFilter.getHostname());
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, newClientKeyPair, userDN);
        X509Certificate clientCert = CAUtils.signAuthCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, 
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

    
    public static void cleanupInvalidatedTokens() {
        Date now = Date.from(Instant.now());
        OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
        List<OnetimeToken> invalidated = onetimeTokens.getTokens().stream()
                .filter(token -> token.getValidUntil().getTime() <= now.getTime()).collect(Collectors.toList());
        if (!invalidated.isEmpty()) {
            onetimeTokens.getTokens().removeAll(invalidated);
        }
    }
}
