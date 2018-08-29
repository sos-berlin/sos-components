package com.sos.jobscheduler.event.master;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.ISender;
import com.sos.jobscheduler.event.master.handler.LoopEventHandler;

public class LoopEventHandlerTest {

    public static void main(String[] args) throws Exception {
        ISender sender = null;
        LoopEventHandler eh = new LoopEventHandler(sender, EventPath.fatEvent, Entry.class);
        try {
            EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
            ms.setHttpHost("localhost");
            ms.setHttpPort("4444");
            ms.useLogin(true);
            ms.setUser("test");
            ms.setPassword("12345");

            eh.init(ms);
            eh.start(new Long(0));
        } catch (Exception e) {
            throw e;
        } finally {
            eh.close();
        }
    }

}
