package com.sos.commons.sign;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.Charsets;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.X509Principal;
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
    public void test01RSACreateRootCertificateCSRAndUserCertificateAndExport() 
            throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException, IOException, CertException, 
            InvalidKeyException, InvalidKeySpecException, SignatureException {
        LOGGER.info("************************************  Test RSA: create rootCertificate, CSR and userCertificate  ******");
        // create a KeyPair for the root CA
        KeyPair rootKeyPair = KeyUtil.createRSAKeyPair();
        String rootSubjectDN = CAUtils.createRootSubjectDN("SOS root CA", "www.sos-berlin.com", "SOS GmbH", "DE");
        LOGGER.info("issuerDN: " + rootSubjectDN);
        // create a root certificate for the root CA
        Certificate rootCertificate = CAUtils.createSelfSignedCertificate(SOSPGPConstants.RSA_ALGORITHM, rootKeyPair, rootSubjectDN, true, false);
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
            LOGGER.info("IssuerDN: " + ((X509Certificate)rootCertificate).getIssuerDN().toString());
            LOGGER.info("SubjectDN: " + ((X509Certificate)rootCertificate).getSubjectDN().toString());
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
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSPGPConstants.RSA_ALGORITHM, userKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString = KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));
        LOGGER.info("************************************  CSR:  ***********************************************************");
        LOGGER.info("\n" + csrAsString);
        X509Certificate userCertificate = 
                CAUtils.signCSR(SOSPGPConstants.RSA_ALGORITHM, userKeyPair.getPrivate(), csr, (X509Certificate)rootCertificate, "sp.sos");
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
            LOGGER.info("IssuerDN: " + ((X509Certificate)userCertificate).getIssuerDN().toString());
            LOGGER.info("SubjectDN: " + ((X509Certificate)userCertificate).getSubjectDN().toString());
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
        String filename = "X.509.RSA.certificate_bundle.zip";
        exportCertificateBundle( 
                KeyUtil.formatPrivateKey(DatatypeConverter.printBase64Binary(rootKeyPair.getPrivate().getEncoded())),
                rootCert,
                csrAsString,
                KeyUtil.formatPrivateKey(DatatypeConverter.printBase64Binary(userKeyPair.getPrivate().getEncoded())),
                userCert, 
                filename);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(filename)));
        LOGGER.info("************************************  Test create rootCertificate, CSR and userCertificate finished ***");
    }

    @Test
    public void test02ECDSACreateRootCertificateCSRAndUserCertificateAndExport() 
            throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException, IOException, CertException, 
            InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidAlgorithmParameterException {
        LOGGER.info("");
        LOGGER.info("************************************  Test ECDSA: create rootCertificate, CSR and userCertificate  ****");
        // create a KeyPair for the root CA
        KeyPair rootKeyPair = KeyUtil.createECDSAKeyPair();
        String rootPrivateKeyString = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootKeyPair.getPrivate().getEncoded()),
                SOSPGPConstants.PRIVATE_EC_KEY_HEADER, SOSPGPConstants.PRIVATE_EC_KEY_FOOTER);
        LOGGER.info("************************************  Root Private Key:  **********************************************");
        LOGGER.info("root private key - algorithm: " + rootKeyPair.getPrivate().getAlgorithm());
        LOGGER.info("root private key - format: " + rootKeyPair.getPrivate().getFormat());
        LOGGER.info("\n" + rootPrivateKeyString);
        String rootSubjectDN = CAUtils.createRootSubjectDN("SOS root CA", "www.sos-berlin.com", "SOS GmbH", "DE");
        LOGGER.info("************************************  Root SubjectDN  *************************************************");
        LOGGER.info("issuerDN: " + rootSubjectDN);
        // create a root certificate for the root CA
        Certificate rootCertificate = CAUtils.createSelfSignedCertificate(SOSPGPConstants.ECDSA_ALGORITHM, rootKeyPair, rootSubjectDN, true, false);
        assertNotNull(rootCertificate);
        String rootCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootCertificate.getEncoded()), 
                SOSPGPConstants.CERTIFICATE_HEADER, SOSPGPConstants.CERTIFICATE_FOOTER);
        LOGGER.info("************************************  Root Certificate:  **********************************************");
        LOGGER.info("\n" + rootCert);
        try {
            LOGGER.info("************************************  Verify root Certificate:  ***************************************");
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
        KeyPair userKeyPair = KeyUtil.createECDSAKeyPair();
        String userPrivateKeyString = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userKeyPair.getPrivate().getEncoded()),
                SOSPGPConstants.PRIVATE_EC_KEY_HEADER, SOSPGPConstants.PRIVATE_EC_KEY_FOOTER);
        LOGGER.info("************************************  User Private Key:  **********************************************");
        LOGGER.info("\n" + userPrivateKeyString);
        String userSubjectDN = CAUtils.createUserSubjectDN("SP", "www.sos-berlin.com", "IT", "SOS GmbH", "Berlin", "Berlin", "DE"); 
        LOGGER.info("************************************  User SubjectDN  *************************************************");
        LOGGER.info("user subjectDN: " + userSubjectDN);
        // create a CSR based on the users KeyPair
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSPGPConstants.ECDSA_ALGORITHM, userKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString= KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));
        LOGGER.info("************************************  CSR:  ***********************************************************");
        LOGGER.info("\n" + csrAsString);
        X509Certificate userCertificate = 
                CAUtils.signCSR(SOSPGPConstants.ECDSA_ALGORITHM, userKeyPair.getPrivate(), csr, (X509Certificate)rootCertificate, "sp.sos");
        assertNotNull(userCertificate);
        String userCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userCertificate.getEncoded()), 
                SOSPGPConstants.CERTIFICATE_HEADER, SOSPGPConstants.CERTIFICATE_FOOTER);
        LOGGER.info("************************************  User Certificate:  **********************************************");
        LOGGER.info("\n" + userCert);
        logCertificateProperties(userCertificate);
        try {
            LOGGER.info("************************************  Verify user Certificate:  ***************************************");
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
        String signature = SignObject.signX509(SOSPGPConstants.ECDSA_ALGORITHM, userKeyPair.getPrivate(), testStringToSign);
        LOGGER.info("************************************  Signature:  *****************************************************");
        LOGGER.info("\n" + signature);
        LOGGER.info("************************************  Signature verification with user certificate:  ******************");
        boolean verify = VerifySignature.verifyX509BC(SOSPGPConstants.ECDSA_ALGORITHM, userCertificate, testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verify);
        verify = VerifySignature.verifyX509WithCertifcateString(SOSPGPConstants.ECDSA_ALGORITHM, userCert, testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509WithCertifcateString\" successful: " + verify);
        verify = VerifySignature.verifyX509(SOSPGPConstants.ECDSA_ALGORITHM, userCertificate.getPublicKey(), testStringToSign, signature);
        LOGGER.info("Signature verification with method \"VerifySignature.verifyX509 (PublicKey from Certificate)\" successful: " + verify);
        assertTrue(verify);
        String filename = "X.509.ECDSA.certificate_bundle.zip";
        exportCertificateBundle(
                KeyUtil.formatPrivateKey(DatatypeConverter.printBase64Binary(rootKeyPair.getPrivate().getEncoded())), 
                rootCert, 
                csrAsString,
                KeyUtil.formatPrivateKey(DatatypeConverter.printBase64Binary(userKeyPair.getPrivate().getEncoded())), 
                userCert, 
                filename);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(filename)));
        LOGGER.info("************************************  Test create rootCertificate, CSR and userCertificate finished ***");
    }
    
    @Test
    public void testECNames () {
        if (ECNamedCurveTable.getNames().hasMoreElements()) {
            LOGGER.info(ECNamedCurveTable.getNames().nextElement().toString());
        }

    }

    private void exportCertificateBundle(String rootKey, String rootCert, String userCertificateRequest, String userKey, String userCert, String filename)
            throws IOException {
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        Boolean notExists = Files.notExists(Paths.get("target").resolve("created_test_files"));
        if (notExists) {
            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
            LOGGER.info("subfolder \"created_test_files\" created in target folder.");
        }
        out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(filename));
        zipOut = new ZipOutputStream(new BufferedOutputStream(out), Charsets.UTF_8);
        ZipEntry rootKeyEntry = new ZipEntry("root_private.key");
        zipOut.putNextEntry(rootKeyEntry);
        zipOut.write(rootKey.getBytes());
        zipOut.closeEntry();
        ZipEntry rootCertEntry = new ZipEntry("root_certificate.crt");
        zipOut.putNextEntry(rootCertEntry);
        zipOut.write(rootCert.getBytes());
        zipOut.closeEntry();
        ZipEntry userCertifcateRequestEntry = new ZipEntry("user_cert_req.csr");
        zipOut.putNextEntry(userCertifcateRequestEntry);
        zipOut.write(userCertificateRequest.getBytes());
        zipOut.closeEntry();
        ZipEntry userKeyEntry = new ZipEntry("user_private.key");
        zipOut.putNextEntry(userKeyEntry);
        zipOut.write(userKey.getBytes());
        zipOut.closeEntry();
        ZipEntry userCertEntry = new ZipEntry("user_certificate.crt");
        zipOut.putNextEntry(userCertEntry);
        zipOut.write(userCert.getBytes());
        zipOut.closeEntry();
        zipOut.flush();
        if (zipOut != null) {
            zipOut.close();
        }
    }
    
