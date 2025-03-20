package com.sos.commons.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSClassUtil.class);
    private static final String NEW_LINE = "\r\n";

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
        printStackTrace(true, null);
    }

    public static void printStackTrace(boolean onlySOS) {
        printStackTrace(onlySOS, null);
    }

    public static void printStackTrace(boolean onlySOS, final Logger logger) {
        try {
            StackTraceElement trace[] = new Throwable().getStackTrace();
            for (int i = 1; i < trace.length; i++) {
                StackTraceElement el = trace[i];
                if (onlySOS) {
                    if (!el.getClassName().matches("^com\\.sos.*|^sos\\..*")) {
                        continue;
                    }
                }
                Logger log = logger == null ? LOGGER : logger;
                log.info(String.format("[%s][%s:%s]", el.getClassName(), el.getMethodName(), el.getLineNumber()));
            }
        } catch (Throwable ee) {
            LOGGER.error(ee.toString(), ee);
        }

    }

    public static String getStackTrace(Throwable e) {
        if (e == null) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            StackTraceElement trace[] = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                sb.append(trace[i].toString()).append(NEW_LINE);
            }
            return sb.toString().trim();
        } catch (Throwable ee) {
            LOGGER.error(ee.toString(), ee);
            return null;
        }
    }

    public static String getFullStackTrace(Throwable e) {
        if (e == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getStackTrace(e));

        Throwable ex = e.getCause();
        while (ex != null) {
            String s = getStackTrace(ex.getCause());
            if (s != null) {
                sb.append(NEW_LINE);
                sb.append("Caused by: ").append(s);
            }
            ex = ex.getCause();
        }
        return sb.toString();
    }

    private static String getSimpleName(String className) {
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

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    /** Counts the number of bytes in the given InputStream by transferring its contents to a NullOutputStream.
     * 
     * @apiNote It is not really needed to use try-with-resources for OutputStream.nullOutputStream(),<br/>
     *          as it does not manage any resources.<br/>
     *          However, the try-with-resources statement ensures that the InputStream is properly closed after the operation, avoiding resource leaks.
     *
     * @param inputStream The InputStream whose bytes are to be counted
     * @return The number of bytes in the InputStream
     * @throws IOException If an I/O error occurs */
    public static long countBytes(InputStream inputStream) throws IOException {
        try (OutputStream nullOutputStream = OutputStream.nullOutputStream()) {
            return inputStream.transferTo(nullOutputStream);
        }
    }

}
