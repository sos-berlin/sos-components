package com.sos.pgp.util.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.pgp.util.key.KeyUtil;

public class VerifySignature {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VerifySignature.class);
	
	public static Boolean verify(String publicKey, String original, String signature) throws IOException, PGPException {
	  	InputStream publicKeyStream = IOUtils.toInputStream(publicKey); 
        InputStream originalStream = IOUtils.toInputStream(original);
	  	InputStream signatureStream = IOUtils.toInputStream(signature);
		return verify(publicKeyStream, originalStream, signatureStream);
	}

    public static Boolean verify(Path publicKey, Path original, Path signature) throws IOException, PGPException {
        InputStream publicKeyStream = Files.newInputStream(publicKey);
        InputStream originalStream = Files.newInputStream(original);
        InputStream signatureStream = Files.newInputStream(signature);
        return verify(publicKeyStream, originalStream, signatureStream);
    }

    public static Boolean verify(Path publicKey, Path original, String signature) throws IOException, PGPException {
        InputStream publicKeyStream = Files.newInputStream(publicKey);
        InputStream originalStream = Files.newInputStream(original);
        InputStream signatureStream = IOUtils.toInputStream(signature);
        return verify(publicKeyStream, originalStream, signatureStream);
    }

    public static Boolean verify(Path publicKey, Path original, InputStream signature) throws IOException, PGPException {
        InputStream publicKeyStream = Files.newInputStream(publicKey);
        InputStream originalStream = Files.newInputStream(original);
        return verify(publicKeyStream, originalStream, signature);
    }

	public static Boolean verify(InputStream publicKey, InputStream original, InputStream signature) throws IOException, PGPException {
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

}
