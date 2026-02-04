package com.sos.commons.vfs.ftp.commons;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.ftp.FTPProvider;

public class FTPProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(FTPProvider provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws Exception {
        int counterAdded = 0;
        list(provider, selection, directoryPath, result, counterAdded);
        return result;
    }

    public static String millisecondsToModificationTimeString(long milliseconds) {
        // FTP servers expect the timestamp in UTC and exactly in this format
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        f.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return f.format(new Date(milliseconds));
    }

    public static Calendar modificationTimeStringToCalendar(String modificationTime) {
        if (modificationTime == null) {
            return null;
        }
        // client.getModificationTime gets in this format
        try {
            TimeZone timezone = TimeZone.getTimeZone("Etc/UTC");
            SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
            f.setTimeZone(timezone);

            Calendar calendar = Calendar.getInstance(timezone);
            calendar.setTime(f.parse(modificationTime));
            return calendar;
        } catch (ParseException e) {
            return null;
        }
    }

    public static FTPFile getFTPFileIfExists(String caller, FTPClient client, String path) throws Exception {
        if (client == null || path == null) {
            return null;
        }

        String size = client.getSize(path);
        FTPProtocolReply reply = new FTPProtocolReply(client);
        if (reply.isFileUnavailableReply()) {// not exists
            return null;
        }
        if (!reply.isPositiveReply()) {
            throw new Exception(reply.toString());
        }
        if (size == null) {
            return null;
        }
        String modificationTime = client.getModificationTime(path);
        reply = new FTPProtocolReply(client);

        FTPFile file = new FTPFile();
        file.setSize(Long.parseLong(size));
        file.setTimestamp(modificationTimeStringToCalendar(modificationTime));

        return file;
    }

    public static void deleteDirectoryFilesRecursively(FTPClient client, String pathSeparator, String path) throws Exception {
        FTPFile[] children = client.listFiles(path);
        FTPProtocolReply reply = new FTPProtocolReply(client);
        if (!reply.isPositiveReply()) {
            throw new IOException(reply.toString());
        }
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

    private static int list(FTPProvider provider, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result, int counterAdded)
            throws Exception {
        FTPClient client = provider.requireFTPClient();
        FTPFile[] subDirInfos = client.listFiles(directoryPath);
        FTPProtocolReply reply = new FTPProtocolReply(client);
        if (!reply.isPositiveReply()) {
            throw new IOException(reply.toString());
        }
        for (FTPFile subResource : subDirInfos) {
            if (selection.maxFilesExceeded(counterAdded)) {
                return counterAdded;
            }
            counterAdded = processListEntry(provider, selection, directoryPath, subResource, result, counterAdded);
        }
        return counterAdded;
    }

    // TODO resource.getName() - path???
    private static int processListEntry(FTPProvider provider, ProviderFileSelection selection, String parentDirectory, FTPFile resource,
            List<ProviderFile> result, int counterAdded) throws Exception {

        String fullPath = SOSPathUtils.appendPath(parentDirectory, resource.getName(), provider.getPathSeparator());
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

}
