package com.sos.auth.sosintern;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.format.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
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

			PasswordMatcher p = new org.apache.shiro.authc.credential.PasswordMatcher();
			DefaultPasswordService ps = new DefaultPasswordService();

			DefaultHashService sha1HashService = new DefaultHashService();
			sha1HashService.setHashAlgorithmName(hashParts[2]);
			try {
				sha1HashService.setHashIterations(Integer.valueOf(hashParts[3]));
			} catch (NumberFormatException e) {
				sha1HashService.setHashIterations(1);
			}
			sha1HashService.setGeneratePublicSalt(true);
			ps.setHashService(sha1HashService);
			ps.setHashFormat(new org.apache.shiro.crypto.hash.format.Shiro1CryptFormat());
			p.setPasswordService(ps);
			return p.getPasswordService().passwordsMatch(pwd, hash);
		} else {
			return false;
		}
	}

	public SOSAuthAccessToken login(SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials,
			String password) throws SOSHibernateException {

		SOSHibernateSession sosHibernateSession = null;
		try {
			sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
			SOSAuthAccessToken sosAuthAccessToken = null;
			IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
			String accountPwd;
			forcePasswordChange = false;
			try {
				accountPwd = SOSAuthHelper.getSHA512(password);
				IamAccountFilter filter = new IamAccountFilter();
				filter.setAccountName(sosInternAuthWebserviceCredentials.getAccount());
				filter.setIdentityServiceId(sosInternAuthWebserviceCredentials.getIdentityServiceId());

				DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);

				if (isShiroMatch(dbItemIamAccount.getAccountPassword(), password)
						|| (dbItemIamAccount != null && dbItemIamAccount.getAccountPassword().equals(accountPwd))
								&& !dbItemIamAccount.getDisabled()) {
					sosAuthAccessToken = new SOSAuthAccessToken();
					sosAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
					forcePasswordChange = dbItemIamAccount.getForcePasswordChange();
				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				LOGGER.info(e.getMessage());
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
