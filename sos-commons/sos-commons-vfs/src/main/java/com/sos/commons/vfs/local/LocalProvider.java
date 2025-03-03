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
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
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
    public boolean createDirectoriesIfNotExist(String path) throws SOSProviderException {
        checkParam("createDirectoriesIfNotExist", path, "path");

        try {
            Path p = getPath(path);
            if (exists(p)) {
                return false;
            }
            Files.createDirectories(p);
            return true;
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        checkParam("deleteIfExists", path, "path");

        try {
            return SOSPath.deleteIfExists(getPath(path));
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    @Override
    public DeleteFilesResult deleteFilesIfExist(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }

        DeleteFilesResult r = new DeleteFilesResult(files.size());
        l: for (String file : files) {
            try {
                Path path = getPath(file);
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

    @Override
    public RenameFilesResult renameFilesIfExist(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        if (files == null) {
            return null;
        }

        RenameFilesResult r = new RenameFilesResult(files.size());
        l: for (Map.Entry<String, String> entry : files.entrySet()) {
            String source = entry.getKey();
            String target = entry.getValue();
            try {
                Path p = getPath(source);
                if (exists(p)) {
                    SOSPath.renameTo(p, getPath(target));
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

    @Override
    public boolean exists(String path) {
        try {
            checkParam("exists", path, "path"); // here because should not throw any errors

            return exists(getPath(path));
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[exists=false]%s", getPathOperationPrefix(path), e.toString());
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

    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        checkParam("getFileIfExists", path, "path");

        Path p = getPath(path);
        ProviderFile file = null;
        try {
            BasicFileAttributes attr = readFileAttributes(p);
            if (attr != null) {
                file = createProviderFile(p.toString(), attr.size(), getFileLastModifiedMillis(attr));
            }
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
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
            throw new SOSProviderException(getPathOperationPrefix(file.getFullPath()), e);
        }
        return file;
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            checkParam("isDirectory", path, "path"); // here because should not throw any errors
            return SOSPath.isDirectory(path);
        } catch (Throwable e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[isDirectory=false]%s", getPathOperationPrefix(path), e.toString());
            }
            return false;
        }
    }

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

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return SOSShell.executeCommand(command, timeout, env);
    }

    @Override
    public SOSCommandResult cancelCommands() {
        return new SOSCommandResult("nop");
    }

    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        try {
            return Files.newInputStream(getPath(path));
            // return new FileInputStream(getPath(path).toFile());
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        try {
            Path p = getPath(path);
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

    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        try {
            Path p = getPath(path);
            if (!exists(p)) {
                return null;
            }
            return SOSPath.readFile(p);
        } catch (IOException e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        try {
            SOSPath.overwrite(getPath(path), content);
        } catch (Throwable e) {
            throw new SOSProviderException(getPathOperationPrefix(path), e);
        }
    }

    @Override
    public String getPathSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteFileSystemPath(path);
    }

    @Override
    public String getDirectoryPath(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return super.getDirectoryPath(SOSPath.toAbsoluteNormalizedPath(path).toString());
    }

    @Override
    public String getDirectoryPathWithTrailingPathSeparator(String path) {
        if (SOSString.isEmpty(path)) {
            return null;
        }
        return super.getDirectoryPathWithTrailingPathSeparator(SOSPath.toAbsoluteNormalizedPath(path).toString());
    }

    private Path getPath(String path) {
        return SOSPath.toAbsoluteNormalizedPath(path);
    }

    private boolean exists(Path path) {
        return Files.exists(path);
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
        } catch (IOException e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

}
