package com.sos.joc.db.inventory.items;

import java.util.Date;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class InventoryReleasablesTreeFolderItem {

    private InventoryReleaseItem release;
    private DBItemInventoryConfiguration configuration;

    public InventoryReleasablesTreeFolderItem(DBItemInventoryConfiguration conf, Long releaseId, Date releaseDate, String releasePath,
            String controllerId) {
        if (conf != null) {
            conf.setContent(null);
        }
        configuration = conf;
        if (releaseId != null) {
            release = new InventoryReleaseItem(releaseId, releaseDate, releasePath, controllerId);
        }
    }

    public DBItemInventoryConfiguration getConfiguration() {
        return configuration;
    }

    public InventoryReleaseItem getRelease() {
        return release;
    }

}
