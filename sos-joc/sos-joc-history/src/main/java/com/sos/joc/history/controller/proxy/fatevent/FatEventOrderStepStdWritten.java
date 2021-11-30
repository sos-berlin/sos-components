package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventOrderStepStdWritten extends AFatEvent {

    public enum StdType {
        STDOUT, STDERROR
    };

    private String orderId;
    private String chunck;
    private HistoryEventType type = HistoryEventType.OrderStepStdoutWritten;

    public FatEventOrderStepStdWritten(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length == 3) {
            StdType std = (StdType) objects[0];
            this.orderId = (String) objects[1];
            this.chunck = (String) objects[2];
            this.type = std.equals(StdType.STDOUT) ? HistoryEventType.OrderStepStdoutWritten : HistoryEventType.OrderStepStderrWritten;
        }
    }

    @Override
    public HistoryEventType getType() {
        return type;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getChunck() {
        return chunck;
    }

}
