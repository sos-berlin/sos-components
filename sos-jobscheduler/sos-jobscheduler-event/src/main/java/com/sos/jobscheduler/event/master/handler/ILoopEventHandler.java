package com.sos.jobscheduler.event.master.handler;

import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.handler.configuration.IMasterConfiguration;

public interface ILoopEventHandler {

    void init(IMasterConfiguration conf);

    void run();

    Long onEmptyEvent(Long eventId, Event event);

    Long onNonEmptyEvent(Long eventId, Event event);

    Long onTornEvent(Long eventId, Event event);

    void onRestart(Long eventId, Event event);

    void setIdentifier(String identifier);

    void close();

    void awaitEnd();

    void setMasterConfiguration(IMasterConfiguration conf);

    IMasterConfiguration getMasterConfiguration();
}
