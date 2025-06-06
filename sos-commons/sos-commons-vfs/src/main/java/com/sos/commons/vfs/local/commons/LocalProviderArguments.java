package com.sos.commons.vfs.local.commons;

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

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return null;
    }

    private String getHostname() {
        return SOSShell.getLocalHostNameOptional().orElse("localhost");
    }

}
