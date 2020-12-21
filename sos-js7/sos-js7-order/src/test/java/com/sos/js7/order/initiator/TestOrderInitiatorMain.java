package com.sos.js7.order.initiator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class TestOrderInitiatorMain {

    public static void exitAfter(OrderInitiatorMain history, int seconds) {

        boolean run = true;
        int counter = 0;
        while (run) {
            if (counter >= seconds) {
                run = false;
            } else {
                try {
                    Thread.sleep(1 * 1_000);
                    counter = counter + 1;
                } catch (InterruptedException e) {

                }
            }
        }
        history.stop();

        counter = 0;
        while (run) {
            if (counter >= 2) {
                run = false;
            } else {
                try {
                    Thread.sleep(seconds * 1_000);
                    counter = counter + 1;
                } catch (InterruptedException e) {

                }
            }
        }
    }

    private List<ControllerConfiguration> getControllers() {
        Properties p = new Properties();
        p.setProperty("jobscheduler_id", "controller");
        p.setProperty("primary_master_uri", "http://localhost:4424");

        List<ControllerConfiguration> list = new ArrayList<ControllerConfiguration>();
        ControllerConfiguration c = new ControllerConfiguration();
        try {
            c.load(p);
            list.add(c);
        } catch (Exception e) {
        }

        return list;
    }

    @Test
    public void test() throws Exception {
        Globals.sosCockpitProperties = new JocCockpitProperties();

        Path resDir = Paths.get("src/test/resources");
        JocConfiguration jocConfig = new JocConfiguration(resDir.toString(), "UTC", resDir.resolve("hibernate.cfg.xml"), resDir, JocSecurityLevel.LOW
                .value(), "", 0);

        OrderInitiatorMain hm = new OrderInitiatorMain(jocConfig, new ThreadGroup(JocClusterConfiguration.IDENTIFIER));
        hm.start(getControllers());
        TestOrderInitiatorMain.exitAfter(hm, 3 * 60);

    }

}
