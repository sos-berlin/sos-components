package com.sos.jitl.jobs.encrypt;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sos.commons.encryption.executable.Encrypt;
import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.js7.job.OrderProcessStep;

public class EncryptExecuter {

    private EncryptJobArguments args;
    OrderProcessStep<EncryptJobArguments> step;

    public EncryptExecuter(OrderProcessStep<EncryptJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.step = step;
    }

    public String execute() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SOSException {
        X509Certificate cert = null;
        PublicKey pubKey = null;
        String encryptedValue = "";

        String enciphermentCertificate = args.getEnciphermentCertificate();
        if (enciphermentCertificate.contains("CERTIFICATE")) {
            cert = KeyUtil.getX509Certificate(enciphermentCertificate);
        } else {
            try {
                pubKey = KeyUtil.getRSAPublicKeyFromString(enciphermentCertificate);
            } catch (Exception e) {
                try {
                    pubKey = KeyUtil.getECDSAPublicKeyFromString(enciphermentCertificate);
                } catch (Exception e1) {
                    try {
                        pubKey = KeyUtil.convertToRSAPublicKey(KeyUtil.stripFormatFromPublicKey(enciphermentCertificate).getBytes());
                    } catch (Exception e2) {
                        pubKey = KeyUtil.getECPublicKeyFromString(KeyUtil.stripFormatFromPublicKey(enciphermentCertificate).getBytes());
                    }
                }
            }
        }

        if (args.getInFile() != null && !args.getInFile().isEmpty() && args.getOutFile() != null && !args.getOutFile().isEmpty()) {
            if (cert != null) {
                encryptedValue = Encrypt.encryptFile(cert, Paths.get(args.getInFile()), Paths.get(args
                        .getOutFile()));
            } else {
                encryptedValue = Encrypt.encryptFile(pubKey, Paths.get(args.getInFile()), Paths.get(args
                        .getOutFile()));
            }
        } else {
            String input = args.getIn();
            if (input != null) {
                if (cert != null) {
                    encryptedValue = Encrypt.encrypt(cert, input);
                } else {
                    encryptedValue = Encrypt.encrypt(pubKey, input);
                }
            }
        }
        return encryptedValue;
    }

}
