package com.sos.auth.classes;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.joc.Globals;
import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSAuthAccessTokenHandler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthAccessTokenHandler.class);
    private static final int TIME_GAP_SECONDS = 20;
    private static final String ThreadCtx = "authentication";

    private boolean stop;

    public class AccessTokenTimerTask extends TimerTask {

        SOSAuthCurrentAccount nextAccount;

        public AccessTokenTimerTask(SOSAuthCurrentAccount nextAccount) {
            MDC.put("context", ThreadCtx);
            this.nextAccount = nextAccount;
        }

        public void run() {
            if (nextAccount != null) {
                LOGGER.debug("Renew " + nextAccount.getAccountname());
                boolean valid=false;
                try {
                    valid = nextAccount.getCurrentSubject().getSession().renew();
                }catch(Exception e) {
                    valid = false;
                }
                if (!valid) {
                    LOGGER.info(nextAccount.getAccountname() + " no longer valid");
                    Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeAccount(nextAccount.getAccessToken());
                }
            }
            getNextAccessToken();
        }

    }

    private Timer accessTokenTimer;

    private void resetTimer(SOSAuthCurrentAccount nextAccount, Long next) {
        if (accessTokenTimer != null) {
            accessTokenTimer.cancel();
            accessTokenTimer.purge();
        }
        accessTokenTimer = new Timer();
        LOGGER.debug("will renew " + nextAccount.getAccountname() + " in " +  next/1000 + "s");
        accessTokenTimer.schedule(new AccessTokenTimerTask(nextAccount), next);
    }

    private void getNextAccessToken() {
        if (!stop) {
            Long next = null;
            SOSAuthCurrentAccount nextAccount = null;
            for (String accessToken : Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccessTokens()) {
                SOSAuthCurrentAccount currentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
                if (!currentAccount.isShiro() && currentAccount.isAuthenticated() && (IdentityServiceTypes.VAULT.equals(currentAccount
                        .getIdentityServices().getIdentyServiceType()) || IdentityServiceTypes.VAULT_JOC.equals(currentAccount.getIdentityServices()
                                .getIdentyServiceType()) || IdentityServiceTypes.VAULT_JOC_ACTIVE.equals(currentAccount.getIdentityServices()
                                        .getIdentyServiceType()))) {
                    if (currentAccount.getCurrentSubject().getSession().getSOSVaultAccountAccessToken() != null) {
                        long leaseDuration = currentAccount.getCurrentSubject().getSession().getSOSVaultAccountAccessToken().getAuth()
                                .getLease_duration() * 1000;

                        long n = currentAccount.getCurrentSubject().getSession().getStartSession() + leaseDuration - TIME_GAP_SECONDS * 1000;

                        if (next == null || next > n) {
                            next = leaseDuration - TIME_GAP_SECONDS * 1000;
                            nextAccount = currentAccount;
                        }
                    }
                }
            }
            if (nextAccount != null) {
                resetTimer(nextAccount, next);
            }
        }
    }

    public void run() {
        MDC.put("context", ThreadCtx);
        stop = false;
        getNextAccessToken();
    }

    public void endExecution() {
        stop = true;
        if (accessTokenTimer != null) {
            accessTokenTimer.cancel();
            accessTokenTimer.purge();
        }
    }
}
