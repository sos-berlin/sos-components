package com.sos.joc.classes.audit;

public interface IAuditLog {
    
    public String getComment();
    
    public String getFolder();
    
    public String getJob();
    
    public String getWorkflow();
    
    public String getOrderId();
    
    public String getControllerId();
    
    public Integer getTimeSpent();
    
    public String getTicketLink();
    
    public String getCalendar();
    
    public Long getDepHistoryId();
}
