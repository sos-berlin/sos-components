package com.sos.commons.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SharedSessionContract;
import org.hibernate.StaleStateException;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.jdbc.Work;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate.Dbms;
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
import com.sos.commons.hibernate.transform.SOSAliasToBeanResultTransformer;
import com.sos.commons.hibernate.transform.SOSNativeQueryAliasToMapTransformer;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.zaxxer.hikari.pool.ProxyConnection;
import com.zaxxer.hikari.util.FastList;

import jakarta.persistence.Entity;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;

public class SOSHibernateSession implements Serializable, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSession.class);
    private static final Logger CONNECTION_POOL_LOGGER = LoggerFactory.getLogger("ConnectionPool");
    private static final long serialVersionUID = 1L;

    /** TODO To use reflection – make sure/check this field is present in future Hibernate versions */
    private static final String DECLARED_FIELD_HIBERNATE_JDBCCOORDINATOR_LASTQUERY = "lastQuery";
    private static final String DECLARED_FIELD_HIKARY_PROXYCONNECTION_OPENSTATEMENTS = "openStatements";

    private final SOSHibernateFactory factory;

    private SharedSessionContract currentSession;
    private FlushMode defaultHibernateFlushMode = FlushMode.ALWAYS;

    private boolean autoCommit = false;
    private String identifier;
    private String logIdentifier;
    private boolean isGetCurrentSession = false;
    private boolean isStatelessSession = false;
    private boolean isTransactionOpened = false;
    private boolean isTerminateInProgress = false;
    private SOSHibernateSQLExecutor sqlExecutor;
    private Statement currentStatement;

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
                LOGGER.debug(String.format("%s[skip]autoCommit=true", method));
            }
            return;
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (isTransactionOpened) {
            LOGGER.warn(String.format("%s[skip]transaction is already opened", method));
            return;
        }
        LOGGER.debug(method);
        try {
            try {
                currentSession.beginTransaction();
            } catch (NullPointerException e) {
                throw new SOSHibernateOpenSessionException("session/connection can't be acquired", e.getCause());
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

    public void doWork(Work work) throws SOSHibernateException {
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "doWork"));
        }
        try {
            currentSession.doWork(work);
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
        if (isTerminateInProgress) {// terminated by another thread, no close because close is sent...
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[skip]termination is in process", SOSHibernate.getMethodName(logIdentifier, "close")));
            }
            return;
        }
        CONNECTION_POOL_LOGGER.debug("--------> RELEASE CONNECTION: " + getIdentifier() + " (" + SOSClassUtil.getMethodName(3) + ") --------");
        closeTransaction();
        closeSession();
    }

    /** If the last statement of a session is still running, the execution of this statement is aborted, rollback is executed and connection is closed<br/>
     * The session is then closed.<br/>
     * <br/>
     * Note: Intended for cases where there is only 1 session/connection per factory, otherwise using this method may lead to unpredictable results<br/>
     * <br/>
     * MySQL<br/>
     * - cancel statement - required<br/>
     * - connection close - does not seem to be required (a Hibernate exception is logged when the connection is aborted(no exception if closed))<br/>
     * MSSQL<br/>
     * - cancel statement - required<br/>
     * - connection close - does not seem to be required but is used - in both cases a Hibernate exception is thrown about the aborted query<br/>
     * Oracle<br/>
     * - cancel statement - required<br/>
     * - connection close - does not seem to be required but is used - in both cases a Hibernate exception is thrown about the aborted query<br/>
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean terminate() {
        boolean terminated = false;
        try {
            if (currentSession != null) {
                String method = SOSHibernate.getMethodName(logIdentifier, "terminate");
                try {
                    isTerminateInProgress = false;

                    // 1) SOSHibernate
                    if (currentStatement != null) {
                        isTerminateInProgress = true;
                        try {
                            currentStatement.cancel();
                            LOGGER.info(method + "[currentStatement][cancel]executed");
                        } catch (Throwable t) {
                            LOGGER.info(method + "[currentStatement][cancel]" + t);
                        }
                    }

                    // 2) Hibernate - only for select queries
                    SharedSessionContractImplementor impl = (SharedSessionContractImplementor) currentSession;
                    JdbcCoordinator jdbcCoordinator = impl.getJdbcCoordinator();
                    try {
                        // first try using reflection, because jdbcCoordinator.cancelLastQuery() does not check if the last statement has already been closed...
                        Field lastQueryField = jdbcCoordinator.getClass().getDeclaredField(DECLARED_FIELD_HIBERNATE_JDBCCOORDINATOR_LASTQUERY);
                        lastQueryField.setAccessible(true);
                        Statement statement = (Statement) lastQueryField.get(jdbcCoordinator);
                        if (statement != null && !statement.isClosed()) {
                            isTerminateInProgress = true;
                            try {
                                statement.cancel();
                                LOGGER.info(method + "[hibernate][JdbcCoordinator][statement][cancel]executed");
                            } catch (Throwable t) {
                                LOGGER.info(method + "[hibernate][JdbcCoordinator][statement][cancel]" + t);
                            }
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        try {
                            jdbcCoordinator.cancelLastQuery();
                            isTerminateInProgress = true;
                            LOGGER.info(method + "[hibernate][JdbcCoordinator][cancelLastQuery]executed");
                        } catch (Throwable ex) {// throws an exception if the statement has already been closed
                            LOGGER.info(method + "[hibernate][JdbcCoordinator][cancelLastQuery]" + ex);
                        }
                    }

                    // 3) HIKARI Proxy Connection
                    if (!isTerminateInProgress) {
                        ProxyConnection pc = (ProxyConnection) getConnection();
                        try {
                            Field os = ProxyConnection.class.getDeclaredField(DECLARED_FIELD_HIKARY_PROXYCONNECTION_OPENSTATEMENTS);
                            os.setAccessible(true);
                            FastList<Statement> l = (FastList<Statement>) os.get(pc);
                            if (l != null && l.size() > 0) {
                                isTerminateInProgress = true;
                                for (Statement s : l) {
                                    try {
                                        if (!s.isClosed()) {
                                            s.cancel();
                                            LOGGER.info(method + "[hikari][ProxyConnection][statement][cancel]executed");
                                        }
                                    } catch (Throwable t) {
                                        LOGGER.info(method + "[hikari][ProxyConnection][statement][cancel]" + t);
                                    }
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            LOGGER.info(method + "[hikari][ProxyConnection][field=" + DECLARED_FIELD_HIKARY_PROXYCONNECTION_OPENSTATEMENTS + "]" + e);
                        }
                    }

                    if (isTerminateInProgress) {
                        // rollback if the current statement is canceled.
                        // otherwise, rollback execution waits until the current statement completes.
                        boolean useSessionRollback = true;
                        if (useSessionRollback) {
                            try {
                                doRollback();
                                LOGGER.info(method + "[session][rollback]executed");
                            } catch (Throwable r) {
                                LOGGER.info(method + "[session][rollback]" + r);
                            }
                        }
                        Connection connection = getConnection();
                        if (connection != null) {
                            if (!useSessionRollback) {
                                try {
                                    connection.rollback();
                                    LOGGER.info(method + "[connection][rollback]executed");
                                } catch (Throwable r) {
                                    LOGGER.info(method + "[connection][rollback]" + r);
                                }
                            }
                            try {
                                // connection.abort(Runnable::run);
                                connection.close();
                                LOGGER.info(method + "[connection][close]executed");
                            } catch (Throwable ex) {
                                LOGGER.info(method + "[connection][close]" + ex);
                            }
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                } finally {
                    terminated = isTerminateInProgress;
                    isTerminateInProgress = false;
                    close();
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("%s%s", SOSHibernate.getMethodName(logIdentifier, "terminate"), e.toString()), e);
        } finally {
            isTerminateInProgress = false;
        }
        return terminated;
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateTransactionException */
    public void commit() throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "commit") : "";
        if (autoCommit) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]autoCommit=true", method));
            }
            return;
        }
        if (isTerminateInProgress) {// terminated by another thread, no commit because rollback is sent...
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]termination is in process", method));
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
    public NativeQuery<Object[]> createNativeQuery(String sql) throws SOSHibernateException {
        return createNativeQuery(sql, Object[].class);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> NativeQuery<T> createNativeQuery(String sql, @Nonnull Class<T> resultType) throws SOSHibernateException {
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
            q = currentSession.createNativeQuery(sql, resultType);
            if (!resultType.getPackageName().startsWith("java.")) {
                // custom entity - see createQuery description
                q.setTupleTransformer(new SOSAliasToBeanResultTransformer<T>(resultType));
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
            if (resultType == null) {
                // Default case: mapped entity class – result type matches the query result directly
                q = currentSession.createQuery(hql, null);
            } else {// custom entity
                // Custom result type (not a mapped entity)
                // JOC-1974 (Hibernate 6.6.14): unlike earlier versions, createQuery(hql, resultType) performs type validation immediately and will throw an
                // exception if the result does not directly match the constructor of the given resultType.
                // Therefore, we avoid passing resultType here.
                // q = currentSession.createQuery(hql, resultType);
                q = currentSession.createQuery(hql, null);
                // 1) Without a TupleTransformer, the custom class must have a constructor matching the selected fields in the query:
                // Example: MyCustomEntity(String, Long, String, ...)
                // 2) With a TupleTransformer, field names in the query (aliases) are mapped to the bean properties of the resultType
                q.setTupleTransformer(new SOSAliasToBeanResultTransformer<T>(resultType));
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

    public <T> void setParameter(Query<T> query, String parameter, Object argument) throws SOSHibernateException {
        try {
            query.setParameter(parameter, argument);
        } catch (IllegalArgumentException e) {
            throw new SOSHibernateQueryException(e, query);
        }
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
                session.remove(item);
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
            SharedSessionContractImplementor impl = (SharedSessionContractImplementor) currentSession;
            try {
                return impl.getJdbcCoordinator().getLogicalConnection().getPhysicalConnection();
            } catch (NullPointerException e) {
                throw new SOSHibernateConnectionException(
                        "can't get the SQL connection from the SharedSessionContractImplementor(NullPointerException)");
            }
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateConnectionException(e));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateConnectionException(e));
            return null;
        }
    }

    public SharedSessionContract getCurrentSession() {
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
        return stmt == null ? null : getSingleValueNativeQuery(stmt, String.class);
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
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    @SuppressWarnings("unchecked")
    public <T> List<Map<String, Object>> getResultListAsMaps(NativeQuery<T> nativeQuery) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getResultListAsMaps", nativeQuery, null);
        try {
            nativeQuery.setTupleTransformer(new SOSNativeQueryAliasToMapTransformer<T>());
            return (List<Map<String, Object>>) nativeQuery.getResultList();
        } catch (IllegalStateException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
            return null;
        } catch (PersistenceException e) {
            throwException(e, new SOSHibernateQueryException(e, nativeQuery));
            return null;
        }
    }

    /** execute a SELECT query(NativeQuery)
     * 
     * return a list of rows represented by Map<String,String>:
     * 
     * Map key - column name (lower case), Map value - string representation (null value as an empty string)
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    @SuppressWarnings("unchecked")
    public <T> List<Map<String, String>> getResultListAsStringMaps(NativeQuery<T> nativeQuery, String dateTimeFormat) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getResultListAsStringMaps", nativeQuery, "dateTimeFormat=" + dateTimeFormat);
        try {
            nativeQuery.setTupleTransformer(new SOSNativeQueryAliasToMapTransformer<T>(true, dateTimeFormat));
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
    public <T> List<Map<String, String>> getResultListAsStringMaps(NativeQuery<T> nativeQuery) throws SOSHibernateException {
        return getResultListAsStringMaps(nativeQuery, null);
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> List<T> getResultListNativeQuery(String sql, Class<T> resultType) throws SOSHibernateException {
        return getResultList(createNativeQuery(sql, resultType));
    }

    public List<String> getResultListNativeQuery(String sql) throws SOSHibernateException {
        return getResultList(createNativeQuery(sql, String.class));
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
    @SuppressWarnings("unchecked")
    public <T> Map<String, Object> getSingleResultAsMap(NativeQuery<T> nativeQuery) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getSingleResultAsMap", nativeQuery, null);
        nativeQuery.setTupleTransformer(new SOSNativeQueryAliasToMapTransformer<T>());
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
    @SuppressWarnings("unchecked")
    public <T> Map<String, String> getSingleResultAsStringMap(NativeQuery<T> nativeQuery, String dateTimeFormat) throws SOSHibernateException {
        if (nativeQuery == null) {
            throw new SOSHibernateQueryException("nativeQuery is NULL");
        }
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL", nativeQuery);
        }
        debugQuery("getSingleResultAsStringMap", nativeQuery, "dateTimeFormat=" + dateTimeFormat);
        nativeQuery.setTupleTransformer(new SOSNativeQueryAliasToMapTransformer<T>(true, dateTimeFormat));
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
    public <T> T getSingleResultNativeQuery(String sql, Class<T> resultType) throws SOSHibernateException {
        return getSingleResult(createNativeQuery(sql, resultType));
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
    public <T> T getSingleValueNativeQuery(String sql, Class<T> returnType) throws SOSHibernateException {
        return getSingleValue(createNativeQuery(sql, returnType));
    }

    /** @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException,
     *             SOSHibernateQueryNonUniqueResultException */
    public String getSingleValueNativeQueryAsString(String sql) throws SOSHibernateException {
        Object result = getSingleValueNativeQuery(sql, String.class);
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
            tr = currentSession.getTransaction();
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
            return currentSession.isConnected();
        }
        return false;
    }

    public boolean isGetCurrentSession() {
        return isGetCurrentSession;
    }

    public boolean isOpen() {
        if (currentSession != null) {
            return currentSession.isOpen();
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
                session.refresh(item);
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
        if (isTerminateInProgress) {// terminated by another thread, no commit because rollback is sent...
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[skip]termination is in process", SOSHibernate.getMethodName(logIdentifier, "rollback")));
            }
            return;
        }
        doRollback();
    }

    private void doRollback() throws SOSHibernateException {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "rollback") : "";
        if (autoCommit) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]autoCommit=true", method));
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
                session.persist(item);
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
    public <T> ScrollableResults<T> scroll(Query<T> query) throws SOSHibernateException {
        return scroll(query, ScrollMode.FORWARD_ONLY);
    }

    /** execute a SELECT query(NativeQuery or Query)
     * 
     * @throws SOSHibernateException : SOSHibernateInvalidSessionException, SOSHibernateLockAcquisitionException, SOSHibernateQueryException */
    public <T> ScrollableResults<T> scroll(Query<T> query, ScrollMode scrollMode) throws SOSHibernateException {
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
                session.merge(item);
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

    private void closeSession() {
        try {
            if (currentSession != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s", SOSHibernate.getMethodName(logIdentifier, "closeSession")));
                }
                currentSession.close();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s[skip]currentSession=null", SOSHibernate.getMethodName(logIdentifier, "closeSession")));
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

    private void throwException(IllegalStateException cause, SOSHibernateException ex) throws SOSHibernateException {
        if (cause.getCause() == null) {
            throw new SOSHibernateInvalidSessionException(cause, ex.getStatement());
        }
        throw ex;
    }

    @SuppressWarnings("unused")
    private void throwException(SQLException cause, SOSHibernateException ex) throws SOSHibernateException {
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

    public DatabaseMetaData getDatabaseMetaData() throws SOSHibernateException {
        if (currentSession == null) {
            throw new SOSHibernateInvalidSessionException("currentSession is NULL");
        }
        DatabaseMetaData metaData = null;
        try {
            SharedSessionContractImplementor impl = (SharedSessionContractImplementor) currentSession;
            metaData = impl.getJdbcCoordinator().getLogicalConnection().getPhysicalConnection().getMetaData();
        } catch (SQLException e) {
            throw new SOSHibernateException(e);
        }
        return metaData;
    }

    /** TODO <br/>
     * PgSQL session.getCurrentDateTime():<br/>
     * the returned current date time depends on TimeZone.set/getDefault.<br/>
     * For all others, the returned current date time is independent of TimeZone.set/getDefault.<br/>
     * see SOSHibernateSessionTest */
    public Date getCurrentDateTime() throws SOSHibernateException {
        return getSingleResultNativeQuery(factory.getCurrentTimestampSelectString(), Date.class);
    }

    public Date getCurrentUTCDateTime() throws SOSHibernateException {
        return getSingleResultNativeQuery(factory.getCurrentUTCTimestampSelectString(), Date.class);
    }

    public void setCurrentStatement(Statement val) {
        currentStatement = val;
    }

    public void resetCurrentStatement() {
        currentStatement = null;
    }

    public Statement getCurrentStatement() {
        return currentStatement;
    }
}