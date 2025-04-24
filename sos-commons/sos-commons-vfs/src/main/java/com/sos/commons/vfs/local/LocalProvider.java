package com.sos.commons.vfs.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.exceptions.ProviderNoSuchFileException;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;

public class LocalProvider extends AProvider<LocalProviderArguments> {

    public LocalProvider(ISOSLogger logger, LocalProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        setAccessInfo(getArguments().getAccessInfo());
    }

    /** Overrides {@link IProvider#getPathSeparator()} */
    @Override
    public String getPathSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtils.isAbsoluteFileSystemPath(path);
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        return toPathStyle(getAbsoluteNormalizedPath(path).toString());
    }

    /** Overrides {@link IProvider#connect())} */
    @Override
    public void connect() throws ProviderConnectException {
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
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        selection = ProviderFileSelection.createIfNull(selection);
        selection.setFileTypeChecker(fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            BasicFileAttributes r = (BasicFileAttributes) fileRepresentator;
            return (r.isRegularFile() && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR)) || (r.isSymbolicLink()
                    && getArguments().getValidFileTypes().getValue().contains(FileType.SYMLINK));
        });

        Path directory = Paths.get(selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory());
        if (!Files.exists(directory)) {
            throw new ProviderNoSuchFileException(getDirectoryNotFoundMsg(directory.toString()));
        }
        try {
            List<ProviderFile> result;
            if (selection.getConfig().isRecursive()) {
                result = selectFilesRecursive(selection, directory);
            } else {
                result = selectFilesNonRecursive(selection, directory);
            }
            return result;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory.toString()), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validateArgument("exists", path, "path");

        try {
            return exists(getAbsoluteNormalizedPath(path));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validateArgument("createDirectoriesIfNotExists", path, "path");

        try {
            Path p = getAbsoluteNormalizedPath(path);
            if (exists(p)) {
                return false;
            }
            Files.createDirectories(p);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validateArgument("deleteIfExists", path, "path");

        try {
            return SOSPath.deleteIfExists(getAbsoluteNormalizedPath(path));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        validateArgument("deleteFileIfExists", path, "path");

        try {
            return Files.deleteIfExists(getAbsoluteNormalizedPath(path));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#renameFileIfSourceExists(String, String)} */
    @Override
    public boolean renameFileIfSourceExists(String source, String target) throws ProviderException {
        validateArgument("renameFileIfSourceExists", source, "source");
        validateArgument("renameFileIfSourceExists", target, "target");

        try {
            Path p = getAbsoluteNormalizedPath(source);
            if (exists(p)) {
                SOSPath.renameTo(p, getAbsoluteNormalizedPath(target));
                return true;
            }
            return false;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validateArgument("getFileIfExists", path, "path");

        try {
            Path normalizedPath = getAbsoluteNormalizedPath(path);
            if (!exists(normalizedPath)) {
                return null;
            }
            return createProviderFile(normalizedPath);
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validateArgument("getFileContentIfExists", path, "path");

        try {
            Path p = getAbsoluteNormalizedPath(path);
            if (!exists(p)) {
                return null;
            }
            return SOSPath.readFile(p);
        } catch (IOException e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String, String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validateArgument("writeFile", path, "path");

        try {
            SOSPath.overwrite(getAbsoluteNormalizedPath(path), content);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String, long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validateArgument("setFileLastModifiedFromMillis", path, "path");
        validateModificationTime(path, milliseconds);

        try {
            SOSPath.setLastModifiedFromMillis(path, milliseconds);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        validateArgument("getInputStream", path, "path");

        try {
            return Files.newInputStream(getAbsoluteNormalizedPath(path));
            // return new FileInputStream(getPath(path).toFile());
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String, boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validateArgument("getOutputStream", path, "path");

        try {
            Path p = getAbsoluteNormalizedPath(path);
            if (append) {
                return Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                return Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            // return new FileOutputStream(getPath(path).toFile(), append);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
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

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {

    }

    private Path getAbsoluteNormalizedPath(String path) {
        return SOSPath.toAbsoluteNormalizedPath(path);
    }

    private boolean exists(Path path) {
        return Files.exists(path);
    }

    private ProviderFile createProviderFile(Path path) throws IOException {
        return createProviderFile(path, readFileAttributes(path));
    }

    private ProviderFile createProviderFile(Path path, BasicFileAttributes attr) throws IOException {
        return createProviderFile(path.toAbsolutePath().normalize().toString(), attr.size(), getFileLastModifiedMillis(attr));
    }

    private BasicFileAttributes readFileAttributes(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class);
    }

    private long getFileLastModifiedMillis(BasicFileAttributes attr) {
        return attr.lastModifiedTime().to(TimeUnit.MILLISECONDS);
    }

    private List<ProviderFile> selectFilesRecursive(ProviderFileSelection selection, Path directory) throws Exception {
        boolean isDebugEnabled = getLogger().isDebugEnabled();
        boolean isTraceEnabled = getLogger().isTraceEnabled();

        List<ProviderFile> result = new ArrayList<>();

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            int counterAdded = 0;

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    if (isDebugEnabled) {
                        getLogger().debug(String.format("%s[skip][preVisitDirectory][maxFiles=%s]exceeded", getPathOperationPrefix(path.toString()),
                                selection.getConfig().getMaxFiles()));
                    }
                    // return result;
                    return FileVisitResult.TERMINATE;
                }
                if (selection.checkDirectory(path.toAbsolutePath().toString())) {
                    return FileVisitResult.CONTINUE;
                }
                if (isDebugEnabled) {
                    getLogger().debug(String.format("%s[preVisitDirectory][match][excludedDirectories=%s]", getPathOperationPrefix(path.toString()),
                            selection.getConfig().getExcludedDirectoriesPattern().pattern()));
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
                        BasicFileAttributes attr = readFileAttributes(path);
                        if (selection.isValidFileType(attr)) {
                            ProviderFile file = createProviderFile(path, attr);
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
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    // TODO write to ProviderFileSelectioResult instead to log
    private List<ProviderFile> selectFilesNonRecursive(ProviderFileSelection selection, Path directory) throws Exception {
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
                        BasicFileAttributes attr = readFileAttributes(path);
                        if (selection.isValidFileType(attr)) {
                            ProviderFile file = createProviderFile(path, attr);
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
            }
        }
        return result;
    }
}
