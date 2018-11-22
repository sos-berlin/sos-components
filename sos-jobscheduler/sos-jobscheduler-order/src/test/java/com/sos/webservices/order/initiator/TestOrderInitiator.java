package com.sos.webservices.order.initiator;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Ignore;
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
		String orderConfiguration = "src/test/resources/order_configuration.ini";
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
			throw new Exception(
					String.format("[%s][%s]error on read the history configuration: %s", method, cp, ex.toString()),
					ex);
		}
		
		orderInitiatorSettings.setDayOffset(conf.getProperty("day_offset"));
		String hibernateConfiguration = conf.getProperty("hibernate_configuration").trim();
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
