package com.sos.joc.cleanup.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cleanup.CleanupService;
import com.sos.joc.cleanup.model.CleanupTaskModel;
import com.sos.joc.cluster.JocCluster;

public class CleanupPauseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupPauseHandler.class);

    private final AtomicBoolean isPaused;

    private CleanupTaskModel task;
    private PauseConfig pauseConfig;
    private ScheduledExecutorService scheduler;
    private AtomicBoolean cleanupRunning;
    private String logPrefix = null;

    public CleanupPauseHandler() {
        this.isPaused = new AtomicBoolean(false);
    }

    public void start(CleanupTaskModel task) {
        this.task = task;
        this.pauseConfig = createPauseConfig();
        start();
    }

    public void stop() {
        isPaused.set(false);
        if (cleanupRunning != null) {
            cleanupRunning.set(false);
        }
        if (pauseConfig != null) {
            task.getService().stopPause(CleanupService.IDENTIFIER);
            CleanupService.setServiceLogger();
        }
        if (scheduler != null) {
            JocCluster.shutdownThreadPool(getLogPrefix(), scheduler, 1);
        }
    }

    private void start() {
        if (pauseConfig != null) {
            // TODO cleanup ThreadGroup
            scheduler = Executors.newScheduledThreadPool(1);
            cleanupRunning = new AtomicBoolean(false);

            Runnable pauseTask = () -> {
                CleanupService.setServiceLogger();
                boolean run = true;
                x: while (run) {
                    if (isPaused.get()) {
                        isPaused.set(false);
                    }
                    if (task.isStopped()) {
                        run = false;
                        break x;
                    }

                    // 1) Stop service for duration
                    task.getService().startPause(CleanupService.IDENTIFIER, pauseConfig.duration);
                    CleanupService.setServiceLogger();
                    LOGGER.info(getLogPrefix() + "start cleanup...");
                    isPaused.set(false);
                    try {
                        TimeUnit.SECONDS.sleep(pauseConfig.getDuration());
                    } catch (InterruptedException e) {
                        run = false;
                        Thread.currentThread().interrupt();
                        break x;
                    }

                    if (task.isStopped()) {
                        run = false;
                        break x;
                    }

                    // 2) Unstop service for delay
                    LOGGER.info(getLogPrefix() + "pause cleanup...");
                    isPaused.set(true);
                    waitWhenCleanupRunning();
                    task.getService().stopPause(CleanupService.IDENTIFIER);
                    CleanupService.setServiceLogger();
                    try {
                        TimeUnit.SECONDS.sleep(pauseConfig.getDelay());
                    } catch (InterruptedException e) {
                        run = false;
                        isPaused.set(false);
                        Thread.currentThread().interrupt();
                        break x;
                    }
                }
            };
            scheduler.submit(pauseTask);
        }
    }

    public void waitWhenPaused() {
        if (scheduler != null) {
            int counter = 0;
            while (isPaused.get() && !task.isStopped()) {
                try {
                    if (counter % 30 == 0) {
                        LOGGER.info(getLogPrefix() + "wait because is paused");
                    }
                    TimeUnit.SECONDS.sleep(1);
                    counter++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void waitWhenCleanupRunning() {
        if (scheduler != null) {
            int counter = 0;
            while (cleanupRunning.get() && !task.isStopped()) {
                try {
                    if (counter % 30 == 0) {
                        LOGGER.info(getLogPrefix() + "[service stopPause]wait because the cleanup is running");
                    }
                    TimeUnit.SECONDS.sleep(1);
                    counter++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void setCleanupRunning(boolean val) {
        if (cleanupRunning != null) {
            cleanupRunning.set(val);
        }
    }

    private String getLogPrefix() {
        if (logPrefix == null) {
            logPrefix = "[" + task.getIdentifier() + "][" + CleanupService.IDENTIFIER + "][pauseHandler]";
        }
        return logPrefix;
    }

    private PauseConfig createPauseConfig() {
        PauseConfig config = null;
        long duration = task.getForceCleanup().getHistoryPauseDurationAge().getSeconds();
        if (task.getForceCleanup().force() && duration > 0) {
            config = new PauseConfig(duration, task.getForceCleanup().getHistoryPauseDelayAge().getSeconds());
        }
        return config;
    }

    public PauseConfig getPauseConfig() {
        return pauseConfig;
    }

    public class PauseConfig {

        // in seconds
        private final int duration;
        private final int delay;

        private PauseConfig(long duration, long delay) {
            this.duration = Long.valueOf(duration).intValue();
            this.delay = Long.valueOf(delay).intValue();
        }

        public int getDuration() {
            return duration;
        }

        public int getDelay() {
            return delay;
        }
    }

}
