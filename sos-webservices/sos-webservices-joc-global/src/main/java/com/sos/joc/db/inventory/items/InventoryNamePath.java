package com.sos.joc.db.inventory.items;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sos.controller.model.workflow.WorkflowId;

public class InventoryNamePath {
    
    private String name;
    private String path;
    private WorkflowId workflowId;

    public InventoryNamePath(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    public InventoryNamePath(String name, String commitId, String path) {
        this.name = name + "/" + commitId ;
        this.path = path;
        this.workflowId = new WorkflowId(name, commitId);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
    
    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("path", path).toString();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InventoryNamePath) == false) {
            return false;
        }
        InventoryNamePath rhs = ((InventoryNamePath) other);
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

}
