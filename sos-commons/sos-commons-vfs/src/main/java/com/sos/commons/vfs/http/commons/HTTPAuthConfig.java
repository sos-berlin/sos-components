package com.sos.commons.vfs.http.commons;

import java.net.UnknownHostException;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class HTTPAuthConfig {

    private final String username;
    private final String password;

    private NTLM ntlm;

    public HTTPAuthConfig(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public HTTPAuthConfig(ISOSLogger logger, final String username, final String password, final String workstation, final String domain) {
        this(username, password);
        ntlm = createNTLM(logger, username, workstation, domain);
    }

    private NTLM createNTLM(ISOSLogger logger, final String username, final String workstation, final String domain) {
        String user = username;
        String userWorkstation = workstation;
        String userDomain = domain;

        if (SOSString.isEmpty(userWorkstation)) {
            try {
                userWorkstation = SOSShell.getLocalHostName();
            } catch (UnknownHostException e) {
                logger.warn("[HTTPAuthConfig][workstation][getHostname]" + e.toString());
            }
        }
        if (SOSString.isEmpty(userDomain)) {
            String[] arr = username.split("\\\\");
            if (arr.length > 1) {
                userDomain = arr[0];
                user = arr[1];
            }
        }

        return new NTLM(user, userDomain, userWorkstation);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public NTLM getNTLM() {
        return ntlm;
    }

    public class NTLM {

        private final String username;
        private final String domain;
        private final String workstation;

        private NTLM(String username, String domain, String workstation) {
            this.username = username;
            this.domain = domain;
            this.workstation = workstation;
        }

        public String getUsername() {
            return username;
        }

        public String getDomain() {
            return domain;
        }

        public String getWorkstation() {
            return workstation;
        }

    }
}
