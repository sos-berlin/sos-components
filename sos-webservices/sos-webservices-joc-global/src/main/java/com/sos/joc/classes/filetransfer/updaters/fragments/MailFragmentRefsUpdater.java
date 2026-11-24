package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.mail.MailFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class MailFragmentRefsUpdater extends AProtocolOrProfileFragmentReferencesUpdater<MailFragment, String> {

    @Override
    protected String getLogPrefix() {
        return "updateMailRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "mailFragment";
    }

    @Override
    protected MailFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return null;
    }

    @Override
    protected String getAlternativeName(MailFragment f) {
        return null;
    }

    @Override
    protected void setAlternativeName(MailFragment f, String newName) {
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return null;
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByMailFragmentName(name);
    }

    @Override
    /** @return <code>true</code> */
    public boolean isMail() {
        return true;
    }

    @Override
    protected boolean isSourceFragment(Source source) {
        return false;
    }

    @Override
    protected boolean isTargetFragment(Target target) {
        return false;
    }

    @Override
    protected String getSourceFragment(Source source) {
        return null;
    }

    @Override
    protected String getTargetFragment(Target target) {
        return null;
    }

    @Override
    protected String getFragmentName(String f) {
        return f;
    }

    @Override
    protected void setFragmentName(String f, String newName) {

    }
}