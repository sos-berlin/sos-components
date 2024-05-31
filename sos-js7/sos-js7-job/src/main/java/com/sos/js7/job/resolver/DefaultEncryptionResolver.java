package com.sos.js7.job.resolver;

import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public class DefaultEncryptionResolver implements IJobArgumentValueResolver {

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";
    
    private static PrivateKey privKey;
    private static X509Certificate cert;

    public static String getPrefix() {
        return EncryptionUtils.ENCRYPTION_IDENTIFIER;
    }

    public static void resolve(List<JobArgument<?>> toResolve, OrderProcessStepLogger logger, Map<String, Object> allArguments) throws Exception {

        String enciphermentCertificate = getArgumentValue(allArguments, ARG_NAME_ENCIPHERMENT_CERTIFICATE);
        String privateKeyPath = getArgumentValue(allArguments, ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH);

        // TODO: read PK
        privKey = KeyUtil.getPrivateKey(Paths.get(privateKeyPath));
        cert = KeyUtil.getX509Certificate(enciphermentCertificate);
        
        // TODO: validate PK against certificate
        
        for (JobArgument<?> arg : toResolve) {
            try {
                String decrypted = decrypt((String) arg.getValue(), privateKeyPath);
                logger.info(decrypted);
                arg.applyValue(decrypted);
                System.out.println("decrypted: " + decrypted);
                // TODO: activated before finalize
//                arg.setDisplayMode(DisplayMode.MASKED);
                arg.setDisplayMode(DisplayMode.UNMASKED);
            } catch (Throwable e) {
                arg.setNotAcceptedValue(arg.getValue(), e);
            }
        }
    }

    /*** TODO */
    private static String decrypt(String valWithPrefix, String enciphermentPrivateKeyPath) throws Exception {
        String val = valWithPrefix.substring(getPrefix().length());
        String[] splittedValues = val.split(" ");
        String encryptedKey = splittedValues[0];
        String iv = splittedValues[1];
        String encryptedValue = splittedValues[2];
        SecretKey key = new SecretKeySpec(EncryptionUtils.decryptSymmetricKey(encryptedKey.getBytes(), privKey), "AES");
        byte[] decodedIV = Base64.getDecoder().decode(iv);
        String decryptedValue = Decrypt.decrypt(EncryptionUtils.CIPHER_ALGORITHM, encryptedValue, key,
                new IvParameterSpec(decodedIV));
        return decryptedValue;
    }

    private static String getArgumentValue(Map<String, Object> allArguments, String name) {
        Object v = allArguments.get(name);
        if (v == null) {
            return null;
        }
        return (String) v;
    }

}
