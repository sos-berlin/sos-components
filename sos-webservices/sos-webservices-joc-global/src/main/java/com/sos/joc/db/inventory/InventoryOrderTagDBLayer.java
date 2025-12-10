package com.sos.joc.db.inventory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class InventoryOrderTagDBLayer extends ATagDBLayer<DBItemInventoryOrderTag> {

    private static final long serialVersionUID = 1L;

    public InventoryOrderTagDBLayer(SOSHibernateSession session) {
        super(session);
    }

    @Override
    protected String getTagTable() {
        return DBLayer.DBITEM_INV_ORDER_TAGS;
    }

    @Override
    protected String getTaggingTable() {
        return null;
    }
    
    @Override
    public DBItemInventoryOrderTag newDBItem() {
        return new DBItemInventoryOrderTag();
    }
    
    public List<String> getTagsWithGroups(Collection<String> tagNames) {
        try {
            StringBuilder sql = new StringBuilder("select new ").append(GroupedTag.class.getName());
            sql.append("(g.name, t.name) from ")
                .append(getTagTable()).append(" t left join ")
                .append(DBLayer.DBITEM_INV_TAG_GROUPS).append(" g on g.id = t.groupId");
            
            if (tagNames != null && !tagNames.isEmpty()) {
                if (tagNames.size() == 1) {
                    sql.append(" where t.name=:tagName");
                } else {
                    sql.append(" where t.name in (:tagNames)");
                }
            }
            //sql.append(" order by t.ordering");

            Query<GroupedTag> query = getSession().createQuery(sql.toString());
            if (tagNames != null && !tagNames.isEmpty()) {
                if (tagNames.size() == 1) {
                    query.setParameter("tagName", tagNames.iterator().next());
                } else {
                    query.setParameterList("tagNames", tagNames);
                }
            }

            List<GroupedTag> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }

            return result.stream().map(GroupedTag::toString).collect(Collectors.toList());

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemInventoryConfiguration> getCongurations(Collection<String> tagNames) {
        try {
            StringBuilder sql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_CONFIGURATIONS);
            sql.append(" where type in (:types)");
            sql.append(" and content like :hasTags");

            Query<DBItemInventoryConfiguration> query = getSession().createQuery(sql.toString());
            query.setParameterList("types", Arrays.asList(ConfigurationType.WORKFLOW.intValue(), ConfigurationType.FILEORDERSOURCE.intValue(),
                    ConfigurationType.SCHEDULE.intValue()));
            query.setParameter("hasTags", "%\"tags\"%");
            List<DBItemInventoryConfiguration> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;

        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
}
