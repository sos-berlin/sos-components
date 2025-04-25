package com.sos.commons.vfs.ssh.sshj;

public class SSHJRequiredAuthenticationPartialResult {

    private final boolean authenticated;
    private final boolean hadPartialSuccess;

    protected SSHJRequiredAuthenticationPartialResult(boolean authenticated, boolean hadPartialSuccess) {
        this.authenticated = authenticated;
        this.hadPartialSuccess = hadPartialSuccess;
    }

    protected boolean isAuthenticated() {
        return authenticated;
    }

    protected boolean isHadPartialSuccess() {
        return hadPartialSuccess;
    }

    @Override
    public String toString() {
        return "authenticated=" + authenticated + ", hadPartialSuccess=" + hadPartialSuccess;
    }
}
