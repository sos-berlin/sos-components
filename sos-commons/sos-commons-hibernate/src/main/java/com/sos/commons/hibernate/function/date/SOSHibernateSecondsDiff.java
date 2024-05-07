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
        super(NAME, StandardBasicTypes.INTEGER);
        this.factory = factory;
    }

    public static String getFunction(final String startTimeProperty, final String endTimeProperty) {
        return new StringBuilder(NAME).append("(").append(startTimeProperty).append(",").append(endTimeProperty).append(")").toString();
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments", null, null);
        }
        switch (this.factory.getDbms()) {
        case MYSQL:
            // TIMESTAMPDIFF(SECOND,startTime,endTime)
            sqlAppender.append("TIMESTAMPDIFF(SECOND,");
            arguments.get(0).accept(translator);
            sqlAppender.append(",");
            arguments.get(1).accept(translator);
            sqlAppender.append(")");
            break;
        case MSSQL:
            // DATEDIFF(SECOND,startTime,endTime)
            sqlAppender.append("DATEDIFF(SECOND,");
            arguments.get(0).accept(translator);
            sqlAppender.append(",");
            arguments.get(1).accept(translator);
            sqlAppender.append(")");
            break;
        case ORACLE:
            // ROUND(24*60*60*(endTime-startTime))
            sqlAppender.append("ROUND(24*60*60*(");
            arguments.get(1).accept(translator);
            sqlAppender.append("-");
            arguments.get(0).accept(translator);
            sqlAppender.append("))");
            break;
        case PGSQL:
            // CAST(EXTRACT(EPOCH FROM(endTime-startTime)) AS INTEGER)
            sqlAppender.append("CAST(EXTRACT(EPOCH FROM(");
            arguments.get(1).accept(translator);
            sqlAppender.append("-");
            arguments.get(0).accept(translator);
            sqlAppender.append(")) AS INTEGER)");
            break;
        case H2:
            // DATEDIFF(ss,startTime,endTime)
            sqlAppender.append("DATEDIFF(ss,");
            arguments.get(0).accept(translator);
            sqlAppender.append(",");
            arguments.get(1).accept(translator);
            sqlAppender.append(")");
            break;
        default:
            // (endTimeProperty-startTimeProperty)
            sqlAppender.append("(");
            arguments.get(1).accept(translator);
            sqlAppender.append("-");
            arguments.get(0).accept(translator);
            sqlAppender.append(")");
            break;
        }
    }
}
