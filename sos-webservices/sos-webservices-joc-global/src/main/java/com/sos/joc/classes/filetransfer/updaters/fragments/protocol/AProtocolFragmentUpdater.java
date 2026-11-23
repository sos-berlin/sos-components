package com.sos.joc.classes.filetransfer.updaters.fragments.protocol;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.WebdavFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferJobResourceHandler;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public abstract class AProtocolFragmentUpdater<F, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AProtocolFragmentUpdater.class);

    protected abstract String getLogPrefix();

    protected abstract String getFragmentLogName();

    protected abstract F readFragment(DBItemInventoryConfiguration c) throws Exception;

    protected abstract String getAlternativeName(F f);

    protected abstract void setAlternativeName(F f, String newName);

    protected abstract List<DBItemInventoryConfiguration> getAlternativeFragments(InventoryDBLayer dbLayer, String name) throws SOSHibernateException;

    protected abstract List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException;

    protected abstract boolean isSourceFragment(Source source);

    protected abstract boolean isTargetFragment(Target target);

    protected abstract R getSourceFragment(Source source);

    protected abstract R getTargetFragment(Target target);

    protected abstract String getFragmentName(R r);

    protected abstract void setFragmentName(R r, String newName);

    public void update(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName, List<DBItemInventoryConfiguration> items,
            Set<String> events) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        // 1) other protocol fragments with the referenced alternative name
        List<DBItemInventoryConfiguration> alternatives = getAlternativeFragments(dbLayer, config.getName());
        if (!SOSCollection.isEmpty(alternatives)) {
            for (DBItemInventoryConfiguration c : alternatives) {
                F f = readFragment(c);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s=%s, alternativeName=%s]new name=%s", getLogPrefix(), config.getTypeAsEnum(),
                            getFragmentLogName(), c.getPath(), getAlternativeName(f), newName));
                }

                if (SOSString.isEmpty(getAlternativeName(f)) || newName.equalsIgnoreCase(getAlternativeName(f))) {
                    continue;
                }

                setAlternativeName(f, newName);
                c.setContent(Globals.objectMapper.writeValueAsString(f));
                c.setReleased(false);
                applyChange(dbLayer, c, items, events);
            }
        }

        // 2) profiles
        List<DBItemInventoryConfiguration> referencedIn = getReferencedIn(dbLayer, config.getName());
        if (!SOSCollection.isEmpty(referencedIn)) {
            for (DBItemInventoryConfiguration c : referencedIn) {
                if (isJump()) {
                    handleJump(dbLayer, config, c, newName, items, events, isDebugEnabled);
                } else {
                    handleProfile(dbLayer, config, c, newName, items, events, isDebugEnabled);
                }
            }
        }
    }

    /** see {@link JumpFragmentUpdater#isJump()}
     * 
     * @return */
    public boolean isJump() {
        return false;
    }

    private void handleJump(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, DBItemInventoryConfiguration referencedIn, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events, boolean isDebugEnabled) throws Exception {

        Object o = null;
        switch (referencedIn.getTypeAsEnum()) {
        case FT_FRAGMENT_SFTP:
            SftpFragment sftp = Globals.objectMapper.readValue(referencedIn.getContent(), SftpFragment.class);
            if (config.getName().equalsIgnoreCase(sftp.getJumpFragmentName())) {
                sftp.setJumpFragmentName(newName);
                o = sftp;
            }
            break;

        case FT_FRAGMENT_FTP:
            FtpFragment ftp = Globals.objectMapper.readValue(referencedIn.getContent(), FtpFragment.class);
            if (refNameEquals(config, ftp.getJumpFragmentName())) {
                ftp.setJumpFragmentName(newName);
                o = ftp;
            }
            break;

        case FT_FRAGMENT_WEBDAV:
            WebdavFragment webdav = Globals.objectMapper.readValue(referencedIn.getContent(), WebdavFragment.class);
            if (refNameEquals(config, webdav.getJumpFragmentName())) {
                webdav.setJumpFragmentName(newName);
                o = webdav;
            }
            break;

        case FT_FRAGMENT_HTTP:
            HttpFragment http = Globals.objectMapper.readValue(referencedIn.getContent(), HttpFragment.class);
            if (refNameEquals(config, http.getJumpFragmentName())) {
                http.setJumpFragmentName(newName);
                o = http;
            }
            break;

        case FT_FRAGMENT_AZURE_BLOB_STORAGE:
            AzureBlobStorageFragment azure = Globals.objectMapper.readValue(referencedIn.getContent(), AzureBlobStorageFragment.class);
            if (refNameEquals(config, azure.getJumpFragmentName())) {
                azure.setJumpFragmentName(newName);
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

    private void handleProfile(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, DBItemInventoryConfiguration referencedIn,
            String newName, List<DBItemInventoryConfiguration> items, Set<String> events, boolean isDebugEnabled) throws Exception {
        FileTransferJobResource jr = FileTransferJobResourceHandler.toFileTransferJobResource(referencedIn.getContent());
        if (jr.getProfile() == null) {
            return;
        }
        boolean changed = false;
        Source source = jr.getProfile().getSource();
        Target target = jr.getProfile().getTarget();

        if (source != null && isSourceFragment(source)) {
            R srcFragment = getSourceFragment(source);
            if (config.getName().equalsIgnoreCase(getFragmentName(srcFragment))) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][profile=%s][source.fragment.name=%s]new name=%s", getLogPrefix(), config.getTypeAsEnum(),
                            referencedIn.getPath(), getFragmentName(srcFragment), newName));
                }
                setFragmentName(srcFragment, newName);
                changed = true;
            }
        }

        if (target != null && isTargetFragment(target)) {
            R tgtFragment = getTargetFragment(target);
            if (refNameEquals(config, getFragmentName(tgtFragment))) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][profile=%s][target.fragment.name=%s]new name=%s", getLogPrefix(), config.getTypeAsEnum(),
                            referencedIn.getPath(), getFragmentName(tgtFragment), newName));
                }
                setFragmentName(tgtFragment, newName);
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        referencedIn.setContent(Globals.objectMapper.writeValueAsString(jr));
        referencedIn.setDeployed(false);
        applyChange(dbLayer, referencedIn, items, events);
    }

    private void applyChange(InventoryDBLayer dbLayer, DBItemInventoryConfiguration c, List<DBItemInventoryConfiguration> items, Set<String> events)
            throws Exception {
        int i = items.indexOf(c);
        if (i != -1) {
            items.get(i).setContent(c.getContent());
            items.get(i).setReleased(false);
        } else {
            JocInventory.updateConfiguration(dbLayer, c);
            events.add(c.getFolder());
        }
    }

    private boolean refNameEquals(DBItemInventoryConfiguration config, String referencedInName) {
        return config.getName().equalsIgnoreCase(referencedInName);
    }
}
