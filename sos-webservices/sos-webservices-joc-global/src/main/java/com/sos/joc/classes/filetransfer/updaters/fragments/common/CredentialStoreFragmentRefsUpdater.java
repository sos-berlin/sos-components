package com.sos.joc.classes.filetransfer.updaters.fragments.common;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class CredentialStoreFragmentRefsUpdater extends ACommonFragmentReferencesUpdater {

    @Override
    protected String getLogPrefix() {
        return "updateCsRefs";
    }

    @Override
    protected String getFragmentLogName() {
        return "csFragment";
    }

    @Override
    protected String getRefName(AProtocolFragment f) {
        return f.getCredentialStoreFragmentName();
    }

    @Override
    protected void setRefName(AProtocolFragment f, String newName) {
        f.setCredentialStoreFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFragmentsByCredentialStoreFragmentName(name);
    }

}
