package com.sos.js7.job.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSBase64;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public class StandardBase64Resolver extends JobArgumentValueResolver {

    private static final String CLASS_NAME = StandardBase64Resolver.class.getSimpleName();

    public static String getPrefix() {
        return "base64:";
    }

    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {
        for (JobArgument<?> arg : argumentsToResolve) {
            debugArgument(logger, arg, CLASS_NAME);
            // Throw exception if any argument cannot be resolved
            arg.applyValue(SOSBase64.decode(getValueWithoutPrefix(arg, getPrefix())));
        }
    }

}
