package com.sos.joc.db.deploy;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.model.common.Folder;

public class DeployedConfigurationFilter {
    
    private String controllerId = "";
    private Set<Integer> objectTypes;
    private Set<Folder> folders;
    private Set<String> paths;
    private Set<WorkflowId> workflowIds;

    
    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
    
    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public Set<Folder> getFolders() {
        return folders;
    }
    
    public void setFolders(Collection<Folder> folders) {
        if (folders != null && !folders.isEmpty()) {
            this.folders = folders.stream().collect(Collectors.toSet());
        }
    }
    
    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public Set<String> getPaths() {
        return paths;
    }
    
    public void setPaths(Collection<String> paths) {
        if (paths != null && !paths.isEmpty()) {
            this.paths = paths.stream().collect(Collectors.toSet());
        }
    }
    
    public void setObjectTypes(Set<Integer> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public Set<Integer> getObjectTypes() {
        return objectTypes;
    }
    
    public void setObjectTypes(Collection<Integer> objectTypes) {
        if (objectTypes != null && !objectTypes.isEmpty()) {
            this.objectTypes = objectTypes.stream().collect(Collectors.toSet());
        }
    }
    
    public void setWorkflowIds(Set<WorkflowId> workflowIds) {
        this.workflowIds = workflowIds;
    }

    public Set<WorkflowId> getWorkflowIds() {
        return workflowIds;
    }
    
    public void setWorkflowIds(Collection<WorkflowId> workflowIds) {
        if (workflowIds != null && !workflowIds.isEmpty()) {
            this.workflowIds = workflowIds.stream().collect(Collectors.toSet());
        }
    }
}
