package com.sos.js7.order.initiator.classes;

public class OrderCounter {

    protected Long countSingle = 0L;
    protected Long countCycled = 0L;
    protected Long countCycledAll = 0L;

    public Long getCountSingle() {
        return countSingle;
    }

    public Long getCountCycled() {
        return countCycled;
    }

    public Long getCountCycledAll() {
        return countCycledAll;
    }

    public Long getCount() {
        return countSingle + countCycled;
    }

    public String cycledOrdersDesc() {
        String s = "";
        String orders = "orders";
        String ordersCycledAll = " orders";
        if (countCycled > 0) {
            if (countCycled == 1){
                orders = "order";
            }
            if (countCycledAll == 1){
                ordersCycledAll = " order";
            }
            s = "(" + countCycled + " cycled " + orders + " with " + countCycledAll   + ordersCycledAll + ")";
        }
        return s;

    }

}
