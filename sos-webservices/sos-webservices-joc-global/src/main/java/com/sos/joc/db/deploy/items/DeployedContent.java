package com.sos.joc.db.deploy.items;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.joc.Globals;

public class DeployedContent {

    private String json;
    private String path;
    private String name;
    private String title;
    private String commitId;
    private Boolean isCurrentVersion;
    private Date created;
    
    public DeployedContent(String path, String json, String commitId) {
        this.path = path;
        this.json = json;
        this.commitId = commitId;
        this.created = null;
        this.isCurrentVersion = null;
    }
    
    public DeployedContent(String path, String name, String title, String json, String commitId) {
        this.path = path;
        this.title = title;
        this.name = name;
        this.json = json;
        this.commitId = commitId;
        this.created = null;
        this.isCurrentVersion = null;
    }
    
    public DeployedContent(String path, String json, String commitId, Date created, Boolean isCurrentVersion) {
        this.path = path;
        this.json = json;
        this.commitId = commitId;
        this.created = created;
        this.isCurrentVersion = isCurrentVersion;
    }
    
    public DeployedContent(String path, String name, String title, String json, String commitId, Date created, Boolean isCurrentVersion) {
        this.path = path;
        this.title = title;
        this.name = name;
        this.json = json;
        this.commitId = commitId;
        this.created = created;
        this.isCurrentVersion = isCurrentVersion;
    }
    
    public void setContent(String json) {
        this.json = json;
    }

    public String getContent() {
        return json;
    }
    
    public WorkflowBoards mapToWorkflowBoards() {
        try {
            WorkflowBoards wb = Globals.objectMapper.readValue(json, WorkflowBoards.class);
            wb.setPath(path);
            wb.setVersionId(commitId);
            return wb;
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    
    public String getTitle() {
        if (title == null) {
           return ""; 
        }
        return title;
    }
    
    public Date getCreated() {
        return created;
    }
    
    public String getCommitId() {
        return commitId;
    }
    
    public void setIsCurrentVersion(Boolean isCurrentVersion) {
       this.isCurrentVersion = isCurrentVersion;
    }
    
    public Boolean isCurrentVersion() {
        return isCurrentVersion;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(commitId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployedContent) == false) {
            return false;
        }
        DeployedContent rhs = ((DeployedContent) other);
        return new EqualsBuilder().append(path, rhs.path).append(commitId, rhs.commitId).isEquals();
    }
}
