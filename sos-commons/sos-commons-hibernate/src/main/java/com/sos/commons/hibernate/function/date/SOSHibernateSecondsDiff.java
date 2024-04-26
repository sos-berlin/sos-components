package com.sos.commons.hibernate.function.date;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;

public class SOSHibernateSecondsDiff extends StandardSQLFunction {

    public static final String NAME = "SOS_SECONDSDIFF";

    private SOSHibernateFactory factory;

    public SOSHibernateSecondsDiff(SOSHibernateFactory factory) {
        //TODO 6.4.5.Final
        super(NAME, StandardBasicTypes.INTEGER);
        this.factory = factory;
    }

    public static String getFunction(final String startTimeProperty, final String endTimeProperty) {
        return new StringBuilder(NAME).append("(").append(startTimeProperty).append(",").append(endTimeProperty).append(")").toString();
    }

    //TODO 6.4.5.Final
//    @SuppressWarnings("rawtypes")
//    @Override
//    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
//        if (arguments == null || arguments.size() < 2) {
//            throw new QueryException("missing arguments");
//        }
//        String startTimeProperty = arguments.get(0).toString();
//        String endTimeProperty = arguments.get(1).toString();
//
//        switch (this.factory.getDbms()) {
//        case MYSQL:
//            return "TIMESTAMPDIFF(SECOND," + startTimeProperty + "," + endTimeProperty + ")";
//        case MSSQL:
//            return "DATEDIFF(SECOND," + startTimeProperty + "," + endTimeProperty + ")";
//        case ORACLE:
//            return "ROUND(24*60*60*(" + endTimeProperty + "-" + startTimeProperty + "))";
//        case PGSQL:
//            return "CAST(EXTRACT(EPOCH FROM(" + endTimeProperty + "-" + startTimeProperty + ")) AS INTEGER)";
//        case H2:
//            return "DATEDIFF(ss," + startTimeProperty + "," + endTimeProperty + ")";
//        case SYBASE:
//            return "DATEDIFF(ss," + endTimeProperty + "," + startTimeProperty + ")";
//        default:
//            return "(" + endTimeProperty + "-" + startTimeProperty + ")";
//        }
//    }
    

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments", null, null);
        }
        String startTimeProperty = arguments.get(0).toString();
        String endTimeProperty = arguments.get(1).toString();
        
        switch (this.factory.getDbms()) {
        case MYSQL:
            sqlAppender.append("TIMESTAMPDIFF(SECOND," + startTimeProperty + "," + endTimeProperty + ")");
            break;
        case MSSQL:
            sqlAppender.append("DATEDIFF(SECOND," + startTimeProperty + "," + endTimeProperty + ")");
            break;
        case ORACLE:
            sqlAppender.append("ROUND(24*60*60*(" + endTimeProperty + "-" + startTimeProperty + "))");
            break;
        case PGSQL:
            sqlAppender.append("CAST(EXTRACT(EPOCH FROM(" + endTimeProperty + "-" + startTimeProperty + ")) AS INTEGER)");
            break;
        case H2:
            sqlAppender.append("DATEDIFF(ss," + startTimeProperty + "," + endTimeProperty + ")");
            break;
        case SYBASE:
            sqlAppender.append("DATEDIFF(ss," + endTimeProperty + "," + startTimeProperty + ")");
            break;
        default:
            sqlAppender.append("(" + endTimeProperty + "-" + startTimeProperty + ")");
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
}
