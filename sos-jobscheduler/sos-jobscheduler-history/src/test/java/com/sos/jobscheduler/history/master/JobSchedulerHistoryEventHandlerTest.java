package com.sos.jobscheduler.history.master;

import java.nio.file.Paths;

import com.sos.jobscheduler.event.master.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.EventHandlerSettings;

public class JobSchedulerHistoryEventHandlerTest {

    public static void main(String[] args) throws Exception {
        String baseDir = "C:/jobscheduler/jobscheduler_data/";
        String schedulerId = "jobscheduler2";
        String host = "host";
        String port = "40444";
        String configDir = baseDir + schedulerId + "/config";

        EventHandlerMasterSettings ms1 = new EventHandlerMasterSettings();
        ms1.setSchedulerId(schedulerId);
        ms1.setHost(host);
        ms1.setHttpHost(host);
        ms1.setHttpPort(port);
        ms1.setConfigDirectory(Paths.get(configDir));
        ms1.setLiveDirectory(ms1.getConfigDirectory().resolve("live"));

        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(ms1.getConfigDirectory().resolve("reporting.hibernate.cfg.xml"));
        s.addMaster(ms1);

        EventHandlerMasterSettings ms2 = new EventHandlerMasterSettings();
        ms2.setSchedulerId(schedulerId + "XXXX");
        ms2.setHost(host + "XXX");
        ms2.setHttpHost(host + "XXX");
        ms2.setHttpPort(port);
        ms2.setConfigDirectory(Paths.get(configDir));
        ms2.setLiveDirectory(ms2.getConfigDirectory().resolve("live"));
        s.addMaster(ms2);

        JobSchedulerHistoryEventHandler eventHandler = new JobSchedulerHistoryEventHandler(s);
        try {
            eventHandler.start();
        } catch (Exception e) {
            throw e;
        } finally {
            Thread.sleep(10 * 1000);

            eventHandler.exit();
        }
    }

}
