package com.sos.joc.db.documentation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.criterion.MatchMode;
import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
//import com.sos.joc.db.documentation.DBItemDocumentationUsage;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.tree.Tree;

public class DocumentationDBLayer {

    private SOSHibernateSession session;

    public DocumentationDBLayer(SOSHibernateSession connection) {
        session = connection;
    }

    public SOSHibernateSession getSession() {
        return session;
    }
    
    public String getPath(String docRef) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select path from ").append(DBLayer.DBITEM_DOCUMENTATION);
            sql.append(" where docRef = :docRef");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("docRef", docRef);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public String getDocRef(String path) throws DBConnectionRefusedException, DBInvalidDataException {
        if (!path.contains("/")) {
            return path;
        }
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select docRef from ").append(DBLayer.DBITEM_DOCUMENTATION);
            sql.append(" where path = :path");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemDocumentation getDocumentation(String path) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION);
            sql.append(" where path = :path");
            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
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

    public List<DBItemDocumentation> getDocumentations(List<String> paths) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_DOCUMENTATION);
            if (paths != null && !paths.isEmpty()) {
                sql.append(" where path in (:paths)");
            }
            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            if (paths != null && !paths.isEmpty()) {
                query.setParameterList("paths", paths);
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
                        sql.append(" (folder = :folder");
                        sql.append(" or folder like :folder2)");
                    }
                } else {
                    sql.append(" folder = :folder");
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
  
    public Set<Tree> getFoldersByFolder(String folderName) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_DOCUMENTATION);
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                sql.append(" where ( folder = :folderName or folder like :likeFolderName )");
            }
            sql.append(" group by folder");
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

}
