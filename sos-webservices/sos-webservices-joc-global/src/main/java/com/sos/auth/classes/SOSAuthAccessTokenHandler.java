package com.sos.auth.classes;

import java.util.Timer;
import java.util.TimerTask;

import com.sos.joc.Globals;
import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSAuthAccessTokenHandler extends Thread {

    private static final int TIME_GAP_SECONDS = 28;
    private boolean stop;

    public class AccessTokenTimerTask extends TimerTask {

        SOSAuthCurrentAccount nextAccount;

        public AccessTokenTimerTask(SOSAuthCurrentAccount nextAccount) {
            this.nextAccount = nextAccount;
        }

        public void run() {
            if (nextAccount != null) {
                System.out.println(this.nextAccount.getAccountname() + " " + this.nextAccount.getCurrentSubject().getSession().getTimeout());
                if (!nextAccount.getCurrentSubject().getSession().renew()) {
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
