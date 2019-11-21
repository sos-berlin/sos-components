package com.sos.jobscheduler.history.master;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSDate;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.EventMeta.EventSeq;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.event.master.handler.LoopEventHandler;
import com.sos.jobscheduler.event.master.handler.notifier.Mailer;
import com.sos.jobscheduler.history.master.configuration.HistoryMasterConfiguration;
import com.sos.jobscheduler.history.master.model.HistoryModel;

public class HistoryMasterHandler extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMasterHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final SOSHibernateFactory factory;
    private HistoryModel model;
    private Long lastKeepEvents;
    private Long lastTornNotifier;
    private long counterTornNotifier = 0;
    // private boolean rerun = false;

    public HistoryMasterHandler(SOSHibernateFactory hibernateFactory, Configuration config, Mailer notifier, EventPath path,
            Class<? extends IEntry> clazz) {
        super(config, path, clazz, notifier);
        factory = hibernateFactory;
    }

    @Override
    public void run() {
        super.run();

        String method = "run";
        try {
            HistoryMasterConfiguration conf = (HistoryMasterConfiguration) getMasterConfig();
            // useLogin(getSettings().getCurrent().useLogin());
            setIdentifier(Thread.currentThread().getName() + "-" + conf.getCurrent().getId());
            model = new HistoryModel(factory, conf, getIdentifier());
            executeGetEventId();
            start(model.getStoredEventId());
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", getIdentifier(), method, e.toString()), e);
            getNotifier().notifyOnError(method, e);
            wait(getConfig().getHandler().getWaitIntervalOnError());
        }
    }

    @Override
    public void close() {
        super.close();
        if (model != null) {
            model.close();
        }
    }

    @Override
    public Long onEmptyEvent(Long eventId, Event event) {
        // if (rerun) {
        return execute(false, eventId, event);
        // }
    }

    @Override
    public Long onNonEmptyEvent(Long eventId, Event event) {
        // rerun = false;
        return execute(true, eventId, event);
    }

    @Override
    public Long onTornEvent(Long eventId, Event event) {
        String msg = String.format("[%s][onTornEvent][%s][%s][%s]%s", getIdentifier(), event.getType().name(), eventId, event.getAfter(),
                getHttpClient().getLastRestServiceDuration());
        LOGGER.warn(msg);
        sendTornNotifierOnError(msg, null);

        return event.getAfter();
    }

    private void executeGetEventId() {
        String method = "executeGetEventId";
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                model.setStoredEventId(model.getEventId());
                run = false;
                LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), method, model.getStoredEventId()));
                lastKeepEvents = SOSDate.getMinutes(new Date());
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, count, e.toString()), e);
                getNotifier().notifyOnError(String.format("[%s][%s]", method, count), e);
                wait(getConfig().getHandler().getWaitIntervalOnError());
            }
        }
    }

    private Long execute(boolean onNonEmptyEvent, Long eventId, Event event) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]%s, eventId=%s", getIdentifier(), method, event.getType().name(), eventId));
        }
        Long newEventId = null;
        try {
            if (onNonEmptyEvent) {
                newEventId = model.process(event, getHttpClient().getLastRestServiceDuration());
            } else {
                newEventId = event.getLastEventId();
                // TODO temporary as info
                // LOGGER.info(String.format("[%s][%s][%s][%s][%s]%s", getIdentifier(), method, event.getType().name(), eventId, newEventId,
                // getLastRestServiceDuration()));
                LOGGER.info(String.format("[%s][%s][%s-%s]%s", getIdentifier(), getHttpClient().getLastRestServiceDuration(), eventId, newEventId,
                        event.getType().name()));
            }
            // TODO EmptyEvent must be stored in the database too or not send KeepEvents by Empty or anything else ...
            sendKeepEvents(newEventId);
            sendTornNotifierOnSuccess(String.format("[%s][%s]%s, eventId=%s", getIdentifier(), method, event.getType().name(), eventId));
        } catch (Throwable e) {
            // rerun = true;
            LOGGER.error(String.format("[%s][%s]%s", getIdentifier(), method, e.toString()), e);
            getNotifier().notifyOnError(method, e);
            wait(getConfig().getHandler().getWaitIntervalOnError());
            // TODO endless loop
            newEventId = eventId;
        }
        return newEventId;
    }

    private void sendTornNotifierOnError(String msg, Throwable e) {
        counterTornNotifier++;
        if (lastTornNotifier == null) {
            lastTornNotifier = new Long(0);
        }
        Long currentMinutes = SOSDate.getMinutes(new Date());
        if ((currentMinutes - lastTornNotifier) >= getConfig().getHandler().getNotifyIntervalOnTornEvent()) {
            if (counterTornNotifier == 1) {
                getNotifier().notifyOnWarning(EventSeq.Torn.name(), msg, e);
            } else {
                getNotifier().notifyOnError(EventSeq.Torn.name(), msg, e);
            }
            lastTornNotifier = currentMinutes;
        }
    }

    private void sendTornNotifierOnSuccess(String body) {
        if (lastTornNotifier != null) {
            getNotifier().notifyOnRecovery(EventSeq.Torn.name(), body);
        }
        lastTornNotifier = null;
        counterTornNotifier = 0;
    }

    private void sendKeepEvents(Long eventId) {
        String method = "sendKeepEvents";
        if (eventId != null && eventId > 0 && lastKeepEvents != null) {
            Long currentMinutes = SOSDate.getMinutes(new Date());
            if ((currentMinutes - lastKeepEvents) >= ((HistoryMasterConfiguration) getMasterConfig()).getKeepEventsInterval()) {
                LOGGER.info(String.format("[%s][%s]eventId=%s", getIdentifier(), method, eventId));
                try {
                    String answer = keepEvents(eventId, getToken());
                    if (answer != null && !answer.equals("Accepted")) {
                        LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, eventId, answer));
                    }
                } catch (Throwable t) {
                    LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, eventId, t.toString()), t);
                } finally {
                    lastKeepEvents = currentMinutes;
                }
            }
        }

    }
}
