package com.sos.joc.db.inventory.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.items.SubagentCluster;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.agent.SubAgentId;

public class InventorySubagentClustersDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
    private boolean withAgentOrdering = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorySubagentClustersDBLayer.class);

    public InventorySubagentClustersDBLayer(SOSHibernateSession conn) {
        super(conn);
    }
    
    public void setWithSubAgentClusterOrdering(boolean withAgentOrdering) {
        this.withAgentOrdering = withAgentOrdering; 
    }

    public List<DBItemInventorySubAgentCluster> getSubagentClusters(String controllerId, List<String> subagentClusterIds)
            throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventorySubAgentCluster> r = new ArrayList<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getSubagentClusters(controllerId, SOSHibernate.getInClausePartition(i, subagentClusterIds)));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
                List<String> clauses = new ArrayList<>(2);
                if (controllerId != null && !controllerId.isEmpty()) {
                    clauses.add("controllerId = :controllerId");
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        clauses.add("subAgentClusterId = :subagentClusterId");
                    } else {
                        clauses.add("subAgentClusterId in (:subagentClusterIds)");
                    }
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                if (withAgentOrdering) {
                    hql.append(" order by ordering");
                }
                Query<DBItemInventorySubAgentCluster> query = getSession().createQuery(hql.toString());
                if (controllerId != null && !controllerId.isEmpty()) {
                    query.setParameter("controllerId", controllerId);
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    } else {
                        query.setParameterList("subagentClusterIds", subagentClusterIds);
                    }
                }
                List<DBItemInventorySubAgentCluster> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public List<DBItemInventorySubAgentCluster> getSubagentClusters(Collection<String> controllerIds, List<String> subagentClusterIds)
            throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        if (controllerIds == null || controllerIds.isEmpty()) {
            return getSubagentClusters((String) null, subagentClusterIds);
        }

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventorySubAgentCluster> r = new ArrayList<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getSubagentClusters(controllerIds, SOSHibernate.getInClausePartition(i, subagentClusterIds)));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("select sac from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS).append(" sac, ");
                hql.append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(" ai ");
                hql.append("where ai.agentId = sac.agentId ");
                if (controllerIds.size() == 1) {
                    hql.append("and ai.controllerId = :controllerId ");
                } else {
                    hql.append("and ai.controllerId in (:controllerIds) ");
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        hql.append(" and sac.subAgentClusterId = :subagentClusterId");
                    } else {
                        hql.append(" and sac.subAgentClusterId in (:subagentClusterIds)");
                    }
                }
                Query<DBItemInventorySubAgentCluster> query = getSession().createQuery(hql.toString());
                if (controllerIds.size() == 1) {
                    query.setParameter("controllerId", controllerIds.iterator().next());
                } else {
                    query.setParameterList("controllerIds", controllerIds);
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    } else {
                        query.setParameterList("subagentClusterIds", subagentClusterIds);
                    }
                }
                List<DBItemInventorySubAgentCluster> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public Map<DBItemInventorySubAgentCluster, List<SubAgentId>> getSubagentClusters(Collection<String> controllerIds, List<String> agentIds,
            List<String> subagentClusterIds) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {

        if (agentIds != null && agentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> r = new HashMap<>();
            for (int i = 0; i < agentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.putAll(getSubagentClusters(controllerIds, SOSHibernate.getInClausePartition(i, agentIds), subagentClusterIds));
            }
            return r;
        } else {
            return getSubagentClusters2(controllerIds, agentIds, subagentClusterIds);
        }
    }

    private Map<DBItemInventorySubAgentCluster, List<SubAgentId>> getSubagentClusters2(Collection<String> controllerIds, List<String> agentIds,
            List<String> subagentClusterIds) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> r = new HashMap<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.putAll(getSubagentClusters(controllerIds, agentIds, SOSHibernate.getInClausePartition(i, subagentClusterIds)));
            }
            return r;
        } else {
            try {
                List<String> clauses = new ArrayList<>(3);
                StringBuilder hql = new StringBuilder();
                hql.append("select new ").append(SubagentCluster.class.getName()).append("(sac, sacm.subAgentId, sacm.priority) from ");
                hql.append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS).append(" sac ");
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    hql.append("left join ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS).append(" sacm ");
                    hql.append("on sac.subAgentClusterId = sacm.subAgentClusterId ");
                    hql.append("left join ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(" ai ");
                    hql.append("on ai.agentId = sac.agentId ");
                    if (controllerIds.size() == 1) {
                        clauses.add("ai.controllerId = :controllerId");
                    } else {
                        clauses.add("ai.controllerId in (:controllerIds)");
                    }
                } else {
                    hql.append("left join ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS).append(" sacm ");
                    hql.append("on sac.subAgentClusterId = sacm.subAgentClusterId ");
                }
                
                if (agentIds != null && !agentIds.isEmpty()) {
                    if (agentIds.size() == 1) {
                        clauses.add("sac.agentId = :agentId");
                    } else {
                        clauses.add("sac.agentId in (:agentIds)");
                    }
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        clauses.add("sac.subAgentClusterId = :subagentClusterId");
                    } else {
                        clauses.add("sac.subAgentClusterId in (:subagentClusterIds)");
                    }
                }
                if (clauses.size() > 0) {
                    hql.append("where ").append(String.join(" and ", clauses)).append(" ");
                }
                Query<SubagentCluster> query = getSession().createQuery(hql.toString());
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    if (controllerIds.size() == 1) {
                        query.setParameter("controllerId", controllerIds.iterator().next());
                    } else {
                        query.setParameterList("controllerIds", controllerIds);
                    }
                }
                if (agentIds != null && !agentIds.isEmpty()) {
                    if (agentIds.size() == 1) {
                        query.setParameter("agentId", agentIds.get(0));
                    } else {
                        query.setParameterList("agentIds", agentIds);
                    }
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    } else {
                        query.setParameterList("subagentClusterIds", subagentClusterIds);
                    }
                }
                List<SubagentCluster> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyMap();
                }
                return result.stream().collect(Collectors.groupingBy(SubagentCluster::getDBItemInventorySubAgentCluster, Collectors.mapping(
                        SubagentCluster::getDBItemInventorySubAgentClusterMember, Collectors.filtering(Objects::nonNull, Collectors.toList()))));
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public List<DBItemInventorySubAgentClusterMember> getSubagentClusterMembers(List<String> subagentClusterIds, String controllerId)
            throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventorySubAgentClusterMember> r = new ArrayList<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getSubagentClusterMembers(SOSHibernate.getInClausePartition(i, subagentClusterIds), controllerId));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS);
                List<String> clauses = new ArrayList<>(2);
                if (controllerId != null && !controllerId.isEmpty()) {
                    clauses.add("controllerId = :controllerId");
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        clauses.add("subAgentClusterId = :subagentClusterId");
                    } else {
                        clauses.add("subAgentClusterId in (:subagentClusterIds)");
                    }
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
                Query<DBItemInventorySubAgentClusterMember> query = getSession().createQuery(hql.toString());
                if (controllerId != null && !controllerId.isEmpty()) {
                    query.setParameter("controllerId", controllerId);
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    } else {
                        query.setParameterList("subagentClusterIds", subagentClusterIds);
                    }
                }
                List<DBItemInventorySubAgentClusterMember> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public String getFirstSubagentIdThatNotExists(List<String> subagentIds) throws DBInvalidDataException, DBMissingDataException,
            DBConnectionRefusedException {

        if (subagentIds == null || subagentIds.isEmpty()) {
            return "";
        }
        if (subagentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            String r = "";
            for (int i = 0; i < subagentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r = getFirstSubagentIdThatNotExists(SOSHibernate.getInClausePartition(i, subagentIds));
                if (!r.isEmpty()) {
                    break;
                }
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("select subAgentId from ").append(DBLayer.DBITEM_INV_SUBAGENT_INSTANCES);
                if (subagentIds.size() == 1) {
                    hql.append(" where subAgentId = :subagentId");
                } else {
                    hql.append(" where subAgentId in (:subagentIds)");
                }
                Query<String> query = getSession().createQuery(hql.toString());
                if (subagentIds.size() == 1) {
                    query.setParameter("subagentId", subagentIds.get(0));
                } else {
                    query.setParameterList("subagentIds", subagentIds);
                }
                List<String> result = getSession().getResultList(query);
                if (result == null) {
                    return subagentIds.get(0);
                }
                subagentIds.removeAll(result);
                if (subagentIds.isEmpty()) {
                    return "";
                }
                return subagentIds.get(0);
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public int deleteSubAgentClusters(List<String> subagentClusterIds) throws SOSHibernateException {
        if (subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            int j = 0;
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                j += deleteSubAgentClusters(SOSHibernate.getInClausePartition(i, subagentClusterIds));
            }
            return j;
        } else {
            StringBuilder hql = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
            StringBuilder hql2 = new StringBuilder("delete from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS);
            if (subagentClusterIds.size() == 1) {
                hql.append(" where subAgentClusterId = :subagentClusterId");
                hql2.append(" where subAgentClusterId = :subagentClusterId");
            } else {
                hql.append(" where subAgentClusterId in (:subagentClusterIds)");
                hql2.append(" where subAgentClusterId in (:subagentClusterIds)");
            }
            Query<?> query = getSession().createQuery(hql.toString());
            Query<?> query2 = getSession().createQuery(hql2.toString());
            if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                if (subagentClusterIds.size() == 1) {
                    query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    query2.setParameter("subagentClusterId", subagentClusterIds.get(0));
                } else {
                    query.setParameterList("subagentClusterIds", subagentClusterIds);
                    query2.setParameterList("subagentClusterIds", subagentClusterIds);
                }
            }
            return getSession().executeUpdate(query2) + getSession().executeUpdate(query);
        }
    }
    
    public List<String> getControllerIds(List<String> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (agentIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<String> r = new ArrayList<>();
            for (int i = 0; i < agentIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getControllerIds(SOSHibernate.getInClausePartition(i, agentIds)));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder("select controllerId from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
                if (agentIds.size() == 1) {
                    hql.append(" where agentId = :agentId");
                } else {
                    hql.append(" where agentId in (:agentIds)");
                }
                Query<String> query = getSession().createQuery(hql.toString());
                if (agentIds.size() == 1) {
                    query.setParameter("agentId", agentIds.get(0));
                } else {
                    query.setParameterList("agentIds", agentIds);
                }
                List<String> result = getSession().getResultList(query);
                if (result == null) {
                    return Collections.emptyList();
                }
                return result;
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    private DBItemInventorySubAgentCluster getSubAgentCluster(String controllerId, String subagentClusterId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
        hql.append(" where controllerId = :controllerId");
        hql.append(" and subAgentClusterId = :subagentClusterId");
        Query<DBItemInventorySubAgentCluster> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("subagentClusterId", subagentClusterId);
        return getSession().getSingleResult(query); //TODO
    }
    
    public void cleanupSubAgentClusterOrdering(boolean force) throws SOSHibernateException {
        setWithSubAgentClusterOrdering(true);
        List<DBItemInventorySubAgentCluster> dbSubagentClusters = getSubagentClusters((String) null, null);
        if (!force) {
            // looking for duplicate orderings
            force = dbSubagentClusters.stream().collect(Collectors.groupingBy(DBItemInventorySubAgentCluster::getOrdering, Collectors.counting()))
                    .entrySet().stream().anyMatch(e -> e.getValue() > 1L);
        }
        if (force) {
            int position = 0;
            for (DBItemInventorySubAgentCluster dbSubagentCluster : dbSubagentClusters) {
                if (dbSubagentCluster.getOrdering() != position) {
                    dbSubagentCluster.setOrdering(position);
                    getSession().update(dbSubagentCluster);
                }
                position++;
            }
        }
    }
    
    public void setSubAgentClusterOrdering(String controllerId, String subagentClusterId, String predecessorSubagentClusterId)
            throws SOSHibernateException, DBMissingDataException, DBInvalidDataException {
        // TODO better with collect by prior
        DBItemInventorySubAgentCluster subagentCluster = getSubAgentCluster(controllerId, subagentClusterId);
        if (subagentCluster == null) {
            throw new DBMissingDataException("SubagentCluster with ID '" + subagentClusterId + "' doesn't exist.");
        }
        int newPosition = -1;
        DBItemInventorySubAgentCluster predecessorSubagentCluster = null;
        if (predecessorSubagentClusterId != null && !predecessorSubagentClusterId.isEmpty()) {
            if (subagentClusterId.equals(predecessorSubagentClusterId)) {
                throw new DBInvalidDataException("SubagentCluster ID '" + subagentClusterId + "' and predecessor SubagentCluster ID '"
                        + predecessorSubagentClusterId + "' are the same.");
            }
            predecessorSubagentCluster = getSubAgentCluster(controllerId, predecessorSubagentClusterId);
            if (predecessorSubagentCluster == null) {
                throw new DBMissingDataException("Predecessor SubagentCluster with ID '" + predecessorSubagentClusterId + "' doesn't exist.");
            }
            newPosition = predecessorSubagentCluster.getOrdering();
        }
        
        // TODO check: subagentClusterId and predecessorSubagentClusterId should have same agentId

        int oldPosition = subagentCluster.getOrdering();
        newPosition++;
        subagentCluster.setOrdering(newPosition);
        if (newPosition != oldPosition) {
            StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
            if (newPosition < oldPosition) {
                hql.append(" set ordering = ordering + 1").append(" where ordering >= :newPosition and ordering < :oldPosition");
            } else {
                hql.append(" set ordering = ordering - 1").append(" where ordering > :oldPosition and ordering <= :newPosition");
            }
            Query<?> query = getSession().createQuery(hql.toString());
            query.setParameter("newPosition", newPosition);
            query.setParameter("oldPosition", oldPosition);

            getSession().executeUpdate(query);
            getSession().update(subagentCluster);
        }
    }
    
    public Integer getMaxOrdering() throws SOSHibernateException {
        Query<Integer> query = getSession().createQuery("select max(ordering) from " + DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
        Integer result = getSession().getSingleResult(query);
        if (result == null) {
            return -1;
        }
        return result;
    }
    
    public List<DBItemInventorySubAgentCluster> getSubagentClustersByAgentId(String agentId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
        hql.append(" where agentId = :agentId");
        Query<DBItemInventorySubAgentCluster> query = getSession().createQuery(hql.toString());
        query.setParameter("agentId", agentId);
        List<DBItemInventorySubAgentCluster> result = getSession().getResultList(query);
        if(result == null) {
            return Collections.emptyList();
        } else {
            return result;
        }
    }
    
    public static void fillEmptyControllerIds() {
        new Thread(() -> {
            SOSHibernateSession connection = null;
            try {
                connection = Globals.createSosHibernateStatelessConnection("fillEmptyControllerIdsInSubAgentClusters");
                new InventorySubagentClustersDBLayer(connection).fillEmptyControllerIdsInSubAgentClusters();
            } catch (Exception e) {
                LOGGER.warn(e.toString());
            } finally {
                Globals.disconnect(connection);
            }
        }, "updateSubAgentClusters").start();
        
    }
    
    public void fillEmptyControllerIdsInSubAgentClusters() throws SOSHibernateException {
        List<DBItemInventorySubAgentCluster> agentClusters = getSubagentClustersWithEmptyControllerId();
        if (!agentClusters.isEmpty()) {

            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(getSession());
            Map<String, String> agentIdToControllerIdMap = agentDbLayer.getAllAgents().stream().collect(Collectors.toMap(
                    DBItemInventoryAgentInstance::getAgentId, DBItemInventoryAgentInstance::getControllerId));

            Map<String, List<DBItemInventorySubAgentClusterMember>> agentClusterMembersPerClusterId = getSubagentClusterMembersWithEmptyControllerId()
                    .stream().collect(Collectors.groupingBy(DBItemInventorySubAgentClusterMember::getSubAgentClusterId));

            agentClusters.stream().peek(a -> a.setControllerId(agentIdToControllerIdMap.get(a.getAgentId()))).forEach(a -> {
                try {
                    if (!agentIdToControllerIdMap.containsKey(a.getAgentId())) {
                        getSession().delete(a); 
                    } else if (a.getControllerId() != null) {
                        getSession().update(a);
                    }
                    List<DBItemInventorySubAgentClusterMember> sacms = agentClusterMembersPerClusterId.get(a.getSubAgentClusterId());
                    if (sacms != null) {
                        sacms.stream().peek(sacm -> sacm.setControllerId(a.getControllerId())).forEach(sacm -> {
                            try {
                                if (!agentIdToControllerIdMap.containsKey(a.getAgentId())) {
                                    getSession().delete(sacm);
                                } else if (a.getControllerId() != null) {
                                    getSession().update(sacm);
                                }
                            } catch (SOSHibernateException e) {
                                LOGGER.warn(e.toString());
                            }
                        });
                    }
                } catch (SOSHibernateException e) {
                    LOGGER.warn(e.toString());
                }
            });

        }
    }
    
    private List<DBItemInventorySubAgentCluster> getSubagentClustersWithEmptyControllerId() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
        hql.append(" where controllerId is null or controllerId = ''");
        Query<DBItemInventorySubAgentCluster> query = getSession().createQuery(hql.toString());
        List<DBItemInventorySubAgentCluster> result = getSession().getResultList(query);
        if(result == null) {
            return Collections.emptyList();
        } else {
            return result;
        }
    }
    
    private List<DBItemInventorySubAgentClusterMember> getSubagentClusterMembersWithEmptyControllerId() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS);
        hql.append(" where controllerId is null or controllerId = ''");
        Query<DBItemInventorySubAgentClusterMember> query = getSession().createQuery(hql.toString());
        List<DBItemInventorySubAgentClusterMember> result = getSession().getResultList(query);
        if(result == null) {
            return Collections.emptyList();
        } else {
            return result;
        }
    }
    
//    public List<DBItemInventorySubAgentClusterMember> getSubagentClusterMembers (String subagentClusterId) throws SOSHibernateException {
//        StringBuilder hql = new StringBuilder();
//        hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS);
//        hql.append(" where subagentClusterId = :subagentClusterId");
//        Query<DBItemInventorySubAgentClusterMember> query = getSession().createQuery(hql.toString());
//        query.setParameter("subagentClusterId", subagentClusterId);
//        List<DBItemInventorySubAgentClusterMember> result = getSession().getResultList(query);
//        if(result == null) {
//            return Collections.emptyList();
//        } else {
//            return result;
//        }
//    } 

}