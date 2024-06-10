package com.sos.js7.job.resolver;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public abstract class AJobArgumentValueResolver implements IJobArgumentValueResolver {

    public static void debugArgument(OrderProcessStepLogger logger, JobArgument<?> arg, String className) {
        if (logger.isDebugEnabled()) {
            logger.debug("[" + className + "][resolve][argument]name=" + arg.getName() + ",value=" + arg.getValue());
        }
    }

    public static String getValueWithoutPrefix(JobArgument<?> arg, String prefix) throws Exception {
        return arg.getValue().toString().substring(prefix.length());
    }
    
}
