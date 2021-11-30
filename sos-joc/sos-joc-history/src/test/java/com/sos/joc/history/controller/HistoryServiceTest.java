package com.sos.joc.history.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.common.JocSecurityLevel;

public class HistoryServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceTest.class);

    private static void stopAfter(IJocClusterService service, StartupMode mode, int seconds) {
        LOGGER.info(String.format("[start][stopAfter][%ss]...", seconds));

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {

        } finally {
            service.stop(mode);
        }
        LOGGER.info(String.format("[end][stopAfter][%ss]", seconds));
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
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Path resDir = Paths.get("src/test/resources");

        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.sosCockpitProperties.getProperties().setProperty("history_log_dir", resDir.resolve("logs").toString());

        JocConfiguration jocConfig = new JocConfiguration(resDir.toString(), "UTC", resDir.resolve("hibernate.cfg.xml"), resDir, JocSecurityLevel.LOW,
                "", 0);

        HistoryService service = new HistoryService(jocConfig, new ThreadGroup(JocClusterConfiguration.IDENTIFIER));
        AConfigurationSection configuration = null;
        service.start(getControllers(), configuration, StartupMode.manual_restart);
        HistoryServiceTest.stopAfter(service, StartupMode.manual_restart, 60);

    }

}
