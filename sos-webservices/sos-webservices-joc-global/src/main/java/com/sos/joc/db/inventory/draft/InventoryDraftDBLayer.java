package com.sos.joc.db.inventory.draft;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.tree.Tree;

public class InventoryDraftDBLayer {
    
    private SOSHibernateSession session;

    public InventoryDraftDBLayer(SOSHibernateSession conn) {
        this.session = conn;
    }
    
    public DBItemJSDraftObject getInventoryInstance(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return session.get(DBItemJSDraftObject.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Tree> Set<T> getFoldersByFolderAndType(String schedulerId, String folder, Set<String> objectTypes)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_JS_DRAFT_OBJECTS);
            sql.append(" where jobschedulerId = :schedulerId");
            if (folder != null && !folder.isEmpty() && !folder.equals("/")) {
                sql.append(" and (folder = :folder or folder like :likeFolder)");
            }
            if (objectTypes != null && !objectTypes.isEmpty()) {
                if (objectTypes.size() == 1) {
                    sql.append(" and objectType = :objectType");
                } else {
                    sql.append(" and objectType in (:objectType)");
                }
            }
            sql.append(" group by folder");
            Query<String> query = this.session.createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            if (folder != null && !folder.isEmpty() && !folder.equals("/")) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
            if (objectTypes != null && !objectTypes.isEmpty()) {
                if (objectTypes.size() == 1) {
                    query.setParameter("objectType", objectTypes.iterator().next());
                } else {
                    query.setParameterList("objectType", objectTypes);
                }
            }
            List<String> result = this.session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    T tree = (T) new Tree(); //new JoeTree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            } else if (folder.equals("/")) {
                T tree = (T) new Tree();
                tree.setPath("/");
                return Arrays.asList(tree).stream().collect(Collectors.toSet());
            }
            return new HashSet<T>();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}
