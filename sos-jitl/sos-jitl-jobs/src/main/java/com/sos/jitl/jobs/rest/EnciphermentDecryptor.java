package com.sos.jitl.jobs.rest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class EnciphermentDecryptor {

    // Load PKCS#8 RSA private key (.pem / .key)
    private static PrivateKey loadPrivateKey(Path privateKeyPath) throws Exception {
        String key = Files.readString(privateKeyPath)
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static byte[] rsaDecrypt(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsa.init(Cipher.DECRYPT_MODE, privateKey);
        return rsa.doFinal(data);
    }

    private static String aesDecrypt(byte[] encryptedData, byte[] aesKey, byte[] iv) throws Exception {
        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        byte[] plainBytes = aes.doFinal(encryptedData);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }
    
    public static String decryptValue(String encrypted, Path privateKeyPath) throws Exception {
    	
        if (encrypted == null || !encrypted.startsWith("enc:")) {
            return encrypted;
        }

        // Remove prefix
        encrypted = encrypted.substring("enc:".length()).trim();

        // Split into the three expected parts
        String[] parts = encrypted.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted format. Expected 3 parts.");
        }

        byte[] encryptedAesKey = Base64.getDecoder().decode(parts[0]);
        byte[] iv = Base64.getDecoder().decode(parts[1]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[2]);

        // Load private key
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);

        // Decrypt AES key using RSA
        byte[] aesKey = rsaDecrypt(encryptedAesKey, privateKey);

        // AES decrypt data
        return aesDecrypt(encryptedData, aesKey, iv);
    }
}
