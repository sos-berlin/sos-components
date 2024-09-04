package com.sos.jitl.jobs.encrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;

public class EncryptExecuter {

    private OrderProcessStepLogger logger;
    private EncryptJobArguments args;
    OrderProcessStep<EncryptJobArguments> step;

    public EncryptExecuter(OrderProcessStep<EncryptJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.logger = step.getLogger();
        this.step = step;
    }

    private String encrypt(String input) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException,
            InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
            SOSException {
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

        if (input != null) {
            if (cert != null) {
                encryptedValue = com.sos.commons.encryption.executable.Encrypt.encrypt(cert, input);
            } else {
                encryptedValue = com.sos.commons.encryption.executable.Encrypt.encrypt(pubKey, input);
            }
        }
        return encryptedValue;
    }

    public String execute() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SOSException {

        String input = "";
        String encryptedValue = "";

        if (args.getInFile() != null && !args.getInFile().isEmpty()) {
            input = new String(Files.readAllBytes(Paths.get(args.getInFile())), StandardCharsets.UTF_8);
        } else {
            input = args.getIn();
        }

        encryptedValue = this.encrypt(input);

        if (args.getInFile() != null && !args.getInFile().isEmpty()) {
            Files.writeString(Path.of(args.getOutFile()), encryptedValue);
            encryptedValue = "";
        }

        return encryptedValue;
    }

}
