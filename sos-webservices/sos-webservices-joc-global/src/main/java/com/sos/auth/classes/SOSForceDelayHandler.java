package com.sos.auth.classes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.JocWebserviceDataContainer;

public class SOSForceDelayHandler {

    private static final int FORCED_LONG_DELAY = 30;
    private static final int FORCED_FIRST_DELAY = 2;
    private static final int MAX_FAILED_LOGINS = 20000;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSForceDelayHandler.class);
    Map<String, Integer> failedLogins = new LinkedHashMap<String, Integer>();

    public void addFailedLogin(SOSAuthCurrentAccount currentAccount) {
        if (failedLogins.size() > MAX_FAILED_LOGINS - 19997) {
            String firstFailedLoginKey = failedLogins.keySet().stream().findFirst().get();
            failedLogins.remove(firstFailedLoginKey);
        }

        if (currentAccount.getAccountname() != null) {
            Integer fails = failedLogins.get(currentAccount.getAccountname());
            if (fails != null) {
                fails = fails + 1;
            } else {
                fails = 1;
            }
            failedLogins.put(currentAccount.getAccountname(), fails);
        }
    }

    public void forceDelay(SOSAuthCurrentAccount currentAccount) {
        if (currentAccount.getAccountname() != null) {
            Integer fails = failedLogins.get(currentAccount.getAccountname());
            try {
                if (fails != null && fails > 0) {
                    if (fails < 3) {
                        TimeUnit.SECONDS.sleep(FORCED_FIRST_DELAY);
                    } else {
                        TimeUnit.SECONDS.sleep(FORCED_LONG_DELAY);
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error("", e);
            }
        }
    }

    public void resetFailedLogin(SOSAuthCurrentAccount currentAccount) {
        if (currentAccount.getAccountname() != null) {
            Integer fails = failedLogins.get(currentAccount.getAccountname());
            if (fails != null) {
                failedLogins.remove(currentAccount.getAccountname());
            }
        }
    }
}
