package com.sos.js7.job.examples.resolver;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.resolver.JobArgumentValueResolver;

/** Resolves values with the prefix <i>apath:</i> into an absolute path */
public class ExampleAbsolutePathResolver extends JobArgumentValueResolver {

    private static final String CLASS_NAME = ExampleAbsolutePathResolver.class.getSimpleName();
    /** true - default - throw exception if any argument cannot be resolved <br/>
     * false - in case of exceptions<br/>
     * - the argument value is not resolved<br/>
     * - the exception reason is set for the argument and is visible in the job output<br/>
     */
    private static final String ARG_NAME_FAIL_ON_RESOLVER_ERROR = "fail_on_resolver_error";

    public static String getPrefix() {
        return "apath:";
    }

    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {
        for (JobArgument<?> arg : argumentsToResolve) {
            debugArgument(logger, arg, CLASS_NAME);

            try {
                arg.applyValue(Paths.get(getValueWithoutPrefix(arg, getPrefix())).toAbsolutePath().toString());
                arg.setDisplayMode(DisplayMode.UNMASKED);
                // arg.setDisplayMode(DisplayMode.MASKED);
            } catch (Throwable e) {
                if (failOnResolverError(allArguments)) {
                    throw e;
                }
                arg.setNotAcceptedValue(arg.getValue(), e);
            }
        }
    }

    private static boolean failOnResolverError(Map<String, JobArgument<?>> allArguments) {
        JobArgument<?> arg = allArguments.get(ARG_NAME_FAIL_ON_RESOLVER_ERROR);
        if (arg == null || arg.getValue() == null) {
            return true;
        }
        return arg.getValue().toString().toLowerCase().equals("true");
    }

}
