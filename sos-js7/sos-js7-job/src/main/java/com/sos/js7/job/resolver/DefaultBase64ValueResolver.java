package com.sos.js7.job.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSBase64;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public class DefaultBase64ValueResolver extends AJobArgumentValueResolver {

    private static final String BASE64_VALUE_PREFIX = "base64:";
    private static final String CLASS_NAME = DefaultBase64ValueResolver.class.getSimpleName();

    public static String getPrefix() {
        return BASE64_VALUE_PREFIX;
    }

    public static void resolve(List<JobArgument<?>> toResolve, OrderProcessStepLogger logger, Map<String, JobArgument<?>> allArguments)
            throws Exception {
        for (JobArgument<?> arg : toResolve) {
            debugArgument(logger, arg, CLASS_NAME);
            // Throw exception if any argument cannot be resolved
            // try {
            arg.applyValue(SOSBase64.decode(getValueWithoutPrefix(arg, getPrefix())));
            // arg.setDisplayMode(DisplayMode.UNKNOWN);
            // } catch (Throwable e) {
            // arg.setNotAcceptedValue(arg.getValue(), e);
            // }
        }
    }

}
