package com.sos.joc.classes.proxy;

import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;


public class ProxiesEdit {

    private ProxiesEdit() {
        //
    }
    
    /**
     * 
     * @param jobschedulerId
     */
    public static synchronized void remove(String jobschedulerId) {
        Proxies.getInstance().removeProxies(jobschedulerId);
    }
    
    /**
     * Restart Proxies with new credentials from database instances
     * @param controllerDbInstances
     * @param connection
     * @throws DBMissingDataException
     * @throws JobSchedulerConnectionRefusedException
     */
    public static synchronized void update(List<DBItemInventoryJSInstance> controllerDbInstances, SOSHibernateSession connection)
            throws DBMissingDataException, JobSchedulerConnectionRefusedException {
        Proxies.getInstance().updateProxies(controllerDbInstances, connection);
    }

}
