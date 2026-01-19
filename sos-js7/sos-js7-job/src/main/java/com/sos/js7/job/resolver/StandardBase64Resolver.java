package com.sos.js7.job.resolver;

import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSBase64;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArgumentValueIterator;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;

public class StandardBase64Resolver extends JobArgumentValueResolver {

    private static final String IDENTIFIER = StandardBase64Resolver.class.getSimpleName();

    public static String getPrefix() {
        return "base64:";
    }

    /** @apiNote Throw exception if any argument cannot be resolved */
    public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?>> allArguments)
            throws Exception {

        for (JobArgument<?> arg : argumentsToResolve) {
            debugArgument(logger, IDENTIFIER, arg);

            JobArgumentValueIterator iterator = arg.newValueIterator(getPrefix());
            while (iterator.hasNext()) {
                try {
                    iterator.set(SOSBase64.decode(iterator.nextWithoutPrefix()));
                } catch (Exception e) {
                    throw new JobArgumentException(iterator, e);
                }
            }
        }
    }

}
