package com.sos.commons.vfs.local.commons;

import java.net.UnknownHostException;

import com.sos.commons.util.SOSShell;
import com.sos.commons.vfs.commons.AProviderArguments;

public class LocalProviderArguments extends AProviderArguments {

    public LocalProviderArguments() {
        getProtocol().setDefaultValue(Protocol.LOCAL);

        getHost().setValue(getHostname());
        getUser().setValue(SOSShell.getUsername());
    }

    private String getHostname() {
        try {
            return SOSShell.getHostname();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
