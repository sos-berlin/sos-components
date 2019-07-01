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
import com.sos.jobscheduler.event.master.handler.configuration.HandlerConfiguration;
import com.sos.jobscheduler.history.master.configuration.HistoryMasterConfiguration;

public class HistoryMainTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMainTest.class);

    public HandlerConfiguration getConfiguration(String ini) throws Exception {
        String method = "getConfiguration";

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

        HandlerConfiguration hc = null;
        try {
            HistoryMasterConfiguration mc = new HistoryMasterConfiguration(conf);

            Path hibernateConfiguration = Paths.get(iniFile.getParent(), conf.getProperty("hibernate_configuration").trim());

            hc = new HandlerConfiguration();
            hc.setHibernateConfiguration(hibernateConfiguration);
            hc.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
            hc.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
            hc.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
            hc.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
            hc.setMailFrom(conf.getProperty("mail_from").trim());
            hc.setMailTo(conf.getProperty("mail_to").trim());

            LOGGER.info(SOSString.toString(hc));
            LOGGER.info(SOSString.toString(mc));

            hc.addMaster(mc);
            LOGGER.info(String.format("[%s]END", method));
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            throw new ServletException(String.format("[%s]%s", method, e.toString()), e);
        }

        return hc;
    }

    public static void main(String[] args) throws Exception {
        HistoryMainTest t = new HistoryMainTest();

        HandlerConfiguration settings = t.getConfiguration(args.length == 1 ? args[0] : "src/test/resources/history_configuration.ini");
        HistoryMain history = new HistoryMain(settings);

        try {
            history.start();
        } catch (Exception e) {
            throw e;
        } finally {
            Thread.sleep(2 * 60_000);
            history.exit();
        }
    }

}
