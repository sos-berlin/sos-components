package com.sos.joc.db.documentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
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
            sql.append("select path from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
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
            sql.append("select docRef from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
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

    public String getDocumentationByRef(String reference, String path) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select path from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
            sql.append(" where docRef = :reference");
            sql.append(" and path != :path");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("path", path);
            query.setParameter("reference", reference);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public DBItemDocumentation getDocumentationByRef(String reference) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
            sql.append(" where docRef = :reference");
            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            query.setParameter("reference", reference);
            query.setMaxResults(1);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public String getUniqueDocRef(String reference) throws SOSHibernateException {
        if (reference == null || reference.isEmpty()) {
            return null;
        }
        StringBuilder hql = new StringBuilder("select docRef from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
        hql.append(" where lower(docRef) = :docRef or lower(docRef) like :likeDocRef");
        hql.append(" group by docRef");
        Query<String> query = getSession().createQuery(hql.toString());
        query.setParameter("docRef", reference.toLowerCase());
        query.setParameter("likeDocRef", reference.toLowerCase() + "-%");
        List<String> result = getSession().getResultList(query);
        if (result == null || result.isEmpty()) {
            return reference;
        }
        if (!result.contains(reference)) {
            return reference;
        }
        String ref = Pattern.quote(reference);
        Predicate<String> predicate = Pattern.compile(ref + "-[0-9]+$", Pattern.CASE_INSENSITIVE).asPredicate();
        Function<String, Integer> mapper = n -> Integer.parseInt(n.replaceFirst(ref + "-([0-9]+)$", "0$1"));
        SortedSet<Integer> numbers = result.stream().map(String::toLowerCase).distinct().filter(predicate).map(mapper).sorted().collect(Collectors
                .toCollection(TreeSet::new));
        if (numbers.isEmpty()) {
            return reference + "-1";
        }
        Integer num = 1;
        for (Integer number : numbers) {
            if (num < number) {
                break;
            }
            num = number + 1;
        }
        return reference + "-" + num;
    }

    public DBItemDocumentation getDocumentation(String path) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
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
    
    public List<DBItemDocumentation> getDocumentations(Collection<String> paths) throws DBConnectionRefusedException, DBInvalidDataException {
        return getDocumentations(paths, false);
    }

    public List<DBItemDocumentation> getDocumentations(Collection<String> paths, boolean onlyWithAssignReference) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
            List<String> clauses = new ArrayList<>();
            if (paths != null && !paths.isEmpty()) {
                clauses.add("path in (:paths)");
            }
            if (onlyWithAssignReference) {
                clauses.add("isRef = :isRef");
            }
            if (!clauses.isEmpty()) {
                sql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            if (paths != null && !paths.isEmpty()) {
                query.setParameterList("paths", paths);
            }
            if (onlyWithAssignReference) {
                query.setParameter("isRef", true);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemDocumentation> getDocumentations(String folder, boolean onlyWithAssignReference) throws DBConnectionRefusedException,
            DBInvalidDataException {
        return getDocumentations(null, folder, false, onlyWithAssignReference);
    }

    public List<DBItemDocumentation> getDocumentations(Stream<String> types, String folder, boolean recursive, boolean onlyWithAssignReference)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
            List<String> clauses = new ArrayList<>();
            if (folder != null && !folder.isEmpty()) {
                if (recursive) {
                    if (!"/".equals(folder)) {
                        clauses.add("(folder = :folder or folder like :likefolder)");
                    }
                } else {
                    clauses.add("folder = :folder");
                }
            }
            if (types != null) {
                clauses.add("type in (:types)");
            }
            if (onlyWithAssignReference) {
                clauses.add("isRef = :isRef");
            }
            if (!clauses.isEmpty()) {
                sql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }

            Query<DBItemDocumentation> query = session.createQuery(sql.toString());
            if (folder != null && !folder.isEmpty()) {
                if (recursive) {
                    if (!"/".equals(folder)) {
                        query.setParameter("folder", folder);
                        query.setParameter("likefolder", folder + "/%");
                    }
                } else {
                    query.setParameter("folder", folder);
                }
            }
            if (types != null) {
                query.setParameterList("types", types.map(String::toLowerCase).collect(Collectors.toSet()));
            }
            if (onlyWithAssignReference) {
                query.setParameter("isRef", true);
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
  
    public Set<Tree> getFoldersByFolder(String folderName, boolean onlyWithAssignReference) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_INV_DOCUMENTATIONS);
            List<String> clauses = new ArrayList<>();
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                clauses.add("( folder = :folderName or folder like :likeFolderName )");
            }
            if (onlyWithAssignReference) {
                clauses.add("isRef = :isRef");
            }
            if (!clauses.isEmpty()) {
                sql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            sql.append(" group by folder");
            Query<String> query = session.createQuery(sql.toString());
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                query.setParameter("folderName", folderName);
                query.setParameter("likeFolderName", folderName + "/%");
            }
            if (onlyWithAssignReference) {
                query.setParameter("isRef", true);
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
