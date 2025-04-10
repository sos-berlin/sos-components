package com.sos.commons.vfs.commons;

import com.sos.commons.vfs.ssh.sshj.SSHJProvider;

/** Defines reusable content.<br/>
 * Example: SSHJ implementation<br/>
 * - the SFTPClient does not support multi-threading and is not globally initialized in {@link SSHJProvider}<br/>
 * - if a reusable resource is enabled (default) - all operations are performed by the same SFTPClient<br/>
 * - otherwise, a new SFTPClient will be created for each operation (e.g., "exists, "delete", etc.)<br/>
 * see {@link SSHJProviderReusableResource} */
public abstract class AProviderReusableResource<A extends AProvider<?>> implements AutoCloseable {

    private final A provider;

    public AProviderReusableResource(A provider) {
        this.provider = provider;
    }

    public A getProvider() {
        return provider;
    }

    // not used - can be removed ...
    public static <D extends AProvider<?>> DefaultReusableResource<D> initializeDefault(D provider) {
        return new DefaultReusableResource<>(provider);
    }

    public static class DefaultReusableResource<D extends AProvider<?>> extends AProviderReusableResource<D> {

        public DefaultReusableResource(D provider) {
            super(provider);
        }

        @Override
        public void close() {
        }
    }
}
