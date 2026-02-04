package com.sos.commons.vfs.ssh.sshj;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.commons.AProviderReusableResource;

import net.schmizz.sshj.sftp.SFTPClient;

public class SSHJProviderReusableResource extends AProviderReusableResource<SSHJProvider> {

    private SFTPClient sftpClient;

    public SSHJProviderReusableResource(SSHJProvider provider) throws Exception {
        super(provider);
        sftpClient = provider.requireSSHClient().newSFTPClient();
    }

    @Override
    public void close() throws Exception {
        SOSClassUtil.closeQuietly(sftpClient);
        sftpClient = null;
    }

    public SFTPClient getSFTPClient() {
        return sftpClient;
    }

}
