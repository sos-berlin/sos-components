package com.sos.jobscheduler.event.master.handler.notifier;

public interface INotifier {

    public void notifyOnError(String title, String msg, Throwable t);

    public void notifyOnError(String msg, Throwable t);
    
    public void notifyOnRecovery(String title, String msg);

    public void notifyOnWarning(String title, String msg, Throwable t);

    public void notifyOnWarning(String msg, Throwable t);
}
