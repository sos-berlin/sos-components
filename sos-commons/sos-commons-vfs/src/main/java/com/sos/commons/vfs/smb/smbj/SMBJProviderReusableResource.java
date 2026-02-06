package com.sos.commons.vfs.smb.smbj;

import com.hierynomus.smbj.share.DiskShare;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.commons.AProviderReusableResource;
import com.sos.commons.vfs.exceptions.ProviderException;

public class SMBJProviderReusableResource extends AProviderReusableResource<DiskShare> {

    private DiskShare diskShare;

    public SMBJProviderReusableResource(long id, SMBJProvider provider, String path) throws Exception {
        super(id, provider, DiskShare.class);
        tryConnectShare(path);
        logOnCreated();
    }

    @Override
    public void close() throws Exception {
        SOSClassUtil.closeQuietly(diskShare);
        diskShare = null;
        logOnClosed();
    }

    @Override
    public DiskShare getResource() {
        return diskShare;
    }

    private void tryConnectShare(String path) throws ProviderException {
        if (diskShare == null || !diskShare.isConnected()) {
            diskShare = ((SMBJProvider) getProvider()).connectShare(path);
        }
    }

}
