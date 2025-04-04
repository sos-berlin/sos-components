package com.sos.commons.sign;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.cert.CertException;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CATests {

    private static final Logger LOGGER = LoggerFactory.getLogger(CATests.class);

    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.trace("************************************  CA Tests started  ***********************************************");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.trace("************************************  CA Tests finished  **********************************************");
    }

    @Test
    public void test01RSACreateRootCertificateCSRAndUserCertificateAndExport() 
            throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException, IOException, CertException, 
            InvalidKeyException, InvalidKeySpecException, SignatureException {
        LOGGER.trace("************************************  Test RSA: create rootCertificate, CSR and userCertificate  ******");
        // create a KeyPair for the root CA
        KeyPair rootKeyPair = KeyUtil.createRSAKeyPair();
        String rootSubjectDN = CAUtils.createRootSubjectDN("SOS root CA", "SOS root CA", "www.sos-berlin.com", "SOS GmbH", "DE");
        LOGGER.trace("issuerDN: " + rootSubjectDN);
        // create a root certificate for the root CA
        Certificate rootCertificate = CAUtils.createSelfSignedRootCertificate(SOSKeyConstants.RSA_SIGNER_ALGORITHM, rootKeyPair, rootSubjectDN, true, false);
        assertNotNull(rootCertificate);
        String rootCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootCertificate.getEncoded()), 
                SOSKeyConstants.CERTIFICATE_HEADER, SOSKeyConstants.CERTIFICATE_FOOTER);
        LOGGER.trace("************************************  root Certificate:  **********************************************");
        LOGGER.trace("\n" + rootCert);
        try {
            LOGGER.trace("************************************  verify root Certificate:  ***************************************");
            rootCertificate.verify(rootKeyPair.getPublic());
            LOGGER.trace("root certificate was successfully verified.");
            LOGGER.trace("\nCertificate cerdentials :\n" + ((X509Certificate)rootCertificate).toString());
            X509Certificate c = ((X509Certificate)rootCertificate);
            List<String> usages = c.getExtendedKeyUsage();
            LOGGER.trace("X500Principal IssuerDN: " + c.getIssuerX500Principal().getName());
            LOGGER.trace("X500Principal SubjectDN: " + c.getSubjectX500Principal().getName());
            LOGGER.trace("X500Principal SubjectDN: " + c.getSubjectX500Principal().getName(X500Principal.RFC1779));
            LOGGER.trace("X500Principal SubjectDN: " + c.getSubjectX500Principal().getName(X500Principal.CANONICAL));
            LOGGER.trace("X500Principal SubjectDN: " + c.getSubjectX500Principal().getName(X500Principal.RFC1779, Collections.singletonMap("2.5.4.46", "DN")));
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.trace("Usage: " + usage);
                } 
            }
        } catch (CertificateException e) {
            // on encoding errors
            LOGGER.trace("CertificateException ocurred. (on encoding errors)");
        } catch (NoSuchAlgorithmException e) {
            // on unsupported signature algorithms
            LOGGER.trace("NoSuchAlgorithmException ocurred. (on unsupported signature algorithms)");
        } catch (InvalidKeyException e) {
            // on incorrect key
            LOGGER.trace("InvalidKeyException ocurred. (on incorrect key)");
        } catch (NoSuchProviderException e) {
            // if there's no default provider
            LOGGER.trace("NoSuchProviderException ocurred. (if there's no default provider)");
        } catch (SignatureException e) {
            // on signature errors
            LOGGER.trace("SignatureException ocurred. (on signature errors)");
        } 
        // create a user KeyPair
        KeyPair userKeyPair = KeyUtil.createRSAKeyPair();
        String userSubjectDN = CAUtils.createUserSubjectDN("SOS root CA", "SP", "www.sos-berlin.com", "SOS GmbH", "Berlin", "Berlin", "DE"); 
        LOGGER.trace("user subjectDN: " + userSubjectDN);
        // create a CSR based on the users KeyPair
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSKeyConstants.RSA_SIGNER_ALGORITHM, userKeyPair, rootKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString = KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));
        LOGGER.trace("************************************  CSR:  ***********************************************************");
        LOGGER.trace("\n" + csrAsString);
        X509Certificate userCertificate = 
                CAUtils.signCSR(SOSKeyConstants.RSA_SIGNER_ALGORITHM, rootKeyPair.getPrivate(), userKeyPair, csr, (X509Certificate)rootCertificate, "sp.sos");
        assertNotNull(userCertificate);
        String userCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userCertificate.getEncoded()), 
                SOSKeyConstants.CERTIFICATE_HEADER, SOSKeyConstants.CERTIFICATE_FOOTER);
        LOGGER.trace("************************************  User Certificate:  **********************************************");
        LOGGER.trace("\n" + userCert);
        try {
            LOGGER.trace("************************************  verify user Certificate:  ***************************************");
            userCertificate.verify(rootKeyPair.getPublic());
            LOGGER.trace("user certificate was successfully verified.");
            LOGGER.trace("\nUser certificate credentials:\n" + userCertificate.toString());
            List<String> usages = ((X509Certificate)userCertificate).getExtendedKeyUsage();
            LOGGER.trace("IssuerDN: " + ((X509Certificate)userCertificate).getIssuerX500Principal().toString());
            LOGGER.trace("SubjectDN: " + ((X509Certificate)userCertificate).getSubjectX500Principal().toString());
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.trace("Usage: " + usage);
                } 
            }
        } catch (CertificateException e) {
            // on encoding errors
            LOGGER.trace("CertificateException ocurred. (on encoding errors)");
        } catch (NoSuchAlgorithmException e) {
            // on unsupported signature algorithms
            LOGGER.trace("NoSuchAlgorithmException ocurred. (on unsupported signature algorithms)");
        } catch (InvalidKeyException e) {
            // on incorrect key
            LOGGER.trace("InvalidKeyException ocurred. (on incorrect key)");
        } catch (NoSuchProviderException e) {
            // if there's no default provider
            LOGGER.trace("NoSuchProviderException ocurred. (if there's no default provider)");
        } catch (SignatureException e) {
            // on signature errors
            LOGGER.trace("SignatureException ocurred. (on signature errors)");
        } 
        LOGGER.trace("**************  check if PublicKey from KeyPair and Public Key from user certificate are the same  ****");
        if (userKeyPair.getPublic().equals(userCertificate.getPublicKey())) {
            LOGGER.trace("Users PublicKey from Key Pair and Public Key from user certificate are the same!");
        } else {
            LOGGER.trace("Users PublicKey from Key Pair and Public Key from user certificate are not the same!");
        }
        String testStringToSign = "Test String to Sign";
        LOGGER.trace("************************************  Sign String with users Private Key:******************************");
        String signature = SignObject.signX509(userKeyPair.getPrivate(), testStringToSign);
        LOGGER.trace("************************************  Signature:  *****************************************************");
        LOGGER.trace("\n" + signature);
        LOGGER.trace("************************************  Signature verification with user certificate:  ******************");
        boolean verify = VerifySignature.verifyX509BC(userCertificate, testStringToSign, signature);
        LOGGER.trace("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verify);
        verify = VerifySignature.verifyX509BC((X509Certificate)rootCertificate, testStringToSign, signature);
        LOGGER.trace("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verify);
        verify = VerifySignature.verifyX509WithCertifcateString(userCert, testStringToSign, signature);
        LOGGER.trace("Signature verification with method \"VerifySignature.verifyX509WithCertifcateString\" successful: " + verify);
        verify = VerifySignature.verifyX509(userCertificate.getPublicKey(), testStringToSign, signature);
        LOGGER.trace("Signature verification with method \"VerifySignature.verifyX509 (PublicKey from Certificate)\" successful: " + verify);
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
        LOGGER.trace("************************************  Test create rootCertificate, CSR and userCertificate finished ***");
    }

    @Test
    public void test02ECDSACreateRootCertificateCSRAndUserCertificateAndExport() 
            throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException, IOException, CertException, 
            InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidAlgorithmParameterException {
        LOGGER.trace("");
        LOGGER.trace("************************************  Test ECDSA: create rootCertificate, CSR and userCertificate  ****");
        // create a KeyPair for the root CA
        KeyPair rootKeyPair = KeyUtil.createECDSAKeyPair();
        String rootPrivateKeyString = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootKeyPair.getPrivate().getEncoded()),
                SOSKeyConstants.PRIVATE_EC_KEY_HEADER, SOSKeyConstants.PRIVATE_EC_KEY_FOOTER);
        LOGGER.trace("************************************  Root Private Key:  **********************************************");
        LOGGER.trace("root private key - algorithm: " + rootKeyPair.getPrivate().getAlgorithm());
        LOGGER.trace("root private key - format: " + rootKeyPair.getPrivate().getFormat());
        LOGGER.trace("\n" + rootPrivateKeyString);
        String rootSubjectDN = CAUtils.createRootSubjectDN("SOS root CA", "SOS root CA", "www.sos-berlin.com", "SOS GmbH", "DE");
        LOGGER.trace("************************************  Root SubjectDN  *************************************************");
        LOGGER.trace("issuerDN: " + rootSubjectDN);
        // create a root certificate for the root CA
        Certificate rootCertificate = CAUtils.createSelfSignedRootCertificate(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, rootKeyPair, rootSubjectDN, true, true);
        assertNotNull(rootCertificate);
        String rootCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootCertificate.getEncoded()), 
                SOSKeyConstants.CERTIFICATE_HEADER, SOSKeyConstants.CERTIFICATE_FOOTER);
        LOGGER.trace("************************************  Root Certificate:  **********************************************");
        LOGGER.trace("\n" + rootCert);
        try {
            LOGGER.trace("************************************  Verify root Certificate:  ***************************************");
            rootCertificate.verify(rootKeyPair.getPublic());
            LOGGER.trace("root certificate was successfully verified.");
            LOGGER.trace("\nCertificate cerdentials :\n" + ((X509Certificate)rootCertificate).toString());
            List<String> usages = ((X509Certificate)rootCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.trace("Usage: " + usage);
                } 
            }
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            // CertificateException on encoding errors
            // NoSuchAlgorithmException on unsupported signature algorithms
            // InvalidKeyException on incorrect key
            // NoSuchProviderException if there's no default provider
            // SignatureException on signature errors
            LOGGER.trace("root certificate verification failed against CA keyPairs public key.");
            LOGGER.trace(e.getMessage());
        }
        // create a user KeyPair
        KeyPair userKeyPair = KeyUtil.createECDSAKeyPair();
        String userPrivateKeyString = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userKeyPair.getPrivate().getEncoded()),
                SOSKeyConstants.PRIVATE_EC_KEY_HEADER, SOSKeyConstants.PRIVATE_EC_KEY_FOOTER);
        LOGGER.trace("************************************  User Private Key:  **********************************************");
        LOGGER.trace("\n" + userPrivateKeyString);
        String userSubjectDN = CAUtils.createUserSubjectDN("SOS root CA", "SP", "www.sos-berlin.com", "SOS GmbH", "Berlin", "Berlin", "DE"); 
        LOGGER.trace("************************************  User SubjectDN  *************************************************");
        LOGGER.trace("user subjectDN: " + userSubjectDN);
        // create a CSR based on the users KeyPair
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, userKeyPair, rootKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString= KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));
        LOGGER.trace("************************************  CSR:  ***********************************************************");
        LOGGER.trace("\n" + csrAsString);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2099, 0, 31);
        Date validUntil = calendar.getTime();
        X509Certificate userSigningCertificate = 
                CAUtils.signCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, rootKeyPair.getPrivate(), userKeyPair, csr, 
                        (X509Certificate)rootCertificate, "sp.sos", validUntil);
        assertNotNull(userSigningCertificate);
        String userCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userSigningCertificate.getEncoded()), 
                SOSKeyConstants.CERTIFICATE_HEADER, SOSKeyConstants.CERTIFICATE_FOOTER);
        LOGGER.trace("************************************  User Certificate:  **********************************************");
        LOGGER.trace("\n" + userCert);
        try {
            LOGGER.trace("************************************  Verify user Signing Certificate:  *******************************");
            userSigningCertificate.verify(rootCertificate.getPublicKey());
            LOGGER.trace("user certificate was successfully verified against CA certificates public key.");
            LOGGER.trace("\nUser certificate credentials:\n" + userSigningCertificate.toString());
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            LOGGER.trace("user certificate verification failed against CA certificates public key.");
            LOGGER.trace(e.getMessage());
        }
        LOGGER.trace("**************  check if PublicKey from KeyPair and Public Key from user certificate are the same  ****");
        if (userKeyPair.getPublic().equals(userSigningCertificate.getPublicKey())) {
            LOGGER.trace("Users PublicKey from Key Pair and Public Key from user certificate are the same!");
        } else {
            LOGGER.trace("Users PublicKey from Key Pair and Public Key from user certificate are not the same!");
        }
        String testStringToSign = "Test String to Sign";
        LOGGER.trace("************************************  Sign String with users Private Key:******************************");
        String signature = SignObject.signX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, userKeyPair.getPrivate(), testStringToSign);
        LOGGER.trace("************************************  Signature:  *****************************************************");
        LOGGER.trace("\n" + signature);
        LOGGER.trace("  Signature verification with user certificate:");
        boolean verify = VerifySignature.verifyX509BC(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, userSigningCertificate, testStringToSign, signature);
        LOGGER.trace("Signature verification successful: " + verify);
        assertTrue(verify);
        LOGGER.trace("  Signature verification with CA certificate:");
        verify = VerifySignature.verifyX509BC(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, (X509Certificate)rootCertificate, testStringToSign, signature);
        LOGGER.trace("Signature verification successful: " + verify);
        String filename = "X.509.ECDSA.certificate_bundle.zip";
        exportCertificateBundle(
                KeyUtil.formatPrivateKey(DatatypeConverter.printBase64Binary(rootKeyPair.getPrivate().getEncoded())), 
                rootCert, 
                csrAsString,
                KeyUtil.formatPrivateKey(DatatypeConverter.printBase64Binary(userKeyPair.getPrivate().getEncoded())), 
                userCert, 
                filename);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(filename)));
        LOGGER.trace("************************************  Test create rootCertificate, CSR and userCertificate finished ***");
    }
    
    @Ignore
    @Test 
    public void test03CheckCA () throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        LOGGER.trace("");
        LOGGER.trace("************************************  Test ECDSA: check certificates  *********************************");
    	String rootCaCrtPath = "C:/sp/devel/js7/keys/sosCa/root-ca.crt";
    	String intermediateCaCrtPath = "C:/sp/devel/js7/keys/sosCa/intermediate-ca.crt";
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate rootCaCert = (X509Certificate)cf.generateCertificate(Files.newInputStream(Paths.get(rootCaCrtPath)));
		X509Certificate intermediateCaCert = (X509Certificate)cf.generateCertificate(Files.newInputStream(Paths.get(intermediateCaCrtPath)));
		boolean intermediateCaCrtValid = false;
		try {
			intermediateCaCert.verify(rootCaCert.getPublicKey());
			intermediateCaCrtValid = true;
			LOGGER.trace("intermediate CA certificate succesfully validated against root ca certificate.");
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			LOGGER.trace("intermediate CA certificate not valid.");
		}
		assertTrue(intermediateCaCrtValid);
        LOGGER.trace("************************************  Test ECDSA: check certificates  finished ************************");
    }
    
    @Ignore
    @Test
    public void testECNames () {
        if (ECNamedCurveTable.getNames().hasMoreElements()) {
            LOGGER.trace(ECNamedCurveTable.getNames().nextElement().toString());
        }

    }
    
    @Test
    public void testExtractFromDistinguishedName () throws InvalidNameException {
        String dn = "CN=HOSTNAME , OU=devel, OU= Büro hinten, O=SOS, L=Berlin, C=DE";
        LdapName ldapNameDN = new LdapName(dn);
        for (Rdn rdn : ldapNameDN.getRdns()) {
            LOGGER.trace(rdn.getType() + " : " + rdn.getValue());
        }
    }

    @Test
    public void testCreateUserSubjectDN () throws InvalidNameException, IOException, CertificateException {
        String dn = "DNQ=SP SOS CA, CN=HOSTNAME, OU=devel, OU= Büro hinten, O=SOS, L=Berlin, C=DE";
        String certificateString = new String(Files.readAllBytes(Paths.get("src/test/resources/sp_root_ca.cer")), StandardCharsets.UTF_8);
        X509Certificate certificate =  KeyUtil.getX509Certificate(certificateString);
        LOGGER.trace("************************************  full DN");
        LOGGER.trace(CAUtils.createUserSubjectDN(dn, certificate, "MyAgent"));
        dn = "OU=Büro hinten, L=Berlin";
        LOGGER.trace("************************************  partial DN");
        LOGGER.trace(CAUtils.createUserSubjectDN(dn, certificate, "MyAgent"));
        LOGGER.trace("************************************  empty DN");
        LOGGER.trace(CAUtils.createUserSubjectDN("", certificate, "MyAgent"));
        LOGGER.trace("************************************  null DN");
        LOGGER.trace(CAUtils.createUserSubjectDN(null, certificate, "MyAgent"));
        dn = "DNQ=SP SOS CA, CN=HOSTNAME, OU=devel, OU= Büro hinten, O=SOS, L=Berlin, C=DE";
        LOGGER.trace("************************************  full DN alt cert null");
        LOGGER.trace(CAUtils.createUserSubjectDN(dn, null, "MyAgent"));
        dn = "DN=SP SOS CA, OU=devel, OU= Büro hinten, O=SOS, L=Berlin, C=DE";
        LOGGER.trace("************************************  partial DN(missing CN) alt cert null");
        LOGGER.trace(CAUtils.createUserSubjectDN(dn, null, "MyAgent"));
        dn = "OU=Büro hinten, L=Berlin";
        LOGGER.trace("************************************  partial DN alt cert null");
        LOGGER.trace(CAUtils.createUserSubjectDN(dn, null, "MyAgent"));
        LOGGER.trace("************************************  empty DN alt cert null");
        LOGGER.trace(CAUtils.createUserSubjectDN("", null, "MyAgent"));
        LOGGER.trace("************************************  null DN alt cert null");
        LOGGER.trace(CAUtils.createUserSubjectDN(null, null, "MyAgent"));
        LOGGER.trace("CAUtils.createUserSubjectDN(\"CN=\" + \"myTestAccount\", certificate)");
        LOGGER.trace(CAUtils.createUserSubjectDN("CN=" + "myTestAccount", certificate));
        LOGGER.trace("CAUtils.createUserSubjectDN(null, certificate, \"myTestAccount\")");
        LOGGER.trace(CAUtils.createUserSubjectDN(null, certificate, "myTestAccount"));
        LOGGER.trace(CAUtils.createUserSubjectDN("", certificate, "myTestAccount"));
        LOGGER.trace("CAUtils.createUserSubjectDN(\"DN=SP SOS CA, OU=devel, OU= Büro hinten, O=SOS, L=Berlin, ST=Berlin, C=DE\", certificate, \"myTestAccount\")");
        LOGGER.trace(CAUtils.createUserSubjectDN("DN=SP SOS CA, OU=devel, OU= Büro hinten, O=SOS, L=Berlin, ST=Berlin, C=DE", certificate, "myTestAccount"));
        LOGGER.trace("CAUtils.createUserSubjectDN(\"DN=SP SOS CA, CN=AnotherAccount, OU=devel, OU=Büro hinten, O=SOS, L=Berlin, ST=Berlin, C=DE\", certificate, \"myTestAccount\")");
        LOGGER.trace(CAUtils.createUserSubjectDN("DN=SP SOS CA, CN=AnotherAccount, OU=devel, OU=Büro hinten, O=SOS, L=Berlin, ST=Berlin, C=DE", certificate, "myTestAccount"));
        LOGGER.trace("CAUtils.createUserSubjectDN(\"DN=SP SOS CA, CN=AnotherAccount, OU=devel, OU=Büro hinten, O=SOS, L=Berlin, ST=Berlin, C=DE\", null, \"myTestAccount\")");
        LOGGER.trace(CAUtils.createUserSubjectDN("DN=SP SOS CA, CN=AnotherAccount, OU=devel, OU=Büro hinten, O=SOS, L=Berlin, ST=Berlin, C=DE", null, "myTestAccount"));
    }

    private void exportCertificateBundle(String rootKey, String rootCert, String userCertificateRequest, String userKey, String userCert, String filename)
            throws IOException {
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        Boolean notExists = Files.notExists(Paths.get("target").resolve("created_test_files"));
        if (notExists) {
            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
            LOGGER.trace("subfolder \"created_test_files\" created in target folder.");
        }
        out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(filename));
        zipOut = new ZipOutputStream(new BufferedOutputStream(out), StandardCharsets.UTF_8);
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
    
}
