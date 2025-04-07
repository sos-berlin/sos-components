package com.sos.commons.vfs.smb.commons;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class SMBProviderArguments extends AProviderArguments {

    private static final int DEFAULT_PORT = 445;

    // JS7 new - not in the XML schema
    private SOSArgument<SMBAuthMethod> authMethod = new SOSArgument<>("auth_method", false, SMBAuthMethod.BASIC);

    private SOSArgument<String> domain = new SOSArgument<>("domain", false);
    // JS7 new - not in the XML schema
    private SOSArgument<String> shareName = new SOSArgument<>("share_name", false);

    public SMBProviderArguments() {
        getProtocol().setDefaultValue(Protocol.SMB);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        String authMethod = "";
        String user = getUser().getDisplayValue();
        switch (getAuthMethod().getValue()) {
        case BASIC:
            if (SOSString.isEmpty(user)) {
                user = "anonymous";
            }
            break;
        case KERBEROS:
        case SPNEGO:
        default:
            authMethod = "[" + getAuthMethod().getValue().name() + "]";
            if (SOSString.isEmpty(user)) {
                user = "[sso]";
            }
            break;
        }
        String domain = getDomain().isEmpty() ? "" : " (" + getDomain().getValue() + ")";
        return String.format("%s%s@%s:%s%s", authMethod, user, getHost().getDisplayValue(), getPort().getDisplayValue(), domain);
    }

    public SOSArgument<SMBAuthMethod> getAuthMethod() {
        return authMethod;
    }

    public SOSArgument<String> getDomain() {
        return domain;
    }

    public SOSArgument<String> getShareName() {
        return shareName;
    }
}
