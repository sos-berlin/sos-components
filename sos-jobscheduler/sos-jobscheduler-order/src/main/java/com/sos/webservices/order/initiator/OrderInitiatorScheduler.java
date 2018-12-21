package com.sos.webservices.order.initiator;

import java.util.TimerTask;
import com.sos.webservices.order.initiator.classes.PlannedOrder;

public class OrderInitiatorScheduler extends TimerTask {

    PlannedOrder plannedOrder;

    public OrderInitiatorScheduler(PlannedOrder plannedOrder) {
        this.plannedOrder = plannedOrder;
    }

    public OrderInitiatorScheduler() {
    }

    @Override
    public void run() {

        System.out.println(plannedOrder.uniqueOrderkey());

    }

}
