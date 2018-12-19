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
            EventHandlerMasterSettings ms = new EventHandlerMasterSettings(conf);

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

        EventHandlerSettings settings = t.getEventHandlerSettings(args.length == 1 ? args[0] : "src/test/resources/history_configuration.ini");
        HistoryEventHandler eventHandler = new HistoryEventHandler(settings);

        try {
            eventHandler.start();
        } catch (Exception e) {
            throw e;
        } finally {
            Thread.sleep(2 * 60_000);
            eventHandler.exit();
        }
    }

}
