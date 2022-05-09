package com.sos.joc.cluster.configuration;

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
import com.sos.joc.model.cluster.common.ClusterServices;

public class JocClusterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterConfiguration.class);

    public static final String IDENTIFIER = ClusterServices.cluster.name();

    public enum StartupMode {
        unknown, automatic, manual_restart, automatic_switchover, manual_switchover, settings_changed, manual;
    }

    private static final String CLASS_NAME_SERVICE_CLEANUP = "com.sos.joc.cleanup.CleanupService";
    private static final String CLASS_NAME_SERVICE_DAILYPLAN = "com.sos.joc.dailyplan.DailyPlanService";
    private static final String CLASS_NAME_SERVICE_HISTORY = "com.sos.joc.history.HistoryService";
    private static final String CLASS_NAME_SERVICE_MONITOR = "com.sos.joc.monitoring.MonitorService";

    private static final String CLASS_NAME_CLUSTER_MODE = "com.sos.js7.license.joc.ClusterLicenseCheck";

    private List<Class<?>> services;
    private final ThreadGroup threadGroup;

    private final ClusterModeResult clusterModeResult;

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

    public JocClusterConfiguration(Properties properties) {
        if (properties != null) {
            setConfiguration(properties);
        }
        threadGroup = new ThreadGroup(JocClusterConfiguration.IDENTIFIER);
        clusterModeResult = clusterMode();
        register();
    }

    private void register() {
        services = new ArrayList<>();
        addServiceClass(CLASS_NAME_SERVICE_HISTORY);
        addServiceClass(CLASS_NAME_SERVICE_DAILYPLAN);
        addServiceClass(CLASS_NAME_SERVICE_CLEANUP);
        addServiceClass(CLASS_NAME_SERVICE_MONITOR);
    }

    private void addServiceClass(String className) {
        try {
            services.add(Class.forName(className));
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

    public List<Class<?>> getServices() {
        return services;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public ClusterModeResult getClusterModeResult() {
        return clusterModeResult;
    }

    private ClusterModeResult clusterMode() {
        final ClassLoader webAppCL = this.getClass().getClassLoader();
        URL lJar = null;
        try {
            lJar = webAppCL.loadClass(CLASS_NAME_CLUSTER_MODE).getProtectionDomain().getCodeSource().getLocation();
        } catch (Throwable e1) {
            return null;
        }

        URLClassLoader currentCL = null;
        try {
            List<URL> jars = new ArrayList<>();
            jars.add(lJar);

            URL slf4jJar = webAppCL.loadClass(LoggerFactory.class.getName()).getProtectionDomain().getCodeSource().getLocation();
            List<Path> logJars = SOSPath.getFileList(Paths.get(slf4jJar.toURI()).getParent(), "^slf4j|^log4j",
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            for (Path jar : logJars) {
                jars.add(jar.toUri().toURL());
            }
            currentCL = new URLClassLoader(jars.stream().toArray(URL[]::new));

            Object o = currentCL.loadClass(CLASS_NAME_CLUSTER_MODE).newInstance();
            ClusterModeResult cmResult = new ClusterModeResult();
            try {
                TimeUnit.SECONDS.sleep(1);// waiting for lJar logging ...
            } catch (Throwable e) {
            }
             
            for(Method m : o.getClass().getDeclaredMethods()) {
                switch(m.getName()) {
                case "getValidFrom":
                    cmResult.setValidFrom((Date)m.invoke(o));
                    break;
                case "getValidUntil":
                    cmResult.setValidUntil((Date)m.invoke(o));
                    break;
                default:
                    cmResult.setUse((boolean) m.invoke(o));
                    break;
                }
            }
            // provide from/until for joc properties api
            return cmResult;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (currentCL != null) {
                try {
                    currentCL.close();
                } catch (Throwable e) {
                }
            }
        }
        return null;
    }
}
