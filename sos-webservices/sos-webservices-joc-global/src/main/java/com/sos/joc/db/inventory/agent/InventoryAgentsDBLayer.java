package com.sos.joc.db.inventory.agent;

import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;

import com.sos.commons.db.jobscheduler.DBItemInventoryAgentCluster;
import com.sos.commons.db.jobscheduler.DBItemInventoryAgentInstance;
import com.sos.commons.db.jobscheduler.JobSchedulerDBItemConstants;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class InventoryAgentsDBLayer {

    private static final String AGENT_CLUSTER_MEMBER = AgentClusterMember.class.getName();
    private static final String AGENT_CLUSTER_P = AgentClusterPermanent.class.getName();
    private SOSHibernateSession session;

    public InventoryAgentsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DBItemInventoryAgentInstance getInventoryAgentInstances(String url, Long instanceId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES);
            sql.append(" where url = :url");
            sql.append(" and instanceId = :instanceId");
            Query<DBItemInventoryAgentInstance> query = session.createQuery(sql.toString());
            query.setParameter("url", url);
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryAgentInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryAgentInstance> getInventoryAgentInstances(Long instanceId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES);
            sql.append(" where instanceId = :instanceId");
            Query<DBItemInventoryAgentInstance> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryAgentInstance> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getProcessClassesFromAgentCluster(Long agentId, Long instanceId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select ipc.name from ");
            // TODO: has to be changed after the JobScheduler Objects are specified
//            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES).append(" ipc, ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTER).append(" iac, ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS).append(" iacm ");
            sql.append("where");
            // TODO: has to be changed after the JobScheduler Objects are specified
//            sql.append(" ipc.id = iac.processClassId");
//            sql.append(" and");
            sql.append(" iac.id = iacm.agentClusterId");
            sql.append(" and iacm.agentInstanceId = :agentId");
            // TODO: has to be changed after the JobScheduler Objects are specified
//            sql.append(" and ipc.instanceId = :instanceId");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("agentId", agentId);
            query.setParameter("instanceId", instanceId);
            List<String> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryAgentCluster> getAgentClusters(Long instanceId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTER);
            sql.append(" where instanceId = :instanceId");
            Query<DBItemInventoryAgentCluster> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            List<DBItemInventoryAgentCluster> result = session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<AgentClusterPermanent> getInventoryAgentClusters(Long instanceId, Set<String> agentClusters)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_P);
            sql.append("(iac.id, iac.schedulingType, iac.numberOfAgents, iac.modified, ipc.name, ipc.maxProcesses) from ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTER).append(" iac, ");
            // TODO: has to be changed after the JobScheduler Objects are specified
//            sql.append(DBITEM_INVENTORY_PROCESS_CLASSES).append(" ipc ");
            sql.append("where");
            // TODO: has to be changed after the JobScheduler Objects are specified
//            sql.append(" iac.processClassId = ipc.id ");
//            sql.append("and");
            sql.append(" iac.instanceId = :instanceId");
            if (agentClusters != null && !agentClusters.isEmpty()) {
                if (agentClusters.size() == 1) {
                    sql.append(" and ipc.name = :agentCluster");
                } else {
                    sql.append(" and ipc.name in (:agentCluster)");
                }
            }
            Query<AgentClusterPermanent> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            if (agentClusters != null && !agentClusters.isEmpty()) {
                if (agentClusters.size() == 1) {
                    query.setParameter("agentCluster", agentClusters.iterator().next());
                } else {
                    query.setParameterList("agentCluster", agentClusters);
                }
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<AgentClusterMember> getInventoryAgentClusterMembers(Long instanceId, Set<Long> agentClusterIds)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_MEMBER);
            sql.append(" (iacm.agentClusterId, iacm.url, iacm.ordering, iacm.modified, iai.version, iai.state, iai.startedAt, ");
            sql.append("ios.hostname, ios.name, ios.architecture, ios.distribution) from ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS).append(" iacm, ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES).append(" iai, ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_OPERATING_SYSTEMS).append(" ios ");
            sql.append("where iacm.agentInstanceId = iai.id ");
            sql.append("and iai.osId = ios.id ");
            sql.append("and iacm.instanceId = :instanceId");
            if (agentClusterIds != null && !agentClusterIds.isEmpty()) {
                if (agentClusterIds.size() == 1) {
                    sql.append(" and iacm.agentClusterId = :agentClusterId");
                } else {
                    sql.append(" and iacm.agentClusterId in (:agentClusterId)");
                }
            }
            Query<AgentClusterMember> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            if (agentClusterIds != null && !agentClusterIds.isEmpty()) {
                if (agentClusterIds.size() == 1) {
                    query.setParameter("agentClusterId", agentClusterIds.iterator().next());
                } else {
                    query.setParameterList("agentClusterId", agentClusterIds);
                }
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<AgentClusterMember> getInventoryAgentClusterMembersById(Long instanceId, Long agentClusterId)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_MEMBER);
            sql.append(" (iacm.agentClusterId, iacm.url, iacm.modified, iai.version, iai.state, iai.startedAt, ios.hostname, ");
            sql.append("ios.name, ios.architecture, ios.distribution) from ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS).append(" iacm, ");
            sql.append(JobSchedulerDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES).append(" iai, ");
            sql.append(JobSchedulerDBItemConstants. DBITEM_INVENTORY_OPERATING_SYSTEMS).append(" ios ");
            sql.append("where iacm.agentInstanceId = iai.id ");
            sql.append("and iai.osId = ios.id ");
            sql.append("and iacm.instanceId = :instanceId");
            if (agentClusterId != null) {
                sql.append(" and iacm.agentClusterId = :agentClusterId");
            }
            sql.append(" order by iacm.ordering");
            Query<AgentClusterMember> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            if (agentClusterId != null) {
                query.setParameter("agentClusterId", agentClusterId);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
}