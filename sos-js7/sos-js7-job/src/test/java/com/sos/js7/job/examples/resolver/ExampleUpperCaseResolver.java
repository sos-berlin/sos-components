package com.sos.js7.job.examples.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArgumentValueIterator;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.resolver.JobArgumentValueResolver;

/** Resolves values with the prefix <i>upper:</i> in uppercase */
public class ExampleUpperCaseResolver extends JobArgumentValueResolver {

    /** Identifier used for logging purposes. */
    private static final String IDENTIFIER = ExampleUpperCaseResolver.class.getSimpleName();

    /** Required method to implement.<br/>
     * Returns the prefix that this resolver is responsible for. */
    public static String getPrefix() {
        return "upper:";
    }

    /** Required method to implement.<br/>
     * Resolves the values of the given arguments by converting them to uppercase.<br/>
     * 
     * @param logger Logger for logging to the job log output.
     * @param argumentsToResolve List of arguments to be resolved.
     * @param allArguments Map of all available arguments.
     * @throws Exception if an error occurs during resolution. */
    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {
        for (JobArgument<?> arg : argumentsToResolve) {
            // Creates standardized debug output, e.g.:
            // [DEBUG][<IDENTIFIER>][resolve][argument][name=<argument name>,...]value=<argument value before resolving>
            // - Note: When the argument is not declared in a JobArgument class, the value will be displayed as <hidden>
            debugArgument(logger, IDENTIFIER, arg);

            // Use iterator to process all argument types (String,List,Map, ...)
            JobArgumentValueIterator iterator = arg.newValueIterator(getPrefix());
            while (iterator.hasNext()) {
                try {
                    // Set converted value
                    iterator.set(iterator.nextWithoutPrefix().toUpperCase());
                } catch (Throwable e) {
                    throw new JobArgumentException(iterator, e);
                }
            }

            // Set argument display mode:
            // - Note: When the argument is not declared in a JobArgument class, the value will be displayed as <hidden>
            // - Set UNMASKED for testing to see the resolution result
            arg.setDisplayMode(DisplayMode.UNMASKED);

            // - Set MASKED to ensure that the resolution result will be displayed in the job log output as ********
            // arg.setDisplayMode(DisplayMode.MASKED);
        }
    }

}
