package com.sos.joc.db.inventory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tree.Tree;

public class InventoryTrashDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventoryTrashDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public Set<Tree> getFoldersByFolderAndType(String folder, Set<Integer> inventoryTypes, Boolean onlyValidObjects)
            throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            List<String> whereClause = new ArrayList<String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select folder, type, path from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            if (folder != null && !folder.isEmpty() && !folder.equals(JocInventory.ROOT_FOLDER)) {
                whereClause.add("(folder = :folder or folder like :likeFolder)");
            }
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    whereClause.add("type = :type");
                } else {
                    whereClause.add("type in (:type)");
                }
            }
            if (onlyValidObjects == Boolean.TRUE) {
                whereClause.add("valid = 1");
            }
            if (!whereClause.isEmpty()) {
                sql.append(whereClause.stream().collect(Collectors.joining(" and ", " where ", "")));
            }
            Query<Object[]> query = getSession().createQuery(sql.toString());
            if (folder != null && !folder.isEmpty() && !folder.equals(JocInventory.ROOT_FOLDER)) {
                query.setParameter("folder", folder);
                query.setParameter("likeFolder", folder + "/%");
            }
            if (inventoryTypes != null && !inventoryTypes.isEmpty()) {
                if (inventoryTypes.size() == 1) {
                    query.setParameter("type", inventoryTypes.iterator().next());
                } else {
                    query.setParameterList("type", inventoryTypes);
                }
            }

            List<Object[]> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                Set<String> folders = result.stream().map(item -> {
                    Integer type = (Integer) item[1];
                    if (type.equals(ConfigurationType.FOLDER.intValue())) {
                        return (String) item[2];
                    }
                    return (String) item[0];
                }).collect(Collectors.toSet());

                Set<String> folderWithParents = new HashSet<>();
                for (String f : folders) {
                    Path p = Paths.get(f);
                    for (int i = 0; i < p.getNameCount(); i++) {
                        folderWithParents.add(("/" + p.subpath(0, i + 1)).replace('\\', '/'));
                    }
                }
                Set<Tree> tree = getFoldersByFolder(folderWithParents);
                Tree root = new Tree();
                root.setPath(JocInventory.ROOT_FOLDER);
                root.setDeleted(false);
                tree.add(root);

                return tree;
            }
            return new HashSet<>();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    private Set<Tree> getFoldersByFolder(Collection<String> folders) throws SOSHibernateException {
        if (folders != null && !folders.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("select path, deleted from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where path in (:folders) and type=:type");
            Query<Object[]> query = getSession().createQuery(sql.toString());
            query.setParameterList("folders", folders);
            query.setParameter("type", ConfigurationType.FOLDER.intValue());
            List<Object[]> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath((String) s[0]);
                    tree.setDeleted((Boolean) s[1]);
                    return tree;
                }).collect(Collectors.toSet());
            }
        }
        return new HashSet<>();
    }
}
