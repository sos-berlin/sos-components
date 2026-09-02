package com.sos.commons.vfs.ssh.sshj;

import java.net.InetSocketAddress;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;

/** SSH client that keeps the remote hostname unresolved.
 * 
 * When a SOCKS proxy is used, the proxy therefore receives the resolved IP address instead of the hostname.<br />
 * This does not work with SOCKS proxies that use hostname-based (FQDN) whitelist rules. */
public class SSHJFqdnSSHClient extends SSHClient {

    public SSHJFqdnSSHClient(Config config) {
        super(config);
    }

    /** Creates an unresolved socket address to preserve the destination hostname. */
    @Override
    protected InetSocketAddress makeInetSocketAddress(String hostname, int port) {
        return InetSocketAddress.createUnresolved(hostname, port);
    }

}
