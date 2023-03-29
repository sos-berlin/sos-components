package com.sos.joc.classes.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.deploy.DeployHistoryFileOrdersSourceEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowEvent;
//import com.sos.joc.model.publish.OperationType;

public class WorkflowRefs {

    private static WorkflowRefs instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRefs.class);
    private volatile ConcurrentMap<String, List<DeployedContent>> fileOrderSources = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<String, Set<String>> workflowNamesWithAddOrders = new ConcurrentHashMap<>();

    private WorkflowRefs() {
        EventBus.getInstance().register(this);
    }

    protected static WorkflowRefs getInstance() {
        if (instance == null) {
            instance = new WorkflowRefs();
        }
        return instance;
    }

    @Subscribe({ DeployHistoryWorkflowEvent.class })
    public void updateWorkflowNamesWithAddOrders(DeployHistoryWorkflowEvent evt) {
        if (evt.getControllerId() != null) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(WorkflowRefs.class.getSimpleName());
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
                updateWorkflowNamesWithAddOrders(evt.getControllerId(), dbLayer);
            } catch (Exception e) {
                LOGGER.error("", e);
            } finally {
                Globals.disconnect(session);
            }
        }
    }
    
    @Subscribe({ DeployHistoryFileOrdersSourceEvent.class })
    public void updateFileOrderSources(DeployHistoryFileOrdersSourceEvent evt) {
        if (evt.getControllerId() != null) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(WorkflowRefs.class.getSimpleName());
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
                updateFileOrderSources(evt.getControllerId(), dbLayer);
            } catch (Exception e) {
                LOGGER.error("", e);
            } finally {
                Globals.disconnect(session);
            }
        }
    }
    
    private void updateWorkflowNamesWithAddOrders(String controllerId, DeployedConfigurationDBLayer dbLayer) {
        workflowNamesWithAddOrders.put(controllerId, dbLayer.getAddOrderWorkflows(controllerId));
    }
    
    private void updateFileOrderSources(String controllerId, DeployedConfigurationDBLayer dbLayer) {
        DeployedConfigurationFilter filter = new DeployedConfigurationFilter();
        filter.setControllerId(controllerId);
        filter.setObjectTypes(Collections.singleton(DeployType.FILEORDERSOURCE.intValue()));
        fileOrderSources.put(controllerId, dbLayer.getDeployedInventory(filter));
    }
    
    public static void init() {
        WorkflowRefs.getInstance()._init();
    }

    // init for servlet call (after proxies)
    private void _init() {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(WorkflowRefs.class.getSimpleName());
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            Proxies.getControllerDbInstances().keySet().forEach(controllerId -> {
                updateWorkflowNamesWithAddOrders(controllerId, dbLayer);
                updateFileOrderSources(controllerId, dbLayer);
            });
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static List<DeployedContent> getFileOrderSources(String controllerId) {
        return WorkflowRefs.getInstance().fileOrderSources.getOrDefault(controllerId, Collections.emptyList());
    }
    
    public static Set<String> getWorkflowNamesWithAddOrders(String controllerId) {
        return WorkflowRefs.getInstance().workflowNamesWithAddOrders.getOrDefault(controllerId, Collections.emptySet());
    }

}
