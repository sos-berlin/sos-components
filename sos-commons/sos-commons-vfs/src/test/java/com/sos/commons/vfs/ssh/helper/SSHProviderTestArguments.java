package com.sos.commons.vfs.ssh.helper;

import java.util.Arrays;

import com.sos.commons.vfs.ssh.common.SSHProviderArguments;

public class SSHProviderTestArguments extends SSHProviderArguments {

    public void setProtocol(Protocol val) {
        getProtocol().setValue(val);
    }

    public void setHost(String val) {
        getHost().setValue(val);
    }

    public void setPort(int val) {
        getPort().setValue(val);
    }

    public void setUser(String val) {
        getUser().setValue(val);
    }

    public void setPassword(String val) {
        getPassword().setValue(val);
    }

    public void setAuthMethod(AuthMethod val) {
        getAuthMethod().setValue(val);
    }

    public void setAuthFile(String val) {
        getAuthFile().setValue(val);
    }

    public void setPreferredAuthentications(AuthMethod... val) {
        getPreferredAuthentications().setValue(Arrays.<AuthMethod> asList(val));
    }

    public void setRequiredAuthentications(AuthMethod... val) {
        getRequiredAuthentications().setValue(Arrays.<AuthMethod> asList(val));
    }
    
    public void setSimulateShell(Boolean val) {
        getSimulateShell().setValue(val);
    }
}
