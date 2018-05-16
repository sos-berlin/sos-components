package com.sos.jobscheduler.event.master.fatevent;

public final class EventMeta {

    public static enum EventType {
        OrderAddedFat, OrderProcessingStartedFat, OrderStdoutWrittenFat, OrderStderrWrittenFat, OrderProcessedFat, OrderFinishedFat
    };

}
