package com.sos.jobscheduler.history.master;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;

public class HistoryEventHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventHandlerTest.class);

    public EventHandlerSettings getEventHandlerSettings(String ini) throws Exception {
        String method = "getEventHandlerSettings";

        LOGGER.info(String.format("[%s]START...", method));

        File iniFile = Paths.get(ini).toFile();

        LOGGER.info(String.format("[%s][%s]%s", method, ini, iniFile.getCanonicalPath()));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(ini)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the history configuration: %s", method, ini, ex.toString()), ex);
        }

        LOGGER.info(String.format("[%s]%s", method, conf));

        EventHandlerSettings s = null;
        try {
            EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
            ms.setMasterId(conf.getProperty("master_id").trim());
            ms.setHostname(conf.getProperty("master_hostname").trim());
            ms.setPort(conf.getProperty("master_port").trim());
            ms.useLogin(Boolean.parseBoolean(conf.getProperty("master_use_login").trim()));
            ms.setUser(conf.getProperty("master_user").trim());
            ms.setPassword(conf.getProperty("master_user_password").trim());

            ms.setMaxTransactions(Integer.parseInt(conf.getProperty("max_transactions").trim()));

            ms.setKeepEventsInterval(Integer.parseInt(conf.getProperty("webservice_keep_events_interval").trim()));

            ms.setWebserviceTimeout(Integer.parseInt(conf.getProperty("webservice_timeout").trim()));
            ms.setWebserviceLimit(Integer.parseInt(conf.getProperty("webservice_limit").trim()));
            ms.setWebserviceDelay(Integer.parseInt(conf.getProperty("webservice_delay").trim()));

            ms.setHttpClientConnectTimeout(Integer.parseInt(conf.getProperty("http_client_connect_timeout").trim()));
            ms.setHttpClientConnectionRequestTimeout(Integer.parseInt(conf.getProperty("http_client_connection_request_timeout").trim()));
            ms.setHttpClientSocketTimeout(Integer.parseInt(conf.getProperty("http_client_socket_timeout").trim()));

            ms.setWaitIntervalOnError(Integer.parseInt(conf.getProperty("wait_interval_on_error").trim()));
            ms.setWaitIntervalOnEmptyEvent(Integer.parseInt(conf.getProperty("wait_interval_on_empty_event").trim()));
            ms.setMaxWaitIntervalOnEnd(Integer.parseInt(conf.getProperty("max_wait_interval_on_end").trim()));

            String hibernateConfiguration = conf.getProperty("hibernate_configuration").trim();
            Path hc = Paths.get(iniFile.getParent(), hibernateConfiguration);

            s = new EventHandlerSettings();
            s.setHibernateConfiguration(hc);
            s.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
            s.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
            s.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
            s.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
            s.setMailFrom(conf.getProperty("mail_from").trim());
            s.setMailTo(conf.getProperty("mail_to").trim());

            LOGGER.info(SOSString.toString(s));
            LOGGER.info(SOSString.toString(ms));

            s.addMaster(ms);
            LOGGER.info(String.format("[%s]END", method));
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            throw new ServletException(String.format("[%s]%s", method, e.toString()), e);
        }

        return s;
    }

    public static void main(String[] args) throws Exception {
        HistoryEventHandlerTest t = new HistoryEventHandlerTest();

        args = new String[] { "src/test/resources/history_configuration.ini" };

        EventHandlerSettings s = null;
        if (args.length == 1) {
            s = t.getEventHandlerSettings(args[0]);
        } else {
            s = new EventHandlerSettings();
            EventHandlerMasterSettings ms1 = new EventHandlerMasterSettings();

            String masterId = "jobscheduler2";
            String masterHost = "localhost";
            String masterPort = "4444";
            Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");

            ms1.setMasterId(masterId);
            ms1.setHostname(masterHost);
            ms1.setPort(masterPort);
            ms1.useLogin(true);
            ms1.setUser("test");
            ms1.setPassword("12345");

            ms1.setMaxTransactions(100);

            ms1.setWebserviceTimeout(60);
            ms1.setWebserviceLimit(1000);
            ms1.setWebserviceDelay(1);

            ms1.setHttpClientConnectTimeout(30_000);
            ms1.setHttpClientConnectionRequestTimeout(30_000);
            ms1.setHttpClientSocketTimeout(75_000);

            ms1.setWaitIntervalOnError(2_000);
            ms1.setWaitIntervalOnEmptyEvent(1_000);
            ms1.setMaxWaitIntervalOnEnd(30_000);

            ms1.setKeepEventsInterval(1);

            s.setHibernateConfiguration(hibernateConfigFile);
            // s.setMailSmtpHost("localhost");
            // s.setMailSmtpPort("25");
            // s.setMailFrom("jobscheduler2.0@localhost");
            // s.setMailTo("to@localhost");

            s.addMaster(ms1);
        }

        HistoryEventHandler eventHandler = new HistoryEventHandler(s);
        try {
            eventHandler.start();
        } catch (Exception e) {
            throw e;
        } finally {
            Thread.sleep(1 * 60 * 1000);
            eventHandler.exit();
        }
    }

}
