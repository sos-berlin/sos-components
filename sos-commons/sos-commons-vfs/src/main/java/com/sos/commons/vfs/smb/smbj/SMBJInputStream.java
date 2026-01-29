package com.sos.commons.vfs.smb.smbj;

import java.io.IOException;
import java.io.InputStream;

import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSClassUtil;

public class SMBJInputStream extends InputStream {

    private final DiskShare share;
    private final boolean closeShareOnStreamClose;
    private final File file;
    private final InputStream is;

    /** @param accessMaskMaximumAllowed
     * @param share
     * @param smbPath The normalized SMB path of the file to open. This path should already be processed using {@link SMBJProviderImpl#getSMBPath(String)} to
     *            match the expected format.
     * @param offset
     * @throws IOException */
    public SMBJInputStream(final boolean accessMaskMaximumAllowed, final DiskShare share, final boolean closeShareOnStreamClose, final String smbPath,
            long offset) throws Exception {
        this.share = share;
        this.closeShareOnStreamClose = closeShareOnStreamClose;
        if (!this.share.fileExists(smbPath)) {
            throw new SOSNoSuchFileException(smbPath, new Exception(smbPath));
        }
        this.file = SMBJProviderUtils.openFileWithReadAccess(accessMaskMaximumAllowed, share, smbPath);
        this.is = file.getInputStream();

        // SMBJ does not support offset-based InputStreams - implemented via client-side skipping
        if (offset > 0) {
            SOSClassUtil.skipFully(this.is, offset);
        }
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;

        // 1) super.close() is called for completeness.
        // InputStream.close() is a no-op today, but subclasses may override it.
        try {
            super.close();
        } catch (IOException e) {
            exception = SOSException.mergeException(exception, e);
        }
        // 2) close is
        try {
            SOSClassUtil.close(is);
        } catch (IOException e) {
            exception = SOSException.mergeException(exception, e);
        }
        // 3) close file
        try {
            SOSClassUtil.close(file);
        } catch (IOException e) {
            exception = SOSException.mergeException(exception, e);
        }
        // 4) close share
        if (closeShareOnStreamClose) {
            try {
                SOSClassUtil.close(share);
            } catch (IOException e) {
                exception = SOSException.mergeException(exception, e);
            } catch (Exception e) {
                exception = SOSException.mergeException(exception, new IOException(e));
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
