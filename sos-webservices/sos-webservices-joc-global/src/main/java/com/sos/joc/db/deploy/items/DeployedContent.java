package com.sos.joc.db.deploy.items;

public class DeployedContent {

    private String json;
    private String path;
    
    public DeployedContent(String path, String json) {
        this.path = path;
        this.json = json;
    }

    public String getContent() {
        return json;
    }
    
    public String getPath() {
        return path;
    }
}
