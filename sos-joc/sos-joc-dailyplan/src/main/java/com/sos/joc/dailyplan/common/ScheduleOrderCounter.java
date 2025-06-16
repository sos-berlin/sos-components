package com.sos.joc.dailyplan.common;

import java.util.Set;

public class ScheduleOrderCounter {

    private Set<String> orderNames;
    private final Integer total; // schedule*workflows

    public ScheduleOrderCounter(Set<String> orderNames, int total) {
        this.orderNames = orderNames;
        this.total = total;
    }

    public ScheduleOrderCounter(Set<String> orderNames, Long total) {
        this(orderNames, total.intValue());
    }

    public Long getTotalAsLong() {
        return Long.valueOf(total);
    }

    /** @return scheduleOrders only if it is greater than default(1) - so, if multiple schedule orders are defined, otherwise null */
    public Set<String> getOrderNamesNormalized() {
        if (orderNames == null || orderNames.size() < 2) {
            return null;
        }
        return orderNames;
    }

}
