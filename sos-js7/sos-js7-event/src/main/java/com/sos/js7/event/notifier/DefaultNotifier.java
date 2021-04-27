package com.sos.js7.event.notifier;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;

public class DefaultNotifier implements INotifier {

    public static final String NEW_LINE = "\r\n";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNotifier.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private ErrorNotifier errorNotifier = new ErrorNotifier();
    private int notifyInterval = 5; // in minutes for the reoccurred exceptions
    private boolean notifyFirstErrorAsWarning;

    @Override
    public void notifyOnError(String msg, Throwable t) {
        notifyOnError(null, msg, t);
    }

    @Override
    public void notifyOnError(String title, String msg, Throwable t) {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            sb.append("[").append(title).append("]");
        }
        if (t == null) {
            sb.append(msg);
        } else {
            sb.append("[").append(msg).append("]").append(t.toString());
        }
        LOGGER.error(sb.toString());
    }

    @Override
    public void notifyOnWarning(String msg, Throwable t) {
        notifyOnWarning(null, msg, t);
    }

    @Override
    public void notifyOnWarning(String title, String msg, Throwable t) {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            sb.append("[").append(title).append("]");
        }
        if (t == null) {
            sb.append(msg);
        } else {
            sb.append("[").append(msg).append("]").append(t.toString());
        }
        LOGGER.warn(sb.toString());
    }

    @Override
    public void notifyOnRecovery(String title, String msg) {
        LOGGER.info(String.format("[%s]%s", title, msg));
    }

    @Override
    public void notifyOnRecovery(String title, Throwable ex) {
        notifyOnRecovery(title, ex == null ? null : SOSString.toString(ex));
    }

    public boolean smartNotifyOnError(Class<?> clazz, Throwable e) {
        return smartNotifyOnError(clazz, null, e);
    }

    public boolean smartNotifyOnError(Class<?> clazz, String bodyPart, Throwable e) {
        return smartNotifyOnError(clazz, bodyPart, e, notifyInterval);
    }

    public boolean smartNotifyOnError(Class<?> clazz, String bodyPart, Throwable e, int notifyInterval) {
        if (errorNotifier.getException() != null && e != null) {
            if (!errorNotifier.getException().getClass().equals(e.getClass())) {
                reset();
            }
        }

        errorNotifier.addCounter();

        if (errorNotifier.calculate()) {
            if (notifyFirstErrorAsWarning && errorNotifier.getCounter() == 1) {
                notifyOnWarning(clazz.getSimpleName(), bodyPart, e);
            } else {
                if (errorNotifier.getCounter() > 1) {
                    String msg = "This error has now occurs " + errorNotifier.getCounter() + " times.";
                    if (SOSString.isEmpty(bodyPart)) {
                        bodyPart = msg;
                    } else {
                        StringBuilder sb = new StringBuilder(msg);
                        sb.append(String.format("%s", NEW_LINE));
                        sb.append(bodyPart);
                        bodyPart = sb.toString();
                    }
                }
                notifyOnError(clazz.getSimpleName(), bodyPart, e);
            }
            errorNotifier.setException(e);
            errorNotifier.setCaller(clazz);
            return true;
        } else {
            LOGGER.error(String.format("[%s][reoccurred][%s]%s", clazz.getSimpleName(), errorNotifier.getCounter(), e.toString()));
        }
        return false;
    }

    public boolean smartNotifyOnRecovery() {
        if (errorNotifier.getCounter() > 0) {
            notifyOnRecovery(errorNotifier.getCaller().getSimpleName(), errorNotifier.getException());
            reset();
            return true;
        }
        return false;
    }

    private void reset() {
        errorNotifier = new ErrorNotifier();
    }

    private class ErrorNotifier {

        private long counter = 0;
        private Long last = new Long(0);
        private Throwable exception;
        private Class<?> caller;

        public void addCounter() {
            counter++;
        }

        public long getCounter() {
            return counter;
        }

        public boolean calculate() {
            Long current = SOSDate.getMinutes(new Date());
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][notifyInterval=%sm][diff=%sm][current=%sm, last=%sm]", caller, notifyInterval, (current - last),
                        current, last));
            }
            if ((current - last) >= notifyInterval) {
                last = current;
                return true;
            }
            return false;
        }

        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable val) {
            exception = val;
        }

        public void setCaller(Class<?> val) {
            caller = val;
        }

        public Class<?> getCaller() {
            return caller;
        }
    }
}
