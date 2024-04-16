package com.sos.commons.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;

import com.sos.commons.exception.SOSException;

public class EncryptionUtils {

  private static final String AES_FORMAT = "PBKDF2WithHmacSHA256";
  private static final String KEY_ALGORITHM = "AES";
  public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
  public static final String RSA_CIPHER_ALGORITHM = "RSA/ECB/NOPADDING";
  public static final String ENV_KEY = "JS7_ENCRYPTED_KEY";
  public static final String ENV_VALUE = "JS7_ENCRYPTED_VALUE";
  public static final String ENV_FILE = "JS7_ENCRYPTED_FILE";

  public static IvParameterSpec generateIv() {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    return new IvParameterSpec(iv);
  }

  public static byte[] getIv(IvParameterSpec ivSpec) {
    return ivSpec.getIV();
  }

  public static IvParameterSpec updateIvParameterSpec(byte[] iv) {
    return new IvParameterSpec(iv);
  }

  public static SecretKey generateSecretKey(int n) throws NoSuchAlgorithmException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
    keyGenerator.init(n);
    SecretKey key = keyGenerator.generateKey();
    return key;
  }

  public static SecretKey getSecretKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance(AES_FORMAT);
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
    SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), KEY_ALGORITHM);
    return secret;
  }

  public static String enOrDecrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv, int cipherMode) throws NoSuchPaddingException,
      NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Security.addProvider(new BouncyCastleProvider());
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(cipherMode, key, iv);
    String outcome = "";
    if (Cipher.ENCRYPT_MODE == cipherMode) {
      // encrypt
      byte[] cipherText = cipher.doFinal(input.getBytes());
      outcome = Base64.getEncoder().encodeToString(cipherText);
    } else if (Cipher.DECRYPT_MODE == cipherMode) {
      // decrypt
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
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(cipherMode, key, iv);
    InputStream inputStream = Files.newInputStream(inputFile);
    OutputStream outputStream = Files.newOutputStream(outputFile);
    try {
      byte[] buffer = new byte[16];
      int bytesRead;
      if (Cipher.ENCRYPT_MODE == cipherMode) {
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          byte[] output = cipher.update(buffer, 0, bytesRead);
          if (output != null) {
            output = Base64.getEncoder().encode(output);
            outputStream.write(output);
          }
        }
        // encrypt
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
          outputBytes = Base64.getEncoder().encode(outputBytes);
          outputStream.write(outputBytes);
        }
      } else if (Cipher.DECRYPT_MODE == cipherMode) {
        buffer = new byte[24];
        // decrypt
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          byte[] output = Base64.getDecoder().decode(buffer);
          if (output != null) {
            output = cipher.update(output);
            outputStream.write(output);
          }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
          outputStream.write(outputBytes);
        }
      }
    } finally {
      inputStream.close();
      outputStream.flush();
      outputStream.close();
    }
  }

  public static byte[] encryptSymmetricKey(SecretKey secretKey, X509Certificate cert) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SOSException,
      InvalidAlgorithmParameterException {
      Security.addProvider(new BouncyCastleProvider());
    if (cert != null) {
      PublicKey publicKey = cert.getPublicKey();
      return encryptSymmetricKey(secretKey, publicKey);
    } else {
      throw new SOSException("Cannot read public key from certificate. no certificate present.");
    }
  }

  public static byte[] encryptSymmetricKey(SecretKey secretKey, PublicKey key) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SOSException,
      InvalidAlgorithmParameterException {
      Security.addProvider(new BouncyCastleProvider());
    Cipher cipher = null;
    IESParameterSpec spec = null;
    if (key != null) {
      String algorithm = key.getAlgorithm();
      if(algorithm.contains("EC")) {
        algorithm = "ECIES";
        spec = new IESParameterSpec(null, null, 256, 256, null, false);
      }
      cipher = Cipher.getInstance(algorithm);
      if (spec != null) {
          cipher.init(Cipher.ENCRYPT_MODE, key, spec);
      } else {
          cipher.init(Cipher.ENCRYPT_MODE, key);
      }
      return Base64.getEncoder().encode(cipher.doFinal(secretKey.getEncoded()));
    } else {
      throw new SOSException("Cannot read public key from certificate. no certificate present.");
    }
  }

  public static byte[] decryptSymmetricKey(byte[] encryptedSecretKey, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    Cipher cipher = null;
    IESParameterSpec spec = null;
    String algorithm = privateKey.getAlgorithm();
    if(algorithm.contains("EC")) {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
      algorithm = "ECIES";
      spec = new IESParameterSpec(null, null, 256, 256, null, false);
    }
    cipher = Cipher.getInstance(algorithm);
    if (spec != null) {
        cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
    } else {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
    }
    return cipher.doFinal(Base64.getDecoder().decode(encryptedSecretKey));
  }
}
