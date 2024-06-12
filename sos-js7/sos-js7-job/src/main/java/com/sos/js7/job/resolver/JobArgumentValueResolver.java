package com.sos.js7.job.resolver;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

/** The two public static methods required to implement are:<br/>
 * - 1) public static String getPrefix()<br/>
 * - 2) public static void resolve(OrderProcessStepLogger logger, List&lt;JobArgument&lt;?&gt;&gt; argumentsToResolve, Map&lt;String,JobArgument&lt;?&gt;&gt;
 * allArguments) throws Exception<br/>
 */
public abstract class JobArgumentValueResolver implements IJobArgumentValueResolver {

    public static void debugArgument(OrderProcessStepLogger logger, JobArgument<?> arg, String className) {
        if (logger.isDebugEnabled()) {
            logger.debug("[" + className + "][resolve][argument]name=" + arg.getName() + ",value=" + arg.getValue());
        }
    }

    public static String getValueWithoutPrefix(JobArgument<?> arg, String prefix) throws Exception {
        return arg.getValue().toString().substring(prefix.length());
    }

}
