package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class JumpFragmentRefsUpdater extends AProtocolOrProfileFragmentRefsUpdater<JumpFragment, String> {

    @Override
    protected String getLogPrefix() {
        return "updateJumpRefs";
    }

    @Override
    protected String getLogObjectName() {
        return "jumpFragment";
    }

    @Override
    protected JumpFragment readAlternativeFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), JumpFragment.class);
    }

    @Override
    protected String getAlternativeName(JumpFragment f) {
        return f.getAlternativeFragmentName();
    }

    @Override
    protected void setAlternativeName(JumpFragment f, String newName) {
        f.setAlternativeFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternatives(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferJumpFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFragmentsByJumpFragmentNameExcludingSelf(name);
    }

    @Override
    /** @return <code>true</code> */
    public boolean isJump() {
        return true;
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
        return f;
    }

    @Override
    protected void setRefName(String f, String newName) {

    }
}