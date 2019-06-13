package com.sos.jobscheduler.event.master;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.LoopEventHandler;
import com.sos.jobscheduler.event.master.handler.MasterSettings;
import com.sos.jobscheduler.event.master.handler.notifier.INotifier;

public class LoopEventHandlerTest {

    public static void main(String[] args) throws Exception {
        INotifier notifier = null;
        LoopEventHandler eh = new LoopEventHandler(EventPath.fatEvent, Entry.class, notifier);
        try {
            MasterSettings primaryMaster = new MasterSettings("jobscheduler2", "localhost", "4444", "test", "12345");
            MasterSettings backupMaster = null;

            EventHandlerMasterSettings ms = new EventHandlerMasterSettings(primaryMaster, backupMaster);
            eh.init(ms);
            eh.start(new Long(0));
        } catch (Exception e) {
            throw e;
        } finally {
            eh.close();
        }
    }

}
