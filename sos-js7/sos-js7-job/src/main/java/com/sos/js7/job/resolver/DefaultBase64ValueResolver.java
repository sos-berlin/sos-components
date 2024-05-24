package com.sos.js7.job.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSBase64;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public class DefaultBase64ValueResolver implements IJobArgumentValueResolver {

    private static final String BASE64_VALUE_PREFIX = "base64:";

    public static String getPrefix() {
        return BASE64_VALUE_PREFIX;
    }

    public static void resolve(List<JobArgument<?>> toResolve, OrderProcessStepLogger logger, Map<String, Object> allArguments) throws Exception {
        for (JobArgument<?> arg : toResolve) {
            try {
                arg.applyValue(SOSBase64.decode(arg.getValue().toString().substring(BASE64_VALUE_PREFIX.length())));
            } catch (Throwable e) {
                arg.setNotAcceptedValue(arg.getValue(), e);
            }
        }
    }

}
