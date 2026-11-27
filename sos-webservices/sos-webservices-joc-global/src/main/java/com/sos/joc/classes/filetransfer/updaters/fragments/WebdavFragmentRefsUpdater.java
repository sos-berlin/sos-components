package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.WebdavFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.WebdavFragmentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class WebdavFragmentRefsUpdater extends AProtocolOrProfileFragmentRefsUpdater<WebdavFragment, WebdavFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateWebdavRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "webdavFragment";
    }

    @Override
    protected WebdavFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), WebdavFragment.class);
    }

    @Override
    protected String getAlternativeName(WebdavFragment f) {
        return f.getAlternativeFragmentName();
    }

    @Override
    protected void setAlternativeName(WebdavFragment f, String newName) {
        f.setAlternativeFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferWebdavFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByWebdavFragmentName(name);
    }

    @Override
    protected boolean hasSourceFragmentRef(Source source) {
        return FileTransferHelper.isSourceFragmentWebdav(source);
    }

    @Override
    protected boolean hasTargetFragmentRef(Target target) {
        return FileTransferHelper.isTargetFragmentWebdav(target);
    }

    @Override
    protected WebdavFragmentRef getSourceFragmentRef(Source source) {
        return source.getFragment().getWebdav();
    }

    @Override
    protected WebdavFragmentRef getTargetFragmentRef(Target target) {
        return target.getFragment().getWebdav();
    }

    @Override
    protected String getRefName(WebdavFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setRefName(WebdavFragmentRef f, String newName) {
        f.setName(newName);
    }
}