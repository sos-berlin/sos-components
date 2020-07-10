package com.sos.js7.event.controller.fatevent;

public final class FatEventMeta {

    public static enum FatEventType {
        ControllerReadyFat, AgentReadyFat, OrderAddedFat, OrderCancelledFat, OrderFailedFat, OrderFinishedFat, OrderForkedFat, OrderJoinedFat, OrderProcessedFat, OrderProcessingStartedFat, OrderStdoutWrittenFat, OrderStderrWrittenFat
    };

}
