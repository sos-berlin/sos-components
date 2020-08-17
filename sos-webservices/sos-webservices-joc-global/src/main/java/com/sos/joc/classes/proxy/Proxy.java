package com.sos.joc.classes.proxy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;

import js7.proxy.javaapi.JControllerProxy;


public class Proxy {

    private Proxy() {
        //
    }
    
    /**
     * Starts Proxy for user 'JOC'
     * @see start(String jobschedulerId, ProxyUser user)
     * @param jobschedulerId
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException 
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized ProxyContext start(String jobschedulerId) throws JobSchedulerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().start(jobschedulerId, ProxyUser.JOC);
    }
    
    /**
     * Starts Proxy for specified user
     * @param jobschedulerId
     * @param user
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized ProxyContext start(String jobschedulerId, ProxyUser user) throws JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().start(jobschedulerId, user);
    }
    
    /**
     * Returns Proxy for user 'JOC' and starts it if necessary
     * @see of(String jobschedulerId, ProxyUser user)
     * @param jobschedulerId
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerProxy of(String jobschedulerId) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(jobschedulerId, ProxyUser.JOC, Globals.httpConnectionTimeout);
    }

    /**
     * Returns Proxy for specified user and starts it if necessary
     * @param jobschedulerId
     * @param user
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerProxy of(String jobschedulerId, ProxyUser user) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(jobschedulerId, user, Globals.httpConnectionTimeout);
    }
    
    /**
     * Only for Unit tests
     * @param credentials
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     * @throws DBMissingDataException
     */
    protected static synchronized JControllerProxy of(ProxyCredentials credentials) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException {
        return Proxies.getInstance().of(credentials, Globals.httpConnectionTimeout);
    }
    
    /**
     * Returns Proxy for user 'JOC' and starts it if necessary
     * @see of(String jobschedulerId, ProxyUser user, long connectionTimeout)
     * @param jobschedulerId
     * @param connectionTimeout (in milliseconds)
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerProxy of(String jobschedulerId, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(jobschedulerId, ProxyUser.JOC, connectionTimeout);
    }
    
    /**
     * Returns Proxy for specified user and starts it if necessary
     * @param jobschedulerId
     * @param user
     * @param connectionTimeout (in milliseconds)
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException
     * @throws ExecutionException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerProxy of(String jobschedulerId, ProxyUser user, long connectionTimeout)
            throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(jobschedulerId, user, connectionTimeout);
    }
    
    /**
     * Closes Proxy for user 'JOC'
     * @see close(String jobschedulerId, ProxyUser user)
     * @param jobschedulerId
     * @return CompletableFuture&lt;Void&gt; 
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized CompletableFuture<Void> close(String jobschedulerId) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().close(jobschedulerId, ProxyUser.JOC);
    }
    
    /**
     * Closes Proxy for specified user
     * @param jobschedulerId
     * @param user
     * @return CompletableFuture&lt;Void&gt;
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized CompletableFuture<Void> close(String jobschedulerId, ProxyUser user) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().close(jobschedulerId, user);
    }

}
