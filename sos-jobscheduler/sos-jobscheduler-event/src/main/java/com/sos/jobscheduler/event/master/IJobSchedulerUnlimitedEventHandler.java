package com.sos.jobscheduler.event.master;

import javax.json.JsonArray;

import com.sos.jobscheduler.event.master.EventHandlerMasterSettings;

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
