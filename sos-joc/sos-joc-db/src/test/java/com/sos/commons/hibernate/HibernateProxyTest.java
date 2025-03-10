package com.sos.commons.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.beans.SOSCommandResult;

public class HibernateProxyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateProxyTest.class);

    @Ignore
    @Test
    public void testJCMDCommand() throws Exception {
        createCloseFactory(3);
        executeJCMDCommand();
    }

    private void createCloseFactory(int counter) {
        for (int i = 0; i < counter; i++) {
            SOSHibernateFactory factory = null;
            try {
                factory = SOSHibernateTest.createFactory();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            } finally {
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
        Scanner s = new Scanner(r.getStdOut());
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

        LOGGER.info("### com.sos...$HibernateProxy$... #########################");
        if (l.size() == 0) {
            LOGGER.info("not found");
        } else {
            l.stream().forEach(line -> LOGGER.info(line));
        }
    }

}
