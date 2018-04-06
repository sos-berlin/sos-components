package com.sos.jobscheduler.master.event;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.sos.jobscheduler.master.event.JobSchedulerEvent.EventOverview;
import com.sos.jobscheduler.master.event.JobSchedulerEvent.EventPath;
import com.sos.jobscheduler.master.event.JobSchedulerEvent.EventSeq;
import com.sos.jobscheduler.master.event.JobSchedulerEvent.EventType;

public class JobSchedulerUnlimitedEventHandler extends JobSchedulerEventHandler implements IJobSchedulerUnlimitedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerUnlimitedEventHandler.class);

    private EventHandlerMasterSettings settings;

    private boolean closed = false;
    private boolean ended = false;
    private EventOverview eventOverview;
    private EventType[] eventTypes;
    private String eventTypesJoined;
    private Long tornEventId = null;

    private String bodyParamPathForEventId = "/not_exists/";
    /* all intervals in seconds */
    private int waitIntervalOnError = 30;
    private int waitIntervalOnEnd = 30;
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

    public void start() {
        start(null, null);
    }

    public void start(EventType[] et) {
        start(et, null);
    }

    public void start(EventType[] et, EventOverview ov) {
        String method = getMethodName("start");

        if (ov == null && (et == null || et.length == 0)) {
            ov = EventOverview.FileBasedOverview;
        } else if (ov == null && et != null && et.length > 0) {
            ov = getEventOverviewByEventTypes(et);
        }
        eventOverview = ov;
        eventTypes = et;
        eventTypesJoined = joinEventTypes(eventTypes);

        LOGGER.debug(String.format("%s eventOverview=%s, eventTypes=%s", method, eventOverview, eventTypesJoined));

        EventPath path = getEventPathByEventOverview(eventOverview);
        Long eventId = null;
        try {
            eventId = getEventId(path, eventOverview, bodyParamPathForEventId);
        } catch (Exception e) {
            eventId = rerunGetEventId(method, e, path, eventOverview, bodyParamPathForEventId);
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
    }

    public void onTornEvent(Long eventId, JsonArray events) {
        String method = getMethodName("onTornEvent");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));
    }

    public void onRestart(Long eventId, JsonArray events) {
        String method = getMethodName("onRestart");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));
    }

    private Long getEventId(EventPath path, EventOverview overview, String bodyParamPath) throws Exception {
        String method = getMethodName("getEventId");

        tryCreateRestApiClient();

        LOGGER.debug(String.format("%s eventPath=%s, eventOverview=%s, bodyParamPath=%s", method, path, overview, bodyParamPath));
        JsonObject result = getOverview(path, overview, bodyParamPath);

        return getEventId(result);
    }

    private Long rerunGetEventId(String callerMethod, Exception ex, EventPath path, EventOverview overview, String bodyParamPath) {
        String method = getMethodName("rerunGetEventId");

        if (closed) {
            LOGGER.info(String.format("%s processing stopped.", method));
            return null;
        }
        if (ex != null) {
            LOGGER.error(String.format("%s error on %s: %s", method, callerMethod, ex.toString()), ex);
            if (ex instanceof UncheckedTimeoutException) {
                LOGGER.debug(String.format("%s close httpClient due method execution timeout (%sms). see details above ...", method,
                        getMethodExecutionTimeout()));
            } else {
                LOGGER.debug(String.format("%s close httpClient due exeption. see details above ...", method));
            }
            closeRestApiClient();
        }
        LOGGER.debug(String.format("%s", method));

        wait(waitIntervalOnError);

        if (closed) {
            LOGGER.info(String.format("%s processing stopped.", method));
            return null;
        }

        Long eventId = null;
        try {
            eventId = getEventId(path, overview, bodyParamPath);
        } catch (Exception e) {
            eventId = rerunGetEventId(method, e, path, overview, bodyParamPath);
        }
        return eventId;
    }

    private Long process(Long eventId) throws Exception {
        String method = getMethodName("process");
        LOGGER.debug(String.format("%s eventId=%s", method, eventId));

        tryCreateRestApiClient();

        JsonObject result = getEvents(eventId, this.eventTypesJoined);
        Long newEventId = getEventId(result);
        String type = getEventType(result);
        JsonArray events = getEventSnapshots(result);

        LOGGER.debug(String.format("%s newEventId=%s, type=%s, closed=%s", method, newEventId, type, closed));

        if (type.equalsIgnoreCase(EventSeq.NonEmpty.name())) {
            tornEventId = null;
            onNonEmptyEvent(newEventId, events);
        } else if (type.equalsIgnoreCase(EventSeq.Empty.name())) {
            tornEventId = null;
            onEmptyEvent(newEventId);
        } else if (type.equalsIgnoreCase(EventSeq.Torn.name())) {
            tornEventId = newEventId;
            onTornEvent(newEventId, events);
            throw new Exception(String.format("%s Torn event occured. Try to retry events ...", method));
        } else {
            throw new Exception(String.format("%s unknown event type=%s", method, type));
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
            LOGGER.debug(String.format("%s waiting %ss ...", method, interval));
            try {
                wait = true;
                Thread.sleep(interval * 1000);
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

    public String getBodyParamPathForEventId() {
        return bodyParamPathForEventId;
    }

    public void setBodyParamPathForEventId(String val) {
        bodyParamPathForEventId = val;
    }

    public EventOverview getEventOverview() {
        return eventOverview;
    }

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public void setWaitIntervalOnError(int val) {
        waitIntervalOnError = val;
    }

    public String getEventTypesJoined() {
        return eventTypesJoined;
    }

    public EventType[] getEventTypes() {
        return eventTypes;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isWait() {
        return wait;
    }
}