package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.FtpFragmentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class FtpFragmentRefsUpdater extends AProtocolOrProfileFragmentRefsUpdater<FtpFragment, FtpFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateFtpRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "ftpFragment";
    }

    @Override
    protected FtpFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), FtpFragment.class);
    }

    @Override
    protected String getAlternativeName(FtpFragment f) {
        return f.getAlternativeFtpFragmentName();
    }

    @Override
    protected void setAlternativeName(FtpFragment f, String newName) {
        f.setAlternativeFtpFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFtpFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByFtpFragmentName(name);
    }

    @Override
    protected boolean hasSourceFragmentRef(Source source) {
        return FileTransferHelper.isSourceFragmentFtp(source);
    }

    @Override
    protected boolean hasTargetFragmentRef(Target target) {
        return FileTransferHelper.isTargetFragmentFtp(target);
    }

    @Override
    protected FtpFragmentRef getSourceFragmentRef(Source source) {
        return source.getFragment().getFtp();
    }

    @Override
    protected FtpFragmentRef getTargetFragmentRef(Target target) {
        return target.getFragment().getFtp();
    }

    @Override
    protected String getRefName(FtpFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setRefName(FtpFragmentRef f, String newName) {
        f.setName(newName);
    }
}