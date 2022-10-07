package com.sos.auth.classes;

import java.util.Map.Entry;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.joc.Globals;

public class SOSAuthLockerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthLockerHandler.class);
    private static final String ThreadCtx = "authentication";
    private static final Long lifetime = 30L;
    private Timer lockerRemoveTimer;

    public class LockerRemoveTimerTask extends TimerTask {

        String nextLocker;

        public LockerRemoveTimerTask(String nextLocker) {
            this.nextLocker = nextLocker;
        }

        public void run() {
            MDC.put("context", ThreadCtx);
            LOGGER.debug("Try to remove");
            if (nextLocker != null) {
                LOGGER.debug("Remove " + nextLocker);
                Globals.jocWebserviceDataContainer.getSOSLocker().removeContent(nextLocker);

            } else {
                LOGGER.debug("nextLocker is null");
            }
            getLockerToRemove();
        }

    }

    private void resetTimer(Entry<String, SOSLockerContent> eldestContent) {
        if (lockerRemoveTimer != null) {
            lockerRemoveTimer.cancel();
            lockerRemoveTimer.purge();
        }
        lockerRemoveTimer = new Timer("Locker");
        Long waitTask = eldestContent.getValue().getCreated() + lifetime * 60 * 1000 - Instant.now().toEpochMilli();
        if (waitTask < 0) {
            waitTask = 0L;
        }
        LOGGER.debug("will remove " + eldestContent.getKey() + " in " + waitTask / 1000 + " seconds");

        lockerRemoveTimer.schedule(new LockerRemoveTimerTask(eldestContent.getKey()), waitTask);
    }

    private void getLockerToRemove() {
        Entry<String, SOSLockerContent> eldestContent = null;
        eldestContent = Globals.jocWebserviceDataContainer.getSOSLocker().getEldestContent();

        if (eldestContent != null) {
            int count = 0;
            boolean success = true;
            do
                try {
                    count = count + 1;
                    resetTimer(eldestContent);
                    success = true;
                } catch (IllegalStateException e) {
                    try {
                        java.lang.Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    success = false;
                    LOGGER.debug("IllegalStateException scheduling remove of locker content");
                }
            while (count < 60 && !success);

        } else {
            if (lockerRemoveTimer != null) {
                lockerRemoveTimer.cancel();
                lockerRemoveTimer.purge();
            }
        }
    }

    public void start() {
        MDC.put("context", ThreadCtx);
        getLockerToRemove();
    }

}
