package com.sos.joc.bean;

public interface OrdersSnapshotMBean {

    public int getBlockedOrders();
    public int getPendingOrders();
    public int getScheduledOrdersForNext24Hours();
    public int getInProgessOrders();
    public int getRunningOrders();
    public int getFailedOrders();
    public int getSuspendedOrders();
    public int getWaitingOrders();
    public int getTerminatedOrders();
    public int getPromptingOrders();
}
