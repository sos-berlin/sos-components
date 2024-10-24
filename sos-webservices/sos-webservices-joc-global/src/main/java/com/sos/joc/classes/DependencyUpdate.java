package com.sos.joc.classes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;

public class DependencyUpdate {

    private static DependencyUpdate updater;
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyUpdate.class);
    private Thread updateThread;
    
    private DependencyUpdate () {
        
    }
    
    public static synchronized DependencyUpdate getInstance() {
        if (updater == null) {
            updater = new DependencyUpdate();
        }
        return updater;
    }
    
    public void updateThreaded () {
        updateThread = new Thread(() -> update(), DependencyUpdate.class.getSimpleName()); 
        updateThread.start();
    }
    
    public static void update() {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(DependencyUpdate.class.getSimpleName());
            InventoryDBLayer dblayer = new InventoryDBLayer(session);
            DBLayerDependencies depDblayer = new DBLayerDependencies(session);
            if(!depDblayer.checkDependenciesPresent()) {
                List<DBItemInventoryConfiguration> allConfigs = dblayer.getConfigurationsByType(DependencyResolver.dependencyTypes);
                DependencyResolver.updateDependencies(session, allConfigs);
            }
        } catch (InterruptedException e) {
            // do nothing
        } catch (Throwable e) {
            LOGGER.warn("error ocurred, dependencies not processed.");
        } finally {
            Globals.disconnect(session);
        }
    }

    public void close() {
        try {
            if(updateThread != null) {
                if(updateThread.isAlive()) {
                    updateThread.interrupt();
                }
                updateThread = null;
            }
        } catch (Throwable e) {}
    }
}
