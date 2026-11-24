package com.sos.joc.classes.filetransfer;

import java.util.List;
import java.util.Set;

import com.sos.joc.classes.filetransfer.updaters.fragments.AzureBlobStorageFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.FtpFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.HttpFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.JumpFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.MailFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.ProfileRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.SftpFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.SmbFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.WebdavFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.common.CredentialStoreFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.common.DecryptionFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.common.HttpProxyFragmentRefsUpdater;
import com.sos.joc.classes.filetransfer.updaters.fragments.common.SocksProxyFragmentRefsUpdater;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class FileTransferUpdateConfigurationHelper {

    public static void updateCredentialStoreRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new CredentialStoreFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateDecryptionRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new DecryptionFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateMailRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new MailFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateHttpProxyRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new HttpProxyFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateSocksProxyRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new SocksProxyFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateProfileRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new ProfileRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateAzureBlobStorageRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new AzureBlobStorageFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateFtpRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new FtpFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateHttpRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new HttpFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateSftpRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new SftpFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateJumpRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new JumpFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateSmbRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new SmbFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

    public static void updateWebdavRefs(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events) throws Exception {
        new WebdavFragmentRefsUpdater().update(dbLayer, config, newName, items, events);
    }

}
