package com.sos.joc.db.documentation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.criterion.MatchMode;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
//import com.sos.joc.db.documentation.DBItemDocumentationUsage;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.common.JobSchedulerObject;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.docu.DocumentationShowFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tree.Tree;

public class DocumentationDBLayer {

    private SOSHibernateSession session;

    public DocumentationDBLayer(SOSHibernateSession connection) {
        session = connection;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public DBItemDocumentation getDocumentation(String name) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION);
            sql.append(" where name = :name");
            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            query.setParameter("name", name);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Long getDocumentationId(String name) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select id from ").append(DBLayer.DBITEM_DOCUMENTATION);
            sql.append(" where name = :name");
            Query<Long> query = session.createQuery(sql.toString());
            query.setParameter("name", name);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemDocumentationImage getDocumentationImage(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return session.get(DBItemDocumentationImage.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDocumentation> getDocumentations(List<String> names) throws DBConnectionRefusedException, DBInvalidDataException {
        try {

            //TODO: Remove when names instead of paths are in the schema.
            List<String> n = new ArrayList<String>();
            for (String name : names) {
                n.add(Paths.get(name).getFileName().toString());
            }
            names.clear();
            names.addAll(n);

            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION);
            if (names != null && !names.isEmpty()) {
                sql.append(" where name in (:names)");
            }
            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            if (names != null && !names.isEmpty()) {
                query.setParameterList("names", names);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDocumentation> getDocumentations(String folder) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getDocumentations(null, folder, false);
    }

    public List<DBItemDocumentation> getDocumentations(Set<String> types, String folder, boolean recursive) throws DBConnectionRefusedException,
            DBInvalidDataException {
        String and = "";
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION);
            sql.append(" where ");
            if (folder != null && !folder.isEmpty()) {
                and = " and ";
                if (recursive) {
                    if (!"/".equals(folder)) {
                        sql.append(" (directory = :folder");
                        sql.append(" or directory like :folder2)");
                    }
                } else {
                    sql.append(" directory = :folder");
                }
            }
            if (types != null && !types.isEmpty()) {
                sql.append(and);
                sql.append("type in (:types)");
            }

            String sqlString = sql.toString();
            if (sqlString.endsWith(" where ")) {
                sqlString = sqlString.substring(0, sqlString.length() - " where ".length());
            }

            Query<DBItemDocumentation> query = session.createQuery(sqlString);
            if (folder != null && !folder.isEmpty()) {
                if (recursive) {
                    if (!"/".equals(folder)) {
                        query.setParameter("folder", folder);
                        query.setParameter("folder2", MatchMode.START.toMatchString(folder + "/"));
                    }
                } else {
                    query.setParameter("folder", folder);
                }
            }
            if (types != null && !types.isEmpty()) {
                query.setParameterList("types", types);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

//    public String getDocumentationName(DocumentationShowFilter documentationFilter) throws DBConnectionRefusedException, DBInvalidDataException {
//        return getDocumentationPath(documentationFilter.getType(), documentationFilter.getPath());
//    }

//    private String getDocumentationPath(ConfigurationType objectType, String name) throws DBConnectionRefusedException, DBInvalidDataException {
//        //TODO: Remove when names instead of paths are in the schema.
//        name = Paths.get(name).getFileName().toString();
//        if (ConfigurationType.WORKINGDAYSCALENDAR == objectType || ConfigurationType.NONWORKINGDAYSCALENDAR == objectType) {
//            return getDocumentationPathOfCalendar(name);
//        } else {
//            try {
//                StringBuilder sql = new StringBuilder();
//                sql.append("select d.path from ").append(DBLayer.DBITEM_DOCUMENTATION).append(" d, ").append(DBLayer.DBITEM_DOCUMENTATION_USAGE)
//                        .append(" du");
//                sql.append(" where d.id = du.documentationId");
//                sql.append(" and du.objectType = :objectType");
//                sql.append(" and du.name = :name");
//                Query<String> query = session.createQuery(sql.toString());
//                query.setParameter("objectType", objectType.name());
//                query.setParameter("name", name);
//                return session.getSingleResult(query);
//            } catch (SOSHibernateInvalidSessionException ex) {
//                throw new DBConnectionRefusedException(ex);
//            } catch (Exception ex) {
//                throw new DBInvalidDataException(ex);
//            }
//        }
//    }

//    public String getDocumentationPathOfCalendar(String name) throws DBConnectionRefusedException, DBInvalidDataException {
//        try {
//            Set<String> types = new HashSet<String>();
//            types.add(JobSchedulerObjectType.WORKINGDAYSCALENDAR.name());
//            types.add(JobSchedulerObjectType.NONWORKINGDAYSCALENDAR.name());
//
//            StringBuilder sql = new StringBuilder();
//
//            sql.append("select d.name from ").append(DBLayer.DBITEM_DOCUMENTATION).append(" d, ").append(DBLayer.DBITEM_DOCUMENTATION_USAGE).append(
//                    " du");
//            sql.append(" where d.id = du.documentationId");
//            sql.append(" and du.objectType in (:objectType)");
//            sql.append(" and du.name = :name");
//            Query<String> query = session.createQuery(sql.toString());
//            query.setParameterList("objectType", types);
//            query.setParameter("name", name);
//            return session.getSingleResult(query);
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

//    public Map<String, String> getDocumentationPaths(JobSchedulerObjectType objectType) throws DBConnectionRefusedException, DBInvalidDataException {
//        if (JobSchedulerObjectType.WORKINGDAYSCALENDAR == objectType || JobSchedulerObjectType.NONWORKINGDAYSCALENDAR == objectType) {
//            return getDocumentationPathsOfCalendar();
//        } else {
//            try {
//                StringBuilder sql = new StringBuilder();
//                sql.append("select new ").append(DocumentationOfObject.class.getName()).append("(d.name, du.name) from ");
//                sql.append(DBLayer.DBITEM_DOCUMENTATION).append(" d, ").append(DBLayer.DBITEM_DOCUMENTATION_USAGE).append(" du");
//                sql.append(" where d.id = du.documentationId");
//                sql.append(" and du.objectType = :objectType");
//                Query<DocumentationOfObject> query = session.createQuery(sql.toString());
//                query.setParameter("objectType", objectType.name());
//                List<DocumentationOfObject> result = session.getResultList(query);
//                if (result == null) {
//                    return new HashMap<String, String>();
//                }
//                return result.stream().collect(Collectors.toMap(DocumentationOfObject::getObjPath, DocumentationOfObject::getDocPath));
//            } catch (SOSHibernateInvalidSessionException ex) {
//                throw new DBConnectionRefusedException(ex);
//            } catch (Exception ex) {
//                throw new DBInvalidDataException(ex);
//            }
//        }
//    }

//    public Map<String, String> getDocumentationPathsOfCalendar() throws DBConnectionRefusedException, DBInvalidDataException {
//        Set<String> types = new HashSet<String>();
//        types.add(JobSchedulerObjectType.WORKINGDAYSCALENDAR.name());
//        types.add(JobSchedulerObjectType.NONWORKINGDAYSCALENDAR.name());
//        try {
//            StringBuilder sql = new StringBuilder();
//            sql.append("select new ").append(DocumentationOfObject.class.getName()).append("(d.name, du.name) from ");
//            sql.append(DBLayer.DBITEM_DOCUMENTATION).append(" d, ").append(DBLayer.DBITEM_DOCUMENTATION_USAGE).append(" du");
//            sql.append(" where d.id = du.documentationId");
//            sql.append(" and du.objectType in (:objectType)");
//            Query<DocumentationOfObject> query = session.createQuery(sql.toString());
//            query.setParameterList("objectType", types);
//            List<DocumentationOfObject> result = session.getResultList(query);
//            if (result == null) {
//                return new HashMap<String, String>();
//            }
//            return result.stream().collect(Collectors.toMap(DocumentationOfObject::getObjPath, DocumentationOfObject::getDocPath));
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

    public Set<Tree> getFoldersByFolder(String folderName) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select directory from ").append(DBLayer.DBITEM_DOCUMENTATION);
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                sql.append(" where ( directory = :folderName or directory like :likeFolderName )");
            }
            sql.append(" group by directory");
            Query<String> query = session.createQuery(sql.toString());
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                query.setParameter("folderName", folderName);
                query.setParameter("likeFolderName", MatchMode.START.toMatchString(folderName + "/"));
            }
            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            } else if (folderName.equals(JocInventory.ROOT_FOLDER)) {
                Tree tree = new Tree();
                tree.setPath(JocInventory.ROOT_FOLDER);
                return Arrays.asList(tree).stream().collect(Collectors.toSet());
            }
            return new HashSet<>();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

//    public List<DBItemDocumentationUsage> getDocumentationUsages(Long documentationId) throws DBConnectionRefusedException, DBInvalidDataException {
//        try {
//            StringBuilder sql = new StringBuilder();
//            sql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION_USAGE);
//            sql.append(" where documentationId = :documentationId");
//            Query<DBItemDocumentationUsage> query = session.createQuery(sql.toString());
//            query.setParameter("documentationId", documentationId);
//            return session.getResultList(query);
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

//    public List<JobSchedulerObject> getDocumentationUsages(String docName) throws DBConnectionRefusedException,
//            DBInvalidDataException {
//        try {
//            StringBuilder hql = new StringBuilder();
//            hql.append("select new ").append(ObjectOfDocumentation.class.getName()).append("(du.name, du.objectType) from ");
//            hql.append(DBLayer.DBITEM_DOCUMENTATION_USAGE).append(" du, ");
//            hql.append(DBLayer.DBITEM_DOCUMENTATION).append(" d");
//            hql.append(" where du.documentationId = d.id");
//            hql.append(" and d.name = :name");
//
//            Query<ObjectOfDocumentation> query = session.createQuery(hql.toString());
//            query.setParameter("name", docName);
//            List<ObjectOfDocumentation> result = session.getResultList(query);
//            if (result == null || result.isEmpty()) {
//                return null;
//            }
//            // Map<JobSchedulerObjectType, List<String>> dbUsages = result.stream().collect(Collectors.groupingBy(ObjectOfDocumentation::getType,
//            // Collectors.mapping(ObjectOfDocumentation::getPath, Collectors.toList())));
//            // String hqlStr = "select name from %s where instanceId = :instanceId and name in (:paths)";
//            List<JobSchedulerObject> jsObjs = new ArrayList<JobSchedulerObject>();
//            // for (Entry<JobSchedulerObjectType, List<String>> entry : dbUsages.entrySet()) {
//            // String sql = null;
//            // switch (entry.getKey()) {
//            // case JOB:
//            // sql = String.format(hqlStr, DBITEM_INVENTORY_JOBS);
//            // break;
//            // case JOBCHAIN:
//            // sql = String.format(hqlStr, DBITEM_INVENTORY_JOB_CHAINS);
//            // break;
//            // case ORDER:
//            // sql = String.format(hqlStr, DBITEM_INVENTORY_ORDERS);
//            // break;
//            // case NONWORKINGDAYSCALENDAR:
//            // case WORKINGDAYSCALENDAR:
//            // sql = String.format("select name from %s where schedulerId = :schedulerId and name in (:paths)", DBLayer.DBITEM_CALENDARS_DEPRECATED);
//            // break;
//            // case PROCESSCLASS:
//            // sql = String.format(hqlStr, DBITEM_INVENTORY_PROCESS_CLASSES);
//            // break;
//            // case LOCK:
//            // sql = String.format(hqlStr, DBITEM_INVENTORY_LOCKS);
//            // break;
//            // case SCHEDULE:
//            // sql = String.format(hqlStr, DBITEM_INVENTORY_SCHEDULES);
//            // break;
//            // default:
//            // break;
//            // }
//            // if (sql != null) {
//            // Query<String> query2 = session.createQuery(sql);
//            // if (entry.getKey() == JobSchedulerObjectType.NONWORKINGDAYSCALENDAR || entry.getKey() == JobSchedulerObjectType.WORKINGDAYSCALENDAR) {
//            // query2.setParameter("schedulerId", schedulerId);
//            //// } else {
//            //// query2.setParameter("instanceId", instanceId);
//            // }
//            // query2.setParameterList("paths", entry.getValue());
//            // List<String> paths = session.getResultList(query2);
//            // if (paths != null && !paths.isEmpty()) {
//            // for (String path : paths) {
//            // JobSchedulerObject jsObj = new JobSchedulerObject();
//            // jsObj.setPath(path);
//            // jsObj.setType(entry.getKey());
//            // jsObjs.add(jsObj);
//            // }
//            // }
//            // }
//            // }
//            return jsObjs;
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

//    public DBItemDocumentationUsage getDocumentationUsageForAssignment(String name, String objectType) throws DBConnectionRefusedException,
//            DBInvalidDataException {
//        try {
//            StringBuilder hql = new StringBuilder();
//            hql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION_USAGE);
//            hql.append(" where schedulerId = :schedulerId");
//            hql.append(" and name = :name");
//            hql.append(" and objectType = :objectType");
//            Query<DBItemDocumentationUsage> query = session.createQuery(hql.toString());
//            query.setParameter("name", name);
//            query.setParameter("objectType", objectType);
//            return session.getSingleResult(query);
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

//    public Map<String, List<JobSchedulerObject>> getDocumentationUsages(Collection<String> paths) throws DBConnectionRefusedException,
//            DBInvalidDataException {
//        try {
//            StringBuilder hql = new StringBuilder();
//            hql.append("select new ").append(DocumentationOfObject.class.getName()).append("(d.path, du.path, du.objectType) from ");
//            hql.append(DBLayer.DBITEM_DOCUMENTATION_USAGE).append(" du, ");
//            hql.append(DBLayer.DBITEM_DOCUMENTATION).append(" d");
//            hql.append(" where du.documentationId = d.id");
//
//            if (paths != null && !paths.isEmpty()) {
//                hql.append(" and d.path in (:paths)");
//            }
//
//            Query<DocumentationOfObject> query = session.createQuery(hql.toString());
//
//            if (paths != null && !paths.isEmpty()) {
//                query.setParameterList("paths", paths);
//            }
//            List<DocumentationOfObject> result = session.getResultList(query);
//            if (result == null || result.isEmpty()) {
//                return new HashMap<String, List<JobSchedulerObject>>();
//            }
//            return result.stream().collect(Collectors.groupingBy(DocumentationOfObject::getDocPath, Collectors.mapping(
//                    DocumentationOfObject::getDocUsage, Collectors.toList())));
//        } catch (SOSHibernateInvalidSessionException ex) {
//            throw new DBConnectionRefusedException(ex);
//        } catch (Exception ex) {
//            throw new DBInvalidDataException(ex);
//        }
//    }

}
