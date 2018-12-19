package com.sos.jobscheduler.event.master.handler.notifier;

public interface INotifier {

    public void notifyOnError(String msg, Throwable t);

    public void notifyOnError(Throwable t);

    public void notifyOnSuccess(String title, String msg);

    public void notifyOnWarning(String msg, Throwable t);
}
