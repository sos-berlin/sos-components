package com.sos.jobscheduler.event.master.fatevent;

public final class EventMeta {

    public static enum EventType {
        OrderProcessingStartedFat, OrderStdoutWrittenFat, OrderStderrWrittenFat, OrderProcessedFat, OrderFinishedFat
    };

}
