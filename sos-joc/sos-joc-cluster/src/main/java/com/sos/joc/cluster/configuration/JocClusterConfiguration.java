package com.sos.joc.cluster.configuration;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.joc.cluster.service.embedded.IJocEmbeddedService;
import com.sos.joc.model.cluster.common.ClusterServices;

public class JocClusterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterConfiguration.class);

    public static final String IDENTIFIER = ClusterServices.cluster.name();

    public enum StartupMode {
        unknown, automatic, manual_restart, failover, manual_switchover, settings_changed, webservice, run_now, controller_added, controller_updated, controller_removed;
    }

    private static final String ACTIVE_MEMBER_SERVICE_CLEANUP = "com.sos.joc.cleanup.CleanupService";
    private static final String ACTIVE_MEMBER_SERVICE_DAILYPLAN = "com.sos.joc.dailyplan.DailyPlanService";
    private static final String ACTIVE_MEMBER_SERVICE_HISTORY = "com.sos.joc.history.HistoryService";
    private static final String ACTIVE_MEMBER_SERVICE_MONITOR = "com.sos.joc.monitoring.HistoryMonitorService";
    private static final String ACTIVE_MEMBER_SERVICE_LOGNOTIFICATION = "com.sos.joc.logmanagement.LogNotificationService";

    private static final String EMBEDDED_SERVICE_MONITOR = "com.sos.joc.monitoring.SystemMonitorService";

    private static final String CLUSTER_MODE = "com.sos.js7.license.joc.ClusterLicenseCheck";

    private final ThreadGroup threadGroup;
    private List<Class<?>> activeMemberServices;
    private List<Class<IJocEmbeddedService>> embeddedServices;

    private ClusterModeResult clusterModeResult;

    private int heartBeatExceededInterval = 60;// seconds

    private int pollingInterval = 30; // seconds
    private int pollingWaitIntervalOnError = 2; // seconds

    // waiting for the answer after change the active memberId
    private int switchMemberWaitCounterOnSuccess = 10; // counter
    // seconds - max wait time = switch_member_wait_counter_on_success*switch_member_wait_interval_on_success+ execution time
    private int switchMemberWaitIntervalOnSuccess = 5;

    // waiting for change the active memberId (on transactions concurrency errors)
    private int switchMemberWaitCounterOnError = 10; // counter
    // seconds - max wait time = switch_member_wait_counter_on_error*switchMemberWaitIntervalOnError+ execution time
    private int switchMemberWaitIntervalOnError = 2;

    private JocClusterConfiguration() {
        threadGroup = null;
    }

    public JocClusterConfiguration(Properties properties) {
        if (properties != null) {
            setConfiguration(properties);
        }
        threadGroup = new ThreadGroup(JocClusterConfiguration.IDENTIFIER);
        clusterModeResult = clusterMode();
        registerActiveMemberServices();
        registerEmbeddedServices();
    }

    public static JocClusterConfiguration defaultConfiguration() {
        return new JocClusterConfiguration();
    }

    private void registerActiveMemberServices() {
        activeMemberServices = new ArrayList<>();
        addActiveMemberService(ACTIVE_MEMBER_SERVICE_HISTORY);
        addActiveMemberService(ACTIVE_MEMBER_SERVICE_DAILYPLAN);
        addActiveMemberService(ACTIVE_MEMBER_SERVICE_CLEANUP);
        addActiveMemberService(ACTIVE_MEMBER_SERVICE_MONITOR);
        addActiveMemberService(ACTIVE_MEMBER_SERVICE_LOGNOTIFICATION);
    }

    private void registerEmbeddedServices() {
        embeddedServices = new ArrayList<>();
        addEmbeddedService(EMBEDDED_SERVICE_MONITOR);
    }

    private void addActiveMemberService(String className) {
        try {
            activeMemberServices.add(Class.forName(className));
        } catch (ClassNotFoundException e) {
            LOGGER.error(String.format("[%s]%s", className, e.toString()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addEmbeddedService(String className) {
        try {
            embeddedServices.add((Class<IJocEmbeddedService>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            LOGGER.error(String.format("[%s]%s", className, e.toString()), e);
        }
    }

    private void setConfiguration(Properties conf) {
        try {
            if (conf.getProperty("cluster_heart_beat_exceeded_interval") != null) {
                heartBeatExceededInterval = Integer.parseInt(conf.getProperty("cluster_heart_beat_exceeded_interval").trim());
            }
            if (conf.getProperty("cluster_polling_interval") != null) {
                pollingInterval = Integer.parseInt(conf.getProperty("cluster_polling_interval").trim());
            }
            if (conf.getProperty("cluster_polling_wait_interval_on_error") != null) {
                pollingWaitIntervalOnError = Integer.parseInt(conf.getProperty("cluster_polling_wait_interval_on_error").trim());
            }
            if (conf.getProperty("cluster_switch_member_wait_counter_on_success") != null) {
                switchMemberWaitCounterOnSuccess = Integer.parseInt(conf.getProperty("cluster_switch_member_wait_counter_on_success").trim());
            }
            if (conf.getProperty("cluster_switch_member_wait_interval_on_success") != null) {
                switchMemberWaitIntervalOnSuccess = Integer.parseInt(conf.getProperty("cluster_switch_member_wait_interval_on_success").trim());
            }
            if (conf.getProperty("cluster_switch_member_wait_counter_on_error") != null) {
                switchMemberWaitCounterOnError = Integer.parseInt(conf.getProperty("cluster_switch_member_wait_counter_on_error").trim());
            }
            if (conf.getProperty("cluster_switch_member_wait_interval_on_error") != null) {
                switchMemberWaitIntervalOnError = Integer.parseInt(conf.getProperty("cluster_switch_member_wait_interval_on_error").trim());
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public int getPollingWaitIntervalOnError() {
        return pollingWaitIntervalOnError;
    }

    public int getSwitchMemberWaitCounterOnSuccess() {
        return switchMemberWaitCounterOnSuccess;
    }

    public int getSwitchMemberWaitIntervalOnSuccess() {
        return switchMemberWaitIntervalOnSuccess;
    }

    public int getSwitchMemberWaitCounterOnError() {
        return switchMemberWaitCounterOnError;
    }

    public int getSwitchMemberWaitIntervalOnError() {
        return switchMemberWaitIntervalOnError;
    }

    public int getHeartBeatExceededInterval() {
        return heartBeatExceededInterval;
    }

    public List<Class<?>> getActiveMemberServices() {
        return activeMemberServices;
    }

    public List<Class<IJocEmbeddedService>> getEmbeddedServices() {
        return embeddedServices;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public ClusterModeResult getClusterModeResult() {
        if (clusterModeResult == null) {
            LOGGER.error("can't evaluate cluster mode. set cluster mode=false");
            clusterModeResult = new ClusterModeResult(false);
        }
        return clusterModeResult;
    }

    public void rereadClusterMode() {
        clusterModeResult = clusterMode();
    }

    private synchronized ClusterModeResult clusterMode() {
        final ClassLoader currentCL = this.getClass().getClassLoader();
        URL clusterModeJar = null;
        try {
            clusterModeJar = currentCL.loadClass(CLUSTER_MODE).getProtectionDomain().getCodeSource().getLocation();
        } catch (Throwable e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[clusterMode][skip][cannot be loaded/located][because not found?]" + e);
            }
            return new ClusterModeResult(false);
        }

        ClusterModeResult result = new ClusterModeResult(true);
        List<URL> jars = new ArrayList<>();
        jars.add(clusterModeJar);

        Throwable dependenciesException = null;
        try {
            // Support old versions - slf4j,log4j + asynchronous logging based on LMAX Disruptor
            URL slf4jJar = currentCL.loadClass(LoggerFactory.class.getName()).getProtectionDomain().getCodeSource().getLocation();
            List<Path> logJars = SOSPath.getFileList(Paths.get(slf4jJar.toURI()).getParent(), "^slf4j|^log4j|^disruptor-",
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            for (Path jar : logJars) {
                jars.add(jar.toUri().toURL());
            }
        } catch (Throwable e) {
            dependenciesException = e;
        }

        try (URLClassLoader tmpCL = new URLClassLoader(jars.stream().toArray(URL[]::new))) {
            try {
                Object o = tmpCL.loadClass(CLUSTER_MODE).getDeclaredConstructor().newInstance();
                for (Method m : o.getClass().getDeclaredMethods()) {
                    switch (m.getName()) {
                    case "getValidFrom":
                        result.setValidFrom((Date) m.invoke(o));
                        break;
                    case "getValidUntil":
                        result.setValidUntil((Date) m.invoke(o));
                        break;
                    default:
                        result.setUse((boolean) m.invoke(o));
                        break;
                    }
                }

                try {
                    // Support old versions - wait for possible logging,
                    // because if tmpCL is closed immediately, the log libraries are no longer available for "later" logging.
                    // An exception can occur(logged in jetty.log), especially with asynchronous logging:
                    // - AsyncLogger error handling event seq=0, value='org.apache.logging.log4j.core.async.RingBufferLogEvent@ ...
                    // -- java.lang.NoClassDefFoundError: org/apache/logging/log4j/message/ParameterizedMessage
                    // - This exception can be ignored because the check was successful and "only" the asynchronous logging failed.
                    // - Sleep tries to avoid this exception...
                    // TODO - This is not particularly efficient because sleep is also executed for current versions where no logging is used...
                    TimeUnit.SECONDS.sleep(1);
                } catch (Throwable e) {

                }
            } catch (Throwable e) {
                if (dependenciesException != null) {
                    LOGGER.warn("[clusterMode][dependencies cannot be loaded]" + dependenciesException, dependenciesException);
                    dependenciesException = null;
                }
                String msg = "Licensed binary code check failed. ";
                if (e instanceof SecurityException) {
                    msg += "SecurityException occurred: Access denied to class or method";
                } else {
                    // ClassNotFoundException,InstantiationException,NoSuchMethodException
                    // IllegalAccessException,IllegalArgumentException,InvocationTargetException,
                    msg += "Ensure that the current Licensed binary code is applied";
                }
                LOGGER.error("[clusterMode][" + msg + "]" + e, e);
            }
        } catch (IOException e) {
            if (dependenciesException != null) {
                LOGGER.warn("[clusterMode][dependencies cannot be loaded]" + dependenciesException, dependenciesException);
            }
            LOGGER.warn("[clusterMode][error while closing the class loader]" + e, e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[clusterMode]executed");
        }
        return result;
    }
}
