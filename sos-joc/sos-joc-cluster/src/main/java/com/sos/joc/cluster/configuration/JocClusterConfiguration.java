package com.sos.joc.cluster.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JocClusterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterConfiguration.class);

    public static final String CLUSTER_ID = "cluster";

    public enum HandlerIdentifier {
        cluster, history, dailyplan, jobstream;
    }

    private static final String PROPERTIES_FILE = "joc/cluster.properties";

    private int heartBeatExceededInterval = 60;// seconds

    private int pollingInterval = 30; // seconds
    private int pollingWaitIntervalOnError = 2; // seconds

    // waiting for the answer after change the active memberId
    private int switchMemberWaitCounterOnSuccess = 10; // counter
    private int switchMemberWaitIntervalOnSuccess = 5;// seconds

    // waiting for change the active memberId (on transactions concurrency errors)
    private int switchMemberWaitCounterOnError = 10; // counter
    private int switchMemberWaitIntervalOnError = 2;// seconds

    // TMP to remove
    private boolean currentIsClusterMember = true;

    public JocClusterConfiguration(Path resourceDirectory) {
        Path configFile = resourceDirectory.resolve(PROPERTIES_FILE).normalize();
        if (Files.exists(configFile)) {
            Properties conf = JocConfiguration.readConfiguration(resourceDirectory.resolve(PROPERTIES_FILE).normalize());
            if (conf != null) {
                setConfiguration(conf);
            }
        }
    }

    private void setConfiguration(Properties conf) {
        try {
            if (conf.getProperty("current_is_cluster_member") != null) {
                currentIsClusterMember = Boolean.parseBoolean(conf.getProperty("current_is_cluster_member").trim());
            }

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

    public boolean currentIsClusterMember() {
        return currentIsClusterMember;
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

}
