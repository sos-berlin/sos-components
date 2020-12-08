package com.sos.commons.hibernate.function.regex;

import java.util.List;
import java.util.regex.Pattern;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;

public class SOSHibernateRegexp extends StandardSQLFunction {

    public static final String NAME = "SOS_REGEXP";

    private SOSHibernateFactory factory;

    public SOSHibernateRegexp(SOSHibernateFactory factory) {
        super(NAME);
        this.factory = factory;
    }

    public static String getFunction(final String property, final String regexp) {
        return new StringBuilder(NAME).append("(").append(property).append(",").append(quote(regexp)).append(")=1").toString();
    }

    public static String getFunction(final String property, final String regexp, final String mssqlRegexp) {
        return new StringBuilder(NAME).append("(").append(property).append(",").append(quote(regexp)).append("," + quote(mssqlRegexp) + ")=1")
                .toString();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments");
        }
        String property = arguments.get(0).toString();
        String regexp = arguments.get(1).toString();
        String mssqlRegexp = arguments.size() == 3 ? arguments.get(2).toString() : null;

        Enum<SOSHibernateFactory.Dbms> dbms = this.factory.getDbms();
        if (Dbms.MYSQL.equals(dbms)) {
            // REGEXP_LIKE from MySQL 8.0
            return property + " REGEXP " + regexp;
        } else if (Dbms.MSSQL.equals(dbms)) {
            // TODO
            String innRegexp = null;
            if (mssqlRegexp == null) {
                regexp = regexp.replaceAll(Pattern.quote(".*"), "%");
                innRegexp = regexp.substring(1, regexp.length() - 1);
                if (!innRegexp.startsWith("^")) {
                    innRegexp = "%" + innRegexp;
                }
                if (!innRegexp.endsWith("$")) {
                    innRegexp = innRegexp + "%";
                }
            } else {
                innRegexp = mssqlRegexp.substring(1, mssqlRegexp.length() - 1);
            }
            return "(case when (" + property + " like '" + innRegexp + "') then 1 else 0 end)";
        } else if (Dbms.ORACLE.equals(dbms)) {
            return "(case when (REGEXP_LIKE(" + property + "," + regexp + ")) then 1 else 0 end)";
        } else if (Dbms.PGSQL.equals(dbms)) {
            return "(case when (" + property + " ~ " + regexp + ") then 1 else 0 end)";
        }
        return NAME;
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

    private static String quote(String regexp) {
        if (regexp == null) {
            return "'null'";
        }
        // : for parameter binding
        return regexp.startsWith(":") ? regexp : "'" + regexp + "'";
    }

}
