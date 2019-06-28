package com.sos.jobscheduler.event.master.handler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.exception.SOSForbiddenException;
import com.sos.commons.httpclient.exception.SOSTooManyRequestsException;
import com.sos.commons.httpclient.exception.SOSUnauthorizedException;
import com.sos.commons.util.SOSDate;
import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.EventMeta.EventSeq;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.handler.notifier.DefaultNotifier;
import com.sos.jobscheduler.event.master.handler.notifier.INotifier;

public class LoopEventHandler extends EventHandler implements ILoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private EventHandlerMasterSettings settings;
    private INotifier notifier;

    private boolean closed = false;
    private boolean ended = false;

    /* all intervals in milliseconds */
    private int waitIntervalOnConnectionRefused = 30_000;
    private int waitIntervalOnMasterSwitch = 2_000;
    private int waitIntervalOnTooManyRequests = 2_000;
    private int waitIntervalOnError = 2_000;
    private int waitIntervalOnEmptyEvent = 1_000;
    private int waitIntervalOnNonEmptyEvent = 0;
    private int waitIntervalOnTornEvent = 1_000;
    private int maxWaitIntervalOnEnd = 30_000;
    private int minExecutionTimeOnNonEmptyEvent = 10; // to avoid master 429 TooManyRequestsException

    /* in minutes */
    private int notifyIntervalOnConnectionRefused = 15;
    private Long lastConnectionRefusedNotifier;

    private boolean wait = false;
    private String token;

    public LoopEventHandler(EventPath path, Class<? extends IEntry> clazz, INotifier n) {
        super(path, clazz);
        notifier = n;
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
            // logout();
            closeRestApiClient();
        }
    }

    /** called from the JobScheduler thread */
    @Override
    public void awaitEnd() {
        if (wait) {
            return;
        }

        int counter = 0;
        int limit = maxWaitIntervalOnEnd / 1000 * 2;// seconds*2 due sleep(500)
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
        String method = getMethodName("start");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        String token = doLogin();
        while (!closed) {
            try {
                eventId = process(eventId, token);
            } catch (Throwable ex) {
                if (closed) {
                    LOGGER.info(String.format("%s[closed][exception ignored]%s", method, ex.toString()), ex);
                } else {
                    closeRestApiClient();

                    int waitInterval = waitIntervalOnError;
                    boolean doLogin = false;

                    if (ex instanceof SOSTooManyRequestsException) {
                        LOGGER.warn(String.format("%s[exception]%s", method, ex.toString()), ex);
                        if (notifier != null) {
                            notifier.notifyOnWarning(method, ex);
                        }
                        waitInterval = waitIntervalOnTooManyRequests;
                    } else {
                        LOGGER.error(String.format("%s[exception]%s", method, ex.toString()), ex);

                        Exception cre = EventHandler.findConnectionRefusedException(ex);
                        if (cre == null) {
                            if (notifier != null) {
                                notifier.notifyOnError(method, ex);
                            }
                            if (ex instanceof SOSUnauthorizedException || ex instanceof SOSForbiddenException) {
                                doLogin = true;
                            }
                        } else {
                            doLogin = true;

                            if (tryChangeMaster()) {
                                waitInterval = waitIntervalOnMasterSwitch;
                            } else {
                                waitInterval = waitIntervalOnConnectionRefused;
                            }
                        }
                    }

                    wait(waitInterval);
                    if (doLogin) {
                        token = doLogin();
                    }
                }
            }
        }
        onEnded();
        ended = true;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%send", method));
        }
    }

    public void onEnded() {
        // closeRestApiClient();
    }

    public Long onEmptyEvent(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onEmptyEvent");
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        return event.getLastEventId();
    }

    public Long onNonEmptyEvent(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onNonEmptyEvent");
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        return event.getStamped().get(event.getStamped().size() - 1).getEventId();
    }

    public Long onTornEvent(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onTornEvent");
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        return event.getAfter();
    }

    public void onRestart(Long eventId, Event event) {
        if (isDebugEnabled) {
            String method = getMethodName("onRestart");
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
    }

    private Long process(Long eventId, String token) throws Exception {
        String method = getMethodName("process");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        tryCreateRestApiClient();

        Event event = getAfterEvent(eventId, token);
        Long newEventId = null;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%stype=%s, closed=%s", method, event.getType(), closed));
        }
        if (event.getType().equals(EventSeq.NonEmpty)) {
            long start = System.currentTimeMillis();

            newEventId = onNonEmptyEvent(eventId, event);
            wait(waitIntervalOnNonEmptyEvent);

            if (minExecutionTimeOnNonEmptyEvent > 0) {
                Long diff = System.currentTimeMillis() - start;
                if (diff < minExecutionTimeOnNonEmptyEvent) {
                    wait(minExecutionTimeOnNonEmptyEvent - diff.intValue());
                }
            }
        } else if (event.getType().equals(EventSeq.Empty)) {
            newEventId = onEmptyEvent(eventId, event);
            wait(waitIntervalOnEmptyEvent);
        } else if (event.getType().equals(EventSeq.Torn)) {
            newEventId = onTornEvent(eventId, event);
            if (isDebugEnabled) {
                String msg = "";
                if (waitIntervalOnTornEvent > 0 && !closed) {
                    msg = String.format("waiting %sms ...", waitIntervalOnTornEvent);
                }
                LOGGER.debug(String.format("%s[TORN][%s][%s]%s", method, eventId, newEventId, msg));
            }
            wait(waitIntervalOnTornEvent);
        } else {
            throw new Exception(String.format("%sunknown event type=%s", method, event.getType()));
        }
        return newEventId;
    }

    private void tryCreateRestApiClient() {
        if (getRestApiClient() == null) {
            createRestApiClient();
        }
    }

    private boolean tryChangeMaster() {
        if (getSettings().getBackup() != null) {

            MasterSettings previousMaster = getSettings().getCurrent();

            if (getSettings().getCurrent().isPrimary()) {
                getSettings().setCurrent(getSettings().getBackup());
            } else {
                getSettings().setCurrent(getSettings().getPrimary());
            }
            setSettings(getSettings());

            LOGGER.info(String.format("[master switched][current %s][previous %s]", getSettings().getCurrent(), previousMaster));
            return true;
        }
        return false;
    }

    private String doLogin() {
        if (closed) {
            return null;
        }
        String method = getMethodName("doLogin");
        int count = 0;
        boolean run = true;
        token = null;
        while (!closed && run) {
            count++;
            try {
                tryCreateRestApiClient();
                token = login(getSettings().getCurrent().getUser(), getSettings().getCurrent().getPassword());
                run = false;

                sendConnectionRefusedNotifierOnSuccess();
            } catch (Exception e) {
                closeRestApiClient();
                LOGGER.error(String.format("%s[%s]%s", method, count, e.toString()), e);

                Exception cre = EventHandler.findConnectionRefusedException(e);
                if (cre == null) {
                    if (notifier != null) {
                        notifier.notifyOnError(String.format("%s[%s]", method, count), e);
                    }
                    wait(waitIntervalOnError);
                } else {
                    sendConnectionRefusedNotifierOnError(String.format("%s[%s]", method, count), e);
                    int waitInterval = waitIntervalOnConnectionRefused;
                    if (tryChangeMaster()) {
                        waitInterval = waitIntervalOnMasterSwitch;
                    }
                    wait(waitInterval);
                }
            }
        }
        return token;
    }

    private void sendConnectionRefusedNotifierOnError(String msg, Throwable e) {
        if (lastConnectionRefusedNotifier == null) {
            lastConnectionRefusedNotifier = new Long(0);
        }
        Long currentMinutes = SOSDate.getMinutes(new Date());
        if ((currentMinutes - lastConnectionRefusedNotifier) >= getSettings().getNotifyIntervalOnConnectionRefused()) {
            getNotifier().notifyOnError(msg, e);
            lastConnectionRefusedNotifier = currentMinutes;
        }
    }

    private void sendConnectionRefusedNotifierOnSuccess() {
        if (lastConnectionRefusedNotifier != null) {
            getNotifier().notifyOnRecovery("SOSConnectionRefusedException", null);
        }
        lastConnectionRefusedNotifier = null;
    }

    public void wait(int interval) {
        wait = false;
        if (!closed && interval > 0) {
            String method = getMethodName("wait");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%swaiting %sms ...", method, interval));
            }
            try {
                wait = true;
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due handler close", method));
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
        try {
            setBaseUri(st.getCurrent().getHostname(), settings.getCurrent().getPort());
            useLogin(st.getCurrent().useLogin());
        } catch (Throwable t) {
            LOGGER.error(t.toString(), t);
            closed = true;
        }
    }

    public EventHandlerMasterSettings getSettings() {
        return settings;
    }

    public int getWaitIntervalOnConnectionRefused() {
        return waitIntervalOnConnectionRefused;
    }

    public void setWaitIntervalOnConnectionRefused(int val) {
        waitIntervalOnConnectionRefused = val;
    }

    public int getWaitIntervalOnMasterSwitch() {
        return waitIntervalOnMasterSwitch;
    }

    public void setWaitIntervalOnMasterSwitch(int val) {
        waitIntervalOnMasterSwitch = val;
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

    public int getWaitIntervalOnNonEmptyEvent() {
        return waitIntervalOnNonEmptyEvent;
    }

    public void setWaitIntervalOnNonEmptyEvent(int val) {
        waitIntervalOnNonEmptyEvent = val;
    }

    public int getWaitIntervalOnTornEvent() {
        return waitIntervalOnTornEvent;
    }

    public void setWaitIntervalOnTornEvent(int val) {
        waitIntervalOnTornEvent = val;
    }

    public int geMaxtWaitIntervalOnEnd() {
        return maxWaitIntervalOnEnd;
    }

    public void setMaxWaitIntervalOnEnd(int val) {
        maxWaitIntervalOnEnd = val;
    }

    public int getMinExecutionTimeOnNonEmptyEvent() {
        return minExecutionTimeOnNonEmptyEvent;
    }

    public void setMinExecutionTimeOnNonEmptyEvent(int val) {
        minExecutionTimeOnNonEmptyEvent = val;
    }

    public int getWaitIntervalOnTooManyRequests() {
        return waitIntervalOnTooManyRequests;
    }

    public void setWaitIntervalOnTooManyRequests(int val) {
        waitIntervalOnTooManyRequests = val;
    }

    public int getNotifyIntervalOnConnectionRefused() {
        return notifyIntervalOnConnectionRefused;
    }

    public void setNotifyIntervalOnConnectionRefused(int val) {
        notifyIntervalOnConnectionRefused = val;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isWait() {
        return wait;
    }

    public INotifier getNotifier() {
        return notifier == null ? new DefaultNotifier() : notifier;
    }

    public String getToken() {
        return token;
    }
}