package com.sos.commons.vfs.ssh.sshj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.RenameFlags;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProviderUtils {

    protected static boolean is(ISOSLogger logger, String logPrefix, SFTPClient sftp, String path, FileMode.Type type) {
        try {
            FileAttributes attr = sftp.stat(path);
            if (attr != null) {
                return type.equals(attr.getType());
            }
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("%s[is][%s][type=%s]%s", logPrefix, path, type, e.toString());
            }
        }
        return false;
    }

    protected static void throwException(SFTPException e, String msg) throws Exception {
        StatusCode sc = e.getStatusCode();
        if (sc != null) {
            if (sc.equals(StatusCode.NO_SUCH_FILE) || sc.equals(StatusCode.NO_SUCH_PATH)) {
                throw new SOSNoSuchFileException(msg, e);
            }
        }
        throw e;
    }

    protected static void dirInfo(SFTPClient sftp, String path, Deque<RemoteResourceInfo> result, boolean recursive) throws Exception {
        List<RemoteResourceInfo> infos = sftp.ls(path);// SFTPException: No such file
        for (RemoteResourceInfo resource : infos) {
            result.push(resource);
            if (recursive && resource.isDirectory()) {
                dirInfo(sftp, resource.getPath(), result, recursive);
            }
        }
    }

    protected static List<String> getConnectedInfos(SSHClient ssh) {
        List<String> msg = new ArrayList<String>();
        if (ssh.getTimeout() > 0 || ssh.getConnectTimeout() > 0) {
            msg.add("ConnectTimeout=" + AProvider.millis2string(ssh.getConnectTimeout()) + ", SocketTimeout=" + AProvider.millis2string(ssh
                    .getTimeout()));
        }
        if (ssh.getConnection() != null) {
            KeepAlive ka = ssh.getConnection().getKeepAlive();
            if (ka.getKeepAliveInterval() > 0) {
                if (ka instanceof KeepAliveRunner) {
                    msg.add("KeepAliveInterval=" + ka.getKeepAliveInterval() + "s, MaxAliveCount=" + ((KeepAliveRunner) ka).getMaxAliveCount());
                } else {
                    msg.add("KeepAliveInterval=" + ka.getKeepAliveInterval() + "s");
                }
            }
        }
        return msg;
    }

    protected static long getFileLastModifiedMillis(FileAttributes attr) {
        // getMtime is in seconds
        return attr.getMtime() * 1_000L;
    }

    /** @param sftp
     * @param path
     * @throws Exception can throw SOSNoSuchFileException */
    protected static void delete(SFTPClient sftp, String path) throws Exception {
        try {
            path = sftp.canonicalize(path);
        } catch (SFTPException e) {
            throwException(e, path);
        }
        FileAttributes attr = sftp.stat(path);
        switch (attr.getType()) {
        case DIRECTORY:
            deleteDirectories(sftp, path);
            break;
        case REGULAR:
            sftp.rm(path);
            break;
        // case SYMLINK:
        default:
            break;
        }
    }

    protected static void deleteFile(SFTPClient sftp, String path) throws Exception {
        try {
            path = sftp.canonicalize(path);
        } catch (SFTPException e) {
            throwException(e, path);
        }
        try {
            sftp.rm(path);
        } catch (SFTPException e) {
            throwException(e, path);
        }
    }

    protected static boolean exists(SFTPClient sftp, String path) throws IOException {
        return sftp.statExistence(path) != null;
    }

    // possible recursion
    protected static List<ProviderFile> selectFiles(SSHJProvider provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws Exception {
        int counterAdded = 0;
        try (SFTPClient sftp = provider.getSSHClient().newSFTPClient()) {
            list(provider, sftp, selection, directoryPath, result, counterAdded);
        }
        return result;
    }

    protected static void put(SFTPClient sftp, String source, String target) throws IOException {
        sftp.put(new FileSystemFile(source), target);
    }

    protected static void rename(SFTPClient sftp, String sourcePath, String targetPath) throws Exception {
        try {
            sourcePath = sftp.canonicalize(sourcePath);
        } catch (SFTPException e) {
            throwException(e, sourcePath);
        }
        // sftp.rename(sourcePath, targetPath);
        sftp.rename(sourcePath, targetPath, Set.of(RenameFlags.OVERWRITE));
    }

    protected static void setFileLastModifiedFromMillis(SFTPClient sftp, String path, long milliseconds) throws IOException {
        FileAttributes attr = sftp.stat(path);
        long seconds = milliseconds / 1_000L;
        FileAttributes newAttr = new FileAttributes.Builder().withAtimeMtime(attr.getAtime(), seconds).build();
        sftp.setattr(path, newAttr);

    }

    protected static String getFileContentIfExists(SFTPClient sftp, String path) throws IOException {
        if (!SSHJProviderUtils.exists(sftp, path)) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (RemoteFile file = sftp.open(path); InputStream is = file.new RemoteFileInputStream(0)) {
            byte[] buffer = new byte[8_192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    protected static void uploadContent(SFTPClient sftp, String path, String content) throws IOException {
        EnumSet<OpenMode> mode = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (RemoteFile remoteFile = sftp.open(path, mode)) {
            remoteFile.write(0, bytes, 0, bytes.length);
        }
    }

    protected static String toString(FileAttributes attr) {
        if (attr == null) {
            return null;
        }
        return "type=" + attr.getType() + "," + attr.toString();
    }

    /** Deletes all files and sub folders. */
    private static void deleteDirectories(SFTPClient sftp, String path) throws Exception {
        final Deque<RemoteResourceInfo> toRemove = new LinkedList<RemoteResourceInfo>();
        dirInfo(sftp, path, toRemove, true);
        while (!toRemove.isEmpty()) {
            RemoteResourceInfo resource = toRemove.pop();
            if (resource.isDirectory()) {
                sftp.rmdir(resource.getPath());
            } else if (resource.isRegularFile()) {
                sftp.rm(resource.getPath());
            }
        }
        sftp.rmdir(path);
    }

    private static int list(SSHJProvider provider, SFTPClient sftp, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result,
            int counterAdded) throws Exception {
        List<RemoteResourceInfo> subDirInfos = sftp.ls(directoryPath);
        for (RemoteResourceInfo subResource : subDirInfos) {
            if (selection.maxFilesExceeded(counterAdded)) {
                return counterAdded;
            }
            counterAdded = processListEntry(provider, sftp, selection, subResource, result, counterAdded);
        }
        return counterAdded;
    }

    private static int processListEntry(SSHJProvider provider, SFTPClient sftp, ProviderFileSelection selection, RemoteResourceInfo resource,
            List<ProviderFile> result, int counterAdded) throws Exception {
        if (resource.isDirectory()) {
            if (selection.getConfig().isRecursive()) {
                if (selection.checkDirectory(resource.getPath())) {
                    counterAdded = list(provider, sftp, selection, resource.getPath(), result, counterAdded);
                }
            }
        } else {
            if (selection.checkFileName(resource.getName()) && selection.isValidFileType(resource.getAttributes())) {
                ProviderFile file = provider.createProviderFile(resource.getPath(), resource.getAttributes());
                if (file == null) {
                    if (provider.getLogger().isDebugEnabled()) {
                        provider.getLogger().debug(provider.getPathOperationPrefix(resource.getPath()) + "[skip]" + toString(resource
                                .getAttributes()));
                    }
                } else {
                    if (selection.checkProviderFileMinMaxSize(file)) {
                        counterAdded++;

                        file.setIndex(counterAdded);
                        result.add(file);

                        if (provider.getLogger().isDebugEnabled()) {
                            provider.getLogger().debug(provider.getPathOperationPrefix(file.getFullPath()) + "added");
                        }
                    }
                }
            }
        }
        return counterAdded;
    }

}
