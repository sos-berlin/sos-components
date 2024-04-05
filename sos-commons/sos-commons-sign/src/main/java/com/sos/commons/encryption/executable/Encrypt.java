package com.sos.commons.encryption.executable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.sign.keys.key.KeyUtil;

public class Encrypt {

  private static final String HELP = "--help";
  private static final String CERT = "--cert";
  private static final String IN = "--in";
  private static final String IN_FILE = "--infile";
  private static final String OUT_FILE = "--outfile";

  private static String certPath;
  private static String filePath;
  private static String outfilePath;

  private static SecretKey createSecretKey() throws NoSuchAlgorithmException {
    return EncryptionUtils.generateSecretKey(128);
  }

  private static IvParameterSpec createIV() {
    return EncryptionUtils.generateIv();
  }

  public static void encrypt(X509Certificate cert, String input) throws NoSuchAlgorithmException, InvalidKeyException,
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, 
      SOSException {
    encrypt(cert, null, input);
  }

  public static void encrypt(PublicKey pubKey, String input) throws NoSuchAlgorithmException, InvalidKeyException, 
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, 
      SOSException {
    encrypt(null, pubKey, input);
  }

  private static void encrypt(X509Certificate cert, PublicKey pubKey, String input) throws NoSuchAlgorithmException, 
      InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, 
      InvalidAlgorithmParameterException, SOSException {
    IvParameterSpec iv = createIV();
    SecretKey key = createSecretKey();
    String encryptedKey = null;
    if(cert != null) {
      encryptedKey = new String(EncryptionUtils.encryptSymmetricKey(key, cert));
    } else if(pubKey != null){
      encryptedKey = new String(EncryptionUtils.encryptSymmetricKey(key, pubKey));
    }
    String ivBase64Encoded = new String(Base64.getEncoder().encode(iv.getIV()));
    String encryptedValue = com.sos.commons.encryption.encrypt.Encrypt.encrypt(EncryptionUtils.CIPHER_ALGORITHM, input, key, iv);
    // concatenating the output
    String output = encryptedKey.concat(" ").concat(ivBase64Encoded).concat(" ").concat(encryptedValue);
    System.out.println(output);
  }

  public static void encryptFile(PublicKey pubKey, Path filePath, Path outfile) throws InvalidKeyException, 
      NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException, SOSException, IOException {
    encryptFile(null, pubKey, filePath, outfile);
  }

  public static void encryptFile(X509Certificate cert, Path filePath, Path outfile) throws InvalidKeyException,
      NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException, SOSException, IOException {
    encryptFile(cert, null, filePath, outfile);
  }
  
  private static void encryptFile(X509Certificate cert, PublicKey pubKey, Path filePath, Path outfile) throws 
      NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, 
      BadPaddingException, SOSException, InvalidAlgorithmParameterException, IOException {
    IvParameterSpec iv = createIV();
    SecretKey key = createSecretKey();
    String encryptedKey = null;
    if(cert != null) {
      encryptedKey = new String(EncryptionUtils.encryptSymmetricKey(key, cert));
    } else if (pubKey != null) {
      encryptedKey = new String(EncryptionUtils.encryptSymmetricKey(key, pubKey));
    }
    String ivBase64Encoded = new String(Base64.getEncoder().encode(iv.getIV()));
    com.sos.commons.encryption.encrypt.Encrypt.encryptFile(EncryptionUtils.CIPHER_ALGORITHM, key, iv, filePath, outfile);
    String output = encryptedKey.concat(" ").concat(ivBase64Encoded).concat(" ").concat(outfile.toString());
    System.out.println(output);
  }

  public static void main(String[] args) {
    X509Certificate cert = null;
    PublicKey pubKey = null;
    String input = null;
    try {
      if (args == null || args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("--help"))) {
        printUsage();
      } else {
        for (int i = 0; i < args.length; i++) {
          String[] split = args[i].split("=", 2);
          if (args[i].startsWith(CERT + "=")) {
            certPath = split[1];
            String fileContent = new String(Files.readAllBytes(Paths.get(certPath)), StandardCharsets.UTF_8);
            if (fileContent.contains("CERTIFICATE")) {
              cert = KeyUtil.getX509Certificate(fileContent);
            } else {
              try {
                pubKey = KeyUtil.getRSAPublicKeyFromString(fileContent);
              } catch (Exception e) {
                try {
                  pubKey = KeyUtil.getECDSAPublicKeyFromString(fileContent);
                } catch (Exception e1) {
                  try {
                    pubKey = KeyUtil.convertToRSAPublicKey(KeyUtil.stripFormatFromPublicKey(fileContent).getBytes());
                  } catch (Exception e2) {
                    pubKey = KeyUtil.getECPublicKeyFromString(KeyUtil.stripFormatFromPublicKey(fileContent).getBytes());
                  }
                }
              }
            }
          } else if (args[i].startsWith(IN + "=")) {
            input = split[1];
          } else if (args[i].startsWith(IN_FILE + "=")) {
            filePath = split[1];
          } else if (args[i].startsWith(OUT_FILE + "=")) {
            outfilePath = split[1];
          }
        }
        if (certPath == null || (input == null && filePath == null)) {
          if (certPath == null) {
            if (input == null && filePath == null) {
              throw new SOSMissingDataException("The parameter --cert and at least one of the parameters " + "--in or --in-file is required!");
            } else {
              throw new SOSMissingDataException("The parameter --cert is not set, but is required!");
            }
          } else if (input == null && filePath == null) {
            throw new SOSMissingDataException("At least one of the parameters --in or --in-file is required!");
          }
        }
        if (input != null) {
          if (cert != null) {
            encrypt(cert, input);
          } else {
            encrypt(pubKey, input);
          }
        } else if (filePath != null) {
          if (outfilePath == null) {
            outfilePath = filePath.concat(".encrypted");
          }
          if (cert != null) {
            encryptFile(cert, Paths.get(filePath), Paths.get(outfilePath));
          } else {
            encryptFile(pubKey, Paths.get(filePath), Paths.get(outfilePath));
          }
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
    System.out.println("Encrypts a string or a file. The encrypted content as well as the dynamically generated secret key\n"
        + " are published as a single string to STDOUT separated with blanks like <encrypted key> <iv> <encrypted value>"
        + " or as <encrypted key> <iv> <path to encrypted file> respectively.");
    System.out.println();
    System.out.println(" [Encrypt] [Options]");
    System.out.println();
    System.out.printf("  %-29s | %s%n", HELP, "Shows this help page, this option is exclusive and has no value");
    System.out.printf("  %-29s | %s%n", CERT + "=<PATH TO CERTIFICATE>",
        "Path to the X509 certificate, the dynamically created secret key has to be encrypted with.");
    System.out.printf("  %-29s | %s%n", IN + "=<VALUE>", "The input value, that should be encrypted with the dynamically created secret key.");
    System.out.printf("  %-29s | %s%n", IN_FILE + "=<PATH>",
        "The path to the input file, that should be encrypted with the dynamically created secret key.");
    System.out.printf("  %-29s | %s%n", OUT_FILE + "=<PATH>", "The path to the output file, that holds the encrypted content.");
    System.out.println();
  }

}