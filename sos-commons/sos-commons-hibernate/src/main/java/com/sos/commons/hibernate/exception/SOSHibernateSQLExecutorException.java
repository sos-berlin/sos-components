package com.sos.commons.hibernate.exception;

import java.sql.SQLException;

/** can occurs if SOSHibernateSQLExecutor methods are called */
public class SOSHibernateSQLExecutorException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateSQLExecutorException(SQLException cause) {
        super(cause);
    }

    public SOSHibernateSQLExecutorException(SQLException cause, String sql) {
        super(cause, sql);
    }

    public SOSHibernateSQLExecutorException(String msg) {
        super(msg);
    }

    public SOSHibernateSQLExecutorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
