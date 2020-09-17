package com.sos.joc.classes.proxy;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;

import js7.proxy.javaapi.JControllerApi;


public class ControllerApi {

    private ControllerApi() {
        //
    }
    
    
    /**
     * Returns ControllerApi for user 'JOC'
     * @see of(String jobschedulerId, ProxyUser user)
     * @param jobschedulerId
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String jobschedulerId) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().ofApi(jobschedulerId, ProxyUser.JOC, Globals.httpConnectionTimeout);
    }

    /**
     * Returns ControllerApi for specified user
     * @param jobschedulerId
     * @param user
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String jobschedulerId, ProxyUser user) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().ofApi(jobschedulerId, user, Globals.httpConnectionTimeout);
    }
    
    /**
     * Returns ControllerApi for user 'JOC'
     * @see of(String jobschedulerId, ProxyUser user, long connectionTimeout)
     * @param jobschedulerId
     * @param connectionTimeout (in milliseconds)
     * @return JControllerApi
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    public static synchronized JControllerApi of(String jobschedulerId, long connectionTimeout) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().ofApi(jobschedulerId, ProxyUser.JOC, connectionTimeout);
    }
    
    /**
     * Returns ControllerApi for specified user
     * @param jobschedulerId
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
    public static synchronized JControllerApi of(String jobschedulerId, ProxyUser user, long connectionTimeout) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            JobSchedulerConnectionRefusedException {
        return Proxies.getInstance().ofApi(jobschedulerId, user, connectionTimeout);
    }

}
