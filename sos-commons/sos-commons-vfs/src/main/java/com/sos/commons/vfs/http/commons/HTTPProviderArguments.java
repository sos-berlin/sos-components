package com.sos.commons.vfs.http.commons;

import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;

public class HTTPProviderArguments extends AProviderArguments {

    public enum Impl {
        APACHE, JAVA
    }

    public static final int DEFAULT_PORT = 80;

    // JS7 new - impl, auth_method - not in the XML schema - currently only BASIC supported
    private SOSArgument<Impl> impl = new SOSArgument<>("impl", false, Impl.APACHE);
    private SOSArgument<HTTPAuthMethod> authMethod = new SOSArgument<>("auth_method", false, HTTPAuthMethod.BASIC);
    private SOSArgument<List<String>> httpHeaders = new SOSArgument<>("http_headers", false);

    public HTTPProviderArguments() {
        getProtocol().setValue(Protocol.HTTP);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    public SOSArgument<Impl> getImpl() {
        return impl;
    }

    public SOSArgument<HTTPAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<List<String>> getHTTPHeaders() {
        return httpHeaders;
    }

}
