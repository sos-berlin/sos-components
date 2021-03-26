package com.sos.joc.cluster.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private List<Class<?>> services;
    private final ThreadGroup threadGroup;

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
        register();
    }

    private void register() {
        services = new ArrayList<>();
        addServiceClass(CLASS_NAME_HISTORY);
        addServiceClass(CLASS_NAME_DAILYPLAN);
        addServiceClass(CLASS_NAME_CLEANUP);
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

}
