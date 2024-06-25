package com.sos.commons.sign;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.executable.Decrypt;
import com.sos.commons.encryption.executable.Encrypt;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.key.KeyUtil;

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
      BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, CertificateException, InvalidKeySpecException,
      SOSException {
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
    LOGGER.trace("original symmetric key Base64 encoded:");
    String base64SymmetricKey = Base64.getEncoder().encodeToString(key.getEncoded());
    LOGGER.trace(base64SymmetricKey);
    byte[] encryptedKey = EncryptionUtils.encryptSymmetricKey(key, cert);
    String encryptedKeyString = new String(encryptedKey);
    LOGGER.trace("encrypted symmetric key:");
    LOGGER.trace(encryptedKeyString);
    byte[] decodedBase64Key = Base64.getDecoder().decode(base64SymmetricKey);
    String decodedKey = new String(decodedBase64Key);
    LOGGER.trace("decoded symmetric key:");
    LOGGER.trace(decodedKey);
    byte[] decryptedKey = EncryptionUtils.decryptSymmetricKey(encryptedKey, KeyUtil.getPrivateKeyFromString(privateKeyString));
//    byte[] decryptedKey = EncryptionUtils.decryptSymmetricKey(decodedKey.getBytes(), KeyUtil.getPrivateKeyFromString(privateKeyString));
    String decryptedKeyString = new String(decryptedKey);
    LOGGER.trace("decrypted symmetric key:");
    LOGGER.trace(decryptedKeyString);
    Assert.assertEquals(decryptedKeyString, symmetricKey);
    LOGGER.trace("*************  Test 'create secret key encrypt it and decrypt it again' finished  ***************");
  }

  @Test
  @Ignore
  public void test02FileEncryption() throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException,
      BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, CertificateException, InvalidKeySpecException,
      SOSException {
    LOGGER.trace("*************  Test 'create encrypted file and decrypt it again' started  ***********************");
    // Paths
    Path certificatePath = Paths.get("C:/sp/devel/js7/keys/sp.crt"); 
    Path privKeyPath = Paths.get("C:/sp/devel/js7/keys/sp.key"); 
    X509Certificate cert = KeyUtil.getX509Certificate(certificatePath);
    String privateKeyString = new String(Files.readAllBytes(privKeyPath), StandardCharsets.UTF_8);
    // objects for en-/decryption
    SecretKey key = EncryptionUtils.generateSecretKey(128);
    LOGGER.trace("original symmetric key:");
    String symmetricKey = new String(key.getEncoded());
    LOGGER.trace(symmetricKey);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    LOGGER.trace("original symmetric key Base64 encoded:");
    String base64SymmetricKey = Base64.getEncoder().encodeToString(key.getEncoded());
    LOGGER.trace(base64SymmetricKey);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    byte[] encryptedKey = EncryptionUtils.encryptSymmetricKey(key, cert);
    String encryptedKeyString = new String(encryptedKey);
    LOGGER.trace("encrypted symmetric key (binary):");
    LOGGER.trace(encryptedKeyString);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    IvParameterSpec ivParameterSpec = EncryptionUtils.generateIv();
    byte[] iv = ivParameterSpec.getIV();
    byte[] ivBase64Encoded = Base64.getEncoder().encode(iv);
    String ivBase64EncodedString = new String(ivBase64Encoded);
    LOGGER.trace("IV base64 encoded:");
    LOGGER.trace(ivBase64EncodedString);
    String algorithm = "AES/CBC/PKCS5Padding";
    String value = "this statement will be en- and decrypted";
    LOGGER.trace("original string:");
    LOGGER.trace(value);
    byte[] ivDecoded = Base64.getDecoder().decode(ivBase64EncodedString);
    IvParameterSpec ivParamSpecDecoded = new IvParameterSpec(ivDecoded);
    String encryptedValue = EncryptionUtils.enOrDecrypt(algorithm, value, key, ivParamSpecDecoded, Cipher.ENCRYPT_MODE);
    LOGGER.trace("encrypted string:");
    LOGGER.trace(encryptedValue);
    String decryptedValue = EncryptionUtils.enOrDecrypt(algorithm, encryptedValue, key, ivParameterSpec, Cipher.DECRYPT_MODE);
    LOGGER.trace("decrypted string:");
    LOGGER.trace(decryptedValue);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    byte[] decodedBase64SymmetricKey = Base64.getDecoder().decode(base64SymmetricKey);
    SecretKey decodedSecretKey = new SecretKeySpec(decodedBase64SymmetricKey, "AES");
    String decodedSecretKeyString = new String(decodedSecretKey.getEncoded());
    LOGGER.trace("symmetric key Base64 decoded:");
    LOGGER.trace(decodedSecretKeyString);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    byte[] decryptedKey = EncryptionUtils.decryptSymmetricKey(encryptedKey, KeyUtil.getPrivateKeyFromString(privateKeyString));
    String decryptedKeyString = new String(decryptedKey);
    LOGGER.trace("decrypted symmetric key:");
    LOGGER.trace(decryptedKeyString);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    byte[] decodedBase64Key = Base64.getDecoder().decode(base64SymmetricKey);
    String decodedKey = new String(decodedBase64Key);
    LOGGER.trace("decoded symmetric key:");
    LOGGER.trace(decodedKey);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    Assert.assertEquals(decryptedKeyString, symmetricKey);
    LOGGER.trace("*************  Test 'create encrypted file and decrypt it again' finished  **********************");
  }

  @Test
