package com.sos.jobscheduler.history.master;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;

public class HistoryEventHandlerTest {

    public static void main(String[] args) throws Exception {
        String schedulerId = "jobscheduler2";
        String schedulerHost = "localhost";
        String schedulerPort = "4444";
        Path hibernateConfigFile = Paths.get("src/test/resources/hibernate.cfg.xml");

        EventHandlerMasterSettings ms1 = new EventHandlerMasterSettings();
        ms1.setSchedulerId(schedulerId);
        ms1.setHttpHost(schedulerHost);
        ms1.setHttpPort(schedulerPort);
        ms1.useLogin(true);
        ms1.setUser("test");
        ms1.setPassword("12345");

        ms1.setWebserviceTimeout(60);
        ms1.setWebserviceLimit(1000);
        ms1.setWebserviceDelay(1);

        ms1.setHttpClientConnectTimeout(30_000);
        ms1.setHttpClientConnectionRequestTimeout(30_000);
        ms1.setHttpClientSocketTimeout(75_000);

        ms1.setWaitIntervalOnError(30_000);
        ms1.setWaitIntervalOnEmptyEvent(1_000);
        ms1.setMaxWaitIntervalOnEnd(30_000);
        
        EventHandlerMasterSettings ms2 = new EventHandlerMasterSettings();
        ms2.setSchedulerId(schedulerId + "XXXX");
        ms2.setHttpHost(schedulerHost + "XXX");
        ms2.setHttpPort(schedulerPort + "1");
        // s.addMaster(ms2);

        
        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(hibernateConfigFile);
        s.addMaster(ms1);

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
