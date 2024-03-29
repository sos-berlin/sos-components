package com.sos.commons.sign.keys.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;

public class VerifySignature {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VerifySignature.class);
	
	public static Boolean verifyPGP(String publicKey, String original, String signature) throws IOException, PGPException {
	  	InputStream publicKeyStream = IOUtils.toInputStream(publicKey, StandardCharsets.UTF_8); 
        InputStream originalStream = IOUtils.toInputStream(original, StandardCharsets.UTF_8);
	  	InputStream signatureStream = IOUtils.toInputStream(signature, StandardCharsets.UTF_8);
		return verifyPGP(publicKeyStream, originalStream, signatureStream);
	}

    public static Boolean verifyPGP(Path publicKey, Path original, Path signature) throws IOException, PGPException {
        InputStream publicKeyStream = Files.newInputStream(publicKey);
        InputStream originalStream = Files.newInputStream(original);
        InputStream signatureStream = Files.newInputStream(signature);
        return verifyPGP(publicKeyStream, originalStream, signatureStream);
    }

    public static Boolean verifyPGP(Path publicKey, Path original, String signature) throws IOException, PGPException {
        InputStream publicKeyStream = Files.newInputStream(publicKey);
        InputStream originalStream = Files.newInputStream(original);
        InputStream signatureStream = IOUtils.toInputStream(signature, StandardCharsets.UTF_8);
        return verifyPGP(publicKeyStream, originalStream, signatureStream);
    }

    public static Boolean verifyPGP(Path publicKey, Path original, InputStream signature) throws IOException, PGPException {
        InputStream publicKeyStream = Files.newInputStream(publicKey);
        InputStream originalStream = Files.newInputStream(original);
        return verifyPGP(publicKeyStream, originalStream, signature);
    }

	public static Boolean verifyPGP(InputStream publicKey, InputStream original, InputStream signature) throws IOException, PGPException {
	  	try {
			InputStream signatureDecoderStream = PGPUtil.getDecoderStream(signature);
			JcaPGPObjectFactory pgpFactory = new JcaPGPObjectFactory(signatureDecoderStream);
			PGPSignature pgpSignature = ((PGPSignatureList) pgpFactory.nextObject()).get(0);
			PGPPublicKey pgpPublicKey = KeyUtil.getPGPPublicKeyFromInputStream(publicKey);
			pgpSignature.init(new JcaPGPContentVerifierBuilderProvider(), pgpPublicKey);
	        byte[] buff = new byte[1024];
	        int read = 0;
	        while ((read = original.read(buff)) != -1) {
	            pgpSignature.update(buff, 0, read);
	        }
	        original.close();
	        return pgpSignature.verify();
		} catch (IOException | PGPException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public static Boolean verifyX509WithPublicKeyString (String publicKey, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
	    PublicKey pubKey = KeyUtil.getPublicKeyFromString(KeyUtil.decodePublicKeyString(publicKey));
        return verifyX509(pubKey, original, signature);
	}

    public static Boolean verifyX509WithCertifcateString(String certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException, CertificateException {
        Certificate cert = KeyUtil.getCertificate(certificate);
        return verifyX509((X509Certificate)cert, original, signature);
    }
    
    public static Boolean verifyX509WithCertifcateString(String algorythm, String certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException, CertificateException {
        Certificate cert = KeyUtil.getCertificate(certificate);
        return verifyX509(algorythm, (X509Certificate)cert, original, signature);
    }
    
    public static Boolean verifyX509 (PublicKey publicKey, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = null;
        if (publicKey instanceof ECPublicKey) {
            sig = Signature.getInstance(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM);
        } else if (publicKey instanceof RSAPublicKey) {
            sig = Signature.getInstance(SOSKeyConstants.RSA_SIGNER_ALGORITHM);
        }
        sig.initVerify(publicKey);
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    public static Boolean verifyX509 (String algorythm, PublicKey publicKey, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance(algorythm);
        sig.initVerify(publicKey);
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    public static Boolean verifyX509(X509Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Signature sig = null;
        PublicKey publicKey = certificate.getPublicKey();
        if (publicKey instanceof ECPublicKey) {
            sig = Signature.getInstance(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM);
        } else if (publicKey instanceof RSAPublicKey) {
            sig = Signature.getInstance(SOSKeyConstants.RSA_SIGNER_ALGORITHM);
        }
        sig.initVerify(certificate);
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    public static Boolean verifyX509(String algorythm, X509Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Signature sig = Signature.getInstance(algorythm);
        sig.initVerify(certificate);
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    public static Boolean verifyX509Mime(String algorythm, X509Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Signature sig = Signature.getInstance(algorythm);
        sig.initVerify(certificate);
        sig.update(original.getBytes());
        return sig.verify(java.util.Base64.getMimeDecoder().decode(normalizeSignature(signature).getBytes()));
    }

    public static Boolean verifyX509MimeWithCRLF(String algorythm, X509Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Signature sig = Signature.getInstance(algorythm);
        sig.initVerify(certificate);
        sig.update(original.getBytes());
        return sig.verify(java.util.Base64.getMimeDecoder().decode(signature.getBytes()));
    }

    public static Boolean verifyX509(Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Signature sig = Signature.getInstance(SOSKeyConstants.RSA_SIGNER_ALGORITHM);
        sig.initVerify(certificate);
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    public static Boolean verifyX509BC(X509Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        Signature sig = Signature.getInstance(SOSKeyConstants.RSA_SIGNER_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        sig.initVerify(certificate.getPublicKey());
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    public static Boolean verifyX509BC(String algorythm, X509Certificate certificate, String original, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        Signature sig = Signature.getInstance(algorythm, BouncyCastleProvider.PROVIDER_NAME);
        sig.initVerify(certificate.getPublicKey());
        sig.update(original.getBytes());
        return sig.verify(Base64.decode(normalizeSignature(signature).getBytes()));
    }
    
    private static String normalizeSignature(String signature) {
        String normalizedSignature = signature.replace(SOSKeyConstants.SIGNATURE_HEADER, "").replace(SOSKeyConstants.SIGNATURE_FOOTER, "");
        return normalizedSignature.replaceAll("[\\r\\n]", "");
    }

}