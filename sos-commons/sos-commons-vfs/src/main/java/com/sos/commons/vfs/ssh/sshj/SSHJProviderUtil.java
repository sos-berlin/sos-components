package com.sos.commons.vfs.ssh.sshj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderException;

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProviderUtil {

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
            KeepAlive r = ssh.getConnection().getKeepAlive();
            if (r.getKeepAliveInterval() > 0) {
                if (r instanceof KeepAliveRunner) {
                    msg.add("KeepAliveInterval=" + r.getKeepAliveInterval() + "s, MaxAliveCount=" + ((KeepAliveRunner) r).getMaxAliveCount());
                } else {
                    msg.add("KeepAliveInterval=" + r.getKeepAliveInterval() + "s");
                }
            }
        }
        return msg;
    }

    protected static long getFileLastModifiedMillis(FileAttributes attr) {
        // getMtime is in seconds
        return attr.getMtime() * 1_000L;
    }

    protected static void delete(SFTPClient sftp, String path) throws Exception {
        try {
            path = sftp.canonicalize(path);
        } catch (SFTPException e) {
            SSHJProviderUtil.throwException(e, path);
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

    /** Deletes all files and sub folders. */
    private static void deleteDirectories(SFTPClient sftp, String path) throws Exception {
        final Deque<RemoteResourceInfo> toRemove = new LinkedList<RemoteResourceInfo>();
        SSHJProviderUtil.dirInfo(sftp, path, toRemove, true);
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

    protected static boolean exists(SFTPClient sftp, String path) throws IOException {
        return sftp.statExistence(path) != null;
    }

    // possible recursion
    protected static List<ProviderFile> selectFiles(SSHJProviderImpl provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws SOSProviderException {
        int counterAdded = 0;
        try (SFTPClient sftp = provider.getSSHClient().newSFTPClient()) {
            list(provider, sftp, selection, directoryPath, result, counterAdded);
        } catch (Throwable e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

    private static int list(SSHJProviderImpl provider, SFTPClient sftp, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result,
            int counterAdded) throws SOSProviderException {
        try {
            List<RemoteResourceInfo> subDirInfos = sftp.ls(directoryPath);
            for (RemoteResourceInfo subResource : subDirInfos) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    return counterAdded;
                }
                counterAdded = processListEntry(provider, sftp, selection, subResource, result, counterAdded);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(e);
        }
        return counterAdded;
    }

    private static int processListEntry(SSHJProviderImpl provider, SFTPClient sftp, ProviderFileSelection selection, RemoteResourceInfo resource,
            List<ProviderFile> result, int counterAdded) throws SOSProviderException {
        if (resource.isDirectory()) {
            if (selection.getConfig().isRecursive()) {
                if (selection.checkDirectory(resource.getPath())) {
                    counterAdded = list(provider, sftp, selection, resource.getPath(), result, counterAdded);
                }
            }
        } else {
            String fileName = resource.getName();
            if (selection.checkFileName(fileName)) {
                ProviderFile file = provider.createProviderFile(fileName, resource.getAttributes());
                if (file == null) {
                    if (provider.getLogger().isDebugEnabled()) {
                        provider.getLogger().debug(provider.getPathOperationPrefix(fileName) + "[skip]" + toString(resource.getAttributes()));
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

    protected static void put(SFTPClient sftp, String source, String target) throws IOException {
        sftp.put(new FileSystemFile(source), target);
    }

    protected static void rename(SFTPClient sftp, String sourcePath, String targetPath) throws Exception {
        try {
            sourcePath = sftp.canonicalize(sourcePath);
        } catch (SFTPException e) {
            SSHJProviderUtil.throwException(e, sourcePath);
        }
        sftp.rename(sourcePath, targetPath);
    }

    public static String toString(FileAttributes attr) {
        if (attr == null) {
            return null;
        }
        return "type=" + attr.getType() + "," + attr.toString();
    }

}
