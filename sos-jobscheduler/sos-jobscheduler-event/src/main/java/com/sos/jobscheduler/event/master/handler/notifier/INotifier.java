package com.sos.jobscheduler.event.master.handler.notifier;

public interface INotifier {

    public void notifyOnError(String bodyPart, Throwable t);

    public void notifyOnError(Throwable t);
}
