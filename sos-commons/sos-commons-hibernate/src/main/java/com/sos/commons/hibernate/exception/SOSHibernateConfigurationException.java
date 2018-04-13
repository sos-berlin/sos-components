package com.sos.commons.hibernate.exception;

import javax.persistence.PersistenceException;

public class SOSHibernateConfigurationException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateConfigurationException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateConfigurationException(String msg) {
        super(msg);
    }

    public SOSHibernateConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
