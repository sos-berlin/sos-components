package com.sos.commons.vfs.exceptions;

/** Not supported for:
 * <ul>
 * <li>Azure Blob Storage, which uses virtual directories.
 * <ul>
 * <li>If a directory does not exist, it returns HTTP 200 with an empty <Blobs /> response instead of HTTP 404.</li>
 * </ul>
 * <li>FTP/FTPS.
 * <ul>
 * <li>FTP servers typically return the generic reply code 550, which may indicate a missing file or directory, access denied, insufficient permissions, or
 * another server-specific error.</li>
 * <li>Therefore, a generic {@code ProviderDirectoryException} is thrown instead.</li>
 * </ul>
 * </ul>
 */
public class ProviderDirectoryNotFoundException extends ProviderDirectoryException {

    private static final long serialVersionUID = 1L;

    public ProviderDirectoryNotFoundException(String msg) {
        super(msg);
    }

    public ProviderDirectoryNotFoundException(String msg, Throwable e) {
        super(msg, e);
    }
}
