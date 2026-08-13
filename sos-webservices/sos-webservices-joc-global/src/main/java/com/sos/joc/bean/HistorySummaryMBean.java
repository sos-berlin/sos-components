package com.sos.joc.bean;

public interface HistorySummaryMBean {

    public long getsuccessful_orders_of_last_24_hours();
    public long getfailed_orders_of_last_24_hours();
    public long getsuccessful_jobs_of_last_24_hours();
    public long getfailed_jobs_of_last_24_hours();
}
