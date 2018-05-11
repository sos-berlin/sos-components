package com.sos.jobscheduler.event.master;

import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventPath;

public class JobSchedulerUnlimitedEventHandlerTest {

    public static void main(String[] args) throws Exception {
        JobSchedulerUnlimitedEventHandler eh = new JobSchedulerUnlimitedEventHandler();
        try {
            EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
            ms.setSchedulerId("jobscheduler2");
            ms.setHost("localhost");
            ms.setHttpHost("localhost");
            ms.setHttpPort("4444");
            
            //eh.setWebserviceDelay(2);
            eh.init(ms);
            eh.start(EventPath.fatEvent, new Long(0));
        } catch (Exception e) {
            throw e;
        } finally {
            eh.close();
        }
    }

}
