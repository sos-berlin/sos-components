package com.sos.auth.sosintern;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSPasswordHasher;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthWebserviceCredentials;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;

public class SOSInternAuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSInternAuthHandler.class);
    private Boolean forcePasswordChange = false;

    public SOSInternAuthHandler() {
    }

    private boolean isShiroMatch(String hash, String pwd) {

        String s = hash + "$$$$";

        String[] hashParts = s.split("\\$");
        if ("shiro1".equals(hashParts[1])) {

            PasswordMatcher p = new PasswordMatcher();
            DefaultPasswordService ps = new DefaultPasswordService();

            DefaultHashService defaultHashService = new DefaultHashService();
            defaultHashService.setHashAlgorithmName(hashParts[2]);
            try {
                defaultHashService.setHashIterations(Integer.valueOf(hashParts[3]));
            } catch (NumberFormatException e) {
                defaultHashService.setHashIterations(1);
            }
            defaultHashService.setGeneratePublicSalt(true);
            ps.setHashService(defaultHashService);
            ps.setHashFormat(new org.apache.shiro.crypto.hash.format.Shiro1CryptFormat());
            p.setPasswordService(ps);
            return p.getPasswordService().passwordsMatch(pwd, hash);
        } else {
            return false;
        }
    }

    public SOSAuthAccessToken login(SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials, String password)
            throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            SOSAuthAccessToken sosAuthAccessToken = null;
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            forcePasswordChange = false;
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(sosInternAuthWebserviceCredentials.getAccount());
            filter.setIdentityServiceId(sosInternAuthWebserviceCredentials.getIdentityServiceId());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);

            if (dbItemIamAccount != null && (SOSPasswordHasher.verify(password, dbItemIamAccount.getAccountPassword())) && !dbItemIamAccount
                    .getDisabled()) {
                sosAuthAccessToken = new SOSAuthAccessToken();
                sosAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
                forcePasswordChange = dbItemIamAccount.getForcePasswordChange();
                if (dbItemIamAccount.getAccountPassword().startsWith("$shiro")) {
                    try {
                        dbItemIamAccount.setAccountPassword(SOSPasswordHasher.hash(password));
                    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                        LOGGER.error("", e);
                    }
                    sosHibernateSession.setAutoCommit(false);
                    sosHibernateSession.beginTransaction();
                    sosHibernateSession.update(dbItemIamAccount);
                    sosHibernateSession.commit();
                }
            }
            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

}
