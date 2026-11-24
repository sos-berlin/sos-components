package com.sos.joc.classes.filetransfer.updaters.fragments.common;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class SocksProxyFragmentRefsUpdater extends ACommonFragmentRefsUpdater {

    @Override
    protected String getLogPrefix() {
        return "updateSocksProxyRefs";
    }

    @Override
    protected String getFragmentLogName() {
        return "socksProxyFragment";
    }

    @Override
    protected String getRefName(AProtocolFragment f) {
        if (f instanceof SftpFragment sftp) {
            return sftp.getSocksProxyFragmentName();
        } else if (f instanceof FtpFragment ftp) {
            return ftp.getSocksProxyFragmentName();
        } else if (f instanceof JumpFragment jump) {
            return jump.getSocksProxyFragmentName();
        }
        return null;
    }

    @Override
    protected void setRefName(AProtocolFragment f, String newName) {
        if (f instanceof SftpFragment sftp) {
            sftp.setSocksProxyFragmentName(newName);
        } else if (f instanceof FtpFragment ftp) {
            ftp.setSocksProxyFragmentName(newName);
        } else if (f instanceof JumpFragment jump) {
            jump.setSocksProxyFragmentName(newName);
        }
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFragmentsByHttpProxyFragmentName(name);
    }

}
