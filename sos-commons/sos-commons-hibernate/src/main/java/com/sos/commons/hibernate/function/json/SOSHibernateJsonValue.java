package com.sos.commons.hibernate.function.json;

import java.util.Arrays;
import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;

public class SOSHibernateJsonValue extends StandardSQLFunction {

    public static final String NAME = "SOS_JSON_VALUE";

    public enum ReturnType {
        SCALAR, JSON
    }

    private SOSHibernateFactory factory;

    public SOSHibernateJsonValue(SOSHibernateFactory factory) {
        super(NAME);
        this.factory = factory;
    }

    public static String getFunction(final ReturnType returnType, final String property, final String path) {
        return new StringBuilder(NAME).append("('").append(returnType.name()).append("',").append(property).append(",'").append(path).append("')")
                .toString();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
        if (arguments == null || arguments.size() < 3) {
            throw new QueryException("missing arguments");
        }
        String property = arguments.get(1).toString();
        String path = arguments.get(2).toString();

        Enum<SOSHibernateFactory.Dbms> dbms = this.factory.getDbms();
        if (Dbms.MYSQL.equals(dbms)) {
            // arguments(1) = '$.ports.usb' -> '$.ports.usb'
            String extract = "JSON_EXTRACT(" + property + "," + path + ")";
            // if (returnType.equals(ReturnType.SCALAR)) {
            // return "JSON_UNQUOTE(" + extract + ")";
            // } else {
            // return extract;
            // }
            // TODO JSON_UNQUOTE is only for select JSON_UNQUOTE... and not important for where ...
            return extract;
        } else if (Dbms.MSSQL.equals(dbms) || Dbms.ORACLE.equals(dbms)) {
            ReturnType returnType = argument2ReturnType(arguments.get(0).toString());
            if (returnType.equals(ReturnType.SCALAR)) {
                return "JSON_VALUE(" + property + "," + path + ")";
            } else {
                return "JSON_QUERY(" + property + "," + path + ")";
            }
        } else if (Dbms.PGSQL.equals(dbms)) {
            // arguments(1) = '$.ports.usb' -> 'ports'->>'usb'
            // '<column>->'ports'->>'usb'
            // arguments(1) = '$.arg' -> 'arg'
            // '<column>->>'arg'

            StringBuilder r = new StringBuilder(property.toString());
            String[] arr = path.replaceAll("'", "").substring(2).split("\\.");
            if (arr.length == 1) {
                r.append("->>'").append(arr[0]).append("'");
            } else {
                r.append("->'");
                r.append(String.join("'->'", Arrays.copyOf(arr, arr.length - 1)));
                r.append("'->>'").append(arr[arr.length - 1]).append("'");
            }
            return r.toString();
        }
        return NAME;
    }

    private ReturnType argument2ReturnType(String arg) throws QueryException {
        try {
            return ReturnType.valueOf(arg.replaceAll("'", ""));
        } catch (Exception e) {
            throw new QueryException(String.format("[argument=%s]%s", arg, e.toString()), e);
        }
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return new StringType();
    }

}
