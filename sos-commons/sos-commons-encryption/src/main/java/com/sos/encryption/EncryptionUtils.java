package com.sos.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

  public static IvParameterSpec generateIv() {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    return new IvParameterSpec(iv);
  }

  public static SecretKey generateSecretKey(int n) throws NoSuchAlgorithmException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(n);
    SecretKey key = keyGenerator.generateKey();
    return key;
  }

  public static SecretKey getSecretKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
    SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    return secret;
  }

  public static String enOrDecrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv, int cipherMode) throws NoSuchPaddingException,
      NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(cipherMode, key, iv);
    String outcome = "";
    if (Cipher.ENCRYPT_MODE == cipherMode) {
      // encrypt
      byte[] cipherText = cipher.doFinal(input.getBytes());
      outcome = Base64.getEncoder().encodeToString(cipherText);
    } else if (Cipher.DECRYPT_MODE == cipherMode) {
      // decrypt
      cipher.init(Cipher.DECRYPT_MODE, key, iv);
      byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(input));
      outcome = new String(plainText);
    }
    return outcome;
  }

  public static void enOrDecryptFile(String algorithm, SecretKey key, IvParameterSpec iv, String inputFile, String outputFile, int cipherMode)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
      enOrDecryptFile(algorithm, key, iv, Paths.get(inputFile), Paths.get(outputFile), cipherMode);
  }

  public static void enOrDecryptFile(String algorithm, SecretKey key, IvParameterSpec iv, Path inputFile, Path outputFile, int cipherMode)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
      enOrDecryptFile(algorithm, key, iv, inputFile.toFile(), outputFile.toFile(), cipherMode);
  }

  public static void enOrDecryptFile(String algorithm, SecretKey key, IvParameterSpec iv, File inputFile, File outputFile, int cipherMode)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(cipherMode, key, iv);
    FileInputStream inputStream = new FileInputStream(inputFile);
    FileOutputStream outputStream = new FileOutputStream(outputFile);
    try {
      byte[] buffer = new byte[64];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        byte[] output = cipher.update(buffer, 0, bytesRead);
        if (output != null) {
          outputStream.write(output);
        }
      }
      if(Cipher.ENCRYPT_MODE == cipherMode) {
        // encrypt
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
          outputStream.write(outputBytes);
        }
      } else if (Cipher.DECRYPT_MODE == cipherMode) {
        // decrypt
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
          outputStream.write(outputBytes);
        }
      }
    } finally {
      inputStream.close();
      outputStream.close();
    }
  }
}
