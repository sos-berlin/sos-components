package com.sos.commons.util;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSClassUtil.class);

    public static String getMethodName() {
        return getMethodName(1);
    }

    public static String getMethodName(int index) {
        while (index >= 0) {
            try {
                StackTraceElement trace = new Throwable().getStackTrace()[index];
                // String line = trace.getLineNumber() > 0 ? "(" + trace.getLineNumber() + ")" : "";
                // return getSimpleName(trace.getClassName()) + "." + trace.getMethodName() + line;
                return getSimpleName(trace.getClassName()) + "." + trace.getMethodName();
            } catch (Exception e) {
                index = index - 1;
            }
        }
        return "";
    }

    public static String getClassName() throws Exception {
        return new Throwable().getStackTrace()[1].getClassName();
    }

    public static void printStackTrace() {
        printStackTrace(true);
    }

    public static void printStackTrace(boolean onlySOS) {
        StackTraceElement trace[] = new Throwable().getStackTrace();
        for (int i = 1; i < trace.length; i++) {
            StackTraceElement el = trace[i];
            if (onlySOS) {
                if (!el.getClassName().matches("^com\\.sos.*|^sos\\..*")) {
                    continue;
                }
            }
            LOGGER.info(String.format("[%s][%s:%s]", el.getClassName(), el.getMethodName(), el.getLineNumber()));
        }
    }

    public static String getSimpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public static URL getLocation(String fullyQualifiedClassName) throws ClassNotFoundException {
        return getLocation(SOSClassUtil.class.getClassLoader(), fullyQualifiedClassName);
    }

    public static URL getLocation(ClassLoader cl, Class<?> clazz) throws ClassNotFoundException {
        return getLocation(cl, clazz.getName());
    }

    public static URL getLocation(ClassLoader cl, String fullyQualifiedClassName) throws ClassNotFoundException {
        return getLocation(cl.loadClass(fullyQualifiedClassName));
    }

    public static URL getLocation(Class<?> clazz) {
        return clazz.getProtectionDomain().getCodeSource().getLocation();
    }

}
