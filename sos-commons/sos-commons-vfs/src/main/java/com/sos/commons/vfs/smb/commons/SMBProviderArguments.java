package com.sos.commons.vfs.smb.commons;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;

public class SMBProviderArguments extends AProviderArguments {

    private static final int DEFAULT_PORT = 445;

    // JS7 new - not in the XML schema
    private SOSArgument<AuthMethod> authMethod = new SOSArgument<>("auth_method", false, AuthMethod.BASIC);

    private SOSArgument<String> domain = new SOSArgument<>("domain", false);
    // JS7 new - not in the XML schema
    private SOSArgument<String> shareName = new SOSArgument<>("share_name", false);

    public SMBProviderArguments() {
        getProtocol().setDefaultValue(Protocol.SMB);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    public SOSArgument<AuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<String> getDomain() {
        return domain;
    }

    public SOSArgument<String> getShareName() {
        return shareName;
    }
}
