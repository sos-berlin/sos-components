package com.sos.jobscheduler.event.master.fatevent;

public final class EventMeta {

    public static enum EventType {
        MasterReadyFat, AgentReadyFat, OrderAddedFat, OrderCancelledFat, OrderFailedFat, OrderFinishedFat, OrderForkedFat, OrderJoinedFat, OrderProcessedFat, OrderProcessingStartedFat, OrderStdoutWrittenFat, OrderStderrWrittenFat
    };

}
