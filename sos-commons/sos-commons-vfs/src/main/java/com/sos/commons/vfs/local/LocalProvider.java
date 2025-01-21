package com.sos.commons.vfs.local;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.exception.SOSProviderInitializationException;
import com.sos.commons.vfs.local.common.LocalProviderArguments;

public class LocalProvider extends AProvider<LocalProviderArguments> {

    public LocalProvider(ISOSLogger logger, LocalProviderArguments arguments) throws SOSProviderInitializationException {
        super(logger, arguments);
        if (getArguments().getCredentialStore() != null) {
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
                getLogger().debug("%s[connect]%s", getTypeInfo(), e.toString());
            }
        }
        getArguments().getUser().setValue(SOSShell.getUsername());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[connected]%s", getTypeInfo(), getMainInfo());
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void disconnect() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("%s[disconnected]%s", getTypeInfo(), getMainInfo());
        }
    }

    @Override
    public void createDirectory(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("%s[createDirectory][%s]try to create...", getTypeInfo(), path);
            }
            Files.createDirectory(SOSPath.toAbsolutePath(path));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[createDirectory][%s]created", getTypeInfo(), path);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(getTypeInfo() + "[createDirectory][" + path + "]", e);
        }
    }

    @Override
    public void createDirectories(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("%s[createDirectories][%s]try to create...", getTypeInfo(), path);
            }
            Files.createDirectories(SOSPath.toAbsolutePath(path));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[createDirectories][%s]created", getTypeInfo(), path);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(getTypeInfo() + "[createDirectories][" + path + "]", e);
        }
    }

    @Override
    public void delete(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            SOSPath.delete(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getTypeInfo() + "[delete][" + path + "]", e);
        }
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            return SOSPath.deleteIfExists(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getTypeInfo() + "[deleteIfExists][" + path + "]", e);
        }
    }

    @Override
    public void rename(String source, String target) throws SOSProviderException {
        checkParam(source, "source");
        checkParam(target, "target");

        try {
            SOSPath.renameTo(source, target);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[rename][source=%s][newpath=%s]renamed", getTypeInfo(), source, target);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(getTypeInfo() + "[rename][source=" + source + "][target=" + target + "]", e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            boolean result = Files.exists(SOSPath.toAbsolutePath(path));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists][%s]%s", getTypeInfo(), path, result);
            }
            return result;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists][%s][false]%s", getTypeInfo(), path, e.toString());
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
                getLogger().debug("%s[isRegularFile][%s]%s", getTypeInfo(), path, result);
            }
            return result;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[isRegularFile][%s][false]%s", getTypeInfo(), path, e.toString());
            }
            return false;
        }
    }

    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkParam(path, "path");

        Path p = SOSPath.toAbsolutePath(path);

        ProviderFile f = null;
        try {
            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
            if (attr.isRegularFile() || attr.isSymbolicLink()) {
                f = new ProviderFile();
                f.setSize(attr.size());
                f.setName(p.getFileName().toString());
                f.setFullName(p.toString());
            }
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            throw new SOSProviderException(getTypeInfo() + "[" + path + "]]", e);
        }
        return f;
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            boolean result = SOSPath.isDirectory(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[isDirectory][%s]%s", getTypeInfo(), path, result);
            }
            return result;
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[isDirectory][%s][false]%s", getTypeInfo(), path, e.toString());
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
                getLogger().debug("%s[getSize][%s]%s", getTypeInfo(), path, result);
            }
            return Long.valueOf(result);
        } catch (Throwable e) {
            throw new SOSProviderException(getTypeInfo() + "[getSize][" + path + "]", e);
        }
    }

    @Override
    public Long getLastModifiedMillis(String path) {
        try {
            checkParam(path, "path");

            long result = SOSPath.getLastModifiedMillis(path);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getLastModifiedMillis][%s]%s", getTypeInfo(), path, result);
            }
            return Long.valueOf(result);
        } catch (Throwable e) {
            getLogger().warn("%s[getLastModifiedMillis][%s]%s", getTypeInfo(), path, e);
            return null;
        }
    }

    @Override
    public boolean setLastModifiedFromMillis(String path, Long milliseconds) {
        if (!isValidModificationTime(milliseconds)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[setLastModifiedFromMillis][%s][%s][false]not valid modification time", getTypeInfo(), path, milliseconds);
            }
            return false;
        }

        try {
            checkParam(path, "path");

            SOSPath.setLastModifiedFromMillis(path, milliseconds.longValue());
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[setLastModifiedFromMillis][%s][%s][false]attr=null", getTypeInfo(), path, milliseconds);
            }
            return true;
        } catch (Throwable e) {
            getLogger().warn("%s[setLastModifiedFromMillis][%s][%s]%s", getTypeInfo(), path, milliseconds, e);
            return false;
        }
    }

    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsolutePathFileSystemStyle(path);
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

    private String getMainInfo() {
        return getArguments().getUser().getDisplayValue() + "@" + getArguments().getHost().getDisplayValue();
    }

}
