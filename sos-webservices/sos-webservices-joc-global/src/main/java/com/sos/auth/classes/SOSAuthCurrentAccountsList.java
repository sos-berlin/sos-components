package com.sos.auth.classes;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSAuthCurrentAccountsList {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthCurrentAccountsList.class);
    private static final String VALID = "valid";
    private static final String NOT_VALID = "not-valid";
    private Map<String, SOSAuthCurrentAccount> currentAccounts;

    public SOSAuthCurrentAccountsList() {
        this.currentAccounts = new ConcurrentHashMap <String, SOSAuthCurrentAccount>();
    }

    public void addAccount(SOSAuthCurrentAccount account) {
        this.currentAccounts.put(account.getAccessToken(), account);
    }

    public SOSAuthCurrentAccount getAccount(String accessToken) {
        return this.currentAccounts.get(accessToken);
    }

    public void removeAccount(String accessToken) {
        currentAccounts.remove(accessToken);
    }

    public Set<String> getAccessTokens() {
        return currentAccounts.keySet();
    }

    public void removeTimedOutAccount(String account) {

        ArrayList<String> toBeRemoved = new ArrayList<String>();
        Map<String, SOSAuthCurrentAccount> currentAccountsShadow = new HashMap<String, SOSAuthCurrentAccount>();
        try {
            currentAccountsShadow.putAll(currentAccounts);
        } catch (ConcurrentModificationException e) {
            LOGGER.info("Removing expiring sessions will be deferred");
        }

        for (Map.Entry<String, SOSAuthCurrentAccount> entry : currentAccountsShadow.entrySet()) {
            boolean found = account.equals(entry.getValue().getAccountname());
            if (found) {
                if (entry.getValue().getCurrentSubject() != null && entry.getValue().getCurrentSubject().getSession() != null) {
                    try {
                        long sessionTimeout = entry.getValue().getCurrentSubject().getSession().getTimeout();
                        if (sessionTimeout == 0) {
                            toBeRemoved.add(entry.getValue().getAccessToken());
                        }
                    } catch (Exception e) {
                        toBeRemoved.add(entry.getValue().getAccessToken());
                    }
                }
            }
        }

        Iterator<String> it = toBeRemoved.iterator();
        String accessToken = "";
        while (it.hasNext()) {
            try {
                accessToken = it.next();
                currentAccounts.remove(accessToken);
            } catch (ConcurrentModificationException e) {
                LOGGER.info("Removing expiring sessions will be deferred: " + accessToken);
            }
        }

    }

    public SOSAuthCurrentAccountAnswer getAccountByName(String account) {
        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(account);
        sosAuthCurrentAccountAnswer.setAccount(account);
        sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
        sosAuthCurrentAccountAnswer.setSessionTimeout(0L);
        boolean found = false;
        for (Map.Entry<String, SOSAuthCurrentAccount> entry : currentAccounts.entrySet()) {
            found = account.equals(entry.getValue().getAccountname());
            if (found) {
                sosAuthCurrentAccountAnswer.setAccessToken(VALID);

                if (entry.getValue().getCurrentSubject() != null) {
                    sosAuthCurrentAccountAnswer.setIsAuthenticated(entry.getValue().getCurrentSubject().isAuthenticated());
                    if (entry.getValue().getCurrentSubject().getSession() != null) {
                        long sessionTimeout = entry.getValue().getCurrentSubject().getSession().getTimeout();
                        if (sessionTimeout == 0L) {
                            sosAuthCurrentAccountAnswer.setMessage("Session for " + account + " has expired");
                            sosAuthCurrentAccountAnswer.setSessionTimeout(0l);
                            sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
                            sosAuthCurrentAccountAnswer.setAccessToken(NOT_VALID);
                        } else {
                            sosAuthCurrentAccountAnswer.setSessionTimeout(sessionTimeout);
                        }
                    }
                }
                return sosAuthCurrentAccountAnswer;
            }
        }

        sosAuthCurrentAccountAnswer.setAccessToken(NOT_VALID);
        sosAuthCurrentAccountAnswer.setMessage("account " + account + " not found");
        return sosAuthCurrentAccountAnswer;
    }

    public int size() {
        return currentAccounts.size();
    }

    public SOSAuthCurrentAccountAnswer getAccountByToken(String token) {
        SOSAuthCurrentAccount account = getAccount(token);
        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = null;
        if (account != null) {
            sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer(account.getAccountname());
            sosAuthCurrentAccountAnswer.setAccessToken(VALID);
            sosAuthCurrentAccountAnswer.setIsAuthenticated(account.isAuthenticated());

            if (account.getCurrentSubject() != null && account.getCurrentSubject().getSession() != null) {

                long sessionTimeout = account.getCurrentSubject().getSession().getTimeout();
                if (sessionTimeout == 0L) {
                    sosAuthCurrentAccountAnswer.setMessage("Session for " + token + " has expired");
                    sosAuthCurrentAccountAnswer.setSessionTimeout(0l);
                    sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
                    sosAuthCurrentAccountAnswer.setAccessToken(NOT_VALID);
                } else {
                    sosAuthCurrentAccountAnswer.setSessionTimeout(sessionTimeout);
                }

            } else {
                sosAuthCurrentAccountAnswer.setSessionTimeout(0L);
            }

        } else {
            sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer("");
            sosAuthCurrentAccountAnswer.setAccessToken(NOT_VALID);
            sosAuthCurrentAccountAnswer.setMessage("token not valid");
            sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
            sosAuthCurrentAccountAnswer.setSessionTimeout(0L);
        }

        return sosAuthCurrentAccountAnswer;
    }

}
