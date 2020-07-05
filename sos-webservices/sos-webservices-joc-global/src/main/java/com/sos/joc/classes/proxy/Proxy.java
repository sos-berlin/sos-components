package com.sos.joc.classes.proxy;

import java.util.concurrent.ExecutionException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

import js7.proxy.javaapi.JMasterProxy;

public class Proxy {

    private Proxy() {
        //
    }

    /**
     * 
     * @param url
     * @return JMasterProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JMasterProxy of(String url) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException,
            ExecutionException {
        return Proxies.getInstance().of(ProxyCredentialsBuilder.withUrl(url).build(), Globals.httpConnectionTimeout);
    }

    /**
     * 
     * @param url
     * @param connectionTimeout (in milliseconds)
     * @return JMasterProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JMasterProxy of(String url, int connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(ProxyCredentialsBuilder.withUrl(url).build(), connectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @return JMasterProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JMasterProxy of(ProxyCredentials credentials) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(credentials, Globals.httpConnectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     * @param connectionTimeout (in milliseconds)
     * @return JMasterProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     */
    public static synchronized JMasterProxy of(ProxyCredentials credentials, int connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException {
        return Proxies.getInstance().of(credentials, connectionTimeout);
    }

    /**
     * 
     * @param credentials (use ProxyCredentialsBuilder to create ProxyCredentials)
     */
    public static synchronized void close(ProxyCredentials credentials) {
        Proxies.getInstance().close(credentials);
    }

}
