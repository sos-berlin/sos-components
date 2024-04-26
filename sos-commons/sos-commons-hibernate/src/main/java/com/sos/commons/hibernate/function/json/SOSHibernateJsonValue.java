package com.sos.commons.hibernate.function.json;

import java.util.Arrays;
import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;

public class SOSHibernateJsonValue extends StandardSQLFunction {

    public static final String NAME = "SOS_JSON_VALUE";

    public enum ReturnType {
        SCALAR, JSON
    }

    private SOSHibernateFactory factory;

    public SOSHibernateJsonValue(SOSHibernateFactory factory) {
        //TODO 6.4.5.Final
        super(NAME, StandardBasicTypes.STRING);
        this.factory = factory;
    }

    /** <br/>
     * ReturnType.SCALAR - use if the query returns a single value (string, numeric etc)<br/>
     * ReturnType.JSON - use if the query returns a json (array, object etc)
     * 
     * @param returnType
     * @param property
     * @param path
     * @return */
    public static String getFunction(final ReturnType returnType, final String property, final String path) {
        return new StringBuilder(NAME).append("('").append(returnType.name()).append("',").append(property).append(",'").append(path).append("')")
                .toString();
    }

    //TODO 6.4.5.Final
//    @SuppressWarnings("rawtypes")
//    @Override
//    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
//        if (arguments == null || arguments.size() < 3) {
//            throw new QueryException("missing arguments");
//        }
//        String property = arguments.get(1).toString();
//        String path = arguments.get(2).toString();
//        ReturnType returnType;
//
//        switch (this.factory.getDbms()) {
//        case MYSQL:
//            returnType = argument2ReturnType(arguments.get(0).toString());
//            // path = '$.ports.usb' -> '$.ports.usb'
//            String extract = "JSON_EXTRACT(" + property + "," + path + ")";
//            if (returnType.equals(ReturnType.SCALAR)) {
//                return "JSON_UNQUOTE(" + extract + ")";
//            } else {
//                return extract;
//            }
//        case MSSQL:
//            returnType = argument2ReturnType(arguments.get(0).toString());
//            if (returnType.equals(ReturnType.SCALAR)) {
//                return "JSON_VALUE(" + property + "," + path + ")";
//            } else {
//                return "JSON_QUERY(" + property + "," + path + ")";
//            }
//        case ORACLE:
//            // TODO
//            // returning clob
//            // ---- SELECT json_value(j, '$.bar' returning clob) FROM j
//            // -------- available from 18c? 12c - ORA-40444 error ..;
//            // ERROR ON ERROR
//            // ---- SELECT json_value(j, '$.bar' returning varchar2(32000) ERROR ON ERROR) FROM j;
//            returnType = argument2ReturnType(arguments.get(0).toString());
//            if (returnType.equals(ReturnType.SCALAR)) {
//                // tested with 18c - not support RETURNING CLOB - gets empty value - use VARCHAR2(32000)?
//                return "JSON_VALUE(" + property + "," + path + ")";
//            } else {
//                if (this.factory.getDatabaseMetaData().supportJsonReturningClob()) {
//                    return "JSON_QUERY(" + property + "," + path + " RETURNING CLOB)";
//                } else {
//                    return "JSON_QUERY(" + property + "," + path + ")";
//                }
//            }
//        case PGSQL:
//            // path = '$.ports.usb' -> 'ports'->>'usb'
//            // '<column>->'ports'->>'usb'
//            // path = '$.arg' -> 'arg'
//            // '<column>->>'arg'
//
//            // StringBuilder r = new StringBuilder(property.toString());
//            StringBuilder r = new StringBuilder(property.toString()).append("::jsonb");
//            String[] arr = path.replaceAll("'", "").substring(2).split("\\.");
//            if (arr.length == 1) {
//                r.append("->>'").append(arr[0]).append("'");
//            } else {
//                r.append("->'");
//                r.append(String.join("'->'", Arrays.copyOf(arr, arr.length - 1)));
//                r.append("'->>'").append(arr[arr.length - 1]).append("'");
//            }
//            return r.toString();
//        case H2:
//            // path = '$.ports.usb' -> '$.ports.usb'
//            return com.sos.commons.hibernate.function.json.h2.Functions.NAME_JSON_VALUE + "(" + property + "," + path + ")";
//        default:
//            return NAME;
//        }
//    }
    
    
    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 3) {
            throw new QueryException("missing arguments", null, null);
        }
        String property = arguments.get(1).toString();
        String path = arguments.get(2).toString();
        ReturnType retType;

        switch (this.factory.getDbms()) {
        case MYSQL:
            retType = argument2ReturnType(arguments.get(0).toString());
            // path = '$.ports.usb' -> '$.ports.usb'
            String extract = "JSON_EXTRACT(" + property + "," + path + ")";
            if (retType.equals(ReturnType.SCALAR)) {
                sqlAppender.append("JSON_UNQUOTE(" + extract + ")");
            } else {
                sqlAppender.append(extract);
            }
            break;
        case MSSQL:
            retType = argument2ReturnType(arguments.get(0).toString());
            if (retType.equals(ReturnType.SCALAR)) {
                sqlAppender.append("JSON_VALUE(" + property + "," + path + ")");
            } else {
                sqlAppender.append("JSON_QUERY(" + property + "," + path + ")");
            }
            break;
        case ORACLE:
            // TODO
            // returning clob
            // ---- SELECT json_value(j, '$.bar' returning clob) FROM j
            // -------- available from 18c? 12c - ORA-40444 error ..;
            // ERROR ON ERROR
            // ---- SELECT json_value(j, '$.bar' returning varchar2(32000) ERROR ON ERROR) FROM j;
            retType = argument2ReturnType(arguments.get(0).toString());
            if (retType.equals(ReturnType.SCALAR)) {
                // tested with 18c - not support RETURNING CLOB - gets empty value - use VARCHAR2(32000)?
                sqlAppender.append("JSON_VALUE(" + property + "," + path + ")");
            } else {
                if (this.factory.getDatabaseMetaData().supportJsonReturningClob()) {
                    sqlAppender.append("JSON_QUERY(" + property + "," + path + " RETURNING CLOB)");
                } else {
                    sqlAppender.append("JSON_QUERY(" + property + "," + path + ")");
                }
            }
            break;
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
            sqlAppender.append(r.toString());
            break;
        case H2:
            // path = '$.ports.usb' -> '$.ports.usb'
            sqlAppender.append(com.sos.commons.hibernate.function.json.h2.Functions.NAME_JSON_VALUE + "(" + property + "," + path + ")");
            break;
        default:
            sqlAppender.append(NAME);
            break;
        }
    }

    private ReturnType argument2ReturnType(String arg) throws QueryException {
        try {
            return ReturnType.valueOf(arg.replaceAll("'", ""));
        } catch (Exception e) {
            throw new QueryException(String.format("[argument=%s]%s", arg, e.toString()), null, e);
        }
    }

    //TODO 6.4.5.Final
//    @Override
//    public boolean hasParenthesesIfNoArguments() {
//        return false;
//    }
//
//    @Override
//    public boolean hasArguments() {
//        return true;
//    }
//
//    @Override
//    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
//        return new StringType();
//    }

}
