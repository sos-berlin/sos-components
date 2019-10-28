package com.sos.joc.classes.audit;

import java.util.List;

import com.sos.joc.model.deploy.JSObject;

public interface IAuditLog {
    
    public String getComment();
    
    public String getFolder();
    
    public String getJob();
    
    public String getWorkflow();
    
    public String getOrderId();
    
    public String getJobschedulerId();
    
    public Integer getTimeSpent();
    
    public String getTicketLink();
    
    public String getCalendar();
    
    public List<JSObject> getJSObjects();
}
