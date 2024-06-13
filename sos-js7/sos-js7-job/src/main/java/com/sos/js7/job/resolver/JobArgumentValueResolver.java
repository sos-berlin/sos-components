package com.sos.js7.job.resolver;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

/** The two public static methods required to implement are:<br/>
 * - 1) public static String getPrefix()<br/>
 * - 2) public static void resolve(OrderProcessStepLogger logger, List&lt;JobArgument&lt;?&gt;&gt; argumentsToResolve, Map&lt;String,JobArgument&lt;?&gt;&gt;
 * allArguments) throws Exception<br/>
 */
public abstract class JobArgumentValueResolver implements IJobArgumentValueResolver {

    /** Creates standardized debug output, e.g.:<br/>
     * [DEBUG][&lt;resolverIdentifier&gt;][resolve][argument]name=&lt;argument name&gt;,value=&lt;argument value before resolving&gt;<br />
     * 
     * @apiNote: When the argument is not declared in a JobArgument class, the value will be displayed as &lt;hidden&gt;
     * @param logger Logger for logging to the job log output.
     * @param resolverIdentifier The resolver identifier.
     * @param arg JobArgument to be debugged. */
    public static void debugArgument(OrderProcessStepLogger logger, String resolverIdentifier, JobArgument<?> arg) {
        if (logger.isDebugEnabled()) {
            logger.debug("[" + resolverIdentifier + "][resolve][argument]name=" + arg.getName() + ",value=" + arg.getDisplayValue());
        }
    }

    /** Removes the specified prefix from the value of the given JobArgument.
     * 
     * @param arg The JobArgument whose value's prefix needs to be removed.
     * @param prefix The prefix to be removed from the argument's value.
     * @return The value of the argument without the specified prefix.
     * @throws Exception if an error occurs while processing the argument's value. */
    public static String getValueWithoutPrefix(JobArgument<?> arg, String prefix) throws Exception {
        return arg.getValue().toString().substring(prefix.length());
    }

}
