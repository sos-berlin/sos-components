package com.sos.commons.encryption.decrypt;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.exception.SOSEncryptionException;

public class Decrypt {

  public static String decrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException,
  NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    return EncryptionUtils.enOrDecrypt(algorithm, input, key, iv, Cipher.DECRYPT_MODE);
  }

  public static void decryptFile(String algorithm, SecretKey key, IvParameterSpec iv, String inputFile, String outputFile)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    EncryptionUtils.enOrDecryptFile(algorithm, key, iv, inputFile, outputFile, Cipher.DECRYPT_MODE);
  }

  public static void decryptFile(String algorithm, SecretKey key, IvParameterSpec iv, Path inputFile, Path outputFile)
      throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    EncryptionUtils.enOrDecryptFile(algorithm, key, iv, inputFile, outputFile, Cipher.DECRYPT_MODE);
  }

  public static String decrypt(EncryptedValue encryptedValue, PrivateKey privKey) throws SOSEncryptionException {
      try {
          return enOrDecrypt(encryptedValue.getEncryptedValue(), getSecretKey(encryptedValue.getEncryptedSymmetricKey(), privKey), encryptedValue.getBase64EncodedIv());
      } catch (Throwable e) {
          throw new SOSEncryptionException(String.format("[%s][%s]%s", encryptedValue.getPropertyName(), encryptedValue.getPropertyValue(),
                  e.toString()), e);
      }
  }

  private static String enOrDecrypt(String encryptedValue, SecretKey key, String base64encodedIv) throws InvalidKeyException, NoSuchPaddingException,
          NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
      return EncryptionUtils.enOrDecrypt(EncryptionUtils.CIPHER_ALGORITHM, encryptedValue, key, new IvParameterSpec(Base64.getDecoder().decode(
              base64encodedIv)), Cipher.DECRYPT_MODE);
  }

  private static SecretKey getSecretKey(String symmetricKey, PrivateKey privKey) throws InvalidKeyException, NoSuchAlgorithmException,
          NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
      return new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(symmetricKey.getBytes(), privKey), EncryptionUtils.CIPHER_ALGORITHM);
  }
}
