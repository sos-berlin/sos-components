package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.SmbFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.SmbFragmentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class SmbFragmentRefsUpdater extends AProtocolOrProfileFragmentReferencesUpdater<SmbFragment, SmbFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateSmbRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "smbFragment";
    }

    @Override
    protected SmbFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), SmbFragment.class);
    }

    @Override
    protected String getAlternativeName(SmbFragment f) {
        return f.getAlternativeSmbFragmentName();
    }

    @Override
    protected void setAlternativeName(SmbFragment f, String newName) {
        f.setAlternativeSmbFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferSmbFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesBySmbFragmentName(name);
    }

    @Override
    protected boolean isSourceFragment(Source source) {
        return FileTransferHelper.isSourceFragmentSmb(source);
    }

    @Override
    protected boolean isTargetFragment(Target target) {
        return FileTransferHelper.isTargetFragmentSmb(target);
    }

    @Override
    protected SmbFragmentRef getSourceFragment(Source source) {
        return source.getFragment().getSmb();
    }

    @Override
    protected SmbFragmentRef getTargetFragment(Target target) {
        return target.getFragment().getSmb();
    }

    @Override
    protected String getFragmentName(SmbFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setFragmentName(SmbFragmentRef f, String newName) {
        f.setName(newName);
    }
}