package com.sos.joc.monitoring.db;

import java.util.Date;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;

public class DBLayerMonitoring {

    private final String identifier;
    private SOSHibernateSession session;

    public DBLayerMonitoring(String identifier) {
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

    public String getIdentifier() {
        return identifier;
    }

    public DBItemHistoryOrder getHistoryOrder(Long historyId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_HISTORY_ORDER).append(" ");
        hql.append("where id=:historyId");

        Query<DBItemHistoryOrder> query = session.createQuery(hql.toString());
        query.setParameter("historyId", historyId);
        return session.getSingleResult(query);
    }

    public boolean updateOrderOnResumed(HistoryOrderBean hob) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("severity", hob.getSeverity());
        query.setParameter("state", hob.getState());
        query.setParameter("stateTime", hob.getStateTime());
        query.setParameter("historyId", hob.getHistoryId());

        int r = session.executeUpdate(query);
        return r != 0;
    }

    public boolean updateOrderOnForked(HistoryOrderBean hob) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",hasChildren=true ");
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("severity", hob.getSeverity());
        query.setParameter("state", hob.getState());
        query.setParameter("stateTime", hob.getStateTime());
        query.setParameter("historyId", hob.getHistoryId());

        int r = session.executeUpdate(query);
        return r != 0;
    }

    public boolean updateOrder(HistoryOrderBean hob) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("set modified=:modified ");

        if (hob.getEndTime() != null) {
            hql.append(",endTime=:endTime ");
            hql.append(",endWorkflowPosition=:endWorkflowPosition ");
            hql.append(",endHistoryOrderStepId=:endHistoryOrderStepId ");
        }
        if (hob.getCurrentHistoryOrderStepId() != null) {
            hql.append(",currentHistoryOrderStepId=:currentHistoryOrderStepId ");
        }
        hql.append(",severity=:severity ");
        hql.append(",state=:state ");
        hql.append(",stateTime=:stateTime ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorReturnCode=:errorReturnCode ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append(",logId=:logId ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", new Date());

        if (hob.getEndTime() != null) {
            query.setParameter("endTime", hob.getEndTime());
            query.setParameter("endWorkflowPosition", hob.getEndWorkflowPosition());
            query.setParameter("endHistoryOrderStepId", hob.getEndHistoryOrderStepId());
        }
        if (hob.getCurrentHistoryOrderStepId() != null) {
            query.setParameter("currentHistoryOrderStepId", hob.getCurrentHistoryOrderStepId());
        }
        query.setParameter("severity", hob.getSeverity());
        query.setParameter("state", hob.getState());
        query.setParameter("stateTime", hob.getStateTime());
        query.setParameter("error", hob.getError());
        query.setParameter("errorState", hob.getErrorState());
        query.setParameter("errorReason", hob.getErrorReason());
        query.setParameter("errorReturnCode", hob.getErrorReturnCode());
        query.setParameter("errorCode", DBItemMonitoringOrder.normalizeErrorCode(hob.getErrorCode()));
        query.setParameter("errorText", DBItemMonitoringOrder.normalizeErrorText(hob.getErrorText()));
        query.setParameter("logId", hob.getLogId());
        query.setParameter("historyId", hob.getHistoryId());

        int r = session.executeUpdate(query);
        return r != 0;
    }

    public boolean updateOrderOnOrderStep(Long historyId, Long currentHistoryOrderStepId) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MONITORING_ORDER).append(" ");
        hql.append("set modified=:modified ");
        hql.append(",currentHistoryOrderStepId=:currentHistoryOrderStepId ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrder> query = session.createQuery(hql.toString());
        query.setParameter("modified", new Date());
        query.setParameter("currentHistoryOrderStepId", currentHistoryOrderStepId);
        query.setParameter("historyId", historyId);

        int r = session.executeUpdate(query);
        return r != 0;
    }

    public int setOrderStepEnd(HistoryOrderStepBean hosb) throws SOSHibernateException {

        StringBuilder hql = new StringBuilder("update ").append(DBLayer.DBITEM_MONITORING_ORDER_STEP).append(" ");
        hql.append("set endTime=:endTime ");
        hql.append(",endParameters=:endParameters ");
        hql.append(",returnCode=:returnCode ");
        hql.append(",severity=:severity ");
        hql.append(",error=:error ");
        hql.append(",errorState=:errorState ");
        hql.append(",errorReason=:errorReason ");
        hql.append(",errorCode=:errorCode ");
        hql.append(",errorText=:errorText ");
        hql.append(",modified=:modified ");
        hql.append("where historyId=:historyId");

        Query<DBItemMonitoringOrderStep> query = session.createQuery(hql.toString());
        query.setParameter("endTime", hosb.getEndTime());
        query.setParameter("endParameters", hosb.getEndParameters());
        query.setParameter("returnCode", hosb.getReturnCode());
        query.setParameter("severity", hosb.getSeverity());
        query.setParameter("error", hosb.getError());
        query.setParameter("errorState", hosb.getErrorState());
        query.setParameter("errorReason", hosb.getErrorReason());
        query.setParameter("errorCode", DBItemHistoryOrderStep.normalizeErrorCode(hosb.getErrorCode()));
        query.setParameter("errorText", DBItemHistoryOrderStep.normalizeErrorText(hosb.getErrorText()));
        query.setParameter("modified", new Date());
        query.setParameter("historyId", hosb.getHistoryId());
        return session.executeUpdate(query);
    }

}
