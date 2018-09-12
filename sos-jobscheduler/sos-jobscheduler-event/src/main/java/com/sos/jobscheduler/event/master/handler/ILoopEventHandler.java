package com.sos.jobscheduler.event.master.handler;

import com.sos.jobscheduler.event.master.bean.Event;

public interface ILoopEventHandler {

    void init(EventHandlerMasterSettings settings);

    void run();

    Long onEmptyEvent(Long eventId, Event event);

    Long onNonEmptyEvent(Long eventId, Event event);

    void onTornEvent(Long eventId, Event event);

    void onRestart(Long eventId, Event event);

    void setIdentifier(String identifier);

    void close();

    void awaitEnd();

    void setSettings(EventHandlerMasterSettings settings);

    EventHandlerMasterSettings getSettings();
}
