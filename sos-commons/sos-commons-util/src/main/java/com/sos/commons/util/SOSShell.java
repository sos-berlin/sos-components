package com.sos.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;

public class SOSShell {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShell.class);

    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version");
    public static final String OS_ARCHITECTURE = System.getProperty("os.arch");
    public static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
    public static final String CODEPAGE = IS_WINDOWS ? "CP850" : "UTF-8";

    private static String hostname;

    public static SOSCommandResult executeCommand(String script) {
        return executeCommand(script, null, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout) {
        return executeCommand(script, timeout, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSEnv env) {
        return executeCommand(script, null, env);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout, SOSEnv env) {
        SOSCommandResult result = new SOSCommandResult(script);
        try {
            ProcessBuilder pb = new ProcessBuilder(getCommand(script));
            if (env != null && env.getLocalEnvs().size() > 0) {
                pb.environment().putAll(env.getLocalEnvs());
            }
            final Process p = pb.start();

            CompletableFuture<Boolean> out = redirect(p.getInputStream(), result::setStdOut);
            CompletableFuture<Boolean> err = redirect(p.getErrorStream(), result::setStdErr);

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
            result.setException(e);
        }
        return result;
    }

    private static CompletableFuture<Boolean> redirect(final InputStream is, final Consumer<String> consumer) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStreamReader isr = new InputStreamReader(is, CODEPAGE); BufferedReader br = new BufferedReader(isr);) {
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

    public static String getHostname() throws UnknownHostException {
        if (hostname == null) {
            String env = System.getenv(IS_WINDOWS ? "COMPUTERNAME" : "HOSTNAME");
            hostname = SOSString.isEmpty(env) ? InetAddress.getLocalHost().getHostName() : env;
        }
        return hostname;
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
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            // ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            // int peakThreadCount = bean.getPeakThreadCount();

            String name = runtimeBean.getName();
            String pid = name.split("@")[0];

            LOGGER.info(String.format("[JVM] pid=%s, name=%s, %s %s %s, available processors(cores)=%s, max memory=%s, input arguments=%s", pid, name,
                    System.getProperty("java.version"), runtimeBean.getVmVendor(), runtimeBean.getVmName(), Runtime.getRuntime()
                            .availableProcessors(), getJVMMemory(Runtime.getRuntime().maxMemory()), runtimeBean.getInputArguments()));

            if (LOGGER.isDebugEnabled()) {
                String[] arr = runtimeBean.getClassPath().split(System.getProperty("path.separator"));
                for (String cp : arr) {
                    LOGGER.debug(String.format("[Classpath]%s", cp));
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", SOSClassUtil.getMethodName(), e.toString()), e);
        }
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

    private static String getJVMMemory(long memory) {
        String msg = "no limit";
        if (memory != Long.MAX_VALUE) {
            DecimalFormat df = new DecimalFormat("0.00");
            float sizeKb = 1024.0f;
            float sizeMb = sizeKb * sizeKb;
            msg = df.format(memory / sizeMb) + "Mb";
        }
        return msg;
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
}
