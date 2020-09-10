package com.sos.js7.order.initiator.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;

public class FilterOrderTemplates extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterOrderTemplates.class);
    private String controllerId;
    private String path;
    private String folder;
    private boolean recursive=false;
    private Boolean deployed;
    private Boolean deleted;
    
    public String getControllerId() {
        return controllerId;
    }
    
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    public boolean getRecursive() {
        return recursive;
    }
    
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }
    
    public Boolean getDeployed() {
        return deployed;
    }
    
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
 
    
   
 

}