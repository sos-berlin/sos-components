package com.sos.commons.vfs.smb.smbj;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;

public class ProviderUtils {

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @return */
    protected static File openFileWithReadAccess(boolean accessMaskMaximumAllowed, DiskShare share, String smbPath) {
        return openFile(accessMaskMaximumAllowed, share, smbPath, AccessMask.GENERIC_READ, SMB2CreateDisposition.FILE_OPEN);
    }

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @return */
    protected static File openExistingFileWithChangeAttributeAccess(boolean accessMaskMaximumAllowed, DiskShare share, String smbPath) {
        return openFile(accessMaskMaximumAllowed, share, smbPath, AccessMask.GENERIC_WRITE, SMB2CreateDisposition.FILE_OPEN);
    }

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath @param smbPath The normalized SMB path of the file to open. This path should already be processed using
     *            {@link SMBJProviderImpl#getSMBPath(String)} to match the expected format.
     * @param append
     * @return */
    protected static File openFileWithWriteAccess(boolean accessMaskMaximumAllowed, DiskShare share, String smbPath, boolean append) {
        // FILE_OPEN_IF - Open the file if it already exists; otherwise, create the file.
        // FILE_OVERWRITE_IF - Overwrite the file if it already exists; otherwise, create the file.
        SMB2CreateDisposition cd = append ? SMB2CreateDisposition.FILE_OPEN_IF : SMB2CreateDisposition.FILE_OVERWRITE_IF;
        return openFile(accessMaskMaximumAllowed, share, smbPath, AccessMask.GENERIC_WRITE, cd);
    }

    /** FILE_OPEN - If the file already exists, return success; otherwise, fail the operation.
     * 
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @param accessMask
     * @param createDisposition
     * @param accessMaskMaximumAllowed
     * @return */
    protected static File openFile(boolean accessMaskMaximumAllowed, DiskShare share, String smbPath, AccessMask accessMask,
            SMB2CreateDisposition createDisposition) {
        Set<AccessMask> ams = new HashSet<>();
        if (accessMaskMaximumAllowed) {
            ams.add(AccessMask.MAXIMUM_ALLOWED);
        } else {
            ams.add(accessMask);
        }

        Set<SMB2ShareAccess> sa = new HashSet<>();
        sa.addAll(SMB2ShareAccess.ALL);

        Set<SMB2CreateOptions> co = new HashSet<>();
        co.add(SMB2CreateOptions.FILE_WRITE_THROUGH);

        return share.openFile(smbPath, ams, null, sa, createDisposition, co);
    }

    /** @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @return */
    protected static File openExistingFileWithRenameAccess(boolean accessMaskMaximumAllowed, DiskShare share, String smbPath) {
        Set<AccessMask> ams = new HashSet<>();
        if (accessMaskMaximumAllowed) {
            ams.add(AccessMask.MAXIMUM_ALLOWED);
        } else {
            ams.add(AccessMask.GENERIC_WRITE);
            ams.add(AccessMask.DELETE);
        }

        Set<FileAttributes> fa = new HashSet<>();
        fa.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);

        Set<SMB2ShareAccess> sa = new HashSet<>();
        sa.addAll(SMB2ShareAccess.ALL);

        Set<SMB2CreateOptions> co = new HashSet<>();
        co.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);

        return share.openFile(smbPath, ams, fa, sa, SMB2CreateDisposition.FILE_OPEN, co);
    }

    protected static int list(SMBJProviderImpl provider, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result,
            DiskShare share, int counterAdded) {
        List<FileIdBothDirectoryInformation> infos = share.list(directoryPath);
        for (FileIdBothDirectoryInformation info : infos) {
            if (selection.maxFilesExceeded(counterAdded)) {
                return counterAdded;
            }

            String name = info.getFileName();
            if (name == null || ".".equals(name) || "..".equals(name)) {
                continue;
            }
            String fullPath = directoryPath.isEmpty() ? name : directoryPath + provider.getPathSeparator() + name;
            if (isDirectory(info)) {
                if (selection.getConfig().isRecursive()) {
                    if (selection.checkDirectory(fullPath)) {
                        counterAdded = list(provider, selection, fullPath, result, share, counterAdded);
                    }
                }
            } else {
                if (selection.checkFileName(name) && selection.isValidFileType(info)) {
                    ProviderFile file = provider.createProviderFile(fullPath, info);
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

    protected static boolean isDirectory(FileIdBothDirectoryInformation info) {
        // EnumWithValue.EnumUtils.isSet(info.getFileAttributes(), FileAttributes.FILE_ATTRIBUTE_DIRECTORY)
        return FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue() == info.getFileAttributes();
    }

    protected static boolean isDirectory(FileAllInformation info) {
        return info.getStandardInformation().isDirectory();
    }

}
