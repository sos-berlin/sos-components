package com.sos.commons.hibernate.exception;

import javax.persistence.PersistenceException;

import org.hibernate.query.Query;

/** can occurs if Query/NativeQuery methods are called */
public class SOSHibernateQueryException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateQueryException(IllegalArgumentException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateQueryException(IllegalStateException cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateQueryException(IllegalStateException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateQueryException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateQueryException(PersistenceException cause, Query<?> query) {
        super(cause, query);
    }

    public SOSHibernateQueryException(PersistenceException cause, String stmt) {
        super(cause, stmt);
    }

    public SOSHibernateQueryException(String msg) {
        super(msg);
    }
}
