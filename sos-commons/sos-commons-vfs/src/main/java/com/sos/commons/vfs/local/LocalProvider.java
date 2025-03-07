package com.sos.commons.vfs.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;

public class LocalProvider extends AProvider<LocalProviderArguments> {

    public LocalProvider(ISOSLogger logger, LocalProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args, fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            BasicFileAttributes a = (BasicFileAttributes) fileRepresentator;
            return (a.isRegularFile() && args.getValidFileTypes().getValue().contains(FileType.REGULAR)) || (a.isSymbolicLink() && args
                    .getValidFileTypes().getValue().contains(FileType.SYMLINK));
        });
        if (getArguments().getCredentialStore() != null) {
            // e.g. see SSHProvider
        }
        setAccessInfo();
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteFileSystemPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        return toPathStyle(getAbsoluteNormalizedPath(path).toString());
    }

    /** Overrides {@link IProvider#connect())} */
    @Override
    public void connect() throws SOSProviderConnectException {
        getLogger().info(getAccessInfo());
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        return true;
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {

    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
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

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) {
        try {
            checkParam("exists", path, "path"); // here because should not throw any errors

            return exists(getAbsoluteNormalizedPath(path));
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists=false]%s", getPathOperationPrefix(path), e.toString());
            }
            return false;
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        checkParam("createDirectoriesIfNotExists", path, "path");

        try {
            Path p = getAbsoluteNormalizedPath(path);
            if (exists(p)) {
                return false;
            }
            Files.createDirectories(p);
            return true;
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkParam("deleteIfExists", path, "path");

        try {
            return SOSPath.deleteIfExists(getAbsoluteNormalizedPath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }

        DeleteFilesResult r = new DeleteFilesResult(files.size());
        l: for (String file : files) {
            try {
                Path path = getAbsoluteNormalizedPath(file);
                if (exists(path)) {
                    SOSPath.delete(path);
                    r.addSuccess();
                } else {
                    r.addNotFound(file);
                }
            } catch (Throwable e) {
                r.addError(file, e);
                if (stopOnSingleFileError) {
                    break l;
                }
            }
        }
        return r;
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }

        RenameFilesResult r = new RenameFilesResult(files.size());
        l: for (Map.Entry<String, String> entry : files.entrySet()) {
            String source = entry.getKey();
            String target = entry.getValue();
            try {
                Path p = getAbsoluteNormalizedPath(source);
                if (exists(p)) {
                    SOSPath.renameTo(p, getAbsoluteNormalizedPath(target));
                    r.addSuccess(source, target);
                } else {
                    r.addNotFound(source);
                }
            } catch (Throwable e) {
                r.addError(source, e);
                if (stopOnSingleFileError) {
                    break l;
                }
            }
        }
        return r;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkParam("getFileIfExists", path, "path");

        try {
            return createProviderFile(getAbsoluteNormalizedPath(path));
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#rereadFileIfExists(ProviderFile)} */
    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        try {
            return refreshFileMetadata(file, createProviderFile(getAbsoluteNormalizedPath(file.getFullPath())));
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        checkParam("getFileContentIfExists", path, "path");

        try {
            Path p = getAbsoluteNormalizedPath(path);
            if (!exists(p)) {
                return null;
            }
            return SOSPath.readFile(p);
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        checkParam("writeFile", path, "path");

        try {
            SOSPath.overwrite(getAbsoluteNormalizedPath(path), content);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        checkParam("setFileLastModifiedFromMillis", path, "path");
        checkModificationTime(path, milliseconds);

        try {
            SOSPath.setLastModifiedFromMillis(path, milliseconds);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        checkParam("getInputStream", path, "path");

        try {
            return Files.newInputStream(getAbsoluteNormalizedPath(path));
            // return new FileInputStream(getPath(path).toFile());
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        checkParam("getOutputStream", path, "path");

        try {
            Path p = getAbsoluteNormalizedPath(path);
            if (append) {
                return Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                return Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            // return new FileOutputStream(getPath(path).toFile(), append);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return SOSShell.executeCommand(command, timeout, env);
    }

    /** Overrides {@link IProvider#cancelCommands()} */
    @Override
    public SOSCommandResult cancelCommands() {
        return new SOSCommandResult("nop");
    }

    private Path getAbsoluteNormalizedPath(String path) {
        return SOSPath.toAbsoluteNormalizedPath(path);
    }

    private boolean exists(Path path) {
        return Files.exists(path);
    }

    private ProviderFile createProviderFile(Path path) throws IOException {
        Path np = path.toAbsolutePath().normalize();
        BasicFileAttributes attr = readFileAttributes(np);
        if (isValidFileType(attr)) {
            return createProviderFile(np.toString(), attr.size(), getFileLastModifiedMillis(attr));
        }
        return null;
    }

    private BasicFileAttributes readFileAttributes(Path path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        if (isValidFileType(attr)) {
            return attr;
        }
        return null;
    }

    private long getFileLastModifiedMillis(BasicFileAttributes attr) {
        return attr.lastModifiedTime().to(TimeUnit.MILLISECONDS);
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
                            getLogger().debug(String.format("%s[skip][preVisitDirectory][maxFiles=%s]exceeded", getPathOperationPrefix(path
                                    .toString()), selection.getConfig().getMaxFiles()));
                        }
                        // return result;
                        return FileVisitResult.TERMINATE;
                    }
                    if (selection.checkDirectory(path.toAbsolutePath().toString())) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (isDebugEnabled) {
                        getLogger().debug(String.format("%s[preVisitDirectory][match][excludedDirectories=%s]", getPathOperationPrefix(path
                                .toString()), selection.getConfig().getExcludedDirectoriesPattern().pattern()));
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        if (selection.maxFilesExceeded(counterAdded)) {
                            if (isDebugEnabled) {
                                getLogger().debug(String.format("%s[skip][preVisitDirectory][maxFiles=%s]exceeded", getPathOperationPrefix(path
                                        .toString()), selection.getConfig().getMaxFiles()));
                            }
                            // return result;
                            return FileVisitResult.TERMINATE;
                        }
                        if (isTraceEnabled) {
                            getLogger().trace(String.format("%s[visitFile]", getPathOperationPrefix(path.toString())));
                        }
                        String fileName = path.getFileName().toString();
                        if (selection.checkFileName(fileName)) {
                            ProviderFile file = createProviderFile(path);
                            if (file != null) {
                                if (selection.checkProviderFileMinMaxSize(file)) {
                                    counterAdded++;
                                    file.setIndex(counterAdded);
                                    result.add(file);

                                    if (isDebugEnabled) {
                                        getLogger().debug(getPathOperationPrefix(path.toString()) + "added");
                                    }
                                }
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

        if (isDebugEnabled) {
            getLogger().debug(SOSString.toString(selection.getConfig(), true));
        }

        List<ProviderFile> result = new ArrayList<>();
        int counterAdded = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    if (isDebugEnabled) {
                        getLogger().debug(String.format("%s[skip][preVisitDirectory][maxFiles=%s]exceeded", getPathOperationPrefix(path.toString()),
                                selection.getConfig().getMaxFiles()));
                    }
                    // return FileVisitResult.TERMINATE;
                    return result;
                }
                if (!Files.isDirectory(path)) {
                    if (isTraceEnabled) {
                        getLogger().trace(getPathOperationPrefix(path.toString()));
                    }
                    String fileName = path.getFileName().toString();
                    if (selection.checkFileName(fileName)) {
                        ProviderFile file = createProviderFile(path);
                        if (file != null) {
                            if (selection.checkProviderFileMinMaxSize(file)) {
                                counterAdded++;
                                file.setIndex(counterAdded);
                                result.add(file);

                                if (isDebugEnabled) {
                                    getLogger().debug(getPathOperationPrefix(path.toString()) + "added");
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

    private void setAccessInfo() {
        try {
            getArguments().getHost().setValue(SOSShell.getHostname());
        } catch (UnknownHostException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[setHostname]%s", getLogPrefix(), e.toString());
            }
        }
        getArguments().getUser().setValue(SOSShell.getUsername());
        setAccessInfo(String.format("%s@%s", getArguments().getUser().getDisplayValue(), getArguments().getHost().getDisplayValue()));
    }

}
