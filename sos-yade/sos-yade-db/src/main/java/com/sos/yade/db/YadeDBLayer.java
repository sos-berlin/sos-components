package com.sos.yade.db;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class YadeDBLayer {

    private SOSHibernateSession session;
    
    public YadeDBLayer(SOSHibernateSession session) {
        this.session = session;
    }

    public DBItemYadeProtocols getProtocolFromDb(String hostname, Integer port, Integer protocol, String account)
    		throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBItemYadeProtocols.class.getSimpleName());
        sql.append(" where hostname = :hostname");
        sql.append(" and");
        sql.append(" port = :port");
        sql.append(" and");
        sql.append(" protocol = :protocol");
        sql.append(" and");
        sql.append(" account = :account");
        Query<DBItemYadeProtocols> query = getSession().createQuery(sql.toString());
        query.setParameter("hostname", hostname);
        query.setParameter("port", port);
        query.setParameter("protocol", protocol);
        query.setParameter("account", account);
        return getSession().getSingleResult(query);
    }
    
    public DBItemYadeTransfers getTransferFromDb(Long id) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBItemYadeTransfers.class.getSimpleName());
        sql.append(" where id = :id");
        Query<DBItemYadeTransfers> query = getSession().createQuery(sql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
    }
    
    public DBItemYadeFiles getTransferFileFromDB(Long id) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBItemYadeFiles.class.getSimpleName());
        sql.append(" where id = :id");
        Query<DBItemYadeFiles> query = getSession().createQuery(sql.toString());
        query.setParameter("id", id);
        return getSession().getSingleResult(query);
    }
    
    public DBItemYadeFiles getTransferFileFromDbByConstraint(Long transferId, String sourcePath) throws SOSHibernateException {
        StringBuilder sql = new StringBuilder();
        sql.append("from ").append(DBItemYadeFiles.class.getSimpleName());
        sql.append(" where transferId = :transferId");
        sql.append(" and sourcePath = :sourcePath");
        Query<DBItemYadeFiles> query = getSession().createQuery(sql.toString());
        query.setParameter("transferId", transferId);
        query.setParameter("sourcePath", sourcePath);
        return getSession().getSingleResult(query);
    }
    
    public SOSHibernateSession getSession() {
        return this.session;
    }
}
