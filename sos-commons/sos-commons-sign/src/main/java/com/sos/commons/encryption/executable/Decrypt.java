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
  private static final String HELP_SHORT = "-h";
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
  private static String encryptedInput;
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
      if (args == null || args.length == 0 || (args.length == 1 && (args[0].equalsIgnoreCase(HELP) || args[0].equalsIgnoreCase(HELP_SHORT)))) {
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
            encryptedInput = split[1];
            if(encryptedInput.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER)) {
              encryptedInput = encryptedInput.substring(EncryptionUtils.ENCRYPTION_IDENTIFIER.length());
            }
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
              throw new SOSMissingDataException("The parameter " + KEY_PWD + " is required for a password encrypted private key.");
            }
          } else {
            privKey = KeyUtil.getPrivateKeyFromString(fileContent);
          }
        }
        String decryptedValue = null;
        if(encryptedInput != null && encryptedInput.contains(" ")) {
          if(outFile != null) {
            String[] splittedValues = encryptedInput.split(" ");
            encryptedKey = splittedValues[0];
            iv = splittedValues[1];
            if(encryptedFile == null) {
              encryptedFile = splittedValues[2];
            }
          } else {
            String[] splittedValues = encryptedInput.split(" ");
            encryptedKey = splittedValues[0];
            iv = splittedValues[1];
            encryptedValue = splittedValues[2];
          }
        } else {
            encryptedValue = encryptedInput;
        }
        if(keyPath == null || encryptedKey == null || iv == null || (encryptedValue == null && encryptedFile == null)) {
          if(keyPath == null) {
            if(encryptedKey == null && iv == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters " + KEY + ", " + ENCRYPTED_KEY + " and " + IV 
                    + " and at least one of the parameters " + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters " + KEY + ", " + ENCRYPTED_KEY + " and " + IV 
                    + " are not set, but are required!");
              }
            } else if (iv == null && encryptedKey != null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters " + KEY + " and " + IV + " and at least one of the parameters "
                    + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters " + KEY + " and " + IV + " are not set, but are required!");
              }
            } else if (iv != null && encryptedKey == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters " + KEY + " and " + ENCRYPTED_KEY + " and at least "
                    + "one of the parameters " + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters " + KEY + " and " + ENCRYPTED_KEY + " are not set,"
                    + " but are required!");
              }
            } else {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameter " + KEY + " and at least one of the parameters "
                    + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameter " + KEY + " is not set, but is required!");
              }
            }
          } else {
            if(encryptedKey == null && iv == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters " + ENCRYPTED_KEY + " and " + IV + " and at least"
                    + " one of the parameters " + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters " + ENCRYPTED_KEY + " and " + IV + " are not set,"
                    + " but are required!");
              }
            } else if (iv == null && encryptedKey != null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters " + IV + " and at least one of the parameters "
                    + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters " + IV + " is not set, but is required!");
              }
            } else if (iv != null && encryptedKey == null) {
              if(encryptedValue == null && encryptedFile == null) {
                throw new SOSMissingDataException("The parameters " + ENCRYPTED_KEY + " and at least one of "
                    + "the parameters " + IN + " or " + IN_FILE + " are not set, but are required!");
              } else {
                throw new SOSMissingDataException("The parameters " + ENCRYPTED_KEY + " is not set, but is required!");
              }
            }
          }
        }
        if(keyPath != null && iv != null && encryptedKey != null && (encryptedValue != null || encryptedFile != null)) {
          if (encryptedValue != null) {
            decryptedValue = decrypt(privKey, iv, encryptedKey, encryptedValue);
          } else if (encryptedFile != null){
            if(outFile == null) {
              throw new SOSMissingDataException("The parameter " + OUT_FILE + " is not set, but is required!");
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
    } catch (Throwable e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
  }

  public static void printUsage() {
    System.out.println();
    System.out.println("Usage: decrypt [Options] [Switches]");
    System.out.println();
    System.out.println("  Options:");
    System.out.printf("  %-29s | %s%n", KEY + "=<path>", "path to private key file for decryption.");
    System.out.printf("  %-29s | %s%n", KEY_PWD + "=<password>", "passphrase for the private key.");
    System.out.printf("  %-29s | %s%n", IV + "=<initialization-vector>", "base64 encoded initialization vector (returned by encryption).");
    System.out.printf("  %-29s | %s%n", ENCRYPTED_KEY + "=<key>", "base64 encoded encrypted symmetric key (returned by encryption).");
    System.out.printf("  %-29s | %s%n", IN + "=<encryption-result>", "result of previous encryption or encrypted secret.");
    System.out.printf("  %-29s | %s%n", IN_FILE + "=<path-to-file>", "path to encrypted input file.");
    System.out.printf("  %-29s | %s%n", OUT_FILE + "=<path-to-file>", "path to decrypted output file.");
    System.out.println();
    System.out.println("  Switches:");
    System.out.printf("  %-29s | %s%n", HELP +" | " + HELP_SHORT, "displays usage. This switch is exclusive without any [Options].");
    System.out.println();
  }

}
