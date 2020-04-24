package com.sos.pgp.util.key;

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
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.pgp.util.SOSPGPConstants;

public abstract class KeyUtil {
    
    public static SOSPGPKeyPair createKeyPair(String userId, String passphrase) 
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, IOException, PGPException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg;
        kpg = KeyPairGenerator.getInstance(SOSPGPConstants.DEFAULT_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        kpg.initialize(SOSPGPConstants.DEFAULT_ALGORITHM_BIT_LENGTH);
        KeyPair kp = kpg.generateKeyPair();
        ByteArrayOutputStream privateOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream publicOutput = new ByteArrayOutputStream();
        if (passphrase != null) {
            exportKeyPair(privateOutput, publicOutput, kp, userId, passphrase.toCharArray(), true);
        } else {
            exportKeyPair(privateOutput, publicOutput, kp, userId, "".toCharArray(), true);
        }
        SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
        keyPair.setPrivateKey(privateOutput.toString());
        keyPair.setPublicKey(publicOutput.toString());
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
    
    private static void exportKeyPair(OutputStream privateOut, OutputStream publicOut, KeyPair pair, String identity, char[] passPhrase, boolean armor)
            throws IOException, InvalidKeyException, NoSuchProviderException, SignatureException, PGPException {
        if (armor) {
            privateOut = new ArmoredOutputStream(privateOut);
        }
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
        PGPSecretKey privateKey = new PGPSecretKey(PGPSignature.CANONICAL_TEXT_DOCUMENT, keyPair, identity, sha1Calc, null, null,
                new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), new JcePBESecretKeyEncryptorBuilder(
                        PGPEncryptedData.CAST5, sha1Calc).setProvider(BouncyCastleProvider.PROVIDER_NAME).build(passPhrase));
        privateKey.encode(privateOut);
        privateOut.close();
        if (armor) {
            publicOut = new ArmoredOutputStream(publicOut);
        }
        PGPPublicKey key = privateKey.getPublicKey();
        key.encode(publicOut);
        publicOut.close();
    }

    public static boolean isKeyPairValid(SOSPGPKeyPair keyPair) {
        String key = keyPair.getPrivateKey();
        if (key != null) {
             try {
                String publicFromPrivateKey = KeyUtil.extractPublicKey(IOUtils.toInputStream(key));
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
                    return isKeyValid(getPGPPublicKeyFromInputStream(IOUtils.toInputStream(key)));
                } catch (IOException | PGPException publicPGPfromPublicException) {
                    return false;
                }
            }
        }
        return false;
    }
    
    public static boolean isKeyValid(String key) {
        if (key != null) {
            try {
               String publicFromPrivateKey = KeyUtil.extractPublicKey(key);
               if (publicFromPrivateKey != null) {
                   return true;
               } else {
                   return false;
               }
           } catch (IOException | PGPException publicFromPrivateException) {
               try {
                   return isKeyValid(getPGPPublicKeyFromInputStream(IOUtils.toInputStream(key)));
               } catch (IOException | PGPException publicPGPfromPublicException) {
                   return false;
               }
           }
       } else {
           return false;
       }
    }

    public static boolean isKeyValid(PGPPublicKey key) {
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
