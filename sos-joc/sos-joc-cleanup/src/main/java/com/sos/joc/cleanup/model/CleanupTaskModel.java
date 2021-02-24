package com.sos.joc.cleanup.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.model.cluster.common.ClusterServices;

public class CleanupTaskModel implements ICleanupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskModel.class);

    protected static final int WAIT_INTERVAL_ON_BUSY = 65;
    protected static final int WAIT_INTERVAL_ON_ERROR = 60;

    private final JocClusterHibernateFactory factory;
    private final IJocClusterService service;
    private final int batchSize;
    private final Object lock = new Object();

    private JocServiceTaskAnswerState state = null;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private Date date = null;
    private String logIdentifier = null;

    protected CleanupTaskModel(JocClusterHibernateFactory factory, IJocClusterService service, int batchSize) {
        this.factory = factory;
        this.service = service;
        this.batchSize = batchSize;
    }

    @Override
    public void start(Date date) {
        state = JocServiceTaskAnswerState.UNCOMPLETED;
        stopped.set(false);

        boolean run = true;
        while (run) {
            try {
                if (isStopped()) {
                    return;
                }
                if (askService()) {
                    setState(cleanup(date));
                    run = false;
                } else {
                    waitFor(WAIT_INTERVAL_ON_BUSY);
                }
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                waitFor(WAIT_INTERVAL_ON_ERROR);
            }
        }
    }

    @Override
    public JocServiceTaskAnswerState cleanup(Date date) throws SOSHibernateException {
        return state;
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

    public JocClusterHibernateFactory getFactory() {
        return factory;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public IJocClusterService getService() {
        return service;
    }

    public Date getDate() {
        return date;
    }

    protected String getLogIdentifier() {
        if (logIdentifier == null) {
            logIdentifier = ClusterServices.cleanup.name() + "_" + service.getIdentifier();
        }
        return logIdentifier;
    }

    protected boolean askService() {
        LOGGER.info(String.format("[%s]ask %s...", getLogIdentifier(), getIdentifier()));
        JocServiceAnswer info = getService().getInfo();
        LOGGER.info(String.format("[%s]%s", getLogIdentifier(), SOSString.toString(info)));
        return info.getState().equals(JocServiceAnswerState.RELAX);
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
