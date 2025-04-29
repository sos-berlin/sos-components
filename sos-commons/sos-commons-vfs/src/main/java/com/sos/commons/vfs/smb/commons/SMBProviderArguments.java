package com.sos.commons.vfs.smb.commons;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class SMBProviderArguments extends AProviderArguments {

    private static final int DEFAULT_PORT = 445;

    // JS7 new
    private SOSArgument<SMBAuthMethod> authMethod = new SOSArgument<>("auth_method", false, SMBAuthMethod.NTLM);

    private SOSArgument<String> domain = new SOSArgument<>("domain", false);
    // JS7 new
    private SOSArgument<String> shareName = new SOSArgument<>("share_name", false);
    // JS7 new - GSSAPI : Kerberos,SPNEGO
    private SOSArgument<String> loginContextName = new SOSArgument<>("login_context_name", false);

    public SMBProviderArguments() {
        getProtocol().setDefaultValue(Protocol.SMB);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        String user = getUser().getValue();
        switch (getAuthMethod().getValue()) {
        case KERBEROS:
        case SPNEGO:
        default:
            if (SOSString.isEmpty(user)) {
                user = "[SSO]";
            }
            break;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(getAuthMethod().getValue().name());
        // Kerberos, SPNEGO -> entire output AuthMethod, e.g.: KERBEROS or KERBEROS(CustomKerberosLogin)
        if (!getLoginContextName().isEmpty()) {// not a default
            sb.append("(").append(getLoginContextName().getValue()).append(")");
        }
        sb.append("]");
        sb.append(user).append("@").append(getHost().getValue()).append(":").append(getPort().getValue());
        if (!domain.isEmpty()) {
            sb.append("(").append(domain.getValue()).append(")");
        }
        if (!shareName.isEmpty()) {
            sb.append("\\").append(shareName.getValue());
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

    public SOSArgument<String> getLoginContextName() {
        return loginContextName;
    }
}
