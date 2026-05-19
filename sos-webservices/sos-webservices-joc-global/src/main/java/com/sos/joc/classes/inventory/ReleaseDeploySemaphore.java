package com.sos.joc.classes.inventory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ReleaseDeploySemaphore extends Semaphore {

    private static final long serialVersionUID = 1L;
    private Set<String> workflowNames;
    private String initialCaller;

    public ReleaseDeploySemaphore(int permits, String initialCaller) {
        super(permits);
        if(this.initialCaller == null) {
            this.initialCaller = initialCaller;
        }
    }
    
    public Set<String> getWorkflowNames() {
        if(workflowNames == null) {
            return Collections.emptySet();
        }
        return workflowNames;
    }
    
    public void setWorkflowNames(Set<String> workflowNames) {
        this.workflowNames = workflowNames;
    }
    
    public String getInitialCaller() {
        return initialCaller;
    }
    
}
