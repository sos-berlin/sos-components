package com.sos.js7.job.examples.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.resolver.JobArgumentValueResolver;

/** Resolves values with the prefix <i>upper:</i> in uppercase */
public class ExampleUpperCaseResolver extends JobArgumentValueResolver {

    private static final String CLASS_NAME = ExampleUpperCaseResolver.class.getSimpleName();

    public static String getPrefix() {
        return "upper:";
    }

    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {
        for (JobArgument<?> arg : argumentsToResolve) {
            debugArgument(logger, arg, CLASS_NAME);

            arg.applyValue(getValueWithoutPrefix(arg, getPrefix()).toUpperCase());
            arg.setDisplayMode(DisplayMode.UNMASKED);
            // arg.setDisplayMode(DisplayMode.MASKED);
        }
    }
}
