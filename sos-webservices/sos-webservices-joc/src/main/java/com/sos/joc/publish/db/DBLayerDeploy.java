package com.sos.joc.publish.db;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.TemporalType;

import org.hibernate.criterion.MatchMode;
import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDepCommitIds;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.deployment.DBItemDeploymentSubmission;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.impl.ValidateResourceImpl;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.jobclass.JobClassPublish;
import com.sos.joc.model.inventory.junction.JunctionPublish;
import com.sos.joc.model.inventory.lock.LockPublish;
import com.sos.joc.model.inventory.workflow.WorkflowPublish;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.DeployablesFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.ExcludeConfiguration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.ReleasablesFilter;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.model.publish.SetVersionsFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.mapper.FilterAttributesMapper;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.exception.SOSJsonSchemaException;

public class DBLayerDeploy {

    private final SOSHibernateSession session;
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
    private static final String FROM_DEP_DATE = "deploymentDate >= :fromDate"; 
    private static final String TO_DEP_DATE = "deploymentDate < :toDate"; 


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
    
    public List<DBItemDepVersions> getVersions (Long invConfigurationId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_DEP_VERSIONS);
        hql.append(" where invConfigurationId = :invConfigurationId");
        Query<DBItemDepVersions> query = session.createQuery(hql.toString());
        query.setParameter("invConfigurationId", invConfigurationId);
        return session.getResultList(query);
    }

    public DBItemDepSignatures getSignature (Long inventoryConfigurationId) throws DBConnectionRefusedException,
            DBInvalidDataException {
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

    public List<DBItemDeploymentHistory> getDeployedConfigurations (Long inventoryConfigurationId) throws DBConnectionRefusedException,
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

    public List<DBItemDeploymentHistory> getDeployedConfigurationByPathAndType(String path, Integer type)
            throws DBConnectionRefusedException, DBInvalidDataException {
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

    public List<Long> getDeployableInventoryConfigurationIdsByFolder(String folder, boolean recursive)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select id from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (recursive) {
                sql.append(" where folder like :folder");
            } else {
                sql.append(" where folder = :folder");
            }
            sql.append(" and type in (:types)");  
            Query<Long> query = session.createQuery(sql.toString());
            if (recursive) {
                query.setParameter("folder", MatchMode.START.toMatchString(folder));
            } else {
                query.setParameter("folder", folder);
            }
            query.setParameterList("types", Arrays.asList(new Integer[] {
                    ConfigurationType.WORKFLOW.intValue(), 
                    ConfigurationType.JUNCTION.intValue(),
                    ConfigurationType.JOBCLASS.intValue(),
                    ConfigurationType.LOCK.intValue()}));
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getInventoryConfigurationIdByPathAndType(String path, Integer type)
            throws DBConnectionRefusedException, DBInvalidDataException {
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

    public List<DBItemDeploymentHistory> getFilteredDeploymentsForSetVersion(SetVersionFilter filter)
            throws DBConnectionRefusedException, DBInvalidDataException {
        return getFilteredDeployments(filter);
    }

    public List<DBItemInventoryConfiguration> getAllInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, false, false, false);
    }

    public List<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, true, false, false, false);
    }

    public List<DBItemInventoryConfiguration> getValidDeployableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, true, false, false, true);
    }

    public List<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, true, false, recursive, false);
    }

    public List<DBItemInventoryConfiguration> getValidDeployableInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, true, false, recursive, true);
    }

    public List<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, true, false, false);
    }

    public List<DBItemInventoryConfiguration> getValidReleasableInventoryConfigurationsByFolder(String folder) {
        return getInventoryConfigurationsByFolder(folder, false, true, false, true);
    }

    public List<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, false, true, recursive, false);
    }

    public List<DBItemInventoryConfiguration> getValidReleasableInventoryConfigurationsByFolder(String folder, boolean recursive) {
        return getInventoryConfigurationsByFolder(folder, false, true, recursive, true);
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsByFolder(String folder) {
        return getReleasedInventoryConfigurationsByFolder(folder, false);
    }

    public List<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsByFolder(String folder, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append(" from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            if (recursive) {
                hql.append(" where folder like :folder");
            } else {
                hql.append(" where folder = :folder");
            }
            Query<DBItemInventoryReleasedConfiguration> query = session.createQuery(hql.toString());
            if (recursive) {
                query.setParameter("folder", MatchMode.START.toMatchString(folder));
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
        
    private List<DBItemInventoryConfiguration> getInventoryConfigurationsByFolder(String folder, boolean onlyDeployables, boolean onlyReleasables,
            boolean recursive, boolean onlyValid) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (recursive) {
                hql.append(" where folder like :folder");
            } else {
                hql.append(" where folder = :folder");
            }
            if (onlyDeployables || onlyReleasables) {
              hql.append(" and type in (:types)");  
            } 
            if (onlyValid) {
                hql.append(" and valid = true");
            }
            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            if (recursive) {
                query.setParameter("folder", MatchMode.START.toMatchString(folder));
            } else {
                query.setParameter("folder", folder);
            }
            if (onlyDeployables) {
                query.setParameterList("types", Arrays.asList(new Integer[] {
                        ConfigurationType.WORKFLOW.intValue(), 
                        ConfigurationType.JUNCTION.intValue(),
                        ConfigurationType.JOBCLASS.intValue(),
                        ConfigurationType.LOCK.intValue()}));
            } else if (onlyReleasables) {
                query.setParameterList("types", Arrays.asList(new Integer[] {
                        ConfigurationType.SCHEDULE.intValue(), 
                        ConfigurationType.WORKINGDAYSCALENDAR.intValue(),
                        ConfigurationType.NONWORKINGDAYSCALENDAR.intValue()}));
            }
            return session.getResultList(query);
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
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where ");
            for (Integer i=0; i < configurations.size(); i++) {
                hql.append("(")
                    .append("path = :path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(")");
                if (i < configurations.size() -1) {
                    hql.append(" or ");
                }
            }
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            for (Integer i=0; i < configurations.size(); i++) {
                query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), configurations.get(i).getPath());
                query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), configurations.get(i).getObjectType().intValue());
            }
            return query.getResultList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemInventoryReleasedConfiguration> getFilteredReleasedConfiguration(List<Configuration> configurations)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where ");
            for (Integer i=0; i < configurations.size(); i++) {
                hql.append("(")
                    .append("path = :path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(")");
                if (i < configurations.size() -1) {
                    hql.append(" or ");
                }
            }
            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            for (Integer i=0; i < configurations.size(); i++) {
                query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), configurations.get(i).getPath());
                query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), configurations.get(i).getObjectType().intValue());
            }
            return query.getResultList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemInventoryConfiguration> getFilteredInventoryConfigurationsByIds(Collection<Long> configurationIds)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (!configurationIds.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                sql.append(" where id in (:ids)");
                Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                query.setParameterList("ids", configurationIds);
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

    public List<DBItemDeploymentHistory> getFilteredDeploymentHistory(List<Configuration> deployConfigurations)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where ");
            for (Integer i=0; i < deployConfigurations.size(); i++) {
                hql.append("(")
                    .append("path = : path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("commitId = :commitId").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(")");
                if (i < deployConfigurations.size() -1) {
                    hql.append(" or ");
                }
            }
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (Integer i=0; i < deployConfigurations.size(); i++) {
                query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), deployConfigurations.get(i).getPath());
                query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), deployConfigurations.get(i).getObjectType().intValue());
                query.setParameter("commitId" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), deployConfigurations.get(i).getCommitId());
            }
            return query.getResultList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeploymentHistoryToDelete(List<Configuration> deployConfigurations)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where ");
            for (Integer i=0; i < deployConfigurations.size(); i++) {
                hql.append("(")
                    .append("path = : path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(")");
                if (i < deployConfigurations.size() -1) {
                    hql.append(" or ");
                }
            }
            hql.append(" group by path");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            for (Integer i=0; i < deployConfigurations.size(); i++) {
                query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), deployConfigurations.get(i).getPath());
                query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), deployConfigurations.get(i).getObjectType().intValue());
            }
            return query.getResultList();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeploymentHistory(Collection<Long> deployIds)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (deployIds != null && !deployIds.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
                sql.append(" where id in (:ids)");
                Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
                query.setParameterList("ids", deployIds);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemDeploymentHistory>();
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
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList());
        if(!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }

    public List<DBItemInventoryConfiguration> getFilteredDeployableConfigurations(DeployablesValidFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDraftConfigurations().stream()
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList());
        if(!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }
        
    public List<DBItemInventoryConfiguration> getFilteredReleasableConfigurations(ReleasablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDraftConfigurations().stream()
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredInventoryConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryConfiguration>();
        }
    }

    public List<DBItemInventoryReleasedConfiguration> getFilteredReleasedConfigurations(ReleasablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getReleasedConfigurations().stream()
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredReleasedConfiguration(configurations);
        } else {
            return new ArrayList<DBItemInventoryReleasedConfiguration>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(DeployablesFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDeployConfigurations().stream()
                .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredDeploymentHistory(configurations);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(DeployablesValidFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        List<Configuration> configurations = filter.getDeployConfigurations().stream()
                .filter(item -> !item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList());
        if (!configurations.isEmpty()) {
            return getFilteredDeploymentHistory(configurations);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(SetVersionFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeploymentHistory(
                filter.getDeployConfigurations().stream()
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList()));
    }

    public List<DBItemDeploymentHistory> getFilteredDeployments(SetVersionsFilter filter) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getFilteredDeploymentHistory(
                filter.getDeployConfigurations().stream()
                .map(item -> item.getConfiguration())
                .collect(Collectors.toList()));
    }

    public List<DBItemInventoryConfiguration> getFilteredConfigurations(List<Long> ids) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (ids != null && !ids.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
                sql.append(" where id in (:ids)");
                Query<DBItemInventoryConfiguration> query = session.createQuery(sql.toString());
                query.setParameterList("ids", ids);
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

    public List<DBItemDeploymentHistory> getFilteredDeployments(List<Long> ids) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            if (ids != null && !ids.isEmpty()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" from ").append(DBLayer.DBITEM_DEP_HISTORY);
                sql.append(" where id in (:ids)");
                Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
                query.setParameterList("ids", ids);
                return session.getResultList(query);
            } else {
                return new ArrayList<DBItemDeploymentHistory>();
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
        
    public DBItemDeploymentHistory getDeployedConfiguration(String path, Integer type) throws DBConnectionRefusedException,
            DBInvalidDataException {
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

    public void saveOrUpdateInventoryConfiguration(ConfigurationObject configuration, String account, Long auditLogId,
            String folder) {
        saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, false, folder);
    }
    
    public void saveOrUpdateInventoryConfiguration(ConfigurationObject configuration, String account, Long auditLogId,
            boolean overwrite) {
        saveOrUpdateInventoryConfiguration(configuration, account, auditLogId, overwrite, null);   
    }
    
    public void saveOrUpdateInventoryConfiguration(ConfigurationObject configuration, String account, Long auditLogId,
            boolean overwrite, String folder) {
        try {
            DBItemInventoryConfiguration existingConfiguration = null;
            StringBuilder hql = new StringBuilder(" from ");
            hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
            query.setParameter("type", configuration.getObjectType().intValue());
            query.setParameter("path", configuration.getPath());
            existingConfiguration = session.getSingleResult(query);
            boolean valid = false;
            try {
                ValidateResourceImpl.validate(configuration.getObjectType(), Globals.objectMapper.writeValueAsBytes(configuration.getConfiguration()));
                valid = true;
            } catch (SOSJsonSchemaException | IOException e) {
                valid = false;
            }
            if (overwrite) {
                if (existingConfiguration != null) {
                    existingConfiguration.setModified(Date.from(Instant.now()));
                    existingConfiguration.setContent(om.writeValueAsString(configuration.getConfiguration()));
                    existingConfiguration.setAuditLogId(auditLogId);
                    existingConfiguration.setValid(valid);
                    existingConfiguration.setDeployed(false);
                    session.update(existingConfiguration);
                } else {
                    DBItemInventoryConfiguration newConfiguration = new DBItemInventoryConfiguration();
                    Date now = Date.from(Instant.now());
                    newConfiguration.setModified(now);
                    newConfiguration.setCreated(now);
                    newConfiguration.setContent(om.writeValueAsString(configuration.getConfiguration()));
                    newConfiguration.setPath(configuration.getPath());
                    newConfiguration.setFolder(Paths.get(configuration.getPath()).getParent().toString().replace('\\', '/'));
                    newConfiguration.setName(Paths.get(newConfiguration.getPath()).getFileName().toString());
                    newConfiguration.setType(configuration.getObjectType());
                    newConfiguration.setAuditLogId(auditLogId);
                    newConfiguration.setDocumentationId(0L);
                    newConfiguration.setDeployed(false);
                    newConfiguration.setReleased(false);
                    newConfiguration.setValid(valid);
                    session.save(newConfiguration);
                }
            } else {
                if (existingConfiguration == null) {
                    DBItemInventoryConfiguration newConfiguration = new DBItemInventoryConfiguration();
                    Date now = Date.from(Instant.now());
                    newConfiguration.setModified(now);
                    newConfiguration.setCreated(now);
                    newConfiguration.setContent(om.writeValueAsString(configuration.getConfiguration()));
                    newConfiguration.setPath(configuration.getPath());
                    newConfiguration.setFolder(Paths.get(configuration.getPath()).getParent().toString().replace('\\', '/'));
                    newConfiguration.setName(Paths.get(newConfiguration.getPath()).getFileName().toString());
                    newConfiguration.setType(configuration.getObjectType());
                    newConfiguration.setAuditLogId(auditLogId);
                    newConfiguration.setDocumentationId(0L);
                    newConfiguration.setDeployed(false);
                    newConfiguration.setReleased(false);
                    newConfiguration.setValid(valid);
                    session.save(newConfiguration);
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } catch (JsonProcessingException e) {
            throw new JocException(e);
        }
    }
    
    public DBItemInventoryConfiguration getConfigurationByPath(String path, ConfigurationType type) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type.intValue());
            return query.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public DBItemInventoryConfiguration getConfigurationByPath(String path, Integer type) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type);
            return query.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public DBItemInventoryConfiguration getConfigurationByName(String name, ConfigurationType type) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where name = :name");
            hql.append(" and type = :type");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("name", name);
            query.setParameter("type", type.intValue());
            return query.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
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
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public DBItemInventoryConfiguration saveOrUpdateInventoryConfiguration(String path, ControllerObject jsObject, DeployType type, String account, Long auditLogId)
            throws SOSHibernateException, JsonProcessingException {
        StringBuilder hql = new StringBuilder(" from ");
        hql.append(DBLayer.DBITEM_INV_CONFIGURATIONS);
        hql.append(" where path = :path");
        Query<DBItemInventoryConfiguration> query = session.createQuery(hql.toString());
        query.setParameter("path", path);
        DBItemInventoryConfiguration existingJsObject = session.getSingleResult(query);
        Path folderPath = null;
        String name = null;
        if (existingJsObject != null) {
            existingJsObject.setModified(Date.from(Instant.now()));
            switch (type) {
            case WORKFLOW:
                // Why cast WorkflowPublish?
                existingJsObject.setContent(om.writeValueAsString(((WorkflowPublish) jsObject).getContent()));
                existingJsObject.setAuditLogId(auditLogId);
                existingJsObject.setDocumentationId(0L);
                existingJsObject.setDeployed(false);
                // save or update signature in different Table
                if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                    saveOrUpdateSignature(existingJsObject.getId(), jsObject, account, type);
                }
                break;
            case LOCK:
                // TODO: 
                break;
            case JUNCTION:
                // TODO: 
                break;
            default:
                break;
            }
            session.update(existingJsObject);
            return existingJsObject;
        } else {
            DBItemInventoryConfiguration newJsObject = new DBItemInventoryConfiguration();
            Date now = Date.from(Instant.now());
            newJsObject.setModified(now);
            newJsObject.setCreated(now);
            switch (type) {
            case WORKFLOW:
                // Why cast WorkflowPublish?
                newJsObject.setContent(om.writeValueAsString(((WorkflowPublish) jsObject).getContent()));
                folderPath = Paths.get(((WorkflowPublish) jsObject).getContent().getPath() + ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION).getParent();
                newJsObject.setFolder(folderPath.toString().replace('\\', '/'));
                newJsObject.setPath(((WorkflowPublish) jsObject).getContent().getPath());
                name = Paths.get(((WorkflowPublish) jsObject).getContent().getPath()).getFileName().toString();
                newJsObject.setName(name);
                newJsObject.setType(ConfigurationType.WORKFLOW);
                newJsObject.setAuditLogId(auditLogId);
                newJsObject.setDocumentationId(0L);
                newJsObject.setDeployed(false);
                newJsObject.setReleased(false);
                session.save(newJsObject);
                // save or update signature in different Table
                if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                    saveOrUpdateSignature(newJsObject.getId(), jsObject, account, type);
                }
                break;
            case LOCK:
                // TODO: 
                break;
            case JUNCTION:
                // TODO: 
                break;
            default:
                break;
            }
            return newJsObject;
        }
    }
    
    public DBItemDepSignatures saveOrUpdateSignature (Long invConfId, ControllerObject jsObject, String account, DeployType type) throws SOSHibernateException {
        DBItemDepSignatures dbItemSig = getSignature(invConfId);
        String signature = null;
        signature = jsObject.getSignedContent();
    //        switch (type) {
    //            case WORKFLOW:
    //                // Why cast WorkflowPublish?
    //                signature = ((WorkflowPublish) jsObject).getSignedContent();
    //                break;
    //            case JUNCTION:
    //            default:
    //                throw new JocNotImplementedException();
    //        }
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
    
    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemInventoryConfiguration invConfig, DBItemInventoryJSInstance controller)
            throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getId(), controller.getControllerId());
    }
    
    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemInventoryConfiguration invConfig, String controllerId)
            throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getId(), controllerId);
    }
    
    public DBItemDeploymentHistory getLatestDepHistoryItem (Long configurationId, String controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
        hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
        hql.append(" where dep.inventoryConfigurationId = :cid");
        hql.append(" and dep.controllerId = :controllerId").append(")");
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        query.setParameter("cid", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemInventoryConfiguration invConfig) throws SOSHibernateException {
        return getLatestDepHistoryItem(invConfig.getPath(), invConfig.getTypeAsEnum());
    }
   
    public DBItemDeploymentHistory getLatestDepHistoryItem (DBItemDeploymentHistory depHistory) throws SOSHibernateException {
        return getLatestDepHistoryItem(depHistory.getPath(), ConfigurationType.fromValue(depHistory.getType()));
    }
   
    public DBItemDeploymentHistory getLatestDepHistoryItem (String path, ConfigurationType objectType) {
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

    public List<DBItemDeploymentHistory> getLatestDepHistoryItems (List<Config> depConfigsToDelete) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            for (Integer i=0; i < depConfigsToDelete.size(); i++) {
                hql.append(" where ((")
                    .append("dep.folder = : path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("dep.type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(")");
                if (i < depConfigsToDelete.size()-1) {
                    hql.append(" or ");
                } else if (i == depConfigsToDelete.size()-1) {
                    hql.append(")");
                }
            }
            hql.append(")");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
             for (Integer i=0; i < depConfigsToDelete.size(); i++) {
                 query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), 
                         depConfigsToDelete.get(i).getConfiguration().getPath());
                 query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), 
                         depConfigsToDelete.get(i).getConfiguration().getObjectType().intValue());
             }
             return query.getResultList();
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (DBItemInventoryConfiguration invConfig, DBItemInventoryJSInstance controller)
            throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getId(), controller.getControllerId());
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (DBItemInventoryConfiguration invConfig, String controllerId)
            throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getId(), controllerId);
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (Long configurationId, String controllerId) throws SOSHibernateException {
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

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (DBItemInventoryConfiguration invConfig) throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(invConfig.getPath(), invConfig.getTypeAsEnum());
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (DBItemDeploymentHistory depHistory) throws SOSHibernateException {
        return getLatestActiveDepHistoryItem(depHistory.getPath(), ConfigurationType.fromValue(depHistory.getType()));
    }

    public DBItemDeploymentHistory getLatestActiveDepHistoryItem (String folder, ConfigurationType objectType) {
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

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder (String folder, String controllerId) {
         return getLatestDepHistoryItemsFromFolder(folder, controllerId, false);
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder (String folder, String controllerId, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ")
                    .append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (")
                .append("select max(history.id) from ")
                .append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            if (recursive) {
                hql.append(" where history.folder like :folder");
            } else {
                hql.append(" where history.folder = :folder");
            }
            hql.append(" and history.controllerId = :controllerId")
                .append(" and history.state = 0")
                .append(" and history.path = dep.path group by history.path")
                .append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                query.setParameter("folder", MatchMode.START.toMatchString(folder));
            } else {
                query.setParameter("folder", folder);
            }
            query.setParameter("controllerId", controllerId);
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder (String folder) {
        return getLatestDepHistoryItemsFromFolder(folder, false);
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolder (String folder, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ")
                    .append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (")
                .append("select max(history.id) from ")
                .append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            if (recursive) {
                hql.append(" where history.folder like :folder");
            } else {
                hql.append(" where history.folder = :folder");
            }
            hql.append(" and history.state = 0")
                .append(" and history.path = dep.path group by history.path")
                .append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                query.setParameter("folder", MatchMode.START.toMatchString(folder));
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getLatestDepHistoryItemsFromFolderPerController (String folder, boolean recursive) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ")
                    .append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (")
                .append("select max(history.id) from ")
                .append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            if (recursive) {
                hql.append(" where history.folder like :folder");
            } else {
                hql.append(" where history.folder = :folder");
            }
            hql.append(" and history.controllerId = dep.controllerId")
                .append(" and history.state = 0")
                .append(" and history.path = dep.path group by history.path")
                .append(")");
            Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
            if (recursive) {
                query.setParameter("folder", MatchMode.START.toMatchString(folder));
            } else {
                query.setParameter("folder", folder);
            }
            return session.getResultList(query);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public List<DBItemDeploymentHistory> getActiveDepHistoryItemsFromFolder (String folder) {
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

    public List<DBItemDeploymentHistory> getDeletedDepHistoryItemsFromFolder (String folder) {
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

    public List<DBItemDeploymentHistory> getLatestActiveDepHistoryItems (List<Config> depConfigsToDelete) {
        try {
            StringBuilder hql = new StringBuilder("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (select max(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history");
            hql.append(" where dep.operation = 0");
            for (Integer i=0; i < depConfigsToDelete.size(); i++) {
                hql.append(" and ((")
                    .append("dep.folder = : path").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(" and ")
                    .append("dep.type = :type").append(PublishUtils.getValueAsStringWithleadingZeros(i, 7))
                    .append(")");
                if (i < depConfigsToDelete.size()-1) {
                    hql.append(" or ");
                } else if (i == depConfigsToDelete.size()-1) {
                    hql.append(")");
                }
            }
            hql.append(")");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
             for (Integer i=0; i < depConfigsToDelete.size(); i++) {
                 query.setParameter("path" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), 
                         depConfigsToDelete.get(i).getConfiguration().getPath());
                 query.setParameter("type" + PublishUtils.getValueAsStringWithleadingZeros(i, 7), 
                         depConfigsToDelete.get(i).getConfiguration().getObjectType().intValue());
             }
             return query.getResultList();
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public Long getLatestDeploymentFromConfigurationId(Long configurationId, Long controllerId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select max(id) from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(" where inventoryConfigurationId = :configurationId");
        hql.append(" and controllerId = :controllerId group by id");
        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("configurationId", configurationId);
        query.setParameter("controllerId", controllerId);
        return session.getSingleResult(query);
    }

    public List<Long> getLatestDeploymentFromConfigurationId(Set<Long> configurationIds, String controllerId) throws SOSHibernateException {
        if (configurationIds != null && !configurationIds.isEmpty()) {
            StringBuilder hql = new StringBuilder("select max(id) from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(" where inventoryConfigurationId in (:configurationIds)");
            hql.append(" and controllerId = :controllerId group by id");
            Query<Long> query = session.createQuery(hql.toString());
            query.setParameter("configurationIds", configurationIds);
            query.setParameter("controllerId", controllerId);
            return session.getResultList(query);
        } else {
            return new ArrayList<Long>();
        }
    }
        
    public List<DBItemDeploymentHistory> updateFailedDeploymentForUpdate(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables, 
            String controllerId, String account, String versionId, String errorMessage) {
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
                DeployType deployType = PublishUtils.mapConfigurationType(ConfigurationType.fromValue(inventoryConfig.getType()));
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
    
    public List<DBItemDeploymentHistory> updateFailedDeploymentForImportDeploy(
            Map<ControllerObject, DBItemDepSignatures> verifiedConfigurations, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedReDeployables, 
            String controllerId, String account, String versionId, String errorMessage) {
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
                switch (jsObject.getObjectType()) {
                case WORKFLOW:
                    inventoryConfig = getConfigurationByPath(((WorkflowPublish)jsObject).getContent().getPath(), ConfigurationType.WORKFLOW);
                    break;
                case LOCK:
                    inventoryConfig = getConfigurationByPath(((LockPublish)jsObject).getPath(), ConfigurationType.LOCK);
                    break;
                case JUNCTION:
                    inventoryConfig = getConfigurationByPath(((JunctionPublish)jsObject).getContent().getPath(), ConfigurationType.JUNCTION);
                    break;
                case JOBCLASS:
                    inventoryConfig = getConfigurationByPath(((JobClassPublish)jsObject).getContent().getPath(), ConfigurationType.JOBCLASS);
                    break;
                }
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
                DBItemInventoryConfiguration inventoryConfig = getConfigurationByPath(deploy.getPath(), ConfigurationType.fromValue(deploy.getType()));
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
    
    public List<DBItemDeploymentHistory> updateFailedDeploymentForUpdate(
            List<DBItemDeploymentHistory> reDeployables, String controllerId, String account, String errorMessage) {
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
                DeployType deployType = PublishUtils.mapConfigurationType(
                        ConfigurationType.fromValue(inventoryConfig.getType()));
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
    
    public List<DBItemDeploymentHistory> updateFailedDeploymentForDelete( List<DBItemDeploymentHistory> depHistoryDBItemsToDeployDelete,
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
                // TODO: get Version to set here
                session.save(deploy);
                depHistoryFailed.add(deploy);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return depHistoryFailed;
    }
    
    public DBItemDeploymentHistory getDeployedInventory(String controllerId, Integer type, String path) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select a.* from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as a inner join (");
            sql.append(" select controllerId, type, path, max(id) as id from ").append(DBLayer.DBITEM_DEP_HISTORY);
            sql.append(" group by controllerId, type, path ) as b on a.id = b.id");
            sql.append(" where a.operation = 0");
            sql.append(" and a.controllerId = :controllerId");
            sql.append(" and a.type = :type");
            sql.append(" and a.path = :path");
            Query<DBItemDeploymentHistory> query = session.createQuery(sql.toString());
            query.setParameter("controllerId", controllerId);
            query.setParameter("type", type);
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public void createSubmissionForFailedDeployments(List<DBItemDeploymentHistory> failedDeployments) {
        try {
            for (DBItemDeploymentHistory failedDeploy : failedDeployments) {
                DBItemDeploymentSubmission submission = new DBItemDeploymentSubmission();
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
                submission.setVersion(failedDeploy.getVersion());
                submission.setCreated(Date.from(Instant.now()));
                session.save(submission);
            }
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

    public String getVersionId (DBItemInventoryConfiguration config) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("Select commitId from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where invConfigurationId = :confId");
        Query<String> query = session.createQuery(hql.toString());
        query.setParameter("confId", config.getId());
        return session.getSingleResult(query);
    }
    
    public DBItemDepCommitIds getCommitId (DBItemInventoryConfiguration config) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where invConfigurationId = :confId");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameter("confId", config.getId());
        return session.getSingleResult(query);
    }

    public DBItemDepCommitIds getCommitId (DBItemDeploymentHistory deployment) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where depHistoryId = :depHistoryId");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameter("depHistoryId", deployment.getId());
        return session.getSingleResult(query);
    }

    public void cleanupSignaturesForConfigurations (Set<DBItemInventoryConfiguration> invConfigurations) throws SOSHibernateException {
        Set<Long> cfgIds = invConfigurations.stream().map(DBItemInventoryConfiguration::getId).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_DEP_SIGNATURES);
        hql.append(" where invConfigurationId in (:cfgIds)");
        Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
        query.setParameterList("cfgIds", cfgIds);
        List<DBItemDepSignatures> signaturesToDelete = session.getResultList(query);
        if (signaturesToDelete != null && !signaturesToDelete.isEmpty()) {
            for (DBItemDepSignatures sig : signaturesToDelete) {
                session.delete(sig);
            }
        }
    }
    
    public void cleanupSignatures (Set<DBItemDepSignatures> signatures) throws SOSHibernateException {
        if (signatures != null && !signatures.isEmpty()) {
            for (DBItemDepSignatures sig : signatures) {
                session.delete(sig);
            }
        }
    }
    
    public void cleanupSignaturesForRedeployments (Set<DBItemDeploymentHistory> deployments) throws SOSHibernateException {
        Set<Long> depHistoryIds = deployments.stream().map(DBItemDeploymentHistory::getId).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_DEP_SIGNATURES);
        hql.append(" where depHistoryId in (:depHistoryIds)");
        Query<DBItemDepSignatures> query = session.createQuery(hql.toString());
        query.setParameterList("depHistoryIds", depHistoryIds);
        List<DBItemDepSignatures> signaturesToDelete = session.getResultList(query);
        if (signaturesToDelete != null && !signaturesToDelete.isEmpty()) {
            for (DBItemDepSignatures sig : signaturesToDelete) {
                session.delete(sig);
            }
        }
    }
    
    public void cleanupCommitIdsForConfigurations (Set<DBItemInventoryConfiguration> invConfigurations) throws SOSHibernateException {
        Set<Long> cfgIds = invConfigurations.stream().map(DBItemInventoryConfiguration::getId).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where invConfigurationId in (:cfgIds)");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameterList("cfgIds", cfgIds);
        List<DBItemDepCommitIds> commitIdsToDelete = session.getResultList(query);
        if (commitIdsToDelete != null && !commitIdsToDelete.isEmpty()) {
            for (DBItemDepCommitIds commitId : commitIdsToDelete) {
                session.delete(commitId);
            }
        }
    }
    
    public void cleanupCommitIdsForConfigurations (String versionId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where commitId = :versionId");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameter("versionId", versionId);
        List<DBItemDepCommitIds> commitIdsToDelete = session.getResultList(query);
        if (commitIdsToDelete != null && !commitIdsToDelete.isEmpty()) {
            for (DBItemDepCommitIds commitId : commitIdsToDelete) {
                session.delete(commitId);
            }
        }
    }
    
    public void cleanupCommitIdsForRedeployments (Set<DBItemDeploymentHistory> deployments) throws SOSHibernateException {
        Set<Long> depHistoryIds = deployments.stream().map(DBItemDeploymentHistory::getId).collect(Collectors.toSet());
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_DEP_COMMIT_IDS);
        hql.append(" where depHistoryId in (:depHistoryIds)");
        Query<DBItemDepCommitIds> query = session.createQuery(hql.toString());
        query.setParameterList("depHistoryIds", depHistoryIds);
        List<DBItemDepCommitIds> commitIdsToDelete = session.getResultList(query);
        if (commitIdsToDelete != null && !commitIdsToDelete.isEmpty()) {
            for (DBItemDepCommitIds commitId : commitIdsToDelete) {
                session.delete(commitId);
            }
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
        Set<DBItemInventoryConfiguration> newFolders = 
                pathsWithParents.stream().map(folder -> {
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
    
    private DBItemInventoryConfiguration createFolderConfiguration (Path path, Long auditLogId) {
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
        folder.setDocumentationId(0L);
        Instant now = Instant.now();
        folder.setCreated(Date.from(now));
        folder.setModified(Date.from(now));
        return folder;
    }

    public List<DBItemDeploymentHistory> getDeploymentHistoryCommits(ShowDepHistoryFilter filter) throws SOSHibernateException {
        if(filter.getCompactFilter() != null) {
            StringBuilder hql = new StringBuilder();
            /*
                SELECT ID, COMMIT_ID FROM DEP_HISTORY AS dep WHERE dep.id = (
                    SELECT MIN(history.id) FROM DEP_HISTORY AS history 
                        WHERE history.COMMIT_ID = dep.COMMIT_ID
                        AND history.CONTROLLER_ID = 'master') order by id asc;
             */

            hql.append("select dep from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as dep");
            hql.append(" where dep.id = (")
                .append(" select min(history.id) from ").append(DBLayer.DBITEM_DEP_HISTORY).append(" as history")
                .append(" where history.commitId = dep.commitId");
                if (filter.getCompactFilter().getAccount() != null) {
                    hql.append(" and history.account = :account");
                }
                if (filter.getCompactFilter().getControllerId() != null) {
                    hql.append(" and history.controllerId = :controllerId");
                }
                if (filter.getCompactFilter().getFolder() != null) {
                    hql.append(" and history.folder = :folder");
                }
                if(filter.getCompactFilter().getDeployType() != null) {
                    hql.append(" and history.type = :type");
                }
                if(filter.getCompactFilter().getOperation() != null) {
                    hql.append(" and history.operation = :operation");
                }
                if(filter.getCompactFilter().getState() != null) {
                    hql.append(" and history.state = :state");
                }
                if (filter.getCompactFilter().getDeploymentDate() != null) {
                    hql.append(" and history.deploymentDate = :deploymentDate");
                }
                if (filter.getCompactFilter().getDeleteDate() != null) {
                    hql.append(" and history.deleteDate = :deleteDate");
                }
                if (filter.getCompactFilter().getFrom() != null) {
                    hql.append(" and history.deploymentDate >= :fromDate");
                }
                if (filter.getCompactFilter().getTo() != null) {
                    hql.append(" and history.deploymentDate < :toDate");
                }
            hql.append(") order by deploymentDate desc");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            
            if (filter.getCompactFilter().getLimit() != null) {
                query.setMaxResults(filter.getCompactFilter().getLimit());
            }
            if (filter.getCompactFilter().getAccount() != null) {
                query.setParameter("account", filter.getCompactFilter().getAccount());
            }
            if (filter.getCompactFilter().getControllerId() != null) {
                query.setParameter("controllerId", filter.getCompactFilter().getControllerId());
            }
            if (filter.getCompactFilter().getFolder() != null) {
                query.setParameter("folder", filter.getCompactFilter().getFolder());
            }
            if(filter.getCompactFilter().getDeployType() != null) {
                query.setParameter("type", ConfigurationType.fromValue(filter.getCompactFilter().getDeployType()).intValue());
            }
            if(filter.getCompactFilter().getOperation() != null) {
                query.setParameter("operation", OperationType.valueOf(filter.getCompactFilter().getOperation()).value());
            }
            if(filter.getCompactFilter().getState() != null) {
                query.setParameter("state", DeploymentState.valueOf(filter.getCompactFilter().getState()).value());
            }
            if (filter.getCompactFilter().getDeploymentDate() != null) {
                query.setParameter("deploymentDate", filter.getCompactFilter().getDeploymentDate());
            }
            if (filter.getCompactFilter().getDeleteDate() != null) {
                query.setParameter("deleteDate", filter.getCompactFilter().getDeleteDate());
            }
            if (filter.getCompactFilter().getFrom() != null) {
                query.setParameter("fromDate", JobSchedulerDate.getDateFrom(filter.getCompactFilter().getFrom(), filter.getCompactFilter().getTimeZone()));
            }
            if (filter.getCompactFilter().getTo() != null) {
                query.setParameter("toDate", JobSchedulerDate.getDateTo(filter.getCompactFilter().getTo(), filter.getCompactFilter().getTimeZone()));
            }
            return getSession().getResultList(query);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }
    
    public List<DBItemDeploymentHistory> getDeploymentHistoryDetails(ShowDepHistoryFilter filter) throws SOSHibernateException {
        if(filter.getDetailFilter() != null) {
            Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getDetailFilter());
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
            hql.append(
                    presentFilterAttributes.stream()
                    .map(item -> {
                        if("from".equals(item)) {
                            return FROM_DEP_DATE;
                        } else if("to".equals(item)) {
                            return TO_DEP_DATE;
                        } else if ("limit".equals(item)) {
                            return null;
                        } else {
                            return item + " = :" + item;
                        }
                    }).filter(Objects::nonNull)
                    .collect(Collectors.joining(" and ", " where ", "")));
            hql.append(" order by deploymentDate desc");
            Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
            presentFilterAttributes.stream().forEach(item -> {
                switch (item) {
                case "from":
                case "to":
                    query.setParameter(item + "Date", FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item), TemporalType.TIMESTAMP);
                    break;
                case "deploymentDate":
                case "deleteDate":
                    query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item), TemporalType.TIMESTAMP);
                    break;
                case "limit":
                    query.setMaxResults((Integer)FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item));
                    break;
                default:
                    query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item));
                    break;
                }
            });
            return getSession().getResultList(query);
        } else {
            return new ArrayList<DBItemDeploymentHistory>();
        }
    }
    
    public List<DBItemDeploymentHistory> getDeploymentsToRedeploy(RedeployFilter filter) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        if (filter.getControllerId() != null) {
            hql.append(" where controllerId = :controllerId");
        }
        if (filter.getFolder() != null) {
            hql.append(" and (folder = :folder or folder like :likeFolder)");
        }
        Query<DBItemDeploymentHistory> query = getSession().createQuery(hql.toString());
        query.setParameter("controllerId", filter.getControllerId());
        query.setParameter("folder", filter.getFolder());
        query.setParameter("likeFolder", MatchMode.START.toMatchString(filter.getFolder()));
        List<DBItemDeploymentHistory> dbItems = query.getResultList();
        Set<DBItemDeploymentHistory> excludes = new HashSet<DBItemDeploymentHistory>();
        // remove excludes from result list
        if (filter.getExcludes() != null && !filter.getExcludes().isEmpty()) {
            for (ExcludeConfiguration exclude : filter.getExcludes()) {
                excludes.addAll(
                        dbItems.stream().map(item -> {
                            if(item.getPath().equals(exclude.getPath()) && DeployType.fromValue(item.getType()).equals(exclude.getDeployType())) {
                                return item;
                            }
                            return null;
                        }).collect(Collectors.toSet())
                );
            }
        }
        dbItems.removeAll(excludes);
        return dbItems;
    }

    public String getAgentIdFromAgentName (String agentName, String controllerId){
        return getAgentIdFromAgentName (agentName, controllerId, null, null);
    }

    public String getAgentIdFromAgentName (String agentName, String controllerId, String workflowPath, String jobname){
        if (agentName != null) {
            try {
                StringBuilder hql = new StringBuilder("select instance.agentId from ");
                hql.append(DBLayer.DBITEM_INV_AGENT_INSTANCES).append(" as instance ")
                    .append(" left join ").append(DBLayer.DBITEM_INV_AGENT_NAMES).append(" as aliases")
                    .append(" on instance.agentId = aliases.agentId")
                    .append(" where instance.controllerId = :controllerId and (")
                    .append(" instance.agentName = :agentName or ")
                    .append(" aliases.agentName = :agentName)");
                Query<String> query = getSession().createQuery(hql.toString());
                query.setParameter("agentName", agentName);
                query.setParameter("controllerId", controllerId);
                return query.getSingleResult();
            } catch (NoResultException e) {
                if (workflowPath != null && jobname != null) {
                    throw new JocSosHibernateException(
                            String.format("No agentId found for agentName=\"%1$s\" and controllerId=\"%2$s\" in Workflow \"%3$s\" and Job \"%4$s\"!",
                                    agentName, controllerId, workflowPath, jobname), e);
                } else {
                    throw new JocSosHibernateException(
                            String.format("No agentId found for agentName=\"%1$s\" and controllerId=\"%2$s\"!", agentName, controllerId), e);
                }
                
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e.getMessage(), e);
            } 
        } else {
            return null;
        }
    }

    public  List<DBItemInventoryConfiguration> getInvConfigurationFolders(Collection<String> paths) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path in (:paths)");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameterList("paths", paths);
            return query.getResultList();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public  DBItemInventoryConfiguration getInvConfigurationFolder(String path) {
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            return query.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public List<DBItemInventoryReleasedConfiguration> getReleasedConfigurations (String folder) {
        try {
            StringBuilder hql = new StringBuilder("from ")
                    .append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS);
            hql.append(" where folder = :folder");
            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("folder", folder);
            return query.getResultList();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public List<DBItemInventoryConfiguration> getReleasableConfigurations (String folder) {
        Set<Integer> types = JocInventory.getReleasableTypes();
        try {
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where folder = :folder");
            hql.append(" and type in (:types)");
            Query<DBItemInventoryConfiguration> query = getSession().createQuery(hql.toString());
            query.setParameter("folder", folder);
            query.setParameterList("types", types);
            return query.getResultList();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
    public String getOriginalContent(String path, ConfigurationType type) {
        try {
            StringBuilder hql = new StringBuilder("select content from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            hql.append(" where path = :path");
            hql.append(" and type == type");
            Query<String> query = getSession().createQuery(hql.toString());
            query.setParameter("path", path);
            query.setParameter("type", type.intValue());
            return query.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        } 
    }
    
}