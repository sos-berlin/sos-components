package com.sos.joc.classes.filetransfer;

import java.util.List;
import java.util.Set;

import com.sos.joc.classes.filetransfer.updaters.fragments.protocol.AzureBlobStorageFragmentUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.protocol.FtpFragmentUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.protocol.HttpFragmentUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.protocol.SftpFragmentUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.protocol.SmbFragmentUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.protocol.WebdavFragmentUpdater;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class FileTransferUpdateConfigurationHelper {

    public static void updateAzureBlobStorageConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new AzureBlobStorageFragmentUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateFtpConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new FtpFragmentUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateHttpConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new HttpFragmentUpdater().update(dbLayer, config, newName, items, events);
    }
    
    public static void updateSftpConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new SftpFragmentUpdater().update(dbLayer, config, newName, items, events);
    }
    
    public static void updateSmbConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new SmbFragmentUpdater().update(dbLayer, config, newName, items, events);
    }
    
    public static void updateWebdavConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new WebdavFragmentUpdater().update(dbLayer, config, newName, items, events);
    }

}
