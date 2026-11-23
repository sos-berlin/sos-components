package com.sos.joc.classes.filetransfer.updaters.fragments;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;

public abstract class AFragmentUpdater<F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AFragmentUpdater.class);

    protected abstract String getLogPrefix();

    protected abstract String getFragmentLogName();

    protected abstract F readFragment(DBItemInventoryConfiguration c) throws Exception;

    protected abstract String getAlternativeName(F f);

    protected abstract void setAlternativeName(F f, String newName);

    protected abstract List<DBItemInventoryConfiguration> getAlternativeFragments(InventoryDBLayer dbLayer, String name) throws SOSHibernateException;

    public void update(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, String newName, List<DBItemInventoryConfiguration> items,
            Set<String> events) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        
    }

    private void applyChange(InventoryDBLayer dbLayer, DBItemInventoryConfiguration c, List<DBItemInventoryConfiguration> items, Set<String> events)
            throws Exception {
        int i = items.indexOf(c);
        if (i != -1) {
            items.get(i).setContent(c.getContent());
            items.get(i).setReleased(false);
        } else {
            JocInventory.updateConfiguration(dbLayer, c);
            events.add(c.getFolder());
        }
    }
}
