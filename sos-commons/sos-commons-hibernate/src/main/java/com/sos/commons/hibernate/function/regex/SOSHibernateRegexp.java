package com.sos.commons.hibernate.function.regex;

import java.util.List;
import java.util.regex.Pattern;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSString;

public class SOSHibernateRegexp extends StandardSQLFunction {

    public static final String NAME = "SOS_REGEXP";

    private SOSHibernateFactory factory;

    public SOSHibernateRegexp(SOSHibernateFactory factory) {
        //TODO 6.4.5.Final
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

    //TODO 6.4.5.Final
//    @SuppressWarnings("rawtypes")
//    @Override
//    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
//        if (arguments == null || arguments.size() < 2) {
//            throw new QueryException("missing arguments");
//        }
//        String property = arguments.get(0).toString();
//        String regexp = arguments.get(1).toString();
//        String mssqlRegexp = arguments.size() == 3 ? arguments.get(2).toString() : null;
//
//        switch (this.factory.getDbms()) {
//        case MYSQL:
//            // REGEXP_LIKE from MySQL 8.0
//            return property + " REGEXP " + regexp;
//        case MSSQL:
//            // TODO
//            String innRegexp = null;
//            String quote = "'";
//            if (mssqlRegexp == null) {
//                if (!SOSString.isEmpty(regexp) && !regexp.equals("?")) {
//                    regexp = regexp.replaceAll(Pattern.quote(".*"), "%");
//                    innRegexp = regexp.substring(1, regexp.length() - 1);
//                    if (!innRegexp.startsWith("^")) {
//                        innRegexp = "%" + innRegexp;
//                    }
//                    if (!innRegexp.endsWith("$")) {
//                        innRegexp = innRegexp + "%";
//                    }
//                } else {
//                    innRegexp = "?";
//                    quote = "";
//                }
//            } else {
//                innRegexp = mssqlRegexp.substring(1, mssqlRegexp.length() - 1);
//            }
//            return "(case when (" + property + " like " + quote + innRegexp + quote + ") then 1 else 0 end)";
//        case ORACLE:
//            return "(case when (REGEXP_LIKE(" + property + "," + regexp + ")) then 1 else 0 end)";
//        case PGSQL:
//            return "(case when (" + property + " ~ " + regexp + ") then 1 else 0 end)";
//        case H2:
//            return "REGEXP_LIKE(" + property + "," + regexp + ")";
//        default:
//            return NAME;
//        }
//    }
    
    
    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments", null, null);
        }
        String property = arguments.get(0).toString();
        String regexp = arguments.get(1).toString();
        String mssqlRegexp = arguments.size() == 3 ? arguments.get(2).toString() : null;

        switch (this.factory.getDbms()) {
        case MYSQL:
            // REGEXP_LIKE from MySQL 8.0
            sqlAppender.append(property + " REGEXP " + regexp);
            break;
        case MSSQL:
            // TODO
            String innRegexp = null;
            String quote = "'";
            if (mssqlRegexp == null) {
                if (!SOSString.isEmpty(regexp) && !regexp.equals("?")) {
                    regexp = regexp.replaceAll(Pattern.quote(".*"), "%");
                    innRegexp = regexp.substring(1, regexp.length() - 1);
                    if (!innRegexp.startsWith("^")) {
                        innRegexp = "%" + innRegexp;
                    }
                    if (!innRegexp.endsWith("$")) {
                        innRegexp = innRegexp + "%";
                    }
                } else {
                    innRegexp = "?";
                    quote = "";
                }
            } else {
                innRegexp = mssqlRegexp.substring(1, mssqlRegexp.length() - 1);
            }
            sqlAppender.append("(case when (" + property + " like " + quote + innRegexp + quote + ") then 1 else 0 end)");
            break;
        case ORACLE:
            sqlAppender.append("(case when (REGEXP_LIKE(" + property + "," + regexp + ")) then 1 else 0 end)");
            break;
        case PGSQL:
            sqlAppender.append("(case when (" + property + " ~ " + regexp + ") then 1 else 0 end)");
            break;
        case H2:
            sqlAppender.append("REGEXP_LIKE(" + property + "," + regexp + ")");
            break;
        default:
            sqlAppender.append(NAME);
            break;
        }
    }

    //TODO 6.4.5.Final
//    @Override
//    public boolean hasParenthesesIfNoArguments() {
//        return true;
//    }
//
//    @Override
//    public boolean hasArguments() {
//        return true;
//    }
//
//    @Override
//    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
//        return StandardBasicTypes.INTEGER;
//    }

    private static String quote(String regexp) {
        if (regexp == null) {
            return "'null'";
        }
        // : for parameter binding
        return regexp.startsWith(":") ? regexp : "'" + regexp + "'";
    }

}
