package com.sos.jobscheduler.event.master.fatevent;

public final class EventMeta {

    public static enum EventType {
        MasterReadyFat, AgentReadyFat, OrderForkedFat, OrderJoinedFat, OrderAddedFat, OrderProcessingStartedFat, OrderStdoutWrittenFat, OrderStderrWrittenFat, OrderProcessedFat, OrderFinishedFat
    };

}
