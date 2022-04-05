package com.sos.commons.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.internal.SessionImpl;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateConnectionException;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.commons.hibernate.exception.SOSHibernateLockAcquisitionException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.commons.hibernate.exception.SOSHibernateQueryException;
import com.sos.commons.hibernate.exception.SOSHibernateQueryNonUniqueResultException;
import com.sos.commons.hibernate.exception.SOSHibernateSessionException;
import com.sos.commons.hibernate.exception.SOSHibernateTransactionException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;

public class SOSHibernateSession implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSession.class);
    private static final Logger CONNECTION_POOL_LOGGER = LoggerFactory.getLogger("ConnectionPool");
    private static final long serialVersionUID = 1L;
    private boolean autoCommit = false;
    private Object currentSession;
    private FlushMode defaultHibernateFlushMode = FlushMode.ALWAYS;
    private final SOSHibernateFactory factory;
    private String identifier;
    private String logIdentifier;
    private boolean isGetCurrentSession = false;
    private boolean isStatelessSession = false;
    private boolean isTransactionOpened = false;
    private SOSHibernateSQLExecutor sqlExecutor;

    /** use factory.openSession(), factory.openStatelessSession() or factory.getCurrentSession() */
    protected SOSHibernateSession(SOSHibernateFactory hibernateFactory) {
        setIdentifier(null);
        factory = hibernateFactory;
    }

    /** @throws SOSHibernateOpenSessionException */
    protected void openSession() throws SOSHibernateOpenSessionException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "openSession") : "";
        if (currentSession != null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%sclose currentSession", method));
            }
            closeSession();
        }

        if (factory == null || factory.getSessionFactory() == null) {
            throw new SOSHibernateOpenSessionException("no valid session factory available");
        }

        try {
            String sessionName = null;
            if (isStatelessSession) {
                currentSession = factory.getSessionFactory().openStatelessSession();
                sessionName = "StatelessSession";
            } else {
                Session session = null;
                if (isGetCurrentSession) {
                    session = factory.getSessionFactory().getCurrentSession();
                    sessionName = "getCurrentSession";
                } else {
                    session = factory.getSessionFactory().openSession();
                    sessionName = "Session";
                }
                if (defaultHibernateFlushMode != null) {
                    session.setHibernateFlushMode(defaultHibernateFlushMode);
                }
                currentSession = session;
            }
            try {
                autoCommit = factory.getAutoCommit();
            } catch (SOSHibernateConfigurationException e) {
                throw new SOSHibernateOpenSessionException("can't get configured autocommit", e);
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%s, autoCommit=%s", method, sessionName, autoCommit));
            }
            onOpenSession();
        } catch (IllegalStateException e) {
            throw new SOSHibernateOpenSessionException(e);
        } catch (PersistenceException e) {
            throw new SOSHibernateOpenSessionException(e);
        }
    }

    protected void setIsGetCurrentSession(boolean val) {
        isGetCurrentSession = val;
    }

    protected void setIsStatelessSession(boolean val) {
        isStatelessSession = val;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateTransactionException */
    public void beginTransaction() throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "beginTransaction") : "";
        if (autoCommit) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%sskip (autoCommit is true)", method));
            }
            return;
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (isTransactionOpened) {
            LOGGER.warn(String.format("%sskip (transaction is already opened)", method));
            return;
        }
        LOGGER.debug(method);
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                try {
                    session.beginTransaction();
                } catch (NullPointerException e) {
                    throw new SOSHibernateOpenSessionException("session/connection can't be acquired", e.getCause());
                }
            } else {
                Session session = ((Session) currentSession);
                try {
                    session.beginTransaction();
                } catch (NullPointerException e) {
                    throw new SOSHibernateOpenSessionException("session/connection can't be acquired", e.getCause());
                }
            }
            isTransactionOpened = true;
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateSessionException */
    public void clearSession() throws SOSHibernateException {
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "clearSession"));
        }
        try {
            if (!isStatelessSession) {
                Session session = (Session) currentSession;
                session.clear();
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateSessionException(e));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateSessionException(e));
        }
    }

    public void close() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(SOSHibernate.getMethodName(logIdentifier, "close"));
        }
        CONNECTION_POOL_LOGGER.debug("--------> RELEASE CONNECTION: " + getIdentifier() + " (" + SOSClassUtil.getMethodName(3) + ") --------");
        closeTransaction();
        closeSession();
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateTransactionException */
    public void commit() throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "commit") : "";
        if (autoCommit) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%sskip (autoCommit is true)", method));
            }
            return;
        }
        LOGGER.debug(method);
        Transaction tr = getTransaction();
        if (tr == null) {
            isTransactionOpened = false;
            throw new SOSHibernateTransactionException("transaction is NULL");
        }
        try {
            if (!isStatelessSession) {
                ((Session) currentSession).flush();
            }
            tr.commit();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        } finally {
            isTransactionOpened = false;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> NativeQuery<T> createNativeQuery(String sql) throws SOSHibernateException {
        return createNativeQuery(sql, null);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    @SuppressWarnings("unchecked")
    public <T> NativeQuery<T> createNativeQuery(String sql, Class<T> resultType) throws SOSHibernateException {
        if (SOSString.isEmpty(sql)) {
            throw new SOSHibernateQueryException("sql statement is empty");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", sql);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("%s[sql][%s]", SOSHibernate.getMethodName(logIdentifier, "createNativeQuery"), sql));
        }
        NativeQuery<T> q = null;
        try {
            if (isStatelessSession) {
                if (resultType == null) {
                    q = ((StatelessSession) currentSession).createNativeQuery(sql);
                } else {
                    // q = ((StatelessSession) currentSession).createNativeQuery(sql, resultType);
                    q = ((StatelessSession) currentSession).createNativeQuery(sql);
                    q = (NativeQuery<T>) setResultTransformer(q, resultType);
                }
            } else {
                if (resultType == null) {
                    q = ((Session) currentSession).createNativeQuery(sql);
                } else {
                    // q = ((Session) currentSession).createNativeQuery(sql, entityClass);
                    q = ((Session) currentSession).createNativeQuery(sql);
                    q = (NativeQuery<T>) setResultTransformer(q, resultType);
                }
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, sql));
        } catch (IllegalArgumentException e) {
            throw new SOSHibernateQueryException(e, sql);
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, sql));
        }
        return q;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> Query<T> createQuery(StringBuilder hql) throws SOSHibernateException {
        return createQuery(hql.toString(), null);
    }

    public <T> Query<T> createQuery(String hql) throws SOSHibernateException {
        return createQuery(hql, null);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    @SuppressWarnings("unchecked")
    public <T> Query<T> createQuery(String hql, Class<T> resultType) throws SOSHibernateException {
        if (SOSString.isEmpty(hql)) {
            throw new SOSHibernateQueryException("hql statement is empty");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", hql);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("%s[hql][%s]", SOSHibernate.getMethodName(logIdentifier, "createQuery"), hql));
        }
        Query<T> q = null;
        try {
            if (isStatelessSession) {
                if (resultType == null) {
                    q = ((StatelessSession) currentSession).createQuery(hql);
                } else {
                    // q = ((StatelessSession) currentSession).createQuery(hql, entityClass);
                    q = ((StatelessSession) currentSession).createQuery(hql);
                    q = setResultTransformer(q, resultType);
                }
            } else {
                if (resultType == null) {
                    q = ((Session) currentSession).createQuery(hql);
                } else {
                    // q = ((Session) currentSession).createQuery(hql, resultType);
                    q = ((StatelessSession) currentSession).createQuery(hql);
                    q = setResultTransformer(q, resultType);
                }
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, hql));
        } catch (IllegalArgumentException e) {
            throw new SOSHibernateQueryException(e, hql);
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, hql));
        }
        return q;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateObjectOperationException */
    public void delete(Object item) throws SOSHibernateException {
        if (item == null) {
            throw new SOSHibernateObjectOperationException("item is NULL", item);
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s%s", SOSHibernate.getMethodName(logIdentifier, "delete"), SOSHibernate.toString(item)));
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.delete(item);
            } else {
                Session session = ((Session) currentSession);
                session.delete(item);
                session.flush();
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        }
    }

    /** execute an update or delete (NativeQuery or Query)
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public int executeUpdate(Query<?> query) throws SOSHibernateException {
        if (query == null) {
            throw new SOSHibernateQueryException("query is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        debugQuery("executeUpdate", query, null);
        try {
            return query.executeUpdate();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
            return 0;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
            return 0;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public int executeUpdate(String hql) throws SOSHibernateException {
        return executeUpdate(createQuery(hql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public int executeUpdateNativeQuery(String sql) throws SOSHibernateException {
        return executeUpdate(createNativeQuery(sql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateObjectOperationException */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> entityClass, Serializable id) throws SOSHibernateException {
        if (entityClass == null) {
            throw new SOSHibernateObjectOperationException("entityClass is NULL", null);
        }
        if (id == null) {
            throw new SOSHibernateObjectOperationException("id is NULL", null);
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%sentityClass=%s, id=%s", SOSHibernate.getMethodName(logIdentifier, "get"), entityClass.getName(), id));
        }
        T item = null;
        try {
            if (isStatelessSession) {
                item = (T) ((StatelessSession) currentSession).get(entityClass, id);
            } else {
                item = ((Session) currentSession).get(entityClass, id);
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
            return null;
        }
        return item;
    }

    public CacheMode getCacheMode() {
        if (!isStatelessSession && currentSession != null) {
            Session session = (Session) currentSession;
            return session.getCacheMode();
        }
        return null;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateConnectionException */
    public Connection getConnection() throws SOSHibernateException {
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSessionImpl sf = (StatelessSessionImpl) currentSession;
                try {
                    return sf.connection();
                } catch (NullPointerException e) {
                    throw new SOSHibernateConnectionException("can't get the SQL connection from the StatelessSessionImpl(NullPointerException)");
                }
            } else {
                SessionImpl sf = (SessionImpl) currentSession;
                try {
                    return sf.connection();
                } catch (NullPointerException e) {
                    throw new SOSHibernateConnectionException("can't get the SQL connection from the SessionImpl(NullPointerException)");
                }
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateConnectionException(e));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateConnectionException(e));
            return null;
        }
    }

    public Object getCurrentSession() {
        return currentSession;
    }

    public SOSHibernateFactory getFactory() {
        return factory;
    }

    public FlushMode getHibernateFlushMode() {
        if (!isStatelessSession && currentSession != null) {
            Session session = (Session) currentSession;
            return session.getHibernateFlushMode();
        }
        return null;
    }

    public String getIdentifier() {
        return identifier;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public String getLastSequenceValue(String sequenceName) throws SOSHibernateException {
        String stmt = factory.getSequenceLastValString(sequenceName);
        return stmt == null ? null : getSingleValueNativeQuery(stmt);
    }

    /** execute a SELECT query(NativeQuery or Query)
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> List<T> getResultList(Query<T> query) throws SOSHibernateException {
        if (query == null) {
            throw new SOSHibernateQueryException("query is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", query);
        }
        debugQuery("getResultList", query, null);
        try {
            return query.getResultList();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
            return null;
        }
    }

    /** execute a SELECT query(Query)
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> List<T> getResultList(String hql) throws SOSHibernateException {
        Query<T> query = createQuery(hql);
        return getResultList(query);
    }

    /** execute a SELECT query(NativeQuery)
     * 
     * return a list of rows represented by Map<String,Object>:
     * 
     * Map key - column name (lower case), Map value - object (null value as NULL)
     * 
     * 
     * setResultTransformer is deprecated (see below), but currently without alternative
     * 
     * excerpt from Query.setResultTransformer comment:
     * 
     * deprecated (since 5.2), todo develop a new approach to result transformers
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public <T> List<Map<String, Object>> getResultListAsMaps(NativeQuery<T> nativeQuery) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getResultListAsMaps", nativeQuery, null);
        try {
            nativeQuery.setResultTransformer(getNativeQueryResultToMapTransformer(false, SOSDate.DATETIME_FORMAT));
            return (List<Map<String, Object>>) nativeQuery.getResultList();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
            return null;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> List<Map<String, String>> getResultListAsStringMaps(NativeQuery<T> nativeQuery) throws SOSHibernateException {
        return getResultListAsStringMaps(nativeQuery, null);
    }

    /** execute a SELECT query(NativeQuery)
     * 
     * return a list of rows represented by Map<String,String>:
     * 
     * Map key - column name (lower case), Map value - string representation (null value as an empty string)
     * 
     * 
     * setResultTransformer is deprecated (see below), but currently without alternative
     * 
     * excerpt from Query.setResultTransformer comment:
     * 
     * deprecated (since 5.2), todo develop a new approach to result transformers
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public <T> List<Map<String, String>> getResultListAsStringMaps(NativeQuery<T> nativeQuery, String dateTimeFormat) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getResultListAsStringMaps", nativeQuery, "dateTimeFormat=" + dateTimeFormat);
        try {
            nativeQuery.setResultTransformer(getNativeQueryResultToMapTransformer(true, dateTimeFormat));
            return (List<Map<String, String>>) nativeQuery.getResultList();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
            return null;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> List<T> getResultListNativeQuery(String sql) throws SOSHibernateException {
        return getResultList(createNativeQuery(sql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> List<Map<String, Object>> getResultListNativeQueryAsMaps(String sql) throws SOSHibernateException {
        return getResultListAsMaps(createNativeQuery(sql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> List<Map<String, String>> getResultListNativeQueryAsStringMaps(String sql) throws SOSHibernateException {
        return getResultListAsStringMaps(createNativeQuery(sql), null);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> List<Map<String, String>> getResultListNativeQueryAsStringMaps(String sql, String dateTimeFormat) throws SOSHibernateException {
        return getResultListAsStringMaps(createNativeQuery(sql), dateTimeFormat);
    }

    /** execute a SELECT query(NativeQuery or Query)
     * 
     * return a single row or null
     * 
     * difference to Query.getSingleResult - not throw NoResultException
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> T getSingleResult(Query<T> query) throws SOSHibernateException {
        if (query == null) {
            throw new SOSHibernateQueryException("query is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", query);
        }
        debugQuery("getSingleResult", query, null);
        T result = null;
        try {
            result = query.getSingleResult();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
        } catch (NoResultException e) {
            result = null;
        } catch (NonUniqueResultException e) {
            throw new SOSHibernateQueryNonUniqueResultException(e, query);
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> T getSingleResult(String hql) throws SOSHibernateException {
        return getSingleResult(createQuery(hql));
    }

    /** execute a SELECT query(NativeQuery)
     * 
     * return a single row represented by Map<String,Object> or null
     * 
     * Map key - column name (lower case), Map value - object (null value as NULL)
     * 
     * see getResultListAsMaps
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public <T> Map<String, Object> getSingleResultAsMap(NativeQuery<T> nativeQuery) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getSingleResultAsMap", nativeQuery, null);
        nativeQuery.setResultTransformer(getNativeQueryResultToMapTransformer(false, SOSDate.DATETIME_FORMAT));
        Map<String, Object> result = null;
        try {
            result = (Map<String, Object>) nativeQuery.getSingleResult();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
        } catch (NoResultException e) {
            result = null;
        } catch (NonUniqueResultException e) {
            throw new SOSHibernateQueryNonUniqueResultException(e, nativeQuery);
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> Map<String, String> getSingleResultAsStringMap(NativeQuery<T> query) throws SOSHibernateException {
        return getSingleResultAsStringMap(query, null);
    }

    /** execute a SELECT query(NativeQuery)
     * 
     * return a single row represented by Map<String,String> or null
     * 
     * Map key - column name (lower case), Map value - string representation (null value as an empty string)
     * 
     * see getResultListAsStringMaps
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public <T> Map<String, String> getSingleResultAsStringMap(NativeQuery<T> nativeQuery, String dateTimeFormat) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getSingleResultAsStringMap", nativeQuery, "dateTimeFormat=" + dateTimeFormat);
        nativeQuery.setResultTransformer(getNativeQueryResultToMapTransformer(true, dateTimeFormat));
        Map<String, String> result = null;
        try {
            result = (Map<String, String>) nativeQuery.getSingleResult();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
        } catch (NoResultException e) {
            result = null;
        } catch (NonUniqueResultException e) {
            throw new SOSHibernateQueryNonUniqueResultException(e, nativeQuery);
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
        }
        return result;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> T getSingleResultNativeQuery(String sql) throws SOSHibernateException {
        return getSingleResult(createNativeQuery(sql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> Map<String, Object> getSingleResultNativeQueryAsMap(String sql) throws SOSHibernateException {
        return getSingleResultAsMap(createNativeQuery(sql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> Map<String, String> getSingleResultNativeQueryAsStringMap(String sql) throws SOSHibernateException {
        return getSingleResultAsStringMap(createNativeQuery(sql), null);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> Map<String, String> getSingleResultNativeQueryAsStringMap(String sql, String dateTimeFormat) throws SOSHibernateException {
        return getSingleResultAsStringMap(createNativeQuery(sql), dateTimeFormat);
    }

    /** execute a SELECT query(NativeQuery or Query)
     * 
     * return a single field value or null
     * 
     * difference to Query.getSingleResult - not throw NoResultException, return single value as string
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> T getSingleValue(Query<T> query) throws SOSHibernateException {
        if (query == null) {
            throw new SOSHibernateQueryException("query is NULL");
        }
        debugQuery("getSingleValue", query, null);
        T result = getSingleResult(query);
        if (result != null) {
            if (query instanceof NativeQuery<?>) {
                if (result instanceof Object[]) {
                    throw new SOSHibernateQueryNonUniqueResultException("query return a row and not a unique field result", query);
                }
            } else {
                if (result.getClass().getAnnotation(Entity.class) != null) {
                    throw new SOSHibernateQueryNonUniqueResultException("query return an entity object and not a unique field result", query);
                }
            }
            return result;
        }
        return null;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> T getSingleValue(String hql) throws SOSHibernateException {
        return getSingleValue(createQuery(hql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> String getSingleValueAsString(Query<T> query) throws SOSHibernateException {
        T result = getSingleValue(query);
        if (result != null) {
            return result + "";
        }
        return null;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> String getSingleValueAsString(String hql) throws SOSHibernateException {
        T result = getSingleValue(hql);
        if (result != null) {
            return result + "";
        }
        return null;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> T getSingleValueNativeQuery(String sql) throws SOSHibernateException {
        return getSingleValue(createNativeQuery(sql));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public <T> String getSingleValueNativeQueryAsString(String sql) throws SOSHibernateException {
        T result = getSingleValueNativeQuery(sql);
        if (result != null) {
            return result + "";
        }
        return null;
    }

    public SOSHibernateSQLExecutor getSQLExecutor() {
        if (sqlExecutor == null) {
            sqlExecutor = new SOSHibernateSQLExecutor(this);
        }
        return sqlExecutor;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateTransactionException */
    public Transaction getTransaction() throws SOSHibernateException {
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        Transaction tr = null;
        try {
            if (isStatelessSession) {
                StatelessSession s = ((StatelessSession) currentSession);
                tr = s.getTransaction();
            } else {
                Session s = ((Session) currentSession);
                tr = s.getTransaction();
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        }
        return tr;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public boolean isConnected() {
        if (currentSession != null) {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                return session.isConnected();
            } else {
                Session session = (Session) currentSession;
                return session.isConnected();
            }
        }
        return false;
    }

    public boolean isGetCurrentSession() {
        return isGetCurrentSession;
    }

    public boolean isOpen() {
        if (currentSession != null) {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                return session.isOpen();
            } else {
                Session session = (Session) currentSession;
                return session.isOpen();
            }
        }
        return false;
    }

    public boolean isStatelessSession() {
        return isStatelessSession;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateSessionException */
    public void refresh(Object item) throws SOSHibernateException {
        refresh(null, item);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateObjectOperationException */
    public void refresh(String entityName, Object item) throws SOSHibernateException {
        if (item == null) {
            throw new SOSHibernateObjectOperationException("item is NULL", item);
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                if (entityName == null) {
                    session.refresh(item);
                } else {
                    session.refresh(entityName, item);
                }
            } else {
                Session session = (Session) currentSession;
                if (entityName == null) {
                    session.refresh(item);
                } else {
                    session.refresh(entityName, item);
                }
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } finally {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s%s", SOSHibernate.getMethodName(logIdentifier, "refresh"), SOSHibernate.toString(item)));
            }
        }
    }

    /** @throws SOSHibernateOpenSessionException */
    public void reopen() throws SOSHibernateOpenSessionException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%sisStatelessSession=%s", SOSHibernate.getMethodName(logIdentifier, "reopen"), isStatelessSession));
        }
        closeSession();
        openSession();
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateTransactionException */
    public void rollback() throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "rollback") : "";
        if (autoCommit) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%sskip (autoCommit is true)", method));
            }
            return;
        }
        LOGGER.debug(method);
        Transaction tr = getTransaction();
        if (tr == null) {
            isTransactionOpened = false;
            throw new SOSHibernateTransactionException("transaction is NULL");
        }
        try {
            tr.rollback();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateTransactionException(e));
        } finally {
            isTransactionOpened = false;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateObjectOperationException */
    public void save(Object item) throws SOSHibernateException {
        if (item == null) {
            throw new SOSHibernateObjectOperationException("item is NULL", item);
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.insert(item);
            } else {
                Session session = ((Session) currentSession);
                session.save(item);
                session.flush();
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } finally {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s%s", SOSHibernate.getMethodName(logIdentifier, "save"), SOSHibernate.toString(item)));
            }
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> ScrollableResults scroll(Query<T> query) throws SOSHibernateException {
        return scroll(query, ScrollMode.FORWARD_ONLY);
    }

    /** execute a SELECT query(NativeQuery or Query)
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> ScrollableResults scroll(Query<T> query, ScrollMode scrollMode) throws SOSHibernateException {
        if (query == null) {
            throw new SOSHibernateQueryException("query is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", query);
        }
        debugQuery("scroll", query, "scrollMode=" + scrollMode);
        try {
            return query.scroll(scrollMode);
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, query));
            return null;
        }
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateSessionException */
    public void sessionDoWork(Work work) throws SOSHibernateException {
        if (work == null) {
            throw new SOSHibernateSessionException("work is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "sessionDoWork"));
        }
        try {
            if (!isStatelessSession) {
                Session session = (Session) currentSession;
                session.doWork(work);
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateSessionException(e));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateSessionException(e));
        }
    }

    public void setAutoCommit(boolean val) throws SOSHibernateException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s%s", SOSHibernate.getMethodName(logIdentifier, "setAutoCommit"), val));
        }
        autoCommit = val;
        try {
            getConnection().setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new SOSHibernateTransactionException(e);
        }
    }

    public void setCacheMode(CacheMode cacheMode) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.setCacheMode(cacheMode);
        }
    }

    public void setHibernateFlushMode(FlushMode flushMode) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.setHibernateFlushMode(flushMode);
        }
    }

    public void setIdentifier(String val) {
        identifier = val;
        logIdentifier = SOSHibernate.getLogIdentifier(identifier);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateObjectOperationException */
    public void update(Object item) throws SOSHibernateException {
        if (item == null) {
            throw new SOSHibernateObjectOperationException("item is NULL", item);
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.update(item);
            } else {
                Session session = ((Session) currentSession);
                session.update(item);
                session.flush();
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateObjectOperationException(e, item));
        } finally {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s%s", SOSHibernate.getMethodName(logIdentifier, "update"), SOSHibernate.toString(item)));
            }
        }
    }

    public boolean isTransactionOpened() {
        return isTransactionOpened;
    }

    @SuppressWarnings("deprecation")
    private <T> Query<T> setResultTransformer(Query<T> query, Class<T> entityClass) {
        query.setResultTransformer(Transformers.aliasToBean(entityClass));
        return query;
    }

    private void closeSession() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s", SOSHibernate.getMethodName(logIdentifier, "closeSession")));
        }
        try {
            if (currentSession != null) {
                if (isStatelessSession) {
                    StatelessSession s = (StatelessSession) currentSession;
                    s.close();
                } else {
                    Session session = (Session) currentSession;
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
        } catch (Throwable e) {
        }
        currentSession = null;
    }

    private void closeTransaction() {
        try {
            if (currentSession != null) {
                Transaction tr = getTransaction();
                if (tr != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("%s[rollback]%s", SOSHibernate.getMethodName(logIdentifier, "closeTransaction"), tr.getStatus()));
                    }
                    tr.rollback();
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("%s[skip][rollback]transaction is null", SOSHibernate.getMethodName(logIdentifier,
                                "closeTransaction")));
                    }
                }
            }
        } catch (Throwable ex) {
            //
        }

    }

    private ResultTransformer getNativeQueryResultToMapTransformer(boolean asString, String dateTimeFormat) {
        return new ResultTransformer() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("rawtypes")
            @Override
            public List<?> transformList(List collection) {
                return collection;
            }

            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                if (aliases.length == 0) {
                    return tuple;
                }
                Map<String, Object> result = new HashMap<String, Object>(tuple.length);
                for (int i = 0; i < tuple.length; i++) {
                    String alias = aliases[i];
                    if (alias != null) {
                        Object origValue = tuple[i];
                        if (asString) {
                            String value = "";
                            if (origValue != null) {
                                value = origValue + "";
                                if (dateTimeFormat != null && origValue instanceof java.sql.Timestamp) {
                                    try {
                                        value = SOSDate.format(value, dateTimeFormat);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            result.put(alias.toLowerCase(), value);
                        } else {
                            result.put(alias.toLowerCase(), origValue);
                        }
                    }
                }
                return result;
            }
        };
    }

    private void onOpenSession() throws SOSHibernateOpenSessionException {
        String method = SOSHibernate.getMethodName(logIdentifier, "onOpenSession");
        try {
            if (Dbms.MSSQL.equals(getFactory().getDbms())) {
                String value = getFactory().getConfiguration().getProperties().getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_MSSQL_LOCK_TIMEOUT);
                if (value != null && !value.equals("-1")) {
                    getSQLExecutor().execute("set LOCK_TIMEOUT " + value);
                }
            }
            if (getFactory().readDatabaseMetaData() && !getFactory().getDatabaseMetaData().isSet()) {
                // TODO
                // currently only for Oracle because the returningClob issue
                // should be later activated for all Dbms
                if (Dbms.ORACLE.equals(getFactory().getDbms())) {
                    getFactory().getDatabaseMetaData().set(this);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("%s[databaseMetaData]%s", method, getFactory().getDatabaseMetaData()));
                    }
                }
            }
        } catch (SOSHibernateConnectionException e) {
            throw new SOSHibernateOpenSessionException(String.format("%s%s", method, e.getMessage()), e);
        } catch (Exception e) {
            LOGGER.warn(String.format("%s%s", method, e.toString()));
        }
    }

    private void throwException(IllegalStateException cause, SOSHibernateException ex) throws SOSHibernateException {
        if (cause.getCause() == null) {
            throw new SOSHibernateInvalidSessionException(cause, ex.getStatement());
        }
        throw ex;
    }

    private void throwException(PersistenceException cause, SOSHibernateException ex) throws SOSHibernateException {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof JDBCConnectionException) {
                throw new SOSHibernateInvalidSessionException((JDBCConnectionException) e, ex.getStatement());
            } else if (e instanceof SQLNonTransientConnectionException) {// can occur without hibernate JDBCConnectionException
                throw new SOSHibernateInvalidSessionException((SQLNonTransientConnectionException) e, ex.getStatement());
            } else if (e instanceof LockAcquisitionException) {
                throw new SOSHibernateLockAcquisitionException((LockAcquisitionException) e, ex.getStatement());
            } else if (e instanceof StaleStateException) {
                throw new SOSHibernateObjectOperationStaleStateException((StaleStateException) e, ex.getDbItem());
            } else if (e instanceof SQLException) {
                if (Dbms.MYSQL.equals(getFactory().getDbms())) {
                    // MySQL with the mariadb driver not throws a specific exception - check error code
                    SQLException se = (SQLException) e;
                    if (se.getErrorCode() == 1205 || se.getErrorCode() == 1213) {// Lock wait timeout, Deadlock
                        throw new SOSHibernateLockAcquisitionException(se, ex.getStatement());
                    }
                }
            }
            e = e.getCause();
        }
        throw ex;
    }

    private <T> void debugQuery(String method, Query<T> query, String infos) {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(SOSHibernate.getMethodName(logIdentifier, method));
            if (query != null) {
                String params = SOSHibernate.getQueryParametersAsString(query);
                if (params != null) {
                    sb.append("[");
                    sb.append(params);
                    sb.append("]");
                }
                int limit = query.getMaxResults();
                if (limit != Integer.MAX_VALUE) {
                    sb.append("[limit=").append(limit).append("]");
                }
                sb.append("[").append(query.getQueryString()).append("]");
            }
            if (infos != null) {
                sb.append(infos);
            }
            LOGGER.debug(sb.toString());
        }
    }

    // TODO currently only for select statements. todo delete, update ...
    public String getSQLString(Query<?> query) {
        if (query == null || factory == null) {
            return null;
        }
        try {
            ASTQueryTranslatorFactory f = new ASTQueryTranslatorFactory();
            SessionFactoryImplementor impl = (SessionFactoryImplementor) factory.getSessionFactory();
            String hql = query.unwrap(org.hibernate.query.Query.class).getQueryString();

            QueryTranslator qt = f.createQueryTranslator("", hql, java.util.Collections.EMPTY_MAP, impl, null);
            qt.compile(java.util.Collections.EMPTY_MAP, false);
            return String.format("[%s][maxResults=%s][%s]", qt.getSQLString(), query.getMaxResults(), SOSHibernate.getQueryParametersAsString(query));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }
}