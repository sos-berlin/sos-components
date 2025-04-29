package com.sos.commons.vfs.smb.smbj;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.auth.GSSAuthenticationContext;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.smb.commons.SMBAuthMethod;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;

public class SMBAuthenticationContextFactory {

    public static AuthenticationContext create(SMBProviderArguments args) throws Exception {
        switch (args.getAuthMethod().getValue()) {
        case ANONYMOUS:
            // sets: user='', pass=new char[0] ,domain=null
            return AuthenticationContext.anonymous();
        case GUEST:
            // return new AuthenticationContext(args.getUser().getValue(), new char[0], args.getDomain().getValue());
            // sets: user='Guest', pass=new char[0] ,domain=null
            return AuthenticationContext.guest();
        case NTLM:
            char[] pass = args.getPassword().getValue() == null ? new char[0] : args.getPassword().getValue().toCharArray();
            return new AuthenticationContext(args.getUser().getValue(), pass, args.getDomain().getValue());
        case KERBEROS:
        case SPNEGO:
        default:
            return createGSSAPIContext(args);
        }
    }

    /** Creates a GSSAPI authentication context based on the selected mechanism (e.g. Kerberos, SPNEGO, etc.). */
    private static GSSAuthenticationContext createGSSAPIContext(SMBProviderArguments args) throws Exception {
        SMBAuthMethod authMethod = args.getAuthMethod().getValue();
        String username = args.getUser().getValue();
        String password = args.getPassword().getValue();
        String domain = args.getDomain().getValue();
        String loginContextName = args.getLoginContextName().isEmpty() ? authMethod.getLoginContextName() : args.getLoginContextName().getValue();
        LoginContext loginContext;
        if (password == null) {
            // uses the ticket cache (e.g. kinit for Kerberos SSO)
            loginContext = new LoginContext(loginContextName);
        } else {
            loginContext = new LoginContext(loginContextName, new CallbackHandler() {

                @Override
                public void handle(Callback[] callbacks) {
                    for (Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(username);
                        } else if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        }
                    }
                }
            });
        }
        loginContext.login();
        return new GSSAuthenticationContext(username, domain == null ? "" : domain, loginContext.getSubject(), getGSSCredential(username, domain,
                new Oid(authMethod.getOid())));
    }

    private static GSSCredential getGSSCredential(String username, String domain, Oid oid) throws GSSException {
        GSSManager manager = GSSManager.getInstance();
        GSSName gssName = null;
        if (!SOSString.isEmpty(username)) {
            // uses the ticket cache
            gssName = manager.createName(SOSString.isEmpty(domain) ? username : (username + "@" + domain), GSSName.NT_USER_NAME);
        }
        // GSSCredential.INDEFINITE_LIFETIME
        // - is for Credentials to indicate an indefinite validity, meaning that the credential is valid without any expiration time.
        // GSSContext.DEFAULT_LIFETIME
        // - is for the GSSContext to set the lifetime of the context to the default lifetime defined by the mechanism being used,
        // -- if no explicit lifetime is provided.
        return manager.createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, oid, GSSCredential.INITIATE_ONLY);
    }
}
