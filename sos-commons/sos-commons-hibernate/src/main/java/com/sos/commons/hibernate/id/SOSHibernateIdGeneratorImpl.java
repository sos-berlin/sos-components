package com.sos.commons.hibernate.id;

import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Properties;

import org.hibernate.boot.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.OnExecutionGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;
import org.hibernate.id.insert.GetGeneratedKeysDelegate;
import org.hibernate.id.insert.InsertReturningDelegate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateIdGeneratorImpl implements BeforeExecutionGenerator, Configurable, OnExecutionGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateIdGeneratorImpl.class);
    private static final long serialVersionUID = 1L;

    private final String sequenceName;
    private String sequenceCallSyntax;

    public SOSHibernateIdGeneratorImpl(SOSHibernateIdGenerator config, Member member, CustomIdGeneratorCreationContext creationContext) {
        sequenceName = config.sequenceName();
    }

    /** dialect.getSequenceSupport().supportsSequences(); - returns true for MSSQL<br/>
     * therefore, sequences are not supported dynamically but only for Oracle/PgSQL<br/>
     */
    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        final Dialect dialect = serviceRegistry.getService(JdbcEnvironment.class).getDialect();
        if (dialect instanceof OracleDialect || dialect instanceof PostgreSQLDialect) {
            sequenceCallSyntax = dialect.getSequenceSupport().getSequenceNextValString(sequenceName);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("sequenceCallSyntax=" + sequenceCallSyntax);
        }
    }

    /** true - AutoIncrement/Identity - MySQL/MSSQL<br />
     * false - Sequence - Oracle, PgSQL<br/>
     */
    @Override
    public boolean generatedOnExecution() {
        return sequenceCallSyntax == null;
    }

    /** AutoIncrement/Identity only<br/>
     */
    @SuppressWarnings({ "removal" })
    @Override
    public org.hibernate.id.insert.InsertGeneratedIdentifierDelegate getGeneratedIdentifierDelegate(
            org.hibernate.id.PostInsertIdentityPersister persister) {
        if (persister.getFactory().getSessionFactoryOptions().isGetGeneratedKeysEnabled()) {
            // this case is used
            return new GetGeneratedKeysDelegate(persister, false, EventType.INSERT);
        } else {
            return new InsertReturningDelegate(persister, EventType.INSERT);
        }
    }

    /** Sequence only<br/>
     */
    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return session.createNativeQuery(sequenceCallSyntax, Long.class).uniqueResult();
    }

    @Override
    public boolean referenceColumnsInSql(Dialect dialect) {
        return false;
    }

    @Override
    public boolean writePropertyValue() {
        return false;
    }

    @Override
    public String[] getReferencedColumnValues(Dialect dialect) {
        return null;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return INSERT_ONLY;
    }

}
