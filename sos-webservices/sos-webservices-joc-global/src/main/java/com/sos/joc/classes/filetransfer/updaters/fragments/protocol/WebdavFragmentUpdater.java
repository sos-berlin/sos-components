package com.sos.joc.classes.filetransfer.updaters.fragments.protocol;

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

public class WebdavFragmentUpdater extends AProtocolFragmentUpdater<WebdavFragment, WebdavFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updatewebdavConfiguration";
    }

    @Override
    protected String getFragmentLogName() {
        return "webdavFragment";
    }

    @Override
    protected WebdavFragment readFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), WebdavFragment.class);
    }

    @Override
    protected String getAlternativeName(WebdavFragment f) {
        return f.getAlternativeWebdavFragmentName();
    }

    @Override
    protected void setAlternativeName(WebdavFragment f, String newName) {
        f.setAlternativeWebdavFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternativeFragments(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferWebdavFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByWebdavFragmentName(name);
    }

    @Override
    protected boolean isSourceFragment(Source source) {
        return FileTransferHelper.isSourceFragmentWebdav(source);
    }

    @Override
    protected boolean isTargetFragment(Target target) {
        return FileTransferHelper.isTargetFragmentWebdav(target);
    }

    @Override
    protected WebdavFragmentRef getSourceFragment(Source source) {
        return source.getFragment().getWebdav();
    }

    @Override
    protected WebdavFragmentRef getTargetFragment(Target target) {
        return target.getFragment().getWebdav();
    }

    @Override
    protected String getFragmentName(WebdavFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setFragmentName(WebdavFragmentRef f, String newName) {
        f.setName(newName);
    }
}