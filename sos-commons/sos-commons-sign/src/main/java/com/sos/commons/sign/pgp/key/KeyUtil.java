package com.sos.commons.sign.pgp.key;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
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

import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.joc.model.pgp.JocKeyPair;

public abstract class KeyUtil {
    
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
//            exportKeyPair(privateOutput, publicOutput, kp, userId, passphrase.toCharArray(), true, secondsToExpire);
        } else {
            privateKey = exportSecretKey(kp, userId, "".toCharArray(), secondsToExpire);
//            exportKeyPair(privateOutput, publicOutput, kp, userId, "".toCharArray(), true, secondsToExpire);
        }
        createStreamsWithKeyData(privateOutput, publicOutput, privateKey, true);
        JocKeyPair keyPair = new JocKeyPair();
        keyPair.setPrivateKey(privateOutput.toString());
        keyPair.setPublicKey(publicOutput.toString());
        keyPair.setValidUntil(getValidUntil(privateKey));
        keyPair.setKeyID(getKeyIDAsHexString(privateKey).toUpperCase());
        return keyPair;
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
    
}
