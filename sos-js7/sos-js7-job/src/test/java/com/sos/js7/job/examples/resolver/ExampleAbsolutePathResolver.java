package com.sos.js7.job.examples.resolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArgumentValueIterator;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.resolver.JobArgumentValueResolver;

/** Resolves values with the prefix <i>apath:</i> into an absolute path */
public class ExampleAbsolutePathResolver extends JobArgumentValueResolver {

    /** Identifier used for logging purposes. */
    private static final String IDENTIFIER = ExampleAbsolutePathResolver.class.getSimpleName();

    /** Determines the behavior when an argument cannot be resolved.
     * <ul>
     * <li><strong>true</strong> (default): Throws an exception if any argument cannot be resolved.</li>
     * <li><strong>false</strong>: In case of exceptions:
     * <ul>
     * <li>The argument value is not resolved.</li>
     * <li>The reason for the exception is set for the argument and is visible in the job output.</li>
     * </ul>
     * </li>
     * </ul>
     */
    private static final String ARG_NAME_FAIL_ON_RESOLVER_ERROR = "fail_on_resolver_error";

    /** Required method to implement.<br/>
     * Returns the prefix that this resolver is responsible for. */
    public static String getPrefix() {
        return "apath:";
    }

    /** Required method to implement.<br/>
     * Resolves the values of the given arguments by converting them into an absolute path.<br/>
     * <br/>
     * See explanation of debugArgument, DisplayMode, etc., provided with the example in the
     * <i>com.sos.js7.job.examples.resolver.ExampleUpperCaseResolver.resolve(...)</i> method.
     * 
     * @param logger Logger for logging to the job log output.
     * @param argumentsToResolve List of arguments to be resolved.
     * @param allArguments Map of all available arguments.
     * @throws Exception if an error occurs during resolution. */
    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {

        for (JobArgument<?> arg : argumentsToResolve) {
            debugArgument(logger, IDENTIFIER, arg);

            // Use iterator to process all argument types (String,List,Map, ...)
            JobArgumentValueIterator iterator = arg.newValueIterator(getPrefix());
            try {
                while (iterator.hasNext()) {
                    Path path = Paths.get(iterator.nextWithoutPrefix()).toAbsolutePath();
                    iterator.set(path);
                }

                arg.setDisplayMode(DisplayMode.UNMASKED);
                // arg.setDisplayMode(DisplayMode.MASKED);
            } catch (Exception e) {
                if (failOnResolverError(allArguments)) {
                    throw new JobArgumentException(iterator, e);
                }
                arg.setNotAcceptedValue(iterator, e);
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
