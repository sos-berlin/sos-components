package com.sos.jitl.jobs.rest;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobResourceUtils {

    private static List<JobArgument<String>> collectArguments(RestJobArguments args) {
        return Arrays.asList(
                args.getReturnVariable(),
                args.getMyRequest(),
                args.getLogItems(),
                args.getKeystoreFile(),
                args.getKeystoreKeyPassword(),
                args.getKeystoreStorePassword(),
                args.getKeystoreAlias(),
                args.getTruststoreFile(),
                args.getTruststoreStorePassword(),
                args.getApiServerCSFile(),
                args.getApiServerCSKey(),
                args.getApiServerCSPassword(),
                args.getApiServerUsername(),
                args.getApiServerToken(),
                args.getApiServerPassword(),
                args.getApiServerPrivateKeyPath(),
                args.getApiUrl()
        );
    }


    private  static  void logArgument(JobArgument<String> arg, OrderProcessStepLogger logger) {
        if (arg.isDirty()) {
            logger.info(arg.getName() + " = " + arg.getValue());
        } else if (arg.getDefaultValue() != null && !arg.getDefaultValue().isEmpty()) {
            logger.info(arg.getName() + " (default) = " + arg.getDefaultValue());
        } else {
            logger.info(arg.getName() + " was not set and did not hold a default value");
        }
    }

    public static void logKnownArguments(OrderProcessStepLogger logger, RestJobArguments myArgs) {
        List<JobArgument<String>> allArgs = collectArguments(myArgs);
        for (JobArgument arg : allArgs) {
            logArgument(arg, logger);
        }
    }

   // Log grouped arguments
   
    public static void logAllArguments(OrderProcessStepLogger logger, Map<String, JobArgument<?>> allArgs) {
        for (Map.Entry<String, JobArgument<?>> entry : allArgs.entrySet()) {
            JobArgument<?> arg = entry.getValue();
            ValueSource valueSource = arg.getValueSource();

            String typeName = "UNKNOWN_TYPE";
            String sourceName = "UNKNOWN_SOURCE";

            if (valueSource != null) {
                try {
                    // Access protected getType() using reflection
                    java.lang.reflect.Method getTypeMethod = ValueSource.class.getDeclaredMethod("getType");
                    getTypeMethod.setAccessible(true);
                    Object typeObj = getTypeMethod.invoke(valueSource);

                    if (typeObj != null) {
                        typeName = typeObj.toString(); // e.g., JOB_RESOURCE, JAVA, etc.
                    }
                } catch (Exception e) {
                    typeName = "INACCESSIBLE_TYPE";
                }

                if (valueSource.getSource() != null && !valueSource.getSource().isEmpty()) {
                    sourceName = valueSource.getSource();
                }else {
                	logger.info(
                            entry.getKey() + " : " + arg.getValue()
                            + " ( source=" + typeName +  ", modified=" + arg.isDirty() + " )"
                        );
                	logger.debug("no jobresource found");
                	continue;
                }
            }

            logger.info(
                entry.getKey() + " : " + arg.getValue()
                + " ( source=" + typeName + "(" + sourceName + ")"
                + ", modified=" + arg.isDirty() + " )"
            );
        }
    }

    
 
}
