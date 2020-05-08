package com.sos.jobscheduler.event.master.handler;

import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.configuration.master.IMasterConfiguration;

public interface ILoopEventHandler {

    void init(IMasterConfiguration masterConfiguration);

    void run();

    void onProcessingStart(Long eventId);

    void onProcessingEnd(Long eventId);

    boolean onProcessingException();

    Long onEmptyEvent(Long eventId, Event event);

    Long onNonEmptyEvent(Long eventId, Event event);

    Long onTornEvent(Long eventId, Event event);

    void onRestart(Long eventId, Event event);

    void setIdentifier(String identifier);

    String getIdentifier();

    void close();

    void setMasterConfig(IMasterConfiguration conf);

    IMasterConfiguration getMasterConfig();
}
