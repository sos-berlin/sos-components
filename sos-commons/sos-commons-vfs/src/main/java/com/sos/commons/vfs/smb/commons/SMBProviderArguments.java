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

        StringBuilder sb = new StringBuilder();
        sb.append(authMethod);
        sb.append(user).append("@").append(getHost().getValue()).append(":").append(getPort().getValue());
        if (!getDomain().isEmpty()) {
            sb.append("(").append(getDomain().getValue()).append(")");
        }
        return sb.toString();
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return null;
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
