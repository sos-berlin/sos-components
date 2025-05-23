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
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Charset SYSTEM_ENCODING;
    private static String HOSTNAME;

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
        SOSCommandResult result = new SOSCommandResult(script, getEncoding(encoding), timeout);
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
        } catch (Throwable e) {
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

    public static Charset getSystemEncoding() {
        return getEncoding(null);
    }

    private static Charset getEncoding(Charset defaultEncoding) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (defaultEncoding != null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getEncoding][default]%s", defaultEncoding.name()));
            }
            return defaultEncoding;
        }
        if (SYSTEM_ENCODING == null) {
            SYSTEM_ENCODING = IS_WINDOWS ? getWindowsEncoding() : Charset.forName("UTF-8");
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getEncoding]%s", SYSTEM_ENCODING.name()));
        }
        return SYSTEM_ENCODING;
    }

    private static Charset getWindowsEncoding() {
        String method = "getWindowsEncoding";
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
        } catch (Throwable e) {
            return String.format("Exception on get Kernel32.GetLastError:%s", e.toString());
        }
    }

    public static String getLocalHostName() throws UnknownHostException {
        if (HOSTNAME == null) {
            String env = System.getenv(IS_WINDOWS ? "COMPUTERNAME" : "HOSTNAME");
            HOSTNAME = SOSString.isEmpty(env) ? InetAddress.getLocalHost().getHostName() : env;
        }
        return HOSTNAME;
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
        } catch (Throwable e) {
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
        } catch (Throwable e) {
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
        } catch (Throwable e) {
            return null;
        }
    }

    public static String getJavaHome() {
        return System.getProperties().getProperty("java.home");
    }

    private static String[] getCommand(String script) {
        String[] command = new String[2 + 1];
        if (IS_WINDOWS) {
            command[0] = System.getenv("comspec");
            command[1] = "/C";
            command[2] = script;
        } else {
            String shell = System.getenv("SHELL");
            if (shell == null) {
                shell = "/bin/sh";
            }
            command[0] = shell;
            command[1] = "-c";
            command[2] = script;
        }
        return command;
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int unit = 1024;
        String[] units = { "KB", "MB", "GB", "TB" };
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        return new DecimalFormat("#,##0.00").format(bytes / Math.pow(unit, exp)) + " " + units[exp - 1];
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
        } catch (Throwable e) {
            logger.error(String.format("[%s]%s", SOSClassUtil.getMethodName(), e.toString()), e);
        }
    }

    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
