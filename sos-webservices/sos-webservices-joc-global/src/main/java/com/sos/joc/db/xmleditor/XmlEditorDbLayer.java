package com.sos.joc.db.xmleditor;

import java.util.List;
import java.util.Map;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class XmlEditorDbLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public XmlEditorDbLayer(SOSHibernateSession session) {
        super(session);
    }

    // YADE, OTHER
    public int deleteMultiple(ObjectType type, Long id) throws Exception {
        // JOC uses autoCommit=true, executeUpdate is not supported
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_CONFIGURATIONS).append(" ");
        hql.append("where id=:id ");
        hql.append("and type=:type");
        Query<DBItemXmlEditorConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("type", type.name());

        DBItemXmlEditorConfiguration item = getSession().getSingleResult(query);
        if (item != null) {
            getSession().delete(item);
            return 1;
        }
        return 0;
    }

    public int deleteAllMultiple(ObjectType type) throws Exception {
        // JOC uses autoCommit=true, executeUpdate is not supported
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");

        Query<DBItemXmlEditorConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", type.name());

        List<DBItemXmlEditorConfiguration> items = getSession().getResultList(query);
        if (items != null) {
            int size = items.size();
            for (int i = 0; i < size; i++) {
                getSession().delete(items.get(i));
            }
            return size;
        }
        return 0;
    }

    public DBItemXmlEditorConfiguration getObject(Long id) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_CONFIGURATIONS).append(" ");
        hql.append("where id=:id");

        Query<DBItemXmlEditorConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
    }

    public DBItemXmlEditorConfiguration getObject(String type, String name) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_CONFIGURATIONS).append(" ");
        hql.append("where type=:type ");
        hql.append("and name=:name ");

        Query<DBItemXmlEditorConfiguration> query = getSession().createQuery(hql.toString());
        query.setParameter("type", type);
        query.setParameter("name", name);
        return getSession().getSingleResult(query);
    }

    public List<Map<String, Object>> getObjectProperties(String type, String properties, String orderBy) throws Exception {
        StringBuilder hql = new StringBuilder("select new map(").append(properties).append(") from ").append(DBITEM_XML_EDITOR_CONFIGURATIONS).append(
                " ");
        hql.append("where type=:type ");
        if (!SOSString.isEmpty(orderBy)) {
            hql.append(orderBy);
        }

        Query<Map<String, Object>> query = getSession().createQuery(hql.toString());
        query.setParameter("type", type);
        return getSession().getResultList(query);
    }

}
