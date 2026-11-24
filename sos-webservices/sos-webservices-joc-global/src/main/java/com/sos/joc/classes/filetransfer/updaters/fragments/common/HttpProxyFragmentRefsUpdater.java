package com.sos.joc.classes.filetransfer.updaters.fragments.common;

import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.fragments.protocol.AProtocolFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.WebdavFragment;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public class HttpProxyFragmentRefsUpdater extends ACommonFragmentRefsUpdater {

    @Override
    protected String getLogPrefix() {
        return "updateHttpProxyRefs";
    }

    @Override
    protected String getFragmentLogName() {
        return "httpProxyFragment";
    }

    @Override
    protected String getRefName(AProtocolFragment f) {
        if (f instanceof SftpFragment sftp) {
            return sftp.getHttpProxyFragmentName();
        } else if (f instanceof FtpFragment ftp) {
            return ftp.getHttpProxyFragmentName();
        } else if (f instanceof JumpFragment jump) {
            return jump.getHttpProxyFragmentName();
        } else if (f instanceof HttpFragment http) {
            return http.getHttpProxyFragmentName();
        } else if (f instanceof WebdavFragment webdav) {
            return webdav.getHttpProxyFragmentName();
        } else if (f instanceof AzureBlobStorageFragment azure) {
            return azure.getHttpProxyFragmentName();
        }
        return null;
    }

    @Override
    protected void setRefName(AProtocolFragment f, String newName) {
        if (f instanceof SftpFragment sftp) {
            sftp.setHttpProxyFragmentName(newName);
        } else if (f instanceof FtpFragment ftp) {
            ftp.setHttpProxyFragmentName(newName);
        } else if (f instanceof JumpFragment jump) {
            jump.setHttpProxyFragmentName(newName);
        } else if (f instanceof HttpFragment http) {
            http.setHttpProxyFragmentName(newName);
        } else if (f instanceof WebdavFragment webdav) {
            webdav.setHttpProxyFragmentName(newName);
        } else if (f instanceof AzureBlobStorageFragment azure) {
            azure.setHttpProxyFragmentName(newName);
        }
    }

    @Override
    protected List<DBItemInventoryConfiguration> getReferencedIn(InventoryDBLayer dbLayer, String name) throws SOSHibernateException {
        return dbLayer.getUsedFileTransferFragmentsByHttpProxyFragmentName(name);
    }

}
