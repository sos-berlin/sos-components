package com.sos.joc.db.inventory.items;

import java.util.Date;

public class InventoryReleaseItem {

    private Long releaseId;
    private Date releaseDate;
    private String controllerId;
    private String path;
    private String content;
    
    public InventoryReleaseItem(Long id, Date modified, String path, String controllerId) {
        this.releaseId = id;
        this.releaseDate = modified;
        this.path = path;
        this.controllerId = controllerId;
    }
    
    public InventoryReleaseItem(Long id, Date modified, String path, String content, String controllerId) {
        this.releaseId = id;
        this.releaseDate = modified;
        this.path = path;
        this.content = content;
        this.controllerId = controllerId;
    }

    public Long getId() {
        return releaseId;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getPath() {
        return path;
    }

    public String getControllerId() {
        return controllerId;
    }
    
    public String getContent() {
        return content;
    }

}
