package com.sos.commons.vfs.common;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.vfs.common.AProviderArguments;

public abstract class AProvider<A extends AProviderArguments> {

    private final A arguments;

    public AProvider(A arguments) {
        this.arguments = arguments;
    }

    public abstract void connect() throws Exception;

    public abstract boolean isConnected();

    public abstract void disconnect();

    public abstract void mkdir(String path) throws Exception;

    public abstract void rmdir(String path) throws Exception;

    public abstract boolean fileExists(String path);

    public abstract boolean directoryExists(String path);

    public abstract long getSize(String path) throws Exception;

    public abstract long getModificationTime(String path) throws Exception;

    public abstract SOSCommandResult executeCommand(String command);

    public abstract SOSCommandResult executeCommand(String command, SOSTimeout timeout);

    public abstract SOSCommandResult executeCommand(String command, SOSEnv env);

    public abstract SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env);

    public A getArguments() {
        return arguments;
    }

}
