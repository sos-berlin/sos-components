package com.sos.joc.classes.filetransfer.updaters.fragments.protocol;

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

public class FtpFragmentUpdater extends AProtocolFragmentUpdater<FtpFragment, FtpFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateFtpConfiguration";
    }

    @Override
    protected String getFragmentLogName() {
        return "ftpFragment";
    }

    @Override
    protected FtpFragment readFragment(DBItemInventoryConfiguration c) throws Exception {
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
    protected List<DBItemInventoryConfiguration> getAlternativeFragments(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFtpFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByFtpFragmentName(name);
    }

    @Override
    protected boolean isSourceFragment(Source source) {
        return FileTransferHelper.isSourceFragmentFtp(source);
    }

    @Override
    protected boolean isTargetFragment(Target target) {
        return FileTransferHelper.isTargetFragmentFtp(target);
    }

    @Override
    protected FtpFragmentRef getSourceFragment(Source source) {
        return source.getFragment().getFtp();
    }

    @Override
    protected FtpFragmentRef getTargetFragment(Target target) {
        return target.getFragment().getFtp();
    }

    @Override
    protected String getFragmentName(FtpFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setFragmentName(FtpFragmentRef f, String newName) {
        f.setName(newName);
    }
}