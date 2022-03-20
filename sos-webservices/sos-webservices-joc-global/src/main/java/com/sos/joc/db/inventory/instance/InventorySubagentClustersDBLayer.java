package com.sos.joc.db.inventory.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.items.SubagentCluster;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.agent.SubAgentId;

public class InventorySubagentClustersDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventorySubagentClustersDBLayer(SOSHibernateSession conn) {
        super(conn);
    }

    public List<DBItemInventorySubAgentCluster> getSubagentClusters(List<String> subagentClusterIds) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventorySubAgentCluster> r = new ArrayList<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getSubagentClusters(SOSHibernate.getInClausePartition(i, subagentClusterIds)));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS);
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        hql.append(" where subAgentClusterId = :subagentClusterId");
                    } else {
                        hql.append(" where subAgentClusterId in (:subagentClusterIds)");
                    }
                }
                Query<DBItemInventorySubAgentCluster> query = getSession().createQuery(hql.toString());
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    } else {
                        query.setParameterList("subagentClusterIds", subagentClusterIds);
                    }
                }
                return getSession().getResultList(query);
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }
    
    public Map<DBItemInventorySubAgentCluster, List<SubAgentId>> getSubagentClusters(Set<String> controllerIds, List<String> agentIds,
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

    private Map<DBItemInventorySubAgentCluster, List<SubAgentId>> getSubagentClusters2(Set<String> controllerIds, List<String> agentIds,
            List<String> subagentClusterIds) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> r = new HashMap<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.putAll(getSubagentClusters(controllerIds, agentIds, SOSHibernate.getInClausePartition(i, subagentClusterIds)));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("select new ").append(SubagentCluster.class.getName()).append("(sac, sacm.subAgentId, sacm.priority) from ");
                hql.append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTERS).append(" sac, ");
                if (controllerIds != null && !controllerIds.isEmpty()) {
                    hql.append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS).append(" sacm, ");
                    hql.append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(" ai ");
                    hql.append("where sac.subAgentClusterId = sacm.subAgentClusterId ");
                    hql.append("and ai.agentId = sac.agentId ");
                    if (controllerIds.size() == 1) {
                        hql.append("and ai.controllerId = :controllerId ");
                    } else {
                        hql.append("and ai.controllerId in (:controllerIds) ");
                    }
                } else {
                    hql.append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS).append(" sacm ");
                    hql.append("where sac.subAgentClusterId = sacm.subAgentClusterId ");
                }
                if (agentIds != null && !agentIds.isEmpty()) {
                    if (agentIds.size() == 1) {
                        hql.append(" and sac.agentId = :agentId ");
                    } else {
                        hql.append(" and sac.agentId in (:agentIds) ");
                    }
                }
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        hql.append(" and sac.subAgentClusterId = :subagentClusterId");
                    } else {
                        hql.append(" and sac.subAgentClusterId in (:subagentClusterIds)");
                    }
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
                        SubagentCluster::getDBItemInventorySubAgentClusterMember, Collectors.toList())));
            } catch (DBMissingDataException ex) {
                throw ex;
            } catch (SOSHibernateInvalidSessionException ex) {
                throw new DBConnectionRefusedException(ex);
            } catch (Exception ex) {
                throw new DBInvalidDataException(ex);
            }
        }
    }

    public List<DBItemInventorySubAgentClusterMember> getSubagentClusterMembers(List<String> subagentClusterIds) throws DBInvalidDataException,
            DBMissingDataException, DBConnectionRefusedException {

        if (subagentClusterIds != null && subagentClusterIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            List<DBItemInventorySubAgentClusterMember> r = new ArrayList<>();
            for (int i = 0; i < subagentClusterIds.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                r.addAll(getSubagentClusterMembers(SOSHibernate.getInClausePartition(i, subagentClusterIds)));
            }
            return r;
        } else {
            try {
                StringBuilder hql = new StringBuilder();
                hql.append("from ").append(DBLayer.DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS);
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        hql.append(" where subAgentClusterId = :subagentClusterId");
                    } else {
                        hql.append(" where subAgentClusterId in (:subagentClusterIds)");
                    }
                }
                Query<DBItemInventorySubAgentClusterMember> query = getSession().createQuery(hql.toString());
                if (subagentClusterIds != null && !subagentClusterIds.isEmpty()) {
                    if (subagentClusterIds.size() == 1) {
                        query.setParameter("subagentClusterId", subagentClusterIds.get(0));
                    } else {
                        query.setParameterList("subagentClusterIds", subagentClusterIds);
                    }
                }
                return getSession().getResultList(query);
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

}