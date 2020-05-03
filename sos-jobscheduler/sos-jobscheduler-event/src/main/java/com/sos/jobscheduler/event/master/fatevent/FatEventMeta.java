package com.sos.jobscheduler.event.master.fatevent;

public final class FatEventMeta {

    public static enum FatEventType {
        MasterReadyFat, AgentReadyFat, OrderAddedFat, OrderCancelledFat, OrderFailedFat, OrderFinishedFat, OrderForkedFat, OrderJoinedFat, OrderProcessedFat, OrderProcessingStartedFat, OrderStdoutWrittenFat, OrderStderrWrittenFat
    };

}
