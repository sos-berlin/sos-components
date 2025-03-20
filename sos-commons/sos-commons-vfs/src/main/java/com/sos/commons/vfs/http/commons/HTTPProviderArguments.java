package com.sos.commons.vfs.http.commons;

import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.vfs.commons.AProviderArguments;

public class HTTPProviderArguments extends AProviderArguments {

    public static final int DEFAULT_PORT = 80;

    private SSLArguments ssl;

    // JS7 new - auth_method - not in the XML schema - currently only BASIC supported
    private SOSArgument<HTTPAuthMethod> authMethod = new SOSArgument<>("auth_method", false, HTTPAuthMethod.BASIC);
    private SOSArgument<List<String>> httpHeaders = new SOSArgument<>("http_headers", false);

    // JS7 new - if WebDAVAuthMethod.NTLM
    private SOSArgument<String> domain = new SOSArgument<>("domain", false);
    // JS7 new - if WebDAVAuthMethod.NTLM
    private SOSArgument<String> workstation = new SOSArgument<>("workstation", false);

    public HTTPProviderArguments() {
        getProtocol().setValue(Protocol.HTTP);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    public SSLArguments getSSL() {
        if (ssl == null) {
            ssl = new SSLArguments();
            ssl.applyDefaultIfNullQuietly();
        }
        return ssl;
    }

    public void setSSL(SSLArguments val) {
        ssl = val;
    }

    public SOSArgument<HTTPAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<List<String>> getHTTPHeaders() {
        return httpHeaders;
    }

    public SOSArgument<String> getDomain() {
        return domain;
    }

    public SOSArgument<String> getWorkstation() {
        return workstation;
    }

}
