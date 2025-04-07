package com.sos.commons.vfs.smb;

import java.nio.file.Path;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;

public abstract class SMBProvider extends AProvider<SMBProviderArguments> {

    private String shareName;

    public static SMBProvider createInstance(ISOSLogger logger, SMBProviderArguments args) throws ProviderInitializationException {
        return new com.sos.commons.vfs.smb.smbj.SMBJProviderImpl(logger, args);
    }

    protected SMBProvider(ISOSLogger logger, SMBProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        setAccessInfo(getArguments().getAccessInfo());
    }

    /** Overrides {@link IProvider#getPathSeparator()}<br/>
     * {@linkplain https://github.com/hierynomus/smbj/issues/170}<br/>
     * {@linkplain https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/ffb795f3-027d-4a3c-997d-3085f2332f6f?redirectedfrom=MSDN} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtils.PATH_SEPARATOR_WINDOWS;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        if (SOSString.isEmpty(getShareName(path))) {
            return true;
        }
        if (SOSPathUtils.isAbsoluteWindowsUNCPath(path)) {
            return true;
        }
        String p = SOSString.trimStart(toPathStyle(path), getPathSeparator());
        return p.startsWith(getShareName(path));
    }

    /** Overrides {@link IProvider#normalizePath(String)} */
    @Override
    public String normalizePath(String path) {
        return toPathStyle(Path.of(path).normalize().toString());
    }

    /** 'sos/documents/myfile.txt' -> 'sos'<br/>
     * '/sos/documents/mydocuments/myfile.txt -> 'sos'<br/>
     * '\\sos\\documents\\myfile.txt' -> 'sos'<br/>
     * 'myfile.txt' -> ''<br/>
     * '/myfile.txt' -> ''<br/>
     * '/' -> '' <br/>
     * '' -> '' <br/>
     */
    public String getShareName(String path) {
        if (shareName == null) {
            if (!getArguments().getShareName().isEmpty()) {
                shareName = getArguments().getShareName().getValue();
            } else if (SOSString.isEmpty(path)) {
                shareName = "";
            } else {
                // Remove leading backslashes or slashes, if they exist and split in 2 parts
                String[] pathParts = path.replaceAll("^[/\\\\]+", "").split("[/\\\\]", 2);
                shareName = pathParts.length > 1 ? pathParts[0] : "";
            }
        }
        return shareName;
    }
}