//    private void logCertificateProperties(X509Certificate userCertificate) throws CertificateParsingException {
//        Set<String> criticalExtesionOIDs = userCertificate.getCriticalExtensionOIDs();
//        List<String> extendedKeyUsages = userCertificate.getExtendedKeyUsage();
//        Collection<List<?>> issuerAlternativeNames = userCertificate.getIssuerAlternativeNames();
//        Principal issuerDN = userCertificate.getIssuerDN();
//        boolean[] issuerUniqueID = userCertificate.getIssuerUniqueID();
//        X500Principal issuerX509Principal = userCertificate.getIssuerX500Principal();
//        boolean[] keyUsage = userCertificate.getKeyUsage();
//        Set<String> nonCriticalExtensionOIDs = userCertificate.getNonCriticalExtensionOIDs();
//        Date notBefore = userCertificate.getNotBefore();
//        Date notAfter = userCertificate.getNotAfter();
//        BigInteger serialNumber = userCertificate.getSerialNumber();
//        byte[] sigAlgParams = userCertificate.getSigAlgParams();
//        Collection<List<?>> subjectAlternativeNames = userCertificate.getSubjectAlternativeNames();
//        
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgOID() = %1$s", userCertificate.getSigAlgOID()));
//        LOGGER.info(String.format("X509Certificate.getType() = %1$s", userCertificate.getType()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//        LOGGER.info(String.format("X509Certificate.getSigAlgName() = %1$s", userCertificate.getSigAlgName()));
//    }
    
}
