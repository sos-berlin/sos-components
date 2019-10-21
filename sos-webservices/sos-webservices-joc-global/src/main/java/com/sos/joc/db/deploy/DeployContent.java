package com.sos.joc.db.deploy;


public class DeployContent {
    private String path;
    private byte[] content;
    private String type;
    
    public DeployContent(String path, byte[] content, String type) {
        this.path = path;
        this.content = content;
        this.type = type;
    }

    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
