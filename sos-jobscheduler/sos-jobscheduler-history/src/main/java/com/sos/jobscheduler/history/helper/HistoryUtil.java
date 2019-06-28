package com.sos.jobscheduler.history.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.zip.GZIPOutputStream;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

public class HistoryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryUtil.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    public static final String NEW_LINE = "\r\n";

    public static String getFolderFromPath(String path) {
        int li = path.lastIndexOf("/");
        if (li == 0) {
            return path.substring(0, 1);
        }
        return li > -1 ? path.substring(0, li) : path;
    }

    public static String getBasenameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

    public static String hashString(String val) {
        return Hashing.sha256().hashString(val, StandardCharsets.UTF_8).toString();
    }

    public static void printSystemInfos() {
        try {
            String osName = System.getProperty("os.name");
            LOGGER.info(String.format("[SYSTEM]name=%s, version=%s, arch=%s", osName, System.getProperty("os.version"), System.getProperty(
                    "os.arch")));
            if (osName.startsWith("Windows")) {
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

            if (isDebugEnabled) {
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

    public static void printCpuLoad(Logger logger, String identifier) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[] { "SystemCpuLoad", "ProcessCpuLoad" });

            if (list.isEmpty()) {
                logger.info(String.format("[%s][CpuLoad][System,Process]%s", identifier, Double.NaN));
                return;
            }
            Double val = (Double) ((Attribute) list.get(0)).getValue();
            Double value = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
            logger.info(String.format("[%s][CpuLoad][System]%s", identifier, value));
            if (list.size() > 1) {
                val = (Double) ((Attribute) list.get(1)).getValue();
                value = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
                logger.info(String.format("[%s][CpuLoad][Process]%s", identifier, value));
            }
        } catch (Throwable e) {
            logger.error(String.format("[printCpuLoad]%s", e.toString()), e);
        }
    }

    public static byte[] gzipCompress(Path path) throws Exception {
        byte[] uncompressedData = Files.readAllBytes(path);
        byte[] result = new byte[] {};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length); GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            gzipOS.write(uncompressedData);
            gzipOS.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            throw e;
        }
        return result;
    }

    public static void executeCommand(Logger logger, String script, String identifier) {
        BufferedReader br = null;
        try {
            boolean isWindows = System.getProperty("os.name").startsWith("Windows");
            String[] command = new String[2 + 1];
            if (isWindows) {
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

            logger.info(String.format("[%s][%s] execute ...", identifier, script));

            Process proc = rt.exec(command);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(String.format("[%s][out]        %s", identifier, line));
            }
            br.close();
            br = null;

        } catch (Throwable e) {
            logger.error(String.format("[%s][executeCommand]%s", identifier, e.toString()), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
