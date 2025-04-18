package com.sos.commons.sign.keys.sign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openssl.PEMKeyPair;

import com.sos.commons.sign.keys.interfaces.StreamHandler;
import com.sos.commons.sign.keys.key.KeyUtil;

public class SignObject {

	private static final int BUFFER_SIZE = 4096;

	public static String signPGP(String privateKey, String original, String passPhrase) throws IOException, PGPException {
	  	InputStream privateKeyStream = IOUtils.toInputStream(privateKey, StandardCharsets.UTF_8); 
	  	InputStream originalStream = IOUtils.toInputStream(original, StandardCharsets.UTF_8);
		return signPGP(privateKeyStream, originalStream, passPhrase);
	}

	public static String signPGP(Path privateKey, Path original, String passPhrase) throws IOException, PGPException {
		InputStream privateKeyPath = Files.newInputStream(privateKey);
	  	InputStream originalPath = Files.newInputStream(original);
		return signPGP(privateKeyPath, originalPath, passPhrase);
	}

	public static String signPGP(InputStream privateKey, InputStream original, String passPhrase) throws IOException, PGPException {
	    Security.addProvider(new BouncyCastleProvider());
		PGPSecretKey secretKey = KeyUtil.readSecretKey(privateKey);
		PGPPublicKey publicKey = secretKey.getPublicKey();
		PGPPrivateKey pgpPrivateKey = null;
		if (passPhrase != null) {
			pgpPrivateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder()
					.setProvider(BouncyCastleProvider.PROVIDER_NAME).build(passPhrase.toCharArray()));
		} else {
			pgpPrivateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder()
					.setProvider(BouncyCastleProvider.PROVIDER_NAME).build("".toCharArray()));
		}
        final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(
                pgpPrivateKey.getPublicKeyPacket().getAlgorithm(), PGPUtil.SHA256), publicKey);
		signatureGenerator.init(PGPSignature.CANONICAL_TEXT_DOCUMENT, pgpPrivateKey);

		ByteArrayOutputStream signatureOutput = new ByteArrayOutputStream();
		try (BCPGOutputStream outputStream = new BCPGOutputStream(new ArmoredOutputStream(signatureOutput))) {
			processStream(original, new StreamHandler() {
				@Override
				public void handleStreamBuffer(byte[] buffer, int offset, int length) throws IOException {
					signatureGenerator.update(buffer, offset, length);
				}
			});
			signatureGenerator.generate().encode(outputStream);
		}
		return new String(signatureOutput.toByteArray(), "UTF-8");
	}

	private static void processStream(InputStream is, StreamHandler handler) throws IOException {
		int read;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((read = is.read(buffer)) != -1) {
			handler.handleStreamBuffer(buffer, 0, read);
		}
	}

	@SuppressWarnings("unused")
    private static void processStringAsStream(String data, StreamHandler handler) throws IOException {
		InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8.name()));
		processStream(is, handler);
	}

	@SuppressWarnings("unused")
    private static void processByteArrayAsStream(byte[] data, StreamHandler handler) throws IOException {
		InputStream is = new ByteArrayInputStream(data);
		processStream(is, handler);
	}

    public static String signX509(PrivateKey privateKey, String original) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
            SignatureException, IOException {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(privateKey);
        signature.update(original.getBytes("UTF-8"));
        return java.util.Base64.getMimeEncoder().encodeToString(signature.sign());
    }

    public static String signX509(String algorythm, PrivateKey privateKey, String original) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
            SignatureException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        Signature signature = Signature.getInstance(algorythm);
        signature.initSign(privateKey);
        signature.update(original.getBytes("UTF-8"));
        return java.util.Base64.getMimeEncoder().encodeToString(signature.sign());
    }

	public static final String signX509(String privateKey, String original) 
	        throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, DataLengthException, CryptoException {
	    PEMKeyPair keyPair = KeyUtil.getPemKeyPairFromRSAPrivatKeyString(privateKey);
	    PrivateKeyInfo pki = keyPair.getPrivateKeyInfo();
	    AsymmetricKeyParameter akpPrivateKey = PrivateKeyFactory.createKey(pki);
	    RSADigestSigner signer = new RSADigestSigner(new SHA256Digest());
	    signer.init(true, akpPrivateKey);
	    signer.update(original.getBytes(), 0, original.getBytes().length);
	    byte[] signature = signer.generateSignature();
        return java.util.Base64.getMimeEncoder().encodeToString(signature);
	}
	
}
