package com.sos.jitl.common;

import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;

public class HttpClientTests {

	private static final String CLIENT_AUTH_OID = "1.3.6.1.5.5.7.3.2";
	private static final String SERVER_AUTH_OID = "1.3.6.1.5.5.7.3.1";
	private static final String KEY_USAGE_OID = "2.5.29.15";
	private static final String NETSCAPE_CERTIFICATE_TYPE_OID = "2.16.840.1.113730.1.1";
	private static final String SUBJECT_ALTERNATIVE_NAME_OID = "2.5.29.17";
	private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";

	private static final String KEYSTORE_PATH  = "C:/sp/devel/js7/keys/sp-teststore.p12";
	private static final String TRUSTORE_PATH = "";
//	private static final String fingerprint = "69:3E:C0:BD:89:28:8F:F8:71:9E:1E:22:BB:31:FC:38:15:55:D3:7B";
	private static final String ALIAS = "sp";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientTests.class);

	@Ignore
	@Test
	public void testReadKeyStore() {
		
		KeyStore keyStore = null;
		try {
			keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeyStoreType.PKCS12);
			X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS);
			if (cert != null) {
				try {
					cert.checkValidity();
					LOGGER.info("X509 certificate is valid.");
				} catch (CertificateExpiredException e) {
					LOGGER.info("X509 certificate has expired.");
				} catch (CertificateNotYetValidException e) {
					LOGGER.info("X509 certificate not yet valid.");
				}
				LOGGER.info("x509 certificate valid from - " + cert.getNotBefore() + " - until - " + cert.getNotAfter());
				LOGGER.info("Subject DN: " + cert.getSubjectDN().getName());
                LOGGER.info("CN: " + ((sun.security.x509.X500Name)cert.getSubjectDN()).getCommonName());
				String subjectPrincipalName = cert.getSubjectX500Principal().getName();
				LOGGER.info("Issuer DN: " + cert.getIssuerDN().getName());
				String issuerPrincipalName = cert.getIssuerX500Principal().getName();
				LOGGER.info("Version: v" + cert.getVersion());
				LOGGER.info("Type: " + cert.getType());
				LOGGER.info("Serial Number: " + cert.getSerialNumber());
				LOGGER.info("signature Algorithm Name: " + cert.getSigAlgName());
				LOGGER.info("signature Algorithm OID: " + cert.getSigAlgOID());
				Set<String> criticalExtensions = cert.getCriticalExtensionOIDs();
				criticalExtensions.stream().forEach(item -> {
					if (KEY_USAGE_OID.equals(item)) {
						LOGGER.info("critical extension - Key Usage - activated");
					}
				});
				boolean[] keyUsages = cert.getKeyUsage();
				LOGGER.info("Active Key Usages are:");
				if (keyUsages[0]) {
					LOGGER.info("  digitalSignature        (0)");
				}
				if (keyUsages[1]) {
					LOGGER.info("  nonRepudiation          (1)");
				}
				if (keyUsages[2]) {
					LOGGER.info("  keyEncipherment         (2)");
				}
				if (keyUsages[3]) {
					LOGGER.info("  dataEncipherment        (3)");
				}
				if (keyUsages[4]) {
					LOGGER.info("  keyAgreement            (4)");
				}
				if (keyUsages[5]) {
					LOGGER.info("  keyCertSign             (5)");
				}
				if (keyUsages[6]) {
					LOGGER.info("  cRLSign                 (6)");
				}
				if (keyUsages[7]) {
					LOGGER.info("  encipherOnly            (7)");
				}
				if (keyUsages[8]) {
					LOGGER.info("  decipherOnly            (8)");
				}
				Set<String> nonCriticalExtensions = cert.getNonCriticalExtensionOIDs();
				nonCriticalExtensions.stream().forEach(item -> {
					if (NETSCAPE_CERTIFICATE_TYPE_OID.equals(item)) {
						LOGGER.info("non critical extension - Netscape Certificate Type - activated");
					} else if (SUBJECT_ALTERNATIVE_NAME_OID.equals(item)) {
						LOGGER.info("non critical extension - Subject Alternative Name - activated");
						LOGGER.info("Subject Alternative Names are: ");
						try {
							Collection<List<?>> subjectAlternativeNames = cert.getSubjectAlternativeNames();
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
								LOGGER.info("  " + keyName + " : " + value + " -");
							}
						} catch (CertificateParsingException e) {
							LOGGER.error(e.getMessage(), e);
						}
						
					} else if (EXTENDED_KEY_USAGE_OID.equals(item)) {
						LOGGER.info("non critical extension - Extended Key Usage - activated");
						LOGGER.info("Extended Key Usages are: ");
						try {
							List<String> extendedKeyUsage = cert.getExtendedKeyUsage();
							extendedKeyUsage.stream().forEach(extKexUsage -> {
								if (CLIENT_AUTH_OID.equals(extKexUsage)) {
									LOGGER.info("  'clientAuth' active");
								} else if (SERVER_AUTH_OID.equals(extKexUsage)) {
									LOGGER.info("  'serverAuth' active");
								}
							});
						} catch (CertificateParsingException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
