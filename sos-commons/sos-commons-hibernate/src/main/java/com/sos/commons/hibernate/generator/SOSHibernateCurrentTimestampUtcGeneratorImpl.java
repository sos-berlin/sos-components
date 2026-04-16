package com.sos.commons.hibernate.generator;

import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;
import static org.hibernate.generator.EventTypeSets.fromArray;

import java.util.EnumSet;

import org.hibernate.dialect.Dialect;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.generator.OnExecutionGenerator;
import org.hibernate.id.Configurable;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.annotations.SOSCreationTimestampUtc;
import com.sos.commons.hibernate.annotations.SOSCurrentTimestampUtc;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateFinalPropertiesResolver;

public class SOSHibernateCurrentTimestampUtcGeneratorImpl implements OnExecutionGenerator, Configurable {

    private static final long serialVersionUID = 1L;

    private final EnumSet<EventType> eventTypes;

    public SOSHibernateCurrentTimestampUtcGeneratorImpl(SOSCreationTimestampUtc annotation, GeneratorCreationContext context) {
        eventTypes = INSERT_ONLY;
    }

    public SOSHibernateCurrentTimestampUtcGeneratorImpl(SOSCurrentTimestampUtc annotation, GeneratorCreationContext context) {
        eventTypes = fromArray(annotation.event());
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return eventTypes;
    }

    @Override
    public String[] getReferencedColumnValues(Dialect dialect) {
        return new String[] { SOSHibernateFactory.getCurrentTimestampUtcExpression(SOSHibernateFinalPropertiesResolver.getDbms(dialect)) };
    }

    @Override
    public boolean referenceColumnsInSql(Dialect dialect) {
        return true;
    }

    @Override
    public boolean writePropertyValue() {
        return false;
    }

}
