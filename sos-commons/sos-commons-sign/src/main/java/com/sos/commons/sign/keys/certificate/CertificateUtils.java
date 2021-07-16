package com.sos.commons.sign.keys.certificate;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.SOSKeyConstants;

public abstract class CertificateUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CertificateUtils.class);
	
	// Access restriction: The type 'X500Name' is not API (restriction on required library 'rt.jar')
	@SuppressWarnings("restriction")
	public static String getCommonName(X509Certificate cert) throws IOException {
		return ((sun.security.x509.X500Name)cert.getSubjectDN()).getCommonName();
	}

	public static void logCertificateInfo(X509Certificate certificate) {
		if (certificate != null) {
			try {
				certificate.checkValidity();
				LOGGER.info("X509 certificate is valid.");
			} catch (CertificateExpiredException e) {
				LOGGER.info("X509 certificate has expired.");
			} catch (CertificateNotYetValidException e) {
				LOGGER.info("X509 certificate not yet valid.");
			}
			LOGGER.info("x509 certificate valid from - " + certificate.getNotBefore() + " - until - " + certificate.getNotAfter());
			String licenceCheckMessage = "";
			// not public API
//			if (CertificateUtils.checkLicence(certificate)) {
//				licenceCheckMessage = "SOS Licence is valid.";
//			} else {
//				licenceCheckMessage = "SOS Licence is not valid.";
//			}
			if(!licenceCheckMessage.isEmpty()) {
				LOGGER.info(licenceCheckMessage);
			}
			LOGGER.info("Subject DN: " + certificate.getSubjectDN().getName());
            try {
				LOGGER.info("CN: " + CertificateUtils.getCommonName(certificate));
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			String subjectPrincipalName = certificate.getSubjectX500Principal().getName();
			LOGGER.info("Issuer DN: " + certificate.getIssuerDN().getName());
			String issuerPrincipalName = certificate.getIssuerX500Principal().getName();
			LOGGER.info("Version: v" + certificate.getVersion());
			LOGGER.info("Type: " + certificate.getType());
			LOGGER.info("Serial Number: " + certificate.getSerialNumber());
			LOGGER.info("signature Algorithm Name: " + certificate.getSigAlgName());
			LOGGER.info("signature Algorithm OID: " + certificate.getSigAlgOID());
			Set<String> criticalExtensions = certificate.getCriticalExtensionOIDs();
			if (!criticalExtensions.isEmpty()) {
				LOGGER.info("Active Critical Extensions:");
				criticalExtensions.stream().forEach(item -> {
					if (SOSKeyConstants.KEY_USAGE_OID.equals(item)) {
						LOGGER.info("  Key Usage (OID: " + SOSKeyConstants.KEY_USAGE_OID + ")");
						boolean[] keyUsages = certificate.getKeyUsage();
						if (keyUsages[0]) {
							LOGGER.info("    digitalSignature        (0)");
						}
						if (keyUsages[1]) {
							LOGGER.info("    nonRepudiation          (1)");
						}
						if (keyUsages[2]) {
							LOGGER.info("    keyEncipherment         (2)");
						}
						if (keyUsages[3]) {
							LOGGER.info("    dataEncipherment        (3)");
						}
						if (keyUsages[4]) {
							LOGGER.info("    keyAgreement            (4)");
						}
						if (keyUsages[5]) {
							LOGGER.info("    keyCertSign             (5)");
						}
						if (keyUsages[6]) {
							LOGGER.info("    cRLSign                 (6)");
						}
						if (keyUsages[7]) {
							LOGGER.info("    encipherOnly            (7)");
						}
						if (keyUsages[8]) {
							LOGGER.info("    decipherOnly            (8)");
						}
					} else if (SOSKeyConstants.BASIC_CONSTRAINTS_OID.equals(item)) {
						LOGGER.info("  Basic Constraints (OID: " + SOSKeyConstants.BASIC_CONSTRAINTS_OID + ")");
						int pathLenConstraint = certificate.getBasicConstraints();
						if (pathLenConstraint != -1) {
							LOGGER.info("    is CA: true");
							LOGGER.info("    path length constraint: " + pathLenConstraint);
							if(pathLenConstraint == 0) {
								LOGGER.info("    only end entity may follow");
							} else if (pathLenConstraint == Integer.MAX_VALUE) {
								LOGGER.info("    unlimited CAs or end entities may follow");
							} else {
								LOGGER.info("    " + pathLenConstraint + " CAs may follow");
							}
						} else {
							LOGGER.info("    is CA: false");
						}
						
					}
				});
			}
			Set<String> nonCriticalExtensions = certificate.getNonCriticalExtensionOIDs();
			if(!nonCriticalExtensions.isEmpty()) {
				LOGGER.info("Active Non Critical Extensions:");
				nonCriticalExtensions.stream().forEach(item -> {
					if (SOSKeyConstants.NETSCAPE_CERTIFICATE_TYPE_OID.equals(item)) {
						LOGGER.info("  Netscape Certificate Type (OID: " + SOSKeyConstants.NETSCAPE_CERTIFICATE_TYPE_OID + ")");
					} else if (SOSKeyConstants.SUBJECT_ALTERNATIVE_NAME_OID.equals(item)) {
						LOGGER.info("  Subject Alternative Name (OID: " + SOSKeyConstants.SUBJECT_ALTERNATIVE_NAME_OID + ")");
						try {
							Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
							for (List<?> entry : subjectAlternativeNames) {
								int key = (Integer)entry.get(0);
								String keyName = "";
								switch(key) {
								case 0:
									keyName = "otherName";
									break;
								case 1:
									keyName = "rfc822Name";
									break;
								case 2:
									keyName = "dNSName";
									break;
								case 3:
									keyName = "x400Address";
									break;
								case 4:
									keyName = "directoryName";
									break;
								case 5:
									keyName = "ediPartyName";
									break;
								case 6:
									keyName = "uniformResourceIdentifier";
									break;
								case 7:
									keyName = "iPAddress";
									break;
								case 8:
									keyName = "registeredID";
									break;
								}
								String value = (String)entry.get(1);
								LOGGER.info("    " + keyName + " : " + value);
							}
						} catch (CertificateParsingException e) {
							LOGGER.error(e.getMessage(), e);
						}
						
					} else if (SOSKeyConstants.EXTENDED_KEY_USAGE_OID.equals(item)) {
						LOGGER.info("  Extended Key Usage (OID: " + SOSKeyConstants.EXTENDED_KEY_USAGE_OID + ")");
						try {
							List<String> extendedKeyUsage = certificate.getExtendedKeyUsage();
							extendedKeyUsage.stream().forEach(extKexUsage -> {
								if (SOSKeyConstants.CLIENT_AUTH_OID.equals(extKexUsage)) {
									LOGGER.info("    'clientAuth' (OID: " + SOSKeyConstants.CLIENT_AUTH_OID + ")");
								} else if (SOSKeyConstants.SERVER_AUTH_OID.equals(extKexUsage)) {
									LOGGER.info("    'serverAuth' (OID: " + SOSKeyConstants.SERVER_AUTH_OID + ")");
								}
							});
						} catch (CertificateParsingException e) {
							LOGGER.error(e.getMessage(), e);
						}
					} else if (SOSKeyConstants.SUBJECT_KEY_IDENTIFIER_OID.equals(item)) {
						LOGGER.info("  Subject Key Identifier (OID: " + SOSKeyConstants.SUBJECT_KEY_IDENTIFIER_OID + ")");
					} else if (SOSKeyConstants.AUTHORITY_KEY_IDENTIFIER_OID.equals(item)) {
						LOGGER.info("  Authority Key Identifier (OID: " + SOSKeyConstants.AUTHORITY_KEY_IDENTIFIER_OID + ")");
					}
				});
			}
			LOGGER.info("");
		}
	}

	public static String asPEMString(X509Certificate cert) throws IOException, CertificateEncodingException {
	    String lineSeparator = "\n";
	    Base64.Encoder encoder = Base64.getMimeEncoder(64, lineSeparator.getBytes());
	    String encodedCert = new String(encoder.encode(cert.getEncoded()));
	    // prettify certificate string
	    return SOSKeyConstants.CERTIFICATE_HEADER + lineSeparator + encodedCert + lineSeparator + SOSKeyConstants.CERTIFICATE_FOOTER;
	}
	
}
