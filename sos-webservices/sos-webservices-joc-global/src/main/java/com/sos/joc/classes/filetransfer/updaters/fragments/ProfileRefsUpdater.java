package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.joc.classes.filetransfer.FileTransferJobResourceHandler;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class ProfileRefsUpdater extends AProtocolOrProfileFragmentRefsUpdater<FileTransferJobResource, String> {

    @Override
    protected String getLogPrefix() {
        return "updateProfileRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "profile";
    }

    @Override
    protected FileTransferJobResource readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return FileTransferJobResourceHandler.toFileTransferJobResource(c.getContent());
    }

    @Override
    protected String getAlternativeName(FileTransferJobResource f) {
        if (f.getProfile() == null) {
            return null;
        }
        return f.getProfile().getAlternativeProfileName();
    }

    @Override
    protected void setAlternativeName(FileTransferJobResource f, String newName) {
        if (f.getProfile() == null) {
            return;
        }
        f.getProfile().setAlternativeProfileName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfileAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return null;
    }

    @Override
    protected boolean hasSourceFragmentRef(Source source) {
        return false;
    }

    @Override
    protected boolean hasTargetFragmentRef(Target target) {
        return false;
    }

    @Override
    protected String getSourceFragmentRef(Source source) {
        return null;
    }

    @Override
    protected String getTargetFragmentRef(Target target) {
        return null;
    }

    @Override
    protected String getRefName(String f) {
        return null;
    }

    @Override
    protected void setRefName(String f, String newName) {
    }
}