package com.sos.joc.db.deploy.items;

public class DeployedContent {

    private String json;
    private String path;
    private String commitId;
    private Boolean isCurrentVersion;
    
    public DeployedContent(String path, String json, String commitId) {
        this.path = path;
        this.json = json;
        this.commitId = commitId;
        this.isCurrentVersion = null;
    }
    
    public DeployedContent(String path, String json, String commitId, Boolean isCurrentVersion) {
        this.path = path;
        this.json = json;
        this.commitId = commitId;
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
