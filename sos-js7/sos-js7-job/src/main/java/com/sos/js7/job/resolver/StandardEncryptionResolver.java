package com.sos.js7.job.resolver;

import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.exception.SOSKeyException;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArgumentValueIterator;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;

public class StandardEncryptionResolver extends JobArgumentValueResolver {

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";

    private static final String IDENTIFIER = StandardEncryptionResolver.class.getSimpleName();

    public static String getPrefix() {
        return EncryptionUtils.ENCRYPTION_IDENTIFIER;
    }

    /** @apiNote Throw exception if any argument cannot be resolved */
    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {

        PrivateKey privKey = getPrivateKey(allArguments);
        if (!validate(allArguments.get(ARG_NAME_ENCIPHERMENT_CERTIFICATE), privKey)) {
            throw new SOSKeyException("Private key and certificate do not match");
        }

        for (JobArgument<?> arg : argumentsToResolve) {
            debugArgument(logger, IDENTIFIER, arg);

            JobArgumentValueIterator iterator = arg.newValueIterator(getPrefix());
            while (iterator.hasNext()) {
                try {
                    iterator.set(Decrypt.decrypt(EncryptedValue.getInstance(arg.getName(), iterator.nextWithoutPrefix()), privKey));
                } catch (Throwable e) {
                    throw new JobArgumentException(iterator, e);
                }
            }
            arg.setDisplayMode(DisplayMode.MASKED);
        }
    }

    private static PrivateKey getPrivateKey(Map<String, JobArgument<?>> allArguments) throws Exception {
        JobArgument<?> arg = allArguments.get(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH);
        try {
            // Intentionally no support for private keys with password
            PrivateKey pk = KeyUtil.getPrivateKey(arg == null || arg.getValue() == null ? null : arg.getValue().toString(), null);
            arg.setDisplayMode(DisplayMode.UNMASKED);
            return pk;
        } catch (Throwable e) {
            String m = arg == null || arg.getValue() == null ? " missing" : ("=" + arg.getValue().toString());
            throw new SOSKeyException("[argument " + ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH + m + "]" + e.toString(), e);
        }
    }

    // TODO: validate PK against certificate
    private static boolean validate(JobArgument<?> argCertificate, PrivateKey privKey) throws Exception {
        if (argCertificate == null || argCertificate.getValue() == null) {
            return true;
        }
        // return KeyUtil.pubKeyFromCertMatchPrivKey(privKey, KeyUtil.getCertificate(argCertificate.getValue().toString()));
        return true;
    }

}
