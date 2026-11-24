package com.sos.joc.classes.filetransfer.updaters.fragments.common;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.WebdavFragment;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public abstract class ACommonFragmentRefsUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ACommonFragmentRefsUpdater.class);

    protected abstract String getLogPrefix();

    protected abstract String getFragmentLogName();

    protected abstract String getRefName(AProtocolFragment f);

    protected abstract void setRefName(AProtocolFragment f, String newName);

    protected abstract List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException;

    public void update(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName, List<DBItemInventoryConfiguration> items,
            Set<String> events) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        List<DBItemInventoryConfiguration> referencedIn = getReferencedIn(dbLayer, config.getName());
        if (!SOSCollection.isEmpty(referencedIn)) {
            for (DBItemInventoryConfiguration c : referencedIn) {
                handle(dbLayer, config, c, newName, items, events, isDebugEnabled);
            }
        }

    }

    public static void applyChange(InventoryDBLayer dbLayer, DBItemInventoryConfiguration c, List<DBItemInventoryConfiguration> items,
            Set<String> events) throws Exception {
        int i = items.indexOf(c);
        if (i != -1) {
            items.get(i).setContent(c.getContent());
            items.get(i).setReleased(false);
        } else {
            JocInventory.updateConfiguration(dbLayer, c);
            events.add(c.getFolder());
        }
    }

    public static boolean refNameEquals(DBItemInventoryConfiguration config, String referencedInName) {
        return config.getName().equalsIgnoreCase(referencedInName);
    }

    private void handle(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, DBItemInventoryConfiguration referencedIn, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events, boolean isDebugEnabled) throws Exception {

        Object o = null;
        switch (referencedIn.getTypeAsEnum()) {
        case FT_FRAGMENT_SFTP:
            SftpFragment sftp = Globals.objectMapper.readValue(referencedIn.getContent(), SftpFragment.class);
            if (refNameEquals(config, getRefName(sftp))) {
                setRefName(sftp, newName);
                o = sftp;
            }
            break;
        case FT_FRAGMENT_JUMP:
            JumpFragment jump = Globals.objectMapper.readValue(referencedIn.getContent(), JumpFragment.class);
            if (refNameEquals(config, getRefName(jump))) {
                setRefName(jump, newName);
                o = jump;
            }
            break;

        case FT_FRAGMENT_FTP:
            FtpFragment ftp = Globals.objectMapper.readValue(referencedIn.getContent(), FtpFragment.class);
            if (refNameEquals(config, getRefName(ftp))) {
                setRefName(ftp, newName);
                o = ftp;
            }
            break;

        case FT_FRAGMENT_WEBDAV:
            WebdavFragment webdav = Globals.objectMapper.readValue(referencedIn.getContent(), WebdavFragment.class);
            if (refNameEquals(config, getRefName(webdav))) {
                setRefName(webdav, newName);
                o = webdav;
            }
            break;

        case FT_FRAGMENT_HTTP:
            HttpFragment http = Globals.objectMapper.readValue(referencedIn.getContent(), HttpFragment.class);
            if (refNameEquals(config, getRefName(http))) {
                setRefName(http, newName);
                o = http;
            }
            break;

        case FT_FRAGMENT_AZURE_BLOB_STORAGE:
            AzureBlobStorageFragment azure = Globals.objectMapper.readValue(referencedIn.getContent(), AzureBlobStorageFragment.class);
            if (refNameEquals(config, getRefName(azure))) {
                setRefName(azure, newName);
                o = azure;
            }
            break;

        default:
            return;
        }

        if (o == null) {
            return;
        }

        referencedIn.setContent(Globals.objectMapper.writeValueAsString(o));
        referencedIn.setReleased(false);
        applyChange(dbLayer, referencedIn, items, events);
    }
}
