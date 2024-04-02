package com.sos.commons.encryption.executable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.sign.keys.key.KeyUtil;

public class Decrypt {

  private static final String HELP = "--help";
  private static final String KEY = "--key";
  private static final String ENCRYPTED_KEY = "--encrypt_key";
  private static final String IV = "--iv";
  private static final String IN = "--in";
  private static final String IN_FILE = "--in_file";

  private static String keyPath;
  private static String iv;
  private static String encryptedKey;
  private static String encryptedValue;
  private static String encryptedFile;

  public static String decrypt(PrivateKey privKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
      IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    String encryptedSecretKey = System.getProperty(EncryptionUtils.ENV_KEY);
    String encryptedValueWithIv = System.getProperty(EncryptionUtils.ENV_VALUE);
    String exportedIv = encryptedValueWithIv.substring(0, 24);
    String exportedValue = encryptedValueWithIv.substring(24);
    return decrypt(privKey, exportedIv, encryptedSecretKey, exportedValue);
  }

  public static String decrypt(PrivateKey privKey, String iv, String encryptedKey, String encryptedValue) throws InvalidKeyException, 
      NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException {
    SecretKey key = new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(encryptedKey.getBytes(), privKey), "AES");
    byte[] decodedIV = Base64.getDecoder().decode(iv);
    String decryptedValue = com.sos.commons.encryption.decrypt.Decrypt.decrypt(EncryptionUtils.CIPHER_ALGORITHM, encryptedValue, key,
        new IvParameterSpec(decodedIV));
    return decryptedValue;
  }
  
  public static void main(String[] args) {
    PrivateKey privKey = null;
    try {
      if (args == null || args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("--help"))) {
        printUsage();
      } else {
        for (int i = 0; i < args.length; i++) {
          String[] split = args[i].split("=", 2);
          if (args[i].startsWith(KEY + "=")) {
            keyPath = split[1];
            privKey = KeyUtil.getPrivateKeyFromString(new String(Files.readAllBytes(Paths.get(keyPath)), StandardCharsets.UTF_8));
          } else if (args[i].startsWith(IV + "=")) {
            iv = split[1];
          } else if (args[i].startsWith(ENCRYPTED_KEY + "=")) {
            encryptedKey = split[1];
          } else if (args[i].startsWith(IN + "=")) {
            encryptedValue = split[1];
          } else if (args[i].startsWith(IN_FILE + "=")) {
            encryptedFile = split[1];
          }
        }
        String decryptedValue = null;
        if(iv != null && encryptedKey != null && encryptedValue != null) {
          decryptedValue = decrypt(privKey, iv, encryptedKey, encryptedValue);
        } else {
          decryptedValue = decrypt(privKey);
        }
        if(decryptedValue != null) {
          System.out.println(decryptedValue);
        }
      }
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }

  public static void printUsage() {
    System.out.println();
    System.out.println("Decrypts a string. The encrypted string as well as the dynamically generated secret key\n and the IV have to be provided.");
//        + " are read from environment variables as " + EncryptionUtils.ENV_KEY + " and " + EncryptionUtils.ENV_VALUE + " if exist. Otherwise");
    System.out.println();
    System.out.println(" [Decrypt] [Options]");
    System.out.println();
    System.out.printf("  %-29s | %s%n", HELP, "Shows this help page, this option is exclusive and has no value");
    System.out.printf("  %-29s | %s%n", KEY + "=<PATH TO PRIVATE KEY>", "Path to the PrivateKey the encrypted secret key should be decrypted with.");
    System.out.printf("  %-29s | %s%n", IV + "=", "Base64 encoded IV.");
    System.out.printf("  %-29s | %s%n", ENCRYPTED_KEY + "=", "Base64 encoded encrypted secret key.");
    System.out.printf("  %-29s | %s%n", IN + "=", "The encrypted value to decrypt.");
//    System.out.printf("  %-29s | %s%n", IN_FILE + "=", "The encrypted file to decrypt.");
    System.out.println();
  }

}
