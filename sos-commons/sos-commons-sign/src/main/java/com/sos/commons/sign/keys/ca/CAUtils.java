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
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public abstract class CAUtils {

    public static Certificate createSelfSignedRootCertificate(String algorithm, KeyPair keyPair, String subjectDN, boolean operatesAsCA,
            boolean critical) throws OperatorCreationException, CertificateException, IOException, NoSuchAlgorithmException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
        Date startDate = Date.from(Instant.now());
        // X500Name subjectDNX500Name = new X500Name(subjectDN);
        X500Name subjectDNX500Name = createX500NameWithReverseOrder(subjectDN);
        
        // Using the current timestamp as the certificate serial number
        BigInteger certSerialNumber = new BigInteger(Long.toString(startDate.getTime()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        // 1 Year validity
        calendar.add(Calendar.YEAR, 5);
        Date endDate = calendar.getTime();
        // Use appropriate signature algorithm based on your keyPair algorithm.
        ContentSigner contentSigner = new JcaContentSignerBuilder(algorithm).build(keyPair.getPrivate());
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

    public static PKCS10CertificationRequest createCSR(String algorithm, KeyPair issuerKeyPair, KeyPair signerKeyPair, String userDN)
            throws CertException {
        try {
            PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(createX500NameWithReverseOrder(userDN),
                    issuerKeyPair.getPublic());
            JcaContentSignerBuilder jcaBuilder = new JcaContentSignerBuilder(algorithm);
            jcaBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            ContentSigner contentSigner = jcaBuilder.build(issuerKeyPair.getPrivate());
            PKCS10CertificationRequest certificationRequest = builder.build(contentSigner);
            return certificationRequest;
        } catch (Exception e) {
            throw new CertException("createCSR failed", e);
        }
    }

    public static X509Certificate signCSR(String algorithm, PrivateKey caPrivateKey, KeyPair subjectKeyPair, PKCS10CertificationRequest csr,
            X509Certificate rootCaCert, String subjectAlternativeName) throws OperatorCreationException, IOException, CertificateException,
            NoSuchAlgorithmException {
        return signCSR(algorithm, caPrivateKey, subjectKeyPair, csr, rootCaCert, subjectAlternativeName, null);
    }

    public static X509Certificate signCSR(String algorithm, PrivateKey caPrivateKey, KeyPair subjectKeyPair, PKCS10CertificationRequest csr,
            X509Certificate rootCaCert, String subjectAlternativeName, Date validUntil) throws OperatorCreationException, IOException,
            CertificateException, NoSuchAlgorithmException {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithm);
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        X500Name issuerName = X500Name.getInstance(rootCaCert.getSubjectX500Principal().getEncoded());
//        X500Name subName = CaUtils.getX509Name(subject);
        Date validFrom = Date.from(Instant.now());
        // Using the current timestamp as the certificate serial number
        BigInteger certSerialNumber = new BigInteger(Long.toString(validFrom.getTime()));
        if (validUntil == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(validFrom);
            // 2 Year validity
            calendar.add(Calendar.YEAR, 2);
            validUntil = calendar.getTime();
        }
        X509v3CertificateBuilder certgen = new X509v3CertificateBuilder(issuerName, certSerialNumber, validFrom, validUntil, csr.getSubject(),
                csr.getSubjectPublicKeyInfo());
        // 2.5.29.17 is the oid value for Subject Alternative Name [SAN]
        // new ASN1ObjectIdentifier("2.5.29.17")
        if (subjectAlternativeName != null && !subjectAlternativeName.isEmpty()) {
            if (subjectAlternativeName.contains(",")) {
                String[] sans = subjectAlternativeName.split(",");
                List<GeneralName> generalNames = new ArrayList<>();
                for (int i = 0; i < sans.length; i++) {
                    GeneralName altName = new GeneralName(GeneralName.dNSName, sans[i].trim());
                    generalNames.add(altName);
                }
                GeneralNames san = new GeneralNames(generalNames.toArray(new GeneralName[0]));
                certgen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), false, san);
            } else {
                GeneralName altName = new GeneralName(GeneralName.dNSName, subjectAlternativeName);
                GeneralNames san = new GeneralNames(altName);
                certgen.addExtension(new ASN1ObjectIdentifier("2.5.29.17"), false, san);
            }
        }
        // client and server authentication
        certgen.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        certgen.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.nonRepudiation | KeyUsage.keyAgreement | KeyUsage.digitalSignature
                | KeyUsage.dataEncipherment));
        certgen.addExtension(MiscObjectIdentifiers.netscapeCertType, false, new NetscapeCertType(NetscapeCertType.sslClient
                | NetscapeCertType.sslServer));
        certgen.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(new KeyPurposeId[] { KeyPurposeId.id_kp_serverAuth,
                KeyPurposeId.id_kp_clientAuth, KeyPurposeId.id_kp_codeSigning }));
        
        AuthorityKeyIdentifier authorityKeyIdentifier = new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(rootCaCert.getPublicKey(),
                rootCaCert.getSubjectX500Principal(), certSerialNumber);
        certgen.addExtension(Extension.authorityKeyIdentifier, false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = new JcaX509ExtensionUtils().createSubjectKeyIdentifier(subjectKeyPair.getPublic());
        certgen.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);
        
        ContentSigner signer = new JcaContentSignerBuilder(algorithm).build(caPrivateKey);
        return new JcaX509CertificateConverter().getCertificate(certgen.build(signer));
    }

    public static String createRootSubjectDN(String dnQualifier, String commonName, String organizationUnit, String organization,
            String countryCode) {
        final String separator = ",";
        StringBuilder rootSubjectDN = new StringBuilder();
        if (dnQualifier != null) {
            rootSubjectDN.append("DN=").append(dnQualifier).append(separator);
        }
        if (commonName != null) {
            if (dnQualifier == null) {
                rootSubjectDN.append("DN=").append(commonName).append(separator).append("CN=").append(commonName).append(separator);
            } else {
                rootSubjectDN.append("CN=").append(commonName).append(separator);
            }
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

    public static String createUserSubjectDN(String dnQualifier, String commonName, String organizationUnit, String organization, String locality,
            String state, String countryCode) {
        final String separator = ",";
        StringBuilder userSubjectDN = new StringBuilder();
        if (dnQualifier != null) {
            userSubjectDN.append("DN=").append(dnQualifier).append(separator);
        }
        if (commonName != null) {
            if (dnQualifier == null) {
                userSubjectDN.append("DN=").append(commonName).append(separator).append("CN=").append(commonName).append(separator);
            } else {
                userSubjectDN.append("CN=").append(commonName).append(separator);
            }
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

    public static String createUserSubjectDN(String dnQualifier, String commonName, String[] organizationUnits, String organization, String locality,
            String state, String countryCode) {
        final String separator = ",";
        StringBuilder userSubjectDN = new StringBuilder();
        if (dnQualifier != null) {
            userSubjectDN.append("DN=").append(dnQualifier).append(separator);
        }
        if (commonName != null) {
            if (dnQualifier == null) {
                userSubjectDN.append("DN=").append(commonName);
            }
            userSubjectDN.append(separator).append("CN=").append(commonName);
        }
        if (organizationUnits != null && organizationUnits.length > 0) {
            for (int i = organizationUnits.length; i > 0; i--) {
                userSubjectDN.append(separator).append("OU=").append(organizationUnits[i-1]);
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

    public static String createUserSubjectDN(String dn, X509Certificate alternativeSource) throws InvalidNameException {
        return createUserSubjectDN(dn, alternativeSource, null);
    }

    public static String createUserSubjectDN(String dn, X509Certificate alternativeSource, String defaultTargetHostname) throws InvalidNameException {
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
        String dnQualifier = null;
        String commonName = null;
        List<String> organizationUnits = null;
        String organization = null;
        String countryCode = null;
        String locality = null;
        String state = null;
        if (dn != null && (dn.contains("DN=") || dn.contains("DNQ="))) {
            if(dn.contains("DNQ=")) {
                dnQualifier = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("DNQ")).map(rdn -> rdn.getValue().toString())
                        .collect(Collectors.toList()).get(0);
            } else if (dn.contains("DN=")) {
                dnQualifier = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("DN")).map(rdn -> rdn.getValue().toString())
                        .collect(Collectors.toList()).get(0);
            }
        }
        if (altSourceIssuerDN != null) {
            dnQualifier = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).findFirst().get().getValue()
                    .toString();
        }
        if (dn != null && dn.contains("CN=")) {
            commonName = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).map(rdn -> rdn.getValue().toString()).collect(
                    Collectors.toList()).get(0);
        } else if (defaultTargetHostname != null) {
            commonName = defaultTargetHostname;
        }
        if (dn != null && dn.contains("OU=")) {
            organizationUnits = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("OU")).map(rdn -> rdn.getValue().toString())
                    .collect(Collectors.toList());
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("OU=")) {
            organizationUnits = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("OU")).map(rdn -> rdn.getValue()
                    .toString()).collect(Collectors.toList());
        }
        if (dn != null && dn.contains("O=")) {
            organization = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("O")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("O=")) {
            organization = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("O")).findFirst().get().getValue()
                    .toString();
        }
        if (dn != null && dn.contains("C=")) {
            countryCode = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("C")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("C=")) {
            countryCode = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("C")).findFirst().get().getValue()
                    .toString();
        }
        if (dn != null && dn.contains("L=")) {
            locality = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("L")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("L=")) {
            locality = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("L")).findFirst().get().getValue()
                    .toString();
        }
        if (dn != null && dn.contains("ST=")) {
            state = dnName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("ST")).findFirst().get().getValue().toString();
        } else if (alternativeSource != null && alternativeSource.getIssuerDN().getName().contains("ST=")) {
            state = altSourceIssuerDN.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("ST")).findFirst().get().getValue().toString();
        }
        if (organizationUnits != null) {
            return createUserSubjectDN(dnQualifier, commonName, organizationUnits.toArray(new String[0]), organization, locality, state, countryCode);
        } else {
            return createUserSubjectDN(dnQualifier, commonName, new String[0], organization, locality, state, countryCode);
        }
    }

    private static X500Name createX500NameWithReverseOrder(String dn) {
        String[] RDN = dn.split(",");
        StringBuffer buf = new StringBuffer(dn.length());
        for (int i = RDN.length - 1; i >= 0; i--) {
            if (i != RDN.length - 1) {
                buf.append(',');
            }
            buf.append(RDN[i]);
        }
        return new X500Name(buf.toString());
    }
}
