package com.sos.js7.event.controller.handler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.exception.SOSForbiddenException;
import com.sos.commons.httpclient.exception.SOSTooManyRequestsException;
import com.sos.commons.httpclient.exception.SOSUnauthorizedException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.js7.event.controller.EventMeta.EventPath;
import com.sos.js7.event.controller.EventMeta.EventSeq;
import com.sos.js7.event.controller.bean.Event;
import com.sos.js7.event.controller.bean.IEntry;
import com.sos.js7.event.controller.cluster.bean.ClusterEvent;
import com.sos.js7.event.controller.configuration.Configuration;
import com.sos.js7.event.controller.configuration.controller.Controller;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.js7.event.http.HttpClient;
import com.sos.js7.event.notifier.DefaultNotifier;
import com.sos.js7.event.notifier.INotifier;

public class LoopEventHandler extends EventHandler implements ILoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private INotifier notifier;
    private ControllerConfiguration controllerConfig;
    private String token;
    private boolean closed = false;

    /* in minutes */
    private int notifyIntervalOnConnectionRefused = 15;
    private Long lastConnectionRefusedNotifier;

    /* in milliseconds */
    private int minExecutionTimeOnNonEmptyEvent = 10; // to avoid controller 429 TooManyRequestsException
    private int tooManyRequestsExceptionCounter = 0;

    public LoopEventHandler(Configuration configuration, EventPath path, Class<? extends IEntry> clazz, INotifier n) {
        super(configuration, path, clazz);
        notifier = n;
    }

    @Override
    public void init(ControllerConfiguration conf) {
        setControllerConfig(conf);
    }

    @Override
    public void run() {
        closed = false;
    }

    @Override
    public void close() {
        closed = true;
        getHttpClient().close();

        synchronized (getHttpClient()) {
            getHttpClient().notifyAll();
        }
    }

    public void start(Long eventId) {
        String method = getMethodName("start");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        doLogin(true);
        setCurrentController();
        onProcessingStart(eventId);
        eventId = doProcessing(eventId);
        onProcessingEnd(eventId);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[end]%s", method, eventId));
        }
    }

    public void onProcessingStart(Long eventId) {
    }

    private Long doProcessing(Long eventId) {
        String method = getMethodName("doProcessing");
        while (!closed) {
            try {
                eventId = process(eventId, token);
                tooManyRequestsExceptionCounter = 0;
            } catch (Throwable ex) {
                if (closed) {
                    LOGGER.info(String.format("%s[closed][exception ignored]%s", method, ex.toString()));
                } else {
                    getHttpClient().close();
                    int waitInterval = getConfig().getHandler().getWaitIntervalOnError();
                    boolean doLogin = false;
                    if (ex instanceof SOSTooManyRequestsException) {
                        tooManyRequestsExceptionCounter++;
                        LOGGER.warn(String.format("%s[%s][exception]%s", method, tooManyRequestsExceptionCounter, ex.toString()));
                        if (notifier != null) {
                            notifier.notifyOnWarning(method, ex);
                        }
                        waitInterval = getConfig().getHandler().getWaitIntervalOnTooManyRequests();
                        if (tooManyRequestsExceptionCounter >= 5) {// TODO
                            LOGGER.warn(String.format("%s wait 1m due to SOSTooManyRequestsException exception ...", method));
                            waitInterval = 60;
                        }
                    } else {
                        Exception cre = HttpClient.findConnectionResetRefusedException(ex);
                        if (cre == null) {
                            LOGGER.error(String.format("%s[exception]%s", method, ex.toString()), ex);
                            if (notifier != null) {
                                notifier.notifyOnError(method, ex);
                            }
                            if (ex instanceof SOSUnauthorizedException || ex instanceof SOSForbiddenException) {
                                doLogin = true;
                            }
                        } else {
                            LOGGER.error(String.format("%s[exception]%s", method, ex.toString()));
                            doLogin = true;
                            waitInterval = getConfig().getHandler().getWaitIntervalOnConnectionRefused();
                        }
                    }
                    wait(waitInterval);
                    if (doLogin) {
                        doLogin(true);
                    }
                    setCurrentController();
                }
            }
        }
        return eventId;
    }

    public void onProcessingEnd(Long eventId) {
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
        getHttpClient().tryCreate(getConfig().getHttpClient());

        Event event = getAfterEvent(eventId, token);
        Long newEventId = null;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%stype=%s, closed=%s", method, event.getType(), closed));
        }
        if (event.getType().equals(EventSeq.NonEmpty)) {
            long start = System.currentTimeMillis();

            newEventId = onNonEmptyEvent(eventId, event);
            wait(getConfig().getHandler().getWaitIntervalOnNonEmptyEvent());

            if (minExecutionTimeOnNonEmptyEvent > 0) {
                Long diff = System.currentTimeMillis() - start;
                if (diff < minExecutionTimeOnNonEmptyEvent) {
                    wait((minExecutionTimeOnNonEmptyEvent - diff.intValue()) / 1_000);
                }
            }
        } else if (event.getType().equals(EventSeq.Empty)) {
            newEventId = onEmptyEvent(eventId, event);
            wait(getConfig().getHandler().getWaitIntervalOnEmptyEvent());
        } else if (event.getType().equals(EventSeq.Torn)) {
            newEventId = onTornEvent(eventId, event);
            if (isDebugEnabled) {
                String msg = "";
                if (getConfig().getHandler().getWaitIntervalOnTornEvent() > 0 && !closed) {
                    msg = String.format("waiting %sms ...", getConfig().getHandler().getWaitIntervalOnTornEvent());
                }
                LOGGER.debug(String.format("%s[TORN][%s][%s]%s", method, eventId, newEventId, msg));
            }
            wait(getConfig().getHandler().getWaitIntervalOnTornEvent());
        } else {
            throw new Exception(String.format("%sunknown event type=%s", method, event.getType()));
        }
        return newEventId;
    }

    private void doLogin(boolean switchOnError) {
        if (closed) {
            return;
        }
        String method = getMethodName("doLogin");
        int count = 0;
        boolean run = true;
        token = null;
        while (!closed && run) {
            count++;
            try {
                setIdentifier(controllerConfig.getCurrent().getType());
                method = getMethodName("doLogin");

                LOGGER.info(String.format("[%s][doLogin][%s]%s", getIdentifier(), controllerConfig.getCurrent().getUri(), useLogin()
                        ? controllerConfig.getCurrent().getUser() : "public"));
                getHttpClient().tryCreate(getConfig().getHttpClient());
                token = login(controllerConfig.getCurrent().getUser(), controllerConfig.getCurrent().getPassword());
                run = false;

                sendConnectionRefusedNotifierOnSuccess();
            } catch (Exception e) {
                getHttpClient().close();
                Exception cre = HttpClient.findConnectionResetRefusedException(e);
                if (cre == null) {
                    LOGGER.error(String.format("%s[%s]%s", method, count, e.toString()), e);
                    if (notifier != null) {
                        notifier.notifyOnError(String.format("%s[%s]", method, count), e);
                    }
                    wait(getConfig().getHandler().getWaitIntervalOnError());
                } else {
                    LOGGER.error(String.format("%s[%s]%s", method, count, e.toString()));
                    sendConnectionRefusedNotifierOnError(String.format("%s[%s]", method, count), e);
                    wait(getConfig().getHandler().getWaitIntervalOnConnectionRefused());
                    if (controllerConfig.getBackup() != null && switchOnError) {
                        controllerConfig.switchCurrent();
                        try {
                            setUri(controllerConfig.getCurrent().getUri());
                        } catch (Exception e1) {
                            LOGGER.error(e.toString(), e);
                        }
                        // setIdentifier(controllerConfig.getCurrent().getType());
                        LOGGER.info(String.format("[%s][doLogin][switched]%s", getIdentifier(), controllerConfig.getCurrent().getUri()));
                    }
                }
            }
        }
    }

    private void sendConnectionRefusedNotifierOnError(String msg, Throwable e) {
        if (lastConnectionRefusedNotifier == null) {
            lastConnectionRefusedNotifier = new Long(0);
        }
        Long currentMinutes = SOSDate.getMinutes(new Date());
        if ((currentMinutes - lastConnectionRefusedNotifier) >= getConfig().getHandler().getNotifyIntervalOnConnectionRefused()) {
            getNotifier().notifyOnError(msg, e);
            lastConnectionRefusedNotifier = currentMinutes;
        }
    }

    private void sendConnectionRefusedNotifierOnSuccess() {
        if (lastConnectionRefusedNotifier != null) {
            getNotifier().notifyOnRecovery("SOSConnectionRefusedException", "");
        }
        lastConnectionRefusedNotifier = null;
    }

    public boolean setCurrentController() {
        if (controllerConfig.getBackup() != null) {
            EventHandler handler = new EventHandler(getConfig());
            try {
                handler.setUri(controllerConfig.getCurrent().getUri());
                handler.getHttpClient().create(getConfig().getHttpClient());
                ClusterEvent event = handler.getEvent(ClusterEvent.class, EventPath.cluster, getToken());
                if (!SOSString.isEmpty(event.getActiveId()) && event.getActiveClusterUri() != null) {
                    // if (LOGGER.isTraceEnabled()) {
                    // LOGGER.trace(SOSString.toString(event));
                    // }
                    LOGGER.info(SOSString.toString(event));
                    String activeClusterUri = event.getActiveClusterUri();
                    if (activeClusterUri.equals(controllerConfig.getCurrent().getClusterUri())) {
                        setIdentifier(event.getActiveId());
                        LOGGER.info(String.format("[%s][current]%s", getIdentifier(), controllerConfig.getCurrent().getUri4Log()));
                    } else {
                        Controller notCurrent = controllerConfig.getNotCurrent();
                        if (activeClusterUri.equals(notCurrent.getClusterUri())) {
                            String previousUri4Log = controllerConfig.getCurrent().getUri4Log();
                            controllerConfig.setCurrent(notCurrent);
                            setUri(controllerConfig.getCurrent().getUri());

                            setIdentifier(event.getNotActiveId());
                            LOGGER.info(String.format("[%s][switched][current %s %s][previous %s]", getIdentifier(), event.getActiveId(),
                                    controllerConfig.getCurrent().getUri4Log(), previousUri4Log));
                            setIdentifier(event.getActiveId());

                            doLogin(false);
                            return true;
                        } else {
                            LOGGER.error(String.format("[%s][controller switch]can't identify controller to switch", getIdentifier()));
                            LOGGER.error(String.format("[%s][controller switch][controller answer][active=%s]%s", getIdentifier(), event
                                    .getActiveId(), event.getIdToUri()));
                            LOGGER.error(String.format("[%s][controller switch][configured controllers][primary=%s][backup=%s]", getIdentifier(),
                                    controllerConfig.getPrimary().getUri4Log(), controllerConfig.getBackup().getUri4Log()));
                        }
                    }
                    controllerConfig.setClusterControllers(controllerConfig.getCurrent(), event.getActiveId().equalsIgnoreCase("primary"));
                } else {
                    LOGGER.warn(String.format("[%s][not switched][current %s]%s", getIdentifier(), controllerConfig.getCurrent().getUri4Log(),
                            SOSString.toString(event)));
                }
            } catch (Exception ex) {
                LOGGER.error(ex.toString(), ex);
            } finally {
                handler.getHttpClient().close();
            }
        }

        return false;

    }

    @Override
    public void setIdentifier(String type) {
        String identifier = controllerConfig.getCurrent().getJobSchedulerId();
        if (controllerConfig.getBackup() != null) {
            identifier = "cluster][" + identifier;
            if (!SOSString.isEmpty(type)) {
                identifier = identifier + "][" + type;
            }
        }
        super.setIdentifier(identifier);
        onSetIdentifier();
    }

    @Override
    public void onSetIdentifier() {

    }

    public void wait(int interval) {
        if (!closed && interval > 0) {
            String method = getMethodName("wait");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%ss ...", method, interval));
            }
            try {
                // Thread.sleep(interval * 1_000);
                synchronized (getHttpClient()) {
                    getHttpClient().wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due to handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

    @Override
    public void setControllerConfig(ControllerConfiguration conf) {
        controllerConfig = conf;
        try {
            setUri(controllerConfig.getCurrent().getUri());
            useLogin(controllerConfig.getCurrent().useLogin());
        } catch (Throwable t) {
            LOGGER.error(t.toString(), t);
            closed = true;
        }
    }

    @Override
    public ControllerConfiguration getControllerConfig() {
        return controllerConfig;
    }

    public int getMinExecutionTimeOnNonEmptyEvent() {
        return minExecutionTimeOnNonEmptyEvent;
    }

    public void setMinExecutionTimeOnNonEmptyEvent(int val) {
        minExecutionTimeOnNonEmptyEvent = val;
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

    public INotifier getNotifier() {
        return notifier == null ? new DefaultNotifier() : notifier;
    }

    public String getToken() {
        return token;
    }
}