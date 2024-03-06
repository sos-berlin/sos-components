package com.sos.encryption;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.encryption.decrypt.Decrypt;
import com.sos.encryption.encrypt.Encrypt;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EncryptionTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionTests.class);

  @BeforeClass
  public static void logTestsStarted() {
    LOGGER.trace("****************************  Encryption/Decryption Tests started  *****************************");
  }

  @AfterClass
  public static void logTestsFinished() {
    LOGGER.trace("****************************  Encryption/Decryption Tests finished  ****************************");
  }

  @Test
  public void test01EncryptTextAndDecrypt() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
      InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
    LOGGER.trace("******************  Test encrypt a String and decrypt it afterwards started  *******************");
    String input = "myTestInput";
    SecretKey key = EncryptionUtils.generateSecretKey(128);
    IvParameterSpec ivParameterSpec = EncryptionUtils.generateIv();
    String algorithm = "AES/CBC/PKCS5Padding";
    String cipherText = Encrypt.encrypt(algorithm, input, key, ivParameterSpec);
    String plainText = Decrypt.decrypt(algorithm, cipherText, key, ivParameterSpec);
    LOGGER.trace(String.format("input: %1$s", input));
    LOGGER.trace(String.format("algorithm: %1$s", algorithm));
    LOGGER.trace(String.format("encrypted text: %1$s", cipherText));
    LOGGER.trace(String.format("decrypted text: %1$s", plainText));
    Assert.assertEquals(input, plainText);
    LOGGER.trace("******************  Test encrypt a String and decrypt it afterwards finished  ******************");
  }

  @Test
  public void test02EncryptFileAndDecrypt() throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
    LOGGER.trace("*  Test encrypt a source file, store new encrypted file, decrypt and store new target started  *");
    System.setProperty("file.encoding", "UTF-8");
    // Paths
    Path userdir = Paths.get(System.getProperty("user.dir"));
    Path input = userdir.resolve("src/test/resources/TestFile.txt");
    Path encrypted = userdir.resolve("target/outcome/TestFile.txt.encrypted");
    Path decrypted = userdir.resolve("target/outcome/TestFile.txt.decrypted");
    // source and target files, folders will be created if not already exist
    File inputFile = input.toFile();
    if(!Files.exists(encrypted.getParent())) {
      Files.createDirectory(encrypted.getParent());
    }
    if(Files.exists(encrypted)) {
      Files.delete(encrypted);
    }
    Files.createFile(encrypted);
    if(!Files.exists(decrypted.getParent())) {
      Files.createDirectory(decrypted.getParent());
    }
    if(Files.exists(decrypted)) {
      Files.delete(decrypted);
    }
    Files.createFile(decrypted);
    // objects for en-/decryption
    SecretKey key = EncryptionUtils.generateSecretKey(128);
    String algorithm = "AES/CBC/PKCS5Padding";
    IvParameterSpec ivParameterSpec = EncryptionUtils.generateIv();
    
    Encrypt.encryptFile(algorithm, key, ivParameterSpec, input, encrypted);
    Decrypt.decryptFile(algorithm, key, ivParameterSpec, encrypted, decrypted);
    LOGGER.trace("[INPUT]\n" + new String(Files.readAllBytes(input), StandardCharsets.UTF_8));
    LOGGER.trace("[ENCRYPTED]\n" + new String(Files.readAllBytes(encrypted), StandardCharsets.UTF_8));
    LOGGER.trace("[DECRYPTED]\n" + new String(Files.readAllBytes(decrypted), StandardCharsets.UTF_8));
    Assert.assertEquals(new String(Files.readAllBytes(input)), new String(Files.readAllBytes(decrypted)));
    LOGGER.trace("*  Test encrypt a source file, store new encrypted file, decrypt and store new target finished *");
  }
  
  @Test
  public void testIvParameterSpec() {
    IvParameterSpec ivSpecOrig =  EncryptionUtils.generateIv();
    byte[] iv = EncryptionUtils.getIv(ivSpecOrig);
    IvParameterSpec ivSpecAfter = EncryptionUtils.updateIvParameterSpec(iv);
    Assert.assertEquals(new String(ivSpecOrig.getIV()), new String(ivSpecAfter.getIV()));
  }

}
