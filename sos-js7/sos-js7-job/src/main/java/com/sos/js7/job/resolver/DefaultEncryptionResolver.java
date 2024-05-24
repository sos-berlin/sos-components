package com.sos.js7.job.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public class DefaultEncryptionResolver implements IJobArgumentValueResolver {

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";

    public static String getPrefix() {
        return EncryptionUtils.ENCRYPTION_IDENTIFIER;
    }

    public static void resolve(List<JobArgument<?>> toResolve, OrderProcessStepLogger logger, Map<String, Object> allArguments) throws Exception {

        String ec = getArgumentValue(allArguments, ARG_NAME_ENCIPHERMENT_CERTIFICATE);
        String epkp = getArgumentValue(allArguments, ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH);

        for (JobArgument<?> arg : toResolve) {
            try {
                arg.applyValue(encrypt((String) arg.getValue(), ec, epkp));
                arg.setDisplayMode(DisplayMode.MASKED);
            } catch (Throwable e) {
                e.printStackTrace();
                arg.setNotAcceptedValue(arg.getValue(), e);
            }
        }
    }

    /*** TODO */
    private static String encrypt(String valWithPrefix, String enciphermentCertificate, String enciphermentPrivateKeyPath) {
        String val = valWithPrefix.substring(getPrefix().length());
        return val;
    }

    private static String getArgumentValue(Map<String, Object> allArguments, String name) {
        Object v = allArguments.get(name);
        if (v == null) {
            return null;
        }
        return (String) v;
    }

}
