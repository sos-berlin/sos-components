package com.sos.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSClassUtil extends java.lang.Object {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSClassUtil.class);

    public static String getMethodName() {
        try {
            StackTraceElement trace[] = new Throwable().getStackTrace();
            String lineNumber = trace[1].getLineNumber() > 0 ? "(" + trace[1].getLineNumber() + ")" : "";
            return trace[1].getClassName() + "." + trace[1].getMethodName() + lineNumber;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return "";

    }

    public static String getClassName() throws Exception {
        StackTraceElement trace[] = new Throwable().getStackTrace();
        return trace[1].getClassName();
    }

}
