package com.sos.commons.sign;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.cert.CertException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.commons.sign.pgp.ca.CAUtils;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.commons.sign.pgp.sign.SignObject;
import com.sos.commons.sign.pgp.verify.VerifySignature;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CATests {

    private static final Logger LOGGER = LoggerFactory.getLogger(CATests.class);

    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.info("************************************  CA Tests started  ***********************************************");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("************************************  CA Tests finished  **********************************************");
    }

    @Test
    public void test01RSACreateRootCertificateCSRAndUserCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException,
    CertificateException, IOException, CertException, InvalidKeyException, InvalidKeySpecException, SignatureException {
        LOGGER.info("************************************  Test RSA: create rootCertificate, CSR and userCertificate  ******");
        // create a KeyPair for the root CA
        KeyPair rootKeyPair = KeyUtil.createRSAKeyPair();
        String rootSubjectDN = CAUtils.createRootSubjectDN("SOS root CA", "www.sos-berlin.com", "SOS GmbH", "DE");
        LOGGER.info("issuerDN: " + rootSubjectDN);
        // create a root certificate for the root CA
        Certificate rootCertificate = CAUtils.createSelfSignedCertificate(SOSPGPConstants.DEFAULT_ALGORYTHM, rootKeyPair, rootSubjectDN, true, false);
        assertNotNull(rootCertificate);
        String rootCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootCertificate.getEncoded()), 
                SOSPGPConstants.CERTIFICATE_HEADER, SOSPGPConstants.CERTIFICATE_FOOTER);
        LOGGER.info("************************************  root Certificate:  **********************************************");
        LOGGER.info("\n" + rootCert);
        try {
            LOGGER.info("************************************  verify root Certificate:  ***************************************");
            rootCertificate.verify(rootKeyPair.getPublic());
            LOGGER.info("root certificate was successfully verified.");
            LOGGER.info("\nCertificate cerdentials :\n" + ((X509Certificate)rootCertificate).toString());
            List<String> usages = ((X509Certificate)rootCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.info("Usage: " + usage);
                } 
            }
        } catch (CertificateException e) {
            // on encoding errors
            LOGGER.info("CertificateException ocurred. (on encoding errors)");
        } catch (NoSuchAlgorithmException e) {
            // on unsupported signature algorithms
            LOGGER.info("NoSuchAlgorithmException ocurred. (on unsupported signature algorithms)");
        } catch (InvalidKeyException e) {
            // on incorrect key
            LOGGER.info("InvalidKeyException ocurred. (on incorrect key)");
        } catch (NoSuchProviderException e) {
            // if there's no default provider
            LOGGER.info("NoSuchProviderException ocurred. (if there's no default provider)");
        } catch (SignatureException e) {
            // on signature errors
            LOGGER.info("SignatureException ocurred. (on signature errors)");
        } 
        // create a user KeyPair
        KeyPair userKeyPair = KeyUtil.createRSAKeyPair();
        String userSubjectDN = CAUtils.createUserSubjectDN("SP", "www.sos-berlin.com", "IT", "SOS GmbH", "Berlin", "Berlin", "DE"); 
        LOGGER.info("user subjectDN: " + userSubjectDN);
        // create a CSR based on the users KeyPair
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSPGPConstants.DEFAULT_ALGORYTHM, userKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString= KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));
        LOGGER.info("************************************  CSR:  ***********************************************************");
        LOGGER.info("\n" + csrAsString);
        X509Certificate userCertificate = 
                CAUtils.signCSR(SOSPGPConstants.DEFAULT_ALGORYTHM, userKeyPair.getPrivate(), csr, (X509Certificate)rootCertificate, "sp.sos");
        assertNotNull(userCertificate);
        String userCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userCertificate.getEncoded()), 
                SOSPGPConstants.CERTIFICATE_HEADER, SOSPGPConstants.CERTIFICATE_FOOTER);
        LOGGER.info("************************************  User Certificate:  **********************************************");
        LOGGER.info("\n" + userCert);
        try {
            LOGGER.info("************************************  verify user Certificate:  ***************************************");
            userCertificate.verify(userKeyPair.getPublic());
            LOGGER.info("user certificate was successfully verified.");
            LOGGER.info("\nUser certificate credentials:\n" + userCertificate.toString());
            List<String> usages = ((X509Certificate)userCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.info("Usage: " + usage);
                } 
            }
        } catch (CertificateException e) {
            // on encoding errors
            LOGGER.info("CertificateException ocurred. (on encoding errors)");
        } catch (NoSuchAlgorithmException e) {
            // on unsupported signature algorithms
            LOGGER.info("NoSuchAlgorithmException ocurred. (on unsupported signature algorithms)");
        } catch (InvalidKeyException e) {
            // on incorrect key
            LOGGER.info("InvalidKeyException ocurred. (on incorrect key)");
        } catch (NoSuchProviderException e) {
            // if there's no default provider
            LOGGER.info("NoSuchProviderException ocurred. (if there's no default provider)");
        } catch (SignatureException e) {
            // on signature errors
            LOGGER.info("SignatureException ocurred. (on signature errors)");
        } 
        LOGGER.info("**************  check if PublicKey from KeyPair and Public Key from user certificate are the same  ****");
        if (userKeyPair.getPublic().equals(userCertificate.getPublicKey())) {
            LOGGER.info("Users PublicKey from Key Pair and Public Key from user certificate are the same!");
        } else {
            LOGGER.info("Users PublicKey from Key Pair and Public Key from user certificate are not the same!");
        }
        String testStringToSign = "Test String to Sign";
        LOGGER.info("************************************  Sign String with users Private Key:******************************");
        String signature = SignObject.signX509(userKeyPair.getPrivate(), testStringToSign);
        LOGGER.info("************************************  Signature:  *****************************************************");
        LOGGER.info("\n" + signature);
        LOGGER.info("************************************  Signature verification with user certificate:  ******************");
        boolean verify = VerifySignature.verifyX509BC(userCertificate, testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verify);
        verify = VerifySignature.verifyX509WithCertifcateString(userCert, testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509WithCertifcateString\" successful: " + verify);
        verify = VerifySignature.verifyX509(userCertificate.getPublicKey(), testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509 (PublicKey from Certificate)\" successful: " + verify);
        assertTrue(verify);
        LOGGER.info("************************************  Test create rootCertificate, CSR and userCertificate finished ***");
    }

    @Test
    public void test02ECDSACreateRootCertificateCSRAndUserCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException,
    CertificateException, IOException, CertException, InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidAlgorithmParameterException {
        LOGGER.info("");
        LOGGER.info("************************************  Test ECDSA: create rootCertificate, CSR and userCertificate  ****");
        // create a KeyPair for the root CA
        KeyPair rootKeyPair = KeyUtil.createBCECDSAKeyPair();
        String rootSubjectDN = CAUtils.createRootSubjectDN("SOS root CA", "www.sos-berlin.com", "SOS GmbH", "DE");
        LOGGER.info("issuerDN: " + rootSubjectDN);
        // create a root certificate for the root CA
        Certificate rootCertificate = CAUtils.createSelfSignedCertificate(SOSPGPConstants.ECDSA_ALGORYTHM, rootKeyPair, rootSubjectDN, true, false);
        assertNotNull(rootCertificate);
        String rootCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootCertificate.getEncoded()), 
                SOSPGPConstants.CERTIFICATE_HEADER, SOSPGPConstants.CERTIFICATE_FOOTER);
        LOGGER.info("************************************  root Certificate:  **********************************************");
        LOGGER.info("\n" + rootCert);
        try {
            LOGGER.info("************************************  verify root Certificate:  ***************************************");
            rootCertificate.verify(rootKeyPair.getPublic());
            LOGGER.info("root certificate was successfully verified.");
            LOGGER.info("\nCertificate cerdentials :\n" + ((X509Certificate)rootCertificate).toString());
            List<String> usages = ((X509Certificate)rootCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.info("Usage: " + usage);
                } 
            }
        } catch (CertificateException e) {
            // on encoding errors
            LOGGER.info("CertificateException ocurred. (on encoding errors)");
        } catch (NoSuchAlgorithmException e) {
            // on unsupported signature algorithms
            LOGGER.info("NoSuchAlgorithmException ocurred. (on unsupported signature algorithms)");
        } catch (InvalidKeyException e) {
            // on incorrect key
            LOGGER.info("InvalidKeyException ocurred. (on incorrect key)");
        } catch (NoSuchProviderException e) {
            // if there's no default provider
            LOGGER.info("NoSuchProviderException ocurred. (if there's no default provider)");
        } catch (SignatureException e) {
            // on signature errors
            LOGGER.info("SignatureException ocurred. (on signature errors)");
        } 
        // create a user KeyPair
        KeyPair userKeyPair = KeyUtil.createBCECDSAKeyPair();
        String userSubjectDN = CAUtils.createUserSubjectDN("SP", "www.sos-berlin.com", "IT", "SOS GmbH", "Berlin", "Berlin", "DE"); 
        LOGGER.info("user subjectDN: " + userSubjectDN);
        // create a CSR based on the users KeyPair
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSPGPConstants.ECDSA_ALGORYTHM, userKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString= KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));
        LOGGER.info("************************************  CSR:  ***********************************************************");
        LOGGER.info("\n" + csrAsString);
        X509Certificate userCertificate = 
                CAUtils.signCSR(SOSPGPConstants.ECDSA_ALGORYTHM, userKeyPair.getPrivate(), csr, (X509Certificate)rootCertificate, "sp.sos");
        assertNotNull(userCertificate);
        String userCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userCertificate.getEncoded()), 
                SOSPGPConstants.CERTIFICATE_HEADER, SOSPGPConstants.CERTIFICATE_FOOTER);
        LOGGER.info("************************************  User Certificate:  **********************************************");
        LOGGER.info("\n" + userCert);
        try {
            LOGGER.info("************************************  verify user Certificate:  ***************************************");
            userCertificate.verify(userKeyPair.getPublic());
            LOGGER.info("user certificate was successfully verified.");
            LOGGER.info("\nUser certificate credentials:\n" + userCertificate.toString());
            List<String> usages = ((X509Certificate)userCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.info("Usage: " + usage);
                } 
            }
        } catch (CertificateException e) {
            // on encoding errors
            LOGGER.info("CertificateException ocurred. (on encoding errors)");
        } catch (NoSuchAlgorithmException e) {
            // on unsupported signature algorithms
            LOGGER.info("NoSuchAlgorithmException ocurred. (on unsupported signature algorithms)");
        } catch (InvalidKeyException e) {
            // on incorrect key
            LOGGER.info("InvalidKeyException ocurred. (on incorrect key)");
        } catch (NoSuchProviderException e) {
            // if there's no default provider
            LOGGER.info("NoSuchProviderException ocurred. (if there's no default provider)");
        } catch (SignatureException e) {
            // on signature errors
            LOGGER.info("SignatureException ocurred. (on signature errors)");
        } 
        LOGGER.info("**************  check if PublicKey from KeyPair and Public Key from user certificate are the same  ****");
        if (userKeyPair.getPublic().equals(userCertificate.getPublicKey())) {
            LOGGER.info("Users PublicKey from Key Pair and Public Key from user certificate are the same!");
        } else {
            LOGGER.info("Users PublicKey from Key Pair and Public Key from user certificate are not the same!");
        }
        String testStringToSign = "Test String to Sign";
        LOGGER.info("************************************  Sign String with users Private Key:******************************");
        String signature = SignObject.signX509(SOSPGPConstants.ECDSA_ALGORYTHM, userKeyPair.getPrivate(), testStringToSign);
        LOGGER.info("************************************  Signature:  *****************************************************");
        LOGGER.info("\n" + signature);
        LOGGER.info("************************************  Signature verification with user certificate:  ******************");
        boolean verify = VerifySignature.verifyX509BC(SOSPGPConstants.ECDSA_ALGORYTHM, userCertificate, testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verify);
        verify = VerifySignature.verifyX509WithCertifcateString(SOSPGPConstants.ECDSA_ALGORYTHM, userCert, testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509WithCertifcateString\" successful: " + verify);
        verify = VerifySignature.verifyX509(SOSPGPConstants.ECDSA_ALGORYTHM, userCertificate.getPublicKey(), testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509 (PublicKey from Certificate)\" successful: " + verify);
        assertTrue(verify);
        LOGGER.info("************************************  Test create rootCertificate, CSR and userCertificate finished ***");
    }

}
