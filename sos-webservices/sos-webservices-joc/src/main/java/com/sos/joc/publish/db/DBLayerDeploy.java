package com.sos.joc.publish.db;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.TemporalType;

import org.hibernate.criterion.MatchMode;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.DBSQLBatchPreparator;
import com.sos.joc.db.DBSQLBatchPreparator.BatchPreparator;
import com.sos.joc.db.deployment.DBItemDepCommitIds;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.deployment.DBItemDeploymentSubmission;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.DeployablesFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.ReleasablesFilter;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.model.publish.SetVersionsFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.mapper.FilterAttributesMapper;
import com.sos.joc.publish.util.PublishUtils;

public class DBLayerDeploy {

    private final SOSHibernateSession session;
    private static final String FROM_DEP_DATE = "deploymentDate >= :fromDate";
    private static final String TO_DEP_DATE = "deploymentDate < :toDate";
    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerDeploy.class);

    public DBLayerDeploy(SOSHibernateSession connection) {
        session = connection;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public List<DBItemDeploymentHistory> getConfigurationHistory(String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where controllerId = :controllerId");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        return session.getResultList(query);
    }

    public List<DBItemDepVersions> getVersions(Long invConfigurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_VERSIONS);
        hql.append(" where invConfigurationId = :invConfigurationId");
        Query<DBItemDepVersions> query = session.createQuery(hql.toString());
        query.setParameter("invConfigurationId", invConfigurationId);
        return session.getResultList(query);
    }

    public DBItemDepSignatures getSignature(Long inventoryConfigurationId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_DEP_SIGNATURES);
            hql.append(" where invConfigurationId = :inventoryConfigurationId order by id desc");
            Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
            query.setParameter("inventoryConfigurationId", inventoryConfigurationId);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDeployedConfigurations(Long inventoryConfigurationId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where inventoryConfigurationId = :inventoryConfigurationId");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("inventoryConfigurationId", inventoryConfigurationId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDepHistory(String commitId) throws DBConnectionRefusedException, DBInvalidDataException {
        return getDepHistory(commitId, (ConfigurationType) null);
    }

    public List<DBItemDeploymentHistory> getDepHistory(String commitId, ConfigurationType type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where commitId = :commitId");
            if (type != null) {
                sql.append(" and type = :type");
            }
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("commitId", commitId);
            if (type != null) {
                query.setParameter("type", type.intValue());
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDepHistoryItems(String commitId) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where commitId = :commitId");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("commitId", commitId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDepHistory(String commitId, Set<ConfigurationType> types) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where commitId = :commitId");
            if (types != null) {
                sql.append(" and type in (:types)");
            }
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("commitId", commitId);
            if (types != null) {
                query.setParameterList("types", types.stream().map(item -> item.intValue()).collect(Collectors.toSet()));
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDeployedConfigurationByPath(String path) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where path = :path");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getDeployedConfigurationByPathAndType(String path, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where path = :path");
            sql.append(" and type = :type");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<Long> getDeployableInventoryConfigurationIdsByFolder(String folder, boolean recursive) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where type in (:types)");
            if (recursive) {
                if (!"/".equals(folder)) {
                    sql.append(" and (folder = :folder or folder like :likefolder)");
                }
            } else {
                sql.append(" and folder = :folder");
            }
            Query<Long> query = session.createQuery(sql.toString());
            query.setParameterList("types", JocInventory.DEPLOYABLE_OBJECTS.stream().map(item -> item.intValue()).collect(Collectors.toList()));
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("folder", folder);
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    public List<DBItemInventoryConfiguration> getInventoryConfigurationsByFolder(String folder, boolean recursive)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getInventoryConfigurationsByFolder(folder, JocInventory.getDeployableTypes(), recursive);
    }

    public List<DBItemInventoryConfiguration> getAllInventoryConfigurationsByFolder(String folder, boolean recursive)
            throws DBConnectionRefusedException, DBInvalidDataException {
        Set<Integer> types = JocInventory.getDeployableTypes();
        types.addAll(JocInventory.getReleasableTypes());
        return getInventoryConfigurationsByFolder(folder, types, recursive);
    }

    private List<DBItemInventoryConfiguration> getInventoryConfigurationsByFolder(String folder, Set<Integer> types, boolean recursive)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where type in (:types)");
            if (recursive) {
                if (!"/".equals(folder)) {
                    sql.append(" and (folder = :folder or folder like :likefolder)");
                }
            } else {
                sql.append(" and folder = :folder");
            }
            Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
            query.setParameterList("types", types);
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("folder", folder);
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            } else {
                query.setParameter("folder", folder);
            }
            List<DBItemInventoryConfiguration> result = session.getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryConfiguration> getInventoryConfigurationsByIds(List<Long> ids) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (ids != null && !ids.isEmpty()) {
                if (ids.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
                    List<DBItemInventoryConfiguration> results = new ArrayList<>();
                    for (int i = 0; i < ids.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                        results.addAll(getInventoryConfigurationsByIds(SOSHibernate.getInClausePartition(i, ids)));
                    }
                    return results;
                } else {
                    StringBuilder sql = new StringBuilder();
                    sql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                    sql.append(" where id in (:ids)");
                    Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                    query.setParameterList("ids", ids);
                    List<DBItemInventoryConfiguration> result = session.getResultList(query);
                    if (result == null) {
                        return Collections.emptyList();
                    }
                    return result;
                }
            } else {
                return Collections.emptyList();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getInventoryConfigurationIdByPathAndType(String path, Integer type) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where path = :path");
            sql.append(" and type = :type");
            Query<Long> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryConfiguration> getInventoryConfigurationByTypes(String folder, Collection<Integer> types)
            throws SOSHibernateException {
        if (folder == null) {
            folder = "/";
        }
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        if (!"/".equals(folder)) {
            hql.append(" where (folder=:folder or folder like :likeFolder)");
        } else {
            hql.append(" where folder like :folder");
        }
        if (types != null && !types.isEmpty()) {
            hql.append(" and type in (:types)");
        }
        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        if (!"/".equals(folder)) {
            query.setParameter("folder", folder);
            query.setParameter("likeFolder", folder + "/%");
        } else {
            query.setParameter("folder", folder + "%");
        }
        if (types != null && !types.isEmpty()) {
            query.setParameterList("types", types);
        }
        List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public DBItemInventoryConfiguration getInventoryConfigurationByNameAndType(String name, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where name = :name");
            sql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            DBItemInventoryConfiguration result = session.getSingleResult(query);
            return result;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getInventoryConfigurationIdByNameAndType(String name, Integer type) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where name = :name");
            sql.append(" and type = :type");
            Query<Long> query = session.createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Boolean getInventoryConfigurationDeployedByNameAndType(String name, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select deployed from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where name = :name");
            sql.append(" and type = :type");
            Query<Boolean> query = session.createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            return session.getSingleResult(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public Boolean getInventoryConfigurationReleasedByNameAndType(String name, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select released from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where name = :name");
            sql.append(" and type = :type");
            Query<Boolean> query = session.createQuery(sql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            return session.getSingleResult(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryConfiguration> getAllInventoryConfigurations() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeploymentsForSetVersion(SetVersionFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeployments(filter);
    }

    public List<DBItemInventoryConfiguration> getAllInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, false, false, false, false, false);
    }

    public List<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, true, false, false, false, false);
    }

    public List<DBItemInventoryConfiguration> getValidDeployableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, true, false, false, false, true);
    }

    public List<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, recursive, true, false, false, false, false);
    }

    public List<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsByFolderWithoutDeployed(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, recursive, true, false, true, false, false);
    }

    public List<DBItemInventoryConfiguration> getValidDeployableInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, recursive, true, false, false, false, true);
    }

    public List<DBItemInventoryConfiguration> getValidDeployableDraftInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getDraftInventoryConfigurationsByFolder(folder, true, false, recursive, true);
    }

    public List<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, false, true, false, false, false);
    }

    public List<DBItemInventoryConfiguration> getValidReleasableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, false, true, false, false, true);
    }

    public List<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsByFolderWithoutReleased(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, recursive, false, true, true, true, false);
    }

    public List<DBItemInventoryConfiguration> getValidReleasableInventoryConfigurationsByFolderWithoutReleased(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, recursive, false, true, false, true, true);
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsByFolder(String folder) {
        return getReleasedInventoryConfigurationsByFolder(folder, false);
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsByFolder(String folder, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append(" from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" where (folder = :folder or folder like :likefolder)");
                }
            } else {
                hql.append(" where folder = :folder");
            }
            Query<DBItemInventoryReleasedConfiguration> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("folder", folder);
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsByFolderWithoutReleased(String folder, boolean recursive,
            boolean valid) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where released = false");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (folder = :folder or folder like :likefolder)");
                }
            } else {
                hql.append(" and folder = :folder");
            }
            if (valid) {
                hql.append(" and valid = true");
            }
            hql.append(" and type in (:types)");
            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("folder", folder);
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            } else {
                query.setParameter("folder", folder);
            }
            query.setParameterList("types", JocInventory.getReleasableTypes());
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    private List<DBItemInventoryConfiguration> getInventoryConfigurationsByFolder(String folder, boolean recursive, boolean deployablesOnly,
            boolean releasablesOnly, boolean withoutDeployed, boolean withoutReleased, boolean validOnly) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (recursive) {
                hql.append(" where (folder = :folder or folder like :likefolder)");
            } else {
                hql.append(" where folder = :folder");
            }
            if (deployablesOnly || releasablesOnly) {
                hql.append(" and type in (:types)");
            }
            if (validOnly) {
                hql.append(" and valid = true");
            }
            if (withoutDeployed) {
                hql.append(" and deployed = false");
            }
            if (withoutReleased) {
                hql.append(" and released = false");
            }
            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setParameter("folder", folder);
            if (recursive) {
                if ("/".equals(folder)) {
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder));
                } else {
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            }
            if (deployablesOnly) {

                query.setParameterList("types", JocInventory.DEPLOYABLE_OBJECTS.stream().map(item -> item.intValue()).collect(Collectors.toList()));
            } else if (releasablesOnly) {
                query.setParameterList("types", JocInventory.RELEASABLE_OBJECTS.stream().map(item -> item.intValue()).collect(Collectors.toList()));
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    private List<DBItemInventoryConfiguration> getDraftInventoryConfigurationsByFolder(String folder, boolean onlyDeployables,
            boolean onlyReleasables, boolean recursive, boolean onlyValid) {
        Date getDraftInventoryConfigurationsByFolderStarted = Date.from(Instant.now());
        LOGGER.trace("*** call getDraftInventoryConfigurationsByFolder started ***" + getDraftInventoryConfigurationsByFolderStarted);

        try {
            StringBuilder hql = new StringBuilder();
            hql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where deployed = false");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (folder = :folder or folder like :likefolder)");
                }
            } else {
                hql.append(" and folder = :folder");
            }
            if (onlyDeployables || onlyReleasables) {
                hql.append(" and type in (:types)");
            }
            if (onlyValid) {
                hql.append(" and valid = true");
            }
            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                    query.setParameter("folder", folder);
                }
            } else {
                query.setParameter("folder", folder);
            }
            if (onlyDeployables) {
                query.setParameterList("types", JocInventory.DEPLOYABLE_OBJECTS.stream().map(item -> item.intValue()).collect(Collectors.toList()));
            } else if (onlyReleasables) {
                query.setParameterList("types", JocInventory.RELEASABLE_OBJECTS.stream().map(item -> item.intValue()).collect(Collectors.toList()));
            }
            List<DBItemInventoryConfiguration> result = session.getResultList(query);
            Date getDraftInventoryConfigurationsByFolderFinished = Date.from(Instant.now());
            LOGGER.trace("*** call getDraftInventoryConfigurationsByFolder finished ***" + getDraftInventoryConfigurationsByFolderFinished);
            LOGGER.trace("call getDraftInventoryConfigurationsByFolder took: " + (getDraftInventoryConfigurationsByFolderFinished.getTime() - getDraftInventoryConfigurationsByFolderStarted.getTime()) + " ms");
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsByPaths(List<String> paths) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (paths != null && !paths.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                sql.append(" where path in (:paths)");
                Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                query.setParameterList("paths", paths);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemInventoryConfiguration>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredInventoryConfiguration(List<Configuration> configurations)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<DBItemInventoryConfiguration> results = new ArrayList<DBItemInventoryConfiguration>();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            for (Configuration cfg : configurations) {
                if (ConfigurationType.FOLDER != cfg.getObjectType()) {
                    query.setParameter("path", cfg.getPath());
                    query.setParameter("type", cfg.getObjectType().intValue());
                    query.setMaxResults(1);
                    try {
                        results.add(query.getSingleResult());
                    } catch (NoResultException e) {
                        LOGGER.trace("no result found in db!");
                    }
                }
            }
            return results;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getFilteredReleasedConfiguration(List<Configuration> configurations)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<DBItemInventoryReleasedConfiguration> results = new ArrayList<DBItemInventoryReleasedConfiguration>();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where path = :path and type = :type");

            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            for (Configuration cfg : configurations) {
                if (ConfigurationType.FOLDER != cfg.getObjectType()) {
                    query.setParameter("path", cfg.getPath());
                    query.setParameter("type", cfg.getObjectType().intValue());
                    query.setMaxResults(1);
                    results.add(query.getSingleResult());
                }
            }
            return results;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeploymentHistory(List<Configuration> deployConfigurations) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            List<DBItemDeploymentHistory> results = new ArrayList<DBItemDeploymentHistory>();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where path = : path and type = :type and commitId = :commitId");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (Configuration cfg : deployConfigurations) {
                query.setParameter("path", cfg.getPath());
                query.setParameter("type", cfg.getObjectType().intValue());
                query.setParameter("commitId", cfg.getCommitId());
                query.setMaxResults(1);
                DBItemDeploymentHistory result = null;
                try {
                    result = query.getSingleResult();
                    if (result != null) {
                        results.add(result);
                    }
                } catch (NoResultException e) {
                    LOGGER.trace("no result found in db!");
                }
            }
            return results;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeploymentHistoryToDelete(List<Configuration> deployConfigurations)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<DBItemDeploymentHistory> results = new ArrayList<DBItemDeploymentHistory>();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where path = :path and type = :type");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (Configuration cfg :deployConfigurations) {
                query.setParameter("path", cfg.getPath());
                query.setParameter("type", cfg.getObjectType().intValue());
                query.setMaxResults(1);
                results.add(query.getSingleResult());
            }
            if (results != null && !results.isEmpty()) {
                return results;
            } else {
                // check if configuration(s) exists with the given path(s) and get deployments with the configurations id
                List<DBItemDeploymentHistory> dbItemsByConfId = new ArrayList<DBItemDeploymentHistory>();
                deployConfigurations.stream().forEach(item -> {
                    try {
                        DBItemDeploymentHistory d = getLatestActiveDepHistoryItem(getConfigurationByName(Paths.get(item.getPath()).getFileName()
                                .toString(), item.getObjectType()).getId());
                        if (d != null) {
                            dbItemsByConfId.add(d);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
                return dbItemsByConfId;
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getAllDeployedConfigurations() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredDeployableConfigurations(DeployablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDraftConfigurations().stream()
                .map(item -> item.getConfiguration()).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredDeployableConfigurations(CopyToFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        // TODO: check rollout and local configurations as configured, as JOBRESOURCES may not be the only item
        List<Configuration> configurations = new ArrayList<Configuration>();
        if (filter.getRollout() != null) {
            configurations.addAll(filter.getRollout().getDraftConfigurations().stream()
                    .map(item -> item.getConfiguration()).collect(Collectors.toList()));
        }
        if (filter.getLocal() != null) {
            configurations.addAll(filter.getLocal().getDraftConfigurations().stream()
                    .filter(item -> !ConfigurationType.JOBRESOURCE.equals(item.getConfiguration().getObjectType()))
                    .map(item -> item.getConfiguration()).collect(Collectors.toList()));
        }
        if (!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }
    
    public List<DBItemInventoryConfiguration> getFilteredDeployableConfigurations(DeployablesValidFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDraftConfigurations().stream()
                .map(item -> item.getConfiguration()).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredReleasableConfigurations(CopyToFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = Collections.emptyList();
        // TODO: check local configurations as configured, as JOBRESOURCES may not be the only item
        if (filter.getLocal() != null) {
            configurations = filter.getLocal().getDraftConfigurations().stream()
                    .filter(item -> !ConfigurationType.JOBRESOURCE.equals(item.getConfiguration().getObjectType()))
                    .map(item -> item.getConfiguration()).collect(Collectors.toList());
        }
        if (!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredReleasableConfigurations(ReleasablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDraftConfigurations().stream()
                .map(item -> item.getConfiguration()).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getFilteredReleasedConfigurations(CopyToFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        // TODO: check local configurations as configured, as JOBRESOURCES may not be the only item
        List<Configuration> configurations = Collections.emptyList();
        if (filter.getLocal() != null) {
            configurations = filter.getLocal().getReleasedConfigurations().stream()
                    .filter(item -> !ConfigurationType.JOBRESOURCE.equals(item.getConfiguration().getObjectType()))
                    .map(item -> item.getConfiguration()).collect(Collectors.toList());
        }
        if (!configurations.isEmpty()) {
            return getFilteredReleasedConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryReleasedConfiguration>();
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getFilteredReleasedConfigurations(ReleasablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getReleasedConfigurations().stream()
                .map(item -> item.getConfiguration()).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredReleasedConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryReleasedConfiguration>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(DeployablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDeployConfigurations().stream().filter(item -> !item.getConfiguration().getObjectType().equals(
                ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredDeploymentHistory(configurations);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(CopyToFilter filter) throws DBConnectionRefusedException, DBInvalidDataException {
        // TODO: check rollout and local configurations as configured, as JOBRESOURCES may not be the only item
        List<Configuration> configurations = new ArrayList<Configuration>();
        if (filter.getRollout() != null) {
            configurations = filter.getRollout().getDeployConfigurations().stream()
                    .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                    .map(item -> item.getConfiguration()).collect(Collectors.toList());
        }
        if (filter.getLocal() != null) {
            configurations.addAll(filter.getLocal().getDeployConfigurations().stream()
                    .filter(item -> ConfigurationType.JOBRESOURCE.equals(item.getConfiguration().getObjectType()))
                    .map(item -> item.getConfiguration()).collect(Collectors.toList()));
        }
        if (!configurations.isEmpty()) {
            return getFilteredDeploymentHistory(configurations);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(DeployablesValidFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDeployConfigurations().stream().filter(item -> !item.getConfiguration().getObjectType().equals(
                ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredDeploymentHistory(configurations);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(SetVersionFilter filter) throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredDeploymentHistory(filter.getDeployConfigurations().stream().map(item -> item.getConfiguration()).collect(Collectors
                .toList()));
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(SetVersionsFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeploymentHistory(filter.getDeployConfigurations().stream().map(item -> item.getConfiguration()).collect(Collectors
                .toList()));
    }

    public DBItemDeploymentHistory getDeployedConfiguration(String path, Integer type) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" where path = :path");
            sql.append(" and type = :type");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void saveOrUpdateInventoryConfiguration(ConfigurationObject configuration, String account, Long auditLogId, Set<String> agentNames) {
        saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, false, agentNames);
    }

    public void saveOrUpdateInventoryConfiguration(ConfigurationObject configuration, String account, Long auditLogId, boolean overwrite,
            Set<String> agentNames) {
        try {
            DBItemInventoryConfiguration existingConfiguration = null;
            StringBuilder hql = new StringBuilder(" from ");
            hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where name = :name");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setParameter("type", configuration.getObjectType().intValue());
            query.setParameter("name", configuration.getName());
            query.setMaxResults(1);
            existingConfiguration = session.getSingleResult(query);
            boolean valid = false;
            try {
                Validator.validate(configuration.getObjectType(), configuration.getConfiguration(), new InventoryDBLayer(session), agentNames);
                valid = true;
            } catch (Throwable e) {
                valid = false;
            }
            // check if imported agentName is known. Has to be removed, when the Validator takes over the check!
            if (configuration.getObjectType().equals(ConfigurationType.WORKFLOW)) {
                Workflow workflow = (Workflow) configuration.getConfiguration();
                boolean allAgentNamesKnown = true;
                if (workflow.getJobs() != null && workflow.getJobs().getAdditionalProperties() != null) {
                    for (String jobname : workflow.getJobs().getAdditionalProperties().keySet()) {
                        Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
                        String agentName = job.getAgentName();
                        boolean agentNameKnown = checkAgentNamePresent(agentName);
                        if (!agentNameKnown) {
                            allAgentNamesKnown = false;
                            break;
                        }
                    }
                }
                if (!allAgentNamesKnown) {
                    valid = false;
                }
            }

            if (overwrite) {
                if (existingConfiguration != null) {
                    existingConfiguration.setModified(Date.from(Instant.now()));
                    existingConfiguration.setContent(Globals.objectMapper.writeValueAsString(configuration.getConfiguration()));
                    if (configuration.getConfiguration().getTitle() != null) {
                        existingConfiguration.setTitle(configuration.getConfiguration().getTitle());
                    }
                    if (configuration.getPath() != null) {
                        existingConfiguration.setPath(configuration.getPath());
                        existingConfiguration.setFolder(Paths.get(existingConfiguration.getPath()).getParent().toString().replace('\\', '/'));
                    }
                    existingConfiguration.setAuditLogId(auditLogId);
                    existingConfiguration.setValid(valid);
                    existingConfiguration.setDeployed(false);
                    existingConfiguration.setReleased(false);
                    JocInventory.updateConfiguration(new InventoryDBLayer(session), existingConfiguration);
                } else {
                    DBItemInventoryConfiguration newConfiguration = new DBItemInventoryConfiguration();
                    Date now = Date.from(Instant.now());
                    newConfiguration.setModified(now);
                    newConfiguration.setCreated(now);
                    newConfiguration.setContent(Globals.objectMapper.writeValueAsString(configuration.getConfiguration()));
                    newConfiguration.setPath(configuration.getPath());
                    newConfiguration.setFolder(Paths.get(configuration.getPath()).getParent().toString().replace('\\', '/'));
                    newConfiguration.setName(Paths.get(newConfiguration.getPath()).getFileName().toString());
                    newConfiguration.setType(configuration.getObjectType());
                    newConfiguration.setAuditLogId(auditLogId);
                    newConfiguration.setTitle(configuration.getConfiguration().getTitle());
                    newConfiguration.setDeployed(false);
                    newConfiguration.setReleased(false);
                    newConfiguration.setValid(valid);
                    JocInventory.insertConfiguration(new InventoryDBLayer(session), newConfiguration);
                }
            } else {
                DBItemInventoryConfiguration newConfiguration = new DBItemInventoryConfiguration();
                Date now = Date.from(Instant.now());
                newConfiguration.setModified(now);
                newConfiguration.setCreated(now);
                newConfiguration.setContent(Globals.objectMapper.writeValueAsString(configuration.getConfiguration()));
                newConfiguration.setPath(configuration.getPath());
                newConfiguration.setFolder(Paths.get(configuration.getPath()).getParent().toString().replace('\\', '/'));
                newConfiguration.setName(Paths.get(newConfiguration.getPath()).getFileName().toString());
                newConfiguration.setType(configuration.getObjectType());
                newConfiguration.setAuditLogId(auditLogId);
                newConfiguration.setTitle(configuration.getConfiguration().getTitle());
                newConfiguration.setDeployed(false);
                newConfiguration.setReleased(false);
                newConfiguration.setValid(valid);
                JocInventory.insertConfiguration(new InventoryDBLayer(session), newConfiguration);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    public void saveNewInventoryConfiguration(ConfigurationObject configuration, String account, Long auditLogId, boolean overwrite,
            Set<String> agentNames) {
        boolean valid = false;
        try {
            Validator.validate(configuration.getObjectType(), configuration.getConfiguration(), new InventoryDBLayer(session), agentNames);
            valid = true;
        } catch (Exception e) {
            valid = false;
        }
        // check if imported agentName is known. Has to be removed, when the Validator takes over the check!
        if (configuration.getObjectType().equals(ConfigurationType.WORKFLOW)) {
            Workflow workflow = (Workflow) configuration.getConfiguration();
            boolean allAgentNamesKnown = true;
            for (String jobname : workflow.getJobs().getAdditionalProperties().keySet()) {
                Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
                String agentName = job.getAgentName();
                boolean agentNameKnown = checkAgentNamePresent(agentName);
                if (!agentNameKnown) {
                    allAgentNamesKnown = false;
                    break;
                }
            }
            if (!allAgentNamesKnown) {
                valid = false;
            }
        }
        try {
            DBItemInventoryConfiguration newConfiguration = new DBItemInventoryConfiguration();
            Date now = Date.from(Instant.now());
            newConfiguration.setModified(now);
            newConfiguration.setCreated(now);
            newConfiguration.setContent(Globals.objectMapper.writeValueAsString(configuration.getConfiguration()));
            newConfiguration.setPath(configuration.getPath());
            newConfiguration.setFolder(Paths.get(configuration.getPath()).getParent().toString().replace('\\', '/'));
            newConfiguration.setName(Paths.get(newConfiguration.getPath()).getFileName().toString());
            newConfiguration.setType(configuration.getObjectType());
            newConfiguration.setAuditLogId(auditLogId);
            newConfiguration.setTitle(configuration.getConfiguration().getTitle());
            newConfiguration.setDeployed(false);
            newConfiguration.setReleased(false);
            newConfiguration.setValid(valid);
            JocInventory.insertConfiguration(new InventoryDBLayer(session), newConfiguration);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    public DBItemInventoryConfiguration getConfigurationByPath(String path, ConfigurationType type) {
        return getConfigurationByPath(path, type.intValue());
    }

    public DBItemInventoryConfiguration getConfigurationByPath(String path, Integer type) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type);
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public DBItemInventoryConfiguration getConfiguration(Long id) {
        try {
            return getSession().get(DBItemInventoryConfiguration.class, id);
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public DBItemInventoryConfiguration getConfigurationByName(String name, ConfigurationType type) {
        return getConfigurationByName(name, type.intValue());
    }

    public DBItemInventoryConfiguration getConfigurationByName(String name, Integer type) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where name = :name");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public DBItemDepSignatures saveOrUpdateSignature(Long invConfId, SignaturePath signaturePath, String account, DeployType type)
            throws SOSHibernateException {
        DBItemDepSignatures dbItemSig = getSignature(invConfId);
        String signature = signaturePath.getSignature().getSignatureString();
        if (signature != null && !signature.isEmpty()) {
            if (dbItemSig != null) {
                dbItemSig.setAccount(account);
                dbItemSig.setSignature(signature);
                dbItemSig.setDepHistoryId(null);
                dbItemSig.setModified(Date.from(Instant.now()));
                session.update(dbItemSig);
            } else {
                dbItemSig = new DBItemDepSignatures();
                dbItemSig.setAccount(account);
                dbItemSig.setSignature(signature);
                dbItemSig.setDepHistoryId(null);
                dbItemSig.setInvConfigurationId(invConfId);
                dbItemSig.setModified(Date.from(Instant.now()));
                session.save(dbItemSig);
            }
            return dbItemSig;
        } else {
            return null;
        }
    }

    public DBItemDepSignatures getSignature(long invConfId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_SIGNATURES);
        hql.append(" where invConfigurationId = :invConfId");
        Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
        query.setParameter("invConfId", invConfId);
        return session.getSingleResult(query);
    }

    public List<DBItemInventoryJSInstance> getAllControllers() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
        return session.getResultList(query);
    }

    public List<DBItemInventoryJSInstance> getControllers(Collection<String> controllerIds) throws SOSHibernateException {
        if (controllerIds != null) {
            StringBuilder hql = new StringBuilder(" from ");
            hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
            hql.append(" where controllerId in (:controllerIds)");
            Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
            query.setParameterList("controllerIds", controllerIds);
            return session.getResultList(query);
        } else {
            return new ArrayList<DBItemInventoryJSInstance>();
        }
    }

    public DBItemInventoryJSInstance getController(String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        hql.append(" where controllerId = :controllerId");
        Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        return session.getResultList(query).get(0);
    }

    public Long getActiveClusterControllerDBItemId(String clusterUri) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select id from ");
        hql.append(DBLayer.DBITEM_INV_JS_INSTANCES);
        hql.append(" where clusterUri = :clusterUri");
        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("clusterUri", clusterUri);
        return session.getSingleResult(query);
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem(DBItemInventoryConfiguration invConfig, DBItemInventoryJSInstance controller)
            throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getId(), controller.getControllerId());
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem(DBItemInventoryConfiguration invConfig, String controllerId) throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getId(), controllerId);
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem(Long configurationId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where history.inventoryConfigurationId = :cid");
        hql.append(" and history.controllerId = :controllerId").append(")");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem(DBItemInventoryConfiguration invConfig) throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getPath(), invConfig.getTypeAsEnum());
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem(DBItemDeploymentHistory depHistory) throws SOSHibernateException {
        return getLatestDepHistoryItem(depHistory.getPath(), ConfigurationType.fromValue(depHistory.getType()));
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem(String path, ConfigurationType objectType) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where dep.path = :path");
            hql.append(" and dep.type = :type").append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", objectType.intValue());
            return session.getSingleResult(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItems(List<Config> depConfigsToDelete) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            for (Integer i = 0; i < depConfigsToDelete.size(); i++) {
                hql.append(" where ((").append("dep.folder = : path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7)).append(" and ")
                        .append("dep.type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7)).append(")");
                if (i < depConfigsToDelete.size() - 1) {
                    hql.append(" or ");
                } else if (i == depConfigsToDelete.size() - 1) {
                    hql.append(")");
                }
            }
            hql.append(")");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (Integer i = 0; i < depConfigsToDelete.size(); i++) {
                query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), depConfigsToDelete.get(i).getConfiguration()
                        .getPath());
                query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), depConfigsToDelete.get(i).getConfiguration()
                        .getObjectType().intValue());
            }
            return query.getResultList();
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(DBItemInventoryConfiguration invConfig, DBItemInventoryJSInstance controller)
            throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getId(), controller.getControllerId());
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(DBItemInventoryConfiguration invConfig, String controllerId)
            throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getId(), controllerId);
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(Long configurationId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where dep.inventoryConfigurationId = :cid");
        hql.append(" and dep.controllerId = :controllerId");
        hql.append(" and dep.operation = 0").append(")");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(Long configurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where history.inventoryConfigurationId = :cid");
        hql.append(" and history.state = 0");
        hql.append(" and history.operation = 0").append(")");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        return session.getSingleResult(query);
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(DBItemInventoryConfiguration invConfig) throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getPath(), invConfig.getTypeAsEnum());
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(DBItemDeploymentHistory depHistory) throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(depHistory.getPath(), ConfigurationType.fromValue(depHistory.getType()));
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem(String folder, ConfigurationType objectType) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where dep.folder = :folder");
            hql.append(" and dep.type = :type");
            hql.append(" and dep.operation = 0").append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            query.setParameter("type", objectType.intValue());
            query.setParameter("folder", folder);
            return session.getSingleResult(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder(String folder, String controllerId) {
        return getLatestDepHistoryItemsFromFolder(folder, controllerId, false);
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder(String folder, String controllerId, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (").append("select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where history.state = 0");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (history.folder = :folder or history.folder like :likefolder)");
                }
            } else {
                hql.append(" and history.folder = :folder");
            }
            hql.append(" and history.controllerId = :controllerId").append(" and history.name = dep.name").append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("folder", folder);
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            } else {
                query.setParameter("folder", folder);
            }
            query.setParameter("controllerId", controllerId);
            List<DBItemDeploymentHistory> result = session.getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder(String folder) {
        return getLatestDepHistoryItemsFromFolder(folder, false);
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder(String folder, boolean recursive) {
        try {
            // TODO: improve performance
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (").append("select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where history.state = 0");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (history.folder = :folder or history.folder like :likefolder)");
                }
            } else {
                hql.append(" and history.folder = :folder");
            }
            hql.append(" and history.path = dep.path");
            hql.append(" and history.type = dep.type").append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                    query.setParameter("folder", folder);
                }
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getDepHistoryItemsFromFolder(String folder) {
        return getDepHistoryItemsFromFolder(folder, false);
    }

    public List<DBItemDeploymentHistory> getDepHistoryItemsFromFolder(String folder, boolean recursive) {
        Date getDepHistoryItemsFromFolderStarted = Date.from(Instant.now());
        LOGGER.trace("*** call getDepHistoryItemsFromFolder started ***" + getDepHistoryItemsFromFolderStarted);
        try {
            // TODO: improve performance
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where state = 0");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (folder = :folder or folder like :likefolder)");
                }
            } else {
                hql.append(" and folder = :folder");
            }
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                    query.setParameter("folder", folder);
                }
            } else {
                query.setParameter("folder", folder);
            }
            List<DBItemDeploymentHistory> result = session.getResultList(query);
            Date getDepHistoryItemsFromFolderFinished = Date.from(Instant.now());
            LOGGER.trace("*** call getDepHistoryItemsFromFolder finished ***" + getDepHistoryItemsFromFolderFinished);
            LOGGER.trace("call getDepHistoryItemsFromFolder took: " + (getDepHistoryItemsFromFolderFinished.getTime() - getDepHistoryItemsFromFolderStarted.getTime()) + " ms");
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getDepHistoryItemsFromFolderByType(String folder, Set<Integer> types , boolean recursive) {
        Date getDepHistoryItemsFromFolderStartedByType = Date.from(Instant.now());
        LOGGER.trace("*** call getDepHistoryItemsFromFolderByType started ***" + getDepHistoryItemsFromFolderStartedByType);
        try {
            // TODO: improve performance
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where state = 0");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (folder = :folder or folder like :likefolder)");
                }
            } else {
                hql.append(" and folder = :folder");
            }
            hql.append(" and type in (:types)");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                    query.setParameter("folder", folder);
                }
            } else {
                query.setParameter("folder", folder);
            }
            query.setParameterList("types", types);
            List<DBItemDeploymentHistory> result = session.getResultList(query);
            Date getDepHistoryItemsFromFolderFinishedByType = Date.from(Instant.now());
            LOGGER.trace("*** call getDepHistoryItemsFromFolderByType finished ***" + getDepHistoryItemsFromFolderFinishedByType);
            LOGGER.trace("call getDepHistoryItemsFromFolderByType took: " + (getDepHistoryItemsFromFolderFinishedByType.getTime() - getDepHistoryItemsFromFolderStartedByType.getTime()) + " ms");
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestActiveDepHistoryItemsFromFolder(String folder) {
        return getLatestActiveDepHistoryItemsFromFolder(folder, false);
    }

    public List<DBItemDeploymentHistory> getLatestActiveDepHistoryItemsFromFolder(String folder, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (").append("select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where history.operation = 0");
            if (recursive) {
                if (!"/".equals(folder)) {
                    hql.append(" and (history.folder = :folder or history.folder like :likefolder)");
                }
            } else {
                hql.append(" and history.folder = :folder");
            }
            hql.append(" and history.state = 0").append(" and history.path = dep.path").append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                if (!"/".equals(folder)) {
                    query.setParameter("folder", folder);
                    query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
                }
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolderPerController(String folder, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (").append("select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            if (recursive) {
                hql.append(" where (history.folder = :folder or history.folder like :likefolder)");
            } else {
                hql.append(" where history.folder = :folder");
            }
            hql.append(" and history.controllerId = dep.controllerId").append(" and history.state = 0").append(" and history.path = dep.path").append(
                    ")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            query.setParameter("folder", folder);
            if (recursive) {
                query.setParameter("likefolder", MatchMode.START.toMatchString(folder + "/"));
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getActiveDepHistoryItemsFromFolder(String folder) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where folder = :folder");
            hql.append(" and operation = 0");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            query.setParameter("folder", folder);
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getDeletedDepHistoryItemsFromFolder(String folder) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where folder = :folder");
            hql.append(" and operation = 1");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            query.setParameter("folder", folder);
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestActiveDepHistoryItems(List<Config> depConfigsToDelete) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where dep.operation = 0");
            for (Integer i = 0; i < depConfigsToDelete.size(); i++) {
                hql.append(" and ((").append("dep.folder = : path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7)).append(" and ")
                        .append("dep.type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7)).append(")");
                if (i < depConfigsToDelete.size() - 1) {
                    hql.append(" or ");
                } else if (i == depConfigsToDelete.size() - 1) {
                    hql.append(")");
                }
            }
            hql.append(")");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (Integer i = 0; i < depConfigsToDelete.size(); i++) {
                query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), depConfigsToDelete.get(i).getConfiguration()
                        .getPath());
                query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), depConfigsToDelete.get(i).getConfiguration()
                        .getObjectType().intValue());
            }
            return query.getResultList();
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public Long getLatestDeploymentFromConfigurationId(Long configurationId, Long controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select max(id) from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where inventoryConfigurationId = :configurationId");
        hql.append(" and controllerId = :controllerId");
        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("configurationId", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public List<DBItemDeploymentHistory> updateFailedDeploymentForUpdate(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables, String controllerId, String account, String versionId,
            String errorMessage) {
        List<DBItemDeploymentHistory> depHistoryFailed = new ArrayList<DBItemDeploymentHistory>();
        if (verifiedConfigurations != null) {
            for (DBItemInventoryConfiguration inventoryConfig : verifiedConfigurations.keySet()) {
                DBItemDeploymentHistory newDepHistoryItem = new DBItemDeploymentHistory();
                newDepHistoryItem.setAccount(account);
                newDepHistoryItem.setCommitId(versionId);
                newDepHistoryItem.setContent(inventoryConfig.getContent());
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                newDepHistoryItem.setControllerInstanceId(controllerInstanceId);
                newDepHistoryItem.setControllerId(controllerId);
                newDepHistoryItem.setDeleteDate(null);
                newDepHistoryItem.setDeploymentDate(Date.from(Instant.now()));
                newDepHistoryItem.setInventoryConfigurationId(inventoryConfig.getId());
                DeployType deployType = DeployType.fromValue(inventoryConfig.getType());
                newDepHistoryItem.setType(deployType.intValue());
                newDepHistoryItem.setOperation(OperationType.UPDATE.value());
                newDepHistoryItem.setState(DeploymentState.NOT_DEPLOYED.value());
                newDepHistoryItem.setPath(inventoryConfig.getPath());
                if (inventoryConfig.getName() != null && !inventoryConfig.getName().isEmpty()) {
                    newDepHistoryItem.setName(inventoryConfig.getName());
                } else {
                    newDepHistoryItem.setName(Paths.get(inventoryConfig.getPath()).getFileName().toString());
                }
                newDepHistoryItem.setFolder(inventoryConfig.getFolder());
                newDepHistoryItem.setSignedContent(verifiedConfigurations.get(inventoryConfig).getSignature());
                if (newDepHistoryItem.getSignedContent() == null || newDepHistoryItem.getSignedContent().isEmpty()) {
                    newDepHistoryItem.setSignedContent(".");
                }
                newDepHistoryItem.setInvContent(inventoryConfig.getContent());
                newDepHistoryItem.setErrorMessage(errorMessage);
                // TODO: get Version to set here
                newDepHistoryItem.setVersion(null);
                try {
                    session.save(newDepHistoryItem);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
                depHistoryFailed.add(newDepHistoryItem);
            }
        }
        if (verifiedReDeployables != null) {
            for (DBItemDeploymentHistory deploy : verifiedReDeployables.keySet()) {
                deploy.setSignedContent(verifiedReDeployables.get(deploy).getSignature());
                if (deploy.getSignedContent() == null || deploy.getSignedContent().isEmpty()) {
                    deploy.setSignedContent(".");
                }
                deploy.setId(null);
                deploy.setCommitId(versionId);
                deploy.setAccount(account);
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                deploy.setControllerInstanceId(controllerInstanceId);
                deploy.setControllerId(controllerId);
                deploy.setState(DeploymentState.NOT_DEPLOYED.value());
                deploy.setDeploymentDate(Date.from(Instant.now()));
                deploy.setErrorMessage(errorMessage);
                // TODO: get Version to set here
                deploy.setVersion(null);
                try {
                    session.save(deploy);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
                depHistoryFailed.add(deploy);
            }
        }
        return depHistoryFailed;
    }

    public void updateFailedDeploymentForRedeploy(List<DBItemDeploymentHistory> itemsToUpdate, String controllerId, String account, String versionId,
            String errorMessage) {
        if (itemsToUpdate != null) {
            itemsToUpdate.stream().forEach(item -> {
                item.setState(DeploymentState.NOT_DEPLOYED.value());
                item.setDeploymentDate(Date.from(Instant.now()));
                item.setErrorMessage(errorMessage);
                try {
                    session.update(item);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            });
        }
    }

    public List<DBItemDeploymentHistory> updateFailedDeploymentForImportDeploy(Map<ControllerObject, DBItemDepSignatures> verifiedConfigurations,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables, String controllerId, String account, String versionId,
            String errorMessage) {
        List<DBItemDeploymentHistory> depHistoryFailed = new ArrayList<DBItemDeploymentHistory>();
        if (verifiedConfigurations != null) {
            for (ControllerObject jsObject : verifiedConfigurations.keySet()) {
                DBItemInventoryConfiguration inventoryConfig = null;
                DBItemDeploymentHistory newDepHistoryItem = new DBItemDeploymentHistory();
                newDepHistoryItem.setAccount(account);
                newDepHistoryItem.setCommitId(versionId);
                try {
                    newDepHistoryItem.setContent(Globals.objectMapper.writeValueAsString(jsObject.getContent()));
                    newDepHistoryItem.setInvContent(newDepHistoryItem.getContent());
                } catch (JsonProcessingException e1) {
                    // TODO Auto-generated catch block
                }
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                inventoryConfig = getConfigurationByPath(jsObject.getPath(), ConfigurationType.fromValue(jsObject.getObjectType().intValue()));
                newDepHistoryItem.setControllerInstanceId(controllerInstanceId);
                newDepHistoryItem.setControllerId(controllerId);
                newDepHistoryItem.setDeleteDate(null);
                newDepHistoryItem.setDeploymentDate(Date.from(Instant.now()));
                newDepHistoryItem.setInventoryConfigurationId(inventoryConfig.getId());
                newDepHistoryItem.setType(jsObject.getObjectType().intValue());
                newDepHistoryItem.setOperation(OperationType.UPDATE.value());
                newDepHistoryItem.setState(DeploymentState.NOT_DEPLOYED.value());
                newDepHistoryItem.setPath(inventoryConfig.getPath());
                if (inventoryConfig.getName() != null && !inventoryConfig.getName().isEmpty()) {
                    newDepHistoryItem.setName(inventoryConfig.getName());
                } else {
                    newDepHistoryItem.setName(Paths.get(inventoryConfig.getPath()).getFileName().toString());
                }
                newDepHistoryItem.setFolder(inventoryConfig.getFolder());
                newDepHistoryItem.setSignedContent(verifiedConfigurations.get(jsObject).getSignature());
                if (newDepHistoryItem.getSignedContent() == null || newDepHistoryItem.getSignedContent().isEmpty()) {
                    newDepHistoryItem.setSignedContent(".");
                }
                newDepHistoryItem.setInvContent(inventoryConfig.getContent());
                newDepHistoryItem.setErrorMessage(errorMessage);
                // TODO: get Version to set here
                newDepHistoryItem.setVersion(null);
                try {
                    session.save(newDepHistoryItem);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
                depHistoryFailed.add(newDepHistoryItem);
            }
        }
        if (verifiedReDeployables != null) {
            for (DBItemDeploymentHistory deploy : verifiedReDeployables.keySet()) {
                deploy.setSignedContent(verifiedReDeployables.get(deploy).getSignature());
                if (deploy.getSignedContent() == null || deploy.getSignedContent().isEmpty()) {
                    deploy.setSignedContent(".");
                }
                deploy.setId(null);
                deploy.setCommitId(versionId);
                deploy.setAccount(account);
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                deploy.setControllerInstanceId(controllerInstanceId);
                deploy.setControllerId(controllerId);
                deploy.setState(DeploymentState.NOT_DEPLOYED.value());
                deploy.setDeploymentDate(Date.from(Instant.now()));
                deploy.setErrorMessage(errorMessage);
                // TODO: get Version to set here
                deploy.setVersion(null);
                DBItemInventoryConfiguration inventoryConfig = getConfigurationByPath(deploy.getPath(), ConfigurationType.fromValue(deploy
                        .getType()));
                deploy.setInvContent(inventoryConfig.getContent());
                deploy.setInventoryConfigurationId(inventoryConfig.getId());
                try {
                    session.save(deploy);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
                depHistoryFailed.add(deploy);
            }
        }
        return depHistoryFailed;
    }

    public List<DBItemDeploymentHistory> updateFailedDeploymentForUpdate(List<DBItemDeploymentHistory> reDeployables, String controllerId,
            String account, String errorMessage) {
        List<DBItemDeploymentHistory> depHistoryFailed = new ArrayList<DBItemDeploymentHistory>();
        if (reDeployables != null) {
            for (DBItemDeploymentHistory deploy : reDeployables) {
                deploy.setId(null);
                deploy.setAccount(account);
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                deploy.setControllerInstanceId(controllerInstanceId);
                deploy.setControllerId(controllerId);
                deploy.setState(DeploymentState.NOT_DEPLOYED.value());
                deploy.setDeploymentDate(Date.from(Instant.now()));
                deploy.setErrorMessage(errorMessage);
                if (deploy.getSignedContent() == null || deploy.getSignedContent().isEmpty()) {
                    deploy.setSignedContent(".");
                }
                try {
                    session.save(deploy);
                    // postDeployHistoryWorkflowEvent(deploy);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
                depHistoryFailed.add(deploy);
            }
        }
        return depHistoryFailed;
    }

    public List<DBItemDeploymentHistory> updateFailedDeploymentForUpdate(Map<DBItemInventoryConfiguration, ControllerObject> importedObjects,
            String controllerId, String account, String versionId, String errorMessage) {
        List<DBItemDeploymentHistory> depHistoryFailed;
        try {
            depHistoryFailed = new ArrayList<DBItemDeploymentHistory>();
            for (DBItemInventoryConfiguration inventoryConfig : importedObjects.keySet()) {
                DBItemDeploymentHistory newDepHistoryItem = new DBItemDeploymentHistory();
                newDepHistoryItem.setAccount(account);
                newDepHistoryItem.setCommitId(versionId);
                newDepHistoryItem.setContent(inventoryConfig.getContent());
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                newDepHistoryItem.setControllerInstanceId(controllerInstanceId);
                newDepHistoryItem.setControllerId(controllerId);
                newDepHistoryItem.setDeleteDate(null);
                newDepHistoryItem.setDeploymentDate(Date.from(Instant.now()));
                newDepHistoryItem.setInventoryConfigurationId(inventoryConfig.getId());
                DeployType deployType = DeployType.fromValue(inventoryConfig.getType());
                newDepHistoryItem.setType(deployType.intValue());
                newDepHistoryItem.setOperation(OperationType.UPDATE.value());
                newDepHistoryItem.setState(DeploymentState.NOT_DEPLOYED.value());
                newDepHistoryItem.setPath(inventoryConfig.getPath());
                if (inventoryConfig.getName() != null && !inventoryConfig.getName().isEmpty()) {
                    newDepHistoryItem.setName(inventoryConfig.getName());
                } else {
                    newDepHistoryItem.setName(Paths.get(inventoryConfig.getPath()).getFileName().toString());
                }
                newDepHistoryItem.setFolder(inventoryConfig.getFolder());
                newDepHistoryItem.setSignedContent(importedObjects.get(inventoryConfig).getSignedContent());
                if (newDepHistoryItem.getSignedContent() == null || newDepHistoryItem.getSignedContent().isEmpty()) {
                    newDepHistoryItem.setSignedContent(".");
                }
                newDepHistoryItem.setErrorMessage(errorMessage);
                // TODO: get Version to set here
                newDepHistoryItem.setVersion(null);
                session.save(newDepHistoryItem);
                depHistoryFailed.add(newDepHistoryItem);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return depHistoryFailed;
    }

    public List<DBItemDeploymentHistory> updateFailedDeploymentForDelete(List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete,
            String controllerId, String account, String versionId, String errorMessage) {
        List<DBItemDeploymentHistory> depHistoryFailed = new ArrayList<DBItemDeploymentHistory>();
        try {
            for (DBItemDeploymentHistory deploy : depHistoryDBItemsToDeployDelete) {
                deploy.setId(null);
                deploy.setAccount(account);
                deploy.setCommitId(versionId);
                deploy.setControllerId(controllerId);
                Long controllerInstanceId = 0L;
                try {
                    controllerInstanceId = getController(controllerId).getId();
                } catch (SOSHibernateException e) {
                    continue;
                }
                deploy.setControllerInstanceId(controllerInstanceId);
                deploy.setDeleteDate(Date.from(Instant.now()));
                deploy.setDeploymentDate(Date.from(Instant.now()));
                deploy.setOperation(OperationType.DELETE.value());
                deploy.setState(DeploymentState.NOT_DEPLOYED.value());
                deploy.setErrorMessage(errorMessage);
                if (deploy.getSignedContent() == null || deploy.getSignedContent().isEmpty()) {
                    deploy.setSignedContent(".");
                }
                // TODO: get Version to set here
                session.save(deploy);
                // postDeployHistoryWorkflowEvent(deploy);
                depHistoryFailed.add(deploy);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return depHistoryFailed;
    }

    public void createSubmissionForFailedDeployments(List<DBItemDeploymentHistory> failedDeployments) {
        try {
            for (DBItemDeploymentHistory failedDeploy : failedDeployments) {
                // check if item with same constraint exists
                DBItemDeploymentSubmission submission = getDepSubmission(failedDeploy.getControllerId(), failedDeploy.getCommitId(), failedDeploy
                        .getPath());
                if (submission == null) {
                    submission = new DBItemDeploymentSubmission();
                    submission.setId(null);
                }
                submission.setAccount(failedDeploy.getAccount());
                submission.setCommitId(failedDeploy.getCommitId());
                submission.setContent(failedDeploy.getContent());
                submission.setControllerId(failedDeploy.getControllerId());
                submission.setControllerInstanceId(failedDeploy.getControllerInstanceId());
                submission.setDeletedDate(failedDeploy.getDeleteDate());
                submission.setDepHistoryId(failedDeploy.getId());
                submission.setFolder(failedDeploy.getFolder());
                submission.setInventoryConfigurationId(failedDeploy.getInventoryConfigurationId());
                submission.setType(failedDeploy.getType());
                submission.setOperation(failedDeploy.getOperation());
                submission.setPath(failedDeploy.getPath());
                submission.setSignedContent(failedDeploy.getSignedContent());
                if (submission.getSignedContent() == null || submission.getSignedContent().isEmpty()) {
                    submission.setSignedContent(".");
                }
                submission.setVersion(failedDeploy.getVersion());
                submission.setCreated(Date.from(Instant.now()));
                if (submission.getId() == null) {
                    session.save(submission);
                } else {
                    session.update(submission);
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public DBItemDeploymentSubmission getDepSubmission(String controllerId, String commitId, String path) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_SUBMISSIONS);
            hql.append(" where controllerId = :controllerId");
            hql.append(" and commitId = :commitId");
            hql.append(" and path = :path");
            Query<DBItemDeploymentSubmission> query = session.createQuery(hql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("commitId", commitId);
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public void storeCommitIdForLaterUsage(DBItemInventoryConfiguration config, String commitId) {
        try {
            DBItemDepCommitIds dbCommitId = new DBItemDepCommitIds();
            dbCommitId.setCommitId(commitId);
            dbCommitId.setConfigPath(config.getPath());
            dbCommitId.setInvConfigurationId(config.getId());
            session.save(dbCommitId);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public void storeCommitIdForLaterUsage(DBItemDeploymentHistory depHistory, String commitId) {
        try {
            DBItemDepCommitIds dbCommitId = new DBItemDepCommitIds();
            dbCommitId.setCommitId(commitId);
            dbCommitId.setConfigPath(depHistory.getPath());
            dbCommitId.setInvConfigurationId(depHistory.getInventoryConfigurationId());
            dbCommitId.setDepHistoryId(depHistory.getId());
            session.save(dbCommitId);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public String getVersionId(DBItemInventoryConfiguration config) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("Select commitId from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where invConfigurationId = :confId");
        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("confId", config.getId());
        return session.getSingleResult(query);
    }

    public DBItemDepCommitIds getCommitId(DBItemInventoryConfiguration config) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where invConfigurationId = :confId");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameter("confId", config.getId());
        return session.getSingleResult(query);
    }

    public DBItemDepCommitIds getCommitId(DBItemDeploymentHistory deployment) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where depHistoryId = :depHistoryId");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameter("depHistoryId", deployment.getId());
        return session.getSingleResult(query);
    }

    public void cleanupSignatures(String commitId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("select sig from ").append(DBLayer.DBITEM_DEP_SIGNATURES).append(" as sig ");
        hql.append(" where sig.depHistoryId in (");
        hql.append(" select dep.id from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep ");
        hql.append(" where dep.commitId = :commitId ");
        hql.append(" and dep.controllerId = :controllerId ");
        hql.append(")");
        Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
        query.setParameter("commitId", commitId);
        query.setParameter("controllerId", controllerId);
        List<DBItemDepSignatures> signaturesToDelete = session.getResultList(query);
        if (signaturesToDelete == null) {
            signaturesToDelete = Collections.emptyList();
        }
        for (DBItemDepSignatures sig : signaturesToDelete) {
            session.delete(sig);
        }
    }

//    public void insertNewSignatures (Set<DBItemDepSignatures> signatures) {
//        StringBuilder hql = new StringBuilder();
//        hql.append("select sig from ").append(DBLayer.DBITEM_DEP_SIGNATURES).append(" as sig ");
//        hql.append(" where sig.depHistoryId in (");
//        hql.append(" select dep.id from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep ");
//        hql.append(" where dep.commitId = :commitId ");
//        hql.append(" and dep.controllerId = :controllerId ");
//        hql.append(")");
//        Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
//        query.setParameter("commitId", commitId);
//        query.setParameter("controllerId", controllerId);
//        List<DBItemDepSignatures> signaturesToDelete = session.getResultList(query);
//        
//    }
    
    public void cleanupSignatures(Set<DBItemDepSignatures> signatures) {
        if (signatures != null && !signatures.isEmpty()) {
            for (DBItemDepSignatures sig : signatures) {
                try {
                    session.delete(sig);
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e.getCause());
                }
            }
        }
    }

    public void cleanupCommitIds(String commitId) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
            hql.append(" where commitId = :commitId");
            Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
            query.setParameter("commitId", commitId);
            List<DBItemDepCommitIds> commitIdsToDelete = session.getResultList(query);
            if (commitIdsToDelete != null && !commitIdsToDelete.isEmpty()) {
                for (DBItemDepCommitIds item : commitIdsToDelete) {
                    session.delete(item);
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e.getCause());
        }
    }

    public void createInvConfigurationsDBItemsForFoldersIfNotExists(Set<Path> paths, Long auditLogId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type = :type");
        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.FOLDER.intValue());
        List<String> allExistingFolderNames = session.getResultList(query);
        Set<Path> existingFolderPaths = allExistingFolderNames.stream().map(folder -> Paths.get(folder)).collect(Collectors.toSet());
        Set<Path> pathsWithParents = PublishUtils.updateSetOfPathsWithParents(paths);
        pathsWithParents.removeAll(existingFolderPaths);
        Set<DBItemInventoryConfiguration> newFolders = pathsWithParents.stream().map(folder -> {
            if (!folder.equals(Paths.get("/"))) {
                return createFolderConfiguration(folder, auditLogId);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        for (DBItemInventoryConfiguration folder : newFolders) {
            session.save(folder);
        }
    }

    private DBItemInventoryConfiguration createFolderConfiguration(Path path, Long auditLogId) {
        DBItemInventoryConfiguration folder = new DBItemInventoryConfiguration();
        folder.setType(ConfigurationType.FOLDER.intValue());
        folder.setPath(path.toString().replace('\\', '/'));
        folder.setName(path.getFileName().toString());
        folder.setFolder(path.getParent().toString().replace('\\', '/'));
        folder.setTitle(null);
        folder.setContent(null);
        folder.setValid(true);
        folder.setDeleted(false);
        folder.setDeleted(false);
        folder.setReleased(false);
        folder.setAuditLogId(auditLogId);
        Instant now = Instant.now();
        folder.setCreated(Date.from(now));
        folder.setModified(Date.from(now));
        return folder;
    }

    public List<DBItemDeploymentHistory> getDeploymentHistoryCommits(ShowDepHistoryFilter filter, Collection<String> allowedControllers)
            throws SOSHibernateException {
        if (allowedControllers == null) {
            allowedControllers = Collections.emptySet();
        }
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getCompactFilter(), allowedControllers);
        if (presentFilterAttributes.contains("from") || presentFilterAttributes.contains("to") || presentFilterAttributes.contains("controllerId")) {
            hql.append(presentFilterAttributes.stream().map(item -> {
                if ("from".equals(item)) {
                    return FROM_DEP_DATE;
                } else if ("to".equals(item)) {
                    return TO_DEP_DATE;
                } else if ("limit".equals(item)) {
                    return null;
                } else if ("controllerId".equals(item)) {
                    return "controllerId in (:controllerIds)";
                } else {
                    return item + " = :" + item;
                }
            }).filter(Objects::nonNull).collect(Collectors.joining(" and ", " where ", "")));
        }
        hql.append(" order by deploymentDate desc");
        Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
        for (String item : presentFilterAttributes) {
            switch (item) {
            case "from":
            case "to":
                query.setParameter(item + "Date", FilterAttributesMapper.getValueByFilterAttribute(filter.getCompactFilter(), item),
                        TemporalType.TIMESTAMP);
                break;
            case "deploymentDate":
            case "deleteDate":
                query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getCompactFilter(), item),
                        TemporalType.TIMESTAMP);
                break;
            case "controllerId":
                query.setParameterList("controllerIds", allowedControllers);
                break;
            case "limit":
//                query.setMaxResults((Integer) FilterAttributesMapper.getValueByFilterAttribute(filter.getCompactFilter(), item));
                break;
            default:
                query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getCompactFilter(), item));
                break;
            }
        }
        return getSession().getResultList(query);
    }
    
    public List<DBItemDeploymentHistory> getDeploymentHistoryDetails(ShowDepHistoryFilter filter, Collection<String> allowedControllers)
            throws SOSHibernateException {
        if (allowedControllers == null) {
            allowedControllers = Collections.emptySet();
        }
        if (filter.getDetailFilter() != null) {
            Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getDetailFilter(), allowedControllers);
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            if (presentFilterAttributes.contains("from") || presentFilterAttributes.contains("to") || presentFilterAttributes.contains("controllerId")) {
                hql.append(presentFilterAttributes.stream().map(item -> {
                    if ("from".equals(item)) {
                        return FROM_DEP_DATE;
                    } else if ("to".equals(item)) {
                        return TO_DEP_DATE;
                    } else if ("limit".equals(item)) {
                        return null;
                    } else if ("controllerId".equals(item)) {
                        return "controllerId in (:controllerIds)";
                    } else {
                        return item + " = :" + item;
                    }
                }).filter(Objects::nonNull).collect(Collectors.joining(" and ", " where ", "")));
            }
            hql.append(" order by deploymentDate desc");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (String item : presentFilterAttributes) {
                switch (item) {
                case "from":
                case "to":
                    query.setParameter(item + "Date", FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item),
                            TemporalType.TIMESTAMP);
                    break;
                case "deploymentDate":
                case "deleteDate":
                    query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item),
                            TemporalType.TIMESTAMP);
                    break;
                case "controllerId":
                    query.setParameterList("controllerIds", allowedControllers);
                    break;
                case "limit":
                    query.setMaxResults((Integer) FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item));
                    break;
                default:
                    query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item));
                    break;
                }
            }
            return getSession().getResultList(query);
        } else {
            return Collections.emptyList();
        }
    }

    public List<DBItemInventoryAgentInstance> getAllAgents() {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            Query<DBItemInventoryAgentInstance> query = getSession().createQuery(hql.toString());
            return query.getResultList();
        }catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e.getMessage(), e);
        }
    }
    
    public String getAgentIdFromAgentName(String agentName, String controllerId) {
        return getAgentIdFromAgentName(agentName, controllerId, null, null);
    }

    public String getAgentIdFromAgentName(String agentName, String controllerId, String workflowPath, String jobname) {
        if (agentName != null) {
            try {
                StringBuilder hql = new StringBuilder("select instance.agentId from ");
                hql.append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(" as instance ").append(" left join ").append(DBLayer.DBITEM_INV_AGENT_NAMES)
                        .append(" as aliases").append(" on instance.agentId = aliases.agentId").append(
                                " where instance.controllerId = :controllerId and (").append(" instance.agentName = :agentName or ").append(
                                        " aliases.agentName = :agentName)").append(" group by instance.agentId");
                Query<String> query = getSession().createQuery(hql.toString());
                query.setParameter("agentName", agentName);
                query.setParameter("controllerId", controllerId);
                return query.getSingleResult();
            } catch (NoResultException e) {
                if (workflowPath != null && jobname != null) {
                    throw new JocSosHibernateException(String.format(
                            "No agentId found for agentName=\"%1$s\" and controllerId=\"%2$s\" in Workflow \"%3$s\" and Job \"%4$s\"!", agentName,
                            controllerId, workflowPath, jobname), e);
                } else {
                    throw new JocSosHibernateException(String.format("No agentId found for agentName=\"%1$s\" and controllerId=\"%2$s\"!", agentName,
                            controllerId), e);
                }

            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    public DBItemInventoryConfiguration getInvConfigurationFolder(String path) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path").append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryConfiguration> getInvConfigurationFolders(String path, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (recursive) {
                hql.append(" where (path = :path or path like :likepath)");
            } else {
                hql.append(" where path = :path");
            }
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            if (recursive) {
                query.setParameter("likepath", MatchMode.START.toMatchString(path + "/"));
            }
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedConfigurations(String folder) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where folder = :folder");
            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("folder", folder);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryConfiguration> getReleasableConfigurations(String folder) {
        Set<Integer> types = JocInventory.getReleasableTypes();
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where folder = :folder");
            hql.append(" and type in (:types)");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("folder", folder);
            query.setParameterList("types", types);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public String getOriginalContent(String path, ConfigurationType type) {
        try {
            StringBuilder hql = new StringBuilder("select content from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            hql.append(" and type = :type");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type.intValue());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    private boolean checkAgentNamePresent(String agentName) {
        try {
            StringBuilder instanceHql = new StringBuilder("select agentName from ").append(DBLayer.DBITEM_INV_AGENT_INSTANCES);
            instanceHql.append(" where agentName = :agentName");
            Query<String> instanceQuery = getSession().createQuery(instanceHql.toString());
            instanceQuery.setParameter("agentName", agentName);
            List<String> instanceResults = instanceQuery.getResultList();
            if (instanceResults != null && !instanceResults.isEmpty()) {
                return true;
            } else {
                StringBuilder aliasHql = new StringBuilder("select agentName from ").append(DBLayer.DBITEM_INV_AGENT_NAMES);
                aliasHql.append(" where agentName = :agentName");
                Query<String> aliasQuery = getSession().createQuery(aliasHql.toString());
                aliasQuery.setParameter("agentName", agentName);
                List<String> aliasResults = aliasQuery.getResultList();
                if (aliasResults != null && !aliasResults.isEmpty()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryCertificate> getCaCertificates() {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CERTS);
            hql.append(" where ca = true");
            Query<DBItemInventoryCertificate> query = getSession().createQuery(hql.toString());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemInventoryConfiguration> getUsedWorkflowsByLockId(String lockId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where type=:type ");
        hql.append(" and content like :lockId");
        Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.WORKFLOW.intValue());
        query.setParameter("lockId", "%\"" + lockId + "\"%");
        List<DBItemInventoryConfiguration> results = session.getResultList(query);
        if (results != null) {
            return results;
        } else {
            return Collections.emptyList();
        }
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowPath(String workflowPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        // hql.append("and ");
        hql.append(" and content like :workflowPath");
        // hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowPath");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowPath", "%\"" + workflowPath + "\"%");
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append(" and content like :workflowName");
        // hql.append("and ");
        // hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowName")).append("=:workflowName");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("workflowName", "%\"" + workflowName + "\"%");
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedFileOrderSourcesByWorkflowName(String workflowName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append(" and content like :workflowName");
        // hql.append("and ");
        // hql.append(SOSHibernateJsonValue.getFunction(ReturnType.SCALAR, "jsonContent", "$.workflowPath")).append("=:workflowName");

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.FILEORDERSOURCE.intValue());
        query.setParameter("workflowName", "%\"" + workflowName + "\"%");
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByCalendarPath(String calendarPath) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append(" and content like :calendarPath");
        // hql.append("and ");
        // String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars");
        // hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarPath"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("calendarPath", "%\"" + calendarPath + "\"%");
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryConfiguration> getUsedSchedulesByCalendarName(String calendarName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append(" and content like :calendarName");
        // hql.append("and ");
        // String jsonFunc = SOSHibernateJsonValue.getFunction(ReturnType.JSON, "jsonContent", "$.calendars");
        // hql.append(SOSHibernateRegexp.getFunction(jsonFunc, ":calendarName"));

        Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        query.setParameter("calendarName", "%\"" + calendarName + "\"%");
        return getSession().getResultList(query);
    }
    
    public boolean checkCommitIdAlreadyExists (String commitId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" ");
        hql.append("where controllerId = :controllerId");
        hql.append(" and commitId = :commitId");
        Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", controllerId);
        query.setParameter("commitId", commitId);
        List<DBItemDeploymentHistory> results = getSession().getResultList(query);
        if (results == null || results.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public Map<String, String> getReleasedScripts() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" where type=:type");
        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.INCLUDESCRIPT.intValue());
        List<DBItemInventoryReleasedConfiguration> result = getSession().getResultList(query);
        if (result == null) {
            return Collections.emptyMap();
        }
        return result.stream().collect(Collectors.toMap(DBItemInventoryReleasedConfiguration::getName,
                DBItemInventoryReleasedConfiguration::getContent, (name, content) -> name));
    }
    
    public void insertSignaturesInBatch(List<DBItemDepSignatures> signatures) {
        try {
            // prepare items
            BatchPreparator result = DBSQLBatchPreparator.prepareForSQLBatchInsert(session.getFactory().getDialect(), signatures);
            session.getSQLExecutor().executeBatch(result.getTableName(), result.getSQL(), result.getRows());
        } catch (SOSHibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void insertNewHistoryEntriesInBatch(List<DBItemDeploymentHistory> depHistory) throws SOSHibernateException {
        // prepare items
        BatchPreparator result = DBSQLBatchPreparator.prepareForSQLBatchInsert(session.getFactory().getDialect(), depHistory);
        session.getSQLExecutor().executeBatch(result.getTableName(), result.getSQL(), result.getRows());
    }

    public void updateIdForDBItemDepSignature(DBItemDepSignatures dbItemDepSignatures) {
        // needed as batch stored items do not support id update
        try {
            StringBuilder hql = new StringBuilder("select sig from ").append(DBLayer.DBITEM_DEP_SIGNATURES).append(" as sig");
            hql.append(" where sig.id = (").append("select max(sig2.id) from ").append(DBLayer.DBITEM_DEP_SIGNATURES).append(" as sig2");
            hql.append(" where sig2.invConfigurationId = :invConfigurationId");
            hql.append(" and sig2.signature = :signature");
            hql.append(" and sig2.account = :account");
            hql.append(" and sig2.modified = :modified)");
            Query<DBItemDepSignatures> query = getSession().createQuery(hql.toString());
            query.setParameter("invConfigurationId", dbItemDepSignatures.getInvConfigurationId());
            query.setParameter("signature", dbItemDepSignatures.getSignature());
            query.setParameter("account", dbItemDepSignatures.getAccount());
            query.setParameter("modified", dbItemDepSignatures.getModified());
            DBItemDepSignatures signature = getSession().getSingleResult(query);
            if (signature != null) {
                dbItemDepSignatures.setId(signature.getId());
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

}