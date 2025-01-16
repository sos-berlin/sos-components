package com.sos.commons.vfs.local.common;

import com.sos.commons.vfs.common.AProviderArguments;

public class LocalProviderArguments extends AProviderArguments {

    public LocalProviderArguments() {
        getProtocol().setDefaultValue(Protocol.LOCAL);
    }
}
