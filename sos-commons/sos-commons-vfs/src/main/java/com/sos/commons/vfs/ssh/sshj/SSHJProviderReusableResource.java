package com.sos.commons.vfs.ssh.sshj;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.commons.AProviderReusableResource;
import com.sos.commons.vfs.exceptions.ProviderException;

import net.schmizz.sshj.sftp.SFTPClient;

public class SSHJProviderReusableResource extends AProviderReusableResource<SFTPClient> {

    private SFTPClient client;

    public SSHJProviderReusableResource(long id, SSHJProvider provider) throws ProviderException {
        super(id, provider, SFTPClient.class);
        try {
            client = provider.requireSSHClient().newSFTPClient();
        } catch (Exception e) {
            throw new ProviderException("[create newSFTPClient]" + e.toString(), e);
        }
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
