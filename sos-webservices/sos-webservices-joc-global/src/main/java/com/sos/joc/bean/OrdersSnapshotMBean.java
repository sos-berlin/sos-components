package com.sos.joc.bean;

public interface OrdersSnapshotMBean {

    public int getblocked();
    public int getpending();
    public int getscheduled_for_next_24_hours();
    public int getin_progess();
    public int getrunning();
    public int getfailed();
    public int getsuspended();
    public int getwaiting();
    public int getterminated();
    public int getprompting();
}
