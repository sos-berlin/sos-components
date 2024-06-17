package com.sos.joc.classes.proxy;

import java.util.List;

import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;


public class ProxiesEdit {

    private ProxiesEdit() {
        //
    }
    
    /**
     * 
     * @param controllerId
     */
    public static synchronized void remove(String controllerId) {
        Proxies.getInstance().removeProxies(controllerId);
    }
    
    /**
     * Restart Proxies with new credentials from database instances
     * @param controllerDbInstances
     * @param connection
     * @throws DBMissingDataException
     * @throws ControllerConnectionRefusedException
     */
    public static synchronized void update(List<DBItemInventoryJSInstance> controllerDbInstances) throws DBMissingDataException,
            ControllerConnectionRefusedException {
        Proxies.getInstance().updateProxies(controllerDbInstances);
    }

    public static synchronized void update(String controllerId) throws DBMissingDataException, ControllerConnectionRefusedException {
        Proxies.getInstance().updateProxies(controllerId);
    }
    
    public static synchronized void restart(List<DBItemInventoryJSInstance> controllerDbInstances, ProxyUser account, boolean force)
            throws DBMissingDataException, ControllerConnectionRefusedException {
        Proxies.getInstance().restartProxies(controllerDbInstances, account, force);
    }
    
    public static synchronized void forcedRestartForJOC(List<DBItemInventoryJSInstance> controllerDbInstances)
            throws DBMissingDataException, ControllerConnectionRefusedException {
        Proxies.getInstance().restartProxies(controllerDbInstances, ProxyUser.JOC, true);
    }
    
    public static synchronized void forcedRestartForJOC() throws DBMissingDataException, ControllerConnectionRefusedException {
        Proxies.getControllerDbInstances().values().forEach(ProxiesEdit::forcedRestartForJOC);
    }

}
