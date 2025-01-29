package com.sos.commons.vfs.local;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
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
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteFileSystemPath(path);
    }

    @Override
    public ProviderDirectoryPath getDirectoryPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return new ProviderDirectoryPath(Paths.get(path));
    }

    @Override
    public void connect() throws SOSProviderConnectException {
        try {
            getArguments().getHost().setValue(SOSShell.getHostname());
        } catch (UnknownHostException e) {
            getArguments().getHost().setValue("UNKNOWN_HOST");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[connect]%s", getLogPrefix(), e.toString());
            }
        }
        getArguments().getUser().setValue(SOSShell.getUsername());
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
            Files.createDirectory(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getLogPrefix() + "[createDirectory][" + path + "]", e);
        }
    }

    @Override
    public boolean createDirectoriesIfNotExist(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            Path p = SOSPath.toAbsolutePath(path);
            if (Files.exists(p)) {
                return false;
            }
            Files.createDirectories(p);
            return true;
        } catch (Throwable e) {
            throw new SOSProviderException(getLogPrefix() + "[createDirectoriesIfNotExist][" + path + "]", e);
        }
    }

    @Override
    public void delete(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            SOSPath.delete(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getLogPrefix() + "[delete][" + path + "]", e);
        }
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkParam(path, "path");

        try {
            return SOSPath.deleteIfExists(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getLogPrefix() + "[deleteIfExists][" + path + "]", e);
        }
    }

    @Override
    public void rename(String source, String target) throws SOSProviderException {
        checkParam(source, "source");
        checkParam(target, "target");

        try {
            SOSPath.renameTo(source, target);
        } catch (Throwable e) {
            throw new SOSProviderException(getLogPrefix() + "[rename][source=" + source + "][target=" + target + "]", e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            return Files.exists(SOSPath.toAbsolutePath(path));
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists][%s][false]%s", getLogPrefix(), path, e.toString());
            }
            return false;
        }
    }

    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        selection = ProviderFileSelection.createIfNull(selection);

        Path directory = Paths.get(selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory());
        List<ProviderFile> result;
        if (selection.getConfig().isRecursive()) {
            result = selectFilesRecursive(selection, directory);
        } else {
            result = selectFilesNonRecursive(selection, directory);
        }
        return result;
    }

    private List<ProviderFile> selectFilesRecursive(ProviderFileSelection selection, Path directory) throws SOSProviderException {
        boolean isDebugEnabled = getLogger().isDebugEnabled();
        boolean isTraceEnabled = getLogger().isTraceEnabled();

        List<ProviderFile> result = new ArrayList<>();

        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                int counterAdded = 0;

                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                    if (selection.maxFilesExceeded(counterAdded)) {
                        if (isDebugEnabled) {
                            getLogger().debug(String.format("[%s][skip][preVisitDirectory][maxFiles=%s]exceeded", path, selection.getConfig()
                                    .getMaxFiles()));
                        }
                        // return result;
                        return FileVisitResult.TERMINATE;
                    }
                    if (selection.checkDirectory(path.toAbsolutePath().toString())) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (isDebugEnabled) {
                        getLogger().debug(String.format("[%s][preVisitDirectory][match][excludedDirectories=%s]", path, selection.getConfig()
                                .getExcludedDirectoriesPattern().pattern()));
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        if (selection.maxFilesExceeded(counterAdded)) {
                            if (isDebugEnabled) {
                                getLogger().debug(String.format("[%s][skip][preVisitDirectory][maxFiles=%s]exceeded", path, selection.getConfig()
                                        .getMaxFiles()));
                            }
                            // return result;
                            return FileVisitResult.TERMINATE;
                        }
                        if (isTraceEnabled) {
                            getLogger().trace(String.format("[%s][visitFile]", path));
                        }
                        String fileName = path.getFileName().toString();
                        if (selection.checkFileName(fileName)) {
                            ProviderFile file = createProviderFile(path);
                            if (selection.checkProviderFile(file)) {
                                counterAdded++;
                                result.add(file);
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

    // TODO write to ProviderFileSelectioResult instead to log
    private List<ProviderFile> selectFilesNonRecursive(ProviderFileSelection selection, Path directory) throws SOSProviderException {
        boolean isDebugEnabled = getLogger().isDebugEnabled();
        boolean isTraceEnabled = getLogger().isTraceEnabled();

        List<ProviderFile> result = new ArrayList<>();
        int counterAdded = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    if (isDebugEnabled) {
                        getLogger().debug(String.format("[%s][skip][preVisitDirectory][maxFiles=%s]exceeded", path, selection.getConfig()
                                .getMaxFiles()));
                    }
                    // return FileVisitResult.TERMINATE;
                    return result;
                }
                if (!Files.isDirectory(path)) {
                    if (isTraceEnabled) {
                        getLogger().trace(String.format("[%s]", path));
                    }
                    String fileName = path.getFileName().toString();
                    if (selection.checkFileName(fileName)) {
                        ProviderFile file = createProviderFile(path);
                        if (selection.checkProviderFile(file)) {
                            counterAdded++;
                            result.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkParam(path, "path");

        Path p = SOSPath.toAbsolutePath(path);

        ProviderFile file = null;
        try {
            BasicFileAttributes attr = readFileAttributes(p);
            if (attr != null) {
                file = createProviderFile(p.toString(), attr.size(), getFileLastModifiedMillis(attr));
            }
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            throw new SOSProviderException(getLogPrefix() + "[" + path + "]]", e);
        }
        return file;
    }

    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        try {
            BasicFileAttributes attr = readFileAttributes(Paths.get(file.getFullPath()));
            if (attr != null) {
                file.setSize(attr.size());
                file.setLastModifiedMillis(getFileLastModifiedMillis(attr));
            } else {
                // file = null; ???
            }
        } catch (NoSuchFileException e) {
            file = null;
        } catch (IOException e) {
            throw new SOSProviderException(getLogPrefix() + "[" + file.getFullPath() + "]]", e);
        }
        return file;
    }

    private ProviderFile createProviderFile(Path path) throws IOException {
        Path np = path.toAbsolutePath().normalize();
        BasicFileAttributes attr = readFileAttributes(np);
        if (attr == null) {
            return null;
        }
        return createProviderFile(np.toString(), attr.size(), getFileLastModifiedMillis(attr));
    }

    private BasicFileAttributes readFileAttributes(Path path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        if (attr.isRegularFile() || attr.isSymbolicLink()) {
            return attr;
        }
        return null;
    }

    private long getFileLastModifiedMillis(BasicFileAttributes attr) {
        return attr.lastModifiedTime().to(TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            checkParam(path, "path"); // here because should not throw any errors

            return SOSPath.isDirectory(path);
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[isDirectory][%s][false]%s", getLogPrefix(), path, e.toString());
            }
            return false;
        }
    }

    // @Override
    // public long getFileSize(String path) throws SOSProviderException {
    // checkParam(path, "path");

    // try {
    // long result = SOSPath.getFileSize(path);
    // if (getLogger().isDebugEnabled()) {
    // getLogger().debug("%s[getFileSize][%s]%s", getLogPrefix(), path, result);
    // }
    // return result;
    // } catch (Throwable e) {
    // throw new SOSProviderException(getLogPrefix() + "[getFileSize][" + path + "]", e);
    // }
    // }

    // @Override
    // public long getFileLastModifiedMillis(String path) {
    // try {
    // checkParam(path, "path");

    // long result = SOSPath.getLastModifiedMillis(path);
    // if (getLogger().isDebugEnabled()) {
    // getLogger().debug("%s[getFileLastModifiedMillis][%s]%s", getLogPrefix(), path, result);
    // }
    // return result;
    // } catch (Throwable e) {
    // getLogger().warn("%s[getFileLastModifiedMillis][%s]%s", getLogPrefix(), path, e);
    // return DEFAULT_FILE_ATTR_VALUE;
    // }
    // }

    @Override
    public boolean setFileLastModifiedFromMillis(String path, long milliseconds) {
        if (!isValidModificationTime(milliseconds)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[setFileLastModifiedFromMillis][%s][%s][false]not valid modification time", getLogPrefix(), path, milliseconds);
            }
            return false;
        }

        try {
            checkParam(path, "path");

            SOSPath.setLastModifiedFromMillis(path, milliseconds);
            return true;
        } catch (Throwable e) {
            getLogger().warn("%s[setFileLastModifiedFromMillis][%s][%s]%s", getLogPrefix(), path, milliseconds, e);
            return false;
        }
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return SOSShell.executeCommand(command, timeout, env);
    }

    @Override
    public SOSCommandResult cancelCommands() {
        return new SOSCommandResult("nop");
    }

    // private String getMainInfo() {
    // return getArguments().getUser().getDisplayValue() + "@" + getArguments().getHost().getDisplayValue();
    // }

}
