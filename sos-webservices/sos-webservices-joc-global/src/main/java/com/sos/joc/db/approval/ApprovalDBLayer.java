package com.sos.joc.db.approval;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;

public class ApprovalDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;

    public ApprovalDBLayer(SOSHibernateSession session) {
        super(session);
    }

    public DBItemJocApprovalRequest getRequests(Long id) throws SOSHibernateException {
        return getSession().get(DBItemJocApprovalRequest.class, id);
    }
}
