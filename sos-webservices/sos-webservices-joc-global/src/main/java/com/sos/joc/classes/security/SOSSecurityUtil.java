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

// import org.apache.commons.codec.binary.Base64;

public class SOSSecurityUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSecurityUtil.class);

    private static PublicKey getPublicKey(String base64PublicKey, String alg) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchProviderException {

        PublicKey publicKey = null;
        String publicKeyPEM = base64PublicKey.replace("-----BEGIN PUBLIC KEY-----", "").replace("\n", "").replace("\r", "").replace(
                "-----END PUBLIC KEY-----", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance(alg);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    private static String getAlg(String sAlg) {
        String alg = "EC";

        if (sAlg.toUpperCase().endsWith("WITHDSA")) {
            alg = "DSA";
        }
        if (sAlg.toUpperCase().endsWith("WITHECDSA")) {
            alg = "EC";
        }
        if (sAlg.toUpperCase().endsWith("WITHRSA")) {
            alg = "RSA";
        }   
        return alg;
    }

    public static String getAlgFromJwk(String jwk) {
        String jwkDecoded = new String(Base64.getUrlDecoder().decode(jwk.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        JsonReader jsonReaderJwkDecoded = Json.createReader(new StringReader(jwkDecoded));
        JsonObject jsonJwk = jsonReaderJwkDecoded.readObject();
        String crv = jsonJwk.getString("crv", "");
        String kty = jsonJwk.getString("kty", "");
        String alg = "";
        if (kty.equals("RSA")) {
            alg = "SHA256withRSA";
        }

        if (kty.equals("EC")) {
            switch (crv) {
            case "P-256":
                alg = "SHA256withECDSA";
                break;
            case "P-512":
                alg = "SHA512withECDSA";
                break;
            case "P-384":
                alg = "SHA3846withECDSA";
                break;
            default:
                alg = "SHA256withECDSA";
                break;
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
        byte[] digestbytes = digest.digest(Input.getBytes("UTF-8"));
        String dig = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digestbytes);
        return dig;
    }

    public static byte[] getDigestBytes(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException,
            UnsupportedEncodingException {

        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm);
        byte[] digestbytes = digest.digest(Input.getBytes("UTF-8"));
        return digestbytes;
    }

    public static boolean signatureVerified(String publicKeyBase64, byte[] message, String signaturBase64, String alg) throws InvalidKeyException,
            SignatureException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        if (alg == null || publicKeyBase64 == null || message == null || signaturBase64 == null) {
            LOGGER.info("-- something is null");
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
