package com.sos.commons.encryption.executable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.sign.keys.key.KeyUtil;

public class Decrypt {

  private static final String HELP = "--help";
  private static final String KEY = "--key";
  private static final String KEY_PWD = "--key-password";
  private static final String ENCRYPTED_KEY = "--encrypted-key";
  private static final String IV = "--iv";
  private static final String IN = "--in";
  private static final String IN_FILE = "--infile";
  private static final String OUT_FILE = "--outfile";

  private static String keyPath;
  private static String iv;
  private static String encryptedKey;
  private static String encryptedValue;
  private static String encryptedFile;
  private static String outFile;
  private static String keyPwd;

  public static String decrypt(PrivateKey privKey, String iv, String encryptedKey, String encryptedValue)
      throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
      BadPaddingException, InvalidAlgorithmParameterException {
    SecretKey key = new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(encryptedKey.getBytes(), privKey), "AES");
    byte[] decodedIV = Base64.getDecoder().decode(iv);
    String decryptedValue = com.sos.commons.encryption.decrypt.Decrypt.decrypt(EncryptionUtils.CIPHER_ALGORITHM,
        encryptedValue, key, new IvParameterSpec(decodedIV));
    return decryptedValue;
  }
  
  public static void decryptFile(PrivateKey privKey, String iv, Path inFile, Path outFile) throws InvalidKeyException,
      NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException,
      IllegalBlockSizeException, IOException {
    SecretKey key = new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(encryptedKey.getBytes(), privKey), "AES");
    byte[] decodedIV = Base64.getDecoder().decode(iv);
    com.sos.commons.encryption.decrypt.Decrypt.decryptFile(EncryptionUtils.CIPHER_ALGORITHM, key,
        new IvParameterSpec(decodedIV), inFile, outFile);
  }
  
  public static void main(String[] args) {
    PrivateKey privKey = null;
    String fileContent = null;
    try {
      if (args == null || args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("--help"))) {
        printUsage();
      } else {
        for (int i = 0; i < args.length; i++) {
          String[] split = args[i].split("=", 2);
          if (args[i].startsWith(KEY + "=")) {
            keyPath = split[1];
            fileContent = new String(Files.readAllBytes(Paths.get(keyPath)), StandardCharsets.UTF_8);
          } else if (args[i].startsWith(KEY_PWD + "=")) {
            keyPwd = split[1];
          } else if (args[i].startsWith(IV + "=")) {
            iv = split[1];
          } else if (args[i].startsWith(ENCRYPTED_KEY + "=")) {
            encryptedKey = split[1];
          } else if (args[i].startsWith(IN + "=")) {
            encryptedValue = split[1];
          } else if (args[i].startsWith(IN_FILE + "=")) {
            encryptedFile = split[1];
          } else if (args[i].startsWith(OUT_FILE + "=")) {
            outFile = split[1];
          }
        }
        if(fileContent != null) {
          if(fileContent.contains("ENCRYPTED")) {
            if(keyPwd != null) {
              privKey = KeyUtil.getPrivateEncryptedKey(fileContent, keyPwd);
            } else {
              throw new SOSMissingDataException("The parameter --key-pwd is required for a password encrypted private key.");
            }
          } else {
            privKey = KeyUtil.getPrivateKeyFromString(fileContent);
          }
        }
        String decryptedValue = null;
        if(keyPath == null || encryptedKey == null || iv == null || (encryptedValue == null && encryptedFile == null)) {
          if(keyPath == null) {
            if(encryptedKey == null && iv == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters --key, --encrypted-key and --iv and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters --key, --encrypted-key and --iv are not set, but are required!");
              }
            } else if (iv == null && encryptedKey != null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters --key and --iv and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters --key and --iv are not set, but are required!");
              }
            } else if (iv != null && encryptedKey == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters --key and --encrypted-key and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters --key and --encrypted-key are not set, but are required!");
              }
            } else {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameter --key and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameter --key is not set, but is required!");
              }
            }
          } else {
            if(encryptedKey == null && iv == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters --encrypted-key and --iv and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters --encrypted-key and --iv are not set, but are required!");
              }
            } else if (iv == null && encryptedKey != null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters --iv and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters --iv is not set, but is required!");
              }
            } else if (iv != null && encryptedKey == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters --encrypted-key and at least one of the parameters --in or --in-file are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters --encrypted-key is not set, but is required!");
              }
            }
          }
        }
        if(keyPath != null && iv != null && encryptedKey != null && (encryptedValue != null || encryptedFile != null)) {
          if (encryptedValue != null) {
            decryptedValue = decrypt(privKey, iv, encryptedKey, encryptedValue);
          } else if (encryptedFile != null){
            if(outFile == null) {
              throw new SOSMissingDataException("The parameters --outfile is not set, but is required!");
            }
            decryptFile(privKey, iv, Paths.get(encryptedFile), Paths.get(outFile));
          }
        }
        if(decryptedValue != null) {
          System.out.println(decryptedValue);
        }
      }
      System.exit(0);
    } catch (SOSMissingDataException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
  }

  public static void printUsage() {
    System.out.println();
    System.out.println("Decrypts a string or file. The encrypted content as well as the dynamically generated secret key\n and the IV have to be provided.");
    System.out.println();
    System.out.println(" [Decrypt] [Options]");
    System.out.println();
    System.out.printf("  %-29s | %s%n", HELP, "Shows this help page, this option is exclusive and has no value");
    System.out.printf("  %-29s | %s%n", KEY + "=<PATH TO PRIVATE KEY>", "Path to the PrivateKey the encrypted secret key should be decrypted with.");
    System.out.printf("  %-29s | %s%n", KEY_PWD + "=", "The password for the private key in case a passphrase is used.");
    System.out.printf("  %-29s | %s%n", IV + "=", "Base64 encoded IV.");
    System.out.printf("  %-29s | %s%n", ENCRYPTED_KEY + "=", "Base64 encoded encrypted secret key.");
    System.out.printf("  %-29s | %s%n", IN + "=", "The encrypted value to decrypt.");
    System.out.printf("  %-29s | %s%n", IN_FILE + "=", "The path to the encrypted file to decrypt.");
    System.out.printf("  %-29s | %s%n", OUT_FILE + "=", "The path to the output file holding the decrypted content.");
    System.out.println();
  }

}
