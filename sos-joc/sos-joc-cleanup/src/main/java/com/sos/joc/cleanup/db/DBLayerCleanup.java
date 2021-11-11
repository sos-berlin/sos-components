package com.sos.joc.cleanup.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocVariable;

public class DBLayerCleanup {

    private final String identifier;
    private SOSHibernateSession session;

    public DBLayerCleanup(String identifier) {
        this.identifier = identifier;
    }

    public void setSession(SOSHibernateSession hibernateSession) {
        close();
        session = hibernateSession;
        session.setIdentifier(identifier);
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public void rollback() {
        if (session != null) {
            try {
                session.rollback();
            } catch (Throwable e) {
            }
        }
    }

    public void close() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public DBItemJocVariable getVariable(String name) throws SOSHibernateException {
        String hql = String.format("select name,textValue from %s where name = :name", DBLayer.DBITEM_JOC_VARIABLES);
        Query<Object[]> query = session.createQuery(hql);
        query.setParameter("name", name);
        Object[] o = session.getSingleResult(query);
        if (o == null) {
            return null;
        }
        DBItemJocVariable item = new DBItemJocVariable();
        item.setName(name);
        item.setTextValue(o[1].toString());
        return item;
    }

    public int deleteVariable(String name) throws SOSHibernateException {
        String hql = String.format("delete from %s where name = :name", DBLayer.DBITEM_JOC_VARIABLES);
        Query<DBItemJocVariable> query = session.createQuery(hql);
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }

    public DBItemJocVariable insertVariable(String name, String val) throws SOSHibernateException {
        DBItemJocVariable item = new DBItemJocVariable();
        item.setName(name);
        item.setTextValue(String.valueOf(val));
        session.save(item);
        return item;
    }

    public int updateVariable(String name, String value) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_JOC_VARIABLES).append(" ");
        hql.append("set textValue=:textValue ");
        hql.append("where name=:name");
        Query<DBItemJocVariable> query = session.createQuery(hql.toString());
        query.setParameter("textValue", value);
        query.setParameter("name", name);
        return session.executeUpdate(query);
    }

    public boolean mainOrderLogNotFinished(Long id) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select count(id) from ").append(DBLayer.DBITEM_HISTORY_ORDERS).append(" ");
        hql.append("where id=:id ");
        hql.append("and parentId=0 ");
        hql.append("and logId=0 ");

        Query<Long> query = session.createQuery(hql.toString());
        query.setParameter("id", id);
        Long result = query.getSingleResult();

        return result.intValue() > 0;
    }

}
