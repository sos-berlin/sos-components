package com.sos.jobscheduler.event.master.handler;

import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;

public interface ILoopEventHandler {

    void init(MasterConfiguration masterConfiguration);

    void run();

    void onProcessingStart(Long eventId);

    void onProcessingEnd(Long eventId);

    Long onEmptyEvent(Long eventId, Event event);

    Long onNonEmptyEvent(Long eventId, Event event);

    Long onTornEvent(Long eventId, Event event);

    void onRestart(Long eventId, Event event);

    void setIdentifier(String identifier);

    String getIdentifier();

    void onSetIdentifier();

    void close();

    void setMasterConfig(MasterConfiguration conf);

    MasterConfiguration getMasterConfig();
}
