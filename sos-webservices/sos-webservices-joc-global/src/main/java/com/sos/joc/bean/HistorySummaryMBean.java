package com.sos.joc.bean;

public interface HistorySummaryMBean {

    public long getSuccessfulOrdersOfLast24Hours();
    public long getFailedOrdersOfLast24Hours();
    public long getSuccessfulJobsOfLast24Hours();
    public long getFailedJobsOfLast24Hours();
}
