package com.sos.commons.vfs.local.commons;

import com.sos.commons.vfs.commons.AProviderArguments;

public class LocalProviderArguments extends AProviderArguments {

    public LocalProviderArguments() {
        getProtocol().setDefaultValue(Protocol.LOCAL);
    }
}
