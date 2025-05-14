package com.sos.joc.db.approval;

import java.time.Instant;
import java.util.Date;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.RequestorState;

public class ApprovalDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public ApprovalDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public DBItemJocApprovalRequest getRequests(Long id) throws SOSHibernateException {
        return getSession().get(DBItemJocApprovalRequest.class, id);
    }
    
    public void updateRequestorStatus(Long id, RequestorState state) throws SOSHibernateException {
        updateStatus(id, state.intValue(), "requestorState");
    }
    
    public void updateRequestorStatusInclusiveTransaction(Long id, RequestorState state) throws SOSHibernateException {
        updateStatusInclusiveTransaction(id, state.intValue(), "requestorState");
    }
    
    public void updateApproverStatus(Long id, ApproverState state) throws SOSHibernateException {
        updateStatus(id, state.intValue(), "approverState");
    }
    
    public void updateApproverStatusInclusiveTransaction(Long id, ApproverState state) throws SOSHibernateException {
        updateStatusInclusiveTransaction(id, state.intValue(), "approverState");
    }
    
    private void updateStatus(Long id, Integer state, String field) throws SOSHibernateException {
        try {
            getSession().executeUpdate(getUpdateStatusQuery(id, state, field));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    private void updateStatusInclusiveTransaction(Long id, Integer state, String field) throws SOSHibernateException {
        try {
            Globals.beginTransaction(getSession());
            getSession().executeUpdate(getUpdateStatusQuery(id, state, field));
            Globals.commit(getSession());
        } catch (SOSHibernateInvalidSessionException ex) {
            Globals.rollback(getSession());
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            Globals.rollback(getSession());
            throw new DBInvalidDataException(ex);
        }
    }
    
    private Query<?> getUpdateStatusQuery(Long id, Integer state, String field) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("update ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS).append(" set ").append(field).append(
                "=:state, modified=:now where id=:id");
        Query<?> query = getSession().createQuery(hql);
        query.setParameter("id", id);
        query.setParameter(field, state);
        query.setParameter("now", Date.from(Instant.now()));
        return query;
    }
    
}
