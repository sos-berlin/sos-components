package com.sos.joc.publish.mapper;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class DbItemConfWithOriginalContent {

    private DBItemInventoryConfiguration invCfg;
    private String originalContent;
    
    
    public DbItemConfWithOriginalContent (DBItemInventoryConfiguration invCfg, String originalContent) {
        this.invCfg = invCfg;
        this.originalContent = originalContent;
    }
    
    public DBItemInventoryConfiguration getInvCfg() {
        return invCfg;
    }
    
    public String getOriginalContent() {
        return originalContent;
    }

}
