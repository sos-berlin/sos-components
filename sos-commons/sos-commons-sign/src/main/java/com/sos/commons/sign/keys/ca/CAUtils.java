package com.sos.commons.sign.keys.ca;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
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

import com.sos.commons.sign.keys.SOSKeyConstants;

public abstract class CAUtils {
    
    public static Certificate createSelfSignedRootCertificate(String algorithm, KeyPair keyPair, String subjectDN, boolean operatesAsCA, boolean critical)
            throws OperatorCreationException, CertificateException, IOException, NoSuchAlgorithmException {
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
        ContentSigner contentSigner = new JcaContentSignerBuilder(algorithm)
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
        certBuilder.addExtension(Extension.basicConstraints, critical, basicConstraints);
        certBuilder.addExtension(Extension.keyUsage, critical, new KeyUsage(KeyUsage.cRLSign | KeyUsage.digitalSignature | KeyUsage.keyCertSign));
        AuthorityKeyIdentifier authorityKeyIdentifier = new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = new JcaX509ExtensionUtils().createSubjectKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);
        return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
    }

    public static PKCS10CertificationRequest createCSR(String algorithm, KeyPair keyPair, String userDN) throws CertException {
        try {
            PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(new X500Name(userDN), keyPair.getPublic());
            JcaContentSignerBuilder jcaBuilder = new JcaContentSignerBuilder(algorithm);
            jcaBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            ContentSigner contentSigner = jcaBuilder.build(keyPair.getPrivate());
            PKCS10CertificationRequest certificationRequest = builder.build(contentSigner);
            return certificationRequest;
        } catch (Exception e) {
            throw new CertException("createCSR failed", e);
        } 
    }
    
    public static X509Certificate signCSR(String algorithm, PrivateKey privateKey, PKCS10CertificationRequest csr, X509Certificate rootCa,
            String subjectAlternativeName) throws OperatorCreationException, IOException, CertificateException, NoSuchAlgorithmException {
      AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithm);
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
      X500Name issuer = X500Name.getInstance(rootCa.getSubjectX500Principal().getEncoded());
      Date startDate = Date.from(Instant.now());
      // Using the current timestamp as the certificate serial number
      BigInteger certSerialNumber = new BigInteger(Long.toString(startDate.getTime()));
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(startDate);
      // 2 Year validity
      calendar.add(Calendar.YEAR, 2);
      Date endDate = calendar.getTime();
      X509v3CertificateBuilder certgen = 
              new X509v3CertificateBuilder(issuer, certSerialNumber, startDate, endDate, csr.getSubject(), csr.getSubjectPublicKeyInfo());
      // 2.5.29.17 is the oid value for Subject Alternative Name [SAN] 
      // new ASN1ObjectIdentifier("2.5.29.17")
      if (subjectAlternativeName != null && !subjectAlternativeName.isEmpty()) {
          if (subjectAlternativeName.contains(",")) {
              String[] sans = subjectAlternativeName.split(",");
              List<GeneralName> generalNames = new ArrayList<>();
              for (int i=0; i < sans.length; i++) {
                  GeneralName altName = new GeneralName(GeneralName.dNSName, sans[i].trim());
                  generalNames.add(altName);
              }
              GeneralNames san = new GeneralNames(generalNames.toArray(new GeneralName [0]));
              certgen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), false, san);
          } else {
              GeneralName altName = new GeneralName(GeneralName.dNSName, subjectAlternativeName);
              GeneralNames san = new GeneralNames(altName);
              certgen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), false, san);
          }
      }

      // client and server authentication
      certgen.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
      certgen.addExtension(Extension.keyUsage, false, 
              new KeyUsage(KeyUsage.nonRepudiation | KeyUsage.keyAgreement | KeyUsage.digitalSignature | KeyUsage.dataEncipherment));
      certgen.addExtension(MiscObjectIdentifiers.netscapeCertType, false, 
              new NetscapeCertType(NetscapeCertType.sslClient | NetscapeCertType.sslServer));
      certgen.addExtension(Extension.extendedKeyUsage, true, 
              new ExtendedKeyUsage(new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth}));
      AuthorityKeyIdentifier authorityKeyIdentifier = new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(rootCa.getPublicKey());
      certgen.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);

      ContentSigner signer = null;
      if (algorithm.equals(SOSKeyConstants.RSA_SIGNER_ALGORITHM)) {
          signer = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey( privateKey.getEncoded()));
      } else {
          signer = new BcECContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey( privateKey.getEncoded()));
      }
      X509CertificateHolder holder = certgen.build(signer);
      return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(holder);
    }
    
    public static String createRootSubjectDN (String commonName, String organizationUnit, String organization, String countryCode) {
        final String separator = ",";
        StringBuilder rootSubjectDN = new StringBuilder();
        if (commonName != null) {
            rootSubjectDN.append("CN=").append(commonName).append(separator);
        }
        if (organizationUnit != null) {
            rootSubjectDN.append("OU=").append(organizationUnit).append(separator);
        }
        if (organization != null) {
            rootSubjectDN.append("O=").append(organization).append(separator);
        }
        if (countryCode != null) {
            rootSubjectDN.append("C=").append(countryCode);
        }
        return rootSubjectDN.toString();
    }
    
    public static String createUserSubjectDN (String commonName, String organizationUnit, String organization, String locality, String state,
            String countryCode) {
        final String separator = ",";
        StringBuilder userSubjectDN = new StringBuilder();
        if (commonName != null) {
            userSubjectDN.append("CN=").append(commonName).append(separator);
        }
        if (organizationUnit != null) {
            userSubjectDN.append("OU=").append(organizationUnit).append(separator);
        }
        if (organization != null) {
            userSubjectDN.append("O=").append(organization).append(separator);
        }
        if (locality != null) {
            userSubjectDN.append("L=").append(locality).append(separator);
        }
        if (state != null) {
            userSubjectDN.append("ST=").append(state).append(separator);
        }
        if (countryCode != null) {
            userSubjectDN.append("C=").append(countryCode);
        }
        return userSubjectDN.toString();
    }
    
    public static String createUserSubjectDN (String commonName, String[] organizationUnits, String organization, String locality, String state,
            String countryCode) {
        final String separator = ",";
        StringBuilder userSubjectDN = new StringBuilder();
        userSubjectDN.append("CN=").append(commonName);
        if (organizationUnits != null && organizationUnits.length > 0) {
            for(int i = organizationUnits.length -1; i == 0; i--) {
                userSubjectDN.append(separator).append("OU=").append(organizationUnits[i]);
            }
        }
        if (organization != null) {
            userSubjectDN.append(separator).append("O=").append(organization);
        }
        if (locality != null) {
            userSubjectDN.append(separator).append("L=").append(locality);
        }
        if (state != null) {
            userSubjectDN.append(separator).append("ST=").append(state);
        }
        if (countryCode != null) {
            userSubjectDN.append(separator).append("C=").append(countryCode);
        }
        return userSubjectDN.toString();
    }
    
    public static String createUserSubjectDN (String dn, X509Certificate alternativeSource, String targetHostname) throws InvalidNameException {
        LdapName dnName = null;
        if (dn != null) {
            dnName = new LdapName(dn);
        } else {
            dnName = new LdapName("");
        }
        LdapName altSourceIssuerDN = null;
        if (alternativeSource != null) {
            altSourceIssuerDN = new LdapName(alternativeSource.getIssuerDN().getName());
        }
        String commonName = null;
        List<String> organizationUnits = null;
        String organization = null;
        String countryCode = null;
        String locality = null;
        String state = null;
        if (dn != null && dn.contains("CN=")) {
            commonName = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).map(rdn -> rdn.getValue().toString())
                .collect(Collectors.toList()).get(0);
        } else if (targetHostname != null) {
            commonName = targetHostname;
        }
        if (dn != null && dn.contains("OU=")) {
            organizationUnits = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("OU")).map(rdn -> rdn.getValue().toString())
                .collect(Collectors.toList());
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("OU=")){
            organizationUnits = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("OU")).map(rdn -> rdn.getValue().toString())
                    .collect(Collectors.toList());
        }
        if (dn != null && dn.contains("O=")) {
            organization = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("O")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("O=")){
            organization = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("O")).findFirst().get().getValue().toString();
        }
        if (dn != null && dn.contains("C=")) {
            countryCode = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("C")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("C=")){
            countryCode = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("C")).findFirst().get().getValue().toString();
        }
        if (dn != null && dn.contains("L=")) {
            locality = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("L")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("L=")){
            locality = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("L")).findFirst().get().getValue().toString();
        }
        if (dn != null && dn.contains("ST=")) {
            state = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("ST")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("ST=")){
            state = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("ST")).findFirst().get().getValue().toString();
        }
        if (organizationUnits != null) {
            return createUserSubjectDN(commonName, organizationUnits.toArray(new String[0]), organization, locality, state, countryCode);
        } else {
            return createUserSubjectDN(commonName, new String[0], organization, locality, state, countryCode);
        }
    }
    
}
