package com.sos.joc.classes.filetransfer.updaters.fragments.protocol;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.AzureBlobStorageFragmentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.filetransfer.FileTransferHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class AzureBlobStorageFragmentUpdater extends AProtocolFragmentUpdater<AzureBlobStorageFragment, AzureBlobStorageFragmentRef> {

    @Override
    protected String getLogPrefix() {
        return "updateAzureBlobStorageConfiguration";
    }

    @Override
    protected String getFragmentLogName() {
        return "azureBlobStorageFragment";
    }

    @Override
    protected AzureBlobStorageFragment readFragment(DBItemInventoryConfiguration c) throws Exception {
        return Globals.objectMapper.readValue(c.getContent(), AzureBlobStorageFragment.class);
    }

    @Override
    protected String getAlternativeName(AzureBlobStorageFragment f) {
        return f.getAlternativeAzureBlobStorageFragmentName();
    }

    @Override
    protected void setAlternativeName(AzureBlobStorageFragment f, String newName) {
        f.setAlternativeAzureBlobStorageFragmentName(newName);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getAlternativeFragments(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferAzureBlobStorageFragmentAlternatives(name);
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferProfilesByAzureBlobStorageFragmentName(name);
    }

    @Override
    protected boolean isSourceFragment(Source source) {
        return FileTransferHelper.isSourceFragmentAzureBlobStorage(source);
    }

    @Override
    protected boolean isTargetFragment(Target target) {
        return FileTransferHelper.isTargetFragmentAzureBlobStorage(target);
    }

    @Override
    protected AzureBlobStorageFragmentRef getSourceFragment(Source source) {
        return source.getFragment().getAzureBlobStorage();
    }

    @Override
    protected AzureBlobStorageFragmentRef getTargetFragment(Target target) {
        return target.getFragment().getAzureBlobStorage();
    }

    @Override
    protected String getFragmentName(AzureBlobStorageFragmentRef f) {
        return f.getName();
    }

    @Override
    protected void setFragmentName(AzureBlobStorageFragmentRef f, String newName) {
        f.setName(newName);
    }
}