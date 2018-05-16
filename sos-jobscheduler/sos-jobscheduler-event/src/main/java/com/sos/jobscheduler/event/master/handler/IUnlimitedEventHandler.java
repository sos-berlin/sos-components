package com.sos.jobscheduler.event.master.handler;

import com.sos.jobscheduler.event.master.bean.Event;

public interface IUnlimitedEventHandler {

    void init(EventHandlerMasterSettings settings);

    void run();

    void onEmptyEvent(Long eventId);

    Long onNonEmptyEvent(Long eventId, Event event);

    void onTornEvent(Long eventId, Event event);

    void onRestart(Long eventId, Event event);

    void setIdentifier(String identifier);

    void close();

    void awaitEnd();

    void setSettings(EventHandlerMasterSettings settings);

    EventHandlerMasterSettings getSettings();
}
