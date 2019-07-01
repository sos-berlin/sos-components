package com.sos.jobscheduler.event.master.handler;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.LoopEventHandler;
import com.sos.jobscheduler.event.master.handler.configuration.Master;
import com.sos.jobscheduler.event.master.handler.configuration.MasterConfiguration;
import com.sos.jobscheduler.event.master.handler.notifier.INotifier;

public class LoopEventHandlerTest {

    public static void main(String[] args) throws Exception {
        INotifier notifier = null;
        LoopEventHandler eh = new LoopEventHandler(EventPath.fatEvent, Entry.class, notifier);
        try {
            Master primary = new Master("jobscheduler2", "http://localhost:4444", "test", "12345");
            Master backup = null;

            MasterConfiguration conf = new MasterConfiguration(primary, backup);
            eh.init(conf);
            eh.start(new Long(0));
        } catch (Exception e) {
            throw e;
        } finally {
            eh.close();
        }
    }

}
