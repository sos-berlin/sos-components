package com.sos.joc.db.inventory.items;

import java.util.Date;

public class InventoryReleaseItem {

    private Long releaseId;
    private Date releaseDate;
    private String path;
    private String content;
    
    public InventoryReleaseItem(Long id, Date modified, String path) {
        this.releaseId = id;
        this.releaseDate = modified;
        this.path = path;
    }
    
    public InventoryReleaseItem(Long id, Date modified, String path, String content) {
        this.releaseId = id;
        this.releaseDate = modified;
        this.path = path;
        this.content = content;
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

    public String getContent() {
        return content;
    }

}
