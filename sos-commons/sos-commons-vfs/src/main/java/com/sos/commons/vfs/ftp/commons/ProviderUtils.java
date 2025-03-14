package com.sos.commons.vfs.ftp.commons;

import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.ftp.FTPProvider;

public class ProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(FTPProvider provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws SOSProviderException {
        int counterAdded = 0;
        try {
            list(provider, selection, directoryPath, result, counterAdded);
        } catch (Throwable e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

    private static int list(FTPProvider provider, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result, int counterAdded)
            throws SOSProviderException {
        try {
            FTPFile[] subDirInfos = provider.getClient().listFiles(directoryPath);
            for (FTPFile subResource : subDirInfos) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    return counterAdded;
                }
                counterAdded = processListEntry(provider, selection, directoryPath, subResource, result, counterAdded);
            }
        } catch (Throwable e) {
            throw new SOSProviderException(e);
        }
        return counterAdded;
    }

    // TODO resource.getName() - path???
    private static int processListEntry(FTPProvider provider, ProviderFileSelection selection, String parentDirectory, FTPFile resource,
            List<ProviderFile> result, int counterAdded) throws SOSProviderException {

        String fullPath = SOSPathUtil.appendPath(parentDirectory, resource.getName());
        if (resource.isDirectory()) {
            if (selection.getConfig().isRecursive()) {
                if (selection.checkDirectory(fullPath)) {
                    counterAdded = list(provider, selection, fullPath, result, counterAdded);
                }
            }
        } else {
            if (selection.checkFileName(resource.getName()) && selection.isValidFileType(resource)) {
                ProviderFile file = provider.createProviderFile(fullPath, resource);
                if (file == null) {
                    if (provider.getLogger().isDebugEnabled()) {
                        provider.getLogger().debug(provider.getPathOperationPrefix(fullPath) + "[skip]" + resource);
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

    public static FTPFile getFTPFile(String caller, FTPClient client, String path) throws Exception {
        if (client == null || path == null) {
            return null;
        }
        FTPFile[] files = client.listFiles(path);
        if (SOSCollection.isEmpty(files)) {
            return null;
        }
        if (files.length > 1) {
            throw new Exception("[the path is ambiguous, more than one file found][" + files.length + "]" + SOSString.join(files));
        }
        return files[0];
    }

    public static void deleteDirectoryFilesRecursively(FTPClient client, String pathSeparator, String path) throws Exception {
        FTPFile[] children = client.listFiles(path);
        if (!SOSCollection.isEmpty(children)) {
            for (FTPFile child : children) {
                String childPath = path + pathSeparator + child.getName();
                if (child.isDirectory()) {
                    deleteDirectoryFilesRecursively(client, pathSeparator, childPath);
                } else {
                    if (!client.deleteFile(childPath)) {
                        throw new Exception(String.format("[failed to delete file][%s]%s", childPath, new FTPProtocolReply(client)));
                    }
                }
            }
        }
    }

    public static boolean isCommandListenerEnvVarSet() {
        String val = System.getenv("AddFTPProtocol");
        return val != null && "true".equalsIgnoreCase(val);
    }

}
