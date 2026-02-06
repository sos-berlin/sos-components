package com.sos.commons.vfs.ssh.sshj;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.commons.AProviderReusableResource;

import net.schmizz.sshj.sftp.SFTPClient;

public class SSHJProviderReusableResource extends AProviderReusableResource<SFTPClient> {

    private SFTPClient client;

    public SSHJProviderReusableResource(long id, SSHJProvider provider) throws Exception {
        super(id, provider, SFTPClient.class);
        client = provider.requireSSHClient().newSFTPClient();
        logOnCreated();
    }

    @Override
    public void close() throws Exception {
        SOSClassUtil.closeQuietly(client);
        client = null;
        logOnClosed();
    }

    @Override
    public SFTPClient getResource() {
        return client;
    }

}
