package com.sos.jobscheduler.history.master;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.joc.cluster.configuration.JocConfiguration;

public class HistoryMainTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMainTest.class);

    public static void exitAfter(HistoryMain history, int seconds) {
        Thread thread = new Thread() {

            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s][start][exitAfter]%ss...", name, seconds));
                try {
                    Thread.sleep(seconds * 1_000);
                } catch (InterruptedException e) {
                    LOGGER.info(String.format("[%s][exception][exitAfter]%s", name, e.toString()), e);
                }
                history.stop();
                LOGGER.info(String.format("[%s][end][exitAfter]%ss", name, seconds));
            }
        };
        thread.start();
    }

    public static Configuration getConfiguration(String ini) throws Exception {
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
        Configuration config = new Configuration();
        try {
            config.setHibernateConfiguration(Paths.get(iniFile.getParent(), conf.getProperty("hibernate_configuration").trim()));
            config.getMailer().load(conf);
            config.getHandler().load(conf);
            config.getHttpClient().load(conf);
            config.getWebservice().load(conf);
            // HistoryMasterConfiguration hm = new HistoryMasterConfiguration();
            // hm.load(conf);
            // config.addMaster(hm);

            LOGGER.info(SOSString.toString(config));
            LOGGER.info(String.format("[%s]END", method));
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            throw new Exception(String.format("[%s]%s", method, e.toString()), e);
        }

        return config;
    }

    public static void main(String[] args) throws Exception {
        HistoryMain history = new HistoryMain(new JocConfiguration(System.getProperty("user.dir"), TimeZone.getDefault().getID()));

        HistoryMainTest.exitAfter(history, 60); // exit after n seconds
        history.start();

    }

}
