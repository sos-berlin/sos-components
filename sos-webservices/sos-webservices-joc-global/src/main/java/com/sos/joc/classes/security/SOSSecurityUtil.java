package com.sos.joc.classes.security;

import java.io.UnsupportedEncodingException;
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

// import org.apache.commons.codec.binary.Base64;

public class SOSSecurityUtil {

    private static PublicKey getPublicKey(String base64PublicKey, String alg) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchProviderException {

        PublicKey publicKey = null;
        String publicKeyPEM = base64PublicKey.replace("-----BEGIN PUBLIC KEY-----", "").replaceAll(System.lineSeparator(), "").replace(
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

    public static String getAlgFromIANACOSEAlg(long alg) {
        switch ((int) alg) {
        case -65535:
            return "SHA1withRSA";
        case -257:
            return "SHA256withRSA";
        case -258:
            return "SHA384withRSA";
        case -259:
            return "SHA512withRSA";
        case -37:
            return "SHA256withRSAandMGF1";
        case -38:
            return "SHA384withRSAandMGF1";
        case -39:
            return "SHA512withRSAandMGF1";
        case -7:
            return "SHA256withECDSA";
        case -35:
            return "SHA384withECDSA";
        case -36:
            return "SHA512withECDSA";
        case -8:
            return "NONEwithECDSA";
        case -43:
            return "SHA256withECDSA";
        default:
            throw new UnsupportedOperationException("Unsupported Algorithm: " + alg);
        }
    }

    public static String getCurveFromFIDOECCCurveID(long curveID) {
        switch ((int) curveID) {
        case 1:
            return "P-256";
        case 2:
            return "P-384";
        case 3:
            return "P-521";
        case 6:
            return "Curve25519";
        default:
            throw new UnsupportedOperationException("Unsupported Curve" + curveID);
        }
    }

    public static String getHashAlgFromIANACOSEAlg(long alg) {
        if (alg == -65535) {
            return "SHA-1";
        } else if (alg == -257) {
            return "SHA-256";
        } else if (alg == -7) {
            return "SHA-256";
        } else {
            throw new UnsupportedOperationException("Unsupported Algorithm" + alg);
        }
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
