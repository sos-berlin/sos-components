package com.sos.js7.job.resolver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStepLogger;

public class JobArgumentValueResolverCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobArgumentValueResolverCache.class);

    private static final Map<String, String> PREFIX_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private static final String DEFAULT_RESOLVER_BASE64 = DefaultBase64ValueResolver.class.getName();
    private static final String DEFAULT_RESOLVER_ENCRYPTION = DefaultEncryptionResolver.class.getName();

    static {
        try {
            cacheResolverMethods(DEFAULT_RESOLVER_BASE64);
            cacheResolverMethods(DEFAULT_RESOLVER_ENCRYPTION);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private static void cacheResolverMethods(String className) throws Exception {
        Class<?> resolverClass = Class.forName(className);
        if (!IJobArgumentValueResolver.class.isAssignableFrom(resolverClass)) {
            throw new IllegalArgumentException(className + " does not implement " + IJobArgumentValueResolver.class.getName() + " interface");
        }

        // public static String getPrefix()
        Method getPrefixMethod = resolverClass.getMethod("getPrefix");
        if (!getPrefixMethod.getReturnType().equals(String.class)) {
            throw new IllegalArgumentException(className + " does not have a valid 'public static String getPrefix()' method");
        }

        // public static void resolve(List<JobArgument<String> toResolve,OrderProcessStepLogger logger,Map<String,Object> allArguments)
        Method resolveMethod = resolverClass.getMethod("resolve", List.class, OrderProcessStepLogger.class, Map.class);

        String prefix = (String) getPrefixMethod.invoke(null);
        PREFIX_CACHE.put(prefix, className);
        METHOD_CACHE.put(prefix, resolveMethod);
    }

    public static void addResolver(String resolverClassName) throws Exception {
        cacheResolverMethods(resolverClassName);
    }

    public static Method getResolveMethod(String prefix) {
        return METHOD_CACHE.get(prefix);
    }

    public static String getResolverClassName(String prefix) {
        return PREFIX_CACHE.get(prefix);
    }

    public static List<String> getResolverPrefixes() {
        return METHOD_CACHE.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    }

    public static void resolve(String prefix, List<JobArgument<?>> toResolve, OrderProcessStepLogger logger, Map<String, JobArgument<?>> allArguments)
            throws Exception {
        METHOD_CACHE.get(prefix).invoke(null, toResolve, logger, allArguments);
    }

}
