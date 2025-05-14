package com.sos.joc.db.approval;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.RequestorState;

public class ApprovalDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public ApprovalDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public DBItemJocApprovalRequest getApprovalRequest(Long id) throws SOSHibernateException {
        return getSession().get(DBItemJocApprovalRequest.class, id);
    }
    
    public void updateRequestorStatus(Long id, RequestorState state) throws SOSHibernateException {
        updateStatus(id, state.intValue(), "requestorState", null);
    }
    
    public void updateRequestorStatusInclusiveTransaction(Long id, RequestorState state) throws SOSHibernateException {
        updateStatusInclusiveTransaction(id, state.intValue(), "requestorState", null);
    }
    
    public void updateApproverStatus(Long id, ApproverState state) throws SOSHibernateException {
        updateApproverStatus(id, state, null);
    }
    
    public void updateApproverStatus(Long id, ApproverState state, String approver) throws SOSHibernateException {
        updateStatus(id, state.intValue(), "approverState", approver);
    }
    
    public void updateApproverStatusInclusiveTransaction(Long id, ApproverState state) throws SOSHibernateException {
        updateApproverStatusInclusiveTransaction(id, state, null);
    }
    
    public void updateApproverStatusInclusiveTransaction(Long id, ApproverState state, String approver) throws SOSHibernateException {
        updateStatusInclusiveTransaction(id, state.intValue(), "approverState", approver);
    }
    
    private void updateStatus(Long id, Integer state, String field, String approver) throws SOSHibernateException {
        try {
            getSession().executeUpdate(getUpdateStatusQuery(id, state, field, approver));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    private void updateStatusInclusiveTransaction(Long id, Integer state, String field, String approver) throws SOSHibernateException {
        try {
            Globals.beginTransaction(getSession());
            getSession().executeUpdate(getUpdateStatusQuery(id, state, field, approver));
            Globals.commit(getSession());
        } catch (SOSHibernateInvalidSessionException ex) {
            Globals.rollback(getSession());
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            Globals.rollback(getSession());
            throw new DBInvalidDataException(ex);
        }
    }
    
    private Query<?> getUpdateStatusQuery(Long id, Integer state, String stateField, String approver) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("update ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS).append(" set ").append(stateField).append("=:state");
        if (approver != null) {
            hql.append(", approver=:approver");
        }
        hql.append(", modified=:now where id=:id");
        Query<?> query = getSession().createQuery(hql);
        query.setParameter("id", id);
        query.setParameter(stateField, state);
        if (approver != null) {
            query.setParameter("approver", approver);
        }
        query.setParameter("now", Date.from(Instant.now()));
        return query;
    }
    
    public List<DBItemJocApprover> getApprovers() {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_JOC_APPROVERS).append(" order by ordering");
            Query<DBItemJocApprover> query = getSession().createQuery(hql);
            List<DBItemJocApprover> result = getSession().getResultList(query);
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
