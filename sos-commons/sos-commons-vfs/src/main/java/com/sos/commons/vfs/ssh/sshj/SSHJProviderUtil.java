package com.sos.commons.vfs.ssh.sshj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.ProviderFileBuilder;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SSHJProviderUtil {

    protected static KeyProvider getKeyProviderFromKeepass(SSHClient sshClient, SSHProviderArguments args) throws Exception {
        SOSKeePassDatabase kd = (SOSKeePassDatabase) args.getKeepassDatabase();
        if (kd == null) {
            throw new Exception("[keepass]keepass_database property is null");
        }
        org.linguafranca.pwdb.Entry<?, ?, ?, ?> ke = args.getKeepassDatabaseEntry();
        if (ke == null) {
            throw new Exception(String.format("[keepass][can't find database entry]attachment property name=%s", args
                    .getKeepassAttachmentPropertyName()));
        }
        try {
            String pk = new String(kd.getAttachment(ke, args.getKeepassAttachmentPropertyName()), "UTF-8");
            return sshClient.loadKeys(pk, null, SOSString.isEmpty(args.getPassphrase().getValue()) ? null : getPasswordFinder(args.getPassphrase()
                    .getValue()));
        } catch (Exception e) {
            String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + args.getKeepassAttachmentPropertyName();
            throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
        }
    }

    protected static PasswordFinder getPasswordFinder(String password) {
        return new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return password.toCharArray().clone();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }

        };
    }

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

    protected static boolean isFileType(Type t) {
        return FileMode.Type.REGULAR.equals(t) || FileMode.Type.SYMLINK.equals(t);
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
    protected static List<ProviderFile> selectFiles(SSHClient ssh, Function<ProviderFileBuilder, ProviderFile> providerFileCreator,
            ProviderFileSelection selection, String directoryPath, int counterAdded) throws SOSProviderException {
        List<ProviderFile> recursiveResult = new ArrayList<>();
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            List<RemoteResourceInfo> subDirInfos = sftp.ls(directoryPath);
            for (RemoteResourceInfo subResource : subDirInfos) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    return recursiveResult;
                }
                processRemoteResource(ssh, providerFileCreator, selection, subResource, counterAdded, recursiveResult);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(e);
        }
        return recursiveResult;
    }

    private static void processRemoteResource(SSHClient ssh, Function<ProviderFileBuilder, ProviderFile> providerFileCreator,
            ProviderFileSelection selection, RemoteResourceInfo resource, int counterAdded, List<ProviderFile> result) throws SOSProviderException {
        if (resource.isDirectory()) {
            if (selection.getConfig().isRecursive()) {
                if (selection.checkDirectory(resource.getPath())) {
                    List<ProviderFile> recursiveFiles = selectFiles(ssh, providerFileCreator, selection, resource.getPath(), counterAdded);
                    result.addAll(recursiveFiles);
                }
            }
        } else {
            FileAttributes attr = resource.getAttributes();
            if (attr != null && SSHJProviderUtil.isFileType(attr.getType())) {
                String fileName = resource.getName();
                if (selection.checkFileName(fileName)) {
                    ProviderFile file = AProvider.createProviderFile(providerFileCreator, resource.getPath(), attr.getSize(), SSHJProviderUtil
                            .getFileLastModifiedMillis(attr));
                    if (selection.checkProviderFileMinMaxSize(file)) {
                        counterAdded++;
                        result.add(file);
                    }
                }
            }
        }
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

}
