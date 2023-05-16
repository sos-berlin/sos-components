package com.sos.joc.classes.security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.model.security.fido2.CipherTypes;

public class SOSSecurityUtil {

    private static PublicKey getPublicKey(String base64PublicKey, String alg) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey publicKey = null;
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
        KeyFactory keyFactory = KeyFactory.getInstance(alg);
        publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static byte[] encrypt(String data, String publicKey, CipherTypes cipherType, String alg) throws BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        String cipherTypeString;
        if (cipherType == null) {
            cipherTypeString = "RSA/ECB/PKCS1Padding";
        } else {
            cipherTypeString = cipherType.value();
        }
        Cipher cipher = Cipher.getInstance(cipherTypeString);
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey, alg));
        return cipher.doFinal(data.getBytes());
    }

    private static String getAlg(String sAlg) {
        String alg = "EC";

        if (sAlg.toUpperCase().endsWith("WITHDSA")) {
            alg = "DSA";
        }
        if (sAlg.toUpperCase().endsWith("WITHRSA")) {
            alg = "RSA";
        }
        return alg;
    }

    public static boolean signatureVerified(String publicKeyBase64, String message, String signaturBase64, String alg) throws InvalidKeyException,
            SignatureException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        if (alg == null || publicKeyBase64 == null || message == null || signaturBase64 == null) {
            return false;
        }
        PublicKey publicKey = getPublicKey(publicKeyBase64, getAlg(alg));
        Signature signature = Signature.getInstance(alg);
        signature.initVerify(publicKey);
        byte[] buffer = message.getBytes();
        signature.update(buffer, 0, buffer.length);
        boolean verified = signature.verify(Base64.getDecoder().decode(signaturBase64.getBytes()));
        return verified;
    }

}
