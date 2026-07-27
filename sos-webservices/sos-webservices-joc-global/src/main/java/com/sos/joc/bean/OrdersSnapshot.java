package com.sos.joc.bean;


public class OrdersSnapshot implements OrdersSnapshotMBean, IJocMBean {

    private static OrdersSnapshot instance;
    public int numOfSuspendedOrders = 4711;
    
    private OrdersSnapshot() {
    }

    public static OrdersSnapshot getInstance() {
        if (instance == null) {
            instance = new OrdersSnapshot();
        }
        return instance;
    }
    
    @Override
    public int getSuspendedOrders() {
        return numOfSuspendedOrders;
    }

    @Override
    public String objectName() {
        return "myTestOrdersSnapshotBean";
    }

}
