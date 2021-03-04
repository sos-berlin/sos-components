package com.sos.js7.history.controller;

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
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;

public class HistoryServiceTest {

    public static void exitAfter(HistoryService history, int seconds) {

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
        history.stop(StartupMode.manual);

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
        p.setProperty("controller_id", "js7.x");
        p.setProperty("primary_controller_uri", "http://localhost:5444");

        List<ControllerConfiguration> list = new ArrayList<ControllerConfiguration>();
        ControllerConfiguration c = new ControllerConfiguration();
        try {
            c.load(p);
            list.add(c);
        } catch (Exception e) {
        }

        return list;
    }

    @Ignore
    @Test
    public void test() throws Exception {
        Globals.sosCockpitProperties = new JocCockpitProperties();

        Path resDir = Paths.get("src/test/resources");
        JocConfiguration jocConfig = new JocConfiguration(resDir.toString(), "UTC", resDir.resolve("hibernate.cfg.xml"), resDir, JocSecurityLevel.LOW,
                "", 0);

        HistoryService hm = new HistoryService(jocConfig, new ThreadGroup(JocClusterConfiguration.IDENTIFIER));
        GlobalSettingsSection settings = null;
        hm.start(getControllers(), settings, StartupMode.manual);
        HistoryServiceTest.exitAfter(hm, 2 * 60);

    }

}
