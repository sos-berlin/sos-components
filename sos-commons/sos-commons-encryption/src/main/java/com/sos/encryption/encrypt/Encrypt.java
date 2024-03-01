package com.sos.encryption.encrypt;

import java.io.File;
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

import com.sos.encryption.EncryptionUtils;

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

  public static void encryptFile(String algorithm, SecretKey key, IvParameterSpec iv, File inputFile, File outputFile)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    EncryptionUtils.enOrDecryptFile(algorithm, key, iv, inputFile, outputFile, Cipher.ENCRYPT_MODE);
  }

}
