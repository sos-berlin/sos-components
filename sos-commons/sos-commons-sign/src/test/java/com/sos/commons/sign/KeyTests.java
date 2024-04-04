package com.sos.commons.sign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Date;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.joc.model.sign.JocKeyAlgorithm;
import com.sos.joc.model.sign.JocKeyPair;

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
            + "XVDf/ydZ29B10HZTh0v28wSPkK7SPRY2h4WXCuu7C8DUAXcDW0AqIXDzxCUFIjLe\r\n"
            + "3QlBbv74poqlIVijloelm8E1PUfUp+51+fnCag==\r\n" + "=5342\r\n"
            + "-----END PGP PRIVATE KEY BLOCK-----";
    private static final String PUBLICKEY_PATH = "src/test/resources/test_public.asc";
    private static final String PUBLICKEY_RESOURCE_PATH = "/test_public.asc";
    private static final String PRIVATEKEY_PATH = "src/test/resources/test_private.asc";
    private static final String X509_PRIVATEKEY_PATH = "src/test/resources/sp.key";
    private static final String X509_CERTIFICATE_PATH = "src/test/resources/sp.crt";
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
        LOGGER.trace("************************************  Key Tests started  ***************************************");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.trace("************************************  Key Tests finished  **************************************");
    }

    @Test
    public void test01SignObjectWithStrings() {
        String passphrase = null;
        String signature = null;
        LOGGER.trace("*************************  PGP Tests  **********************************************************");
        LOGGER.trace("*********  Test 1: Sign with Strings  **********************************************************");
        try {
            signature = SignObject.signPGP(PRIVATEKEY_STRING, ORIGINAL_STRING, passphrase);
            LOGGER.trace("Signing with Strings was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
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
        LOGGER.trace("*********  Test 2: Sign with Paths  ************************************************************");
        try {
            signature = SignObject.signPGP(privateKeyPath, originalPath, passphrase);
            LOGGER.trace("Signing with Paths was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
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
        LOGGER.trace("*********  Test 3: Sign with InputStreams  *****************************************************");
        try {
            signature = SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase);
            LOGGER.trace("Signing with InputStreams was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test04VerifySignatureString() {
        LOGGER.trace("*********  Test 4: Verify Signature from String  ***********************************************"); 
        Boolean isVerified = null;
        try {
            isVerified = VerifySignature.verifyPGP(PUBLICKEY_STRING, ORIGINAL_STRING, SIGNATURE_STRING);
            if (isVerified) {
                LOGGER.trace("Signature from String verification was successful!");
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
        LOGGER.trace("*********  Test 5: Verify Signature from Path  *************************************************");
        Boolean isVerified = null;
        try {
            isVerified = VerifySignature.verifyPGP(publicKeyPath, originalPath, signedPath);
            if (isVerified) {
                LOGGER.trace("Signature from path verification was successful!");
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
        LOGGER.trace("*********  Test 6: Verify Signature from InputStream  ******************************************");
        Boolean isVerified = null;
        try {
            isVerified = VerifySignature.verifyPGP(publicKeyInputStream, originalInputStream, signedInputStream);
            if (isVerified) {
                LOGGER.trace("Signature from InputStream verification was successful!");
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
        LOGGER.trace("*********  Test 7: Sign and Verify with Strings  ***********************************************");
        try {
            signature = SignObject.signPGP(PRIVATEKEY_STRING, ORIGINAL_STRING, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyPGP(PUBLICKEY_STRING, ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
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
        LOGGER.trace("*********  Test 8a: Sign and Verify with Paths  ************************************************");
        LOGGER.trace("*********  created signature will be transferred as String  ************************************");
        try {
            signature = SignObject.signPGP(privateKeyPath, originalPath, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyPGP(publicKeyPath, originalPath, signature);
            if (verified) {
                LOGGER.trace("Created Signature verification was successful!");
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
        LOGGER.trace("*********  Test 8b: Sign and Verify with Paths  ************************************************");
        LOGGER.trace("*********  created signature will be transferred as InputStream  *******************************");
        try {
            signature = SignObject.signPGP(privateKeyPath, originalPath, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            InputStream signedInputStream = IOUtils.toInputStream(signature, StandardCharsets.UTF_8);
            Boolean verified = VerifySignature.verifyPGP(publicKeyPath, originalPath, signedInputStream);
            if (verified) {
                LOGGER.trace("Created Signature verification was successful!");
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
        LOGGER.trace("*********  Test 9: Sign and Verify with InputStreams  ******************************************");
        try {
            signature = SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            signedInputStream = IOUtils.toInputStream(signature, StandardCharsets.UTF_8);
            // As already used streams are closed the needed InputStream of the original has to be recreated before verify
            originalInputStream = getClass().getResourceAsStream(ORIGINAL_RESOURCE_PATH);
            Boolean verified = VerifySignature.verifyPGP(publicKeyInputStream, originalInputStream, signedInputStream);
            if (verified) {
                LOGGER.trace("Created Signature verification was successful!");
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
    public void test10aCreateKeyPairNeverExpire() {
        LOGGER.trace("*********  Test 10a: Create KeyPair -never expires-  *******************************************");
        String username = "test";
        JocKeyPair keyPair = null;
        try {
            keyPair = KeyUtil.createKeyPair(username, null, null);
            LOGGER.trace("KeyPair generation was successful");
            LOGGER.trace(String.format("privateKey:\n%1$s%2$s", keyPair.getPrivateKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", keyPair.getPublicKey().substring(0, 120), "..."));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | PGPException e) {
            LOGGER.trace("KeyPair generation was not successful");
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(keyPair.getPrivateKey());
            assertNotNull(keyPair.getPublicKey());
        }
    }

    @Test
    public void test10bCreateKeyPair() {
        LOGGER.trace("*********  Test 10b: Create KeyPair -already expired-  *****************************************");
        String username = "test";
        JocKeyPair keyPair = null;
        try {
            Instant now = Instant.now();
            long yearInMillis = 1000L * 60L * 60L * 24L * 365L;
            Instant yearAgo = now.minusMillis(yearInMillis);
            keyPair = KeyUtil.createKeyPair(username, null, yearAgo.getEpochSecond());
            LOGGER.trace("KeyPair generation was successful");
            LOGGER.trace(String.format("privateKey:\n%1$s%2$s", keyPair.getPrivateKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", keyPair.getPublicKey().substring(0, 120), "..."));
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromString(keyPair.getPublicKey());
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | PGPException e) {
            LOGGER.trace("KeyPair generation was not successful");
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(keyPair.getPrivateKey());
            assertNotNull(keyPair.getPublicKey());
        }
    }

    @Test
    public void test10cCreateKeyPair() {
        LOGGER.trace("*********  Test 10c: Create KeyPair -expires next year-  ***************************************");
        String username = "test";
        JocKeyPair keyPair = null;
        try {
            keyPair = KeyUtil.createKeyPair(username, null, null);
            Instant now = Instant.now();
            long yearInMillis = 1000L * 60L * 60L * 24L * 365L;
            Instant nextYear = now.plusMillis(yearInMillis);
            keyPair = KeyUtil.createKeyPair(username, null, nextYear.getEpochSecond());
            LOGGER.trace("KeyPair generation was successful");
            LOGGER.trace(String.format("privateKey:\n%1$s%2$s", keyPair.getPrivateKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", keyPair.getPublicKey().substring(0, 120), "..."));
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromString(keyPair.getPublicKey());
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | PGPException e) {
            LOGGER.trace("KeyPair generation was not successful");
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(keyPair.getPrivateKey());
            assertNotNull(keyPair.getPublicKey());
        }
    }
    @Test
    public void test11CreateKeyPairSignAndVerify() {
        LOGGER.trace("*********  Test 11: Create KeyPair, Sign and Verify  *******************************************");
        String username = "test";
        String signature = null;
        String passphrase = null;
        JocKeyPair keyPair = null;
        try {
            LOGGER.trace("****************  Create KeyPair  **************************************************************");
            keyPair = KeyUtil.createKeyPair(username, passphrase, null);
            assertNotNull(keyPair.getPrivateKey());
            assertNotNull(keyPair.getPublicKey());
            assertNotEquals(keyPair.getPrivateKey(), "");
            assertNotEquals(keyPair.getPublicKey(), "");
            LOGGER.trace("KeyPair generation was successful!");
            LOGGER.trace(String.format("privateKey:\n%1$s%2$s", keyPair.getPrivateKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", keyPair.getPublicKey().substring(0, 119), "..."));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | PGPException e) {
            LOGGER.trace("KeyPair generation was not successful!");
            LOGGER.error(e.getMessage(), e);
        }
        try {
            LOGGER.trace("****************  Sign  ************************************************************************");
            signature = SignObject.signPGP(keyPair.getPrivateKey(), ORIGINAL_STRING, passphrase);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 112), "..."));
            LOGGER.trace("Signature:\n" + signature);
        } catch (IOException | PGPException e) {
            LOGGER.trace("Signing was not successful!");
            LOGGER.error(e.getMessage(), e);
        }
        try {
            LOGGER.trace("****************  Verify  **********************************************************************");
            Boolean verified = VerifySignature.verifyPGP(keyPair.getPublicKey(), ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.trace("The created private key was used for signing.");
            LOGGER.trace("The created public key was used for verification.");
            LOGGER.trace("The created signature was verified.");
        }
    }
    
    @Test
    public void test12aExtractPublicKeyFromPrivateKeyString () {
        LOGGER.trace("*********  Test 12a: Extract public key from private key String  *******************************");
        try {
            String publicKey = KeyUtil.extractPublicKey(PRIVATEKEY_STRING);
            LOGGER.trace("Public Key successfully restored from Private Key!");
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "..."));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test12bExtractPublicKeyFromPrivateKeyPath () {
        LOGGER.trace("*********  Test 12b: Extract public key from private key Path  *********************************");
       Path privateKeyPath = Paths.get(PRIVATEKEY_PATH);
        try {
            String publicKey = KeyUtil.extractPublicKey(privateKeyPath);
            LOGGER.trace("Public Key successfully restored from Private Key!");
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "..."));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test12cExtractPublicKeyFromPrivateKeyInputStreamFromString () {
        LOGGER.trace("*********  Test 12c: Extract public key from private key InputStream (from String)  ************");
        InputStream privateKeyStream = IOUtils.toInputStream(PRIVATEKEY_STRING, StandardCharsets.UTF_8);
        try {
            String publicKey = KeyUtil.extractPublicKey(privateKeyStream);
            LOGGER.trace("Public Key successfully restored from Private Key!");
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "..."));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test12dExtractPublicKeyFromPrivateKeyInputStreamFromFile () {
        LOGGER.trace("*********  Test 12d: Extract public key from private key InputStream (from File)  **************");
        InputStream privateKeyStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        try {
            String publicKey = KeyUtil.extractPublicKey(privateKeyStream);
            LOGGER.trace("Public Key successfully restored from Private Key!");
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", publicKey.substring(0, 119), "..."));
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13aCheckValidityPeriodFromExpirableKeyInputStream () {
        LOGGER.trace("*********  Test 13a: get validity period for private key (expirable)  **************************");
        InputStream privateKeyStream = getClass().getResourceAsStream(EXPIRABLE_PRIVATEKEY_RESOURCE_PATH);
        
        try {
            PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
            Long keyId = publicPGPKey.getKeyID();
            String keyID = Long.toHexString(keyId).toUpperCase();
            LOGGER.trace(String.format("Extracted KeyId (original as Long): %1$d", keyId));
            LOGGER.trace(String.format("Extracted KeyId (as Hex String): %1$s", keyID));
            LOGGER.trace(String.format("Extracted UserId: %1$s", (String)publicPGPKey.getUserIDs().next()));
            LOGGER.trace(String.format("Extracted \"Fingerprint\": %1$s", Hex.toHexString(publicPGPKey.getFingerprint())));
            LOGGER.trace(String.format("Extracted \"Encoded\": %1$s", Hex.toHexString(publicPGPKey.getEncoded())));
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13bCheckValidityPeriodFromUnexpirableKeyInputStream () {
        LOGGER.trace("*********  Test 13b: get validity period for private key (not expirable)  **********************");
        InputStream privateKeyStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
            Long keyId = publicPGPKey.getKeyID();
            String keyID = Long.toHexString(keyId).toUpperCase();
            LOGGER.trace(String.format("Extracted KeyId (original as Long): %1$d", keyId));
            LOGGER.trace(String.format("Extracted KeyId (as Hex String): %1$s", keyID));
            LOGGER.trace(String.format("Extracted UserId: %1$s", (String)publicPGPKey.getUserIDs().next()));
            LOGGER.trace(String.format("Extracted \"Fingerprint\": %1$s", Hex.toHexString(publicPGPKey.getFingerprint())));
            LOGGER.trace(String.format("Extracted \"Encoded\": %1$s", Hex.toHexString(publicPGPKey.getEncoded())));
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13cCheckValidityPeriodFromAlreadyExpiredKeyInputStream () {
        LOGGER.trace("*********  Test 13c: get validity period for private key (already expired)  ********************");
        InputStream privateKeyStream = getClass().getResourceAsStream(EXPIRED_PRIVATEKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
            Long keyId = publicPGPKey.getKeyID();
            String keyID = Long.toHexString(keyId).toUpperCase();
            LOGGER.trace(String.format("Extracted KeyId (original as Long): %1$d", keyId));
            LOGGER.trace(String.format("Extracted KeyId (as Hex String): %1$s", keyID));
            LOGGER.trace(String.format("Extracted UserId: %1$s", (String)publicPGPKey.getUserIDs().next()));
            LOGGER.trace(String.format("Extracted \"Fingerprint\": %1$s", Hex.toHexString(publicPGPKey.getFingerprint())));
            LOGGER.trace(String.format("Extracted \"Encoded\": %1$s", Hex.toHexString(publicPGPKey.getEncoded())));
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13dCheckValidityPeriodFromExpirableKeyInputStream () {
        LOGGER.trace("*********  Test 13d: get validity period for public key (expirable)  ***************************");
        InputStream publicKeyStream = getClass().getResourceAsStream(EXPIRABLE_PUBLICKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromInputStream(publicKeyStream);
            Long keyId = publicPGPKey.getKeyID();
            String keyID = Long.toHexString(keyId).toUpperCase();
            LOGGER.trace(String.format("Extracted KeyId (original as Long): %1$d", keyId));
            LOGGER.trace(String.format("Extracted KeyId (as Hex String): %1$s", keyID));
            LOGGER.trace(String.format("Extracted UserId: %1$s", (String)publicPGPKey.getUserIDs().next()));
            LOGGER.trace(String.format("Extracted \"Fingerprint\": %1$s", Hex.toHexString(publicPGPKey.getFingerprint())));
            LOGGER.trace(String.format("Extracted \"Encoded\": %1$s", Hex.toHexString(publicPGPKey.getEncoded())));
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13eCheckValidityPeriodFromUnexpirableKeyInputStream () {
        LOGGER.trace("*********  Test 13e: get validity period for public key (not expirable)  ***********************");
        InputStream privateKeyStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromInputStream(privateKeyStream);
            Long keyId = publicPGPKey.getKeyID();
            String keyID = Long.toHexString(keyId).toUpperCase();
            LOGGER.trace(String.format("Extracted KeyId (original as Long): %1$d", keyId));
            LOGGER.trace(String.format("Extracted KeyId (as Hex String): %1$s", keyID));
            LOGGER.trace(String.format("Extracted UserId: %1$s", (String)publicPGPKey.getUserIDs().next()));
            LOGGER.trace(String.format("Extracted \"Fingerprint\": %1$s", Hex.toHexString(publicPGPKey.getFingerprint())));
            LOGGER.trace(String.format("Extracted \"Encoded\": %1$s", Hex.toHexString(publicPGPKey.getEncoded())));
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test13fCheckValidityPeriodFromAlreadyExpiredKeyInputStream () {
        LOGGER.trace("*********  Test 13f: get validity period for public key (already expired)  *********************");
        InputStream privateKeyStream = getClass().getResourceAsStream(EXPIRED_PUBLICKEY_RESOURCE_PATH);
        try {
            PGPPublicKey publicPGPKey = KeyUtil.getPGPPublicKeyFromInputStream(privateKeyStream);
            Long keyId = publicPGPKey.getKeyID();
            String keyID = Long.toHexString(keyId).toUpperCase();
            LOGGER.trace(String.format("Extracted KeyId (original as Long): %1$d", keyId));
            LOGGER.trace(String.format("Extracted KeyId (as Hex String): %1$s", keyID));
            LOGGER.trace(String.format("Extracted UserId: %1$s", (String)publicPGPKey.getUserIDs().next()));
            LOGGER.trace(String.format("Extracted \"Fingerprint\": %1$s", Hex.toHexString(publicPGPKey.getFingerprint())));
            LOGGER.trace(String.format("Extracted \"Encoded\": %1$s", Hex.toHexString(publicPGPKey.getEncoded())));
            Date validUntil = KeyUtil.getValidUntil(publicPGPKey);
            if (validUntil == null) {
                LOGGER.trace("Key does not expire!");
            } else {
                if (validUntil.getTime() < Date.from(Instant.now()).getTime()) {
                    LOGGER.trace("Key has expired on: " + validUntil.toString()); 
                } else {
                    LOGGER.trace("valid until: " + validUntil.toString()); 
                }
            }
        } catch (IOException | PGPException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test14CheckKeyStringIsValidPGPKeyString () throws IOException, PGPException {
        LOGGER.trace("*********  Test 14: check if provided String really is a PGP key String  ***********************");
        Boolean valid = null;
        LOGGER.trace("***************  check 1: private Key; valid true Test  ****************************************");
        valid = KeyUtil.isKeyValid(PRIVATEKEY_STRING, JocKeyAlgorithm.PGP.name());
        LOGGER.trace("Key is valid: " + valid);
        assertTrue(valid);
        LOGGER.trace("***************  check 2: private Key; valid false Test  ***************************************");
        valid = KeyUtil.isKeyValid("ThisIsNotAPGPKey", JocKeyAlgorithm.PGP.name());
        assertFalse(valid);
        LOGGER.trace("Key is valid: " + valid);
        LOGGER.trace("***************  check 3a: public Key; valid true Test  ****************************************");
        valid = KeyUtil.isKeyValid(PUBLICKEY_STRING, JocKeyAlgorithm.PGP.name());
        LOGGER.trace("Key is valid: " + valid);
        assertTrue(valid);
        LOGGER.trace("***************  check 3b: public Key; valid false Test  ***************************************");
        valid = KeyUtil.isKeyValid("ThisIsNotAPGPKey", JocKeyAlgorithm.PGP.name());
        LOGGER.trace("Key is valid: " + valid);
        assertFalse(valid);
        LOGGER.trace("***************  check 4a: PGPPublicKey Object; valid true Test  *******************************");
        InputStream key = Files.newInputStream(Paths.get(PUBLICKEY_PATH));
        PGPPublicKey pgpPublicKey = KeyUtil.getPGPPublicKeyFromInputStream(key);
        valid = KeyUtil.isKeyNotNull(pgpPublicKey);
        LOGGER.trace("Key is valid: " + valid);
        assertTrue(valid);
        LOGGER.trace("***************  check 4b: PGPPublicKey Object; valid false Test  ******************************");
        try {
            pgpPublicKey = KeyUtil.getPGPPublicKeyFromInputStream(IOUtils.toInputStream("ThisIsNotAPGPKey", StandardCharsets.UTF_8));
            valid = KeyUtil.isKeyNotNull(pgpPublicKey);
        } catch (IOException | PGPException e) {
            valid = false;
        }
        LOGGER.trace("Key is valid: " + valid);
        assertFalse(valid);
        LOGGER.trace("***************  check 5a: JocKeyPair private key; valid true  *********************************");
        JocKeyPair keyPair = new JocKeyPair();               
        keyPair.setPrivateKey(PRIVATEKEY_STRING);
        keyPair.setKeyAlgorithm(SOSKeyConstants.PGP_ALGORITHM_NAME);
        keyPair.setPublicKey(null);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.trace("KeyPair is valid: " + valid);
        assertTrue(valid);
        LOGGER.trace("***************  check 5b: JocKeyPair public key; valid true  **********************************");
        keyPair.setPrivateKey(null);
        keyPair.setPublicKey(PUBLICKEY_STRING);
        keyPair.setKeyAlgorithm(SOSKeyConstants.PGP_ALGORITHM_NAME);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.trace("KeyPair is valid: " + valid);
        assertTrue(valid);
        LOGGER.trace("***************  check 5c: JocKeyPair null; valid false  ***************************************");
        keyPair.setPrivateKey(null);
        keyPair.setPublicKey(null);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.trace("KeyPair is valid: " + valid);
        assertFalse(valid);
        LOGGER.trace("***************  check 5d: JocKeyPair private key; valid false  ********************************");
        keyPair.setPrivateKey("ThisIsNotAPGPKey");
        keyPair.setPublicKey(null);
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.trace("KeyPair is valid: " + valid);
        assertFalse(valid);
        LOGGER.trace("***************  check 5e: JocKeyPair public key; valid false  *********************************");
        keyPair.setPrivateKey(null);
        keyPair.setPublicKey("ThisIsNotAPGPKey");
        valid = KeyUtil.isKeyPairValid(keyPair);
        LOGGER.trace("KeyPair is valid: " + valid);
        assertFalse(valid);
    }
    
    @Test
    public void test15CheckValidityInformationFromValidSecondsAndExpirationTime ()
            throws IOException, PGPException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        LOGGER.trace("*********  Test 15: check valid seconds and expirationTime set  ********************************");
        Instant now = Instant.now();
        long yearInMillis = 1000L * 60L * 60L * 24L * 365L;
        Instant yearAgo = now.minusMillis(yearInMillis);
        Instant nextYear = now.plusMillis(yearInMillis);
        JocKeyPair keyPair = null;
        LOGGER.trace("yearAgo: " + yearAgo);
        keyPair = KeyUtil.createKeyPair("testYearAgo", null, yearAgo.getEpochSecond());
        LOGGER.trace("valid until a yearAgo: " + keyPair.getValidUntil());
        LOGGER.trace("now: " + now);
        keyPair = KeyUtil.createKeyPair("testNow", null, now.getEpochSecond());
        LOGGER.trace("valid until now: " + keyPair.getValidUntil());
        LOGGER.trace("nextYear: " + nextYear);
        keyPair = KeyUtil.createKeyPair("testNextYear", null, nextYear.getEpochSecond());
        LOGGER.trace("valid until nextYear: " + keyPair.getValidUntil());
        LOGGER.trace("null: ");
        keyPair = KeyUtil.createKeyPair("testNever", null, null);
        LOGGER.trace("valid until null: " + keyPair.getValidUntil());
    }

    @Test
    public void test16SignAndVerifyX509KeyPair() {
        String signature = null;
        LOGGER.trace("*****************************  RSA and X.509 Tests  ********************************************");
        LOGGER.trace("*********  Test 16: Sign and Verify with Public Key  *******************************************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyX509(keyPair.getPublic(), ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException 
                | InvalidKeySpecException | SignatureException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } 
        finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test17SignAndVerifyX509KeyPairAndGeneratedCertificate() {
        String signature = null;
        LOGGER.trace("*********  Test 17: Sign and Verify with generated Certificate  ********************************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            X509Certificate cert = KeyUtil.generateCertificateFromKeyPair(keyPair, "testaccount", "SHA256withRSA", null);
            Boolean verified = VerifySignature.verifyX509(cert, ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException 
                | InvalidKeySpecException | SignatureException | DataLengthException | NoSuchProviderException e) {
            LOGGER.error(e.getMessage(), e);
        } 
        finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test18SignAndVerifyX509KeyPairAndPubKeyFromGeneratedCertificate() {
        String signature = null;
        LOGGER.trace("*********  Test 18: Sign and Verify with Public Key from generated Certificate  ****************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            X509Certificate cert = KeyUtil.generateCertificateFromKeyPair(keyPair, "testaccount", "SHA256withRSA", null);
            Boolean verified = VerifySignature.verifyX509(cert.getPublicKey(), ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
            assertTrue(verified);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException 
                | InvalidKeySpecException | SignatureException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } 
        finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test19SignAndVerifyX509withPubKeyFromCert() {
        String signature = null;
        LOGGER.trace("*********  Test 19: Sign and Verify with Public Key from Certificate  **************************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            Certificate certificate =  KeyUtil.getCertificate(
                    new String(Files.readAllBytes(Paths.get(X509_CERTIFICATE_PATH)), StandardCharsets.UTF_8));
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyX509(certificate.getPublicKey(), ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException 
                | InvalidKeySpecException | SignatureException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } 
        catch(CertificateException e){
            LOGGER.error(e.getMessage(), e);
        }
        finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test20SignAndVerifyX509withPubKeyFromX509Cert() {
        String signature = null;
        LOGGER.trace("*********  Test 20: Sign and Verify with Public Key from X.509 Certificate  ********************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            X509Certificate certificate =  KeyUtil.getX509Certificate(
                    new String(Files.readAllBytes(Paths.get(X509_CERTIFICATE_PATH)), StandardCharsets.UTF_8));
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyX509(certificate.getPublicKey(), ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException 
                | InvalidKeySpecException | SignatureException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } 
        catch(CertificateException e){
            LOGGER.error(e.getMessage(), e);
        }
        finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test21SignAndVerifyX509Certificate() {
        String signature = null;
        LOGGER.trace("*********  Test 21: Sign and Verify with X.509 Certificate  ************************************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            X509Certificate certificate =  KeyUtil.getX509Certificate(
                    new String(Files.readAllBytes(Paths.get(X509_CERTIFICATE_PATH)), StandardCharsets.UTF_8));
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyX509(certificate, ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | CertificateException | NoSuchProviderException
                | InvalidKeySpecException | SignatureException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test22SignAndVerifyCertificate() {
        String signature = null;
        LOGGER.trace("*********  Test 22: Sign and Verify with Certificate  ******************************************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            Certificate certificate =  KeyUtil.getCertificate(
                    new String(Files.readAllBytes(Paths.get(X509_CERTIFICATE_PATH)), StandardCharsets.UTF_8));
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            Boolean verified = VerifySignature.verifyX509(certificate, ORIGINAL_STRING, signature);
            if (verified) {
                LOGGER.trace("Created signature verification was successful!");
            } else {
                LOGGER.warn("Created signature verification was not successful!");
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | CertificateException | NoSuchProviderException
                | InvalidKeySpecException | SignatureException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            assertNotNull(signature);
        }
    }

    @Test
    public void test23CheckKeys() {
        LOGGER.trace("*********  Test 23: Check Keys From KeyPair and Certificate match  *****************************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            String certificateString = new String(Files.readAllBytes(Paths.get(X509_CERTIFICATE_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            boolean keyPairMatches = KeyUtil.pubKeyMatchesPrivKey(keyPair.getPrivate(), keyPair.getPublic());
            LOGGER.trace("Keys From KeyPair match: " + keyPairMatches);
            boolean compareMatched = KeyUtil.compareRSAKeyAndCertificate(privateKeyString, certificateString);
            X509Certificate x509Certificate =  KeyUtil.getX509Certificate(certificateString);
            Certificate certificate =  KeyUtil.getCertificate(certificateString);
            LOGGER.trace("Private Key and Certificate match: " + compareMatched);
            boolean keysX509Match = KeyUtil.pubKeyFromCertMatchPrivKey(keyPair.getPrivate(), x509Certificate);
            LOGGER.trace("Private Key and Certificate match: " + keysX509Match);
            boolean pupKeysX059Match = KeyUtil.pubKeyFromPairAndCertMatch(keyPair.getPublic(), x509Certificate.getPublicKey());
            LOGGER.trace("Public Key from KeyPair and Public Key from Certificate match: " + pupKeysX059Match);
            boolean pubKeysX059Match2 = KeyUtil.pubKeyFromPairAndCertMatch(keyPair.getPublic(), x509Certificate);
            LOGGER.trace("Public Key from KeyPair and Certificate match: " + pubKeysX059Match2);
            boolean keysX509Matches = KeyUtil.pubKeyMatchesPrivKey(keyPair.getPrivate(), x509Certificate.getPublicKey());
            LOGGER.trace("Private Key from KeyPair  and Public Key from Certificate match: " + keysX509Matches);
            boolean keysMatch = KeyUtil.pubKeyFromCertMatchPrivKey(keyPair.getPrivate(), certificate);
            LOGGER.trace("Private Key and Certificate match: " + keysMatch);
            boolean pupKeysMatch = KeyUtil.pubKeyFromPairAndCertMatch(keyPair.getPublic(), certificate.getPublicKey());
            LOGGER.trace("Public Key from KeyPair and Public Key from Certificate match: " + pupKeysMatch);
            boolean pubKeysMatch2 = KeyUtil.pubKeyFromPairAndCertMatch(keyPair.getPublic(), certificate);
            LOGGER.trace("Public Key from KeyPair and Public Key from Certificate match: " + pubKeysMatch2);
            boolean keysMatches = KeyUtil.pubKeyMatchesPrivKey(keyPair.getPrivate(), certificate.getPublicKey());
            LOGGER.trace("Private Key from KeyPair and Public Key from Certificate match: " + keysMatches);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | DataLengthException | CertificateException e) {
            LOGGER.error(e.getMessage(), e);
        } 
    }

    @Test
    public void test24CheckKeys() {
        LOGGER.trace("*********  Test 24: Check Keys From KeyPair and generated Certificate match  *******************");
        try {
            String privateKeyString = new String(Files.readAllBytes(Paths.get(X509_PRIVATEKEY_PATH)), StandardCharsets.UTF_8);
            KeyPair keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            X509Certificate cert = KeyUtil.generateCertificateFromKeyPair(keyPair, "testaccount", "SHA256withRSA", null);
            boolean keysAndGeneratedX509Match = KeyUtil.pubKeyFromCertMatchPrivKey(keyPair.getPrivate(), cert);
            LOGGER.trace("matches: " + keysAndGeneratedX509Match);
            assertTrue(keysAndGeneratedX509Match);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | DataLengthException e) {
            LOGGER.error(e.getMessage(), e);
        } 
    }

    @Test
    public void test25CheckKeys() {
        LOGGER.trace("*********  Test 25: Check generated KeyPair format  ********************************************");
        try {
            JocKeyPair jocKeyPair = KeyUtil.createRSAJocKeyPair("testaccount", null);
            LOGGER.trace("KeyPair generation was successful");
            LOGGER.trace(String.format("privateKey:\n%1$s%2$s", jocKeyPair.getPrivateKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", jocKeyPair.getPublicKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("KeyID: %1$s", jocKeyPair.getKeyID()));
            LOGGER.trace(String.format("validUntil: %1$s", jocKeyPair.getValidUntil()));
            assertNotNull(jocKeyPair);
        } catch (NoSuchAlgorithmException | DataLengthException | NoSuchProviderException e) {
            LOGGER.error(e.getMessage(), e);
        } 
    }

    @Test
    public void test26CheckKeys() {
        LOGGER.trace("*********  Test 26: generate KeyPair, parse private Key to String and back to private Key  *****");
        try {
            KeyPair keyPair = KeyUtil.createRSAKeyPair();
            JocKeyPair jocKeyPair = KeyUtil.createJOCKeyPair(keyPair);
            LOGGER.trace("KeyPair generation was successful");
            LOGGER.trace(String.format("privateKey:\n%1$s%2$s", jocKeyPair.getPrivateKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("publicKey:\n%1$s%2$s", jocKeyPair.getPublicKey().substring(0, 120), "..."));
            LOGGER.trace(String.format("KeyID: %1$s", jocKeyPair.getKeyID()));
            LOGGER.trace(String.format("validUntil: %1$s", jocKeyPair.getValidUntil()));
            assertNotNull(jocKeyPair);
            KeyPair kp = new KeyPair(
                    KeyUtil.getPublicKeyFromString(KeyUtil.decodePublicKeyString(jocKeyPair.getPublicKey())), 
                    KeyUtil.getPrivateKeyFromString(jocKeyPair.getPrivateKey()));
            LOGGER.trace("PrivateKey before and after parsing match: " + keyPair.getPrivate().equals(kp.getPrivate()));
            assertEquals(keyPair.getPrivate(), kp.getPrivate());
            LOGGER.trace("PublicKey before and after parsing match: " + keyPair.getPublic().equals(kp.getPublic()));
            assertEquals(keyPair.getPublic(), kp.getPublic());
        } catch (NoSuchAlgorithmException | DataLengthException | NoSuchProviderException | InvalidKeySpecException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        } 
    }
    
    @Test
    public void test27ExtRSAKeyAndCertificate () throws IOException {
        LOGGER.trace("*********  Test 27: use OpenSSL generated RSA Key and X.509 certificate  ***********************");
        String privateKeyString = new String(Files.readAllBytes(
                Paths.get("src/test/resources/sos.private-rsa-key.pem")), StandardCharsets.UTF_8);
        String certificateString = new String(Files.readAllBytes(
                Paths.get("src/test/resources/sos.certificate-rsa-key.pem")), StandardCharsets.UTF_8);
        KeyPair keyPair = null;
        String signature = null;
        try {
            LOGGER.trace("*********  create KeyPair Object with RSA private Key from File  *******************************");
            keyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKeyString);
            assertNotNull(keyPair);
            LOGGER.trace("*********  create X.509 certifcate Object from File  *******************************************");
            X509Certificate certificate =  KeyUtil.getX509Certificate(certificateString);
            assertNotNull(certificate);
            LOGGER.trace("*********  create signature of example String with private RSA key from KeyPair object  ********");
            signature = SignObject.signX509(keyPair.getPrivate(), ORIGINAL_STRING);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s%2$s", signature.substring(0, 120), "..."));
            LOGGER.trace("*********  verify signature with X.509 certificate object  *************************************");
            Boolean verified = VerifySignature.verifyX509(certificate, ORIGINAL_STRING, signature);
            assertTrue(verified);
        } catch (NoSuchAlgorithmException|
                InvalidKeySpecException|
                CertificateException | 
                InvalidKeyException | 
                SignatureException | 
                NoSuchProviderException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test28ExtECDSAKeyAndCertificate () throws IOException {
        LOGGER.trace("*********  Test 28: use OpenSSL generated ECDSA Key and X.509 certificate  *********************");
        String privateKeyString = new String(Files.readAllBytes(
                Paths.get("src/test/resources/sos.private-ec-key.pem")), StandardCharsets.UTF_8);
        String certificateString = new String(Files.readAllBytes(
                Paths.get("src/test/resources/sos.certificate-ec-key.pem")), StandardCharsets.UTF_8);
        KeyPair keyPair = null;
        String signature = null;
        try {
            LOGGER.trace("*********  create KeyPair Object with ECDSA private Key from File  *****************************");
            keyPair = KeyUtil.getKeyPairFromECDSAPrivatKeyString(privateKeyString);
            assertNotNull(keyPair);
            LOGGER.trace("*********  create X.509 certifcate Object from File  *******************************************");
            X509Certificate certificate =  KeyUtil.getX509Certificate(certificateString);
            assertNotNull(certificate);
            LOGGER.trace("*********  create signature of example String with private ECDSA key from KeyPair object  ******");
            signature = SignObject.signX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getPrivate(), ORIGINAL_STRING);
            assertNotNull(signature);
            assertNotEquals(signature, "");
            LOGGER.trace("Signing was successful!");
            LOGGER.trace(String.format("Signature:\n%1$s", signature));
            LOGGER.trace("*********  verify signature with X.509 certificate object  *************************************");
            boolean verified = VerifySignature.verifyX509BC(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, certificate, ORIGINAL_STRING, signature);
            LOGGER.trace("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verified);
            verified = VerifySignature.verifyX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, certificate.getPublicKey(), ORIGINAL_STRING, signature);
            LOGGER.trace(
                    "Signature verification with method \"VerifySignature.verifyX509 (PublicKey from Certificate)\" successful: " + verified);
            assertTrue(verified);
        } catch (NoSuchAlgorithmException|
                InvalidKeySpecException|
                CertificateException | 
                InvalidKeyException | 
                SignatureException | 
                NoSuchProviderException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test29ExtractCNFromX509Certificate () throws IOException, CertificateException, InvalidNameException {
        LOGGER.trace("*********  Test 29: extract CN from client X.509 certificate  **********************************");
        String certificateString = new String(Files.readAllBytes(
                Paths.get("src/test/resources/sp.crt")), StandardCharsets.UTF_8);
        LOGGER.trace("*********  create X.509 certifcate Object from File  *******************************************");
        X509Certificate certificate =  KeyUtil.getX509Certificate(certificateString);
        assertNotNull(certificate);
        String subjectDN = certificate.getSubjectDN().getName();
        // get CN with bouncy castle
        X500Name x500Name = new JcaX509CertificateHolder(certificate).getSubject();
        RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
        String clientCN = IETFUtils.valueToString(cn.getFirst().getValue());
        LOGGER.trace("bouncycastle: CN=" + clientCN);
        
        // get CN with LDAP
        LdapName ldapName = new LdapName(subjectDN);
        clientCN = ldapName.getRdns().stream().filter(rdn -> rdn.getType().equalsIgnoreCase("CN")).findFirst().get().getValue().toString();
        LOGGER.trace("LdapName: CN=" + clientCN);
            
    }

    @Test
    @Ignore
    public void test30ParseKey () throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, PKCSException {
        LOGGER.trace("*********************  Test 30: parse a private key  *******************************************");
        String privateKeyString = new String(Files.readAllBytes(Paths.get("C:\\sp\\devel\\js7\\keys\\centostest_primary\\agent.key")));
        PrivateKey key = KeyUtil.getPrivateKeyFromString(privateKeyString);
    }
    
    @SuppressWarnings("unused")
    private static String byteArrayToHexString(byte[] ba) {
        MessageDigest digest;
        StringBuilder hexString = new StringBuilder();
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ba);

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
                if (hexString.toString().length() % 4 == 0) {
                    hexString.append(" ");
                }
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage());
        }
        return hexString.toString().toUpperCase();
    }

}
