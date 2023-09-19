package com.sos.auth.classes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSForceDelayHandler {

    private static final int FIRST_DELAY_LEVEL = 2;
    private static final int FORCED_LONG_DELAY = 30;
    private static final int FORCED_FIRST_DELAY = 2;
    private static final int MAX_FAILED_LOGINS = 20000;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSForceDelayHandler.class);
    Map<String, Integer> failedLoginsAccounts = new LinkedHashMap<String, Integer>();
    Map<String, Integer> failedLoginsIp = new LinkedHashMap<String, Integer>();

    private Integer getFails(String key, Map<String, Integer> failedLogins) {
        Integer fails = 1;
        if (key != null) {
            fails = failedLogins.get(key);
            if (fails != null) {
                fails = fails + 1;
            } else {
                fails = 1;
            }
        }
        return fails;
    }

    private int getDelay(Integer fails) {
        if (fails != null && fails > 0) {
            if (fails <= FIRST_DELAY_LEVEL) {
                return FORCED_FIRST_DELAY;
            } else {
                return FORCED_LONG_DELAY;
            }
        }
        return 0;
    }

    private void addFailedLoginAccounts(SOSAuthCurrentAccount currentAccount) {
        if (currentAccount.getAccountname() != null && !currentAccount.getAccountname().isEmpty()) {
            if (failedLoginsAccounts.size() >= MAX_FAILED_LOGINS) {
                String firstFailedLoginKey = failedLoginsAccounts.keySet().stream().findFirst().get();
                failedLoginsAccounts.remove(firstFailedLoginKey);
            }
            failedLoginsAccounts.put(currentAccount.getAccountname(), getFails(currentAccount.getAccountname(), failedLoginsAccounts));
        }
    }

    private void addFailedLoginIp(SOSAuthCurrentAccount currentAccount) {
        if (currentAccount.getCallerIpAddress() != null && !currentAccount.getCallerIpAddress().isEmpty()) {
            if (failedLoginsIp.size() >= MAX_FAILED_LOGINS) {
                String firstFailedLoginKey = failedLoginsIp.keySet().stream().findFirst().get();
                failedLoginsIp.remove(firstFailedLoginKey);
            }
            failedLoginsIp.put(currentAccount.getCallerIpAddress(), getFails(currentAccount.getCallerIpAddress(), failedLoginsIp));
        }
    }

    private int forceDelayAccount(String accountName) {
        if (accountName != null) {
            Integer fails = failedLoginsAccounts.get(accountName);
            return getDelay(fails);
        }
        return 0;
    }

    private int forceDelayIp(String ip) {
        if (ip != null) {
            Integer fails = failedLoginsIp.get(ip);
            return getDelay(fails);
        }
        return 0;
    }

    public void addFailedLogin(SOSAuthCurrentAccount currentAccount) {
        if (currentAccount.getAccountname() != null && !currentAccount.getAccountname().isEmpty()) {
            addFailedLoginAccounts(currentAccount);
            addFailedLoginIp(currentAccount);
        }
    }

    public void forceDelay(SOSAuthCurrentAccount currentAccount) {
        int delay = 0;
        delay = forceDelayAccount(currentAccount.getAccountname());
        delay = delay + forceDelayIp(currentAccount.getCallerIpAddress());
        if (delay > FORCED_LONG_DELAY) {
            delay = FORCED_LONG_DELAY;
        } else {
            if (delay > FIRST_DELAY_LEVEL) {
                delay = FIRST_DELAY_LEVEL;
            }
        }
        if (delay > 0) {
            try {
                LOGGER.debug("....force delay for " + delay + " seconds");
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                LOGGER.error("", e);
            }
        }
    }

    public void resetFailedLogin(SOSAuthCurrentAccount currentAccount) {
        if (currentAccount.getAccountname() != null && !currentAccount.getAccountname().isEmpty()) {
            Integer fails = failedLoginsAccounts.get(currentAccount.getAccountname());
            if (fails != null) {
                LOGGER.debug("....Reset force delay for " + currentAccount.getAccountname());
                failedLoginsAccounts.remove(currentAccount.getAccountname());
            }
        }
        if (currentAccount.getCallerIpAddress() != null && !currentAccount.getCallerIpAddress().isEmpty()) {
            Integer fails = failedLoginsIp.get(currentAccount.getCallerIpAddress());
            if (fails != null) {
                LOGGER.debug("....Reset force delay for " + currentAccount.getCallerIpAddress());
                failedLoginsIp.remove(currentAccount.getCallerIpAddress());
            }
        }
    }
}
