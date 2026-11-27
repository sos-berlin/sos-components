package com.sos.joc.classes.filetransfer.updaters.fragments;

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
import com.sos.inventory.model.filetransfer.profile.NotificationTriggers;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.classes.filetransfer.updaters.fragments.common.ACommonFragmentRefsUpdater;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public abstract class AProtocolOrProfileFragmentRefsUpdater<F, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AProtocolOrProfileFragmentRefsUpdater.class);

    protected abstract String getLogPrefix();

    protected abstract String getLogObjectName();

    protected abstract F readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception;

    protected abstract String getAlternativeName(F f);

    protected abstract void setAlternativeName(F f, String newName);

    protected abstract List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException;

    protected abstract List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException;

    protected abstract boolean hasSourceFragmentRef(Source source);

    protected abstract boolean hasTargetFragmentRef(Target target);

    protected abstract R getSourceFragmentRef(Source source);

    protected abstract R getTargetFragmentRef(Target target);

    protected abstract String getRefName(R r);

    protected abstract void setRefName(R r, String newName);

    public void update(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName, List<DBItemInventoryConfiguration> items,
            Set<String> events) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        // 1) other protocol fragments with the referenced alternative name
        List<DBItemInventoryConfiguration> alternatives = getAlternatives(dbLayer, config.getName());
        if (!SOSCollection.isEmpty(alternatives)) {
            for (DBItemInventoryConfiguration c : alternatives) {
                F f = readAlternativeFragment(c);
                if (isDebugEnabled) {
                    LOGGER
                        .debug(
                            String
                                .format(
                                    "[%s][%s][%s=%s, alternativeName=%s]new name=%s",
                                    getLogPrefix(),
                                    config.getTypeAsEnum(),
                                    getLogObjectName(),
                                    c.getPath(),
                                    getAlternativeName(f),
                                    newName));
                }

                if (SOSString.isEmpty(getAlternativeName(f)) || newName.equalsIgnoreCase(getAlternativeName(f))) {
                    continue;
                }

                setAlternativeName(f, newName);
                c.setContent(Globals.objectMapper.writeValueAsString(f));
                c.setReleased(false);
                ACommonFragmentRefsUpdater.applyChange(dbLayer, c, items, events);
            }
        }

        // 2) protocol fragments using jump or profiles
        List<DBItemInventoryConfiguration> referencedIn = getReferencedIn(dbLayer, config.getName());
        if (!SOSCollection.isEmpty(referencedIn)) {
            for (DBItemInventoryConfiguration c : referencedIn) {
                if (isJump()) {
                    handleJump(dbLayer, config, c, newName, items, events, isDebugEnabled);
                } else if (isMail()) {
                    handleMail(dbLayer, config, c, newName, items, events, isDebugEnabled);
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

    /** see {@link MailFragmentUpdater#isMail()}
     * 
     * @return */
    public boolean isMail() {
        return false;
    }

    private void handleJump(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, DBItemInventoryConfiguration referencedIn, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events, boolean isDebugEnabled) throws Exception {

        Object o = null;
        switch (referencedIn.getTypeAsEnum()) {
        case FT_FRAGMENT_SFTP:
            SftpFragment sftp = Globals.objectMapper.readValue(referencedIn.getContent(), SftpFragment.class);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, sftp.getJumpFragmentName())) {
                sftp.setJumpFragmentName(newName);
                o = sftp;
            }
            break;

        case FT_FRAGMENT_FTP:
            FtpFragment ftp = Globals.objectMapper.readValue(referencedIn.getContent(), FtpFragment.class);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, ftp.getJumpFragmentName())) {
                ftp.setJumpFragmentName(newName);
                o = ftp;
            }
            break;

        case FT_FRAGMENT_WEBDAV:
            WebdavFragment webdav = Globals.objectMapper.readValue(referencedIn.getContent(), WebdavFragment.class);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, webdav.getJumpFragmentName())) {
                webdav.setJumpFragmentName(newName);
                o = webdav;
            }
            break;

        case FT_FRAGMENT_HTTP:
            HttpFragment http = Globals.objectMapper.readValue(referencedIn.getContent(), HttpFragment.class);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, http.getJumpFragmentName())) {
                http.setJumpFragmentName(newName);
                o = http;
            }
            break;

        case FT_FRAGMENT_AZURE_BLOB_STORAGE:
            AzureBlobStorageFragment azure = Globals.objectMapper.readValue(referencedIn.getContent(), AzureBlobStorageFragment.class);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, azure.getJumpFragmentName())) {
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
        ACommonFragmentRefsUpdater.applyChange(dbLayer, referencedIn, items, events);
    }

    private void handleProfile(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, DBItemInventoryConfiguration referencedIn,
            String newName, List<DBItemInventoryConfiguration> items, Set<String> events, boolean isDebugEnabled) throws Exception {
        FileTransferJobResource jr = FileTransferHelper.toFileTransferJobResource(referencedIn.getContent());
        if (jr.getProfile() == null) {
            return;
        }
        boolean changed = false;
        Source source = jr.getProfile().getSource();
        Target target = jr.getProfile().getTarget();

        if (source != null && hasSourceFragmentRef(source)) {
            R srcFragment = getSourceFragmentRef(source);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, getRefName(srcFragment))) {
                if (isDebugEnabled) {
                    LOGGER
                        .debug(
                            String
                                .format(
                                    "[%s][%s][profile=%s][source.fragment.name=%s]new name=%s",
                                    getLogPrefix(),
                                    config.getTypeAsEnum(),
                                    referencedIn.getPath(),
                                    getRefName(srcFragment),
                                    newName));
                }
                setRefName(srcFragment, newName);
                changed = true;
            }
        }

        if (target != null && hasTargetFragmentRef(target)) {
            R tgtFragment = getTargetFragmentRef(target);
            if (ACommonFragmentRefsUpdater.refNameEquals(config, getRefName(tgtFragment))) {
                if (isDebugEnabled) {
                    LOGGER
                        .debug(
                            String
                                .format(
                                    "[%s][%s][profile=%s][target.fragment.name=%s]new name=%s",
                                    getLogPrefix(),
                                    config.getTypeAsEnum(),
                                    referencedIn.getPath(),
                                    getRefName(tgtFragment),
                                    newName));
                }
                setRefName(tgtFragment, newName);
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        referencedIn.setContent(Globals.objectMapper.writeValueAsString(jr));
        referencedIn.setDeployed(false);
        ACommonFragmentRefsUpdater.applyChange(dbLayer, referencedIn, items, events);
    }

    private void handleMail(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, DBItemInventoryConfiguration referencedIn, String newName,
            List<DBItemInventoryConfiguration> items, Set<String> events, boolean isDebugEnabled) throws Exception {
        FileTransferJobResource jr = FileTransferHelper.toFileTransferJobResource(referencedIn.getContent());
        if (jr.getProfile() == null) {
            return;
        }
        boolean changed = false;
        NotificationTriggers triggers = jr.getProfile().getNotificationTriggers();
        if (triggers == null) {
            return;
        }

        if (triggers.getOnError() != null) {
            if (ACommonFragmentRefsUpdater.refNameEquals(config, triggers.getOnError().getMailFragmentName())) {
                if (isDebugEnabled) {
                    LOGGER
                        .debug(
                            String
                                .format(
                                    "[%s][%s][profile=%s][notificationTriggers.onError.mailFragmentName=%s]new name=%s",
                                    getLogPrefix(),
                                    config.getTypeAsEnum(),
                                    referencedIn.getPath(),
                                    triggers.getOnError().getMailFragmentName(),
                                    newName));
                }
                triggers.getOnError().setMailFragmentName(newName);
                changed = true;
            }
        }
        if (triggers.getOnSuccess() != null) {
            if (ACommonFragmentRefsUpdater.refNameEquals(config, triggers.getOnSuccess().getMailFragmentName())) {
                if (isDebugEnabled) {
                    LOGGER
                        .debug(
                            String
                                .format(
                                    "[%s][%s][profile=%s][notificationTriggers.onSuccess.mailFragmentName=%s]new name=%s",
                                    getLogPrefix(),
                                    config.getTypeAsEnum(),
                                    referencedIn.getPath(),
                                    triggers.getOnSuccess().getMailFragmentName(),
                                    newName));
                }
                triggers.getOnSuccess().setMailFragmentName(newName);
                changed = true;
            }
        }
        if (triggers.getOnEmptyFiles() != null) {
            if (ACommonFragmentRefsUpdater.refNameEquals(config, triggers.getOnEmptyFiles().getMailFragmentName())) {
                if (isDebugEnabled) {
                    LOGGER
                        .debug(
                            String
                                .format(
                                    "[%s][%s][profile=%s][notificationTriggers.onEmptyFiles.mailFragmentName=%s]new name=%s",
                                    getLogPrefix(),
                                    config.getTypeAsEnum(),
                                    referencedIn.getPath(),
                                    triggers.getOnEmptyFiles().getMailFragmentName(),
                                    newName));
                }
                triggers.getOnEmptyFiles().setMailFragmentName(newName);
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        referencedIn.setContent(Globals.objectMapper.writeValueAsString(jr));
        referencedIn.setDeployed(false);
        ACommonFragmentRefsUpdater.applyChange(dbLayer, referencedIn, items, events);
    }
}