package com.sos.commons.vfs.local;

import java.net.UnknownHostException;
import java.nio.file.Files;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.local.common.LocalProviderArguments;

public class LocalProvider extends AProvider<LocalProviderArguments> {

    public LocalProvider(ISOSLogger logger, LocalProviderArguments arguments, CredentialStoreArguments csArgs) {
        super(logger, arguments, csArgs);
        if (csArgs != null) {
            // e.g. see SSHProvider
        }
    }

    @Override
    public void connect() throws SOSProviderConnectException {
        try {
            getArguments().getHost().setValue(SOSShell.getHostname());
        } catch (UnknownHostException e) {
            getArguments().getHost().setValue("UNKNOWN_HOST");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[connect]%s", e.toString());
            }
        }
        getArguments().getUser().setValue(null);// TODO
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void createDirectory(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("[createDirectory][%s]try to create...", path);
            }
            Files.createDirectory(SOSPath.toAbsolutePath(path));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[createDirectory][%s]created", path);
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[createDirectory][" + path + "]", e);
        }
    }

    @Override
    public void createDirectories(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("[createDirectories][%s]try to create...", path);
            }
            Files.createDirectories(SOSPath.toAbsolutePath(path));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[createDirectories][%s]created", path);
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[createDirectories][" + path + "]", e);
        }
    }

    @Override
    public void delete(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            SOSPath.delete(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException("[delete][" + path + "]", e);
        }
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            return SOSPath.deleteIfExists(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException("[deleteIfExists][" + path + "]", e);
        }
    }

    @Override
    public void rename(String source, String target) throws SOSProviderException {
        checkParam(source, "source");
        checkParam(target, "target");

        try {
            SOSPath.renameTo(source, target);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[rename][source=%s][newpath=%s]renamed", source, target);
            }
        } catch (Throwable e) {
            throw new SOSProviderException("[rename][source=" + source + "][target=" + target + "]", e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            boolean result = Files.exists(SOSPath.toAbsolutePath(path));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[exists][%s]%s", path, result);
            }
            return result;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[exists][%s][false]%s", path, e.toString());
            }
            return false;
        }
    }

    @Override
    public boolean isRegularFile(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            boolean result = SOSPath.isRegularFile(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[isRegularFile][%s]%s", path, result);
            }
            return result;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[isRegularFile][%s][false]%s", path, e.toString());
            }
            return false;
        }
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            boolean result = SOSPath.isDirectory(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[isDirectory][%s]%s", path, result);
            }
            return result;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[isDirectory][%s][false]%s", path, e.toString());
            }
            return false;
        }
    }

    @Override
    public Long getSize(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            long result = SOSPath.getSize(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getSize][%s]%s", path, result);
            }
            return Long.valueOf(result);
        } catch (Throwable e) {
            throw new SOSProviderException("[getSize][" + path + "]", e);
        }
    }

    @Override
    public Long getLastModifiedMillis(String path) {
        try {
            checkParam(path, "path");

            long result = SOSPath.getLastModifiedMillis(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[getLastModifiedMillis][%s]%s", path, result);
            }
            return Long.valueOf(result);
        } catch (Throwable e) {
            getLogger().warn("[getLastModifiedMillis][%s]%s", path, e);
            return null;
        }
    }

    @Override
    public boolean setLastModifiedFromMillis(String path, Long milliseconds) {
        if (!isValidModificationTime(milliseconds)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[setLastModifiedFromMillis][%s][%s][false]not valid modification time", path, milliseconds);
            }
            return false;
        }

        try {
            checkParam(path, "path");

            SOSPath.setLastModifiedFromMillis(path, milliseconds.longValue());
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("[setLastModifiedFromMillis][%s][%s][false]attr=null", path, milliseconds);
            }
            return true;
        } catch (Throwable e) {
            getLogger().warn("[setLastModifiedFromMillis][%s][%s]%s", path, milliseconds, e);
            return false;
        }
    }

    @Override
    public SOSCommandResult executeCommand(String command) {
        return SOSShell.executeCommand(command);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout) {
        return SOSShell.executeCommand(command, timeout);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSEnv env) {
        return SOSShell.executeCommand(command, env);
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return SOSShell.executeCommand(command, timeout, env);
    }

    @Override
    public SOSCommandResult cancelCommands() {
        return new SOSCommandResult("nop");
    }

}
