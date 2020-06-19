package com.sos.js7.order.initiator;

import java.util.TimerTask;
import com.sos.js7.order.initiator.classes.PlannedOrder;

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
