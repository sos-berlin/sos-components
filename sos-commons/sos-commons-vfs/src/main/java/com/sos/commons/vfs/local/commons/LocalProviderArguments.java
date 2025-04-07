package com.sos.commons.vfs.local.commons;

import java.net.UnknownHostException;

import com.sos.commons.util.SOSShell;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class LocalProviderArguments extends AProviderArguments {

    public LocalProviderArguments() {
        getProtocol().setDefaultValue(Protocol.LOCAL);

        getHost().setValue(getHostname());
        getUser().setValue(SOSShell.getUsername());
    }

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        return String.format("%s@%s", getUser().getDisplayValue(), getHost().getDisplayValue());
    }

    private String getHostname() {
        try {
            return SOSShell.getHostname();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
