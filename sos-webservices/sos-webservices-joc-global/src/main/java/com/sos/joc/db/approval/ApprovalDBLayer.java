package com.sos.joc.db.approval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.security.foureyes.ApprovalsFilter;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.RequestorState;

public class ApprovalDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public ApprovalDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public DBItemJocApprovalRequest getApprovalRequest(Long id) {
        try {
            DBItemJocApprovalRequest item = getSession().get(DBItemJocApprovalRequest.class, id);
            if (item == null) {
                throw new DBMissingDataException("Couldn't find an approval request with id " + id);
            }
            return item;
        } catch (SOSHibernateInvalidSessionException ex) {
            Globals.rollback(getSession());
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            Globals.rollback(getSession());
            throw new DBInvalidDataException(ex);
        }
    }
    
    public List<DBItemJocApprovalRequest> getApprovalRequests(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        if (ids.size() == 1) {
            return Collections.singletonList(getApprovalRequest(ids.iterator().next()));
        }
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS);
            hql.append(" where id in (:ids)");
                
            Collection<List<Long>> chunkedIds = getChunkedCollection(ids);
            List<DBItemJocApprovalRequest> result = new ArrayList<>();
            if (chunkedIds != null) {
                if (chunkedIds.size() == 1) {
                    result = getApprovalRequests(hql, chunkedIds.iterator().next());
                } else {
                    for (List<Long> chunk : chunkedIds) {
                        List<DBItemJocApprovalRequest> result1 = getApprovalRequests(hql, chunk);
                        if (result1 != null) {
                            result.addAll(result1);
                        }
                    }
                }
            }
            
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
    
    private List<DBItemJocApprovalRequest> getApprovalRequests(StringBuilder hql, Collection<Long> ids) throws SOSHibernateException {
        Query<DBItemJocApprovalRequest> query = getSession().createQuery(hql);
        query.setParameterList("ids", ids);
        return getSession().getResultList(query);
    }
    
    public List<DBItemJocApprovalRequest> getApprovalRequests(ApprovalsFilter filter) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS);
            if (filter != null) {
                List<String> clauses = new ArrayList<>(4);
                if (filter.getApprovers() != null && !filter.getApprovers().isEmpty()) {
                    if (filter.getApprovers().size() == 1) {
                        clauses.add("approver=:approver");
                    } else {
                        clauses.add("approver in (:approvers)");
                    }
                }
                if (filter.getRequestors() != null && !filter.getRequestors().isEmpty()) {
                    if (filter.getRequestors().size() == 1) {
                        clauses.add("requestor=:requestor");
                    } else {
                        clauses.add("requestor in (:requestors)");
                    }
                }
                if (filter.getApproverStates() != null && !filter.getApproverStates().isEmpty()) {
                    if (filter.getApproverStates().size() == 1) {
                        clauses.add("approverState=:approverState");
                    } else {
                        clauses.add("approverState in (:approverStates)");
                    }
                }
                if (filter.getRequestorStates() != null && !filter.getRequestorStates().isEmpty()) {
                    if (filter.getRequestorStates().size() == 1) {
                        clauses.add("requestorState=:requestorState");
                    } else {
                        clauses.add("requestorState in (:requestorStates)");
                    }
                }
                if (!clauses.isEmpty()) {
                    hql.append(clauses.stream().collect(Collectors.joining(" and ", " where ", "")));
                }
            }
            hql.append(" order by requestorStateDate desc");
            
            Query<DBItemJocApprovalRequest> query = getSession().createQuery(hql);
            if (filter != null) {
                if (filter.getLimit() > 0) {
                    query.setMaxResults(filter.getLimit());
                }
                if (filter.getApprovers() != null && !filter.getApprovers().isEmpty()) {
                    if (filter.getApprovers().size() == 1) {
                        query.setParameter("approver", filter.getApprovers().iterator().next());
                    } else {
                        query.setParameterList("approvers", filter.getApprovers());
                    }
                }
                if (filter.getRequestors() != null && !filter.getRequestors().isEmpty()) {
                    if (filter.getRequestors().size() == 1) {
                        query.setParameter("requestor", filter.getRequestors().iterator().next());
                    } else {
                        query.setParameterList("requestors", filter.getRequestors());
                    }
                }
                if (filter.getApproverStates() != null && !filter.getApproverStates().isEmpty()) {
                    if (filter.getApproverStates().size() == 1) {
                        query.setParameter("approverState", filter.getApproverStates().iterator().next().intValue());
                    } else {
                        query.setParameterList("approverStates", filter.getApproverStates().stream().map(ApproverState::intValue).toList());
                    }
                }
                if (filter.getRequestorStates() != null && !filter.getRequestorStates().isEmpty()) {
                    if (filter.getRequestorStates().size() == 1) {
                        query.setParameter("requestorState", filter.getRequestorStates().iterator().next().intValue());
                    } else {
                        query.setParameterList("requestorStates", filter.getRequestorStates().stream().map(RequestorState::intValue).toList());
                    }
                }
            }
            
            List<DBItemJocApprovalRequest> result = getSession().getResultList(query);
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
    
    public Long getNumOfPendingApprovals(String approver) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("select count(*) from ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS);
            hql.append(" where approver=:approver");
            hql.append(" and approverState=:approverState");
            hql.append(" and requestorState=:requestorState");
            Query<Long> query = getSession().createQuery(hql);
            query.setParameter("approver", approver);
            query.setParameter("approverState", ApproverState.PENDING.intValue());
            query.setParameter("requestorState", RequestorState.REQUESTED.intValue());
            return query.getSingleResult();
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    public void updateRequestorStatus(Long id, RequestorState state) {
        updateStatus(id, state.intValue(), "requestorState", null);
    }
    
    public void updateRequestorStatusInclusiveTransaction(Long id, RequestorState state) {
        updateStatusInclusiveTransaction(id, state.intValue(), "requestorState", null);
    }
    
    public void updateRequestorStatusInclusiveTransaction(Collection<Long> ids, RequestorState state) {
        updateStatusInclusiveTransaction(ids, state.intValue(), "requestorState", null);
    }
    
    public void updateApproverStatus(Long id, ApproverState state) {
        updateApproverStatus(id, state, null);
    }
    
    public void updateApproverStatus(Long id, ApproverState state, String approver) {
        updateStatus(id, state.intValue(), "approverState", approver);
    }
    
    public void updateApproverStatusInclusiveTransaction(Long id, ApproverState state) {
        updateApproverStatusInclusiveTransaction(id, state, null);
    }
    
    public void updateApproverStatusInclusiveTransaction(Collection<Long> ids, ApproverState state) {
        updateApproverStatusInclusiveTransaction(ids, state, null);
    }
    
    public void updateApproverStatusInclusiveTransaction(Long id, ApproverState state, String approver) {
        updateStatusInclusiveTransaction(id, state.intValue(), "approverState", approver);
    }
    
    public void updateApproverStatusInclusiveTransaction(Collection<Long> ids, ApproverState state, String approver) {
        updateStatusInclusiveTransaction(ids, state.intValue(), "approverState", approver);
    }
    
    private void updateStatus(Long id, Integer state, String field, String approver) {
        try {
            getSession().executeUpdate(getUpdateStatusQuery(id, state, field, approver));
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
    
    private void updateStatusInclusiveTransaction(Long id, Integer state, String field, String approver) {
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
    
    private void updateStatusInclusiveTransaction(Collection<Long> ids, Integer state, String field, String approver) {

        if (ids != null && !ids.isEmpty()) {
            Collection<List<Long>> chunkedIds = getChunkedCollection(ids);
            if (chunkedIds != null) {
                try {
                    for (List<Long> chunk : chunkedIds) {
                        Globals.beginTransaction(getSession());
                        getSession().executeUpdate(getUpdateStatusQuery(chunk, state, field, approver));
                        Globals.commit(getSession());
                    }
                } catch (SOSHibernateInvalidSessionException ex) {
                    Globals.rollback(getSession());
                    throw new DBConnectionRefusedException(ex);
                } catch (Exception ex) {
                    Globals.rollback(getSession());
                    throw new DBInvalidDataException(ex);
                }
            }
        }
    }
    
    private Query<?> getUpdateStatusQuery(Long id, Integer state, String stateField, String approver) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        String stateDateField = stateField + "Date";
        hql.append("update ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS).append(" set ").append(stateField).append("=:state, ");
        if (approver != null) {
            hql.append("approver=:approver, ");
        }
        hql.append(stateDateField).append("=:now where id=:id");
        Query<?> query = getSession().createQuery(hql);
        query.setParameter("id", id);
        query.setParameter("state", state);
        if (approver != null) {
            query.setParameter("approver", approver);
        }
        query.setParameter("now", Date.from(Instant.now()));
        return query;
    }
    
    private Query<?> getUpdateStatusQuery(Collection<Long> ids, Integer state, String stateField, String approver) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        String stateDateField = stateField + "Date";
        hql.append("update ").append(DBLayer.DBITEM_JOC_APPROVAL_REQUESTS).append(" set ").append(stateField).append("=:state, ");
        if (approver != null) {
            hql.append("approver=:approver, ");
        }
        hql.append(stateDateField).append("=:now where ");
        if (ids.size() == 1) {
            hql.append("id=:id");
        } else {
            hql.append("id in (:ids)");
        }
        Query<?> query = getSession().createQuery(hql);
        if (ids.size() == 1) {
            query.setParameter("id", ids.iterator().next());
        } else {
            query.setParameterList("ids", ids);
        }
        query.setParameter("state", state);
        if (approver != null) {
            query.setParameter("approver", approver);
        }
        query.setParameter("now", Date.from(Instant.now()));
        return query;
    }
    
    private static <T> Collection<List<T>> getChunkedCollection(Collection<T> coll) {
        if (coll != null) {
            AtomicInteger counter = new AtomicInteger();
            return coll.stream().distinct().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / SOSHibernate.LIMIT_IN_CLAUSE)).values();
        }
        return null;
    }
    
    public List<DBItemJocApprover> getApprovers() {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("from ").append(DBLayer.DBITEM_JOC_APPROVERS).append(" order by ordering asc");
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
    
    public DBItemJocApprover getApprover(String accountName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append("from ").append(DBLayer.DBITEM_JOC_APPROVERS).append(" where accountName = :accountName");
        Query<DBItemJocApprover> query = getSession().createQuery(hql);
        query.setParameter("accountName", accountName);
        return getSession().getSingleResult(query);
    }
    
    public Integer getMaxOrdering () throws SOSHibernateException {
        StringBuilder hql = new StringBuilder();
        hql.append(" select max(ordering) from ").append(DBLayer.DBITEM_JOC_APPROVERS);
        Query<Integer> query = getSession().createQuery(hql);
        Integer result = getSession().getSingleResult(query);
        if(result == null) {
            return 0;
        } else {
            return result;
        }
    }
}
