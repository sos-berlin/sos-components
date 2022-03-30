package com.sos.joc.dailyplan.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DBLayerSchedules extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerSchedules(SOSHibernateSession session) {
        super(session);
    }

    public List<DBBeanReleasedSchedule2DeployedWorkflow> getReleasedSchedule2DeployedWorkflows(String controllerId, Set<Folder> folders)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select ");
        hql.append("dc.controllerId     as controllerId");
        hql.append(",sw.schedulePath    as schedulePath");
        hql.append(",sw.scheduleFolder  as scheduleFolder");
        hql.append(",sw.scheduleName    as scheduleName");
        hql.append(",sw.scheduleContent as scheduleContent ");
        hql.append(",dc.path            as workflowPath");
        hql.append(",dc.folder          as workflowFolder");
        hql.append(",dc.name            as workflowName");
        hql.append(",dc.content         as workflowContent ");
        hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" sw ");
        hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc ");
        hql.append("where dc.name=sw.workflowName ");
        hql.append("and dc.type=:workflowType ");
        if (!SOSString.isEmpty(controllerId)) {
            hql.append("and dc.controllerId=:controllerId ");
        }

        // folders
        boolean useFolders = useFolders(folders);
        Map<String, String> paramsFolder = new HashMap<>();
        Map<String, String> paramsLikeFolder = new HashMap<>();
        if (useFolders) {
            hql.append("and (");
            int i = 0;
            for (Folder folder : folders) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramNameFolder = "folder" + i;
                if (folder.getRecursive()) {
                    String paramNameLikeFolder = "likeFolder" + i;
                    hql.append("(sw.scheduleFolder=:").append(paramNameFolder).append(" or sw.scheduleFolder like :").append(paramNameLikeFolder)
                            .append(") ");
                    paramsLikeFolder.put(paramNameLikeFolder, (folder.getFolder() + "/%").replaceAll("//+", "/"));
                } else {
                    hql.append("sw.scheduleFolder=:").append(paramNameFolder).append(" ");
                }
                paramsFolder.put(paramNameFolder, folder.getFolder());
                i++;
            }
            hql.append(") ");
        }

        Query<DBBeanReleasedSchedule2DeployedWorkflow> query = getSession().createQuery(hql.toString(),
                DBBeanReleasedSchedule2DeployedWorkflow.class);
        query.setParameter("workflowType", ConfigurationType.WORKFLOW.intValue());
        if (!SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        if (useFolders) {
            paramsFolder.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
            paramsLikeFolder.entrySet().stream().forEach(e -> {
                query.setParameter(e.getKey(), e.getValue());
            });
        }
        return getSession().getResultList(query);
    }

    public List<DBBeanReleasedSchedule2DeployedWorkflow> getReleasedSchedule2DeployedWorkflows(String controllerId, Set<Folder> folders,
            Set<String> singlePaths, boolean checkForSchedule) throws SOSHibernateException {
        boolean useFolders = useFolders(folders);
        boolean hasSingles = singlePaths != null && singlePaths.size() > 0;

        String folderField = "sw.scheduleFolder";
        String pathField = "sw.schedulePath";
        if (!checkForSchedule) {
            folderField = "dc.folder";
            pathField = "dc.path";
        }

        if (!useFolders && !hasSingles) {
            return getReleasedSchedule2DeployedWorkflows(controllerId, folders);
        }

        StringBuilder hql = new StringBuilder("select ");
        hql.append("dc.controllerId    as controllerId");
        hql.append(",sw.schedulePath   as schedulePath");
        hql.append(",sw.scheduleFolder as scheduleFolder");
        hql.append(",sw.scheduleName   as scheduleName");
        hql.append(",sw.scheduleContent as scheduleContent ");
        hql.append(",dc.path           as workflowPath");
        hql.append(",dc.folder         as workflowFolder");
        hql.append(",dc.name           as workflowName");
        hql.append(",dc.content        as workflowContent ");
        hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS).append(" sw ");
        hql.append(",").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" dc ");
        hql.append("where dc.name=sw.workflowName ");
        hql.append("and dc.type=:workflowType ");
        if (!SOSString.isEmpty(controllerId)) {
            hql.append("and dc.controllerId=:controllerId ");
        }

        // folders
        Map<String, String> paramsFolder = new HashMap<>();
        Map<String, String> paramsLikeFolder = new HashMap<>();
        if (useFolders) {
            hql.append("and (");
            int i = 0;
            for (Folder folder : folders) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramNameFolder = "folder" + i;
                if (folder.getRecursive()) {
                    String paramNameLikeFolder = "likeFolder" + i;
                    hql.append("(").append(folderField).append("=:").append(paramNameFolder).append(" or ").append(folderField).append(" like :")
                            .append(paramNameLikeFolder).append(") ");
                    paramsLikeFolder.put(paramNameLikeFolder, (folder.getFolder() + "/%").replaceAll("//+", "/"));
                } else {
                    hql.append(folderField).append("=:").append(paramNameFolder).append(" ");
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
            for (String path : singlePaths) {
                if (i > 0) {
                    hql.append(" or ");
                }
                String paramName = "path" + i;
                hql.append(pathField).append("=:").append(paramName).append(" ");
                paramsPaths.put(paramName, path);
                i++;
            }
            hql.append(") ");
        }

        Query<DBBeanReleasedSchedule2DeployedWorkflow> query = getSession().createQuery(hql.toString(),
                DBBeanReleasedSchedule2DeployedWorkflow.class);
        query.setParameter("workflowType", ConfigurationType.WORKFLOW.intValue());
        if (!SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        if (useFolders) {
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

    private boolean useFolders(Set<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            if (folders.size() == 1) {
                Folder f = folders.iterator().next();
                if (f == null) {
                    return false;
                }
                boolean recursive = f.getRecursive() == null ? true : f.getRecursive().booleanValue();
                if (f.getFolder().equals("/") && recursive) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}