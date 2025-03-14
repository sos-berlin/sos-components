package com.sos.commons.vfs.smb.commons;

import java.nio.file.Path;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;

public abstract class ASMBProvider extends AProvider<SMBProviderArguments> {

    private String shareName;

    /** Layer for instantiating a Real Provider: SMBJ or ... */
    public ASMBProvider() throws SOSProviderInitializationException {
        super(null, null);
    }

    /** Real Provider */
    public ASMBProvider(ISOSLogger logger, SMBProviderArguments args) throws SOSProviderInitializationException {
        super(logger, args);
        setAccessInfo();
    }

    /** Overrides {@link IProvider#getPathSeparator()}<br/>
     * {@linkplain https://github.com/hierynomus/smbj/issues/170}<br/>
     * {@linkplain https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/ffb795f3-027d-4a3c-997d-3085f2332f6f?redirectedfrom=MSDN} */
    @Override
    public String getPathSeparator() {
        return SOSPathUtil.PATH_SEPARATOR_WINDOWS;
    }

    /** Overrides {@link IProvider#isAbsolutePath(String)} */
    @Override
    public boolean isAbsolutePath(String path) {
        if (SOSString.isEmpty(getShareName(path))) {
            return true;
        }
        if (SOSPathUtil.isAbsoluteWindowsUNCPath(path)) {
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

    private void setAccessInfo() {
        String authMethod = "";
        String user = getArguments().getUser().getDisplayValue();
        switch (getArguments().getAuthMethod().getValue()) {
        case BASIC:
            if (SOSString.isEmpty(user)) {
                user = "anonymous";
            }
            break;
        case KERBEROS:
        case SPNEGO:
        default:
            authMethod = "[" + getArguments().getAuthMethod().getValue().name() + "]";
            if (SOSString.isEmpty(user)) {
                user = "[sso]";
            }
            break;
        }
        String domain = getArguments().getDomain().isEmpty() ? "" : " (" + getArguments().getDomain().getValue() + ")";
        setAccessInfo(String.format("%s%s@%s:%s%s", authMethod, user, getArguments().getHost().getDisplayValue(), getArguments().getPort()
                .getDisplayValue(), domain));
    }

}