//  @Ignore
  public void test03PWEncryption() throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
        SOSException, InvalidKeySpecException {
    LOGGER.trace("*************  Test 'encrypt password, export as env variable and decrypt' started  *************");
    String pwd = "jobscheduler";
    Path certificatePath = Paths.get("src/test/resources/rsa_test.crt"); 
    X509Certificate cert = KeyUtil.getX509Certificate(certificatePath);
    SecretKey key = EncryptionUtils.generateSecretKey(256);
    String origSecretKey = new String(key.getEncoded());
    LOGGER.trace("secret:");
    LOGGER.trace(origSecretKey);
    byte[] encryptedKey = EncryptionUtils.encryptSymmetricKey(key, cert);
    String algorithm = EncryptionUtils.CIPHER_ALGORITHM;
    IvParameterSpec ivParameterSpec = EncryptionUtils.generateIv();
    byte[] ivBase64Encoded = Base64.getEncoder().encode(ivParameterSpec.getIV());
    String encryptedPwd = EncryptionUtils.enOrDecrypt(algorithm, pwd, key, ivParameterSpec, Cipher.ENCRYPT_MODE);
    LOGGER.trace("encrypted Pwd:");
    LOGGER.trace(encryptedPwd);
    String exportKey = new String(encryptedKey);
    LOGGER.trace("encrypted Key:");
    LOGGER.trace(exportKey);
    String exportIvPlusPwd = new String(exportKey).concat(" ").concat(new String(ivBase64Encoded)).concat(" ").concat(encryptedPwd);
    LOGGER.trace("output:");
    LOGGER.trace(exportIvPlusPwd);
    System.setProperty("JS7_ENCRYPTED_KEY", exportKey);
    System.setProperty("JS7_ENCRYPTED_PW", exportIvPlusPwd);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    String exportedIvPlusPwd = System.getProperty("JS7_ENCRYPTED_PW");
    String[] split = exportedIvPlusPwd.split(" ");
    String exportedKey = split[0]; 
    String exportedIv = split[1];//exportedIvPlusPwd.substring(0, 24);
    LOGGER.trace(exportedIv);
    String exportedPwd = split[2];//exportedIvPlusPwd.substring(24);
    LOGGER.trace(exportedPwd);
    byte[] decodedIv = Base64.getDecoder().decode(exportedIv);
    PrivateKey priv = KeyUtil.getPrivateKeyFromString(Files.readString(Paths.get("src/test/resources/rsa_test.key.pkcs8")));
    SecretKey decryptedSecretKey = new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(exportedKey.getBytes(), priv), algorithm);
    LOGGER.trace("dec:");
    LOGGER.trace(new String(decryptedSecretKey.getEncoded()));
    String decryptedPwd = EncryptionUtils.enOrDecrypt(algorithm, exportedPwd, decryptedSecretKey, new IvParameterSpec(decodedIv), Cipher.DECRYPT_MODE);
    LOGGER.trace(decryptedPwd);
    Assert.assertEquals(pwd, decryptedPwd);
    LOGGER.trace("*************  Test 'encrypt password, export as env variable and decrypt' finished *************");
  }

  @Test
  public void test04PrintEnDecryptUsage() throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    LOGGER.trace("*************  Test 'encrypt value, export as env variable and decrypt' started  ****************");
    Encrypt.printUsage();
    Decrypt.printUsage();
    LOGGER.trace("*************  Test 'encrypt value, export as env variable and decrypt' finished ****************");
  }

  @Test
  @Ignore
  public void test05Decrypt() throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
        InvalidKeySpecException {
    LOGGER.trace("****************************  Test 'decrypt value' started  *************************************");
    String original = "MyP4ssw0rd";
    String encrypted = "ZcEANDl7dWrSvJk1+EhAikjzFtgmIjRH6UtAuNZakJN4awOZ1rJRvPRzRP6N3/pYVYL33hiUYD4nik6KtIhBH7cETdD56TBzenAqRkubf5O8fR0KkPmHEJPQ64M7N8NzS/Sn4n3scYqQemojokr7B169iGLW2na/hUpMG2fsCtXVuM4qeG12zbBwZZHlzAcdafqicAe7WDeJUqIgYYF5ZWkaV9yCqGGTK1IjXHQoDwED5aI78YLL6ZWiAEOV69Tj3DmgDJ7ujxb/b+xmbb4QF9zwvrX7BLtoSdehx4MBoo/00iNqIpLJIS85C3Ou/dBDhKE3v5hCSzLZsly3mvM7oHDWuJaULhTq9wcJHO/t1Am27jOTnerjM2aVFFz/Do4enpZxXQ6REYOVOAe/Vneg7dz9q6EparuHDxmeMDJGxOJFIwNR73tdK2mGmMZ/eJKwu89uQyZqgOxxLhe+ZazD+RYtdQPwsdOKHNuIqN6XdoeyowgndeZywbOVIBVP5w7zmCxfUXlXl+3NdQvVrYsY8RmbM+kMXgQS30lJmcmn7TJ4jh6gSeA3RxUOF/B4wrXkTIOeGNwgJw3hbIhW8Ky/UJVc/JaulUeAyR/BMPLg6PDZ4BDxUrKbtHkAvuKrfMCuKmQZXqZWj7r9JvfRsPzaCEFlmt5qaI/xgqluxcpZSwo= j7u4QfwnSxiVC4Sv4LGSWQ== IEDjzonnjv3ugMQqZ89Wbg==";
    String[] split = encrypted.split(" ");
    String encryptedKey = split[0];
    String iv = split[1];
    String encryptedValue = split[2];
    LOGGER.trace("encrypted key:");
    LOGGER.trace(encryptedKey);
    LOGGER.trace("IV: " + iv);
    LOGGER.trace("encrypted value: " + encryptedValue);
    String decryptedValue = Decrypt.decrypt(KeyUtil.getPrivateKeyFromString(new String(Files.readAllBytes(Paths.get("C:/sp/devel/js7/keys/sp.key")), StandardCharsets.UTF_8)), iv, encryptedKey, encryptedValue);
    LOGGER.trace("decrypted value: " + decryptedValue);
    assertEquals(original, decryptedValue);
    LOGGER.trace("****************************  Test 'decrypt value' finished *************************************");
  }
  
  @Test
  @Ignore
  public void test06EcKey () throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SOSException, InvalidAlgorithmParameterException {
    LOGGER.trace("************************  Test encryption with EC key started  **********************************");
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    byte[] bytesRead = Files.readAllBytes(Paths.get("/sp/tmp/myECprivate.key"));
    String pwd = "m1PwdT03ncry9t";
    String bytesNewString = new String(bytesRead);
    KeyPair kp = KeyUtil.getKeyPairFromECDSAPrivatKeyString(bytesNewString);
    PrivateKey privKey = kp.getPrivate();

    Path certificatePath = Paths.get("/sp/tmp/myX509.crt"); 
    X509Certificate cert = KeyUtil.getX509Certificate(certificatePath);
    SecretKey key = EncryptionUtils.generateSecretKey(128);
    byte[] encryptedKey = EncryptionUtils.encryptSymmetricKey(key, cert);
    String algorithm = "AES/CBC/PKCS5Padding";
    IvParameterSpec ivParameterSpec = EncryptionUtils.generateIv();
    byte[] ivBase64Encoded = Base64.getEncoder().encode(ivParameterSpec.getIV());
    String encryptedPwd = EncryptionUtils.enOrDecrypt(algorithm, pwd, key, ivParameterSpec, Cipher.ENCRYPT_MODE);
    LOGGER.trace("encrypted Pwd:");
    LOGGER.trace(encryptedPwd);
    String exportKey = new String(encryptedKey);
    LOGGER.trace("encrypted Key:");
    LOGGER.trace(exportKey);
    String exportIvPlusPwd = new String(ivBase64Encoded).concat(encryptedPwd);
    LOGGER.trace(exportIvPlusPwd);
    LOGGER.trace("-------------------------------------------------------------------------------------------------");
    String exportedIv = exportIvPlusPwd.substring(0, 24);
    LOGGER.trace(exportedIv);
    String exportedPwd = exportIvPlusPwd.substring(24);
    LOGGER.trace(exportedPwd);
    byte[] decodedIv = Base64.getDecoder().decode(exportedIv);
    byte[] decryptedSecretKey = EncryptionUtils.decryptSymmetricKey(encryptedKey, privKey);
    SecretKey decrypted = new SecretKeySpec(decryptedSecretKey, "AES");
 
    String decryptedPwd = EncryptionUtils.enOrDecrypt(algorithm, exportedPwd, decrypted, new IvParameterSpec(decodedIv), Cipher.DECRYPT_MODE);

    LOGGER.trace("************************  Test encryption with EC key finished  *********************************");
  }

  @Ignore
  @Test
  public void runEncryptMainClass() {
    String[] encryptArgs = new String[] { "--cert=C:/sp/testing/JITL-740/myX509.crt", "--infile=C:/sp/testing/JITL-740/unencrypted.txt",
        "--outfile=C:/sp/testing/JITL-740/encrypted.txt"};
    Encrypt.main(encryptArgs);
  }

  @Ignore
  @Test
  public void runDecryptMainClass() {
    String[] decryptArgs = new String[] { "--key=C:/sp/testing/JITL-740/myECprivate.key",
        "--in=BHAh71N+7gSFVqZmvA2ZbjBQzfoqh8rk1b2M4GSJeIdgBCLjorom8gBsS2JX+V+kGfIJcXkjBAgA0+O/axzraxtiera6/EuGbWVc1K4Vj2hH+nEV7QaGJLhHlQRKkEvt+poDwE0= XpR54jlJfN1W2Es7nUSzuA== C:\\sp\\testing\\JITL-740\\encrypted.txt",
        "--infile=C:/sp/testing/JITL-740/encrypted.txt", "--outfile=C:/sp/testing/JITL-740/decrypted.txt"};
    Decrypt.main(decryptArgs);
  }

}
