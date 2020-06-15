package com.sos.webservices.order.initiator;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Test;

public class TestOrderInitiator {

    @Test
    public void testOrderInitatorGo() throws Exception {
        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(getSettings());
        orderInitiatorRunner.run();
    }

    private OrderInitiatorSettings getSettings() throws Exception {
        String method = "getSettings";

        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

        String jettyBase = System.getProperty("jetty.base");
        String orderConfiguration = "src/test/resources/dailyplan.properties";
        orderInitiatorSettings.setPropertiesFile("/dailyplan.properties");
        Path hc = null;
        if (orderConfiguration.contains("..")) {
            hc = Paths.get(jettyBase, orderConfiguration);
        } else {
            hc = Paths.get(orderConfiguration);
        }
        String cp = hc.toFile().getCanonicalPath();

        Properties conf = new Properties();

        try (FileInputStream in = new FileInputStream(cp)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the history configuration: %s", method, cp, ex.toString()), ex);
        }

        orderInitiatorSettings.setDayOffset(conf.getProperty("day_offset"));
        orderInitiatorSettings.setJobschedulerUrl(conf.getProperty("jobscheduler_url"));
        orderInitiatorSettings.setRunOnStart("true".equalsIgnoreCase(conf.getProperty("run_on_start", "true")));
        orderInitiatorSettings.setRunInterval(conf.getProperty("run_interval", "1440"));
        orderInitiatorSettings.setFirstRunAt(conf.getProperty("first_run_at", "00:00:00"));
        String hibernateConfiguration = conf.getProperty("hibernate_configuration_file");
        if (hibernateConfiguration != null) {
            hibernateConfiguration = hibernateConfiguration.trim();
        }
        if (hibernateConfiguration.contains("..")) {
            hc = Paths.get(jettyBase, hibernateConfiguration);
        } else {
            hc = Paths.get(hibernateConfiguration);
        }

        orderInitiatorSettings.setHibernateConfigurationFile(hc);
        orderInitiatorSettings.setOrderTemplatesDirectory(conf.getProperty("order_templates_directory"));

        return orderInitiatorSettings;
    }

}
