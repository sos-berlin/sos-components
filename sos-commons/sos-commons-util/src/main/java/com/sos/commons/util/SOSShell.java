package com.sos.commons.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSShell {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShell.class);

    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version");
    public static final String OS_ARCHITECTURE = System.getProperty("os.arch");

    public static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

    private static String hostname;

    public static void executeCommand(String script) {
        executeCommand(script, LOGGER);
    }

    public static void executeCommand(String script, Logger logger) {
        BufferedReader br = null;
        try {
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

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(String.format("[out]%s", line));
            }
            br.close();
            br = null;

        } catch (Throwable e) {
            logger.error(String.format("[executeCommand]%s", e.toString()), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
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
            LOGGER.info(String.format("[SYSTEM]name=%s, version=%s, arch=%s", OS_NAME, OS_VERSION, OS_ARCHITECTURE));
            if (IS_WINDOWS) {
                LOGGER.info(String.format("[SYSTEM]%s", System.getenv("PROCESSOR_IDENTIFIER")));
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("[printSystemInfos]%s", e.toString()), e);
        }
    }

    public static void printJVMInfos() {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            // ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            // int peakThreadCount = bean.getPeakThreadCount();

            String name = runtimeBean.getName();
            String pid = name.split("@")[0];

            LOGGER.info(String.format("[JVM]pid=%s, name=%s, %s %s %s, available processors(cores)=%s, max memory=%s, input arguments=%s", pid, name,
                    System.getProperty("java.version"), runtimeBean.getVmVendor(), runtimeBean.getVmName(), Runtime.getRuntime()
                            .availableProcessors(), getJVMMemory(Runtime.getRuntime().maxMemory()), runtimeBean.getInputArguments()));

            if (LOGGER.isDebugEnabled()) {
                String[] arr = runtimeBean.getClassPath().split(System.getProperty("path.separator"));
                for (String cp : arr) {
                    LOGGER.debug(String.format("[Classpath]%s", cp));
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[printJVMInfos]%s", e.toString()), e);
        }
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
            Double value = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
            logger.info(String.format("[CpuLoad][System]%s", value));
            if (list.size() > 1) {
                val = (Double) ((Attribute) list.get(1)).getValue();
                value = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
                logger.info(String.format("[CpuLoad][Process]%s", value));
            }
        } catch (Throwable e) {
            logger.error(String.format("[printCpuLoad]%s", e.toString()), e);
        }
    }

}
