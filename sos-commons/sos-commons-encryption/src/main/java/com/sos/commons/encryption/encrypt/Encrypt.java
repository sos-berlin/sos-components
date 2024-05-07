package com.sos.commons.encryption.encrypt;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.sos.commons.encryption.EncryptionUtils;

public class Encrypt {

  public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException,
  NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    return EncryptionUtils.enOrDecrypt(algorithm, input, key, iv, Cipher.ENCRYPT_MODE);
  }
  
  public static void encryptFile(String algorithm, SecretKey key, IvParameterSpec iv, String inputFile, String outputFile)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    EncryptionUtils.enOrDecryptFile(algorithm, key, iv, inputFile, outputFile, Cipher.ENCRYPT_MODE);
  }

  public static void encryptFile(String algorithm, SecretKey key, IvParameterSpec iv, Path inputFile, Path outputFile)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    EncryptionUtils.enOrDecryptFile(algorithm, key, iv, inputFile, outputFile, Cipher.ENCRYPT_MODE);
  }

  public static String concatOutput (String encryptedKey, String ivBase64Encoded, String encryptedValue) {
    return encryptedKey.concat(" ").concat(ivBase64Encoded).concat(" ").concat(encryptedValue);
  }
  
}
