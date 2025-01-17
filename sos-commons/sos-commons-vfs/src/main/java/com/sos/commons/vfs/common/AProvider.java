package com.sos.commons.vfs.common;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.exception.SOSProviderConnectException;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.commons.vfs.exception.SOSProviderInitializationException;

public abstract class AProvider<A extends AProviderArguments> implements IProvider {

    private final ISOSLogger logger;
    private final A arguments;

    private Boolean typeSource;
    private String typeInfo;

    public AProvider(ISOSLogger logger, A arguments) throws SOSProviderInitializationException {
        this.logger = logger;
        this.arguments = arguments;
    }

    @Override
    public void ensureConnected() throws SOSProviderConnectException {
        if (!isConnected()) {
            connect();
        }
    }

    public ISOSLogger getLogger() {
        return logger;
    }

    public A getArguments() {
        return arguments;
    }

    public Boolean typeSource() {
        return typeSource;
    }

    public void typeSource(Boolean val) {
        typeSource = val;
        if (val != null) {
            typeInfo = typeSource ? "[source]" : "[target]";
        }
    }

    public String getTypeInfo() {
        if (typeInfo == null) {
            typeInfo = "";
        }
        return typeInfo;
    }

    public static String millis2string(int val) {
        if (val <= 0) {
            return String.valueOf(val).concat("ms");
        }
        try {
            return String.valueOf(Math.round(val / 1000)).concat("s");
        } catch (Throwable e) {
            return String.valueOf(val).concat("ms");
        }
    }

    public static void checkParam(String paramValue, String msg) throws SOSProviderException {
        if (SOSString.isEmpty(paramValue)) {
            throw new SOSProviderException(new SOSMissingDataException(msg));
        }
    }

    public static boolean isValidModificationTime(Long milliseconds) {
        return milliseconds != null && milliseconds.longValue() > 0;
    }

    @SuppressWarnings("unused")
    private static boolean isAbsolutePathStyle(final String path) {
        if (SOSString.isEmpty(path)) {
            return false;
        }

        String np = SOSPathUtil.toUnixStylePath(path);
        if (SOSPathUtil.isAbsolutePathWindowsStyle(np)) {
            return true;
        }
        if (SOSPathUtil.isAbsolutePathUnixStyle(np)) {
            return true;
        }
        if (SOSPathUtil.isAbsolutePathURIStyle(np)) {
            return true;
        }
        return false;
    }

}
