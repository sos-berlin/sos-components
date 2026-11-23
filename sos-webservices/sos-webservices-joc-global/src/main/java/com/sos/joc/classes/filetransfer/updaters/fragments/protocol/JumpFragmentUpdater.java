package com.sos.joc.classes.filetransfer.updaters.fragments.protocol;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class JumpFragmentUpdater extends AProtocolFragmentUpdater<JumpFragment, String> {

    @Override
    protected String getLogPrefix() {
        return "updateJumpConfiguration";
    }

    @Override
    protected String getFragmentLogName() {
        return "jumpFragment";
    }

    @Override
    protected JumpFragment readFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), JumpFragment.class);
    }

    @Override
    protected String getAlternativeName(JumpFragment f) {
        return f.getAlternativeJumpFragmentName();
    }

    @Override
    protected void setAlternativeName(JumpFragment f, String newName) {
        f.setAlternativeJumpFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternativeFragments(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferJumpFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFragmentsByJumpExcludingSelf(name);
    }

    @Override
    /** @return <code>true</code> */
    public boolean isJump() {
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