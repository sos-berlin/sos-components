package com.sos.commons.vfs.smb.smbj;

import com.hierynomus.smbj.share.DiskShare;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.commons.AProviderReusableResource;

public class SMBJProviderReusableResource extends AProviderReusableResource<SMBJProvider> {

    private DiskShare diskShare;

    public SMBJProviderReusableResource(SMBJProvider provider) throws Exception {
        super(provider);
    }

    @Override
    public void close() throws Exception {
        SOSClassUtil.closeQuietly(diskShare);
        diskShare = null;
    }

    public DiskShare getDiskShare(String path) {
        tryConnectShare(path);
        return diskShare;
    }

    private void tryConnectShare(String path) {
        if (diskShare == null || !diskShare.isConnected()) {
            diskShare = getProvider().connectShare(path);
        }
    }

}
