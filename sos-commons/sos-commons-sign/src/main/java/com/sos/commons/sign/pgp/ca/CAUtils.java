package com.sos.commons.sign.pgp.ca;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcECContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import com.sos.commons.sign.pgp.SOSPGPConstants;

public abstract class CAUtils {
    
    public static Certificate createSelfSignedCertificate(String algorythm, KeyPair keyPair, String subjectDN, boolean operatesAsCA, boolean critical)
            throws OperatorCreationException, CertificateException, IOException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
        Date startDate = Date.from(Instant.now());
        X500Name subjectDNX500Name = new X500Name(subjectDN);
        // Using the current timestamp as the certificate serial number
        BigInteger certSerialNumber = new BigInteger(Long.toString(startDate.getTime()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        // 1 Year validity
        calendar.add(Calendar.YEAR, 5);
        Date endDate = calendar.getTime();
        // Use appropriate signature algorithm based on your keyPair algorithm.
        ContentSigner contentSigner = new JcaContentSignerBuilder(algorythm)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPair.getPrivate());
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(subjectDNX500Name, certSerialNumber, startDate, endDate,
                subjectDNX500Name, keyPair.getPublic());
        // Extension: Basic Constraint
        // <-- true for CA, false for EndEntity
        BasicConstraints basicConstraints = new BasicConstraints(operatesAsCA);
        // Basic Constraints is usually marked as critical.
        // 2.5.29.19 is the oid value for BasicConstraints to indicate if the subject may act as a CA,
        // with the certified public key being used to verify certificate signatures
        // the boolean value sets the criticality
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), critical, basicConstraints);
        return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
    }

    public static PKCS10CertificationRequest createCSR(String algorythm, KeyPair keyPair, String userDN) throws CertException {
        try {
            PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(new X500Name(userDN), keyPair.getPublic());
            JcaContentSignerBuilder jcaBuilder = new JcaContentSignerBuilder(algorythm);
            jcaBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            ContentSigner contentSigner = jcaBuilder.build(keyPair.getPrivate());
            PKCS10CertificationRequest certificationRequest = builder.build(contentSigner);
            return certificationRequest;
        } catch (Exception e) {
            throw new CertException("createCSR failed", e);
        } 
    }
    
    public static X509Certificate signCSR(String algorythm, PrivateKey privateKey, PKCS10CertificationRequest csr, X509Certificate rootCa,
            String subjectAlternativeName) throws OperatorCreationException, IOException, CertificateException {
      AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorythm);
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
      X500Name issuer = X500Name.getInstance(rootCa.getSubjectX500Principal().getEncoded());
      Date startDate = Date.from(Instant.now());
      // Using the current timestamp as the certificate serial number
      BigInteger certSerialNumber = new BigInteger(Long.toString(startDate.getTime()));
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(startDate);
      // 1 Year validity
      calendar.add(Calendar.YEAR, 2);
      Date endDate = calendar.getTime();
      X509v3CertificateBuilder certgen = 
              new X509v3CertificateBuilder(issuer, certSerialNumber, startDate, endDate, csr.getSubject(), csr.getSubjectPublicKeyInfo());
      // 2.5.29.17 is the oid value for Subject Alternative Name [SAN] 
      // new ASN1ObjectIdentifier("2.5.29.17")
      GeneralName altName = new GeneralName(GeneralName.dNSName, subjectAlternativeName);
      GeneralNames san = new GeneralNames(altName);
      certgen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), false, san);
      ContentSigner signer = null;
      if (algorythm.equals(SOSPGPConstants.DEFAULT_ALGORYTHM)) {
          signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey( privateKey.getEncoded()));
      } else {
          signer = new BcECContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey( privateKey.getEncoded()));
      }
      X509CertificateHolder holder = certgen.build(signer);
      return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(holder);
    }
    
//    public static X509Certificate signECDSACSR(String algorythm, PrivateKey privateKey, PKCS10CertificationRequest csr, X509Certificate rootCa,
//            String subjectAlternativeName) throws OperatorCreationException, IOException, CertificateException {
//      AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorythm);
//      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
//      X500Name issuer = X500Name.getInstance(rootCa.getSubjectX500Principal().getEncoded());
//      Date startDate = Date.from(Instant.now());
//      // Using the current timestamp as the certificate serial number
//      BigInteger certSerialNumber = new BigInteger(Long.toString(startDate.getTime()));
//      Calendar calendar = Calendar.getInstance();
//      calendar.setTime(startDate);
//      // 1 Year validity
//      calendar.add(Calendar.YEAR, 2);
//      Date endDate = calendar.getTime();
//      X509v3CertificateBuilder certgen = 
//              new X509v3CertificateBuilder(issuer, certSerialNumber, startDate, endDate, csr.getSubject(), csr.getSubjectPublicKeyInfo());
//      // 2.5.29.17 is the oid value for Subject Alternative Name [SAN] 
//      // new ASN1ObjectIdentifier("2.5.29.17")
//      GeneralName altName = new GeneralName(GeneralName.dNSName, subjectAlternativeName);
//      GeneralNames san = new GeneralNames(altName);
//      certgen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), false, san);
//      ContentSigner signer = new BcECContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey( privateKey.getEncoded()));
//      X509CertificateHolder holder = certgen.build(signer);
//      return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(holder);
//    }
//    
    public static String createRootSubjectDN (String commonName, String organizationUnit, String organization, String countryCode) {
        final String separator = ",";
        StringBuilder rootSubjectDN = new StringBuilder();
        rootSubjectDN.append("CN=").append(commonName).append(separator);
        rootSubjectDN.append("OU=").append(organizationUnit).append(separator);
        rootSubjectDN.append("O=").append(organization).append(separator);
        rootSubjectDN.append("C=").append(countryCode);
        // ST: STATE hinzufügen
        return rootSubjectDN.toString();
    }
    
    public static String createUserSubjectDN (String commonName, String organizationUnit, String subOrganizationUnit, String organization,
            String location, String state, String countryCode) {
        final String separator = ",";
        StringBuilder userSubjectDN = new StringBuilder();
        userSubjectDN.append("CN=").append(commonName).append(separator);
        userSubjectDN.append("OU=").append(organizationUnit).append(separator);
        userSubjectDN.append("OU=").append(subOrganizationUnit).append(separator);
        userSubjectDN.append("O=").append(organization).append(separator);
        userSubjectDN.append("L=").append(location).append(separator);
        userSubjectDN.append("ST=").append(state).append(separator);
        userSubjectDN.append("C=").append(countryCode);
        return userSubjectDN.toString();
    }
    
}
