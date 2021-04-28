package com.sos.joc.classes.workflow;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventoryNamePath;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowEvent;
import com.sos.joc.model.publish.OperationType;

public class WorkflowPaths {
    
    private static WorkflowPaths instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowPaths.class);
    private volatile ConcurrentMap<WorkflowId, String> workflowIdPathMap = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<String, String> namePathMap = new ConcurrentHashMap<>();
    
    private WorkflowPaths() {
        EventBus.getInstance().register(this);
    }

    protected static WorkflowPaths getInstance() {
        if (instance == null) {
            instance = new WorkflowPaths();
        }
        return instance;
    }
    
    @Subscribe({ DeployHistoryWorkflowEvent.class })
    public void updateMap(DeployHistoryWorkflowEvent evt) {
        //LOGGER.debug(String.format("add Workflow from event: %s/%s/%s", evt.getName(), evt.getCommitId(), evt.getPath()));
        namePathMap.put(evt.getName(), evt.getPath());
        workflowIdPathMap.put(new WorkflowId(evt.getName(), evt.getCommitId()), evt.getPath());
    }
    
    public static ConcurrentMap<WorkflowId, String> getWorkflowPathMap() {
        return WorkflowPaths.getInstance()._getWorkflowPathMap();
    }
    
    public static ConcurrentMap<String, String> getNamePathMap() {
        return WorkflowPaths.getInstance()._getNamePathMap();
    }
    
    public static String getPath(WorkflowId workflowId) {
        return WorkflowPaths.getInstance()._getPath(workflowId);
    }
    
    public static String getPath(String name) {
        return WorkflowPaths.getInstance()._getPath(name);
    }
    
    public static WorkflowId getWorkflowId(WorkflowId workflowId) {
        return WorkflowPaths.getInstance()._getWorkflowId(workflowId);
    }
    
    public static void init() {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(WorkflowPaths.class.getSimpleName());
            WorkflowPaths.getInstance()._init(connection);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private ConcurrentMap<WorkflowId, String> _getWorkflowPathMap() {
        return workflowIdPathMap;
    }
    
    private ConcurrentMap<String, String> _getNamePathMap() {
        return namePathMap;
    }
    
    private String _getPath(WorkflowId workflowId) {
        return workflowIdPathMap.getOrDefault(workflowId, getDBWorkflowId(workflowId));
    }
    
    private String _getPath(String name) {
        return namePathMap.getOrDefault(name, getDBPath(name));
    }
    
    private WorkflowId _getWorkflowId(WorkflowId workflowId) {
        return new WorkflowId(workflowIdPathMap.getOrDefault(workflowId, workflowId.getPath()), workflowId.getVersionId());
    }
    
    private void _init(SOSHibernateSession connection) {
        _initNamePath(connection);
        _initWorkflowIdPath(connection);
    }
    
    private void _initWorkflowIdPath(SOSHibernateSession connection) {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(InventoryNamePath.class.getName());
            hql.append("(name, commitId, path) from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where type=:type and operation=:operation and state=0");
            Query<InventoryNamePath> query = connection.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("operation", OperationType.UPDATE.value());
            List<InventoryNamePath> result = connection.getResultList(query);
            if (result != null) {
                workflowIdPathMap = result.stream().distinct().collect(Collectors.toConcurrentMap(InventoryNamePath::getWorkflowId,
                        InventoryNamePath::getPath));
            }
        } catch (SOSHibernateException e) {
            LOGGER.warn(e.toString());
        }
    }

    private void _initNamePath(SOSHibernateSession connection) {
        try {
            StringBuilder hql = new StringBuilder("select new ").append(InventoryNamePath.class.getName());
            hql.append("(name, path) from ").append(DBLayer.DBITEM_DEP_NAMEPATHS);
            hql.append(" where type=:type");
            Query<InventoryNamePath> query = connection.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            List<InventoryNamePath> result = connection.getResultList(query);
            if (result != null) {
                namePathMap = result.stream().distinct().collect(Collectors.toConcurrentMap(InventoryNamePath::getName, InventoryNamePath::getPath));
            }
        } catch (SOSHibernateException e) {
            LOGGER.warn(e.toString());
        }
    }
    
    private String getDBPath(String name) {
        if (name == null || name.isEmpty() || name.startsWith("/")) {
            return name;
        }
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(WorkflowPaths.class.getSimpleName());
            StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_DEP_NAMEPATHS);
            hql.append(" where type=:type and name=:name");
            Query<String> query = connection.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("name", name);
            query.setMaxResults(1);
            String result = connection.getSingleResult(query);
            if (result != null) {
                namePathMap.put(name, result);
                return result;
            }
        } catch (SOSHibernateException e) {
            LOGGER.warn(e.toString());
        } finally {
            Globals.disconnect(connection);
        }
        return name;
    }
    
    private String getDBWorkflowId(WorkflowId workflowId) {
        String name = workflowId.getPath();
        if (workflowId == null || name == null || name.isEmpty() || name.startsWith("/")) {
            return name;
        }
        if (workflowId.getVersionId() == null || workflowId.getVersionId().isEmpty()) {
            return getDBPath(name);
        }
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(WorkflowPaths.class.getSimpleName());
            StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where type=:type and operation=:operation and state=0 and name=:name and commitId=:commitId");
            Query<String> query = connection.createQuery(hql.toString());
            query.setParameter("type", DeployType.WORKFLOW.intValue());
            query.setParameter("operation", OperationType.UPDATE.value());
            query.setParameter("name", name);
            query.setParameter("commitId", workflowId.getVersionId());
            query.setMaxResults(1);
            String result = connection.getSingleResult(query);
            if (result != null) {
                namePathMap.put(name, result);
                workflowIdPathMap.put(workflowId, result);
                return result;
            }
        } catch (SOSHibernateException e) {
            LOGGER.warn(e.toString());
        } finally {
            Globals.disconnect(connection);
        }
        return name;
    }
    
}
