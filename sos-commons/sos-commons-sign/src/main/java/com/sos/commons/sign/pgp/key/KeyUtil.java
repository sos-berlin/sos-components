package com.sos.commons.sign.pgp.key;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
//import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.joc.model.pgp.JocKeyPair;


public abstract class KeyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUtil.class);
    
    public static JocKeyPair createKeyPair(String userId, String passphrase, Long secondsToExpire) 
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, IOException, PGPException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg;
        kpg = KeyPairGenerator.getInstance(SOSPGPConstants.DEFAULT_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        kpg.initialize(SOSPGPConstants.DEFAULT_ALGORITHM_BIT_LENGTH);
        KeyPair kp = kpg.generateKeyPair();
        ByteArrayOutputStream privateOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream publicOutput = new ByteArrayOutputStream();
        PGPSecretKey privateKey = null;
        if (passphrase != null) {
            privateKey = exportSecretKey(kp, userId, passphrase.toCharArray(), secondsToExpire);
        } else {
            privateKey = exportSecretKey(kp, userId, "".toCharArray(), secondsToExpire);
        }
        createStreamsWithKeyData(privateOutput, publicOutput, privateKey, true);
        JocKeyPair keyPair = new JocKeyPair();
        keyPair.setPrivateKey(privateOutput.toString());
        keyPair.setPublicKey(publicOutput.toString());
        keyPair.setValidUntil(getValidUntil(privateKey));
        keyPair.setKeyID(getKeyIDAsHexString(privateKey).toUpperCase());
        return keyPair;
    }
    
    public static JocKeyPair createRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(SOSPGPConstants.DEFAULT_ALGORITHM);
        kpg.initialize(SOSPGPConstants.DEFAULT_ALGORITHM_BIT_LENGTH);
        KeyPair kp = kpg.generateKeyPair();
        JocKeyPair keyPair = new JocKeyPair();
        byte[] encodedPrivate = kp.getPrivate().getEncoded();
        String encodedPrivateToString = DatatypeConverter.printBase64Binary(encodedPrivate);
        byte[] encodedPublic = kp.getPublic().getEncoded();
        String encodedPublicToString = DatatypeConverter.printBase64Binary(encodedPublic);
        keyPair.setPrivateKey(formatPrivateKey(encodedPrivateToString));
        keyPair.setPublicKey(formatPublicKey(encodedPublicToString));
