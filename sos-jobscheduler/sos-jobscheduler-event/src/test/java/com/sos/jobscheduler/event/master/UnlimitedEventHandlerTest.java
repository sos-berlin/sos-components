package com.sos.jobscheduler.event.master;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.fatevent.bean.FatEntry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.UnlimitedEventHandler;

public class UnlimitedEventHandlerTest {

    public static void main(String[] args) throws Exception {
        UnlimitedEventHandler eh = new UnlimitedEventHandler(EventPath.fatEvent, FatEntry.class);
        try {
            EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
            ms.setSchedulerId("jobscheduler2");
            ms.setHost("localhost");
            ms.setHttpHost("localhost");
            ms.setHttpPort("4444");

            // eh.setWebserviceDelay(2);
            eh.init(ms);
            eh.start(new Long(0));
        } catch (Exception e) {
            throw e;
        } finally {
            eh.close();
        }
    }

}
