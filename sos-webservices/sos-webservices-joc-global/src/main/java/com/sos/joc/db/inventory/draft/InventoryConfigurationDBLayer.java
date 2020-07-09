package com.sos.joc.db.inventory.draft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.tree.Tree;

public class InventoryConfigurationDBLayer {

    private SOSHibernateSession session;

    public InventoryConfigurationDBLayer(SOSHibernateSession conn) {
        this.session = conn;
    }

    public DBItemInventoryConfiguration getInventoryInstance(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (id == null) {
                return null;
            }
            return session.get(DBItemInventoryConfiguration.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Tree> Set<T> getFoldersByFolderAndType(String folder, Set<String> objectTypes) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (folder != null && !folder.isEmpty() && !folder.equals("/")) {
                whereClause.add("(folder = :folder or folder like :likeFolder)");
            }
            if (objectTypes != null && !objectTypes.isEmpty()) {
                if (objectTypes.size() == 1) {
                    whereClause.add("type = :type");
                } else {
                    whereClause.add("type in (:type)");
                }
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            sql.append(" group by folder");
            Query<String> query = this.session.createQuery(sql.toString());
            if (folder != null && !folder.isEmpty() && !folder.equals("/")) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
            if (objectTypes != null && !objectTypes.isEmpty()) {
                if (objectTypes.size() == 1) {
                    query.setParameter("type", InventoryMeta.ConfigurationType.valueOf(objectTypes.iterator().next()).value());
                } else {
                    query.setParameterList("type", objectTypes);// TODO
                }
            }
            List<String> result = this.session.getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    T tree = (T) new Tree(); // new JoeTree();
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
