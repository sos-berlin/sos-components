package com.sos.commons.vfs.commons;

/** Represents a reusable resource for a provider.
 * <p>
 * This class encapsulates a provider-specific client or connection object (e.g., SFTP client, SMB DiskShare, etc.).
 * <p>
 * Key principles:
 * <ul>
 * <li>Each operation borrows an available resource from the pool and returns it after use.</li>
 * <li>Resources may be used by multiple threads, but never concurrently: the pool ensures exclusive access per usage.</li>
 * <li>The maximum number of concurrently active resources is determined by the parallelism level.</li>
 * </ul>
 * <p>
 * The concrete subclass must manage the lifecycle of the underlying resource and implement {@link AutoCloseable#close()}.
 * <p>
 * Example usage:
 * <ul>
 * <li>SSHJProvider: wraps an SFTPClient</li>
 * <li>SMBJProvider: wraps a DiskShare</li>
 * </ul>
 *
 * @param <R> the type of the underlying provider-specific client or resource */
public abstract class AProviderReusableResource<R> implements AutoCloseable {

    private final long id;
    private final Object provider;
    private final String resourceName;

    public AProviderReusableResource(long id, Object provider, Class<?> resourceClazz) {
        this.id = id;
        this.provider = provider;
        this.resourceName = resourceClazz.getSimpleName();
    }

    public long getId() {
        return id;
    }

    public Object getProvider() {
        return provider;
    }

    /** returns SFTPClient, DiskShare ... */
    public abstract R getResource();

    public void logOnCreated() {
        AProvider<?, ?> p = (AProvider<?, ?>) provider;
        if (p.getLogger().isDebugEnabled()) {
            p.getLogger().debug("%s[id=%s]%s created", p.getLogPrefix(), getId(), resourceName);
        }
    }

    public void logOnClosed() {
        AProvider<?, ?> p = (AProvider<?, ?>) provider;
        if (p.getLogger().isDebugEnabled()) {
            p.getLogger().debug("%s[id=%s]%s closed", p.getLogPrefix(), getId(), resourceName);
        }
    }

}
