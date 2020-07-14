package com.sos.joc.classes.proxy;

import java.util.concurrent.ExecutionException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;


public class Proxy {

    private Proxy() {
        //
    }
    
    /**
     * 
     * @param url
     * @return ProxyContext
     */
    public static synchronized ProxyContext start(String url) {
        return Proxies.getInstance().start(ProxyCredentialsBuilder.withUrl(url).build());
    }
    
    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @return ProxyContext
     */
    public static synchronized ProxyContext start(ProxyCredentials credentials) {
        return Proxies.getInstance().start(credentials);
    }

    /**
     * 
     * @param url
     * @return ProxyContext
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized ProxyContext of(String url) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            ExecutionException {
        return Proxies.getInstance().of(ProxyCredentialsBuilder.withUrl(url).build(), Globals.httpConnectionTimeout);
    }

    /**
     * 
     * @param url
     * @param connectionTimeout (in milliseconds)
     * @return ProxyContext
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized ProxyContext of(String url, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(ProxyCredentialsBuilder.withUrl(url).build(), connectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @return ProxyContext
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized ProxyContext of(ProxyCredentials credentials) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(credentials, Globals.httpConnectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @param connectionTimeout (in milliseconds)
     * @return ProxyContext
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized ProxyContext of(ProxyCredentials credentials, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(credentials, connectionTimeout);
    }
    
    /**
     * 
     * @param url
     */
    public static synchronized void close(String url) {
        Proxies.getInstance().close(ProxyCredentialsBuilder.withUrl(url).build());
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     */
    public static synchronized void close(ProxyCredentials credentials) {
        Proxies.getInstance().close(credentials);
    }

}
