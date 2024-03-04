package com.sos.commons.sign;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.encryption.EncryptionUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EncryptionTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionTest.class);

  @BeforeClass
  public static void logTestsStarted() {
    LOGGER.trace("****************************  Encryption/Decryption Tests started  *****************************");
  }

  @AfterClass
  public static void logTestsFinished() {
    LOGGER.trace("****************************  Encryption/Decryption Tests finished  ****************************");
  }

  @Ignore
  @Test
  public void test01SecretKeyEncryption() throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, CertificateException, InvalidKeySpecException {
    LOGGER.trace("*************  Test 'create secret key encrypt it and decrypt it again' started  ****************");
    // Paths
    Path certificatePath = Paths.get("C:/sp/devel/js7/keys/sp.crt"); 
    Path privKeyPath = Paths.get("C:/sp/devel/js7/keys/sp.key"); 
    X509Certificate cert = KeyUtil.getX509Certificate(certificatePath);
    String privateKeyString = new String(Files.readAllBytes(privKeyPath), StandardCharsets.UTF_8);
    // objects for en-/decryption
    SecretKey key = EncryptionUtils.generateSecretKey(128);
    String algorithm = "AES/CBC/PKCS5Padding";
    IvParameterSpec ivParameterSpec = EncryptionUtils.generateIv();
    LOGGER.trace("original symmetric key:");
    String symmetricKey = new String(key.getEncoded());
    LOGGER.trace(symmetricKey);
    byte[] encryptedKey = EncryptionUtils.encryptSymmetricKey(key, cert);
    String encryptedKeyString = new String(encryptedKey);
    LOGGER.trace("encrypted symmetric key:");
    LOGGER.trace(encryptedKeyString);
    byte[] decryptedKey = EncryptionUtils.decryptSymmetricKey(encryptedKey, KeyUtil.getPrivateKeyFromString(privateKeyString));
    String decryptedKeyString = new String(decryptedKey);
    LOGGER.trace("decrypted symmetric key:");
    LOGGER.trace(decryptedKeyString);
    Assert.assertEquals(decryptedKeyString, symmetricKey);
    LOGGER.trace("*************  Test 'create secret key encrypt it and decrypt it again' finished  ***************");
  }

}
