package com.sos.joc.classes.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.deploy.items.DeployedWorkflowWithBoards;
import com.sos.joc.db.deploy.items.WorkflowBoards;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.deploy.DeployHistoryFileOrdersSourceEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowEvent;


public class WorkflowRefs {

    private static WorkflowRefs instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRefs.class);
    private volatile ConcurrentMap<String, List<DeployedContent>> fileOrderSources = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<String, Set<String>> workflowNamesWithAddOrders = new ConcurrentHashMap<>();
    // controllerId, workflowName, addOrderInstruction-Index, orderTags
    private volatile ConcurrentMap<String, Map<String, String>> addOrderTags = new ConcurrentHashMap<>();
    // controllerId, workflowName, position of Consume/ExpectNotices-Instruction, boardNames
    private volatile ConcurrentMap<String, Map<String, WorkflowBoards>> workflowNamesWithBoards = new ConcurrentHashMap<>();

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
    public void updateWorkflowNamesWithAddOrdersOrBoards(DeployHistoryWorkflowEvent evt) {
        if (evt.getControllerId() != null) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(WorkflowRefs.class.getSimpleName());
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
                updateWorkflowNamesWithAddOrders(evt.getControllerId(), dbLayer);
                updateAddOrderTags(evt.getControllerId(), dbLayer);
                updateWorkflowNamesWithBoards(evt.getControllerId(), dbLayer);
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
    
    // only for test
    public static void _updateAddOrderTags(String controllerId, DeployedConfigurationDBLayer dbLayer) {
        WorkflowRefs.getInstance().updateAddOrderTags(controllerId, dbLayer);
    }
    
    private void updateAddOrderTags(String controllerId, DeployedConfigurationDBLayer dbLayer) {
        addOrderTags.put(controllerId, dbLayer.getAddOrderTags(controllerId));
    }
    
    private void updateWorkflowNamesWithBoards(String controllerId, DeployedConfigurationDBLayer dbLayer) {
        workflowNamesWithBoards.put(controllerId, dbLayer.getWorkflowsWithBoards(controllerId).stream().map(
                DeployedWorkflowWithBoards::mapToWorkflowBoardsWithPositions).filter(Objects::nonNull).collect(Collectors.toMap(w -> JocInventory
                        .pathToName(w.getPath()), Function.identity(), (w1, w2) -> w2)));
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
                updateAddOrderTags(controllerId, dbLayer);
                updateFileOrderSources(controllerId, dbLayer);
                updateWorkflowNamesWithBoards(controllerId, dbLayer);
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
    
    public static Map<String, String> getAddOrderTags(String controllerId) {
        return WorkflowRefs.getInstance().addOrderTags.getOrDefault(controllerId, Collections.emptyMap());
    }

    public static String getAddOrderTags(String controllerId, String workflowName) {
        return getAddOrderTags(controllerId).get(workflowName);
    }
    
    public static Map<String, WorkflowBoards> getWorkflowNamesWithBoards(String controllerId) {
        return WorkflowRefs.getInstance().workflowNamesWithBoards.getOrDefault(controllerId, Collections.emptyMap());
    }
    
    public static WorkflowBoards getWorkflowNamesWithBoards(String controllerId, String workflowName) {
        return getWorkflowNamesWithBoards(controllerId).get(workflowName);
    }

}
