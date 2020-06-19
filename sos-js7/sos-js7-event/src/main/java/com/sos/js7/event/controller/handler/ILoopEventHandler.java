package com.sos.js7.event.controller.handler;

import com.sos.js7.event.controller.bean.Event;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public interface ILoopEventHandler {

    void init(ControllerConfiguration conf);

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

    void setControllerConfig(ControllerConfiguration conf);

    ControllerConfiguration getControllerConfig();
}
