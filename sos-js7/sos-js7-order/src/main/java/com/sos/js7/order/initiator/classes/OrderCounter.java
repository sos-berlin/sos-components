package com.sos.js7.order.initiator.classes;

public class OrderCounter {

    private long single;
    private long cyclic;
    private long cyclicTotal;
    private long storedSingle;
    private long storedCyclicTotal;
    private long storeSkippedSingle;
    private long storeSkippedCyclicTotal;

    public OrderCounter() {
        single = 0;
        cyclic = 0;
        cyclicTotal = 0;
        storedSingle = 0;
        storedCyclicTotal = 0;
        storeSkippedSingle = 0;
        storeSkippedCyclicTotal = 0;
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

    public void addStoredSingle() {
        storedSingle += 1;
    }

    public void addStoredCyclicTotal() {
        storedCyclicTotal += 1;
    }

    public void addStoreSkippedSingle() {
        storeSkippedSingle += 1;
    }

    public void addStoreSkippedCyclicTotal() {
        storeSkippedCyclicTotal += 1;
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
        boolean storeDiffSingle = storedSingle > 0 && storedSingle != single;
        boolean storeDiffCyclic = storedCyclicTotal > 0 && storedCyclicTotal != cyclicTotal;
        if (storeDiffSingle || storeDiffCyclic || storeSkippedSingle > 0 || storeSkippedCyclicTotal > 0) {
            sb.append(" (store ");
            sb.append("stored single=").append(storedSingle).append(", cyclic total=" + storedCyclicTotal).append(" ");
            sb.append("skipped single=").append(storeSkippedSingle).append(", cyclic total=" + storeSkippedCyclicTotal).append(" ");
            sb.append(")");
        }
        return sb.toString();
    }
}
