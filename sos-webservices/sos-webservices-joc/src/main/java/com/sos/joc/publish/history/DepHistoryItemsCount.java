package com.sos.joc.publish.history;


public class DepHistoryItemsCount {

    private long countWorkflows;
    private long countLocks;
    private long countFileOrderSources;
    private long countJobResources;
    private long countBoards;
    
    public long getCountWorkflows() {
        return countWorkflows;
    }
    public void setCountWorkflows(long countWorkflows) {
        this.countWorkflows = countWorkflows;
    }
    
    public long getCountLocks() {
        return countLocks;
    }
    public void setCountLocks(long countLocks) {
        this.countLocks = countLocks;
    }
    
    public long getCountFileOrderSources() {
        return countFileOrderSources;
    }
    public void setCountFileOrderSources(long countFileOrderSources) {
        this.countFileOrderSources = countFileOrderSources;
    }
    
    public long getCountJobResources() {
        return countJobResources;
    }
    public void setCountJobResources(long countJobResources) {
        this.countJobResources = countJobResources;
    }
    
    public long getCountBoards() {
        return countBoards;
    }
    public void setCountBoards(long countBoards) {
        this.countBoards = countBoards;
    }
    
}
