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

public class HibernateProxyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateProxyTest.class);

    /** Expected result - not found (all DBItem classes are annotated with @Proxy(lazy = false)) */
    @Ignore
    @Test
    public void testJocClassMapping() throws Exception {
        createCloseFactory(DBLayer.getJocClassMapping(), 3);
        executeJCMDCommand();
    }

    /** Expected result - 1 found (the DBItem class is not annotated with @Proxy(lazy = false)) */
    @Ignore
    @Test
    public void testSingleClassMapping() throws Exception {
        SOSClassList mapping = new SOSClassList();
        mapping.add(DBItemATest.class);
        createCloseFactory(mapping, 30);
        executeJCMDCommand();
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

    private void executeJCMDCommand() {
        Integer pid = SOSShell.getPID();
        LOGGER.info("PID=" + pid + ", JAVA_HOME=" + SOSShell.getJavaHome());

        SOSCommandResult r = SOSShell.executeCommand("jcmd " + pid + " VM.class_hierarchy");
        // LOGGER.info(r.getStdOut());

        List<String> l = new ArrayList<>();
        try (Scanner s = new Scanner(r.getStdOut())) {
            while (s.hasNext()) {
                String line = s.next();
                if (line.contains("com.sos.")) {
                    if (line.contains("$HibernateProxy")) {
                        LOGGER.info("### com.sos...$HibernateProxy$... #########################");
                        l.add(line);
                    }
                    LOGGER.info(line);
                }
            }
        }
        LOGGER.info("### com.sos...$HibernateProxy$... #########################");
        if (l.size() == 0) {
            LOGGER.info("not found"); // expected result
        } else {
            l.stream().sorted().forEach(line -> LOGGER.info(line));
        }
    }

}
