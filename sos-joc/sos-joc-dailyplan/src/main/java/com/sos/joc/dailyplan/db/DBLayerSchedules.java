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
import com.sos.joc.classes.common.FolderPath;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.items.InventoryNamePath;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;

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
        boolean useFolders = FolderPath.useFolders(folders);
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
        boolean useFolders = FolderPath.useFolders(folders);
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
    
}