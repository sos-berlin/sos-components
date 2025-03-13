package com.sos.commons.vfs.webdav.commons;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;

public class WebDAVProviderArguments extends AProviderArguments {

    private ProxyArguments proxy;

    // JS7 new - not in the XML schema
    private SOSArgument<WebDAVAuthMethod> authMethod = new SOSArgument<>("auth_method", false, WebDAVAuthMethod.NTLM);
    // JS7 new - if WebDAVAuthMethod.NTLM
    private SOSArgument<String> domain = new SOSArgument<>("domain", false);
    // JS7 new - if WebDAVAuthMethod.NTLM
    private SOSArgument<String> workstation = new SOSArgument<>("workstation", false);

    public WebDAVProviderArguments() {
        getProtocol().setValue(Protocol.WEBDAV);
        getPort().setDefaultValue(HTTPProviderArguments.DEFAULT_PORT);
    }

    public ProxyArguments getProxy() {
        return proxy;
    }

    public void setProxy(ProxyArguments val) {
        proxy = val;
    }

    public SOSArgument<WebDAVAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<String> getDomain() {
        return domain;
    }

    public SOSArgument<String> getWorkstation() {
        return workstation;
    }

}
