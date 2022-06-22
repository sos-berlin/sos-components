package com.sos.auth.classes;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;

public class SOSAuthAccessTokenHandler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthAccessTokenHandler.class);
    private static final int TIME_GAP_SECONDS = 20;
    private static final String ThreadCtx = "authentication";

    private boolean stop = false;

    public class AccessTokenTimerTask extends TimerTask {

        SOSAuthCurrentAccount nextAccount;

        public AccessTokenTimerTask(SOSAuthCurrentAccount nextAccount) {
            this.nextAccount = nextAccount;
        }

        public void run() {
            MDC.put("context", ThreadCtx);
            LOGGER.debug("Try to renew");
            if (nextAccount != null) {
                LOGGER.debug("Renew " + nextAccount.getAccountname());
                boolean valid = false;
                try {
                    valid = nextAccount.getCurrentSubject().getSession().renew();
                } catch (Exception e) {
                    valid = false;
                }
                if (!valid) {
                    LOGGER.info("account is no longer valid");
                    Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeAccount(nextAccount.getAccessToken());
                }
            } else {
                LOGGER.debug("nextAccount is null");
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
        LOGGER.debug("will renew " + nextAccount.getAccountname() + " in " + next / 1000 + "s");
        accessTokenTimer.schedule(new AccessTokenTimerTask(nextAccount), next);
    }

    private void getNextAccessToken() {
        if (!stop) {
            Long next = null;
            Long nextTime = null;
            SOSAuthCurrentAccount nextAccount = null;
            for (String accessToken : Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccessTokens()) {
                SOSAuthCurrentAccount currentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);

                if (currentAccount.isAuthenticated()) {
                    switch (currentAccount.getIdentityServices().getIdentyServiceType()) {

                    case KEYCLOAK:
                    case KEYCLOAK_JOC:
                        if (currentAccount.getCurrentSubject().getSession().getSOSKeycloakAccountAccessToken() != null) {
                            LOGGER.debug(SOSString.toString(currentAccount.getCurrentSubject().getSession().getSOSKeycloakAccountAccessToken()));

                            long r1 = currentAccount.getCurrentSubject().getSession().getSOSKeycloakAccountAccessToken().getRefresh_expires_in();
                            long r2 = currentAccount.getCurrentSubject().getSession().getSOSKeycloakAccountAccessToken().getExpires_in();
                            if (r1 > r2) {
                                r1 = r2;
                            }

                            long leaseDuration = r1 * 1000;

                            long n = currentAccount.getCurrentSubject().getSession().getStartSession() + leaseDuration - TIME_GAP_SECONDS * 1000;

                            if (next == null || nextTime > n) {
                                nextTime = n;
                                next = leaseDuration - TIME_GAP_SECONDS * 1000;
                                nextAccount = currentAccount;
                            }
                        }
                        break;
                    case VAULT:
                    case VAULT_JOC:
                    case VAULT_JOC_ACTIVE:
                        if (currentAccount.getCurrentSubject().getSession().getSOSVaultAccountAccessToken() != null) {
                            long leaseDuration = currentAccount.getCurrentSubject().getSession().getSOSVaultAccountAccessToken().getAuth()
                                    .getLease_duration() * 1000;

                            long n = currentAccount.getCurrentSubject().getSession().getStartSession() + leaseDuration - TIME_GAP_SECONDS * 1000;

                            if (next == null || nextTime > n) {
                                nextTime = n;
                                next = leaseDuration - TIME_GAP_SECONDS * 1000;
                                nextAccount = currentAccount;
                            }
                        }
                        break;
                    default:
                        break;

                    }
                }

            }
            if (nextAccount != null)

            {
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
