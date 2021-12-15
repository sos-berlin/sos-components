package com.sos.commons.hibernate.function.date;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;

public class SOSHibernateSecondsDiff extends StandardSQLFunction {

    public static final String NAME = "SOS_SECONDSDIFF";

    private SOSHibernateFactory factory;

    public SOSHibernateSecondsDiff(SOSHibernateFactory factory) {
        super(NAME);
        this.factory = factory;
    }

    public static String getFunction(final String endTimeProperty, final String startTimeProperty) {
        return new StringBuilder(NAME).append("('").append(endTimeProperty).append("',").append(startTimeProperty).append("')").toString();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments");
        }
        String endTimeProperty = arguments.get(0).toString();
        String startTimeProperty = arguments.get(1).toString();

        Enum<SOSHibernateFactory.Dbms> dbms = this.factory.getDbms();

        if (Dbms.PGSQL.equals(dbms)) {
            return "EXTRACT(EPOCH FROM((" + endTimeProperty + ")-(" + startTimeProperty + ")))";
        } else if (Dbms.H2.equals(dbms)) {// TODO check with MySQL mode
            return "DATEDIFF(ss," + startTimeProperty + "," + endTimeProperty + ")";
        } else if (Dbms.SYBASE.equals(dbms)) {
            return "DATEDIFF(ss," + endTimeProperty + "," + startTimeProperty + ")";
        } else {
            return "((" + endTimeProperty + ")-(" + startTimeProperty + "))";
        }
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return StandardBasicTypes.INTEGER;
    }
}
