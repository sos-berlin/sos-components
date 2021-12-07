package com.sos.joc.dailyplan.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DBLayerSchedules extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerSchedules(SOSHibernateSession session) {
        super(session);
    }

    public List<DBItemInventoryReleasedConfiguration> getAllSchedules() throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append("where type=:type");

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        return getSession().getResultList(query);
    }

    public List<DBItemInventoryReleasedConfiguration> getSchedules(Set<Folder> scheduleFolders, Set<String> scheduleSingles)
            throws SOSHibernateException {
        boolean hasFolders = scheduleFolders != null && scheduleFolders.size() > 0;
        boolean hasSingles = scheduleSingles != null && scheduleSingles.size() > 0;

        if (!hasFolders && !hasSingles) {
            return getAllSchedules();
        }

        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");

        // folders
        Map<String, String> paramsFolder = new HashMap<>();
        Map<String, String> paramsLikeFolder = new HashMap<>();
        if (hasFolders) {
            hql.append("and (");
            int i = 0;
            for (Folder folder : scheduleFolders) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramNameFolder = "folder" + i;
                if (folder.getRecursive()) {
                    String paramNameLikeFolder = "likeFolder" + i;
                    hql.append("(folder=:").append(paramNameFolder).append(" or folder like :").append(paramNameLikeFolder).append(") ");
                    paramsLikeFolder.put(paramNameLikeFolder, (folder.getFolder() + "/%").replaceAll("//+", "/"));
                } else {
                    hql.append("folder=:").append(paramNameFolder).append(" ");
                }
                paramsFolder.put(paramNameFolder, folder.getFolder());
                i++;
            }
            hql.append(") ");
        }

        // single paths
        Map<String, String> paramsPaths = new HashMap<>();
        if (hasSingles) {
            hql.append("and (");
            int i = 0;
            for (String single : scheduleSingles) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramName = "path" + i;
                hql.append("path=:").append(paramName).append(" ");
                paramsPaths.put(paramName, single);
                i++;
            }
            hql.append(") ");
        }

        Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", ConfigurationType.SCHEDULE.intValue());
        if (hasFolders) {
            paramsFolder.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
            paramsLikeFolder.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
        }
        if (hasSingles) {
            paramsPaths.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
        }
        return getSession().getResultList(query);
    }

    public List<String> getWorkflowNames(String controllerId, Set<Folder> workflowFolders, Set<String> workflowSingles) throws SOSHibernateException {
        boolean hasFolders = workflowFolders != null && workflowFolders.size() > 0;
        boolean hasSingles = workflowSingles != null && workflowSingles.size() > 0;

        if (!hasFolders && !hasSingles) {
            return null; // getAllWorkflowNames(controllerId);
        }

        StringBuilder hql = new StringBuilder("select name from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and controllerId=:controllerId ");

        // folders
        Map<String, String> paramsFolder = new HashMap<>();
        Map<String, String> paramsLikeFolder = new HashMap<>();
        if (hasFolders) {
            hql.append("and (");
            int i = 0;
            for (Folder folder : workflowFolders) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramNameFolder = "folder" + i;
                if (folder.getRecursive()) {
                    String paramNameLikeFolder = "likeFolder" + i;
                    hql.append("(folder=:").append(paramNameFolder).append(" or folder like :").append(paramNameLikeFolder).append(") ");
                    paramsLikeFolder.put(paramNameLikeFolder, (folder.getFolder() + "/%").replaceAll("//+", "/"));
                } else {
                    hql.append("folder=:").append(paramNameFolder).append(" ");
                }
                paramsFolder.put(paramNameFolder, folder.getFolder());
                i++;
            }
            hql.append(") ");
        }

        // single paths
        Map<String, String> paramsName = new HashMap<>();
        if (hasSingles) {
            hql.append("and (");
            int i = 0;
            for (String single : workflowSingles) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramName = "name" + i;
                hql.append("name=:").append(paramName).append(" ");
                paramsName.put(paramName, single);
                i++;
            }
            hql.append(") ");
        }

        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("type", DeployType.WORKFLOW.intValue());
        query.setParameter("controllerId", controllerId);
        if (hasFolders) {
            paramsFolder.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
            paramsLikeFolder.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
        }
        if (hasSingles) {
            paramsName.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
        }
        return getSession().getResultList(query);
    }

    @Deprecated
    public Map<String, String> getSchedulePathNameMap(List<String> scheduleNamesOrPaths) throws SOSHibernateException {

        if (scheduleNamesOrPaths == null || scheduleNamesOrPaths.isEmpty()) {
            return Collections.emptyMap();
        }
        if (scheduleNamesOrPaths.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < scheduleNamesOrPaths.size(); i += SOSHibernate.LIMIT_IN_CLAUSE) {
                result.putAll(getSchedulePathNameMap(SOSHibernate.getInClausePartition(i, scheduleNamesOrPaths)));
            }
            return result;
        } else {
            Map<Boolean, List<String>> namesAndPaths = scheduleNamesOrPaths.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors
                    .groupingBy(s -> s.startsWith("/")));

            StringBuilder sql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" where");
            if (namesAndPaths.containsKey(true)) { // paths
                sql.append(" path in (:paths)");
            }
            if (namesAndPaths.containsKey(true) && namesAndPaths.containsKey(false)) { // paths and names
                sql.append(" or");
            }
            if (namesAndPaths.containsKey(false)) { // names
                sql.append(" name in (:names)");
            }

            sql.append(" and type=:type");

            Query<DBItemInventoryReleasedConfiguration> query = getSession().createQuery(sql);
            query.setParameter("type", ConfigurationType.SCHEDULE.intValue());

            if (namesAndPaths.containsKey(true)) { // paths
                query.setParameterList("paths", namesAndPaths.get(true));
            }
            if (namesAndPaths.containsKey(false)) { // names
                query.setParameterList("names", namesAndPaths.get(false));
            }

            List<DBItemInventoryReleasedConfiguration> resultset = getSession().getResultList(query);
            if (resultset == null || resultset.isEmpty()) {
                return Collections.emptyMap();
            }

            return resultset.stream().distinct().collect(Collectors.toMap(DBItemInventoryReleasedConfiguration::getPath,
                    DBItemInventoryReleasedConfiguration::getName));
        }
    }

}