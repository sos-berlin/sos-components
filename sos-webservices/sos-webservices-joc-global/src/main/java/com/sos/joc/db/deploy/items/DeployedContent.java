package com.sos.joc.db.deploy.items;

import java.util.Date;

public class DeployedContent {

    private String json;
    private String path;
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
    
    public DeployedContent(String path, String json, String commitId, Date created, Boolean isCurrentVersion) {
        this.path = path;
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
    
    public String getPath() {
        return path;
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
}
