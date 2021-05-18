package com.sos.commons.vfs.ssh.exception;

import com.sos.commons.exception.SOSException;

import net.schmizz.sshj.connection.channel.direct.Signal;

public class SOSSSHCommandExitViolentlyException extends SOSException {

    private static final long serialVersionUID = 1L;
    private final Signal signal;

    public SOSSSHCommandExitViolentlyException(Signal signal, String msg) {
        super(String.format("[%s]%s", signal, msg));
        this.signal = signal;
    }

    public Signal getSignal() {
        return signal;
    }
}
