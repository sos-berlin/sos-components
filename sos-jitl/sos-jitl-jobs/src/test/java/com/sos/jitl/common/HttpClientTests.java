package com.sos.jitl.common;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
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

import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;

public class HttpClientTests {


	private static final String CERTSTORE_PATH  = "C:/sp/devel/js7/LicenceCheck/icloud-certstore.p12";
	private static final String KEYSTORE_PATH  = "C:/sp/devel/js7/keys/sp-keystore.p12";
	private static final String TRUSTORE_PATH = "C:/sp/devel/js7/keys/sp-truststore.p12";
	private static final String ALIAS_SP = "sp";
	private static final String ALIAS_ICLOUD = "icloud";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientTests.class);

	@Ignore
	@Test
	public void testReadKeyStore() throws Exception {
		KeyStore keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeyStoreType.PKCS12);
		assertNotNull(keyStore);
	}

	@Ignore
	@Test
	public void testReadCertificateFromKeyStore() {
		
		KeyStore keyStore = null;
		try {
			keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeyStoreType.PKCS12);
			X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS_SP);
			assertNotNull(cert);
			CertificateUtils.logCertificateInfo(cert);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Ignore
	@Test
	public void testCheckLicenceValid() {
		KeyStore keyStore = null;
		try {
			keyStore = KeyStoreUtil.readKeyStore(CERTSTORE_PATH, KeyStoreType.PKCS12);
			X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS_ICLOUD);
			assertNotNull(cert);
			// not public API
//			assertTrue(CertificateUtils.checkLicence(cert));
			CertificateUtils.logCertificateInfo(cert);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Ignore
	@Test
	public void testCheckLicenceInvalid() {
		KeyStore keyStore = null;
		try {
			keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeyStoreType.PKCS12);
			X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS_SP);
			assertNotNull(cert);
			// not public API
//			assertTrue(CertificateUtils.checkLicence(cert));
			CertificateUtils.logCertificateInfo(cert);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	@Ignore
	@Test
	public void testHttpClient () {
		KeyStore keyStore = null;
		KeyStore truststore = null;
		try {
			keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeyStoreType.PKCS12);
			truststore = KeyStoreUtil.readTrustStore(TRUSTORE_PATH, KeyStoreType.PKCS12);
			SOSRestApiClient restApiClient = new SOSRestApiClient();
//			restApiClient.setAutoCloseHttpClient(true);
			restApiClient.setSSLContext(keyStore, "".toCharArray(), truststore);
			restApiClient.setBasicAuthorization("cm9vdDpyb290");
			String response = restApiClient.postRestService(URI.create("https://joc-2-0-secondary.sos:7543/joc/api/authentication/login"), null);
			LOGGER.info(response);
			assertNotNull(response);
			String accessToken = null;
			if (response.contains("X-Access-Token")) {
				int indexTokenStart = response.indexOf("X-Access-Token");
				int indexAccesTokenEnd = indexTokenStart + 1;
			}
			if (response.contains("accessToken")) {
				int indexTokenStart = response.indexOf("accessToken");
				int indexAccessTokenValue = indexTokenStart + "accessToken".length() + 3;
				String substring = response.substring(indexAccessTokenValue);
				int indexSubstring = substring.indexOf('"');
				accessToken = substring.substring(0, indexSubstring);
				LOGGER.info("accessToken: " + accessToken);
			}
			if (accessToken != null) {
				restApiClient.addHeader("X-Access-Token", accessToken);
				response = restApiClient.postRestService(URI.create("https://joc-2-0-secondary.sos:7543/joc/api/authentication/logout"), null);
				LOGGER.info(response);
				assertNotNull(response);
			}
			restApiClient.closeHttpClient();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Ignore
	@Test
	public void testReadCertificateChainFromKeyStore() {
		KeyStore keyStore = null;
		try {
			keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeyStoreType.PKCS12);
			Certificate[] certificateChain = keyStore.getCertificateChain(ALIAS_SP);
			if (certificateChain != null) {
				int count = 1;
				for (Certificate certificate : certificateChain) {
					LOGGER.info("Certificate (" + count++ + ") CN: " + CertificateUtils.getCommonName((X509Certificate)certificate));
					CertificateUtils.logCertificateInfo(((X509Certificate)certificate));
				}
			} else {
				LOGGER.info("No certificate chain present.");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
}
