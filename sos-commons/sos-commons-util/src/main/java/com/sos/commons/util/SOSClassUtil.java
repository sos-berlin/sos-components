package com.sos.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSMissingDataException;

public class SOSClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSClassUtil.class);
    private static final String NEW_LINE = "\n";

    public static String getMethodName() {
        // 0 - this method - SOSClassUtil.getMethodName()
        // 1 - called method - SOSClassUtil.getMethodName(index=2)
        // 2 - expected method
        return getMethodName(2);
    }

    public static String getMethodName(int index) {
        // for (StackTraceElement trace : new Throwable().getStackTrace()) {
        // LOGGER.info(getSimpleName(trace.getClassName()) + "." + trace.getMethodName());
        // }
        while (index >= 0) {
            try {
                // 1 index more vs new Throwable().getStackTrace - for Thread.currentThread().getStackTrace itself
                // StackTraceElement trace = Thread.currentThread().getStackTrace()[index];

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
            Logger log = logger == null ? LOGGER : logger;
            StackTraceElement trace[] = new Throwable().getStackTrace();
            for (int i = 1; i < trace.length; i++) {
                StackTraceElement el = trace[i];
                if (onlySOS) {
                    if (!el.getClassName().matches("^com\\.sos.*|^sos\\..*")) {
                        continue;
                    }
                }
                log.info(String.format("[%s][%s:%s]", el.getClassName(), el.getMethodName(), el.getLineNumber()));
            }
        } catch (Exception ee) {
            LOGGER.error(ee.toString(), ee);
        }

    }

    public static String getStackTrace(Throwable e) {
        return getStackTrace(e, NEW_LINE);
    }

    public static String getStackTrace(Throwable e, String newLine) {
        if (e == null) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            StackTraceElement trace[] = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                sb.append(trace[i].toString()).append(newLine);
            }
            return sb.toString().trim();
        } catch (Exception ee) {
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

    /** Closes the given {@link Closeable} if it is not {@code null}.
     *
     * <p>
     * This method is a convenience wrapper that performs a null-check before invoking {@link Closeable#close()}.
     *
     * @param closeable the {@link Closeable} to close, may be {@code null}
     * @throws IOException if an I/O error occurs while closing the resource */
    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    /** Closes the given {@link AutoCloseable} if it is not {@code null}.
     *
     * <p>
     * This method is a convenience wrapper that performs a null-check before invoking {@link AutoCloseable#close()}.
     *
     * @param closeable the {@link AutoCloseable} to close, may be {@code null}
     * @throws Exception if an error occurs while closing the resource */
    public static void close(AutoCloseable closeable) throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    /** Closes the given {@link Closeable} quietly.
     *
     * <p>
     * If the resource is {@code null}, this method does nothing.<br/>
     * Any exception thrown by {@link Closeable#close()} is caught and ignored.
     *
     * @param closeable the {@link Closeable} to close, may be {@code null} */
    public static void closeQuietly(Closeable closeable) {
        try {
            close(closeable);
        } catch (Exception ignored) {
        }
    }

    /** Closes the given {@link AutoCloseable} quietly.
     *
     * <p>
     * If the resource is {@code null}, this method does nothing.<br/>
     * Any exception thrown by {@link AutoCloseable#close()} is caught and ignored.
     *
     * @param closeable the {@link AutoCloseable} to close, may be {@code null} */
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            close(closeable);
        } catch (Exception ignored) {
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
     * @throws Exception If an error occurs */
    public static long countBytes(InputStream input) throws Exception {
        if (input == null) {
            return 0L;
        }
        try (OutputStream nullOutputStream = OutputStream.nullOutputStream()) {
            return input.transferTo(nullOutputStream);
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096]; // 4KB Puffer
        int nRead;
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public static void skipFully(InputStream input, long offset) throws Exception {
        if (input == null) {
            throw new SOSMissingDataException("input");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }

        byte[] buffer = new byte[8_192];
        long toSkip = offset;
        while (toSkip > 0) {
            int len = (int) Math.min(buffer.length, toSkip);
            int read = input.read(buffer, 0, len);
            if (read == -1) {
                throw new EOFException("Stream end reached before skipping all bytes");
            }
            toSkip -= read;
        }
    }

    /** @param contextClass
     * @param resourcePath no leading slash – path is relative to the classpath
     * @return InputStream */
    public static InputStream openResourceStream(Class<?> contextClass, String resourcePath) throws Exception {
        if (contextClass == null) {
            throw new SOSMissingDataException("contextClass");
        }
        if (resourcePath == null) {
            throw new SOSMissingDataException("resourcePath");
        }

        InputStream is = contextClass.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("[" + contextClass.getName() + "]Resource not found: " + resourcePath);
        }
        return is;
    }

    /** @param contextClass
     * @param resourcePath - no leading slash – path is relative to the classpath
     * @return resource file content
     * @throws Exception */
    public static String readResourceFile(Class<?> contextClass, String resourcePath) throws Exception {
        if (contextClass == null) {
            throw new SOSMissingDataException("contextClass");
        }
        if (resourcePath == null) {
            throw new SOSMissingDataException("resourcePath");
        }
        try (InputStream is = openResourceStream(contextClass, resourcePath)) {
            return SOSString.toString(is);
        }
    }

    private static String getSimpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

}
