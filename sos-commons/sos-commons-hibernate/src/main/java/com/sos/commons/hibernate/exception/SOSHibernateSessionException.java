package com.sos.commons.hibernate.exception;

import jakarta.persistence.PersistenceException;

/** can occurs if following methods are called: clearSession, sessionDoWork */
public class SOSHibernateSessionException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSessionException(IllegalStateException cause) {
        super(cause);
    }

    public SOSHibernateSessionException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateSessionException(String msg) {
        super(msg);
    }
}