//        keyPair.setPrivateKey(encodedPrivateToString);
//        keyPair.setPublicKey(encodedPublicToString);
        keyPair.setKeyID(getRSAKeyIDAsHexString(kp.getPublic()).toUpperCase());
        return keyPair;
    }
    
    public static KeyPair createKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(SOSPGPConstants.DEFAULT_ALGORITHM);
        kpg.initialize(SOSPGPConstants.DEFAULT_ALGORITHM_BIT_LENGTH);
        return kpg.generateKeyPair();
    }
    
    public static JocKeyPair createJOCKeyPair(KeyPair keyPair) throws NoSuchAlgorithmException, NoSuchProviderException {
        JocKeyPair jocKeyPair = new JocKeyPair();
        byte[] encodedPrivate = keyPair.getPrivate().getEncoded();
        String encodedPrivateToString = DatatypeConverter.printBase64Binary(encodedPrivate);
        byte[] encodedPublic = keyPair.getPublic().getEncoded();
        String encodedPublicToString = DatatypeConverter.printBase64Binary(encodedPublic);
        jocKeyPair.setPrivateKey(formatPrivateKey(encodedPrivateToString));
        jocKeyPair.setPublicKey(formatPublicKey(encodedPublicToString));
        jocKeyPair.setKeyID(getRSAKeyIDAsHexString(keyPair.getPublic()).toUpperCase());
        return jocKeyPair;
    }
    
    public static String extractPublicKey(String privateKey) throws IOException, PGPException {
            InputStream privateKeyStream = IOUtils.toInputStream(privateKey); 
            return extractPublicKey(privateKeyStream);
    }
    
    public static String extractPublicKey(Path privateKey) throws IOException, PGPException {
        InputStream privateKeyPath = Files.newInputStream(privateKey);
        return extractPublicKey(privateKeyPath);
    }
    
    public static String extractPublicKey(InputStream privateKey) throws IOException, PGPException {
        OutputStream publicOutput = null;
        publicOutput = new ByteArrayOutputStream();
        PGPPublicKey pgpPublicKey = extractPGPPublicKey(privateKey);
        if (pgpPublicKey != null) {
            OutputStream publicOutputArmored = new ArmoredOutputStream(publicOutput);
            pgpPublicKey.encode(publicOutputArmored);
            publicOutputArmored.close();
            return publicOutput.toString();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static PGPPublicKey extractPGPPublicKey (InputStream privateKey) throws IOException, PGPException {
        PGPSecretKeyRingCollection pgpSec = null;
        PGPPublicKey pgpPublicKey = null;
        pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(privateKey), new JcaKeyFingerprintCalculator());
        Iterator keyRingIter = pgpSec.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing keyRing = (PGPSecretKeyRing)keyRingIter.next();
            Iterator keyIter = keyRing.getSecretKeys();
            while (keyIter.hasNext()) {
                PGPSecretKey key = (PGPSecretKey)keyIter.next();
                if (key.isSigningKey()) {
                    pgpPublicKey = key.getPublicKey();
                    break;
                }
            }
        }
        return pgpPublicKey;
    }
    
    private static void createStreamsWithKeyData (OutputStream privateOut, OutputStream publicOut, PGPSecretKey privateKey, boolean armored) throws IOException {
        if (armored) {
            privateOut = new ArmoredOutputStream(privateOut);
            publicOut = new ArmoredOutputStream(publicOut);
        }
        privateKey.encode(privateOut);
        privateOut.close();
        PGPPublicKey key = privateKey.getPublicKey();
        key.encode(publicOut);
        publicOut.close();
    }
    
    private static PGPSecretKey exportSecretKey(KeyPair pair, String identity, char[] passPhrase, Long secondsToExpire) throws PGPException {
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
        PGPSignatureSubpacketVector subpacketVector = null;
        if (secondsToExpire != null) {
            PGPSignatureSubpacketGenerator subpacketGenerator = new PGPSignatureSubpacketGenerator();
            subpacketGenerator.setKeyExpirationTime(false, secondsToExpire);
            subpacketVector = subpacketGenerator.generate();
        }

        PGPSecretKey privateKey = new PGPSecretKey(PGPSignature.CANONICAL_TEXT_DOCUMENT, keyPair, identity, sha1Calc, subpacketVector, null,
                new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), new JcePBESecretKeyEncryptorBuilder(
                        PGPEncryptedData.CAST5, sha1Calc).setProvider(BouncyCastleProvider.PROVIDER_NAME).build(passPhrase));
        return privateKey;
    }

    public static String getKeyIDAsHexString(PGPSecretKey privateKey) {
        return Long.toHexString(privateKey.getKeyID());
    }

    public static String getKeyIDAsHexString(PGPPublicKey publicKey) {
        return Long.toHexString(publicKey.getKeyID());
    }

    public static String getRSAKeyIDAsHexString(PublicKey key) {
        return toHex(createRSAKeyID(key));
    }

    public static Date getValidUntil(PGPSecretKey privateKey) {
        return getValidUntil(privateKey.getPublicKey());
    }
    
    public static Date getValidUntil(PGPPublicKey publicKey) {
        @SuppressWarnings("rawtypes")
        Iterator iter = publicKey.getSignaturesOfType(PGPSignature.CANONICAL_TEXT_DOCUMENT);
        Long keyExpirationTime = null;
        while(iter.hasNext())         {
            keyExpirationTime = ((PGPSignature)iter.next()).getHashedSubPackets().getKeyExpirationTime();
        }

        Date creationDate = publicKey.getCreationTime();
        Long validSeconds = publicKey.getValidSeconds();
        Date validUntil = null;
        // first check if valid seconds is set
        // if not check if keyExpirationTime is set
        if (validSeconds != null && validSeconds != 0) {
            validUntil = new Date(creationDate.getTime() + (validSeconds * 1000));
        } else if (keyExpirationTime != null && keyExpirationTime != 0) {
            validUntil = Date.from(Instant.ofEpochSecond(keyExpirationTime));
        }
        return validUntil;
    }
    
    // checks if the provided KeyPair contains an ASCII representation of a PGP key
    public static boolean isKeyPairValid(JocKeyPair keyPair) {
        String key = keyPair.getPrivateKey();
        if (key != null) {
             try {
                String publicFromPrivateKey = extractPublicKey(IOUtils.toInputStream(key));
                if (publicFromPrivateKey != null) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException | PGPException e) {
                return false;
            }
        } else {
            key = keyPair.getPublicKey();
            if (key != null) {
                try {
                    return isKeyNotNull(getPGPPublicKeyFromInputStream(IOUtils.toInputStream(key)));
                } catch (IOException | PGPException publicPGPfromPublicException) {
                    return false;
                }
            }
        }
        return false;
    }
    
    // checks if the provided String really is an ASCII representation of a PGP key
    public static boolean isKeyValid(String key) {
        if (key != null) {
            try {
               String publicFromPrivateKey = extractPublicKey(key);
               if (publicFromPrivateKey != null) {
                   return true;
               } else {
                   return false;
               }
           } catch (IOException | PGPException publicFromPrivateException) {
               try {
                   return isKeyNotNull(getPGPPublicKeyFromInputStream(IOUtils.toInputStream(key)));
               } catch (IOException | PGPException publicPGPfromPublicException) {
                   return false;
               }
           }
       } else {
           return false;
       }
    }

    public static boolean isKeyNotNull(PGPPublicKey key) {
        if (key != null) {
            return true;
        } else {
            return false;
        }
    }

    public static PGPPublicKey getPGPPublicKeyFromString (String publicKey) throws IOException, PGPException {
        InputStream publicKeyDecoderStream = PGPUtil.getDecoderStream(IOUtils.toInputStream(publicKey));
        JcaPGPPublicKeyRingCollection pgpPubKeyRing = new JcaPGPPublicKeyRingCollection(publicKeyDecoderStream);
        Iterator<PGPPublicKeyRing> publicKeyRingIterator = pgpPubKeyRing.getKeyRings();
        
        PGPPublicKey pgpPublicKey = null;
        while (pgpPublicKey == null && publicKeyRingIterator.hasNext()) {
            PGPPublicKeyRing pgpPublicKeyRing = publicKeyRingIterator.next();
            Iterator<PGPPublicKey> pgpPublicKeyIterator = pgpPublicKeyRing.getPublicKeys();
            while (pgpPublicKey == null && pgpPublicKeyIterator.hasNext()) {
                PGPPublicKey key = pgpPublicKeyIterator.next();
                if (key.isEncryptionKey()) {
                    pgpPublicKey = key;                    
                }
            }
        }
        return pgpPublicKey;
    }

    public static PGPPublicKey getPGPPublicKeyFromInputStream (InputStream publicKey) throws IOException, PGPException {
        InputStream publicKeyDecoderStream = PGPUtil.getDecoderStream(publicKey);
        JcaPGPPublicKeyRingCollection pgpPubKeyRing = new JcaPGPPublicKeyRingCollection(publicKeyDecoderStream);
        Iterator<PGPPublicKeyRing> publicKeyRingIterator = pgpPubKeyRing.getKeyRings();
        
        PGPPublicKey pgpPublicKey = null;
        while (pgpPublicKey == null && publicKeyRingIterator.hasNext()) {
            PGPPublicKeyRing pgpPublicKeyRing = publicKeyRingIterator.next();
            Iterator<PGPPublicKey> pgpPublicKeyIterator = pgpPublicKeyRing.getPublicKeys();
            while (pgpPublicKey == null && pgpPublicKeyIterator.hasNext()) {
                PGPPublicKey key = pgpPublicKeyIterator.next();
                if (key.isEncryptionKey()) {
                    pgpPublicKey = key;                    
                }
            }
        }
        return pgpPublicKey;
    }
    
    public static PrivateKey getPemPrivateKeyFromRSAString (String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PEMParser pemParser = new PEMParser(new StringReader(privateKey));
        final PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
        final byte[] privateEncoded = pemKeyPair.getPrivateKeyInfo().getEncoded();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateEncoded));
        pemParser.close();
        return privKey;
    }
    
    public static KeyPair getKeyPairFromRSAPrivatKeyString (String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PEMParser pemParser = new PEMParser(new StringReader(privateKey));
        PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
        final byte[] privateEncoded = pemKeyPair.getPrivateKeyInfo().getEncoded();
        final byte[] publicEncoded = pemKeyPair.getPublicKeyInfo().getEncoded();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateEncoded));
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicEncoded));
        pemParser.close();
        return new KeyPair(publicKey, privKey);
    }
    
    public static KeyPair getKeyPairFromPrivatKeyString (String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PrivateKey privKey = getPrivateKeyFromString(privateKey);
        RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey)privKey;
        RSAPublicKeySpec rsaPubKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(rsaPubKeySpec);
        return new KeyPair(publicKey, privKey);
    }

    public static boolean pubKeyFromPairAndCertMatch (PublicKey fromPair, PublicKey fromCert) {
        return fromPair.equals(fromCert);
    }

    public static boolean pubKeyMatchesPrivKey (PrivateKey privKey, PublicKey pubKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) pubKey;
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privKey;
        return rsaPublicKey.getModulus().equals( rsaPrivateKey.getModulus() )
            && BigInteger.valueOf( 2 ).modPow( rsaPublicKey.getPublicExponent()
            .multiply( rsaPrivateKey.getPrivateExponent() ).subtract( BigInteger.ONE ),
            rsaPublicKey.getModulus() ).equals( BigInteger.ONE );
    }
    
    public static boolean pubKeyFromCertMatchPrivKey (PrivateKey privKey, Certificate certificate) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) certificate.getPublicKey();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privKey;
        return rsaPublicKey.getModulus().equals( rsaPrivateKey.getModulus() )
            && BigInteger.valueOf( 2 ).modPow( rsaPublicKey.getPublicExponent()
            .multiply( rsaPrivateKey.getPrivateExponent() ).subtract( BigInteger.ONE ),
            rsaPublicKey.getModulus() ).equals( BigInteger.ONE );
    }
    
    public static boolean pubKeyFromPairAndCertMatch (PublicKey pubKey, X509Certificate cert) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey)pubKey;
        BigInteger modFromKey = rsaPublicKey.getModulus();
        BigInteger exponentFromKey = rsaPublicKey.getPublicExponent();
        RSAPublicKey rsaPublicKeyFromCert = (RSAPublicKey) cert.getPublicKey();
        BigInteger modFromCertKey = rsaPublicKeyFromCert.getModulus();
        BigInteger exponentFromCertKey = rsaPublicKeyFromCert.getPublicExponent();
        return modFromKey.equals(modFromCertKey) && exponentFromKey.equals(exponentFromCertKey);
    }
    
    public static boolean pubKeyFromPairAndCertMatch (PublicKey pubKey, Certificate cert) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey)pubKey;
        BigInteger modFromKey = rsaPublicKey.getModulus();
        BigInteger exponentFromKey = rsaPublicKey.getPublicExponent();
        RSAPublicKey rsaPublicKeyFromCert = (RSAPublicKey) cert.getPublicKey();
        BigInteger modFromCertKey = rsaPublicKeyFromCert.getModulus();
        BigInteger exponentFromCertKey = rsaPublicKeyFromCert.getPublicExponent();
        return modFromKey.equals(modFromCertKey) && exponentFromKey.equals(exponentFromCertKey);
    }
    
    public static boolean compareKeyAndCertificate (String privateKey, String certificate)
            throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        PemReader certReader = new PemReader(new StringReader(certificate));
        PemObject certAsPemObject = certReader.readPemObject();
        certReader.close();
        if (!certAsPemObject.getType().equalsIgnoreCase("CERTIFICATE")) {
            throw new IllegalArgumentException("Certificate file does not contain a certificate but a " + certAsPemObject.getType());
        }
        byte[] x509Data = certAsPemObject.getContent();
        CertificateFactory fact = CertificateFactory.getInstance("X509");
        Certificate cert = fact.generateCertificate(new ByteArrayInputStream(x509Data));
        if (!(cert instanceof X509Certificate)) {
            throw new IllegalArgumentException("Certificate file does not contain an X509 certificate");
        }
        PublicKey publicKey = cert.getPublicKey();
        if (!(publicKey instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("Certificate file does not contain an RSA public key but a " + publicKey.getClass().getName());
        }
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        byte[] certModulusData = rsaPublicKey.getModulus().toByteArray();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] certID = sha256.digest(certModulusData);
        String certIDinHex = new String (DatatypeConverter.printHexBinary(certID));
        PemReader privKeyReader = new PemReader(new StringReader(privateKey));
        PemObject privKeyAsPemObject = privKeyReader.readPemObject();
        privKeyReader.close();
        if (!privKeyAsPemObject.getType().equalsIgnoreCase("RSA PRIVATE KEY")) {
            throw new IllegalArgumentException("Key file does not contain a private key but a " + privKeyAsPemObject.getType());
        }
        PrivateKey privKey = getPemPrivateKeyFromRSAString(privateKey);
        byte[] privateKeyData = privKeyAsPemObject.getContent();
        KeyFactory keyFact = KeyFactory.getInstance("RSA");
        KeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyData);
        if (!(privKey instanceof RSAPrivateKey)) {
            throw new IllegalArgumentException("Key file does not contain an X509 encoded private key");
        }
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privKey;
        byte[] keyModulusData = rsaPrivateKey.getModulus().toByteArray();
        byte[] keyID = sha256.digest(keyModulusData);
        String keyIDinHex = new String (DatatypeConverter.printHexBinary(keyID));
        if (certIDinHex.equals(keyIDinHex)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static PEMKeyPair getPemKeyPairFromRSAPrivatKeyString (String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PEMParser pemParser = new PEMParser(new StringReader(privateKey));
        PEMKeyPair keyPair = (PEMKeyPair) pemParser.readObject();
        pemParser.close();
        return keyPair;
    }
    
    public static AsymmetricKeyParameter loadPublicKey(InputStream is) {
        SubjectPublicKeyInfo spki = (SubjectPublicKeyInfo) readPemObject(is);
        try {
            return PublicKeyFactory.createKey(spki);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create public key object based on input data", ex);
        }
    }

    public static SubjectPublicKeyInfo getSubjectPublicKeyInfo(String publicKey) {
        SubjectPublicKeyInfo spki = (SubjectPublicKeyInfo) readPemObject(publicKey);
        return spki;
    }

    public static AsymmetricKeyParameter loadPublicKey(String publicKey) {
        SubjectPublicKeyInfo spki = (SubjectPublicKeyInfo) readPemObject(publicKey);
        try {
            return PublicKeyFactory.createKey(spki);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create public key object based on input data", ex);
        }
    }

    public static AsymmetricKeyParameter loadPublicKeyFromCertificate(String certificate) {
        X509CertificateHolder x509CertHolder = (X509CertificateHolder) readPemObject(certificate);
        SubjectPublicKeyInfo spki = x509CertHolder.getSubjectPublicKeyInfo();
        try {
            return PublicKeyFactory.createKey(spki);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create public key object based on input data", ex);
        }
    }

    public static SubjectPublicKeyInfo getSubjectPublicKeyInfoFromCertificate(String certificate) {
        X509CertificateHolder x509CertHolder = (X509CertificateHolder) readPemObject(certificate);
        SubjectPublicKeyInfo spki = x509CertHolder.getSubjectPublicKeyInfo();
        return spki;
    }

    public static AsymmetricKeyParameter loadPrivateKey(InputStream is) {
        PEMKeyPair keyPair = (PEMKeyPair) readPemObject(is);
        PrivateKeyInfo pki = keyPair.getPrivateKeyInfo();
        try {
            return PrivateKeyFactory.createKey(pki);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create private key object based on input data", ex);
        }
    }

    public static AsymmetricKeyParameter loadPrivateKey(String privateKey) {
        PEMKeyPair keyPair = (PEMKeyPair) readPemObject(privateKey);
        PrivateKeyInfo pki = keyPair.getPrivateKeyInfo();
        try {
            return PrivateKeyFactory.createKey(pki);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create private key object based on input data", ex);
        }
    }

    private static Object readPemObject(InputStream is) {
        try {
            Validate.notNull(is, "Input data stream cannot be null");
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            PEMParser pemParser = new PEMParser(isr);
            Object obj = pemParser.readObject();
            if (obj == null) {
                throw new Exception("No PEM object found");
            }
            return obj;
        } catch (Throwable ex) {
            throw new RuntimeException("Cannot read PEM object from input data", ex);
        }
    }
    
    public static Object readPemObject(String value) {
        PEMParser pemParser = null;
        try {
            Validate.notNull(value, "Input data stream cannot be null");
            pemParser = new PEMParser(new StringReader(value));
            Object obj = pemParser.readObject();
            if (obj == null) {
                throw new Exception("No PEM object found");
            }
            return obj;
        } catch (Throwable ex) {
            throw new RuntimeException("Cannot read PEM object from input data", ex);
        } finally {
            try {
                pemParser.close();
            } catch (IOException e) {}
        }
    }
    
    public static PrivateKey getPrivateKeyFromString (String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo)KeyUtil.readPemObject(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded()));
    }
    
    public static PublicKey getPublicKeyFromString (String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] decoded = null;
        if (publicKey.startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER)) {
            decoded = Base64.decode(stripFormatFromPublicRSAKey(publicKey));
        } else if (publicKey.startsWith(SOSPGPConstants.PUBLIC_KEY_HEADER)) {
            decoded = Base64.decode(stripFormatFromPublicKey(publicKey));
        } else {
            decoded = Base64.decode(publicKey);
        }
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decoded);
        return kf.generatePublic(pubKeySpec);
    }
    
    public static PublicKey extractPublicKey (PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static X509Certificate getX509Certificate(String certificate) throws CertificateException, UnsupportedEncodingException {
       InputStream certificateStream = IOUtils.toInputStream(certificate); 
        return getX509Certificate(certificateStream);
    }
    
    public static X509Certificate getX509Certificate(Path certificate) throws IOException, CertificateException {
        InputStream certificatePathStream = Files.newInputStream(certificate);
        return getX509Certificate(certificatePathStream);
    }
    
    public static X509Certificate getX509Certificate(InputStream certificate) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate(certificate);
    }

    public static Certificate getCertificate(String certificate) throws CertificateException {
        InputStream certificateStream = IOUtils.toInputStream(certificate); 
        return getCertificate(certificateStream);
    }
    
    public static Certificate getCertificate(Path certificate) throws IOException, CertificateException {
        InputStream certificatePathStream = Files.newInputStream(certificate);
        return getX509Certificate(certificatePathStream);
    }
    
    public static Certificate getCertificate(InputStream certificate) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(certificate);
    }
    
    public static X509Certificate generateCertificateFromKeyPair(KeyPair keyPair) {
        try {
            BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
            Date startDate = Date.from(Instant.now());
            Date expiryDate = Date.from(Instant.now().plusSeconds(365 * 24 * 60 * 60));
            X500Name issuer = new X500Name("O=SOS,OU=INT");
            X500Name subject = new X500Name("CN=SP,OU=IT");

            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                    issuer, serialNumber, startDate, expiryDate, subject,
                    SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256withRSA");
            ContentSigner signer = builder.build(keyPair.getPrivate());


            byte[] certBytes = certBuilder.build(signer).getEncoded();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
//            throw new DeltaClientException("Error generating certificate", e);
            return null;
        }
    }
    
    private static byte[] integerToOctetByteArray(BigInteger i, int octets) {
        if (i.bitLength() > octets * Byte.SIZE) {
            throw new IllegalArgumentException("i does not fit in " + octets + " octets");
        }
        final byte[] is = i.toByteArray();
        if (is.length == octets) {
            return is;
        }
        final byte[] ius = new byte[octets];
        if (is.length == octets + 1) {
            System.arraycopy(is, 1, ius, 0, octets);
        } else {
            System.arraycopy(is, 0, ius, octets - is.length, is.length);
        }
        return ius;
    }

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X", data[i]));
        }
        return sb.toString();
    }

    private static byte[] createRSAKeyID(PublicKey key) {
        BigInteger modulus = ((RSAPublicKey)key).getModulus();
        if (modulus.bitLength() % Byte.SIZE != 0) {
            throw new IllegalArgumentException("This method currently only works with RSA key sizes that are a multiple of 8 in bits");
        }
        final byte[] modulusData = integerToOctetByteArray(modulus, modulus.bitLength() / Byte.SIZE);
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 message digest should be available in any Java SE runtime", e);
        }
        return sha1.digest(modulusData);
    }
    
    private static String formatPrivateRSAKey (byte[] key) {
        String base64Key = DatatypeConverter.printBase64Binary(key);
        return String.join("\n", 
                SOSPGPConstants.PRIVATE_RSA_KEY_HEADER, 
                insertLineFeedsInEncodedString(base64Key), 
                SOSPGPConstants.PRIVATE_RSA_KEY_FOOTER);
    }

    private static String formatPrivateRSAKey (String key) {
        return String.join("\n", 
                SOSPGPConstants.PRIVATE_RSA_KEY_HEADER, 
                insertLineFeedsInEncodedString(key), 
                SOSPGPConstants.PRIVATE_RSA_KEY_FOOTER);
    }

    private static String formatPrivateKey (byte[] key) {
        String base64Key = DatatypeConverter.printBase64Binary(key);
        return String.join("\n", 
                SOSPGPConstants.PRIVATE_KEY_HEADER, 
                insertLineFeedsInEncodedString(base64Key), 
                SOSPGPConstants.PRIVATE_KEY_FOOTER);
    }

    private static String formatPrivateKey (String key) {
        return String.join("\n", 
                SOSPGPConstants.PRIVATE_KEY_HEADER, 
                insertLineFeedsInEncodedString(key), 
                SOSPGPConstants.PRIVATE_KEY_FOOTER);
    }

    private static String formatPublicRSAKey (byte[] key) {
        String base64Key = DatatypeConverter.printBase64Binary(key);
        return String.join("\n", 
                SOSPGPConstants.PUBLIC_RSA_KEY_HEADER, 
                insertLineFeedsInEncodedString(base64Key), 
                SOSPGPConstants.PUBLIC_RSA_KEY_FOOTER);
    }
    
    private static String formatPublicRSAKey (String key) {
        return String.join("\n", 
                SOSPGPConstants.PUBLIC_RSA_KEY_HEADER, 
                insertLineFeedsInEncodedString(key), 
                SOSPGPConstants.PUBLIC_RSA_KEY_FOOTER);
    }
    
    private static String formatPublicKey (byte[] key) {
        String base64Key = DatatypeConverter.printBase64Binary(key);
        return String.join("\n", 
                SOSPGPConstants.PUBLIC_KEY_HEADER, 
                insertLineFeedsInEncodedString(base64Key), 
                SOSPGPConstants.PUBLIC_KEY_FOOTER);
    }
    
    private static String formatPublicKey (String key) {
        return String.join("\n", 
                SOSPGPConstants.PUBLIC_KEY_HEADER, 
                insertLineFeedsInEncodedString(key), 
                SOSPGPConstants.PUBLIC_KEY_FOOTER);
    }
    
    public static String stripFormatFromPrivateRSAKey (String key) {
        return key.replace(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER, "")
                .replace(SOSPGPConstants.PRIVATE_RSA_KEY_FOOTER, "")
                .replaceAll("\n", "");
    }
    
    public static String stripFormatFromPrivateKey (String key) {
        return key.replace(SOSPGPConstants.PRIVATE_KEY_HEADER, "")
                .replace(SOSPGPConstants.PRIVATE_KEY_FOOTER, "")
                .replaceAll("\n", "");
    }
    
    public static String stripFormatFromPublicRSAKey (String key) {
        return key.replace(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER, "")
                .replace(SOSPGPConstants.PUBLIC_RSA_KEY_FOOTER, "")
                .replaceAll("\n", "");
    }
    
    public static String stripFormatFromPublicKey (String key) {
        return key.replace(SOSPGPConstants.PUBLIC_KEY_HEADER, "")
                .replace(SOSPGPConstants.PUBLIC_KEY_FOOTER, "")
                .replaceAll("\n", "");
    }
    
    public static String formatEncodedDataString (String data, String header, String footer) {
        return String.join("\n", 
                header, 
                insertLineFeedsInEncodedString(data), 
                footer);
    }
    
    private static String insertLineFeedsInEncodedString (String key) {
        return key.replaceAll("(.{64})", "$1\n").trim();
    }

}
