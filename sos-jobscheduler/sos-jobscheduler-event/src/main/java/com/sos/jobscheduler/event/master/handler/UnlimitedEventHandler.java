package com.sos.jobscheduler.event.master.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.EventMeta.EventSeq;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;

public class UnlimitedEventHandler extends EventHandler implements IUnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnlimitedEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private EventHandlerMasterSettings settings;

    private boolean closed = false;
    private boolean ended = false;
    private Long tornEventId = null;
  
    /* all intervals in milliseconds */
    private int waitIntervalOnError = 30_000;
    private int waitIntervalOnEnd = 30_000;
    private int waitIntervalOnEmptyEvent = 1_000;
    private boolean wait = false;

    public UnlimitedEventHandler(EventPath path, Class<? extends IEntry> clazz) {
        super(path, clazz);
    }

    /** called from a separate thread */
    @Override
    public void init(EventHandlerMasterSettings st) {
        setSettings(st);
    }

    /** called from a separate thread */
    @Override
    public void run() {
        closed = false;
        ended = false;
    }

    /** called from the JobScheduler thread */
    @Override
    public void close() {
        closed = true;
        if (getRestApiClient() != null) {
            getRestApiClient().closeHttpClient();
        }
    }

    /** called from the JobScheduler thread */
    @Override
    public void awaitEnd() {
        if (wait) {
            return;
        }

        int counter = 0;
        int limit = waitIntervalOnEnd * 2;
        while (!ended) {
            if (counter > limit) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (Throwable e) {
                break;
            }
            counter++;
        }
    }

    public void start(Long eventId) {
        String method = "";
        if (isDebugEnabled) {
            method = getMethodName("start");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        while (!closed) {
            try {
                eventId = process(eventId);
            } catch (Throwable ex) {
                if (closed) {
                    LOGGER.info(String.format("%s processing stopped.", method));
                } else {
                    LOGGER.error(String.format("%s exception: %s", method, ex.toString()), ex);
                    closeRestApiClient();
                    if (tornEventId != null) {
                        eventId = tornEventId;
                    }
                    wait(waitIntervalOnError);
                }
            }
        }
        onEnded();
        ended = true;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s end", method));
        }
    }

    public void onEnded() {
        closeRestApiClient();
    }

    public void onEmptyEvent(Long eventId) {
        if (isDebugEnabled) {
            String method = getMethodName("onEmptyEvent");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
    }

    public Long onNonEmptyEvent(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onNonEmptyEvent");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        return event.getStampeds().get(event.getStampeds().size() - 1).getEventId();
    }

    public void onTornEvent(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onTornEvent");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
    }

    public void onRestart(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onRestart");
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
    }

    private Long process(Long eventId) throws Exception {
        String method = getMethodName("process");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        }
        tryCreateRestApiClient();

        Event event = getEvents(eventId);
        Long newEventId = null;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s type=%s, closed=%s", method, event.getType(), closed));
        }
        if (event.getType().equals(EventSeq.NonEmpty)) {
            tornEventId = null;
            newEventId = onNonEmptyEvent(eventId, event);
        } else if (event.getType().equals(EventSeq.Empty)) {
            tornEventId = null;
            newEventId = event.getLastEventId();
            onEmptyEvent(eventId);
            wait(waitIntervalOnEmptyEvent);
        } else if (event.getType().equals(EventSeq.Torn)) {
            tornEventId = event.getLastEventId();
            newEventId = event.getLastEventId();
            onTornEvent(eventId, event);
            throw new Exception(String.format("%s Torn event occured. Try to retry events ...", method));
        } else {
            throw new Exception(String.format("%s unknown event type=%s", method, event.getType()));
        }
        return newEventId;
    }

    private void tryCreateRestApiClient() {
        if (getRestApiClient() == null) {
            createRestApiClient();
        }
    }

    public void wait(int interval) {
        wait = false;
        if (!closed && interval > 0) {
            String method = getMethodName("wait");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s waiting %sms ...", method, interval));
            }
            try {
                wait = true;
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s sleep interrupted due handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s %s", method, e.toString()), e);
                }
            } finally {
                wait = false;
            }
        }
    }

    public void setSettings(EventHandlerMasterSettings st) {
        settings = st;
        setBaseUrl(st.getHttpHost(), settings.getHttpPort());
    }

    public EventHandlerMasterSettings getSettings() {
        return settings;
    }

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public void setWaitIntervalOnError(int val) {
        waitIntervalOnError = val;
    }

    public int getWaitIntervalOnEmptyEvent() {
        return waitIntervalOnEmptyEvent;
    }

    public void setWaitIntervalOnEmptyEvent(int val) {
        waitIntervalOnEmptyEvent = val;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isWait() {
        return wait;
    }
}