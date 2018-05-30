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
        ms1.setHost(schedulerHost);
        ms1.setHttpHost(schedulerHost);
        ms1.setHttpPort(schedulerPort);

        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(hibernateConfigFile);
        s.addMaster(ms1);

        EventHandlerMasterSettings ms2 = new EventHandlerMasterSettings();
        ms2.setSchedulerId(schedulerId + "XXXX");
        ms2.setHost(schedulerHost + "XXX");
        ms2.setHttpHost(schedulerHost + "XXX");
        ms2.setHttpPort(schedulerPort + "1");
        // s.addMaster(ms2);

        HistoryEventHandler eventHandler = new HistoryEventHandler(s);
        try {
            eventHandler.start();
        } catch (Exception e) {
            throw e;
        } finally {
            Thread.sleep(2 * 60 * 1000);

            eventHandler.exit();
        }
    }

}
