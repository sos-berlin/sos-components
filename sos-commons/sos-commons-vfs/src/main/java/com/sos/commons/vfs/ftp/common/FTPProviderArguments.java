package com.sos.commons.vfs.ftp.common;

import java.nio.file.Path;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.common.AProviderArguments;

public class FTPProviderArguments extends AProviderArguments {

    private SOSArgument<Integer> port = new SOSArgument<Integer>("port", true, 21);

    // Java Keystore/Truststore
    private SOSArgument<KeyStoreType> keystoreType = new SOSArgument<KeyStoreType>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> keystoreFile = new SOSArgument<Path>("keystore_file", false);
    private SOSArgument<String> keystorePassword = new SOSArgument<String>("keystore_password", false, DisplayMode.MASKED);

    public FTPProviderArguments() {
        getProtocol().setDefaultValue(Protocol.FTP);
    }

    public SOSArgument<Integer> getPort() {
        return port;
    }

    public SOSArgument<KeyStoreType> getKeystoreType() {
        return keystoreType;
    }

    public SOSArgument<Path> getKeystoreFile() {
        return keystoreFile;
    }

    public SOSArgument<String> getKeystorePassword() {
        return keystorePassword;
    }
}
