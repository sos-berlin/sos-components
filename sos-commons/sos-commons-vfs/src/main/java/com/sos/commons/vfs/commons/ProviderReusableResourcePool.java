package com.sos.commons.vfs.commons;

/** A thread-safe pool for reusable provider resources.
 * <p>
 * This pool manages provider-specific reusable resources (e.g. SFTPClient, SMBJ DiskShare, etc) and ensures controlled creation, reuse, and cleanup of these
 * resources.
 * <p>
 * Each resource is created lazily on demand and may be reused across multiple operations.<br />
 * Resources are automatically returned to the pool when using {@link #withResource(ResourceFunction)} or {@link #withResource(ResourceConsumer)}.
 * <p>
 * Once the pool is closed, all idle resources are closed and no further borrowing is allowed.
 *
 * @param <R> the concrete reusable resource type
 * @param <C> the underlying provider-specific resource (client/handle/session) */
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.vfs.exceptions.ProviderException;

public final class ProviderReusableResourcePool<R extends AProviderReusableResource<C>, C> {

    /** Functional interface for creating a new reusable provider resource.
     *
     * @param <R> the reusable provider resource type */
    @FunctionalInterface
    public interface ResourceFactory<R> {

        R create(Long id) throws Exception;
    }

    /** Functional interface for executing an operation with a provider resource that returns a result.
     *
     * @param <C> the underlying provider-specific resource type
     * @param <T> the result type */
    @FunctionalInterface
    public interface ResourceFunction<C, T> {

        /** Executes the operation.
         *
         * @param resource the provider-specific resource
         * @return the operation result
         * @throws Exception if the operation fails */
        T apply(C resource) throws Exception;
    }

    /** Functional interface for executing an operation with a provider resource that does not return a result.
     *
     * @param <C> the underlying provider-specific resource type */
    @FunctionalInterface
    public interface ResourceConsumer<C> {

        /** Executes the operation.
         *
         * @param resource the provider-specific resource
         * @throws Exception if the operation fails */
        void accept(C resource) throws Exception;
    }

    private final AtomicLong sequence = new AtomicLong(0);
    private final ConcurrentLinkedQueue<R> idle = new ConcurrentLinkedQueue<>();
    private final ResourceFactory<R> factory;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private int minResources = 1;

    public ProviderReusableResourcePool(ResourceFactory<R> factory) {
        this.factory = factory;
    }

    /** Borrows a reusable resource from the pool.
     * <p>
     * If an idle resource is available, it is reused. Otherwise, a new resource is created using the configured factory.
     *
     * @return a reusable provider resource
     * @throws ProviderException if the pool has already been closed or can't be created */
    public R borrow() throws ProviderException {
        if (closed.get()) {
            throw new ProviderException("Reusable resource pool already closed");
        }

        R resource = idle.poll();
        if (resource != null) {
            return resource;
        }
        try {
            return factory.create(sequence.incrementAndGet());
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException(e);
        }
    }

    /** Releases a previously borrowed resource back to the pool.
     * <p>
     * If the pool is already closed, the resource is closed immediately.
     *
     * @param resource the resource to release (may be {@code null}) */
    public <T extends R> void release(T resource) {
        if (resource == null) {
            return;
        }

        if (closed.get()) {
            SOSClassUtil.closeQuietly(resource);
        } else {
            idle.offer(resource);
        }
    }

    /** Reduces the pool size to the minimal number of resources.<br/>
     * Closes extra resources beyond minResources. */
    public void reduce() {
        while (idle.size() > minResources) {
            R resource = idle.poll();
            if (resource != null) {
                SOSClassUtil.closeQuietly(resource);
            }
        }
    }

    /** Closes the pool and all currently idle resources.
     * <p>
     * After calling this method, the pool is considered closed and no further resources can be borrowed.<br/>
     * Releasing resources after closure will cause them to be closed immediately. */
    public void closeAll() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        R resource;
        while ((resource = idle.poll()) != null) {
            SOSClassUtil.closeQuietly(resource);
        }
    }

    /** Executes an operation using a borrowed resource and automatically releases the resource back to the pool.
     * <p>
     * The resource is guaranteed to be released even if the operation throws an exception.
     *
     * @param fn the operation to execute
     * @param <T> the result type
     * @return the result of the operation
     * @throws Exception if the operation fails */
    public <T> T withResource(ResourceFunction<C, T> fn) throws Exception {
        R res = null;
        try {
            res = borrow();
            return fn.apply(res.getResource());
        } finally {
            release(res);
        }
    }

    /** Executes an operation using a borrowed resource and automatically releases the resource back to the pool.
     * <p>
     * The resource is guaranteed to be released even if the operation throws an exception.
     *
     * @param fn the operation to execute
     * @throws Exception if the operation fails */
    public void withResource(ResourceConsumer<C> fn) throws Exception {
        R res = null;
        try {
            res = borrow();
            fn.accept(res.getResource());
        } finally {
            release(res);
        }
    }

}
