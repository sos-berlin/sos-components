package com.sos.jobscheduler.event.master;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventPath;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventSeq;

public class JobSchedulerUnlimitedEventHandler extends JobSchedulerEventHandler implements IJobSchedulerUnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerUnlimitedEventHandler.class);

    private EventHandlerMasterSettings settings;

    private boolean closed = false;
    private boolean ended = false;
    private Long tornEventId = null;
    private EventPath eventPath = null;

    /* all intervals in milliseconds */
    private int waitIntervalOnError = 30_000;
    private int waitIntervalOnEnd = 30_000;
    private int waitIntervalOnEmptyEvent = 1_000;
    private boolean wait = false;

    public JobSchedulerUnlimitedEventHandler() {
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

    public void start(EventPath eventPath, Long eventId) {
        String method = getMethodName("start");

        this.eventPath = eventPath;

        LOGGER.debug(String.format("%s eventPath=%s, eventId=%s", method, eventPath, eventId));

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
        LOGGER.debug(String.format("%s end", method));
    }

    public void onEnded() {
        closeRestApiClient();
    }

    public void onEmptyEvent(Long eventId) {
        String method = getMethodName("onEmptyEvent");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));
    }

    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = getMethodName("onNonEmptyEvent");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));
        
        /**
        try {
            int sleep = 10;
            LOGGER.debug(String.format("%s !!!!!! eventId=%s sleep %s s", method, eventId, sleep));
            Thread.sleep(sleep*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public void onTornEvent(Long eventId, JsonArray events) {
        String method = getMethodName("onTornEvent");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));
    }

    public void onRestart(Long eventId, JsonArray events) {
        String method = getMethodName("onRestart");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));
    }

    private Long process(Long eventId) throws Exception {
        String method = getMethodName("process");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));

        tryCreateRestApiClient();

        JobSchedulerEvent em = getEvents(eventPath, eventId);
        LOGGER.debug(String.format("%s type=%s, closed=%s", method, em.getEventSeq(), closed));
        if (em.getEventSeq().equals(EventSeq.NonEmpty)) {
            tornEventId = null;
            onNonEmptyEvent(em.getLastEventId(), em.getStampeds());
        } else if (em.getEventSeq().equals(EventSeq.Empty)) {
            tornEventId = null;
            onEmptyEvent(em.getLastEventId());
            wait(waitIntervalOnEmptyEvent);
        } else if (em.getEventSeq().equals(EventSeq.Torn)) {
            tornEventId = em.getLastEventId();
            onTornEvent(em.getLastEventId(), em.getStampeds());
            throw new Exception(String.format("%s Torn event occured. Try to retry events ...", method));
        } else {
            throw new Exception(String.format("%s unknown event type=%s", method, em.getEventSeq()));
        }
        return em.getLastEventId();
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
            LOGGER.debug(String.format("%s waiting %sms ...", method, interval));
            try {
                wait = true;
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                if (closed) {
                    LOGGER.debug(String.format("%s sleep interrupted due handler close", method));
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