package com.sos.joc.db.inventory.agent;

import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.deprecated.InventoryDBItemConstants;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentCluster;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.jobscheduler.AgentOfCluster;
import com.sos.joc.model.jobscheduler.JobSchedulerStateText;

public class InventoryAgentsDBLayer {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryAgentsDBLayer.class);
	private static final String AGENT_CLUSTER_MEMBER = AgentClusterMember.class.getName();
    private static final String AGENT_CLUSTER_P = AgentClusterPermanent.class.getName();
    private SOSHibernateSession session;

    public InventoryAgentsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }

    public DBItemInventoryAgentInstance getInventoryAgentInstance(String url, Long instanceId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES);
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
    
    public void updateInventoryAgentInstance(Long instanceId, AgentOfCluster agent) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            if (agent.getState().get_text() != JobSchedulerStateText.UNKNOWN_AGENT) {
                DBItemInventoryAgentInstance item = getInventoryAgentInstance(agent.getUrl(), instanceId);
                if (item != null) {
                    if (agent.getState().get_text() == JobSchedulerStateText.UNREACHABLE) {
                        item.setStartedAt(null);
                        item.setState(1);
                    } else {
                        item.setHostname(agent.getHost());
                        item.setStartedAt(agent.getStartedAt());
                        item.setState(0);
                        item.setVersion(agent.getVersion());
                    }
                    item.setModified(agent.getSurveyDate());
                    session.update(item);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Problem during update INVENTORY_AGENT_INSTANCES", ex);
        }
    }

    public List<DBItemInventoryAgentInstance> getInventoryAgentInstances(Long instanceId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES);
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

    public List<String> getProcessClassesFromAgentCluster(Long agentId, Long instanceId) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select ipc.name from ");
            // TODO: has to be changed after the JobScheduler Objects are specified
            // sql.append(DBITEM_INVENTORY_PROCESS_CLASSES).append(" ipc, ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTER).append(" iac, ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS).append(" iacm ");
            sql.append("where");
            // TODO: has to be changed after the JobScheduler Objects are specified
            // sql.append(" ipc.id = iac.processClassId");
            // sql.append(" and");
            sql.append(" iac.id = iacm.agentClusterId");
            sql.append(" and iacm.agentInstanceId = :agentId");
            // TODO: has to be changed after the JobScheduler Objects are specified
            // sql.append(" and ipc.instanceId = :instanceId");
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

    public List<DBItemInventoryAgentCluster> getAgentClusters(Long instanceId) throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTER);
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

    public List<AgentClusterPermanent> getInventoryAgentClusters(Long instanceId, Set<String> agentClusters) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_P);
            sql.append("(iac.id, iac.schedulingType, iac.numberOfAgents, iac.modified, ipc.name, ipc.maxProcesses) from ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTER).append(" iac, ");
            // TODO: has to be changed after the JobScheduler Objects are specified
            // sql.append(DBITEM_INVENTORY_PROCESS_CLASSES).append(" ipc ");
            sql.append("where");
            // TODO: has to be changed after the JobScheduler Objects are specified
            // sql.append(" iac.processClassId = ipc.id ");
            // sql.append("and");
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

    public List<AgentClusterMember> getInventoryAgentClusterMembers(Long instanceId, Set<Long> agentClusterIds) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_MEMBER);
            sql.append(" (iacm.agentClusterId, iacm.url, iacm.ordering, iacm.modified, iai.version, iai.state, iai.startedAt, ");
            sql.append("ios.hostname, ios.name, ios.architecture, ios.distribution) from ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS).append(" iacm, ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES).append(" iai, ");
            sql.append(DBLayer.DBITEM_INV_OPERATING_SYSTEMS).append(" ios ");
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
    
    public List<AgentClusterMember> getInventoryAgentClusterMembersByUrls(Long instanceId, Set<String> agentUrls)
            throws DBInvalidDataException, DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_MEMBER);
            sql.append(" (iai.url, iai.version, ios.hostname, ios.name, ios.architecture, ios.distribution) from ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES).append(" iai, ");
            sql.append(DBLayer.DBITEM_INV_OPERATING_SYSTEMS).append(" ios ");
            sql.append("where iai.osId = ios.id ");
            sql.append("and iai.instanceId = :instanceId");
            if (agentUrls != null && !agentUrls.isEmpty()) {
                if (agentUrls.size() == 1) {
                    sql.append(" and iai.url = :agentUrl");
                } else {
                    sql.append(" and iai.url in (:agentUrl)");
                }
            }
            Query<AgentClusterMember> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", instanceId);
            if (agentUrls != null && !agentUrls.isEmpty()) {
                if (agentUrls.size() == 1) {
                    query.setParameter("agentUrl", agentUrls.iterator().next());
                } else {
                    query.setParameterList("agentUrl", agentUrls);
                }
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<AgentClusterMember> getInventoryAgentClusterMembersById(Long instanceId, Long agentClusterId) throws DBInvalidDataException,
            DBConnectionRefusedException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select new ").append(AGENT_CLUSTER_MEMBER);
            sql.append(" (iacm.agentClusterId, iacm.url, iacm.modified, iai.version, iai.state, iai.startedAt, ios.hostname, ");
            sql.append("ios.name, ios.architecture, ios.distribution) from ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_CLUSTERMEMBERS).append(" iacm, ");
            sql.append(InventoryDBItemConstants.DBITEM_INVENTORY_AGENT_INSTANCES).append(" iai, ");
            sql.append(DBLayer.DBITEM_INV_OPERATING_SYSTEMS).append(" ios ");
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