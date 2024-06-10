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
import com.sos.js7.job.OrderProcessStepLogger;

public class DefaultEncryptionResolver extends AJobArgumentValueResolver {

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PASSWORD = "encipherment_private_key_password";
    public static final String ARG_NAME_ENCIPHERMENT_FAIL_ON_ERROR = "encipherment_fail_on_error";// default true

    private static final String CLASS_NAME = DefaultEncryptionResolver.class.getSimpleName();

    public static String getPrefix() {
        return EncryptionUtils.ENCRYPTION_IDENTIFIER;
    }

    public static void resolve(List<JobArgument<?>> toResolve, OrderProcessStepLogger logger, Map<String, JobArgument<?>> allArguments)
            throws Exception {

        PrivateKey privKey = getPrivateKey(allArguments);
        validate(allArguments.get(ARG_NAME_ENCIPHERMENT_CERTIFICATE), privKey);

        for (JobArgument<?> arg : toResolve) {
            debugArgument(logger, arg, CLASS_NAME);
            try {
                arg.applyValue(Decrypt.decrypt(EncryptedValue.getInstance(arg.getName(), arg.getValue().toString()), privKey));
                arg.setDisplayMode(DisplayMode.MASKED);
            } catch (Throwable e) {
                if (getFailOnError(allArguments)) {
                    throw e;
                }
                arg.setNotAcceptedValue(arg.getValue(), e);
            }
        }
    }

    private static PrivateKey getPrivateKey(Map<String, JobArgument<?>> allArguments) throws Exception {
        JobArgument<?> arg = allArguments.get(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH);
        try {
            PrivateKey pk = KeyUtil.getPrivateKey(arg == null || arg.getValue() == null ? null : arg.getValue().toString(), getPrivateKeyPassword(
                    allArguments));
            arg.setDisplayMode(DisplayMode.UNMASKED);
            return pk;
        } catch (Throwable e) {
            String m = arg == null || arg.getValue() == null ? " missing" : ("=" + arg.getValue().toString());
            throw new SOSKeyException("[argument " + ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH + m + "]" + e.toString(), e);
        }
    }

    private static String getPrivateKeyPassword(Map<String, JobArgument<?>> allArguments) {
        JobArgument<?> arg = allArguments.get(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PASSWORD);
        if (arg == null || arg.getValue() == null) {
            return null;
        }
        arg.setDisplayMode(DisplayMode.MASKED);
        return arg.getValue().toString();
    }

    private static boolean getFailOnError(Map<String, JobArgument<?>> allArguments) {
        JobArgument<?> arg = allArguments.get(ARG_NAME_ENCIPHERMENT_FAIL_ON_ERROR);
        if (arg == null || arg.getValue() == null) {
            return true;
        }
        return arg.getValue().toString().toLowerCase().equals("true");
    }

    // TODO: validate PK against certificate
    private static void validate(JobArgument<?> argCertificate, PrivateKey privKey) throws Exception {
        if (argCertificate == null || argCertificate.getValue() == null) {
            return;
        }
        // X509Certificate cert = KeyUtil.getX509Certificate(argCertificate.getValue().toString());
    }

}
