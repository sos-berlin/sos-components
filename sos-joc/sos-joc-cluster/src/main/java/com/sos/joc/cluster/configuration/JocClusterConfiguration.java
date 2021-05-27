package com.sos.joc.cluster.configuration;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        unknown, automatic, manual, automatic_switchover, manual_switchover, settings_changed;
    }

    private static final String CLASS_NAME_HISTORY = "com.sos.js7.history.controller.HistoryService";
    private static final String CLASS_NAME_DAILYPLAN = "com.sos.js7.order.initiator.OrderInitiatorService";
    private static final String CLASS_NAME_CLEANUP = "com.sos.joc.cleanup.CleanupService";
    private static final String CLASS_NAME_MONITORING = "com.sos.joc.monitoring.MonitoringService";
    private static final String CLASS_NAME_NOTIFICATION = "com.sos.joc.notification.NotificationService";
    private static final String CLASS_NAME_CLUSTER_MODE = "com.sos.js7.license.joc.ClusterLicenseCheck";

    private List<Class<?>> services;
    private final ThreadGroup threadGroup;
    private final boolean clusterMode;

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
        clusterMode = clusterMode();
        register();
    }

    private void register() {
        services = new ArrayList<>();
        addServiceClass(CLASS_NAME_HISTORY);
        addServiceClass(CLASS_NAME_DAILYPLAN);
        addServiceClass(CLASS_NAME_CLEANUP);
        addServiceClass(CLASS_NAME_MONITORING);
        addServiceClass(CLASS_NAME_NOTIFICATION);
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

    public boolean getClusterMode() {
        return clusterMode;
    }

    private boolean clusterMode() {
        final ClassLoader webAppCL = this.getClass().getClassLoader();
        URL lj = null;
        try {
            lj = webAppCL.loadClass(CLASS_NAME_CLUSTER_MODE).getProtectionDomain().getCodeSource().getLocation();
        } catch (Throwable e1) {
            return false;
        }

        URLClassLoader ucl = null;
        try {
            List<URL> jars = new ArrayList<>();
            jars.add(lj.toURI().toURL());

            URL slf4j = webAppCL.loadClass(LoggerFactory.class.getName()).getProtectionDomain().getCodeSource().getLocation();
            List<Path> logJars = SOSPath.getFileList(Paths.get(slf4j.toURI()).getParent(), "^slf4j|^log4j", java.util.regex.Pattern.CASE_INSENSITIVE);
            for (Path jar : logJars) {
                jars.add(jar.toUri().toURL());
            }
            ucl = new URLClassLoader(jars.stream().toArray(URL[]::new));

            Object o = ucl.loadClass(CLASS_NAME_CLUSTER_MODE).newInstance();
            boolean result = (boolean) o.getClass().getDeclaredMethods()[0].invoke(o);
            try {
                TimeUnit.SECONDS.sleep(1);// wait for logging ..
            } catch (Throwable e) {
            }
            return result;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (ucl != null) {
                try {
                    ucl.close();
                } catch (Throwable e) {
                }
            }
        }
        return false;
    }
}
