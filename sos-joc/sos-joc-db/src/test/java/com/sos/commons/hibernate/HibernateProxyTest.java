package com.sos.commons.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.helpers.dbitems.DBItemATest;
import com.sos.commons.util.SOSClassList;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.joc.db.DBLayer;

/** @see <a href="https://hibernate.atlassian.net/browse/HHH-14694">HHH-14694</a>
 * @see <a href="https://discourse.hibernate.org/t/outofmemoryerror-metaspace-with-bytebuddy-hhh-14694/6391/4"> OutOfMemoryError Metaspace with ByteBuddy</a>
 * 
 *      <h2>Short explanation</h2>
 * 
 *      <h3>Hibernate 5.x - memory leak possible</h3>
 *      <ul>
 *      <li><b>Cause:</b> When a database job uses a DBItem, each job run adds new Proxy objects to the Hibernate cache, even though the factory is properly
 *      closed after each run.</li>
 *      <li><b>Consequence:</b> Eventually, memory becomes insufficient.</li>
 *      <li><b>Solution:</b> Use {@code @Proxy(lazy = false)} annotation on the DBItem.</li>
 *      </ul>
 * 
 *      <h3>Hibernate 6.6 - problem fixed by Hibernate</h3>
 *      <ul>
 *      <li>{@code @Proxy(lazy = false)} is marked for removal but still works - no proxies are generated.</li>
 *      <li>Without {@code @Proxy(lazy = false)}: A {@code $HibernateProxy$} class is generated, but only once per DBItem, regardless of how many factories are
 *      opened/closed.</li>
 *      </ul>
 * 
 *      <h3>Hibernate 7.3.1</h3>
 *      <ul>
 *      <li>{@code @Proxy(lazy = false)} has been removed.</li>
 *      <li>Behavior is the same as described for Hibernate 6.6.</li>
 *      </ul>
 */
public class HibernateProxyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateProxyTest.class);

    /** Expected result - not found (all DBItem classes are annotated with @Proxy(lazy = false)) */
    @Ignore
    @Test
    public void testJocClassMapping() throws Exception {
        createCloseFactory(DBLayer.getJocClassMapping(), 3);

        Integer pid = SOSShell.getPID();
        LOGGER.info("PID=" + pid + ", JAVA_HOME=" + SOSShell.getJavaHome());
        executeJCMDCommand(pid, "VM.class_hierarchy", true);
    }

    /** Expected result - 1 found (the DBItem class is not annotated with @Proxy(lazy = false)) */
    @Ignore
    @Test
    public void testSingleClassMapping() throws Exception {
        Integer pid = SOSShell.getPID();
        LOGGER.info("PID=" + pid + ", JAVA_HOME=" + SOSShell.getJavaHome());

        // if java -XX:NativeMemoryTracking=detail set
        executeJCMDCommand(pid, "VM.native_memory baseline", false);

        SOSClassList mapping = new SOSClassList();
        mapping.add(DBItemATest.class);
        createCloseFactory(mapping, 30);

        executeJCMDCommand(pid, "VM.class_hierarchy", true);
        executeJCMDCommand(pid, "VM.native_memory summary.diff scale=MB", false);
        executeJCMDCommand(pid, "GC.class_histogram", true); // current HEAP
    }

    private void createCloseFactory(SOSClassList mapping, int counter) {
        for (int i = 0; i < counter; i++) {
            SOSHibernateFactory factory = null;
            SOSHibernateSession session = null;
            try {
                factory = SOSHibernateTest.createFactory(mapping);
                session = factory.openStatelessSession();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            } finally {
                if (session != null) {
                    session.close();
                }
                if (factory != null) {
                    factory.close();
                }
            }
        }
    }

    private void executeJCMDCommand(Integer pid, String command, boolean checkProxy) {
        LOGGER.info("####################################################");
        LOGGER.info("# jcmd " + pid + " " + command);
        LOGGER.info("####################################################");

        SOSCommandResult r = SOSShell.executeCommand("jcmd " + pid + " " + command);
        if (checkProxy) {
            List<String> l = new ArrayList<>();
            try (Scanner s = new Scanner(r.getStdOut())) {
                while (s.hasNext()) {
                    String line = s.next();
                    if (line.contains("com.sos.")) {
                        boolean found = false;
                        if (line.contains("$HibernateProxy")) {
                            found = true;
                            l.add(line);
                        }
                        if (found) {
                            LOGGER.info(
                                    "-----------------------------------------------------------------------------------------------------------");
                        }
                        LOGGER.info(line);
                        if (found) {
                            LOGGER.info(
                                    "-----------------------------------------------------------------------------------------------------------");
                        }
                    }
                }
            }
            LOGGER.info("### Result with com.sos...$HibernateProxy$...:");
            if (l.size() == 0) {
                LOGGER.info("   Not found"); // expected result
            } else {
                l.stream().sorted().forEach(line -> LOGGER.info(line));
            }
        } else {
            LOGGER.info(r.getStdOut());
        }
    }

}
