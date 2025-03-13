package com.sos.commons.vfs.smb.smbj;

import java.io.IOException;
import java.io.InputStream;

import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSClassUtil;

public class SMBInputStream extends InputStream {

    private final DiskShare share;
    private final File file;
    private final InputStream fileInputStream;

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @throws IOException */
    public SMBInputStream(final boolean accessMaskMaximumAllowed, final DiskShare share, final String smbPath) throws Exception {
        this.share = share;
        if (!this.share.fileExists(smbPath)) {
            throw new SOSNoSuchFileException(smbPath, new Exception(smbPath));
        }
        this.file = ProviderUtils.openFileWithReadAccess(accessMaskMaximumAllowed, share, smbPath);
        this.fileInputStream = file.getInputStream();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            SOSClassUtil.closeQuietly(fileInputStream);
            SOSClassUtil.closeQuietly(file);
            SOSClassUtil.closeQuietly(share);
        }
    }

    @Override
    public int read() throws IOException {
        return fileInputStream.read();
    }

}
