package com.sos.jobscheduler.event.master;

public final class JobSchedulerEvent {

    public static final String MASTER_API_PATH = "/jobscheduler/master/api/";

    public static enum EventType {
        FileBasedEvent, FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, TaskEvent, TaskStarted, TaskEnded, TaskClosed, OrderEvent, OrderStarted, OrderFinished, OrderStepStarted, OrderStepEnded, OrderSetBack, OrderNodeChanged, OrderSuspended, OrderResumed, OrderWaitingInTask, JobChainEvent, JobChainStateChanged, JobChainNodeActionChanged, SchedulerClosed, SchedulerEvent, VariablesCustomEvent
    };

    public static enum EventSeq {
        NonEmpty, Empty, Torn
    };

    public static enum EventPath {
        event, fileBased, task, order, jobChain
    };

    public static enum EventKey {
        TYPE, key, eventId, eventSnapshots, jobPath
    };

    public static enum EventOverview {
        FileBasedOverview, FileBasedDetailed, TaskOverview, OrderOverview, JobChainOverview
    };
}
