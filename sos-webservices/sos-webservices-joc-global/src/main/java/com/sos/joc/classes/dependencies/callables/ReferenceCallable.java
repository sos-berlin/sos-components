package com.sos.joc.classes.dependencies.callables;

import java.util.concurrent.Callable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;

public class ReferenceCallable implements Callable<ReferencedDbItem>{

    private final DBItemInventoryConfiguration inventoryItem;
    
    public ReferenceCallable(DBItemInventoryConfiguration inventoryItem) {
        this.inventoryItem = inventoryItem;
    }
    
    @Override
    public ReferencedDbItem call() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("identifyDependencies");
            ReferencedDbItem item = DependencyResolver.resolveReferencedBy(session, inventoryItem);
            DependencyResolver.resolveReferences(item, session);
            return item;
        } finally {
            Globals.disconnect(session);
        }
    }

}
