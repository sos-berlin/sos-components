package com.sos.commons.hibernate.function.json;

import java.util.Arrays;
import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;

public class SOSHibernateJsonValue extends StandardSQLFunction {

    public static final String NAME = "SOS_JSON_VALUE";

    public enum ReturnType {
        SCALAR, JSON
    }

    private SOSHibernateFactory factory;

    public SOSHibernateJsonValue(SOSHibernateFactory factory) {
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

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 3) {
            throw new QueryException("missing arguments", null, null);
        }

        ReturnType retType;
        switch (this.factory.getDbms()) {
        case MYSQL:
            retType = argument2ReturnType(arguments.get(0));
            // path = '$.ports.usb' -> '$.ports.usb'
            if (retType.equals(ReturnType.SCALAR)) {
                // JSON_UNQUOTE(JSON_EXTRACT(property,path))
                sqlAppender.append("JSON_UNQUOTE(JSON_EXTRACT(");
                arguments.get(1).accept(translator);
                sqlAppender.append(",");
                arguments.get(2).accept(translator);
                sqlAppender.append("))");
            } else {
                // JSON_EXTRACT(property,path)
                sqlAppender.append("JSON_EXTRACT(");
                arguments.get(1).accept(translator);
                sqlAppender.append(",");
                arguments.get(2).accept(translator);
                sqlAppender.append(")");
            }
            break;
        case MSSQL:
            retType = argument2ReturnType(arguments.get(0));
            if (retType.equals(ReturnType.SCALAR)) {
                // JSON_VALUE(property,path)
                sqlAppender.append("JSON_VALUE(");
                arguments.get(1).accept(translator);
                sqlAppender.append(",");
                arguments.get(2).accept(translator);
                sqlAppender.append(")");
            } else {
                // JSON_QUERY(property,path)
                sqlAppender.append("JSON_QUERY(");
                arguments.get(1).accept(translator);
                sqlAppender.append(",");
                arguments.get(2).accept(translator);
                sqlAppender.append(")");
            }
            break;
        case ORACLE:
            // RETURNING CLOB - available from 18 (c ?)
            // - Used with JSON_QUERY to retrieve JSON result longer than 4000 characters
            // -- If such case is not handled separately, Oracle will return null in this case,
            // --- because JSON_QUERY uses the return type VARCHAR2(4000) by default
            retType = argument2ReturnType(arguments.get(0));
            if (retType.equals(ReturnType.SCALAR)) {
                // OK without
                sqlAppender.append("JSON_VALUE(");
                arguments.get(1).accept(translator);
                sqlAppender.append(",");
                arguments.get(2).accept(translator);
                sqlAppender.append(")");
            } else {
                // JSON_QUERY(property,path <RETURNING CLOB>)
                sqlAppender.append("JSON_QUERY(");
                arguments.get(1).accept(translator);
                sqlAppender.append(",");
                arguments.get(2).accept(translator);
                if (this.factory.getDatabaseMetaData().supportJsonReturningClob()) {
                    // RETURNING NCLOB:
                    // - 18c-ORA-40449 invalid data type for return value
                    // - 21c-OK - TODO from which version is RETURNING NCLOB supported?
                    sqlAppender.append(" RETURNING CLOB");
                }// else - if result longer than 4000 will returns null ...
                sqlAppender.append(")");
            }
            break;
        case PGSQL:
            // path = '$.ports.usb' -> 'ports'->>'usb'
            // '<column>->'ports'->>'usb'
            // path = '$.arg' -> 'arg'
            // '<column>->>'arg'
            String path = getLiteralValue(arguments.get(2));
            String[] arr = path.replaceAll("'", "").substring(2).split("\\.");
            StringBuilder r = new StringBuilder();
            if (arr.length == 1) {
                r.append("->>'").append(arr[0]).append("'");
            } else {
                r.append("->'");
                r.append(String.join("'->'", Arrays.copyOf(arr, arr.length - 1)));
                r.append("'->>'").append(arr[arr.length - 1]).append("'");
            }
            // property::jsonb
            arguments.get(1).accept(translator);
            sqlAppender.append("::jsonb");
            sqlAppender.append(r.toString());
            break;
        case H2:
            // path = '$.ports.usb' -> '$.ports.usb'
            // SOS_JSON_VALUE(property,path)
            // does not use ReturnType(SCALAR,JSON) because the H2 NAME_JSON_VALUE function also returns a String value for the JSON objects
            sqlAppender.append(com.sos.commons.hibernate.function.json.h2.Functions.NAME_JSON_VALUE);
            sqlAppender.append("(");
            arguments.get(1).accept(translator);
            sqlAppender.append(",");
            arguments.get(2).accept(translator);
            sqlAppender.append(")");
            break;
        default:
            sqlAppender.append(NAME);
            break;
        }
    }

    private String getLiteralValue(SqlAstNode arg) {
        return ((QueryLiteral<?>) arg).getLiteralValue().toString();
    }

    private ReturnType argument2ReturnType(SqlAstNode arg) throws QueryException {
        try {
            return ReturnType.valueOf(getLiteralValue(arg));
        } catch (Exception e) {
            throw new QueryException(String.format("[argument=%s]%s", arg, e.toString()), null, e);
        }
    }

}
