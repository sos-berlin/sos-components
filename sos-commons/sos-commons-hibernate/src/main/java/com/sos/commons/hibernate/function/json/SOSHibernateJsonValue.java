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
        ReturnType returnType;

        switch (this.factory.getDbms()) {
        case MYSQL:
            returnType = argument2ReturnType(arguments.get(0).toString());
            // path = '$.ports.usb' -> '$.ports.usb'
            String extract = "JSON_EXTRACT(" + property + "," + path + ")";
            if (returnType.equals(ReturnType.SCALAR)) {
                return "JSON_UNQUOTE(" + extract + ")";
            } else {
                return extract;
            }
        case MSSQL:
            returnType = argument2ReturnType(arguments.get(0).toString());
            if (returnType.equals(ReturnType.SCALAR)) {
                return "JSON_VALUE(" + property + "," + path + ")";
            } else {
                return "JSON_QUERY(" + property + "," + path + ")";
            }
        case ORACLE:
            // TODO
            // returning clob
            // ---- SELECT json_value(j, '$.bar' returning clob) FROM j
            // -------- available from 18c? 12c - ORA-40444 error ..;
            // ERROR ON ERROR
            // ---- SELECT json_value(j, '$.bar' returning varchar2(32000) ERROR ON ERROR) FROM j;
            returnType = argument2ReturnType(arguments.get(0).toString());
            if (returnType.equals(ReturnType.SCALAR)) {
                // testsed with 18c - not support RETURNING CLOB - gets empty value - use VARCHAR2(32000)?
                return "JSON_VALUE(" + property + "," + path + ")";
            } else {
                if (this.factory.getSupportJsonReturningClob()) {
                    return "JSON_QUERY(" + property + "," + path + " RETURNING CLOB)";
                } else {
                    return "JSON_QUERY(" + property + "," + path + ")";
                }
            }
        case PGSQL:
            // path = '$.ports.usb' -> 'ports'->>'usb'
            // '<column>->'ports'->>'usb'
            // path = '$.arg' -> 'arg'
            // '<column>->>'arg'

            // StringBuilder r = new StringBuilder(property.toString());
            StringBuilder r = new StringBuilder(property.toString()).append("::jsonb");
            String[] arr = path.replaceAll("'", "").substring(2).split("\\.");
            if (arr.length == 1) {
                r.append("->>'").append(arr[0]).append("'");
            } else {
                r.append("->'");
                r.append(String.join("'->'", Arrays.copyOf(arr, arr.length - 1)));
                r.append("'->>'").append(arr[arr.length - 1]).append("'");
            }
            return r.toString();
        case H2:
            // path = '$.ports.usb' -> '$.ports.usb'
            return com.sos.commons.hibernate.function.json.h2.Functions.NAME_JSON_VALUE + "(" + property + "," + path + ")";
        default:
            return NAME;
        }
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
