package com.sos.joc.classes.dependencies.callables;

import java.util.Map;
import java.util.concurrent.Callable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class ReferenceCallable implements Callable<ReferencedDbItem>{

    private final DBItemInventoryConfiguration inventoryItem;
    private final Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems;
    
    public ReferenceCallable(DBItemInventoryConfiguration inventoryItem, Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems) {
        this.inventoryItem = inventoryItem;
        this.groupedItems = groupedItems;
    }
    
    @Override
    public ReferencedDbItem call() throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.sosHibernateFactory.openStatelessSession();
            ReferencedDbItem item = DependencyResolver.resolveReferencedBy(session, inventoryItem);
            DependencyResolver.resolveReferences(item, session);
            return item;
        } finally {
            Globals.disconnect(session);
        }
    }

}
