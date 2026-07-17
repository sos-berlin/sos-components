package com.sos.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSTimeoutExeededException;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;

public class SOSShell {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShell.class);

    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version");
    public static final String OS_ARCHITECTURE = System.getProperty("os.arch");
    public static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

    private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(GB|MB|KB|B)", Pattern.CASE_INSENSITIVE);

    private static Charset consoleEncoding;
    private static String hostname;

    public static SOSCommandResult executeCommand(String script) {
        return executeCommand(script, null, null, null, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset encoding) {
        return executeCommand(script, encoding, null, null, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout) {
        return executeCommand(script, null, timeout, null, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset encoding, SOSTimeout timeout) {
        return executeCommand(script, encoding, timeout, null, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSEnv env) {
        return executeCommand(script, null, null, env, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset encoding, SOSEnv env) {
        return executeCommand(script, encoding, null, env, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout, SOSEnv env) {
        return executeCommand(script, null, timeout, env, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset encoding, SOSTimeout timeout, SOSEnv env, Path workingDirectory) {
        SOSCommandResult result = new SOSCommandResult(script, getConsoleEncoding(encoding), timeout);
        try {
            ProcessBuilder pb = new ProcessBuilder(getCommand(script));
            if (workingDirectory != null) {
                pb.directory(workingDirectory.toFile());
            }
            if (env != null && env.getLocalEnvs().size() > 0) {
                pb.environment().putAll(env.getLocalEnvs());
            }
            final Process p = pb.start();
            CompletableFuture<Boolean> out = redirect(p.getInputStream(), result::setStdOut, result.getEncoding());
            CompletableFuture<Boolean> err = redirect(p.getErrorStream(), result::setStdErr, result.getEncoding());

            if (timeout == null) {
                result.setExitCode(p.waitFor());
            } else {
                if (!p.waitFor(timeout.getInterval(), timeout.getTimeUnit())) {
                    result.setTimeoutExeeded(true);
                }
                result.setExitCode(p.exitValue());
            }
            result.setCommand(pb.command().get(pb.command().size() - 1));
            out.join();
            err.join();
        } catch (Exception e) {
            if (result.isTimeoutExeeded() && timeout != null) {
                result.setException(new SOSTimeoutExeededException(timeout.toString(), e));
            } else {
                result.setException(e);
            }
        }
        return result;
    }

    private static CompletableFuture<Boolean> redirect(final InputStream is, final Consumer<String> consumer, final Charset charset) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStreamReader isr = new InputStreamReader(is, charset); BufferedReader br = new BufferedReader(isr);) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    consumer.accept(line + System.lineSeparator());
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    public static Charset getConsoleEncoding() {
        return getConsoleEncoding(null);
    }

    private static Charset getConsoleEncoding(Charset defaultEncoding) {
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        if (defaultEncoding != null) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[getConsoleEncoding][default]%s", defaultEncoding.name()));
            }
            return defaultEncoding;
        }
        if (consoleEncoding == null) {
            synchronized (SOSShell.class) {
                if (consoleEncoding == null) { // double-checked locking
                    consoleEncoding = IS_WINDOWS ? getWindowsConsoleEncoding() : Charset.forName("UTF-8");
                }
            }
        }
        if (isTraceEnabled) {
            LOGGER.trace(String.format("[getConsoleEncoding]%s", consoleEncoding));
        }
        return consoleEncoding;
    }

    private static Charset getWindowsConsoleEncoding() {
        String method = "getWindowsConsoleEncoding";
        int cp = Kernel32.INSTANCE.GetConsoleCP();
        if (cp == 0) {
            Charset defaultCharset = Charset.defaultCharset();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][codepage=%s(%s)]use default charset=%s", method, cp, getKernel32LastError(), defaultCharset.name()));
            }
            return defaultCharset;
        }

        String name = "cp" + cp;
        if (!Charset.isSupported(name)) {
            name = "CP" + cp;
            if (!Charset.isSupported(name)) {
                String defaultName = Charset.defaultCharset().name();
                LOGGER.warn(String.format("[%s][codepage=%s(charset cp|%s not supported)]use default charset=%s", method, cp, name, defaultName));
                name = defaultName;
            }
        }
        Charset charset = Charset.forName(name);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]codepage=%s(charsetName=%s),charset=%s", method, cp, name, charset.name()));
        }
        return charset;
    }

    private static String getKernel32LastError() {
        try {
            int err = Kernel32.INSTANCE.GetLastError();
            return String.format("Kernel32.GetLastError:err=%s,msg=%s", err, new Win32Exception(err).getMessage());
        } catch (Exception e) {
            return String.format("Exception on get Kernel32.GetLastError:%s", e.toString());
        }
    }

    public static String getLocalHostName() throws UnknownHostException {
        if (hostname == null) {
            synchronized (SOSShell.class) {
                if (hostname == null) { // double-checked locking
                    String env = System.getenv(IS_WINDOWS ? "COMPUTERNAME" : "HOSTNAME");
                    hostname = SOSString.isEmpty(env) ? InetAddress.getLocalHost().getHostName() : env;
                }
            }
        }
        return hostname;
    }

    public static Optional<String> getLocalHostNameOptional() {
        try {
            return Optional.of(getLocalHostName());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    public static String getHostAddress(String host) throws UnknownHostException {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException originalException) {
            try {
                return InetAddress.getByName(new URL(host).getHost()).getHostAddress();
            } catch (Exception urlException) {
                throw originalException;
            }
        }
    }

    public static Optional<String> getHostAddressOptional(String host) {
        try {
            return Optional.of(getHostAddress(host));
        } catch (Exception e1) {
            return Optional.empty();
        }
    }

    public static String getHostAddressQuietly(String host) {
        return getHostAddressOptional(host).orElse("could not be resolved!");
    }

    public static String getUsername() {
        return System.getProperty("user.name");
    }

    public static void printSystemInfos() {
        try {
            StringBuilder sb = new StringBuilder("[SYSTEM] ").append(OS_NAME);
            sb.append(", version=").append(OS_VERSION);
            sb.append(", arch=").append(OS_ARCHITECTURE);
            if (IS_WINDOWS) {
                sb.append(", ").append(System.getenv("PROCESSOR_IDENTIFIER"));
            }
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", SOSClassUtil.getMethodName(), e.toString()), e);
        }
    }

    public static void printJVMInfos() {
        printJVMInfos(true);
    }

    public static void printJVMInfos(boolean debugClasspath) {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            // ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            // int peakThreadCount = bean.getPeakThreadCount();
            LOGGER.info(getJVMInfos(runtimeBean));
            if (debugClasspath && LOGGER.isDebugEnabled()) {
                String[] arr = runtimeBean.getClassPath().split(System.getProperty("path.separator"));
                for (String cp : arr) {
                    LOGGER.debug(String.format("[Classpath]%s", cp));
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", SOSClassUtil.getMethodName(), e.toString()), e);
        }
    }

    public static String getJVMInfos() {
        return getJVMInfos(null);
    }

    public static String getJVMInfos(RuntimeMXBean runtimeBean) {
        if (runtimeBean == null) {
            runtimeBean = ManagementFactory.getRuntimeMXBean();
        }

        String name = runtimeBean.getName();

        StringBuilder sb = new StringBuilder();
        sb.append("[JVM]");
        sb.append("[").append(name).append("]");
        sb.append("[pid=").append(getPID(runtimeBean)).append("]");
        sb.append("[version=").append(System.getProperty("java.version")).append("]");
        sb.append("[vm=").append(runtimeBean.getVmVendor()).append(" ").append(runtimeBean.getVmName()).append("]");
        sb.append("[available processors=").append(Runtime.getRuntime().availableProcessors()).append("]");
        sb.append("[memory ");
        sb.append("max=").append(formatBytes(Runtime.getRuntime().maxMemory()));
        sb.append(",total=").append(formatBytes(Runtime.getRuntime().totalMemory()));
        sb.append(",free=").append(formatBytes(Runtime.getRuntime().freeMemory()));
        sb.append("]");
        sb.append("[input arguments=").append(runtimeBean.getInputArguments()).append("]");
        return sb.toString();
    }

    public static Integer getPID() {
        return getPID(ManagementFactory.getRuntimeMXBean());
    }

    public static Integer getPID(RuntimeMXBean runtimeBean) {
        if (runtimeBean == null) {
            runtimeBean = ManagementFactory.getRuntimeMXBean();
        }
        try {
            return Integer.parseInt(runtimeBean.getName().split("@")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getJavaHome() {
        return System.getProperties().getProperty("java.home");
    }

    /** Returns the CMD command for Windows or the default shell command for Unix.
     *
     * <p>
     * <b>Note on Windows CMD:</b>
     * <ul>
     * <li>The <code>/u</code> switch forces UTF-16LE output.</li>
     * <li>Many standard commands like <code>chcp</code>, <code>dir</code>,<br />
     * or other ANSI-based commands do not work correctly with <code>/u</code>, because they rely on the current code page.</li>
     * <li>Therefore, <code>/u</code> should only be used for commands that explicitly require Unicode output, and not for all CMD invocations.</li>
     * <li>In general: <br>
     * - ANSI commands: current code page<br/>
     * - Unicode commands -> /u + UTF-16LE.</li>
     * </ul>
     */

    private static String[] getCommand(String script) {
        if (IS_WINDOWS) {
            String comspec = System.getenv("comspec");
            if (SOSString.isEmpty(comspec)) {
                comspec = "cmd.exe";
            }
            // return new String[] { comspec, "/u", "/C", script };
            return new String[] { comspec, "/C", script };
        } else {
            String shell = System.getenv("SHELL");
            if (shell == null) {
                shell = "/bin/sh";
            }
            return new String[] { shell, "-c", script };
        }
    }

    /** Formats a byte value into a human-readable string representation.
     *
     * <p>
     * The method converts the given number of bytes into the largest appropriate unit using binary multiples (base 1024).
     *
     * <p>
     * Supported units:
     * <ul>
     * <li>B - bytes</li>
     * <li>KB - kilobytes (1024 bytes)</li>
     * <li>MB - megabytes (1024 KB)</li>
     * <li>GB - gigabytes (1024 MB)</li>
     * <li>TB - terabytes (1024 GB)</li>
     * </ul>
     *
     * <p>
     * Examples:
     * <ul>
     * <li>{@code 512} - {@code "512 B"}</li>
     * <li>{@code 2048} - {@code "2 KB"}</li>
     * <li>{@code 1310720} - {@code "1.25 MB"}</li>
     * <li>{@code 1572864} - {@code "1.5 MB"}</li>
     * </ul>
     *
     * <p>
     * The output is formatted using a fixed US locale to ensure the decimal separator is always a dot (.) regardless of system locale settings.
     *
     * @param bytes the size in bytes
     * @return a formatted human-readable string representation of the byte size */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String[] units = { "KB", "MB", "GB", "TB" };
        int unit = 1024;

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        if (exp >= units.length) {
            exp = units.length - 1;
        }

        double value = bytes / Math.pow(unit, exp);
        double rounded = Math.round(value * 100.0) / 100.0;
        long asLong = (long) rounded;

        String number;
        if (rounded == asLong) {
            number = String.format(java.util.Locale.US, "%.0f", rounded);
        } else if (Math.round(rounded * 10) == rounded * 10) {
            number = String.format(java.util.Locale.US, "%.1f", rounded);
        } else {
            number = String.format(java.util.Locale.US, "%.2f", rounded);
        }
        return number + " " + units[exp - 1];
    }

    /** Parses a human-readable byte size string into a byte value.
     *
     * <p>
     * The input may contain one or more size components. Each component consists of a numeric value and a unit, optionally separated by whitespace.
     *
     * <p>
     * Supported formats:
     * <ul>
     * <li>Plain numbers (interpreted as bytes), e.g. {@code "123"}</li>
     * <li>Composite values with units, e.g. {@code "1GB 200MB 5KB 10B"}</li>
     * <li>Decimal values using dot or comma, e.g. {@code "1.5GB"} or {@code "1,5GB"}</li>
     * </ul>
     *
     * <p>
     * Decimal values may use either dot or comma as separator (e.g. {@code "1.5GB"} or {@code "1,5GB"}).
     *
     * <p>
     * Supported units (case-insensitive):
     * <ul>
     * <li>B - bytes</li>
     * <li>KB - kilobytes (1024 bytes)</li>
     * <li>MB - megabytes (1024 KB)</li>
     * <li>GB - gigabytes (1024 MB)</li>
     * </ul>
     *
     * <p>
     * The entire input must conform to the expected format. Any invalid tokens or unexpected characters will result in an {@link IllegalArgumentException}.
     *
     * @param input the byte size string to parse
     * @return the total number of bytes
     * @throws NullPointerException if input is null
     * @throws SOSInvalidDataException if the input is invalid or partially malformed */
    public static long parseByteSize(String input) throws SOSInvalidDataException {
        if (input == null) {
            throw new SOSInvalidDataException("input");
        }

        input = input.trim();
        if (input.matches("\\d+")) {
            return Long.parseLong(input);
        }

        Matcher matcher = BYTE_SIZE_PATTERN.matcher(input);
        double total = 0;
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1).replace(',', '.'));
            String unit = matcher.group(2).toUpperCase(Locale.ROOT);

            switch (unit) {
            case "GB":
                total += value * 1024d * 1024d * 1024d;
                break;
            case "MB":
                total += value * 1024d * 1024d;
                break;
            case "KB":
                total += value * 1024d;
                break;
            case "B":
                total += value;
                break;
            }
        }

        // 1GB MB abc
        if (!matcher.replaceAll("").trim().isEmpty()) {
            throw new SOSInvalidDataException("Invalid byte size: " + input);
        }

        return Math.round(total);
    }

    public static void printCpuLoad() {
        printCpuLoad(LOGGER);

    }

    public static void printCpuLoad(Logger logger) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[] { "SystemCpuLoad", "ProcessCpuLoad" });
            if (list.isEmpty()) {
                logger.info(String.format("[CpuLoad][System,Process]%s", Double.NaN));
                return;
            }

            Double val = (Double) ((Attribute) list.get(0)).getValue();
            Double systemValue = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
            if (list.size() > 1) {
                val = (Double) ((Attribute) list.get(1)).getValue();
                Double processValue = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
                logger.info(String.format("[CpuLoad][System=%s][Process=%s]", systemValue, processValue));
            } else {
                logger.info(String.format("[CpuLoad][System=%s]", systemValue));
            }
        } catch (Exception e) {
            logger.error(String.format("[%s]%s", SOSClassUtil.getMethodName(), e.toString()), e);
        }
    }

    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
