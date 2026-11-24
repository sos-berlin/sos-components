package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.HttpFragmentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class HttpFragmentRefsUpdater extends AProtocolOrProfileFragmentReferencesUpdater<HttpFragment, HttpFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateHttpRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "httpFragment";
    }

    @Override
    protected HttpFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), HttpFragment.class);
    }

    @Override
    protected String getAlternativeName(HttpFragment f) {
        return f.getAlternativeHttpFragmentName();
    }

    @Override
    protected void setAlternativeName(HttpFragment f, String newName) {
        f.setAlternativeHttpFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferHttpFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByHttpFragmentName(name);
    }

    @Override
    protected boolean isSourceFragment(Source source) {
        return FileTransferHelper.isSourceFragmentHttp(source);
    }

    @Override
    protected boolean isTargetFragment(Target target) {
        return FileTransferHelper.isTargetFragmentHttp(target);
    }

    @Override
    protected HttpFragmentRef getSourceFragment(Source source) {
        return source.getFragment().getHttp();
    }

    @Override
    protected HttpFragmentRef getTargetFragment(Target target) {
        return target.getFragment().getHttp();
    }

    @Override
    protected String getFragmentName(HttpFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setFragmentName(HttpFragmentRef f, String newName) {
        f.setName(newName);
    }
}