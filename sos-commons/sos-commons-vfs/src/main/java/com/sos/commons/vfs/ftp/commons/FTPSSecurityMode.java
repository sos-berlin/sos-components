package com.sos.commons.vfs.ftp.commons;

import com.sos.commons.util.SOSString;

public enum FTPSSecurityMode {

    EXPLICIT, IMPLICIT;

    public static FTPSSecurityMode fromString(String securityMode) {
        if (SOSString.isEmpty(securityMode)) {
            return null;
        }
        return FTPSSecurityMode.valueOf(securityMode.trim().toUpperCase());
    }
}
