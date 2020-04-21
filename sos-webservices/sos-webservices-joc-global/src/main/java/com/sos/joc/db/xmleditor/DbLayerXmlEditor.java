package com.sos.joc.db.xmleditor;

import java.util.List;
import java.util.Map;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.xmleditor.DBItemXmlEditorObject;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class DbLayerXmlEditor extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DbLayerXmlEditor(SOSHibernateSession session) {
        super(session);
    }

    public int deleteOtherObject(Long id) throws Exception {
        // JOC uses autoCommit=true, executeUpdate is not supported
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where id=:id ");
        hql.append("and objectType=:objectType");
        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        query.setParameter("objectType", ObjectType.OTHER.name());

        DBItemXmlEditorObject item = getSession().getSingleResult(query);
        if (item != null) {
            getSession().delete(item);
            return 1;
        }
        return 0;
    }

    public int deleteOtherObjects(String schedulerId) throws Exception {
        // JOC uses autoCommit=true, executeUpdate is not supported
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and objectType=:objectType");
        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("objectType", ObjectType.OTHER.name());

        List<DBItemXmlEditorObject> items = getSession().getResultList(query);
        if (items != null) {
            int size = items.size();
            for (int i = 0; i < size; i++) {
                getSession().delete(items.get(i));
            }
            return size;
        }
        return 0;
    }

    public DBItemXmlEditorObject getObject(Long id) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where id=:id");

        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
    }

    public DBItemXmlEditorObject getObject(String schedulerId, String objectType, String name) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and objectType=:objectType ");
        hql.append("and name=:name");

        Query<DBItemXmlEditorObject> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("objectType", objectType);
        query.setParameter("name", name);
        return getSession().getSingleResult(query);
    }

    public List<Map<String, Object>> getObjectProperties(String schedulerId, String objectType, String properties, String orderBy) throws Exception {
        StringBuilder hql = new StringBuilder("select new map(").append(properties).append(") from ").append(DBITEM_XML_EDITOR_OBJECTS).append(" ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and objectType=:objectType ");
        if (!SOSString.isEmpty(orderBy)) {
            hql.append(orderBy);
        }

        Query<Map<String, Object>> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("objectType", objectType);
        return getSession().getResultList(query);
    }

}
