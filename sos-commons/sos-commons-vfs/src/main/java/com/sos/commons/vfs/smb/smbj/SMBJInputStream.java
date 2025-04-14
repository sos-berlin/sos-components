package com.sos.commons.vfs.smb.smbj;

import java.io.IOException;
import java.io.InputStream;

import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSClassUtil;

public class SMBJInputStream extends InputStream {

    private final DiskShare share;
    private final boolean closeShare;
    private final File file;
    private final InputStream is;

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @throws IOException */
    public SMBJInputStream(final boolean accessMaskMaximumAllowed, final DiskShare share, final boolean closeShare, final String smbPath)
            throws Exception {
        this.share = share;
        this.closeShare = closeShare;
        if (!this.share.fileExists(smbPath)) {
            throw new SOSNoSuchFileException(smbPath, new Exception(smbPath));
        }
        this.file = SMBJProviderUtils.openFileWithReadAccess(accessMaskMaximumAllowed, share, smbPath);
        this.is = file.getInputStream();
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            SOSClassUtil.closeQuietly(is);
            SOSClassUtil.closeQuietly(file);
            if (closeShare) {
                SOSClassUtil.closeQuietly(share);
            }
        }
    }

}
