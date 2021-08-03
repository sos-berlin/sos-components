package com.sos.joc.db.inventory;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventorySearchDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public InventorySearchDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public List<InventorySearchItem> getInventoryConfigurations(ConfigurationType type, String search, List<String> folders)
            throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);

        StringBuilder hql = new StringBuilder("select ic.id as id ");
        hql.append(",ic.path as path ");
        hql.append(",ic.name as name ");
        hql.append(",ic.title as title ");
        hql.append(",ic.valid as valid ");
        hql.append(",ic.deleted as deleted ");
        hql.append(",ic.deployed as deployed ");
        hql.append(",ic.released as released ");
        if (isReleasable) {
            hql.append(",count(irc.id) as countReleased ");
            hql.append(",0 as countDeployed ");

        } else {
            hql.append(",0 as countReleased ");
            hql.append(",count(dh.id) as countDeployed ");
        }
        hql.append("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS).append(" ic ");
        if (isReleasable) {
            hql.append("left join ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" irc ");
            hql.append("on ic.id=irc.cid ");
        } else {
            hql.append("left join ").append(DBLayer.DBITEM_DEP_HISTORY).append(" dh ");
            hql.append("on ic.id=dh.inventoryConfigurationId ");
        }
        hql.append("where ic.type=:type ");
        if (!SOSString.isEmpty(search)) {
            hql.append("and (ic.name like :search or ic.title like :search) ");
        }
        boolean searchInFolders = false;
        if (folders != null && folders.size() > 0 && !folders.contains("/")) {
            searchInFolders = true;
            hql.append("and (");
            List<String> f = new ArrayList<>();
            for (int i = 0; i < folders.size(); i++) {
                f.add("ic.folder like :folder" + i + " ");
            }
            hql.append(String.join(" or ", f));
            hql.append(") ");
        }
        hql.append("group by ic.id");

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (!SOSString.isEmpty(search)) {
            query.setParameter("search", '%' + search + '%');
        }
        if (searchInFolders) {
            for (int i = 0; i < folders.size(); i++) {
                query.setParameter("folder" + i, folders.get(i) + '%');
            }
        }
        return getSession().getResultList(query);
    }

    public List<InventorySearchItem> getDeployedOrReleasedConfigurations(ConfigurationType type, String search, List<String> folders,
            String controllerId) throws SOSHibernateException {

        boolean isReleasable = isReleasable(type);
        StringBuilder hql = new StringBuilder("select ");
        if (isReleasable) {
            hql.append("mt.cid as id");
            hql.append(",mt.path as path");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",false as deployed ");
            hql.append(",true as released ");
            hql.append(",1 as countReleased ");
            hql.append(",0 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_INV_RELEASED_CONFIGURATIONS).append(" mt ");
        } else {
            hql.append("mt.inventoryConfigurationId as id");
            hql.append(",mt.path as path");
            hql.append(",mt.name as name");
            hql.append(",mt.title as title ");
            hql.append(",mt.controllerId as controllerId ");
            hql.append(",true as valid ");
            hql.append(",false as deleted ");
            hql.append(",true as deployed ");
            hql.append(",false as released ");
            hql.append(",0 as countReleased ");
            hql.append(",1 as countDeployed ");
            hql.append("from ").append(DBLayer.DBITEM_DEP_CONFIGURATIONS).append(" mt ");
        }
        hql.append("where mt.type=:type ");
        if (!SOSString.isEmpty(search)) {
            hql.append("and (mt.name like :search or mt.title like :search) ");
        }

        boolean searchInFolders = false;
        if (folders != null && folders.size() > 0 && !folders.contains("/")) {
            searchInFolders = true;
            hql.append("and (");
            List<String> f = new ArrayList<>();
            for (int i = 0; i < folders.size(); i++) {
                f.add("mt.folder like :folder" + i + " ");
            }
            hql.append(String.join(" or ", f));
            hql.append(") ");
        }
        if (!isReleasable && !SOSString.isEmpty(controllerId)) {
            hql.append("and mt.controllerId=:controllerId ");
        }

        Query<InventorySearchItem> query = getSession().createQuery(hql.toString(), InventorySearchItem.class);
        query.setParameter("type", type.intValue());
        if (!SOSString.isEmpty(search)) {
            query.setParameter("search", '%' + search + '%');
        }
        if (searchInFolders) {
            for (int i = 0; i < folders.size(); i++) {
                query.setParameter("folder" + i, folders.get(i) + '%');
            }
        }
        if (!isReleasable && !SOSString.isEmpty(controllerId)) {
            query.setParameter("controllerId", controllerId);
        }
        return getSession().getResultList(query);
    }

    private boolean isReleasable(ConfigurationType type) {
        switch (type) {
        case SCHEDULE:
            return true;
        default:
            return false;
        }
    }

}
