package com.sos.webservices.order.initiator;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestOrderInitiator {

    @Test
    public void testOrderInitatorGo() {

        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

        String jettyBase = System.getProperty("jetty.base");
        String hibernateConfiguration = "src/test/resources/hibernate_jobscheduler2.cfg.xml";
        Path hc = null;
        if (hibernateConfiguration.contains("..")) {
            hc = Paths.get(jettyBase, hibernateConfiguration);
        } else {
            hc = Paths.get(hibernateConfiguration);
        }
        orderInitiatorSettings.setHibernateConfigurationFile(hc);
        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings);
        orderInitiatorRunner.run();
    }

}
