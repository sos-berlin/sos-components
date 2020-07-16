package com.sos.joc.classes.proxy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

import js7.proxy.javaapi.JControllerProxy;


public class Proxy {

    private Proxy() {
        //
    }
    
    /**
     * 
     * @param url
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException 
     */
    public static synchronized ProxyContext start(String url) throws JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().start(ProxyCredentialsBuilder.withUrl(url).build());
    }
    
    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException 
     */
    public static synchronized ProxyContext start(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().start(credentials);
    }

    /**
     * 
     * @param url
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JControllerProxy of(String url) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            ExecutionException {
        return Proxies.getInstance().of(ProxyCredentialsBuilder.withUrl(url).build(), Globals.httpConnectionTimeout);
    }

    /**
     * 
     * @param url
     * @param connectionTimeout (in milliseconds)
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JControllerProxy of(String url, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(ProxyCredentialsBuilder.withUrl(url).build(), connectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JControllerProxy of(ProxyCredentials credentials) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(credentials, Globals.httpConnectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @param connectionTimeout (in milliseconds)
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JControllerProxy of(ProxyCredentials credentials, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(credentials, connectionTimeout);
    }
    
    /**
     * 
     * @param url
     * @return CompletableFuture<Void> 
     */
    public static synchronized CompletableFuture<Void> close(String url) {
        return Proxies.getInstance().close(ProxyCredentialsBuilder.withUrl(url).build());
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @return CompletableFuture<Void>
     */
    public static synchronized CompletableFuture<Void> close(ProxyCredentials credentials) {
        return Proxies.getInstance().close(credentials);
    }

}
