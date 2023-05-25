package com.sos.joc.classes.inventory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class RevalidateCallable implements Callable<RevalidateCallable> {
    
    private final DBItemInventoryConfiguration dbItem;
    private final Set<String> agentNames;
    private final IConfigurationObject conf;
    private final Map<ConfigurationType, Set<String>> inventoryObjectNamesByType;
    private final Map<String, String> workflowJsonsByName;
    private Throwable throwable = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(RevalidateCallable.class);
    
    public RevalidateCallable(IConfigurationObject conf, DBItemInventoryConfiguration dbItem,
            Map<ConfigurationType, Set<String>> inventoryObjectNamesByType, Map<String, String> workflowJsonsByName, Set<String> agentNames) {
        this.conf = conf;
        this.dbItem = dbItem;
        this.inventoryObjectNamesByType = inventoryObjectNamesByType;
        this.workflowJsonsByName = workflowJsonsByName;
        this.agentNames = agentNames;
    }

    @Override
    public RevalidateCallable call() {

        LOGGER.debug("revalidate " + dbItem.getTypeAsEnum() + ": " + dbItem.getPath());
        try {
            Validator.revalidate(dbItem.getTypeAsEnum(), dbItem.getContent().getBytes(StandardCharsets.UTF_8), conf, inventoryObjectNamesByType,
                    workflowJsonsByName, agentNames);
        } catch (Throwable e) {
            throwable = e;
            //dbItem.setValidationError(e);
        }
        return this;
    }
    
    public DBItemInventoryConfiguration getDbItem() {
        return dbItem;
    }
    
    public IConfigurationObject getConf() {
        return conf;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
    public boolean hasThrowable() {
        return throwable != null;
    }

}
