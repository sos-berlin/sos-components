package com.sos.joc.classes.proxy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;

import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.cluster.JClusterState;


public class Proxy {

    private Proxy() {
        //
    }
    
    /**
     * Starts Proxy for user 'JOC'
     * @see start(String jobschedulerId, ProxyUser user)
     * @param controllerId
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException 
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized ProxyContext start(String controllerId) throws JobSchedulerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().start(controllerId, ProxyUser.JOC);
    }
    
    /**
     * Starts Proxy for specified user
     * @param controllerId
     * @param user
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized ProxyContext start(String controllerId, ProxyUser user) throws JobSchedulerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().start(controllerId, user);
    }
    
    /**
     * Returns Proxy for user 'JOC' and starts it if necessary
     * @see of(String jobschedulerId, ProxyUser user)
     * @param controllerId
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
    public static synchronized JControllerProxy of(String controllerId) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(controllerId, ProxyUser.JOC, Globals.httpConnectionTimeout);
    }

    /**
     * Returns Proxy for specified user and starts it if necessary
     * @param controllerId
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
    public static synchronized JControllerProxy of(String controllerId, ProxyUser user) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(controllerId, user, Globals.httpConnectionTimeout);
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
     * @param controllerId
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
    public static synchronized JControllerProxy of(String controllerId, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(controllerId, ProxyUser.JOC, connectionTimeout);
    }
    
    /**
     * Returns Proxy for specified user and starts it if necessary
     * @param controllerId
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
    public static synchronized JControllerProxy of(String controllerId, ProxyUser user, long connectionTimeout)
            throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().of(controllerId, user, connectionTimeout);
    }
    
    /**
     * Closes Proxy for user 'JOC'
     * @see close(String jobschedulerId, ProxyUser user)
     * @param controllerId
     * @return CompletableFuture&lt;Void&gt; 
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized CompletableFuture<Void> close(String controllerId) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().close(controllerId, ProxyUser.JOC);
    }
    
    /**
     * Closes Proxy for specified user
     * @param controllerId
     * @param user
     * @return CompletableFuture&lt;Void&gt;
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized CompletableFuture<Void> close(String controllerId, ProxyUser user) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        return Proxies.getInstance().close(controllerId, user);
    }

}
