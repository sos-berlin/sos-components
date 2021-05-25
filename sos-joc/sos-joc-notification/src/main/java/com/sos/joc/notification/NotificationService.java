package com.sos.joc.notification;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.cluster.common.ClusterServices;

public class NotificationService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private static final String IDENTIFIER = ClusterServices.notification.name();

    private ExecutorService threadPool = null;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();
    private final Object lock = new Object();

    public NotificationService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
        AJocClusterService.setLogger(IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, AConfigurationSection configuration, StartupMode mode) {
        try {
            closed.set(false);
            lastActivityStart.set(new Date().getTime());
            AJocClusterService.setLogger(IDENTIFIER);

            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));
            lastActivityEnd.set(new Date().getTime());
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(getThreadGroup(), IDENTIFIER + "-start"));
            AtomicLong errors = new AtomicLong();
            Runnable thread = new Runnable() {

                @Override
                public void run() {
                    while (!closed.get()) {
                        AJocClusterService.setLogger(IDENTIFIER);
                        try {

                            waitFor(30);
                        } catch (Throwable e) {
                            AJocClusterService.setLogger(IDENTIFIER);
                            LOGGER.error(e.toString(), e);
                            long current = errors.get();
                            if (current > 100) {
                                closed.set(true);
                                AJocClusterService.setLogger(IDENTIFIER);
                                LOGGER.error(String.format("[%s][%s][start][stopped]max errors(%s) reached", getIdentifier(), mode, current));
                            } else {
                                errors.set(current + 1);
                                waitFor(60);
                            }
                        }
                        AJocClusterService.clearLogger();
                    }
                }
            };
            threadPool.submit(thread);
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        } finally {
            AJocClusterService.clearLogger();
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));

        closed.set(true);
        close(mode);
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        AJocClusterService.clearLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        return new JocServiceAnswer(Instant.ofEpochMilli(lastActivityStart.get()), Instant.ofEpochMilli(lastActivityEnd.get()));
    }

    @Override
    public void update(List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    private void close(StartupMode mode) {
        synchronized (lock) {
            lock.notifyAll();
        }
        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            threadPool = null;
        }
    }

    private void waitFor(int interval) {
        if (!closed.get() && interval > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[wait]%ss ...", interval));
            }
            try {
                synchronized (lock) {
                    lock.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[wait]sleep interrupted due to task stop");
                    }
                } else {
                    LOGGER.warn(String.format("[wait]%s", e.toString()), e);
                }
            }
        }
    }

    protected void setLastActivityStart(Long val) {
        lastActivityStart.set(val);
    }

    protected void setLastActivityEnd(Long val) {
        lastActivityEnd.set(val);
    }
}
