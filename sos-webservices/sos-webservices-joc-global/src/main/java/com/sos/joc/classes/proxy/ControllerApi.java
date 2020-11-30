package com.sos.joc.classes.proxy;

import java.util.concurrent.ExecutionException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;

import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;


public class ControllerApi {

    private ControllerApi() {
        //
    }
    
    
    /**
     * Returns ControllerApi for user 'JOC'
     * @see of(String controllerId, ProxyUser user)
     * @param controllerId
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String controllerId) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().loadApi(controllerId, ProxyUser.JOC, Globals.httpConnectionTimeout);
    }

    /**
     * Returns ControllerApi for specified user
     * @param controllerId
     * @param user
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String controllerId, ProxyUser user) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().loadApi(controllerId, user, Globals.httpConnectionTimeout);
    }
    
    /**
     * Returns ControllerApi for user 'JOC'
     * @see of(String controllerId, ProxyUser user, long connectionTimeout)
     * @param controllerId
     * @param connectionTimeout (in milliseconds)
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String controllerId, long connectionTimeout) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().loadApi(controllerId, ProxyUser.JOC, connectionTimeout);
    }
    
    /**
     * Returns ControllerApi for specified user
     * @param controllerId
     * @param user
     * @param connectionTimeout (in milliseconds)
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String controllerId, ProxyUser user, long connectionTimeout) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().loadApi(controllerId, user, connectionTimeout);
    }
    
    // only for testing
    protected static synchronized JControllerApi of(ProxyCredentials credentials) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, ExecutionException, DBMissingDataException {
        return Proxies.getInstance().loadApi(credentials);
    }

}
