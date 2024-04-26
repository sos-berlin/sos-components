package com.sos.commons.hibernate.exception;

import jakarta.persistence.PersistenceException;

/** can occurs if following methods are called: delete, update. Occurs if we try delete or update a row that does not exist. */
public class SOSHibernateObjectOperationStaleStateException extends SOSHibernateObjectOperationException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateObjectOperationStaleStateException(PersistenceException cause, Object item) {
        super(cause, item);
    }
}
