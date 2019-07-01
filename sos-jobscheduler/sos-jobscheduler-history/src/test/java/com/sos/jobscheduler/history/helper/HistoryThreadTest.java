package com.sos.jobscheduler.history.helper;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class HistoryThreadTest {

    public void dumpThreadDump() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        for (ThreadInfo ti : threadMxBean.dumpAllThreads(true, true)) {
            System.out.print("[dumpThread]" + ti.toString());
        }
    }

    public static String dumpThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        StringBuilder dump = new StringBuilder();
        dump.append("Thread count: " + threadMXBean.getThreadCount());
        dump.append("\nCurrent thread CPU time: " + threadMXBean.getCurrentThreadCpuTime());
        dump.append("\nCurrent thread User time: " + threadMXBean.getCurrentThreadUserTime());

        dump.append(String.format("%n"));
        for (ThreadInfo threadInfo : threadInfos) {
            dump.append(threadInfo);
        }

        long[] deadLocks = threadMXBean.findDeadlockedThreads();
        if (deadLocks != null && deadLocks.length > 0) {
            ThreadInfo[] deadlockedThreads = threadMXBean.getThreadInfo(deadLocks);
            dump.append(String.format("%n"));
            dump.append("Deadlock is detected!");
            dump.append(String.format("%n"));
            for (ThreadInfo threadInfo : deadlockedThreads) {
                dump.append(threadInfo);
            }
        }
        return dump.toString();
    }

    public synchronized void dumpStack(PrintStream ps) {

        ThreadMXBean theadMxBean = ManagementFactory.getThreadMXBean();

        for (ThreadInfo ti : theadMxBean.dumpAllThreads(true, true)) {
            System.out.print(ti.toString());

            // ThreadInfo only prints out the first 8 lines, so make sure
            // we write out the rest
            StackTraceElement ste[] = ti.getStackTrace();
            if (ste.length > 8) {
                ps.println("[Extra stack]");
                for (int element = 8; element < ste.length; element++) {
                    ps.println("\tat " + ste[element]);
                    for (MonitorInfo mi : ti.getLockedMonitors()) {
                        if (mi.getLockedStackDepth() == element) {
                            ps.append("\t-  locked " + mi);
                            ps.append('\n');
                        }
                    }
                }
                ps.println("[Extra stack]");
            }
        }
    }

    public static void getCpuLoad() throws Exception {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[] { "SystemCpuLoad", "ProcessCpuLoad" });

        if (list.isEmpty()) {
            System.out.println("[ProcessCpuLoad,SystemCpuLoad]" + Double.NaN);
            return;
        }
        Double val = (Double) ((Attribute) list.get(0)).getValue();
        Double value = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
        System.out.println("[SystemCpuLoad]" + value);
        if (list.size() > 1) {
            val = (Double) ((Attribute) list.get(1)).getValue();
            value = val == -1.0 ? Double.NaN : ((int) (val * 1000) / 10.0);
            System.out.println("[ProcessCpuLoad]" + value);
        }
    }

    public static void main(String[] args) throws Exception {

        HistoryThreadTest.getCpuLoad();
    }

}
