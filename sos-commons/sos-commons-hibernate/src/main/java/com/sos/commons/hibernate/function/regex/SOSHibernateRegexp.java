package com.sos.commons.hibernate.function.regex;

import java.util.List;
import java.util.regex.Pattern;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSString;

public class SOSHibernateRegexp extends StandardSQLFunction {

    public static final String NAME = "SOS_REGEXP";

    private SOSHibernateFactory factory;

    public SOSHibernateRegexp(SOSHibernateFactory factory) {
        super(NAME, StandardBasicTypes.INTEGER);
        this.factory = factory;
    }

    public static String getFunction(final String property, final String regexp) {
        return new StringBuilder(NAME).append("(").append(property).append(",").append(quote(regexp)).append(")=1").toString();
    }

    public static String getFunction(final String property, final String regexp, final String mssqlRegexp) {
        return new StringBuilder(NAME).append("(").append(property).append(",").append(quote(regexp)).append("," + quote(mssqlRegexp) + ")=1")
                .toString();
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments", null, null);
        }

        switch (this.factory.getDbms()) {
        case MYSQL:
            // REGEXP_LIKE from MySQL 8.0
            // property REGEXP regexp
            arguments.get(0).accept(translator);
            sqlAppender.append(" REGEXP ");
            arguments.get(1).accept(translator);
            break;
        case MSSQL:
            String regexp = getLiteralValue(arguments.get(1));
            String mssqlRegexp = arguments.size() == 3 ? getLiteralValue(arguments.get(2)) : null;

            String innRegexp = null;
            if (mssqlRegexp == null) {
                if (!SOSString.isEmpty(regexp) && !regexp.equals("?")) {
                    innRegexp = regexp.replaceAll(Pattern.quote(".*"), "%");
                    if (innRegexp.startsWith("^")) {
                        innRegexp = innRegexp.substring(1);
                    } else {
                        if (!innRegexp.startsWith("%")) {
                            innRegexp = "%" + innRegexp;
                        }
                    }
                    if (innRegexp.endsWith("$")) {
                        innRegexp = innRegexp.substring(0, innRegexp.length() - 1);
                    } else {
                        if (!innRegexp.endsWith("%")) {
                            innRegexp = innRegexp + "%";
                        }
                    }
                }
            } else {
                innRegexp = mssqlRegexp;
            }
            // (case when (property like quote + innRegexp + quote) then 1 else 0 end)
            sqlAppender.append("(case when (");
            arguments.get(0).accept(translator);
            sqlAppender.append(" like ");
            if (innRegexp == null) {// column, like :name
                arguments.get(1).accept(translator);
            } else {
                sqlAppender.append("'");
                sqlAppender.append(innRegexp);
                sqlAppender.append("'");
            }
            sqlAppender.append(") then 1 else 0 end)");
            break;
        case ORACLE:
            // (case when (REGEXP_LIKE(property,regexp)) then 1 else 0 end)
            sqlAppender.append("(case when (REGEXP_LIKE(");
            arguments.get(0).accept(translator);
            sqlAppender.append(",");
            arguments.get(1).accept(translator);
            sqlAppender.append(")) then 1 else 0 end)");
            break;
        case PGSQL:
            // (case when (property ~ regexp) then 1 else 0 end)
            sqlAppender.append("(case when (");
            arguments.get(0).accept(translator);
            sqlAppender.append(" ~ ");
            arguments.get(1).accept(translator);
            sqlAppender.append(") then 1 else 0 end)");
            break;
        case H2:
            // REGEXP_LIKE(property,regexp)
            sqlAppender.append("REGEXP_LIKE(");
            arguments.get(0).accept(translator);
            sqlAppender.append(",");
            arguments.get(1).accept(translator);
            sqlAppender.append(")");
            break;
        default:
            sqlAppender.append(NAME);
            break;
        }
    }

    private static String quote(String regexp) {
        if (regexp == null) {
            return "'null'";
        }
        // : for parameter binding
        return regexp.startsWith(":") ? regexp : "'" + regexp + "'";
    }

    private String getLiteralValue(SqlAstNode arg) {
        if (arg instanceof QueryLiteral) {
            return ((QueryLiteral<?>) arg).getLiteralValue().toString();
        }
        return null;
    }
}
