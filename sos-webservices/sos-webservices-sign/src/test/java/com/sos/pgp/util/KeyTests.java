package com.sos.pgp.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.pgp.util.key.KeyUtil;
import com.sos.pgp.util.sign.SignObject;
import com.sos.pgp.util.verify.VerifySignature;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KeyTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyTests.class);
    private static final String SIGNATURE_STRING = "-----BEGIN PGP SIGNATURE-----\r\n" + "Version: BCPG v1.65\r\n" + "\r\n"
            + "iQEcBAEBCAAGBQJelYMbAAoJEC+k1WnvpnBh9bYIAMCpnWV3aSZJwhnS6+VadHx/\r\n"
            + "nfB6idfARFRB1JSFvyKN7y4DTVy6XHbOagu8h9Z6YTApxvjPkNt5inX0VTmN5I8a\r\n"
            + "540CfF1BRzI8zzDjf+zpswJQIttv0abfUiEkNeK4mXxoUie7+p/pSXhgqfN4Bg0w\r\n"
            + "7juBnZkMtOiO1xxL3GFHoF/gfU6+iOhvQC4yLzav4GlmP7HCPJ8+7Itw0ycPxGtE\r\n"
            + "Kc3tV/P7bubl2RiJ9BRwqUCYviGrHpoytt0bUKDRj5lNyhd994CUJjK55MSebT5i\r\n"
            + "t65KgMlObVnzfZZ1tnVvvqWlKaX/uY7xYmKbaSuoqldzRhpIvRPuo42z4bb3tsg=\r\n" + "=vrx/\r\n" + "-----END PGP SIGNATURE-----";
    private static final String ORIGINAL_STRING =
            "{\"TYPE\": \"AgentRef\", \"path\": \"/test-agent-2\", \"versionId\": \"201904031529\", \"uri\":\"http://localhost:41420\"}";
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
            + "29B10HZTh0v28wSPkK7SPRY2h4WXCuu7C8DUAXcDW0AqIXDzxCUFIjLe3QlBbv74\r\n" + "poqlIVijloelm8E1PUfUp+51+fnCag==\r\n" + "=X0lG\r\n"
            + "-----END PGP PUBLIC KEY BLOCK-----";
    private static final String PRIVATEKEY_STRING = "-----BEGIN PGP PRIVATE KEY BLOCK-----\r\n" + "\r\n"
            + "lQOYBF6NaHkBCADV+7R21JQ2PEM5u60D/AFe45k+CbNN6owO5iszLN7fc+uWmDEG\r\n"
            + "biykEo56k6uifLDAF4jh1urM41Gu+0JOJ37v2D1Hdmd7fRIdcZez++MAk3RU7V+a\r\n"
            + "+DpGKIJ3faws2CCBVAk3YK2ElmNgOzKYiSCEbqKdIm+Cz3AyMMUVAnyVlaeNKv+W\r\n"
            + "r7ROimbit5ojaKY0IqGwZpHkcJKgug1MX1M6HPIPNpKuOYviB46u63uz+jwAEKvr\r\n"
            + "EftdLcoL+8+9qQTlW2gfLOF2mRe+X6g0LG0lMJUWknWmgskrNF7tKkxXuYXRhGRb\r\n"
            + "3HBLwGiCAHSLA2hg7cIyyVeycULIFqWdJLBFABEBAAEAB/4hGRrT2pPGQ9H+Jxpe\r\n"
            + "6Gbp33h/kQ6L+cCKOW3rL4CVvZ9uHsJOhVIgWsJxEVBWBMVuIPXKQOz2nh6vWyvp\r\n"
            + "XNCUlrT7zcO55SGdLknRzB9Tons2+7RzCvwwh+mORAESuqdkebQnPav5Oy3J3742\r\n"
            + "u9csxNQgTAhFioKHMOX7FvRgSoo/yI5yYd949fd1kRK3H3U6x3Rbt6IwhR/PCYeP\r\n"
            + "nnZAOBPt/RIt80LQJtzfRiOOOf9qtwRkoizftfY+PMvuTFXk4zKGPT9Zj8JHw2oh\r\n"
            + "gQyePAOkGVVr5lsLje6kX0MUo/BwBZIzZ+QW/gQakSjhRY/MhnbyLC5rQxFEVLuc\r\n"
            + "VNXBBADkXULmFfA/066xoaz92GOwhlCzvfJRRAg3LDFMjJNWFxqEqWbGJuUi71Dl\r\n"
            + "gc8WT8qhGqiP864dmnvUGOwbFVcROxAv7eB1syv+82Xo++UziNL57kp1sbUfH19q\r\n"
            + "gPEw5+ZssfA9ZQkOxtlSuEY8aGGS1Gg65LLZAex+dUBHfjM7MQQA7+Dqvtewe6gA\r\n"
            + "V0uvaT/tsRu7xzp0GnBKcrw/P6WbmvuA/ymHxS3ebdy5t0otkraM8/L1BiFmAAIC\r\n"
            + "2bxVuxhKKzFzdslIcPH96Eq+TTmpowoHI7P2PUrZdZIVMi+3hHIL5Hg+0DEiii5a\r\n"
            + "vwAeMEuvHbKMKI+k4yoA4IWy8A/uWVUD/j5T5vlz2PoNUmlh4Xz9WCZS5ss+prgK\r\n"
            + "vIESNMTsYYuNp2IjU6JsdCcur5eBbL4Fx/ZsTutNy/OGOGwnsCa64dXfqq/lPrOo\r\n"
            + "s8AaWMm3npVm316yG38g0vw3mzkho+DgeQBgJxIPsouAJWB7l/m33rWZ0JjuXuRk\r\n"
            + "zhMtwwORfW+RQs+0GnRlc3QgPGluZm9Ac29zLWJlcmxpbi5jb20+iQFOBBMBCAA4\r\n"
            + "FiEEuqKt+z1+Ev1dyCrML6TVae+mcGEFAl6NaHkCGwMFCwkIBwIGFQoJCAsCBBYC\r\n"
            + "AwECHgECF4AACgkQL6TVae+mcGF3CAgAu6yjlR90jxzQnEYS1zWF/q4o7ic3RS5B\r\n"
            + "vOoS+HtYwwac9wqfYcPIn5y1ppQczteP1iE0gq0mUQAC4LX031yKoKGl9AY3jrAS\r\n"
            + "Oe1srCesMOp4fgqeyJZzS0WiVYsMdSnH0RMHF0Vfnag4q665nJfnMYbhUN0rNXF0\r\n"
            + "nVXQLJ8AzFJe6ogEpkDwyy128XLz+NDTJZeO9oVpOKZ+Cn4xp6MUkuIaJOzcLfMI\r\n"
            + "LIn37yVV5CFknpk9I+Frtv2xCPZIREy9+nkGVyDuUYgMy5rAM/LNdiLM2KLKFAvZ\r\n"
            + "A8pdxOAph6014y5kuhDrC7jiq0au6ouohlphBKREtwUXw0CffcG3QJ0DmARejWh5\r\n"
            + "AQgA39FpDp7Knih28kBg1PPiDk1ttnmqN+JXMZJiQNH8Euxl+z+TNKY9UW/GHjis\r\n"
            + "OBOG48SSN+pWyJC+1CmOMxhctiJM8GuPnzBZgsPKTu9Y2WSGIEpdAJ1EQ+v8eYCZ\r\n"
            + "qh5C4hPZjeTmims/tTs0b3/K9D9HPb+kMxp0rm1XfLKdUf2An/BwJjtmojFCwj7X\r\n"
            + "+9iiPbaEOhU16mu/TAFmvy0aix3B4QFx/NwUmzDhHb41mLIzOcs+jd2BR1us0v32\r\n"
            + "zuh3/g3MMsRjHgPYE4uuj1pFNuDqT74hNlyvVFGXTdARgY8PUrilIUYbxfITGP6T\r\n"
            + "IcNPHMj0dxlF8Gqvtmt9qoIe8QARAQABAAf9FTPLmvn0lx+dh6diROocO7SetGQ5\r\n"
            + "eXUfQ2/q5Nm/jQoImHuA9heFbtsteRwZ8cSlHkXh1XAv68INLHYJ+tHPqTqomoyQ\r\n"
            + "nMjswdQgVx1EsnCxEAqwsr0zLqgCpn6bOqAGlU9svYy164jrY79fC+z0N0fDTO3Z\r\n"
            + "n98IDKSzs/lg/oIy5vpAvAvswEAy2hNGBF1UB6WOKeCcEF1mVRIXGEG4TeMfSDX1\r\n"
            + "k2QQHykFTBH6qbw8Ws6FgF7vi0pfDV1DMNXkNiwyFq9/SGcRUp25yAgD9e0Qd68r\r\n"
            + "Onsx0lUjehMwlMAQqvRkCMMupCVXnqA8Ayn9V6cO+hJmCVq8pT0s6C8+jQQA7TvW\r\n"
            + "coZwwds0zGcLbQnTZsTcHZTfYhrWZdQ4wcZjltW44sOd2odauk4GFyOTZFOpQCgs\r\n"
            + "j+d4Jtg/s8sepmnv/vXxEvOJ26rxmPC5csRHv5fJs9jPTe2Hj77ovAIuMKI9ZV5e\r\n"
            + "O1+TgpoCUCXrH3kKognPlr+tWaP0VZSANwtqw3cEAPGF5N1TLWxf3Bo558aAdYMu\r\n"
            + "PC0+Bbx1j+7GGVIPxsGQOUsDRwLK8fU2NQmgXCi9nCeEkvj5NH5QE52OHsPxvj9F\r\n"
            + "z3BLKZ/EurrhmW/M7CyAYiXX8jXPIYFig30NF73vY0ECyHEoqn/omcTY/npnuKs/\r\n"
            + "Pmwdp6d5XNrsf+92qTrXA/4i6Zslli7b/PEdexIfKUT+C0vJB82OqBQCvzWYxxor\r\n"
            + "eJlpzWePgiVUU5O41Uw42/htZUX0f8cKd4VG8zJaByjKJ3kvQx52r9bvz4lJarXq\r\n"
            + "nerfyBzZMMwJbNHb4vjVgAxACQuAXNTQX8boDtbdmi4IqkqPyXxx3hTFL6M5eIMn\r\n"
            + "6DzmiQE2BBgBCAAgFiEEuqKt+z1+Ev1dyCrML6TVae+mcGEFAl6NaHkCGwwACgkQ\r\n"
            + "L6TVae+mcGHH7wf+MWcNl75dhqkeAnCJjpJ5bZJx9bG+ExKWczs0KSV5JEZor9Jx\r\n"
            + "FnpfDFXjx2FNcn/OUAOy8bmmDRmCxBPKbW68joGTHSrS1BNvsIChwkmiqSowHPlO\r\n"
            + "FgMx9RdCIAQZSqFzXas25+84++1DyH9W9GdvV10xoMQ/8xumV2J5AzmZiob+5w8l\r\n"
            + "plaJ+LpWvEcmyrfiajTRS62iGa7MxNhY+hkV1NJmmvwR9hZU1zHPD9SkPHxgIq85\r\n"
            + "XVDf/ydZ29B10HZTh0v28wSPkK7SPRY2h4WXCuu7C8DUAXcDW0AqIXDzxCUFIjLe\r\n" + "3QlBbv74poqlIVijloelm8E1PUfUp+51+fnCag==\r\n" + "=5342\r\n"
            + "-----END PGP PRIVATE KEY BLOCK-----";
    private static final String PUBLICKEY_PATH = "src/test/resources/test_public.asc";
    private static final String PUBLICKEY_RESOURCE_PATH = "/test_public.asc";
    private static final String PRIVATEKEY_PATH = "src/test/resources/test_private.asc";
    private static final String PRIVATEKEY_RESOURCE_PATH = "/test_private.asc";
    private static final String EXPIRED_PRIVATEKEY_RESOURCE_PATH = "/already_expired_private.asc";
    private static final String EXPIRED_PUBLICKEY_RESOURCE_PATH = "/already_expired_public.asc";
    private static final String EXPIRABLE_PRIVATEKEY_RESOURCE_PATH = "/expire_test_private.asc";
    private static final String EXPIRABLE_PUBLICKEY_RESOURCE_PATH = "/expire_test_public.asc";
    private static final String ORIGINAL_PATH = "src/test/resources/agent.json";
    private static final String ORIGINAL_RESOURCE_PATH = "/agent.json";
    private static final String SIGNATURE_PATH = "src/test/resources/agent.json.asc";
    private static final String SIGNATURE_RESOURCE_PATH = "/agent.json.asc";

    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.info("************************************  Key Tests started  ***************************************");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("************************************  Key Tests finished  **************************************");
    }

    @Test
    public void test01SignObjectWithStrings() {
        String passphrase = null;
        String signature = null;
        LOGGER.info("*********  Sign with Strings Test  *************************************************************");
        try {
            signature = SignObject.sign(PRIVATEKEY_STRING, ORIGINAL_STRING, passphrase);
            LOGGER.info("Signing with Strings was successful!");
            LOGGER.trace("Signature:\n" + signature);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test02SignObjectWithPaths() {
        Path privateKeyPath = Paths.get(PRIVATEKEY_PATH);
        Path originalPath = Paths.get(ORIGINAL_PATH);
        String passphrase = null;
        String signature = null;
        LOGGER.info("*********  Sign with Paths Test  ***************************************************************");
try {
            signature = SignObject.sign(privateKeyPath, originalPath, passphrase);
            LOGGER.info("Signing with Paths was successful!");
            LOGGER.trace("Signature:\n" + signature);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test03SignObjectWithInputStreams() {
        InputStream privateKeyInputStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        InputStream originalInputStream = getClass().getResourceAsStream(ORIGINAL_RESOURCE_PATH);
        String passphrase = null;
        String signature = null;
        LOGGER.info("*********  Sign with InputStreams Test  ********************************************************");
        try {
            signature = SignObject.sign(privateKeyInputStream, originalInputStream, passphrase);
            LOGGER.info("Signing with InputStreams was successful!");
            LOGGER.trace("Signature:\n" + signature);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test04VerifySignatureString() {
        LOGGER.info("*********  Verify Signature from String Test  **************************************************"); 
        Boolean isVerified = null;
        try {
            isVerified = VerifySignature.verify(PUBLICKEY_STRING, ORIGINAL_STRING, SIGNATURE_STRING);
            if (isVerified) {
                LOGGER.info("Signature from String verification was successful!");
            } else {
                LOGGER.warn("Signature from String verification was not successful!");
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertTrue(isVerified);
        }

    }

    @Test
    public void test05VerifySignatureFile() {
        Path publicKeyPath = Paths.get(PUBLICKEY_PATH);
        Path originalPath = Paths.get(ORIGINAL_PATH);
        Path signedPath = Paths.get(SIGNATURE_PATH);
        LOGGER.info("*********  Verify Signature from Path Test  ****************************************************");
        Boolean isVerified = null;
        try {
            isVerified = VerifySignature.verify(publicKeyPath, originalPath, signedPath);
            if (isVerified) {
                LOGGER.info("Signature from path verification was successful!");
            } else {
                LOGGER.warn("Signature from path verification was not successful!");
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertTrue(isVerified);
        }

    }

    @Test
    public void test06VerifySignatureInputStream() {
        InputStream publicKeyInputStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        InputStream originalInputStream = getClass().getResourceAsStream(ORIGINAL_RESOURCE_PATH);
        InputStream signedInputStream = getClass().getResourceAsStream(SIGNATURE_RESOURCE_PATH);
        LOGGER.info("*********  Verify Signature from InputStream Test  *********************************************");
        Boolean isVerified = null;
        try {
            isVerified = VerifySignature.verify(publicKeyInputStream, originalInputStream, signedInputStream);
            if (isVerified) {
                LOGGER.info("Signature from InputStream verification was successful!");
            } else {
                LOGGER.warn("Signature from InputStream verification was not successful!");
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertTrue(isVerified);
        }

    }

    @Test
    public void test07SignAndVerifyStrings() {
        String passphrase = null;
        String signature = null;
        LOGGER.info("*********  Sign and Verify with Strings Test  **************************************************");
        try {
            signature = SignObject.sign(PRIVATEKEY_STRING, ORIGINAL_STRING, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.info("Signing was successful!");
            LOGGER.trace("Signature:\n" + signature);
            Boolean verified = VerifySignature.verify(PUBLICKEY_STRING, ORIGINAL_STRING, signature);
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
        }
    }

    @Test
    public void test08aSignAndVerifyPathsWithString() {
        Path privateKeyPath = Paths.get(PRIVATEKEY_PATH);
        Path originalPath = Paths.get(ORIGINAL_PATH);
        Path publicKeyPath = Paths.get(PUBLICKEY_PATH);
        String passphrase = null;
        String signature = null;
        LOGGER.info("*********  Sign and Verify with Paths Test  ****************************************************");
        LOGGER.info("*********  created signature will be transferred as String  ************************************");
        try {
            signature = SignObject.sign(privateKeyPath, originalPath, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.info("Signing was successful!");
            LOGGER.trace("Signature:\n" + signature);
            Boolean verified = VerifySignature.verify(publicKeyPath, originalPath, signature);
            if (verified) {
                LOGGER.info("Created Signature verification was successful!");
            } else {
                LOGGER.warn("Created Signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test08bSignAndVerifyPathsWithInputStream() {
        Path privateKeyPath = Paths.get(PRIVATEKEY_PATH);
        Path originalPath = Paths.get(ORIGINAL_PATH);
        Path publicKeyPath = Paths.get(PUBLICKEY_PATH);
        String passphrase = null;
        String signature = null;
        LOGGER.info("*********  Sign and Verify with Paths Test  ****************************************************");
        LOGGER.info("*********  created signature will be transferred as InputStream  *******************************");
        try {
            signature = SignObject.sign(privateKeyPath, originalPath, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.info("Signing was successful!");
            LOGGER.trace("Signature:\n" + signature);
            InputStream signedInputStream = IOUtils.toInputStream(signature);
            Boolean verified = VerifySignature.verify(publicKeyPath, originalPath, signedInputStream);
            if (verified) {
                LOGGER.info("Created Signature verification was successful!");
            } else {
                LOGGER.warn("Created Signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test09SignAndVerifyInputStreams() {
        InputStream privateKeyInputStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        InputStream originalInputStream = getClass().getResourceAsStream(ORIGINAL_RESOURCE_PATH);
        InputStream publicKeyInputStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        String passphrase = null;
        String signature = null;
        InputStream signedInputStream = null;
        LOGGER.info("*********  Sign and Verify with InputStreams Test  *********************************************");
        try {
            signature = SignObject.sign(privateKeyInputStream, originalInputStream, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.info("Signing was successful!");
            LOGGER.trace("Signature:\n" + signature);
            signedInputStream = IOUtils.toInputStream(signature);
            // As already used streams are closed the needed InputStream of the original has to be recreated before verify
            originalInputStream = getClass().getResourceAsStream(ORIGINAL_RESOURCE_PATH);
            Boolean verified = VerifySignature.verify(publicKeyInputStream, originalInputStream, signedInputStream);
            if (verified) {
                LOGGER.info("Created Signature verification was successful!");
            } else {
                LOGGER.warn("Created Signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test10CreateKeyPair() {
        LOGGER.info("*********  Create KeyPair Test  ****************************************************************");
        String username = "test";
        SOSPGPKeyPair keyPair = null;
        try {
            keyPair = KeyUtil.createKeyPair(username, null);
            LOGGER.info("KeyPair generation was successful");
            LOGGER.info(String.format("privateKey:\n%1$s%2$s", keyPair.getPrivateKey().substring(0, 120), "...\n"));
            LOGGER.info(String.format("publicKey:\n%1$s%2$s", keyPair.getPublicKey().substring(0, 120), "...\n"));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | PGPException e) {
            LOGGER.info("KeyPair generation was not successful");
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(keyPair.getPrivateKey());
            assertNotNull(keyPair.getPublicKey());
        }
    }

    @Test
    public void test11CreateKeyPairSignAndVerify() {
        LOGGER.info("*********  Create KeyPair, Sign and Verify Test  ***********************************************");
        String username = "test";
        String signature = null;
        String passphrase = null;
        SOSPGPKeyPair keyPair = null;
        try {
            LOGGER.info("****************  Create KeyPair  **************************************************************");
            keyPair = KeyUtil.createKeyPair(username, passphrase);
            assertNotNull(keyPair.getPrivateKey());
            assertNotNull(keyPair.getPublicKey());
            assertNotEquals(keyPair.getPrivateKey(), "");
            assertNotEquals(keyPair.getPublicKey(), "");
            LOGGER.info("KeyPair generation was successful!");
            LOGGER.info(String.format("privateKey:\n%1$s%2$s", keyPair.getPrivateKey().substring(0, 120), "...\n"));
            LOGGER.info(String.format("publicKey:\n%1$s%2$s", keyPair.getPublicKey().substring(0, 119), "...\n"));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | PGPException e) {
            LOGGER.info("KeyPair generation was not successful!");
            LOGGER.error(e.getMessage(), e);
        }
        try {
            LOGGER.info("****************  Sign  ************************************************************************");
            signature = SignObject.sign(keyPair.getPrivateKey(), ORIGINAL_STRING, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.info("Signing was successful!");
            LOGGER.info(String.format("Signature:\n%1$s%2$s", signature.substring(0, 112), "...\n"));
            LOGGER.trace("Signature:\n" + signature);
        } catch (IOException | PGPException e) {
            LOGGER.info("Signing was not successful!");
            LOGGER.error(e.getMessage(), e);
        }
        try {
            LOGGER.info("****************  Verify  **********************************************************************");
            Boolean verified = VerifySignature.verify(keyPair.getPublicKey(), ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.info("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("The created private key was used for signing.");
            LOGGER.info("The created public key was used for verification.");
            LOGGER.info("The created signature was verified.");
        }
    }
    
    @Test
    public void test12aExtractPublicKeyFromPrivateKeyString () {
        LOGGER.info("*********  Extract public key from private key String Test  ************************************");
        try {
            String publicKey = KeyUtil.extractPublicKey(PRIVATEKEY_STRING);
            LOGGER.info("Public Key successfully restored from Private Key!");
            LOGGER.info(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "...\n"));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test12bExtractPublicKeyFromPrivateKeyPath () {
        LOGGER.info("*********  Extract public key from private key Path Test  **************************************");
       Path privateKeyPath = Paths.get(PRIVATEKEY_PATH);
        try {
            String publicKey = KeyUtil.extractPublicKey(privateKeyPath);
            LOGGER.info("Public Key successfully restored from Private Key!");
            LOGGER.info(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "...\n"));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test12c1ExtractPublicKeyFromPrivateKeyInputStreamFromString () {
        LOGGER.info("*********  Extract public key from private key InputStream (from String) Test  *****************");
        InputStream privateKeyStream = IOUtils.toInputStream(PRIVATEKEY_STRING);
        try {
            String publicKey = KeyUtil.extractPublicKey(privateKeyStream);
            LOGGER.info("Public Key successfully restored from Private Key!");
            LOGGER.info(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "...\n"));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test12c2ExtractPublicKeyFromPrivateKeyInputStreamFromFile () {
        LOGGER.info("*********  Extract public key from private key InputStream (from File) Test  *******************");
        InputStream privateKeyStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        try {
            String publicKey = KeyUtil.extractPublicKey(privateKeyStream);
            LOGGER.info("Public Key successfully restored from Private Key!");
            LOGGER.info(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "...\n"));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13aCheckValidityPeriodFromExpirableKeyInputStream () {
        LOGGER.info("*********  get validity period for private key (expirable) Test  *******************************");
        InputStream privateKeyStream = getClass().getResourceAsStream(EXPIRABLE_PRIVATEKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
            Date creationDate = publicPGPKey.getCreationTime();
            Long validSeconds = publicPGPKey.getValidSeconds();
            if (validSeconds == 0) {
                LOGGER.info("Key does not expire!");
            } else {
                Date validTo = new Date(creationDate.getTime() + (validSeconds * 1000));
                if (validTo.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.info("Key has expired on: " + validTo.toString()); 
                } else {
                    LOGGER.info("valid until: " + validTo.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13bCheckValidityPeriodFromUnexpirableKeyInputStream () {
        LOGGER.info("*********  get validity period for private key (not expirable) Test  ***************************");
        InputStream privateKeyStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
            Date creationDate = publicPGPKey.getCreationTime();
            Long validSeconds = publicPGPKey.getValidSeconds();
            if (validSeconds == 0) {
                LOGGER.info("Key does not expire!");
            } else {
                Date validTo = new Date(creationDate.getTime() + (validSeconds * 1000));
                if (validTo.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.info("Key has expired on: " + validTo.toString()); 
                } else {
                    LOGGER.info("valid until: " + validTo.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13cCheckValidityPeriodFromAlreadyExpiredKeyInputStream () {
        LOGGER.info("*********  get validity period for private key (already expired) Test  *************************");
        InputStream privateKeyStream = getClass().getResourceAsStream(EXPIRED_PRIVATEKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
            Date creationDate = publicPGPKey.getCreationTime();
            Long validSeconds = publicPGPKey.getValidSeconds();
            if (validSeconds == 0) {
                LOGGER.info("Key does not expire!");
            } else {
                Date validTo = new Date(creationDate.getTime() + (validSeconds * 1000));
                if (validTo.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.info("Key has expired on: " + validTo.toString()); 
                } else {
                    LOGGER.info("valid until: " + validTo.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13dCheckValidityPeriodFromExpirableKeyInputStream () {
        LOGGER.info("*********  get validity period for public key (expirable) Test  ********************************");
        InputStream publicKeyStream = getClass().getResourceAsStream(EXPIRABLE_PUBLICKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromInputStream(publicKeyStream);
            Date creationDate = publicPGPKey.getCreationTime();
            Long validSeconds = publicPGPKey.getValidSeconds();
            if (validSeconds == 0) {
                LOGGER.info("Key does not expire!");
            } else {
                Date validTo = new Date(creationDate.getTime() + (validSeconds * 1000));
                if (validTo.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.info("Key has expired on: " + validTo.toString()); 
                } else {
                    LOGGER.info("valid until: " + validTo.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13eCheckValidityPeriodFromUnexpirableKeyInputStream () {
        LOGGER.info("*********  get validity period for public key (not expirable) Test  ****************************");
        InputStream privateKeyStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromInputStream(privateKeyStream);
            Date creationDate = publicPGPKey.getCreationTime();
            Long validSeconds = publicPGPKey.getValidSeconds();
            if (validSeconds == 0) {
                LOGGER.info("Key does not expire!");
            } else {
                Date validTo = new Date(creationDate.getTime() + (validSeconds * 1000));
                if (validTo.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.info("Key has expired on: " + validTo.toString()); 
                } else {
                    LOGGER.info("valid until: " + validTo.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13fCheckValidityPeriodFromAlreadyExpiredKeyInputStream () {
        LOGGER.info("*********  get validity period for public key (already expired) Test  **************************");
        InputStream privateKeyStream = getClass().getResourceAsStream(EXPIRED_PUBLICKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromInputStream(privateKeyStream);
            Date creationDate = publicPGPKey.getCreationTime();
            Long validSeconds = publicPGPKey.getValidSeconds();
            if (validSeconds == 0) {
                LOGGER.info("Key does not expire!");
            } else {
                Date validTo = new Date(creationDate.getTime() + (validSeconds * 1000));
                if (validTo.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.info("Key has expired on: " + validTo.toString()); 
                } else {
                    LOGGER.info("valid until: " + validTo.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test14CheckKeyStringIsValidPGPKeyString () throws IOException, PGPException {
        LOGGER.info("*********  check if provided String really is a PGP key String Test  ***************************");
        Boolean valid = null;
        LOGGER.info("***************  check 1: private Key; valid true Test  ****************************************");
        valid = KeyUtil.isKeyValid(PRIVATEKEY_STRING);
        LOGGER.info("Key is valid: " + valid);
        assertTrue(valid);
        LOGGER.info("***************  check 2: private Key; valid false Test  ***************************************");
        valid = KeyUtil.isKeyValid("ThisIsNotAPGPKey");
        assertFalse(valid);
        LOGGER.info("Key is valid: " + valid);
        LOGGER.info("***************  check 3a: public Key; valid true Test  ****************************************");
        valid = KeyUtil.isKeyValid(PUBLICKEY_STRING);
        LOGGER.info("Key is valid: " + valid);
        assertTrue(valid);
        LOGGER.info("***************  check 3b: public Key; valid false Test  ***************************************");
        valid = KeyUtil.isKeyValid("ThisIsNotAPGPKey");
        LOGGER.info("Key is valid: " + valid);
        assertFalse(valid);
        LOGGER.info("***************  check 4a: PGPPublicKey Object; valid true Test  *******************************");
        InputStream key = Files.newInputStream(Paths.get(PUBLICKEY_PATH));
        PGPPublicKey pgpPublicKey = KeyUtil.getPGPPublicKeyFromInputStream(key);
        valid = KeyUtil.isKeyValid(pgpPublicKey);
        LOGGER.info("Key is valid: " + valid);
        assertTrue(valid);
        LOGGER.info("***************  check 4b: PGPPublicKey Object; valid false Test  ******************************");
        try {
            pgpPublicKey = KeyUtil.getPGPPublicKeyFromInputStream(IOUtils.toInputStream("ThisIsNotAPGPKey"));
            valid = KeyUtil.isKeyValid(pgpPublicKey);
        } catch (IOException | PGPException e) {
            valid = false;
        }
        LOGGER.info("Key is valid: " + valid);
        assertFalse(valid);
        LOGGER.info("***************  check 5a: SOSPGPKeyPair private key; valid true Test  *************************");
        SOSPGPKeyPair keyPair = new SOSPGPKeyPair();               
        keyPair.setPrivateKey(PRIVATEKEY_STRING);
        keyPair.setPublicKey(null);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.info("KeyPair is valid: " + valid);
        assertTrue(valid);
        LOGGER.info("***************  check 5b: SOSPGPKeyPair public key; valid true Test  **************************");
        keyPair.setPrivateKey(null);
        keyPair.setPublicKey(PUBLICKEY_STRING);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.info("KeyPair is valid: " + valid);
        assertTrue(valid);
        LOGGER.info("***************  check 5c: SOSPGPKeyPair null; valid false Test  *******************************");
        keyPair.setPrivateKey(null);
        keyPair.setPublicKey(null);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.info("KeyPair is valid: " + valid);
        assertFalse(valid);
        LOGGER.info("***************  check 5d: SOSPGPKeyPair private key; valid false Test  ************************");
        keyPair.setPrivateKey("ThisIsNotAPGPKey");
        keyPair.setPublicKey(null);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.info("KeyPair is valid: " + valid);
        assertFalse(valid);
        LOGGER.info("***************  check 5e: SOSPGPKeyPair public key; valid false Test  *************************");
        keyPair.setPrivateKey(null);
        keyPair.setPublicKey("ThisIsNotAPGPKey");
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.info("KeyPair is valid: " + valid);
        assertFalse(valid);
    }

}
