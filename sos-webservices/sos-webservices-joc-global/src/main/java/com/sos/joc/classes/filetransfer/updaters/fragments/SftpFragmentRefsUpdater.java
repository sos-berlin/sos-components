package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.SftpFragmentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class SftpFragmentRefsUpdater extends AProtocolOrProfileFragmentRefsUpdater<SftpFragment, SftpFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateSftpRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "sftpFragment";
    }

    @Override
    protected SftpFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), SftpFragment.class);
    }

    @Override
    protected String getAlternativeName(SftpFragment f) {
        return f.getAlternativeSftpFragmentName();
    }

    @Override
    protected void setAlternativeName(SftpFragment f, String newName) {
        f.setAlternativeSftpFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferSftpFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesBySftpFragmentName(name);
    }

    @Override
    protected boolean hasSourceFragmentRef(Source source) {
        return FileTransferHelper.isSourceFragmentSftp(source);
    }

    @Override
    protected boolean hasTargetFragmentRef(Target target) {
        return FileTransferHelper.isTargetFragmentSftp(target);
    }

    @Override
    protected SftpFragmentRef getSourceFragmentRef(Source source) {
        return source.getFragment().getSftp();
    }

    @Override
    protected SftpFragmentRef getTargetFragmentRef(Target target) {
        return target.getFragment().getSftp();
    }

    @Override
    protected String getRefName(SftpFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setRefName(SftpFragmentRef f, String newName) {
        f.setName(newName);
    }
}