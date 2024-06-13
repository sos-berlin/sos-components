package com.sos.js7.job.resolver;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSReflection;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.OrderProcessStepLogger;

public class JobArgumentValueResolverCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobArgumentValueResolverCache.class);

    // ConcurrentHashMap is not required because of the static block
    private static Map<String, Method> METHOD_CACHE = new LinkedHashMap<>();

    private static final String CUSTOM_RESOLVERS_DIR = "user_lib";

    static {
        LOGGER.info("start...");
        try {
            cacheStandardResolvers();
            cacheCustomResolvers();
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        LOGGER.info("end");
    }

    // Static method to ensure the class is loaded
    public static void initialize() {
        // This method is intentionally empty
    }

    private static void cacheStandardResolvers() throws Exception {
        cacheResolver(StandardBase64Resolver.class);
        cacheResolver(StandardEncryptionResolver.class);
    }

    // Performance load/check from user_lib
    // -- 122 jars (copy of 3rd-party) ~ 16-20s
    // -- 10 jars ~ 100ms
    // -- 3 jars ~ 12ms
    private static void cacheCustomResolvers() {
        List<Path> jars = getCustomJars();
        if (jars == null) {
            return;
        }
        for (Path jar : jars) {
            try {
                List<Class<?>> classes = SOSReflection.findClassesInJarFile(jar, IJobArgumentValueResolver.class);
                for (Class<?> clazz : classes) {
                    try {
                        cacheResolver(clazz);
                    } catch (Throwable e) {
                        LOGGER.error("[" + jar + "][" + clazz + "]" + e.toString(), e);
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("[" + jar + "]" + e.toString(), e);
            }
        }
    }

    private static List<Path> getCustomJars() {
        // List<Path> jars = SOSReflection.getJarsFromClassPath(CUSTOM_RESOLVERS_DIR);
        Path dir = JobHelper.getAgentLibDir().resolve(CUSTOM_RESOLVERS_DIR);
        try {
            return SOSPath.getFileList(dir, ".*\\.jar$", java.util.regex.Pattern.CASE_INSENSITIVE);
        } catch (FileNotFoundException e) {
            LOGGER.error("[" + dir + "]" + e.getMessage());
        } catch (Throwable e) {
            LOGGER.error("[" + dir + "]" + e.toString(), e);
        }
        return null;
    }

    private static void cacheResolver(Class<?> clazz) throws Exception {
        // This check is not required – standard resolvers implement the interface, custom – already checked…
        // checkAssignableFrom(clazz);

        // public static String getPrefix()
        Method getPrefixMethod = clazz.getMethod("getPrefix");
        if (!getPrefixMethod.getReturnType().equals(String.class)) {
            throw new IllegalArgumentException(clazz + " does not have a valid 'public static String getPrefix()' method");
        }

        // public static void resolve(OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve, Map<String, JobArgument<?> allArguments) throws
        // Exception
        Method resolveMethod = clazz.getMethod("resolve", OrderProcessStepLogger.class, List.class, Map.class);

        String prefix = (String) getPrefixMethod.invoke(null);
        METHOD_CACHE.put(prefix, resolveMethod);

        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("[cached][" + prefix + "]" + clazz.getName());
        // }
        LOGGER.info("[cached][" + prefix + "]" + clazz.getName());
    }

    @SuppressWarnings("unused")
    private static void checkAssignableFrom(Class<?> clazz) throws Exception {
        if (!IJobArgumentValueResolver.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz + " does not implement " + IJobArgumentValueResolver.class.getName() + " interface");
        }
    }

    public static Method getResolveMethod(String prefix) {
        return METHOD_CACHE.get(prefix);
    }

    public static String getResolverClassName(String prefix) {
        Method m = getResolveMethod(prefix);
        if (m == null) {
            return "unknown";
        }
        return m.getDeclaringClass().getName();
    }

    public static List<String> getResolverPrefixes() {
        return METHOD_CACHE.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    }

    public static void resolve(String prefix, OrderProcessStepLogger logger, List<JobArgument<?>> argumentsToResolve,
            Map<String, JobArgument<?>> allArguments) throws Exception {
        METHOD_CACHE.get(prefix).invoke(null, logger, argumentsToResolve, allArguments);
    }

}
