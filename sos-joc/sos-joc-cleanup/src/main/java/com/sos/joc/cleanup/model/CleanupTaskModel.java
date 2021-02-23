package com.sos.joc.cleanup.model;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.model.cluster.common.ClusterServices;

public class CleanupTaskModel implements ICleanupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskModel.class);

    private final IJocClusterService service;
    private final Object lock = new Object();
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private String logIdentifier = null;
    private JocServiceTaskAnswerState state = null;

    protected CleanupTaskModel(IJocClusterService service) {
        this.service = service;
    }

    @Override
    public void start() {
        state = JocServiceTaskAnswerState.UNCOMPLETED;
        stopped.set(false);
    }

    @Override
    public JocServiceTaskAnswer stop() {
        stopped.set(true);

        synchronized (lock) {
            lock.notifyAll();
        }
        return new JocServiceTaskAnswer(state);
    }

    @Override
    public void setState(JocServiceTaskAnswerState val) {
        state = val;
    }

    @Override
    public JocServiceTaskAnswerState getState() {
        return state;
    }

    @Override
    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public String getIdentifier() {
        return service == null ? null : service.getIdentifier();
    }

    public IJocClusterService getService() {
        return service;
    }

    protected String getLogIdentifier() {
        if (logIdentifier == null) {
            logIdentifier = ClusterServices.cleanup.name() + "_" + service.getIdentifier();
        }
        return logIdentifier;
    }

    protected void waitFor(int interval) {
        if (!stopped.get() && interval > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[wait]%ss ...", interval));
            }
            try {
                synchronized (lock) {
                    lock.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (stopped.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[wait]sleep interrupted due to task stop");
                    }
                } else {
                    LOGGER.warn(String.format("[wait]%s", e.toString()), e);
                }
            }
        }
    }

}
