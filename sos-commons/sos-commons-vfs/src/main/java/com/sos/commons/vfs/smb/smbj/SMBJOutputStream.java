package com.sos.commons.vfs.smb.smbj;

import java.io.IOException;
import java.io.OutputStream;

import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.sos.commons.exception.SOSException;
import com.sos.commons.util.SOSClassUtil;

public class SMBJOutputStream extends OutputStream {

    private final DiskShare share;
    private final boolean closeShare;
    private final File file;
    private final OutputStream os;

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @param append
     * @throws IOException */
    public SMBJOutputStream(final boolean accessMaskMaximumAllowed, final DiskShare share, final boolean closeShare, final String smbPath,
            boolean append) throws IOException {
        this.share = share;
        this.closeShare = closeShare;
        this.file = SMBJProviderUtils.openFileWithWriteAccess(accessMaskMaximumAllowed, share, smbPath, append);
        this.os = file.getOutputStream(append);
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;

        // 1) close os
        try {
            SOSClassUtil.close(os);
        } catch (IOException e) {
            exception = SOSException.mergeException(exception, e);
        }
        // 2) close file
        try {
            SOSClassUtil.close(file);
        } catch (IOException e) {
            exception = SOSException.mergeException(exception, e);
        }
        // 3) close share
        if (closeShare) {
            try {
                SOSClassUtil.close(share);
            } catch (IOException e) {
                exception = SOSException.mergeException(exception, e);
            } catch (Exception e) {
                exception = SOSException.mergeException(exception, new IOException(e));
            }
        }
        // 4) super.close() is called for completeness.
        // OutputStream.close() is a no-op today, but subclasses may override it.
        try {
            super.close();
        } catch (IOException e) {
            exception = SOSException.mergeException(exception, e);
        }

        if (exception != null) {
            throw exception;
        }
    }
}
