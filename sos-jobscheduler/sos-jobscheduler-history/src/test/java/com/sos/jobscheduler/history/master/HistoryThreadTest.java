package com.sos.jobscheduler.history.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;

public class HistoryThreadTest {

    public void dumpThreadDump() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        for (ThreadInfo ti : threadMxBean.dumpAllThreads(true, true)) {
            System.out.print("[dumpThread]" + ti.toString());
        }
    }

    public String dumpThreads() {
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

    public static Map<String, Object> getUniqueConstraintFields(Object o) throws Exception {
        Map<String, Object> fields = new LinkedHashMap<>();
        Class<?> clazz = o.getClass();
        Table ta = clazz.getDeclaredAnnotation(Table.class);
        if (ta == null) {
            throw new Exception(String.format("[%s]missing @Table annotation", clazz.getSimpleName()));
        }
        UniqueConstraint[] ucs = ta.uniqueConstraints();
        if (ucs == null || ucs.length == 0) {
            throw new Exception(String.format("[%s][@Table]uniqueConstraints annotation is null or empty", clazz.getSimpleName()));
        }

        for (int i = 0; i < ucs.length; i++) {
            UniqueConstraint uc = ucs[i];
            String[] columnNames = uc.columnNames();
            if (columnNames == null || columnNames.length == 0) {
                throw new Exception(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]columnNames annotation is null or empty", clazz
                        .getSimpleName()));
            }
            for (int j = 0; j < columnNames.length; j++) {
                String columnName = columnNames[j];
                Optional<Field> of = Arrays.stream(clazz.getDeclaredFields()).filter(m -> m.isAnnotationPresent(Column.class) && m.getAnnotation(
                        Column.class).name().equals(columnName)).findFirst();
                if (of.isPresent()) {
                    Field field = of.get();
                    field.setAccessible(true);
                    fields.put(field.getName(), field.get(o));
                } else {
                    throw new Exception(String.format("[%s][@Table][uniqueConstraints @UniqueConstraint]can't find %s annoted field", clazz
                            .getSimpleName(), columnName));
                }
            }
        }

        return fields.size() == 0 ? null : fields;
    }

    public static void insertObject(SOSHibernateSession session, Class<?> clazz, Object o, Map<String, Object> fields) throws Exception {

        // SOSHibernateFactory f = new SOSHibernateFactory();
        // SOSHibernateSession s = f.openStatelessSession();

        StringBuilder hql = new StringBuilder("from ").append(clazz.getSimpleName()).append(" where ");
        int i = 0;
        int size = fields.size();
        for (String key : fields.keySet()) {
            i++;

            // check is null?
            hql.append(key).append("=:").append(key);
            if (i < size) {
                hql.append(" and ");
            }
        }

        Query<?> query = session.createQuery(hql.toString(), clazz);
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            query.setParameter(key, value);
        }

        Object result = session.getSingleResult(query);
        if (result == null) {
            session.save(o);
        } else {
            Object id = getId(result);
            System.out.println("AAAAA=" + id);
            setId(o, id);
            session.update(o);
        }
        System.out.println(hql);

    }

    public static Object getId(Object item) throws Exception {
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            if (of.isPresent()) {
                Field field = of.get();
                field.setAccessible(true);
                return field.get(item);
            }
        }
        return null;
    }

    public static void setId(Object item, Object value) throws Exception {
        if (item != null) {
            Optional<Field> of = Arrays.stream(item.getClass().getDeclaredFields()).filter(m -> m.isAnnotationPresent(Id.class)).findFirst();
            if (of.isPresent()) {
                Field field = of.get();
                field.setAccessible(true);
                field.set(item, value);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        DBItemAgentTest agent = new DBItemAgentTest();
        agent.setMasterId("jobscheduler2");
        agent.setPath("agent_4445");
        agent.setUri("http://localhost:4445");
        agent.setTimezone("Europe/Berlin");
        agent.setStartTime(new Date());
        agent.setLastEntry(false);
        agent.setEventId("x");
        agent.setCreated(new Date());

        agent.equals(agent);
        
        
        /**
        // Map<String, Object> fields = HistoryThreadTest.getUniqueConstraintFields(agent);

        Path hibernateConfigFile = Paths.get("src/test/resources/history_configuration.ini");
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(hibernateConfigFile.toFile().getCanonicalPath())) {
            props.load(in);
        }

        System.out.println(props);

        Path path = Paths.get(".");
        try (Stream<String> stream = Files.lines(path)) {
            long lineCount = stream.count();
        }

        Stream<String> stream = Files.lines(path);
        stream.close();
        
        File[] f = new File(".").listFiles(file -> file.getName().toLowerCase().endsWith(".pdf"));
        
        
        //File[]​ files = null;// ​new​ ​File​(​"."​).listFiles(file -> file.isHidden());*/
        
    }

}
