package com.sos.jobscheduler.master.event;

import javax.json.JsonArray;

public interface IJobSchedulerUnlimitedEventHandler {

    void init(EventHandlerMasterSettings settings);

    void run();

    void onEmptyEvent(Long eventId);

    void onNonEmptyEvent(Long eventId, JsonArray events);

    void onTornEvent(Long eventId, JsonArray events);

    void onRestart(Long eventId, JsonArray events);

    void setIdentifier(String identifier);

    void close();

    void awaitEnd();

    void setSettings(EventHandlerMasterSettings settings);

    EventHandlerMasterSettings getSettings();
}
