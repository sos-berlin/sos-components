package com.sos.sign;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.pgp.sign.SignObject;
import com.sos.pgp.verify.VerifySignature;

public class KeyTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyTests.class);
	private static final String SIGNATURE_STRING = "-----BEGIN PGP SIGNATURE-----\r\n" + 
	        "Version: BCPG v1.62\r\n" + 
	        "\r\n" + 
	        "iQEcBAEBCAAGBQJejw20AAoJEC+k1WnvpnBhVCgH/1kU6+hucwo51Ftpc2j8bIeR\r\n" + 
	        "0OMZYmkmZsWu0Rjhm6eJzV8pVfGRXtYuRl/zYggORArdsDC+MQnKUOG+UvtKMNmP\r\n" + 
	        "hyeDm221bwSmqokmI3Y/jXh2kq4WRBstGBFSJy+kCEWt7TulGZd6H3GS92D/6ZC3\r\n" + 
	        "6WrwTzi2IBJlDVDQ1wS8S3yI/sLb4D4TEGn4tPWqFShYdepavTyN2OyGm8nSWBCt\r\n" + 
	        "891RIAdM8jWonwi1XdBFoXrmXpw2mxXi0fHjVAj61RyVRiovUoMcKM0wYiNyRsKq\r\n" + 
	        "9LQThLBEN7vweznuviuZJIb+7SDCfObeNTrO7RBm+/fjV/dpzEFkIxsePYTisLg=\r\n" + 
	        "=/Net\r\n" + 
	        "-----END PGP SIGNATURE-----";
	private static final String ORIGINAL_STRING = "{\"TYPE\": \"AgentRef\", \"path\": \"/test-agent-2\", \"versionId\": \"201904031529\", \"uri\":\"http://localhost:41420\"}";
	private static final String PUBLICKEY_STRING = "-----BEGIN PGP PUBLIC KEY BLOCK-----\r\n" + "\r\n"
			+ "mQENBF6NaHkBCADV+7R21JQ2PEM5u60D/AFe45k+CbNN6owO5iszLN7fc+uWmDEG\r\n"
			+ "biykEo56k6uifLDAF4jh1urM41Gu+0JOJ37v2D1Hdmd7fRIdcZez++MAk3RU7V+a\r\n"
			+ "+DpGKIJ3faws2CCBVAk3YK2ElmNgOzKYiSCEbqKdIm+Cz3AyMMUVAnyVlaeNKv+W\r\n"
			+ "r7ROimbit5ojaKY0IqGwZpHkcJKgug1MX1M6HPIPNpKuOYviB46u63uz+jwAEKvr\r\n"
			+ "EftdLcoL+8+9qQTlW2gfLOF2mRe+X6g0LG0lMJUWknWmgskrNF7tKkxXuYXRhGRb\r\n"
			+ "3HBLwGiCAHSLA2hg7cIyyVeycULIFqWdJLBFABEBAAG0GnRlc3QgPGluZm9Ac29z\r\n"
			+ "LWJlcmxpbi5jb20+iQFOBBMBCAA4FiEEuqKt+z1+Ev1dyCrML6TVae+mcGEFAl6N\r\n"
			+ "aHkCGwMFCwkIBwIGFQoJCAsCBBYCAwECHgECF4AACgkQL6TVae+mcGF3CAgAu6yj\r\n"
			+ "lR90jxzQnEYS1zWF/q4o7ic3RS5BvOoS+HtYwwac9wqfYcPIn5y1ppQczteP1iE0\r\n"
			+ "gq0mUQAC4LX031yKoKGl9AY3jrASOe1srCesMOp4fgqeyJZzS0WiVYsMdSnH0RMH\r\n"
			+ "F0Vfnag4q665nJfnMYbhUN0rNXF0nVXQLJ8AzFJe6ogEpkDwyy128XLz+NDTJZeO\r\n"
			+ "9oVpOKZ+Cn4xp6MUkuIaJOzcLfMILIn37yVV5CFknpk9I+Frtv2xCPZIREy9+nkG\r\n"
			+ "VyDuUYgMy5rAM/LNdiLM2KLKFAvZA8pdxOAph6014y5kuhDrC7jiq0au6ouohlph\r\n"
			+ "BKREtwUXw0CffcG3QLkBDQRejWh5AQgA39FpDp7Knih28kBg1PPiDk1ttnmqN+JX\r\n"
			+ "MZJiQNH8Euxl+z+TNKY9UW/GHjisOBOG48SSN+pWyJC+1CmOMxhctiJM8GuPnzBZ\r\n"
			+ "gsPKTu9Y2WSGIEpdAJ1EQ+v8eYCZqh5C4hPZjeTmims/tTs0b3/K9D9HPb+kMxp0\r\n"
			+ "rm1XfLKdUf2An/BwJjtmojFCwj7X+9iiPbaEOhU16mu/TAFmvy0aix3B4QFx/NwU\r\n"
			+ "mzDhHb41mLIzOcs+jd2BR1us0v32zuh3/g3MMsRjHgPYE4uuj1pFNuDqT74hNlyv\r\n"
			+ "VFGXTdARgY8PUrilIUYbxfITGP6TIcNPHMj0dxlF8Gqvtmt9qoIe8QARAQABiQE2\r\n"
			+ "BBgBCAAgFiEEuqKt+z1+Ev1dyCrML6TVae+mcGEFAl6NaHkCGwwACgkQL6TVae+m\r\n"
			+ "cGHH7wf+MWcNl75dhqkeAnCJjpJ5bZJx9bG+ExKWczs0KSV5JEZor9JxFnpfDFXj\r\n"
			+ "x2FNcn/OUAOy8bmmDRmCxBPKbW68joGTHSrS1BNvsIChwkmiqSowHPlOFgMx9RdC\r\n"
			+ "IAQZSqFzXas25+84++1DyH9W9GdvV10xoMQ/8xumV2J5AzmZiob+5w8lplaJ+LpW\r\n"
			+ "vEcmyrfiajTRS62iGa7MxNhY+hkV1NJmmvwR9hZU1zHPD9SkPHxgIq85XVDf/ydZ\r\n"
			+ "29B10HZTh0v28wSPkK7SPRY2h4WXCuu7C8DUAXcDW0AqIXDzxCUFIjLe3QlBbv74\r\n"
			+ "poqlIVijloelm8E1PUfUp+51+fnCag==\r\n" + "=X0lG\r\n" + "-----END PGP PUBLIC KEY BLOCK-----";
	private static final String PRIVATEKEY_STRING = "-----BEGIN PGP PRIVATE KEY BLOCK-----\r\n" + 
			"\r\n" + 
			"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\r\n" + 
			"3QlBbv74poqlIVijloelm8E1PUfUp+51+fnCag==\r\n" + 
			"=5342\r\n" + 
			"-----END PGP PRIVATE KEY BLOCK-----";

	@Test
	public void testVerifySignatureString() {
		LOGGER.info("***************************  Verify Signature from String Test  ***************************");
		Boolean isVerified = null;
		try {
			isVerified = VerifySignature.verify(PUBLICKEY_STRING, SIGNATURE_STRING, ORIGINAL_STRING);
			if (isVerified) {
				LOGGER.info("Signature from String verification was successful!");
			} else {
				LOGGER.warn("Signature from String verification was not successful!");
			}
		} catch (IOException | PGPException e) {
			e.printStackTrace();
		} finally {
			assertTrue(isVerified);
			LOGGER.info(
					"*************************  End Verify Signature from String Test  *************************\n");
		}

	}

	@Test
	public void testVerifySignatureFile() {
		Path publicKeyPath = Paths.get("src/test/resources/test_public.asc");
		Path signedPath = Paths.get("src/test/resources/agent.json.asc");
		Path originalPath = Paths.get("src/test/resources/agent.json");
		LOGGER.info("***************************  Verify Signature from Path Test  ***************************");
		Boolean isVerified = null;
		try {
			isVerified = VerifySignature.verify(publicKeyPath, signedPath, originalPath);
			if (isVerified) {
				LOGGER.info("Signature from path verification was successful!");
			} else {
				LOGGER.warn("Signature from path verification was not successful!");
			}
		} catch (IOException | PGPException e) {
			e.printStackTrace();
		} finally {
			assertTrue(isVerified);
			LOGGER.info("*************************  End Verify Signature from Path Test  *************************\n");
		}

	}

	@Test
	public void testVerifySignatureInputStream() {
		InputStream publicKeyInputStream = getClass().getResourceAsStream("/test_public.asc");
		InputStream signedInputStream = getClass().getResourceAsStream("/agent.json.asc");
		InputStream originalInputStream = getClass().getResourceAsStream("/agent.json");
		LOGGER.info("***************************  Verify Signature from InputStream Test  ***************************");
		Boolean isVerified = null;
		try {
			isVerified = VerifySignature.verify(publicKeyInputStream, signedInputStream, originalInputStream);
			if (isVerified) {
				LOGGER.info("Signature from InputStream verification was successful!");
			} else {
				LOGGER.warn("Signature from InputStream verification was not successful!");
			}
		} catch (IOException | PGPException e) {
			e.printStackTrace();
		} finally {
			assertTrue(isVerified);
			LOGGER.info(
					"*************************  End Verify Signature from InputStream Test  *************************\n");
		}

	}

	@Test
	public void testSignObjectWithStrings() {
		String passphrase = null;
		String signature = null;
		LOGGER.info("***************************  Sign with Strings Test  ***************************");
		try {
			signature = SignObject.sign(PRIVATEKEY_STRING, ORIGINAL_STRING, passphrase);
			LOGGER.info("Signing with Strings was successful!");
			LOGGER.trace("Signature: " + signature);
		} catch (IOException | PGPException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage(), e);
		} finally {
			assertNotNull(signature);
			LOGGER.info("*************************  End with Strings Test  *************************\n");
		}
	}

	@Test
	public void testSignObjectWithPaths() {
		Path privateKeyPath = Paths.get("src/test/resources/test_private.asc");
		Path originalPath = Paths.get("src/test/resources/agent.json");
		String passphrase = null;
		String signature = null;
		LOGGER.info("***************************  Sign with Paths Test  ***************************");
		try {
			signature = SignObject.sign(privateKeyPath, originalPath, passphrase);
			LOGGER.info("Signing with Paths was successful!");
			LOGGER.trace("Signature: " + signature);
		} catch (IOException | PGPException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			assertNotNull(signature);
			LOGGER.info("*************************  End with Paths Test  *************************\n");
		}
	}

	@Test
	public void testSignObjectWithInputStreams() {
		InputStream privateKeyInputStream = getClass().getResourceAsStream("/test_private.asc");
		InputStream originalInputStream = getClass().getResourceAsStream("/agent.json");
		String passphrase = null;
		String signature = null;
		LOGGER.info("***************************  Sign with InputStreams Test  ***************************");
		try {
			signature = SignObject.sign(privateKeyInputStream, originalInputStream, passphrase);
			LOGGER.info("Signing with InputStreams was successful!");
			LOGGER.trace("Signature: " + signature);
		} catch (IOException | PGPException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			assertNotNull(signature);
			LOGGER.info("*************************  End with InputStreams Test  *************************\n");
		}
	}

	@Test
	public void testSignAndVerifyStrings () {
		String passphrase = null;
		String signature = null;
		LOGGER.info("***************************  Sign and Verify with Strings Test  ***************************");
		try {
			signature = SignObject.sign(PRIVATEKEY_STRING, ORIGINAL_STRING, passphrase);
			assertNotNull(signature);
			assertNotEquals(signature, "");
			LOGGER.info("Signing was successful!");
			LOGGER.trace("Signature:\n" + signature);
			Boolean verified = VerifySignature.verify(PUBLICKEY_STRING, signature, ORIGINAL_STRING);
			if (verified) {
				LOGGER.info("Created signature verification was successful!");
			} else {
				LOGGER.warn("Created signature verification was not successful!");
			}
			assertTrue(verified);
		} catch (IOException | PGPException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			assertNotNull(signature);
			LOGGER.info("*************************  End Sign and Verify with Strings Test  *************************\n");
		}
	}
	
	@Test
	public void testSignAndVerifyInputStreams () {
		InputStream privateKeyInputStream = getClass().getResourceAsStream("/test_private.asc");
		InputStream originalInputStream = getClass().getResourceAsStream("/agent.json");
		InputStream publicKeyInputStream = getClass().getResourceAsStream("/test_public.asc");
		String passphrase = null;
		String signature = null;
		InputStream signedInputStream = null;
		LOGGER.info("***************************  Sign and Verify with InputStreams Test  ***************************");
		try {
			signature = SignObject.sign(privateKeyInputStream, originalInputStream, passphrase);
			assertNotNull(signature);
			assertNotEquals(signature, "");
			LOGGER.info("Signing was successful!");
			LOGGER.trace("Signature:\n" + signature);
			signedInputStream = IOUtils.toInputStream(signature);
			Boolean verified = VerifySignature.verify(publicKeyInputStream, signedInputStream, originalInputStream);
			if (verified) {
				LOGGER.info("Created Signature verification was successful!");
			} else {
				LOGGER.warn("Created Signature verification was not successful!");
			}
//			assertTrue(verified);
		} catch (IOException | PGPException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			assertNotNull(signature);
			LOGGER.info("*************************  End Sign and Verify with InputStreams Test  *************************\n");
		}
	}
	
}
