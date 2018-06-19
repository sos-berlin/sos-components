package com.sos.jobscheduler.event.master.handler;

public interface ISender {

    public void sendOnError(String bodyPart, Throwable t);

    public void sendOnError(Throwable t);
}
