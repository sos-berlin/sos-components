package com.sos.joc.cluster.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JocClusterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterConfiguration.class);

    public static final String IDENTIFIER = JocClusterServices.cluster.name();

    public enum JocClusterServices {
        cluster, history, dailyplan, proxy;
    }

    private static final String CLASS_NAME_HISTORY = "com.sos.js7.history.controller.HistoryMain";
    private static final String CLASS_NAME_DAILYPLAN = "com.sos.js7.order.initiator.OrderInitiatorMain";

    private static final String PROPERTIES_FILE = "joc/cluster.properties";

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

    public JocClusterConfiguration(Path resourceDirectory) {
        Path configFile = resourceDirectory.resolve(PROPERTIES_FILE).normalize();
        if (Files.exists(configFile)) {
            Properties conf = JocConfiguration.readConfiguration(resourceDirectory.resolve(PROPERTIES_FILE).normalize());
            if (conf != null) {
                setConfiguration(conf);
            }
        }
        threadGroup = new ThreadGroup(JocClusterConfiguration.IDENTIFIER);
        register();
    }

    private void register() {
        services = new ArrayList<>();
        addServiceClass(CLASS_NAME_HISTORY);
        addServiceClass(CLASS_NAME_DAILYPLAN);
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
            if (conf.getProperty("heart_beat_exceeded_interval") != null) {
                heartBeatExceededInterval = Integer.parseInt(conf.getProperty("heart_beat_exceeded_interval").trim());
            }
            if (conf.getProperty("polling_interval") != null) {
                pollingInterval = Integer.parseInt(conf.getProperty("polling_interval").trim());
            }
            if (conf.getProperty("polling_wait_interval_on_error") != null) {
                pollingWaitIntervalOnError = Integer.parseInt(conf.getProperty("polling_wait_interval_on_error").trim());
            }
            if (conf.getProperty("switch_member_wait_counter_on_success") != null) {
                switchMemberWaitCounterOnSuccess = Integer.parseInt(conf.getProperty("switch_member_wait_counter_on_success").trim());
            }
            if (conf.getProperty("switch_member_wait_interval_on_success") != null) {
                switchMemberWaitIntervalOnSuccess = Integer.parseInt(conf.getProperty("switch_member_wait_interval_on_success").trim());
            }
            if (conf.getProperty("switch_member_wait_counter_on_error") != null) {
                switchMemberWaitCounterOnError = Integer.parseInt(conf.getProperty("switch_member_wait_counter_on_error").trim());
            }
            if (conf.getProperty("switch_member_wait_interval_on_error") != null) {
                switchMemberWaitIntervalOnError = Integer.parseInt(conf.getProperty("switch_member_wait_interval_on_error").trim());
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
