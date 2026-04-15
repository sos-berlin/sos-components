package com.sos.commons.hibernate.generator;

import java.lang.reflect.Member;
import java.util.EnumSet;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.OnExecutionGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.persister.entity.EntityPersister;

import com.sos.commons.hibernate.annotations.SOSIdGenerator;

public class SOSIdGeneratorImpl implements BeforeExecutionGenerator, OnExecutionGenerator {

    private static final long serialVersionUID = 1L;
    private String sequenceName;
    private String sequenceCallSyntax;

    public SOSIdGeneratorImpl(SOSIdGenerator config, Member member, org.hibernate.generator.GeneratorCreationContext context) {
        this.sequenceName = config.sequenceName();

        final Dialect dialect = context.getServiceRegistry().getService(JdbcEnvironment.class).getDialect();
        if (dialect instanceof OracleDialect || dialect instanceof PostgreSQLDialect) {
            sequenceCallSyntax = dialect.getSequenceSupport().getSequenceNextValString(sequenceName);
        }
    }

    /** true - Auto-Increment/Identity - H2/MySQL/MSSQL<br />
     * false - Sequence - Oracle/PgSQL<br/>
     */
    @Override
    public boolean generatedOnExecution() {
        return sequenceCallSyntax == null;
    }

    /** Auto-Increment/Identity only */
    @SuppressWarnings("removal")
    @Override
    public InsertGeneratedIdentifierDelegate getGeneratedIdentifierDelegate(EntityPersister persister) {
        return new IdentityGenerator().getGeneratedIdentifierDelegate(persister);
    }

    /** Sequence only */
    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return session.createNativeQuery(sequenceCallSyntax, Long.class).uniqueResult();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
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

}