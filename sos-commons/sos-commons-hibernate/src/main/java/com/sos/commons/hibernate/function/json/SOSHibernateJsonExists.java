package com.sos.commons.hibernate.function.json;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.sqm.sql.internal.BasicValuedPathInterpretation;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.SOSHibernateFactory;

public class SOSHibernateJsonExists extends StandardSQLFunction {

    public static final String NAME = "SOS_JSON_EXISTS";

    public enum JsonOperator {
        EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, LIKE;
    }

    public enum JsonCaseSensitivity {
        SENSITIVE, INSENSITIVE
    }

    private static final JsonCaseSensitivity DEFAULT_CASE_SENSITIVITY = JsonCaseSensitivity.INSENSITIVE;

    private SOSHibernateFactory factory;

    public SOSHibernateJsonExists(SOSHibernateFactory factory) {
        super(NAME, StandardBasicTypes.BOOLEAN);
        this.factory = factory;
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath) {
        return getFunction(jsonColumn, jsonPath, null, null, DEFAULT_CASE_SENSITIVITY);
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param caseSensitivity
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonCaseSensitivity caseSensitivity) {
        return getFunction(jsonColumn, jsonPath, null, null, caseSensitivity);
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param operator
     * @param value
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonOperator operator, final Object value) {
        return getFunction(jsonColumn, jsonPath, operator, value, DEFAULT_CASE_SENSITIVITY);
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param operator
     * @param value
     * @param caseSensitivity
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonOperator operator, final Object value,
            final JsonCaseSensitivity caseSensitivity) {
        String val = null;
        String valShouldBeQuoted = null;
        String valAsQueryParameterName = null; // :my_param
        if (value == null) {
            val = null;
        } else {
            val = value.toString();
            if (val.startsWith(":")) {// :my_param
                valAsQueryParameterName = "'" + val + "'";
            } else {
                val = "'" + val + "'";
                valShouldBeQuoted = "'true'";
                if (value instanceof Boolean || value instanceof Number) {
                    valShouldBeQuoted = "'false'";
                }
            }
        }
        String op = operator == null ? null : "'" + operator.name() + "'";
        JsonCaseSensitivity cs = caseSensitivity == null ? DEFAULT_CASE_SENSITIVITY : caseSensitivity;
        return new StringBuilder(NAME).append("('" + cs.name() + "',").append(jsonColumn).append(",'").append(jsonPath).append("',").append(op)
                .append(",").append(val).append("," + valShouldBeQuoted + "," + valAsQueryParameterName + ")").toString();
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 7) {
            throw new QueryException("missing arguments", null, null);
        }

        boolean caseInsensitive = isCaseInsensitive(arguments.get(0));
        String jsonColumn = getColumn(arguments.get(1));
        String jsonPath = getLiteralValue(arguments.get(2));
        JsonOperator operator = getOperator(arguments.get(3));

        SqlAstNode valueNode = arguments.get(4);
        String value = getLiteralValue(valueNode);
        boolean valueShouldBeQuoted = quoteValue(arguments.get(5));
        String valueAsQueryParameterName = getLiteralValue(arguments.get(6));// :my_param....
        String valueAsQueryParameterNameVarName = valueAsQueryParameterName == null ? null : "SOS_VAR_" + (valueAsQueryParameterName.substring(1))
                .toUpperCase();

        switch (this.factory.getDbms()) {
        case ORACLE:
            // JSON_EXISTS(JSON_CONTENT, '$.workflowNames')
            // JSON_EXISTS(lower(JSON_CONTENT), '$.workflownames')
            sqlAppender.append("JSON_EXISTS(");
            if (caseInsensitive) {
                jsonPath = jsonPath.toLowerCase();
                if (value != null) {
                    value = value.toLowerCase();
                }
                sqlAppender.append("lower(" + jsonColumn + ")");
            } else {
                sqlAppender.append(jsonColumn);
            }
            sqlAppender.append(",'");
            sqlAppender.append(jsonPath);
            if (operator != null && (value != null || valueAsQueryParameterName != null)) {
                // JSON_EXISTS(JSON_CONTENT, '$.workflowNames?(@ == "My_Workflow")')";
                // -- JSON_EXISTS(lower(JSON_CONTENT), '$.workflownames?(@ == "my_workflow")')";
                // JSON_EXISTS(JSON_CONTENT, '$.workflowNames?(@ like "%My_Workflow%")')";
                // -- JSON_EXISTS(lower(JSON_CONTENT), '$.workflownames?(@ like "%my_workflow%")')";
                sqlAppender.append("?(@");
                sqlAppender.append(" " + renderOperator(Dbms.ORACLE, operator));
                if (value == null) {
                    sqlAppender.append(" $" + valueAsQueryParameterNameVarName);
                } else {
                    sqlAppender.append(" " + quote(value, valueShouldBeQuoted));
                }
                sqlAppender.append(")");
            }
            sqlAppender.append("'");
            if (valueAsQueryParameterName != null) {
                // JSON_EXISTS(lower(dic1_0."JSON_CONTENT"), '$.workflownames?(@ like $SOS_VAR_PARAM)' PASSING ? AS "SOS_VAR_PARAM")
                // -- PASSING ? is :my_param (i.e., parameter name specified in the query)
                sqlAppender.append(" PASSING ");
                valueNode.accept(translator);
                sqlAppender.append(" AS \"" + valueAsQueryParameterNameVarName + "\"");
            }
            sqlAppender.append(")");
            break;
        default:
            throw new IllegalArgumentException("[" + this.factory.getDbms() + "][" + NAME + "]not implemented yet");
        }
    }

    private String renderOperator(Dbms dbms, JsonOperator operator) {
        switch (dbms) {
        case ORACLE:
            switch (operator) {
            case GREATER_THAN:
                return ">";
            case GREATER_THAN_OR_EQUALS:
                return ">=";
            case LESS_THAN:
                return "<";
            case LESS_THAN_OR_EQUALS:
                return "<=";
            case LIKE:
                return "like";
            case NOT_EQUALS:
                return "!=";
            case EQUALS:
            default:
                return "==";
            }
        default:
            return null;
        }
    }

    private String quote(String value, boolean valueShouldBeQuoted) {
        return valueShouldBeQuoted ? "\"" + value + "\"" : value;
    }

    private String getColumn(SqlAstNode arg) {
        return ((BasicValuedPathInterpretation<?>) arg).getColumnReference().getExpressionText();
    }

    private JsonOperator getOperator(SqlAstNode arg) {
        String op = getLiteralValue(arg);
        return op == null ? null : JsonOperator.valueOf(op);
    }

    private boolean isCaseInsensitive(SqlAstNode arg) {
        String v = getLiteralValue(arg);
        JsonCaseSensitivity cs = v == null ? DEFAULT_CASE_SENSITIVITY : JsonCaseSensitivity.valueOf(v);
        return JsonCaseSensitivity.INSENSITIVE.equals(cs);
    }

    private boolean quoteValue(SqlAstNode arg) {
        try {
            String q = getLiteralValue(arg);
            return q == null ? true : Boolean.parseBoolean(q);
        } catch (Throwable e) {
            return true;
        }
    }

    private String getLiteralValue(SqlAstNode arg) {
        if (arg == null || !(arg instanceof QueryLiteral)) {
            return null;
        }
        return (String) (((QueryLiteral<?>) arg).getLiteralValue());
    }

}
