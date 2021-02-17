package com.sos.joc.cluster.db;

import java.util.Date;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;

public class DBLayerJocCluster extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerJocCluster(SOSHibernateSession session) {
        super(session);
    }

    public DBItemInventoryOperatingSystem getOS(String hostname) throws Exception {
        StringBuilder sql = new StringBuilder("from ");
        sql.append(DBLayer.DBITEM_INV_OPERATING_SYSTEMS);
        sql.append(" where hostname=:hostname");
        Query<DBItemInventoryOperatingSystem> query = getSession().createQuery(sql.toString());
        query.setParameter("hostname", hostname);
        return getSession().getSingleResult(query);
    }

    public DBItemJocCluster getCluster() throws Exception {
        StringBuilder sql = new StringBuilder("from ");
        sql.append(DBLayer.DBITEM_JOC_CLUSTER);
        Query<DBItemJocCluster> query = getSession().createQuery(sql.toString());
        return getSession().getSingleResult(query);
    }

    public int deleteCluster(String memberId, boolean checkSwitchMemberId) throws Exception {
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(DBLayer.DBITEM_JOC_CLUSTER).append(" ");
        sql.append("where memberId=:memberId");
        if (checkSwitchMemberId) {
            sql.append(" and switchMemberId is null");
        }
        Query<DBItemJocCluster> query = getSession().createQuery(sql.toString());
        query.setParameter("memberId", memberId);
        return getSession().executeUpdate(query);
    }

    public DBItemJocInstance getInstance(String memberId) throws Exception {
        StringBuilder sql = new StringBuilder("from ");
        sql.append(DBLayer.DBITEM_JOC_INSTANCES);
        sql.append(" where memberId=:memberId");
        Query<DBItemJocInstance> query = getSession().createQuery(sql.toString());
        query.setParameter("memberId", memberId);
        return getSession().getSingleResult(query);
    }

    public int updateInstanceHeartBeat(String memberId) throws Exception {
        StringBuilder sql = new StringBuilder("update ");
        sql.append(DBLayer.DBITEM_JOC_INSTANCES);
        sql.append(" set heartBeat=:heartBeat");
        sql.append(" where memberId=:memberId");
        Query<DBItemJocInstance> query = getSession().createQuery(sql.toString());
        query.setParameter("heartBeat", new Date());
        query.setParameter("memberId", memberId);
        return getSession().executeUpdate(query);
    }
}
