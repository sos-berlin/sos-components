package com.sos.js7.order.initiator.classes;

public class OrderCounter {

    private long single;
    private long cyclic;
    private long cyclicTotal;
    private long submitted;

    public OrderCounter() {
        single = 0;
        cyclic = 0;
        cyclicTotal = 0;
        submitted = 0;
    }

    public void addSingle() {
        single += 1;
    }

    public void addCyclic() {
        cyclic += 1;
    }

    public void addCyclicTotal() {
        cyclicTotal += 1;
    }

    public long getCount() {
        return single + cyclic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCount()).append(" orders");
        if (cyclic > 0) {
            sb.append(" (");
            if (single > 0) {
                sb.append("single=").append(single).append(", ");
            }
            sb.append("cyclic=").append(cyclic).append(", cyclic total=").append(cyclicTotal);
            sb.append(")");
        }
        if (submitted > 0) {
            sb.append(" submitted=").append(submitted);
        }
        return sb.toString();
    }
}
