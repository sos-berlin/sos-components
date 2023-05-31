package com.sos.joc.classes.security;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
public class SOSSecurityUtil {

    private static final String UTF_8 = "UTF-8";
    private static final String END_PUBLIC_KEY = "-----END PUBLIC KEY-----";
    private static final String BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----";
    private static final String WITHRSA = "WITHRSA";
    private static final String WITHECDSA = "WITHECDSA";
    private static final String WITHDSA = "WITHDSA";
    private static final String RSA = "RSA";
    private static final String KTY = "kty";
    private static final String CRV = "crv";
    private static final String SHA256WITH_RSA = "SHA256withRSA";
    private static final String EC = "EC";
    private static final String P_256 = "P-256";
    private static final String SHA3846WITH_ECDSA = "SHA3846withECDSA";
    private static final String SHA512WITH_ECDSA = "SHA512withECDSA";
    private static final String SHA256WITH_ECDSA = "SHA256withECDSA";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSecurityUtil.class);

    private static PublicKey getPublicKey(String base64PublicKey, String alg) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchProviderException {

        PublicKey publicKey = null;
        String publicKeyPEM = base64PublicKey.replace(BEGIN_PUBLIC_KEY, "").replace("\n", "").replace("\r", "").replace(
                END_PUBLIC_KEY, "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance(alg);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    private static String getAlg(String sAlg) {
        String alg = EC;

        if (sAlg.toUpperCase().endsWith(WITHDSA)) {
            alg = "DSA";
        }
        if (sAlg.toUpperCase().endsWith(WITHECDSA)) {
            alg = EC;
        }
        if (sAlg.toUpperCase().endsWith(WITHRSA)) {
            alg = RSA;
        }
        return alg;
    }

    public static String getAlgFromJwk(String jwk) {
        String alg = "";
        if (jwk == null || jwk.isEmpty()) {
            alg = SHA256WITH_ECDSA;
        } else {
            String jwkDecoded = new String(Base64.getUrlDecoder().decode(jwk.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            JsonReader jsonReaderJwkDecoded = Json.createReader(new StringReader(jwkDecoded));
            JsonObject jsonJwk = jsonReaderJwkDecoded.readObject();
            String crv = jsonJwk.getString(CRV, "");
            String kty = jsonJwk.getString(KTY, "");
            if (kty.equals(RSA)) {
                alg = SHA256WITH_RSA;
            }

            if (kty.equals(EC)) {
                switch (crv) {
                case P_256:
                    alg = SHA256WITH_ECDSA;
                    break;
                case "P-512":
                    alg = SHA512WITH_ECDSA;
                    break;
                case "P-384":
                    alg = SHA3846WITH_ECDSA;
                    break;
                default:
                    alg = SHA256WITH_ECDSA;
                    break;
                }
            }
        }
        return alg;
    }

    public static byte[] getDigestBytes(byte[] input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException,
            UnsupportedEncodingException {
        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm);
        byte[] digestbytes = digest.digest(input);
        return digestbytes;
    }

    public static String getDigest(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException,
            UnsupportedEncodingException {

        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] digestbytes = digest.digest(Input.getBytes(UTF_8));
        String dig = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digestbytes);
        return dig;
    }

    public static byte[] getDigestBytes(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException,
            UnsupportedEncodingException {

        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm);
        byte[] digestbytes = digest.digest(Input.getBytes(UTF_8));
        return digestbytes;
    }

    public static boolean signatureVerified(String publicKeyBase64, byte[] message, String signaturBase64, String alg) throws InvalidKeyException,
            SignatureException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        if (alg == null || publicKeyBase64 == null || message == null || signaturBase64 == null) {
            return false;
        }
        PublicKey publicKey = getPublicKey(publicKeyBase64, getAlg(alg));
        Signature signature = Signature.getInstance(alg);
        signature.initVerify(publicKey);
        signature.update(message, 0, message.length);
        boolean verified = signature.verify(Base64.getDecoder().decode(signaturBase64));
        return verified;
    }

}
