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
import com.sos.commons.util.SOSString;

public class SOSHibernateJsonExists extends StandardSQLFunction {

    public static final String NAME = "SOS_JSON_EXISTS";

    // TODO full list: EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, LIKE;
    // the full list is already supported for Oracle JSON_EXISTS but not for Oracle REGEXP_LIKE
    public enum JsonOperator {
        EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, LIKE;
    }

    public enum JsonPathType {
        OBJECT, ARRAY;
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

    // TODO limited to 1 path piece
    public static String getOracleRegExExactSearch(String jsonPath, String search) {
        String jp = jsonPath.substring(2);// $.names -> names
        return "\"" + jp + "\":\\s*\\[.*\"" + patternQuote(search) + "\".*\\]";
    }

    public static String getOracleRegExLikeSearch(String jsonPath, String search) {
        String jp = jsonPath.substring(2);// $.names -> names
        return "\"" + jp + "\":\\s*\\[.*\".*" + patternQuote(SOSString.trim(search, "%")) + ".*\".*\\]";
    }

    // Patter.quote quoted to much ...
    private static String patternQuote(String val) {
        // " - because json
        String[] specialChars = { "\"", "\\", "^", "$", ".", "|", "?", "*", "+", "(", ")", "[", "]", "{", "}" };
        // return Arrays.stream(specialChars).reduce(val, (result, ch) -> result.replace(ch, "\\" + ch));
        for (String ch : specialChars) {
            val = val.replace(ch, "\\" + ch);
        }
        return val;
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param jsonPathType type of the jsonPath container(ARRAY/OBJECT)
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonPathType jsonPathType) {
        return getFunction(jsonColumn, jsonPath, jsonPathType, null, null, DEFAULT_CASE_SENSITIVITY);
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param jsonPathType type of the jsonPath container(ARRAY/OBJECT)
     * @param caseSensitivity
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonPathType jsonPathType,
            final JsonCaseSensitivity caseSensitivity) {
        return getFunction(jsonColumn, jsonPath, jsonPathType, null, null, caseSensitivity);
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param jsonPathType type of the jsonPath container(ARRAY/OBJECT)
     * @param operator
     * @param value
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonPathType jsonPathType, final JsonOperator operator,
            final Object value) {
        return getFunction(jsonColumn, jsonPath, jsonPathType, operator, value, DEFAULT_CASE_SENSITIVITY);
    }

    /** - Currently only for Oracle<br/>
     * TODO H2,MSSQL,MYSQL,PGSQL
     * 
     * @param jsonColumn
     * @param jsonPath
     * @param jsonPathType type of the jsonPath container(ARRAY/OBJECT)
     * @param operator
     * @param value
     * @param caseSensitivity
     * @return */
    public static String getFunction(final String jsonColumn, final String jsonPath, final JsonPathType jsonPathType, final JsonOperator operator,
            final Object value, final JsonCaseSensitivity caseSensitivity) {
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
        return new StringBuilder(NAME).append("('" + cs.name() + "',").append(jsonColumn).append(",'").append(jsonPath).append("','").append(
                jsonPathType.name()).append("',").append(op).append(",").append(val).append("," + valShouldBeQuoted + "," + valAsQueryParameterName
                        + ")").toString();
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
        JsonPathType jsonPathType = getPathType(arguments.get(3));
        JsonOperator operator = getOperator(arguments.get(4));

        SqlAstNode valueNode = arguments.get(5);
        String value = getLiteralValue(valueNode);
        boolean valueShouldBeQuoted = quoteValue(arguments.get(6));
        String valueAsQueryParameterName = getLiteralValue(arguments.get(7));// :my_param....
        String valueAsQueryParameterNameVarName = valueAsQueryParameterName == null ? null : "SOS_VAR_" + (valueAsQueryParameterName.substring(1))
                .toUpperCase();

        switch (this.factory.getDbms()) {
        case ORACLE:
            boolean useJsonExists = true;
            if (this.factory.getDatabaseMetaData().getOracle().getJson().fallbackToRegex()) {
                if (operator != null && (value != null || valueAsQueryParameterName != null)) {
                    useJsonExists = false;
                    if (JsonOperator.NOT_EQUALS.equals(operator)) {
                        sqlAppender.append("NOT ");
                    }
                    sqlAppender.append("REGEXP_LIKE(");
                    if (caseInsensitive) {
                        sqlAppender.append("lower(" + jsonColumn + ")");
                    } else {
                        sqlAppender.append(jsonColumn);
                    }
                    sqlAppender.append(",");

                    switch (operator) {
                    case EQUALS:
                        if (value == null) {
                            valueNode.accept(translator);
                        } else {
                            sqlAppender.append(getOracleRegExExactSearch(jsonPath, value));
                        }
                        break;
                    case LIKE:
                        if (value == null) {
                            valueNode.accept(translator);
                        } else {
                            sqlAppender.append(getOracleRegExLikeSearch(jsonPath, value));
                        }
                        break;
                    case GREATER_THAN:
                        break;
                    case GREATER_THAN_OR_EQUALS:
                        break;
                    case LESS_THAN:
                        break;
                    case LESS_THAN_OR_EQUALS:
                        break;
                    case NOT_EQUALS:
                        break;
                    default:
                        break;
                    }
                    sqlAppender.append(")");
                }
            }
            if (useJsonExists) {
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
                if (JsonPathType.ARRAY.equals(jsonPathType)) {
                    sqlAppender.append(jsonPath + "[*]");
                } else {
                    sqlAppender.append(jsonPath);
                }

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
            }
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

    private JsonPathType getPathType(SqlAstNode arg) {
        return JsonPathType.valueOf(getLiteralValue(arg));
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
