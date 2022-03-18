package com.sos.joc.db.inventory.instance;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;

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
    
    public String getFirstSubagentIdThatNotExists(List<String> subagentIds) throws DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {

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